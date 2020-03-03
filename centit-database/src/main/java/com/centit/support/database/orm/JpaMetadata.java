package com.centit.support.database.orm;

import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.compiler.Lexer;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.SimpleTableReference;
import com.centit.support.database.utils.FieldType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by codefan on 17-8-27.
 */
@SuppressWarnings("unused")
public abstract class JpaMetadata {

    private static final Logger logger = LoggerFactory.getLogger(JpaMetadata.class);
    private static final ConcurrentHashMap<String, TableMapInfo> ORM_JPA_METADATA_CLASSPATH =
        new ConcurrentHashMap<>(100);
    private static final ConcurrentHashMap<String, TableMapInfo> ORM_JPA_METADATA_CLASSNAME =
        new ConcurrentHashMap<>(100);
    private static final ConcurrentHashMap<String, TableMapInfo> ORM_JPA_METADATA_TABLENAME =
        new ConcurrentHashMap<>(100);

    private JpaMetadata() {
        throw new IllegalAccessError("Utility class");
    }

    public static TableMapInfo fetchTableMapInfo(Class<?> type) {
        String className = type.getName();
        TableMapInfo mapInfo = ORM_JPA_METADATA_CLASSPATH.get(className);
        if (mapInfo == null) {
            mapInfo = obtainMapInfoFromClass(type);
            if (mapInfo != null) {
                ORM_JPA_METADATA_CLASSPATH.put(className, mapInfo);
                ORM_JPA_METADATA_TABLENAME.put(mapInfo.getTableName(), mapInfo);
                ORM_JPA_METADATA_CLASSNAME.put(/*type.getSimpleName()*/
                    className.substring(className.lastIndexOf(".") + 1), mapInfo);
            }
        }
        return mapInfo;
    }

    /**
     * 将属性名称转换为字段名称
     *
     * @param type         类型
     * @param propertyName 属性名称
     * @return 字段名称
     */
    public static String translatePropertyNameToColumnName(Class<?> type, String propertyName) {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(type);
        if (mapInfo == null) {
            return propertyName;
        }
        SimpleTableField field = mapInfo.findFieldByName(propertyName);
        return field == null ? propertyName : field.getColumnName();
    }

    /**
     * 将属性名称转换为字段名称;
     * 这里有一个问题就是这个类一定是已经扫描过得，不然会找不到这个对象
     *
     * @param propertyName 表名或者类名.属性名称
     * @param tableAlias   表的笔名
     * @return 字段名称
     */
    public static String translatePropertyNameToColumnName(String propertyName, String tableAlias) {
        int n = propertyName.indexOf('.');
        if (n < 0) {
            return propertyName;
        }
        String tableName = propertyName.substring(0, n);
        String fieldName = propertyName.substring(n + 1);

        TableMapInfo mapInfo = ORM_JPA_METADATA_CLASSNAME.get(tableName);
        if (mapInfo == null) {
            mapInfo = ORM_JPA_METADATA_TABLENAME.get(tableName);
        }
        if (mapInfo == null) {
            return propertyName;
        }

        tableName = mapInfo.getTableName();

        SimpleTableField field = mapInfo.findFieldByName(propertyName);

        if (field != null) {
            fieldName = field.getColumnName();
        }

        if (StringUtils.isNotBlank(tableAlias)) {
            return tableAlias + "." + fieldName;
        }

        return tableName + "." + fieldName;
    }


    /**
     * 将属性名称转换为字段名称;
     * 这里有一个问题就是这个类一定是已经扫描过得，不然会找不到这个对象
     *
     * @param propertyName 表名或者类名.属性名称
     * @return 字段名称
     */
    public static String translatePropertyNameToColumnName(String propertyName) {
        return translatePropertyNameToColumnName(propertyName, null);
    }

    private static void obtainField(SimpleTableField column, Class<?> objType, Field field) {
        column.setObjectField(field);
        column.setObjectGetFieldValueFunc(ReflectionOpt.getGetterMethod(objType, field.getType(), field.getName()));
        column.setObjectSetFieldValueFunc(ReflectionOpt.getSetterMethod(objType, field.getType(), field.getName()));
    }

    private static void obtainColumn(SimpleTableField column, Field field) {
        Column colInfo = field.getAnnotation(Column.class);
        column.setColumnName(colInfo.name());
        column.setFieldType(FieldType.mapToFieldType(field.getType()));
        column.setPropertyName(field.getName());
        column.setMaxLength(colInfo.length());
        column.setScale(colInfo.scale());
        column.setPrecision(colInfo.precision());
        column.setObjectField(field);
    }

    private static SimpleTableField obtainColumnFromField(Class<?> objType, Field field) {
        SimpleTableField column = new SimpleTableField();
        obtainColumn(column, field);
        obtainField(column, objType, field);
        return column;
    }

