package com.centit.support.database.orm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.algorithm.*;
import com.centit.support.common.LeftRightPair;
import com.centit.support.common.ObjectException;
import com.centit.support.compiler.VariableFormula;
import com.centit.support.database.jsonmaptable.GeneralJsonObjectDao;
import com.centit.support.database.jsonmaptable.JsonObjectDao;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.FieldType;
import com.centit.support.database.utils.PersistenceException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-8-27.
 *
 * @author codefan@sina.com
 */
@SuppressWarnings("unused")
public abstract class OrmUtils {
    private static final String DNS1123 = "0123456789qwertyuiopasdfghjklzxcvbnm";
    private OrmUtils() {
        throw new IllegalAccessError("Utility class");
    }

    private static void putResultSetObjectToField(Object object, TableMapInfo mapInfo, SimpleTableField field,
                                                  Object newValue)
        throws IOException {
        if(field==null) return;
        if (newValue instanceof Clob) {
            String sValue = DatabaseAccess.fetchClobString((Clob) newValue);
            if (FieldType.JSON_OBJECT.equals(field.getFieldType())) {
                Class<?> clazz = field.getJavaType();
                // 这个地方可能会出现类型不兼容的问题
                if (JSON.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
                    mapInfo.setObjectFieldValue(object, field, JSON.parse(sValue));
                } else {
                    mapInfo.setObjectFieldValue(object, field, JSON.parseObject(sValue, clazz));
                }
            } else {
                mapInfo.setObjectFieldValue(object, field, sValue);
            }
        } else if (newValue instanceof Blob) {
            mapInfo.setObjectFieldValue(object, field,
                DatabaseAccess.fetchBlobBytes((Blob) newValue));
        } else {
            if (FieldType.JSON_OBJECT.equals(field.getFieldType())) {
                Class<?> clazz = field.getJavaType();
                if (JSON.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
                    newValue = JSON.parse(StringBaseOpt.castObjectToString(newValue));
                } else {
                    newValue = JSON.parseObject(
                        StringBaseOpt.castObjectToString(newValue), clazz);
                }
                mapInfo.setObjectFieldValue(object, field, newValue);
            } else {
                mapInfo.setObjectFieldValue(object, field, newValue);
            }
        }
    }

