package com.centit.support.database.metadata;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTableReference implements TableReference{

    private String parentTableName;
    private String tableName;
    private String referenceName;
    private String referenceCode;
    private Class<?> referenceType;
    private Class<?> targetEntityType;
    private List<SimpleTableField> fkColumns;

    private Map<String, String> referenceColumns;
    private int nObjectId; //only used by sqlserver

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

    public List<SimpleTableField> getFkColumns() {
        if(fkColumns==null)
            fkColumns = new ArrayList<SimpleTableField>();
        return fkColumns;
    }

    public void setFkColumns(List<SimpleTableField> fkcolumns) {
        this.fkColumns = fkcolumns;
    }

    public boolean containColumn(String sCol) {
        if(sCol==null || fkColumns==null || fkColumns.size() == 0)
            return false;
        for(SimpleTableField tf : fkColumns){
            if(sCol.equalsIgnoreCase(tf.getColumnName()))
                return true;
        }
        return false;
    }

    public String getClassName() {
        String sClassName = SimpleTableField.mapPropName(tableName);
        return sClassName.substring(0,1).toUpperCase() +
                sClassName.substring(1);
    }

    @Override
    public Map<String, String> getReferenceColumns() {
        if(this.referenceColumns==null)
            this.referenceColumns = new HashMap<>(6);
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

    public void addReferenceColumn(String column, String referencedColumn) {
        getReferenceColumns().put(column,
                StringUtils.isBlank(referencedColumn)?column:referencedColumn);
    }

    public Class<?> getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(Class<?> referenceType) {
        this.referenceType = referenceType;
    }

    public Class<?> getTargetEntityType() {
        return targetEntityType;
    }

    public void setTargetEntityType(Class<?> targetEntityType) {
        this.targetEntityType = targetEntityType;
    }
}
