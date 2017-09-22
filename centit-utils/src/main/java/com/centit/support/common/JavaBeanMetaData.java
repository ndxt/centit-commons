package com.centit.support.common;

import com.centit.support.algorithm.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-9-22.
 */
public class JavaBeanMetaData {
    private Class<?> javaType;
    private Map<String, JavaBeanField> fileds;
    private JavaBeanMetaData(){}

    private JavaBeanMetaData(Class<?> javaType){
        this.javaType = javaType;
        this.fileds = new HashMap<>(30);
    }

    public static JavaBeanMetaData creatBeanMetaDataFromType(Class<?> javaType){
        JavaBeanMetaData metaData = new JavaBeanMetaData(javaType);
        Field[] objFields = javaType.getDeclaredFields();
        for(Field field :objFields){
            metaData.getFileds().put(field.getName(), new JavaBeanField(field));
        }

        List<Method> setters = ReflectionOpt.getAllSetterMethod(javaType);
        if(setters!=null){
            for(Method md : setters) {
                String fieldName = StringUtils.uncapitalize(
                        md.getName().substring(3));
                JavaBeanField javaField = metaData.getFiled(fieldName);
                if(javaField == null){
                    javaField = new JavaBeanField();
                    javaField.setSetFieldValueFunc(md);
                    javaField.setFieldType( md.getParameterTypes()[0]);

                    metaData.getFileds().put(fieldName, javaField);
                }else{
                    if(javaField.isAssignableFrom(md.getParameterTypes()[0] )){
                        javaField.setSetFieldValueFunc(md);
                    }
                }
            }
        }

        List<Method> getters = ReflectionOpt.getAllGetterMethod(javaType);
        if(getters!=null){
            for(Method md : getters) {
                String fieldName = StringUtils.uncapitalize(
                        md.getName().substring(3));
                JavaBeanField javaField = metaData.getFiled(fieldName);
                if(javaField == null){
                    javaField = new JavaBeanField();
                    javaField.setGetFieldValueFunc(md);
                    javaField.setFieldType( md.getReturnType());

                    metaData.getFileds().put(fieldName, javaField);
                }else{
                    if(javaField.isAssignableFrom( md.getReturnType() )){
                        javaField.setGetFieldValueFunc(md);
                    }
                }
            }
        }

        return metaData;
    }

    public Object creatBeanObject() throws IllegalAccessException, InstantiationException {
        return javaType.newInstance();
    }

    public void setObjectFieldValue(Object object, String fieldName, Object newValue){
        JavaBeanField field = this.getFiled(fieldName);
        if(field==null)
            return ;

        switch (field.getFieldJavaType()) {
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

            case "byte[]":
                field.setObjectFieldValue(object,
                        StringBaseOpt.objectToString(newValue).getBytes());
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
                field.setObjectFieldValue(object,
                            StringBaseOpt.objectToString(newValue));
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
            default:
                field.setObjectFieldValue(object, newValue);
                break;
        }
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public Map<String, JavaBeanField> getFileds() {
        return fileds;
    }

    public JavaBeanField getFiled(String fieldName) {
        return fileds.get(fieldName);
    }
}