    private static <T> T makeObjectValueByGenerator(T object, TableMapInfo mapInfo,
                                                    JsonObjectDao sqlDialect, GeneratorTime generatorTime)
        throws SQLException, IOException {
        List<LeftRightPair<String, ValueGenerator>> valueGenerators = mapInfo.getValueGenerators();
        if (valueGenerators == null || valueGenerators.size() < 1)
            return object;
        for (LeftRightPair<String, ValueGenerator> ent : valueGenerators) {
            ValueGenerator valueGenerator = ent.getRight();
            if (valueGenerator.occasion().matchTime(generatorTime)) {
                SimpleTableField field = mapInfo.findFieldByName(ent.getLeft());
                Object fieldValue = mapInfo.getObjectFieldValue(object, field);
                if (fieldValue == null || valueGenerator.condition() == GeneratorCondition.ALWAYS) {
                    switch (valueGenerator.strategy()) {
                        case UUID:
                            mapInfo.setObjectFieldValue(object, field, UuidOpt.getUuidAsString32());
                            break;
                        case UUID22:
                            mapInfo.setObjectFieldValue(object, field, UuidOpt.getUuidAsString22());
                            break;
                        case SEQUENCE: //序列名称 + 前缀 + 长度 + 中间补空字符串
                            //GeneratorTime.READ 读取数据时不能用 SEQUENCE 生成值
                            if (sqlDialect != null) {
                                String genValue = valueGenerator.value();
                                String[] params = genValue.split(":");
                                Long seqNo = sqlDialect.getSequenceNextValue(params[0]);
                                if (params.length == 1) {
                                    mapInfo.setObjectFieldValue(object, field, seqNo);
                                } else {
                                    if (params.length > 3) {
                                        mapInfo.setObjectFieldValue(object, field, StringBaseOpt.midPad(seqNo.toString(),
                                            NumberBaseOpt.castObjectToInteger(params[2], 1),
                                            params[1], params[3]));
                                    } else /*if (params.length > 1)*/ {
                                        mapInfo.setObjectFieldValue(object, field, params[1] + /*String.valueOf*/seqNo);
                                    }
                                }
                            }
                            break;
                        case CONSTANT:
                            mapInfo.setObjectFieldValue(object, field, valueGenerator.value());
                            break;
                        case FUNCTION:
                            mapInfo.setObjectFieldValue(object, field,
                                VariableFormula.calculate(valueGenerator.value(), object));
                            break;
                        case SERIAL_NO: {
                            String genValue = valueGenerator.value();
                            int n = genValue.indexOf(':');
                            if (n > 0 && sqlDialect != null) {
                                String seq = genValue.substring(0, n);
                                Long seqNo = sqlDialect.getSequenceNextValue(seq);
                                JSONObject json = (JSONObject) JSON.toJSON(object);
                                json.put("seqNo", seqNo);
                                mapInfo.setObjectFieldValue(object, field,
                                    VariableFormula.calculate(
                                        genValue.substring(n + 1), object));
                            }
                        }
                        break;
                        case RANDOM_ID: {
                            String genValue = valueGenerator.value();
                            String[] params = genValue.split(":");
                            if (params.length > 0 && sqlDialect != null) {
                                String prefix = params.length > 1 ? params[1] : "";
                                int len = NumberBaseOpt.castObjectToInteger(params[0], 23);
                                if (len > 22) {
                                    mapInfo.setObjectFieldValue(object, field, prefix + UuidOpt.getUuidAsString22());
                                } else /*if (sqlDialect!=null)*/ {
                                    /*if (mapInfo.countPkColumn() != 1 || !field.isPrimaryKey()) {
                                        throw new ObjectException(PersistenceException.ORM_METADATA_EXCEPTION,
                                            "主键生成规则RANDOM_ID只能用于单主键表中！");
                                    }*/
                                    for (int i = 0; i < 100; i++) {
                                        String no = prefix + UuidOpt.getUuidAsString22().substring(0, len);
                                        //检查唯一属性是否冲突
                                        if(NumberBaseOpt.castObjectToInteger(
                                            DatabaseAccess.fetchScalarObject(
                                                sqlDialect.findObjectsBySql("select count(*) hasId from " + mapInfo.getTableName()
                                                    + " where " + field.getColumnName() + " = ?", new Object[]{no})), 0) == 0) {
                                            mapInfo.setObjectFieldValue(object, field, no);
                                            break;// for
                                        }
                                    }
                                }
                            }

                        }
                        case RANDOM_LOW_STRING_ID: {
                            String genValue = valueGenerator.value();
                            String[] params = genValue.split(":");
                            if (params.length > 0 && sqlDialect != null) {
                                String prefix = params.length > 1 ? params[1] : "";
                                int len = NumberBaseOpt.castObjectToInteger(params[0], 10);
                                for (int i = 0; i < 100; i++) {
                                    String no = prefix + RandomStringUtils.random(len, DNS1123);
                                    //检查唯一属性是否冲突
                                    int nHasId = NumberBaseOpt.castObjectToInteger(
                                        DatabaseAccess.fetchScalarObject(
                                            sqlDialect.findObjectsBySql("select count(*) hasId from " + mapInfo.getTableName()
                                                + " where " + field.getColumnName() + " = ?", new Object[]{no})), 0);
                                    if (nHasId == 0) {
                                        mapInfo.setObjectFieldValue(object, field, no);
                                        break;// for
                                    }
                                }
                            }
                        }
                        break;// case
                        // 1577808000000L 2020-1-1 00:00:00
                        case TIME_SEQUENCE:
                            mapInfo.setObjectFieldValue(object, field,
                                ((System.currentTimeMillis() - 1577808000000L) * 10000)
                                + Math.round(Math.random() * 10000));
                            break;
                        case SUB_ORDER: {
                            int pkCount = mapInfo.countPkColumn();
                            if (pkCount < 2 || !field.isPrimaryKey() /*|| filed.getFieldType()*/) {
                                throw new ObjectException(PersistenceException.ORM_METADATA_EXCEPTION,
                                    "主键生成规则SUB_ORDER必须用于符合主键表中，并且只能用于整型字段！");
                            }
                            StringBuilder sqlBuilder = new StringBuilder("select max(");
                            sqlBuilder.append(field.getColumnName())
                                .append(" ) as maxOrder from ")
                                .append(mapInfo.getTableName())
                                .append(" where ");
                            int pki = 0;
                            Object[] pkValues = new Object[pkCount - 1];
                            for (SimpleTableField col : mapInfo.getColumns()) {
                                if (col.isPrimaryKey() &&
                                    !StringUtils.equals(col.getPropertyName(), field.getPropertyName())) {
                                    if (pki > 0) {
                                        sqlBuilder.append(" and ");
                                    }
                                    sqlBuilder.append(col.getColumnName()).append(" = ?");
                                    pkValues[pki] = mapInfo.getObjectFieldValue(object, col);
                                    pki++;
                                }
                            }
                            Long pkSubOrder = NumberBaseOpt.castObjectToLong(
                                DatabaseAccess.fetchScalarObject(
                                    sqlDialect.findObjectsBySql(sqlBuilder.toString(), pkValues)), 0L);
                            mapInfo.setObjectFieldValue(object, field, pkSubOrder + 1);
                        }
                        break;
                    }// switch
                } // if
            } // if
        } // for
        return object;
    }