    public static TableMapInfo obtainMapInfoFromClass(Class<?> objClass) {

        if (!objClass.isAnnotationPresent(Table.class))
            return null;
        Table tableInfo = objClass.getAnnotation(Table.class);
        TableMapInfo mapInfo = new TableMapInfo();
        mapInfo.setTableName(tableInfo.name());
        mapInfo.setSchema(tableInfo.schema());
        Class<?> objType = objClass;
        int nCascade = 0;
        while(nCascade< 5 && objType != Object.class) {
            nCascade ++;
            Field[] objFields = objType.getDeclaredFields();
            for (Field field : objFields) {
                if (field.isAnnotationPresent(Column.class)) {
                    SimpleTableField column = obtainColumnFromField(objType, field);
                    boolean isPK = field.isAnnotationPresent(Id.class);
                    column.setPrimaryKey(isPK);
                    boolean isLazy = false;
                    if (!isPK && field.isAnnotationPresent(Basic.class)) {
                        Basic colBasic = field.getAnnotation(Basic.class);
                        isLazy = colBasic.fetch() == FetchType.LAZY;
                    }
                    column.setLazyFetch(isLazy);
                    mapInfo.addColumn(column);
                    if (field.isAnnotationPresent(ValueGenerator.class)) {
                        ValueGenerator valueGenerator = field.getAnnotation(ValueGenerator.class);
                        mapInfo.addValueGenerator(
                            column.getPropertyName(),
                            valueGenerator);
                    }
                    if (field.isAnnotationPresent(OrderBy.class)) {
                        OrderBy orderBy = field.getAnnotation(OrderBy.class);
                        mapInfo.appendOrderBy(column, orderBy.value());
                    }
                } else if (field.isAnnotationPresent(EmbeddedId.class)) {
                    EmbeddedId embeddedId = field.getAnnotation(EmbeddedId.class);
                    mapInfo.setEmbeddedId(true);
                    mapInfo.setPkName(field.getName());
                    SimpleTableField pkColumn = new SimpleTableField();
                    obtainField(pkColumn, objType, field);
                    mapInfo.setEmbeddedIdField(pkColumn);

                    for (Field idField : field.getType().getDeclaredFields()) {

                        if (idField.isAnnotationPresent(Column.class)) {
                            SimpleTableField column = obtainColumnFromField(field.getType(), idField);
                            column.setPrimaryKey(true);
                            mapInfo.addColumn(column);
                            if (idField.isAnnotationPresent(ValueGenerator.class)) {
                                ValueGenerator valueGenerator = idField.getAnnotation(ValueGenerator.class);
                                mapInfo.addValueGenerator(
                                    column.getPropertyName(),
                                    valueGenerator);
                            }
                            if (idField.isAnnotationPresent(OrderBy.class)) {
                                OrderBy orderBy = idField.getAnnotation(OrderBy.class);
                                mapInfo.appendOrderBy(column, orderBy.value());
                            }
                        }
                    }
                } else { /*if ( field.isAnnotationPresent(OneToOne.class)
                    || field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToOne.class)
                    || field.isAnnotationPresent(ManyToMany.class))*/
                    Class targetClass = null;

                    if (field.isAnnotationPresent(OneToOne.class)) {
                        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
                        targetClass = oneToOne.targetEntity();
                        if (/*targetClass ==null || */targetClass.equals(void.class)) {
                            targetClass = field.getType();
                        }
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                        targetClass = manyToOne.targetEntity();
                        if (/*targetClass ==null || */targetClass.equals(void.class)) {
                            targetClass = field.getType();
                        }
                    } else if (field.isAnnotationPresent(OneToMany.class)) {
                        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
                        targetClass = oneToMany.targetEntity();
                        if (/*targetClass ==null || */targetClass.equals(void.class)) {
                            Type t = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            if (t instanceof Class) {
                                targetClass = (Class<?>) t;
                            }
                        }
                    } else if (field.isAnnotationPresent(ManyToMany.class)) {
                        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
                        targetClass = manyToMany.targetEntity();
                        if (/*targetClass ==null ||*/ targetClass.equals(void.class)) {
                            Type t = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            if (t instanceof Class) {
                                targetClass = (Class<?>) t;
                            }
                        }
                    }

                    if (targetClass != null && !targetClass.equals(void.class)) {
                        SimpleTableReference reference = new SimpleTableReference();
                        reference.setTargetEntityType(targetClass);
                        boolean haveJoinColumns = false;
                        if (field.isAnnotationPresent(JoinColumn.class)) {
                            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                            reference.setReferenceName(field.getName());
                            reference.setReferenceFieldType(field.getType());
                            reference.addReferenceColumn(joinColumn.name(), joinColumn.referencedColumnName());
                            haveJoinColumns = true;
                        } else if (field.isAnnotationPresent(JoinColumns.class)) {
                            JoinColumns joinColumns = field.getAnnotation(JoinColumns.class);
                            reference.setReferenceName(field.getName());
                            reference.setReferenceFieldType(field.getType());
                            for (JoinColumn joinColumn : joinColumns.value()) {
                                reference.addReferenceColumn(joinColumn.name(), joinColumn.referencedColumnName());
                            }
                            haveJoinColumns = true;
                        }

                        if (haveJoinColumns) {
                            reference.setObjectField(field);
                            reference.setObjectGetFieldValueFunc(ReflectionOpt.getGetterMethod(objType, field.getType(), field.getName()));
                            reference.setObjectSetFieldValueFunc(ReflectionOpt.getSetterMethod(objType, field.getType(), field.getName()));
                            mapInfo.addReference(reference);
                        }
                    }
                }
            }
            //获取父类
            objType = objType.getSuperclass();
        }
        //先 注册一下，避免 交叉引用时 死循环
        ORM_JPA_METADATA_CLASSPATH.put(objClass.getName(), mapInfo);
        // 整理 引用字段 ; 如果引用是字段名 就整理为 属性名
        if (mapInfo.getReferences() != null && mapInfo.getReferences().size() > 0) {
            Set<SimpleTableReference> errorRef = new HashSet<>(mapInfo.getReferences().size());
            for (SimpleTableReference reference : mapInfo.getReferences()) {
                TableMapInfo refTalbeInfo = fetchTableMapInfo(reference.getTargetEntityType());
                if (refTalbeInfo == null) {
                    errorRef.add(reference);
                    continue;
                }
                Set<String> keySet = new HashSet<>(reference.getReferenceColumns().keySet());
                for (String keyName : keySet) {
                    SimpleTableField field = mapInfo.findFieldByName(keyName);
                    SimpleTableField refField = refTalbeInfo.findFieldByName(reference.getReferenceColumns().get(keyName));
                    if (field == null || refField == null) {
                        errorRef.add(reference);
                        break;
                    }
                    // 如果 keyName 是字段名 不是属性名，这时会转换
                    if (!keyName.equals(field.getPropertyName())) {
                        reference.getReferenceColumns().remove(keyName);
                    }
                    reference.addReferenceColumn(field.getPropertyName(), refField.getPropertyName());
                }
            }

            if (errorRef.size() > 0) {
                mapInfo.getReferences().removeAll(errorRef);
            }
        }
        return mapInfo;
    }

