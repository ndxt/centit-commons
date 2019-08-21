package com.centit.support.database.orm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.common.LeftRightPair;
import com.centit.support.compiler.VariableFormula;
import com.centit.support.database.jsonmaptable.GeneralJsonObjectDao;
import com.centit.support.database.jsonmaptable.JsonObjectDao;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.FieldType;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-8-27.
 * @author codefan@sina.com
 */
@SuppressWarnings("unused")
public abstract class OrmUtils {
    private OrmUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static void setObjectFieldValue(Object object, SimpleTableField field,
                                           Object newValue)
            throws NoSuchFieldException, IOException {
        if (newValue instanceof Clob) {
            if(FieldType.TEXT.equals(field.getFieldLabelName())){
                field.setObjectFieldValue(object,
                        /*(Clob)*/ newValue );
            }else {
                field.setObjectFieldValue(object,
                        DatabaseAccess.fetchClobString((Clob) newValue));
            }
        }else if (newValue instanceof Blob) {
            if(FieldType.BYTE_ARRAY.equals(field.getFieldLabelName())){
                field.setObjectFieldValue(object,
                        /*(Blob)*/ newValue );
            }else {
                field.setObjectFieldValue(object,
                        DatabaseAccess.fetchBlobBytes((Blob) newValue));
            }
        } else {
            field.setObjectFieldValue(object, newValue);
        }
    }

    private static <T> T makeObjectValueByGenerator(T object, TableMapInfo mapInfo,
                                                    JsonObjectDao sqlDialect, GeneratorTime generatorTime)
            throws SQLException, NoSuchFieldException, IOException {
        List<LeftRightPair<String, ValueGenerator>>  valueGenerators = mapInfo.getValueGenerators();
        if(valueGenerators == null || valueGenerators.size()<1 )
            return object;
        for(LeftRightPair<String, ValueGenerator> ent :  valueGenerators) {
            ValueGenerator valueGenerator =  ent.getRight();
            if ( valueGenerator.occasion().matchTime(generatorTime)){
                SimpleTableField filed = mapInfo.findFieldByName(ent.getLeft());
                Object fieldValue = ReflectionOpt.forceGetProperty(object, filed.getPropertyName());
                if( fieldValue == null || valueGenerator.condition() == GeneratorCondition.ALWAYS ){
                    switch (valueGenerator.strategy()){
                        case UUID:
                            filed.setObjectFieldValue(object, UuidOpt.getUuidAsString32());
                            break;
                        case UUID22:
                            filed.setObjectFieldValue(object, UuidOpt.getUuidAsString22());
                            break;
                        case SEQUENCE:
                            //GeneratorTime.READ 读取数据时不能用 SEQUENCE 生成值
                            if(sqlDialect!=null) {
                                setObjectFieldValue(object, filed,
                                        sqlDialect.getSequenceNextValue(valueGenerator.value()));
                            }
                            break;
                        case CONSTANT:
                            setObjectFieldValue(object, filed, valueGenerator.value());
                            break;
                        case FUNCTION:
                            setObjectFieldValue(object, filed,
                                    VariableFormula.calculate(valueGenerator.value(), object));
                            break;
                        case LSH:
                            {
                                String genValue = valueGenerator.value();
                                int n = genValue.indexOf(':');
                                if(n>0 && sqlDialect!=null){
                                    String seq = genValue.substring(0,n);
                                    Long seqNo = sqlDialect.getSequenceNextValue(valueGenerator.value());
                                    JSONObject json = (JSONObject) JSON.toJSON(object);
                                    json.put("no",seqNo);
                                    setObjectFieldValue(object, filed,
                                        VariableFormula.calculate(
                                            genValue.substring(n+1), object));
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
        return makeObjectValueByGenerator(object, mapInfo, sqlDialect,GeneratorTime.NEW);
    }

    public static <T> T prepareObjectForUpdate(T object, TableMapInfo mapInfo,JsonObjectDao sqlDialect)
            throws SQLException, NoSuchFieldException, IOException {
        return makeObjectValueByGenerator(object, mapInfo, sqlDialect, GeneratorTime.UPDATE);
    }

    public static <T> T prepareObjectForMerge(T object, TableMapInfo mapInfo,JsonObjectDao sqlDialect)
            throws SQLException, NoSuchFieldException, IOException {
        Map<String,Object> objectMap = OrmUtils.fetchObjectDatabaseField(object,mapInfo);
        if(! GeneralJsonObjectDao.checkHasAllPkColumns(mapInfo,objectMap)){
            return makeObjectValueByGenerator(object, mapInfo, sqlDialect, GeneratorTime.NEW);
        }else {
            return makeObjectValueByGenerator(object, mapInfo, sqlDialect, GeneratorTime.UPDATE);
        }
    }

    public static Map<String, Object> fetchObjectField(Object object) {
        if(object instanceof Map) {
            return (Map<String, Object>) object;
        }
        // 这个地方为什么 不用 JsonObject.toJSONObject
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
        return makeObjectValueByGenerator(object, mapInfo, null, GeneratorTime.READ);
        //return object;
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
            listObj.add(makeObjectValueByGenerator(object, mapInfo, null, GeneratorTime.READ));
        }
        return listObj;
    }

}
