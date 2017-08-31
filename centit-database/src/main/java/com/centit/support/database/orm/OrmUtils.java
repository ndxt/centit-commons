package com.centit.support.database.orm;

import com.centit.support.algorithm.*;
import com.centit.support.common.KeyValuePair;
import com.centit.support.database.jsonmaptable.JsonObjectDao;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DatabaseAccess;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-8-27.
 */
@SuppressWarnings("unused")
public abstract class OrmUtils {


    public static void setObjectFieldValue(Object object, String propertyName,
                                           Object newValue, String fieldJavaType)
            throws NoSuchFieldException, IOException {
        switch (fieldJavaType) {
            case "Double":
            case "Float":
                ReflectionOpt.setFieldValue(object,propertyName,
                        NumberBaseOpt.castObjectToDouble(newValue));
                break;
            case "Long":
                ReflectionOpt.setFieldValue(object,propertyName,
                        NumberBaseOpt.castObjectToLong(newValue));
            case "Integer":
                ReflectionOpt.setFieldValue(object,propertyName,
                        NumberBaseOpt.castObjectToInteger(newValue));
                break;
            case "String":
                if (newValue instanceof Clob) {
                    ReflectionOpt.setFieldValue(object,propertyName,
                            DatabaseAccess.fetchClobString((Clob) newValue));
                }else if (newValue instanceof Blob) {
                    ReflectionOpt.setFieldValue(object,propertyName,
                            DatabaseAccess.fetchBlobAsBase64((Blob) newValue));
                } else {
                    ReflectionOpt.setFieldValue(object, propertyName,
                            StringBaseOpt.objectToString(newValue));
                }
                break;
            case "Date":
            case "Timestamp":
                ReflectionOpt.setFieldValue(object,propertyName,
                        DatetimeOpt.castObjectToDate(newValue));
                break;
            case "Boolean":
                ReflectionOpt.setFieldValue(object,propertyName,
                        StringRegularOpt.isTrue(
                                StringBaseOpt.objectToString(newValue)
                        ));
                break;
            default:
                if (newValue instanceof Clob) {
                    ReflectionOpt.setFieldValue(object,propertyName,
                            DatabaseAccess.fetchClobString((Clob) newValue));
                }else if (newValue instanceof Blob) {
                    ReflectionOpt.setFieldValue(object,propertyName,
                            DatabaseAccess.fetchBlobBytes((Blob) newValue));
                } else {
                    ReflectionOpt.setFieldValue(object, propertyName,newValue);
                }
                break;
        }
    }

    private static <T> T prepareObjectForExecuteSql(T object, TableMapInfo mapInfo,
                                                    JsonObjectDao sqlDialect, GeneratorTime generatorTime)
            throws SQLException, NoSuchFieldException, IOException {
        List<KeyValuePair<String, ValueGenerator>>  valueGenerators = mapInfo.getValueGenerators();
        if(valueGenerators == null || valueGenerators.size()<1 )
            return object;
        for(KeyValuePair<String, ValueGenerator> ent :  valueGenerators) {
            ValueGenerator valueGenerator =  ent.getValue();
            if ( generatorTime == valueGenerator.occasion()
                       || valueGenerator.occasion() == GeneratorTime.ALWAYS ){
                SimpleTableField filed = mapInfo.findFieldByName(ent.getKey());
                Object fieldValue = ReflectionOpt.forceGetProperty(object, filed.getPropertyName());
                if( fieldValue == null || valueGenerator.condition() == GeneratorCondition.ALWAYS ){
                    switch (valueGenerator.strategy()){
                        case UUID:
                            setObjectFieldValue(object, filed.getPropertyName(),
                                    UuidOpt.getUuidAsString32(), filed.getJavaType());
                            break;
                        case SEQUENCE:
                            setObjectFieldValue(object, filed.getPropertyName(),
                                    sqlDialect.getSequenceNextValue(
                                            valueGenerator.value()
                                    ), filed.getJavaType());
                            break;
                        case CONSTANT:
                            setObjectFieldValue(object, filed.getPropertyName(),
                                    valueGenerator.value(), filed.getJavaType());
                            break;
                        case FUNCTIION: {
                            switch (valueGenerator.value()){
                                case "now":
                                case "currentTime":
                                case "sysdate":
                                    setObjectFieldValue(object, filed.getPropertyName(),
                                            DatetimeOpt.currentUtilDate(), filed.getJavaType());
                                    break;
                            }
                        }
                            break;
                    }
                }
            }
        }
        return object;
    }