    /**
     * 将sql语句中的属性名 替换为 数据库中表的字段名
     *
     * @param mapInfo 数据库表和对象的映射关系信息
     * @param sql     带有属性名的sql语句
     * @param alias   表的别名
     * @return 转换后的 sql语句
     */
    public static String translateSqlPropertyToColumn(TableMapInfo mapInfo, String sql, String alias) {
        StringBuilder sqlb = new StringBuilder();
        Lexer lex = new Lexer(sql, Lexer.LANG_TYPE_SQL);
        boolean needTranslate = true;
        int prePos = 0;
        int preWordPos = 0;
        String aWord = lex.getAWord();
        boolean addAlias = StringUtils.isNotBlank(alias);
        //skeep to |
        if ("[".equals(aWord)) {
            aWord = lex.getAWord();
            while (aWord != null && !"".equals(aWord) && !"|".equals(aWord)) {
                if ("(".equals(aWord)) {
                    lex.seekToRightBracket();
                }
                aWord = lex.getAWord();
            }
        }

        while (aWord != null && !"".equals(aWord)) {
            if ("select".equalsIgnoreCase(aWord) || "from".equalsIgnoreCase(aWord)
                /* || "group".equalsIgnoreCase(aWord) || "order".equalsIgnoreCase(aWord)*/) {
                needTranslate = false;
            } else if ("where".equalsIgnoreCase(aWord)) {
                needTranslate = true;
            }

            if (!needTranslate) {
                preWordPos = lex.getCurrPos();
                aWord = lex.getAWord();
                continue;
            }

            if (":".equals(aWord)) {
                lex.getAWord(); // 跳过参数
                preWordPos = lex.getCurrPos();
                aWord = lex.getAWord();
            }

            if (Lexer.isLabel(aWord)) {
                SimpleTableField col = mapInfo.findFieldByName(aWord);
                if (col != null) {
                    if (preWordPos > prePos) {
                        sqlb.append(sql.substring(prePos, preWordPos));
                    }
                    sqlb.append(addAlias ? (" " + alias + ".") : " ").append(col.getColumnName());
                    prePos = lex.getCurrPos();
                }
            }
            preWordPos = lex.getCurrPos();
            aWord = lex.getAWord();
        }
        sqlb.append(sql.substring(prePos));
        return sqlb.toString();
    }
}
