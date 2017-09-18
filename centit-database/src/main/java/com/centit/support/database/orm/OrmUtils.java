package com.centit.support.database.orm;

import com.centit.support.algorithm.*;
import com.centit.support.common.KeyValuePair;
import com.centit.support.database.jsonmaptable.JsonObjectDao;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.utils.DatabaseAccess;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-8-27.
 * @Author codefan@sina.com
 */
@SuppressWarnings("unused")
public abstract class OrmUtils {
    private OrmUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static void setObjectFieldValue(Object object, SimpleTableField field,
                                           Object newValue)
            throws NoSuchFieldException, IOException {
        switch (field.getJavaType()) {
            case "int":
            case "Integer":
                field.setObjectFieldValue(object,
                        NumberBaseOpt.castObjectToInteger(newValue));
                break;
            case "long":
            case "Long":
                field.setObjectFieldValue(object,
                        NumberBaseOpt.castObjectToLong(newValue));
                break;
            case "float":
            case "Float":
            case "double":
            case "Double":
                field.setObjectFieldValue(object,
                        NumberBaseOpt.castObjectToDouble(newValue));
                break;

            case "byte[]"://BLOB字段
                if (newValue instanceof Blob) {
                    field.setObjectFieldValue(object,
                            DatabaseAccess.fetchBlobBytes((Blob) newValue));
                }else{
                    field.setObjectFieldValue(object,
                            StringBaseOpt.objectToString(newValue).getBytes());
                }
                break;


            case "BigDecimal":
                field.setObjectFieldValue(object,
                        NumberBaseOpt.castObjectToBigDecimal(newValue));
                break;
            case "BigInteger":
                field.setObjectFieldValue(object,
                        NumberBaseOpt.castObjectToBigInteger(newValue));
                break;
            case "String":
                if (newValue instanceof Clob) {
                    field.setObjectFieldValue(object,
                            DatabaseAccess.fetchClobString((Clob) newValue));
                } /*else if (newValue instanceof Blob) {
                    ReflectionOpt.setFieldValue(object,propertyName,
                            DatabaseAccess.fetchBlobAsBase64((Blob) newValue),String.class);
                } */else {
                    field.setObjectFieldValue(object,
                            StringBaseOpt.objectToString(newValue));
                }
                break;
            case "Date":
            case "Timestamp":
                field.setObjectFieldValue(object,
                        DatetimeOpt.castObjectToDate(newValue));
                break;
            case "boolean":
            case "Boolean":
                field.setObjectFieldValue(object,
                        StringRegularOpt.isTrue(
                                StringBaseOpt.objectToString(newValue)));
                break;
            case "Clob":
            case "Blob":
                field.setObjectFieldValue(object,
                        /*(Clob)*/ newValue );
                break;
            default:
                if (newValue instanceof Clob) {
                    field.setObjectFieldValue(object,
                            DatabaseAccess.fetchClobString((Clob) newValue));
                }else if (newValue instanceof Blob) {
                    field.setObjectFieldValue(object,
                            DatabaseAccess.fetchBlobBytes((Blob) newValue));
                } else {
                    field.setObjectFieldValue(object, newValue);
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
                            filed.setObjectFieldValue(object, UuidOpt.getUuidAsString32());
                            break;
                        case SEQUENCE:
                            setObjectFieldValue(object, filed, sqlDialect.getSequenceNextValue(
                                    valueGenerator.value()));
                            break;
                        case CONSTANT:
                            setObjectFieldValue(object, filed, valueGenerator.value());

                            break;
                        case FUNCTIION: {
                            switch (valueGenerator.value()){
                                case "now":
                                case "currentTime":
                                case "sysdate":
                                    setObjectFieldValue(object, filed, DatetimeOpt.currentUtilDate());
                                    break;
                            }
                            break;
                        }
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
        List<SimpleTableField> tableFields = tableInfo.getColumns();
        if(tableFields == null)
            return null;
        Map<String, Object> fields = new HashMap<>(tableFields.size()*2+6);
        for(SimpleTableField column : tableFields){
            Object value = column.getObjectFieldValue(object);
            //ReflectionOpt.getFieldValue(object, column.getPropertyName());
            if(value!=null){
                fields.put(column.getPropertyName(),value);
            }
        }

        tableFields = tableInfo.getLazyColumns();
        if(tableFields != null) {
            for (SimpleTableField column : tableFields) {
                Object value = column.getObjectFieldValue(object);
                //ReflectionOpt.getFieldValue(object, column.getPropertyName());
                if (value != null) {
                    fields.put(column.getPropertyName(), value);
                }
            }
        }
        return fields;
    }

    private static <T> T insideFetchFieldsFormResultSet(ResultSet rs, T object, TableMapInfo mapInfo )
            throws SQLException, NoSuchFieldException, IOException {
        ResultSetMetaData resMeta = rs.getMetaData();
        int fieldCount = resMeta.getColumnCount();
        for (int i = 1; i <= fieldCount; i++) {
            String columnName = resMeta.getColumnName(i);
            SimpleTableField filed = mapInfo.findFieldByColumn(columnName);
            if (filed != null) {
                setObjectFieldValue(object, filed, rs.getObject(i));
            }
        }
        return object;

    }

    public static <T> T fetchObjectFormResultSet(ResultSet rs, Class<T> clazz)
            throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException, IOException {
        TableMapInfo mapInfo = JpaMetadata.fetchTableMapInfo(clazz);
        if(mapInfo == null)
            return null;
        if(rs.next()) {
            return insideFetchFieldsFormResultSet(rs, clazz.newInstance(), mapInfo);
        }else {
            return null;
        }
    }

    public static <T> T fetchFieldsFormResultSet(ResultSet rs, T object, TableMapInfo mapInfo )
            throws SQLException, NoSuchFieldException, IOException {
        if(rs.next()) {
            object = insideFetchFieldsFormResultSet(rs, object, mapInfo);
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
                    setObjectFieldValue(object, fields[i], rs.getObject(i));
                }
            }
            listObj.add(object);
        }
        return listObj;
    }

}