    public static <T> T prepareObjectForInsert(T object, TableMapInfo mapInfo, JsonObjectDao sqlDialect)
        throws SQLException, IOException {
        return makeObjectValueByGenerator(object, mapInfo, sqlDialect, GeneratorTime.NEW);
    }

    public static <T> T prepareObjectForUpdate(T object, TableMapInfo mapInfo, JsonObjectDao sqlDialect)
        throws SQLException, IOException {
        return makeObjectValueByGenerator(object, mapInfo, sqlDialect, GeneratorTime.UPDATE);
    }

    public static <T> T prepareObjectForMerge(T object, TableMapInfo mapInfo, JsonObjectDao sqlDialect)
        throws SQLException, IOException {
        Map<String, Object> objectMap = OrmUtils.fetchObjectDatabaseField(object, mapInfo);
        if (!GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo, objectMap)) {
            return makeObjectValueByGenerator(object, mapInfo, sqlDialect, GeneratorTime.NEW);
        } else {
            return makeObjectValueByGenerator(object, mapInfo, sqlDialect, GeneratorTime.UPDATE);
        }
    }

    public static Map<String, Object> fetchObjectField(Object object) {
        if (object instanceof Map) {
            return (Map<String, Object>) object;
        }
        // 这个地方为什么 不用 JsonObject.toJSONObject
        Field[] objFields = object.getClass().getDeclaredFields();
        Map<String, Object> fields = new HashMap<>(objFields.length * 2);
        for (Field field : objFields) {
            Object value = ReflectionOpt.forceGetFieldValue(object, field);
            fields.put(field.getName(), value);
        }
        return fields;
    }

    public static Map<String, Object> fetchObjectDatabaseField(Object object, TableMapInfo tableInfo) {
        List<SimpleTableField> tableFields = tableInfo.getColumns();
        if (tableFields == null) {
            return null;
        }
        Map<String, Object> fields = new HashMap<>(tableFields.size() + 6);
        for (SimpleTableField column : tableFields) {
            Object value = tableInfo.getObjectFieldValue(object, column);
            //ReflectionOpt.getFieldValue(object, column.getPropertyName());
            if (value != null) {
                if (FieldType.BOOLEAN.equals(column.getFieldType())) {
                    value = BooleanBaseOpt.castObjectToBoolean(value, false) ?
                        BooleanBaseOpt.ONE_CHAR_TRUE : BooleanBaseOpt.ONE_CHAR_FALSE;
                } /*else if(FieldType.JSON_OBJECT.equals(column.getFieldType())){
                    value = JSON.toJSONString(value);
                }*/
                fields.put(column.getPropertyName(), value);
            }
        }
        return fields;
    }

    private static <T> T insideFetchFieldsFormResultSet(ResultSet rs, T object, TableMapInfo mapInfo)
        throws SQLException, IOException {
        ResultSetMetaData resMeta = rs.getMetaData();
        int fieldCount = resMeta.getColumnCount();
        for (int i = 1; i <= fieldCount; i++) {
            String columnName = resMeta.getColumnName(i);
            SimpleTableField filed = mapInfo.findFieldByColumn(columnName);
            if (filed != null) {
                putResultSetObjectToField(object, mapInfo, filed, rs.getObject(i));
            }
        }
        return makeObjectValueByGenerator(object, mapInfo, null, GeneratorTime.READ);
    }

    private static <T> T insideFetchFieldsFormResultSet(ResultSet rs, T object, TableMapInfo mapInfo,
                                                        TableField[] fields)
        throws SQLException, IOException {
        int fieldCount = rs.getMetaData().getColumnCount();
        if (fieldCount > fields.length) {
            fieldCount = fields.length;
        }
        for (int i = 0; i < fieldCount; i++) {
            putResultSetObjectToField(object, mapInfo, (SimpleTableField) fields[i], rs.getObject(i + 1));
        }
        return makeObjectValueByGenerator(object, mapInfo, null, GeneratorTime.READ);
    }

    static <T> T fetchObjectFormResultSet(ResultSet rs, Class<T> clazz, TableField[] fields)
        throws SQLException, IllegalAccessException, InstantiationException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(clazz);
        if (mapInfo == null)
            return null;
        if (rs.next()) {
            return insideFetchFieldsFormResultSet(rs, clazz.newInstance(), mapInfo, fields);
        } else {
            return null;
        }
    }

    static <T> T fetchFieldsFormResultSet(ResultSet rs, T object, TableMapInfo mapInfo, SimpleTableField[] fields)
        throws SQLException, IOException {
        if (rs.next()) {
            object = insideFetchFieldsFormResultSet(rs, object, mapInfo, fields);
        }
        return object;
    }

    static <T> T fetchObjectFormResultSet(ResultSet rs, Class<T> clazz)
        throws SQLException, IllegalAccessException, InstantiationException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(clazz);
        if (mapInfo == null)
            return null;
        if (rs.next()) {
            return insideFetchFieldsFormResultSet(rs, clazz.newInstance(), mapInfo);
        } else {
            return null;
        }
    }

    static <T> T fetchFieldsFormResultSet(ResultSet rs, T object, TableMapInfo mapInfo)
        throws SQLException, IOException {
        if (rs.next()) {
            object = insideFetchFieldsFormResultSet(rs, object, mapInfo);
        }
        return object;
    }

    static <T> List<T> fetchObjectListFormResultSet(ResultSet rs, Class<T> clazz, TableField[] fields)
        throws SQLException, IllegalAccessException, InstantiationException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(clazz);
        if (mapInfo == null)
            return null;
        int fieldCount = rs.getMetaData().getColumnCount();
        if (fieldCount > fields.length) {
            fieldCount = fields.length;
        }

        List<T> listObj = new ArrayList<>();
        while (rs.next()) {
            T object = clazz.newInstance();
            for (int i = 0; i < fieldCount; i++) {
                putResultSetObjectToField(object, mapInfo, (SimpleTableField) fields[i], rs.getObject(i + 1));
            }
            listObj.add(makeObjectValueByGenerator(object, mapInfo, null, GeneratorTime.READ));
        }
        return listObj;
    }

    static <T> List<T> fetchObjectListFormResultSet(ResultSet rs, Class<T> clazz)
        throws SQLException, IllegalAccessException, InstantiationException, IOException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(clazz);
        if (mapInfo == null)
            return null;
        ResultSetMetaData resMeta = rs.getMetaData();
        int fieldCount = resMeta.getColumnCount();
        SimpleTableField[] fields = new SimpleTableField[fieldCount + 1];
        for (int i = 1; i <= fieldCount; i++) {
            String columnName = resMeta.getColumnName(i);
            fields[i] = mapInfo.findFieldByColumn(columnName);
        }

        List<T> listObj = new ArrayList<>();
        while (rs.next()) {
            T object = clazz.newInstance();
            for (int i = 1; i <= fieldCount; i++) {
                if (fields[i] != null) {
                    putResultSetObjectToField(object, mapInfo, fields[i], rs.getObject(i));
                }
            }
            listObj.add(makeObjectValueByGenerator(object, mapInfo, null, GeneratorTime.READ));
        }
        return listObj;
    }

}
