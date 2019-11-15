package com.centit.support.database.orm;

import com.centit.support.common.LeftRightPair;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.SimpleTableInfo;
import com.centit.support.database.utils.PersistenceException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-8-29.
 */
public class TableMapInfo extends SimpleTableInfo {

    public TableMapInfo() {
        super();
        this.embeddedId = false;
    }

    private boolean embeddedId;
    private SimpleTableField embeddedIdField;

    public List<LeftRightPair<String, ValueGenerator>> getValueGenerators() {
        return valueGenerators;
    }

    private List<LeftRightPair<String,ValueGenerator>> valueGenerators;

    public TableMapInfo addValueGenerator(String fieldName, ValueGenerator generator ){
        if(valueGenerators ==null)
            valueGenerators = new ArrayList<>(5);
        /*boolean add = */
        valueGenerators.add(new LeftRightPair<>(fieldName, generator));
        return this;
    }

    public void appendOrderBy(SimpleTableField column, String orderBy) {
        String orderBySql ;
        if( StringUtils.isBlank(orderBy) || "ASC".equalsIgnoreCase(orderBy) ){
            orderBySql = column.getColumnName();
        }else if("DESC".equalsIgnoreCase(orderBy)){
            // StringUtils.equalsAnyIgnoreCase(orderByTrim, "DESC", "ASC" )){
            orderBySql = column.getColumnName() + " DESC";
        }else{
            orderBySql = orderBy;
        }

        if( StringUtils.isBlank(this.getOrderBy()) ){
            super.setOrderBy( orderBySql);
        }else{
            super.setOrderBy(super.getOrderBy() +", " + orderBySql);
        }
    }

    public boolean isEmbeddedId() {
        return embeddedId;
    }

    public void setEmbeddedId(boolean embeddedId) {
        this.embeddedId = embeddedId;
    }

    public SimpleTableField getEmbeddedIdField() {
        return embeddedIdField;
    }

    public void setEmbeddedIdField(SimpleTableField embeddedIdField) {
        this.embeddedIdField = embeddedIdField;
    }

    public Object getObjectFieldValue(Object object, SimpleTableField field) {
        if(field.isPrimaryKey() && this.isEmbeddedId()){
            Object pkId = embeddedIdField.getBeanField().getObjectFieldValue(object);
            if(pkId!=null) {
                return field.getBeanField().getObjectFieldValue(pkId);
            } else {
                return null;
            }
        } else {
            return field.getBeanField().getObjectFieldValue(object);
        }
    }

    public void setObjectFieldValue(Object object, SimpleTableField field, Object newValue) {
        try {
            if (field.isPrimaryKey() && this.isEmbeddedId()) {
                Object pkId = embeddedIdField.getBeanField().getObjectFieldValue(object);
                if (pkId == null) {
                    pkId = embeddedIdField.getJavaType().newInstance();
                    field.getBeanField().setObjectFieldValue(pkId, newValue);
                    embeddedIdField.getBeanField().setObjectFieldValue(object, pkId);
                } else {
                    field.getBeanField().setObjectFieldValue(pkId, newValue);
                }
            } else {
                field.getBeanField().setObjectFieldValue(object, newValue);
            }
        }  catch (IllegalAccessException | InstantiationException e){
            PersistenceException exception = new PersistenceException(500, "创建EmbeddedId 实例错误", e);
            exception.setObjectData(this);
            throw exception;
        }
    }

    public Object getObjectFieldValue(Object object, String fieldName) {
        SimpleTableField field = this.findField(fieldName);
        if(field==null){
            return null;
        }
        return getObjectFieldValue(object, field);
    }

    public void setObjectFieldValue(Object object, String fieldName, Object newValue) {
        SimpleTableField field = this.findField(fieldName);
        if(field==null){
            return;
        }
        setObjectFieldValue(object, field, newValue);
    }

    public Map<String, Object> fetchObjectPk(Object object){
        Map<String, Object> pk = new HashMap<>(8);
        List<SimpleTableField> columns = this.getColumns();
        if(columns!=null) {
            for (SimpleTableField c : columns) {
                if (c.isPrimaryKey()) {
                    Object pkValue = this.getObjectFieldValue(object, c);
                    if (pkValue == null) {
                        return null;
                    }
                    pk.put(c.getPropertyName(), pkValue);
                }
            }
        }
        return pk;
    }
}