    public static <T> T prepareObjectForInsert(T object, TableMapInfo mapInfo,JsonObjectDao sqlDialect)
            throws SQLException, NoSuchFieldException, IOException {
        return prepareObjectForExecuteSql(object, mapInfo, sqlDialect,GeneratorTime.NEW);
    }

    public static <T> T prepareObjectForUpdate(T object, TableMapInfo mapInfo,JsonObjectDao sqlDialect)
            throws SQLException, NoSuchFieldException, IOException {
        return prepareObjectForExecuteSql(object, mapInfo, sqlDialect, GeneratorTime.UPDATE);
    }

    public static Map<String, Object> fetchObjectField(Object object)
             {
        if(object instanceof Map) {
            return (Map<String, Object>) object;
        }

        Field[] objFields = object.getClass().getDeclaredFields();
        Map<String, Object> fields = new HashMap<>(objFields.length*2);
        for(Field field :objFields){
            Object value = ReflectionOpt.forceGetFieldValue(object,field);
            fields.put(field.getName() ,value);
        }
        return fields;
    }

    public static Map<String, Object> fetchObjectDatabaseField(Object object, TableMapInfo tableInfo) {
        List<? extends TableField> tableFields = tableInfo.getColumns();
        if(tableFields == null)
            return null;
        Map<String, Object> fields = new HashMap<>(tableFields.size()*2+6);
        for(TableField column : tableFields){
            Object value = ReflectionOpt.getFieldValue(object, column.getPropertyName());
            if(value!=null){
                fields.put(column.getPropertyName(),value);
            }
        }

        tableFields = tableInfo.getLazyColumns();
        if(tableFields != null) {
            for (TableField column : tableFields) {
                Object value = ReflectionOpt.getFieldValue(object, column.getPropertyName());
                if (value != null) {
                    fields.put(column.getPropertyName(), value);
                }
            }
        }
        return fields;
    }

    public static <T> T fetchObjectFormResultSet(ResultSet rs, Class<T> clazz)
            throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(clazz);
        if(mapInfo == null)
            return null;
        ResultSetMetaData resMeta = rs.getMetaData();
        int fieldCount = resMeta.getColumnCount();
        if(rs.next()) {
            T object = clazz.newInstance();
            for (int i = 1; i <= fieldCount; i++) {
                String columnName = resMeta.getColumnName(i);
                SimpleTableField filed = mapInfo.findFieldByColumn(columnName);
                if (filed != null) {
                    //filed.getJavaType()
                    setObjectFieldValue(object, filed.getPropertyName(),
                            rs.getObject(i), filed.getJavaType());
                }
            }
            return object;
        }
        return null;
    }

    public static <T> T fetchFieldsFormResultSet(ResultSet rs, T object, TableMapInfo mapInfo )
            throws SQLException, NoSuchFieldException, IOException {
        ResultSetMetaData resMeta = rs.getMetaData();
        int fieldCount = resMeta.getColumnCount();
        if(rs.next()) {
            for (int i = 1; i <= fieldCount; i++) {
                String columnName = resMeta.getColumnName(i);
                SimpleTableField filed = mapInfo.findFieldByColumn(columnName);
                if (filed != null) {
                    setObjectFieldValue(object, filed.getPropertyName(),
                            rs.getObject(i), filed.getJavaType());
                }
            }
            return object;
        }
        return object;
    }

    public static <T> List<T> fetchObjectListFormResultSet(ResultSet rs, Class<T> clazz)
            throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException, IOException {

        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(clazz);
        if(mapInfo == null)
            return null;
        ResultSetMetaData resMeta = rs.getMetaData();
        int fieldCount = resMeta.getColumnCount();
        SimpleTableField[] fields = new SimpleTableField[fieldCount+1];
        for(int i=1;i<=fieldCount;i++) {
            String columnName = resMeta.getColumnName(i);
            fields[i] = mapInfo.findFieldByColumn(columnName);
        }

        List<T> listObj = new ArrayList<>();
        while(rs.next()){
            T object = clazz.newInstance();
            for(int i=1;i<=fieldCount;i++){
                if(fields[i] != null){
                    setObjectFieldValue(object,fields[i].getPropertyName(),
                            rs.getObject(i),fields[i].getJavaType());
                }
            }
            listObj.add(object);
        }
        return listObj;
    }

}
