package com.centit.support.database.metadata;

import com.centit.support.common.JavaBeanField;
import com.centit.support.database.orm.JpaMetadata;
import com.centit.support.database.orm.TableMapInfo;
import com.centit.support.database.utils.FieldType;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SimpleTableReference implements TableReference{

    private String parentTableName;
    private String tableName;
    private String referenceName;
    private String referenceCode;
    // 父表属性 类型
    private Class<?> referenceFieldType;
    // 字表类型 和 父表属性类型不一定相等， 父表属性类型可能是 List Set
    private Class<?> targetEntityType;
   /* 字表关联的字段 这个以前只有 转换 Hibernate 的map文件需要，现在都用注解不需要了
    private List<SimpleTableField> fkColumns;*/
    /**
     * key： 父表属性 value：子表属性
     */
    private Map<String, String> referenceColumns;
    private int nObjectId; //only used by sqlserver
    // 父表 属性字段
    private JavaBeanField beanField;

    public int getObjectId() {
        return nObjectId;
    }

    public void setObjectId(int objectId) {
        nObjectId = objectId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public boolean containColumn(String sCol) {
        if(sCol==null || referenceColumns==null || referenceColumns.size() == 0)
            return false;
        return referenceColumns.containsKey(sCol);
    }

    public String getClassName() {
        if(tableName==null)
            return null;
        String sClassName = FieldType.mapPropName(tableName);
        return sClassName.substring(0,1).toUpperCase() +
                sClassName.substring(1);
    }

    @Override
    public Map<String, String> getReferenceColumns() {
        if(this.referenceColumns==null) {
            this.referenceColumns = new HashMap<>(6);
        }
        return this.referenceColumns;
    }

    @Override
    public String getParentTableName() {
        return this.parentTableName;
    }

    public void setParentTableName(String parentTableName) {
        this.parentTableName = parentTableName;
    }

    public void setReferenceColumns(Map<String, String> referenceColumns) {
        this.referenceColumns = referenceColumns;
    }

    /**
     * @param column 父表字段
     * @param referencedColumn 子表字段
     */
    public void addReferenceColumn(String column, String referencedColumn) {
        if(this.referenceColumns==null) {
            this.referenceColumns = new HashMap<>(6);
        }
        this.referenceColumns.put(column,
                StringUtils.isBlank(referencedColumn) ? column : referencedColumn);
    }

    public Class<?> getReferenceFieldType() {
        return referenceFieldType;
    }

    public void setReferenceFieldType(Class<?> referenceFieldType) {
        this.referenceFieldType = referenceFieldType;
    }

    public Class<?> getTargetEntityType() {
        return targetEntityType;
    }

    public void setTargetEntityType(Class<?> targetEntityType) {
        this.targetEntityType = targetEntityType;
    }

    public void setObjectField(Field objectField) {
        if(beanField==null)
            beanField = new JavaBeanField();
        beanField.setObjectField(objectField);
    }

    public void setObjectSetFieldValueFunc(Method objectSetFieldValueFunc) {
        if(beanField==null)
            beanField = new JavaBeanField();
        beanField.setSetFieldValueFunc(objectSetFieldValueFunc);
    }

    public void setObjectGetFieldValueFunc(Method objectGetFieldValueFunc) {
        if(beanField==null)
            beanField = new JavaBeanField();
        beanField.setGetFieldValueFunc(objectGetFieldValueFunc);
    }

    public void setObjectFieldValue(Object obj, Object fieldValue) {
        beanField.setObjectFieldValue(obj,fieldValue);
    }

    public Object getObjectFieldValue(Object obj) {
        return beanField.getObjectFieldValue(obj);
    }

    public Map<String, Object> fetchChildFk(Object parentObject){
        /*if(referenceColumns == null){
            return null;
        }*/
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(parentObject.getClass());
        Map<String, Object> fk = new HashMap<>(8);
        for (Map.Entry<String, String> end : referenceColumns.entrySet()) {
            Object fkValue = mapInfo.getObjectFieldValue(parentObject, end.getKey());
            if(fkValue == null){
                return null;
            }
            fk.put(end.getValue(), fkValue);
        }
        return fk;
    }

    public Map<String, Object> fetchParentPk(Object childObject){
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(childObject.getClass());
        Map<String, Object> pk = new HashMap<>(8);
        for (Map.Entry<String, String> end : referenceColumns.entrySet()) {
            Object fkValue = mapInfo.getObjectFieldValue(childObject, end.getValue());
            if(fkValue == null){
                return null;
            }
            pk.put(end.getKey(), fkValue);
        }
        return pk;
    }
}
