package com.centit.support.common;

import com.centit.support.algorithm.ReflectionOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by codefan on 17-9-22.
 */
public class JavaBeanField {

    private static final Logger logger = LoggerFactory.getLogger(JavaBeanField.class);

    public JavaBeanField(){

    }

    public JavaBeanField(Field objectField){
        this.setObjectField(objectField);
    }

    private String fieldJavaType;
    private Class<?> fieldType;
    private Method setFieldValueFunc;
    private Method getFieldValueFunc;
    private Field objectField;

    public Method getSetFieldValueFunc() {
        return setFieldValueFunc;
    }

    public void setSetFieldValueFunc(Method setFieldValueFunc) {
        this.setFieldValueFunc = setFieldValueFunc;
    }

    public Method getGetFieldValueFunc() {
        return getFieldValueFunc;
    }

    public void setGetFieldValueFunc(Method getFieldValueFunc) {
        this.getFieldValueFunc = getFieldValueFunc;
    }

    public Field getObjectField() {
        return objectField;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
        this.fieldJavaType = ReflectionOpt.getJavaTypeName(fieldType);
    }

    public void setObjectField(Field objectField) {
        this.objectField = objectField;
        this.setFieldType(objectField.getType());

    }

    public String getFieldJavaType() {
        return fieldJavaType;
    }

    public boolean isAssignableFrom(Class<?> valueType){
        return this.fieldType.isAssignableFrom(valueType);
    }


    public void setObjectFieldValue(Object obj, Object fieldValue) {
        //if( fieldType.isAssignableFrom(fieldValue.getClass()) ) {
            try {
                if (setFieldValueFunc != null) {
                    setFieldValueFunc.invoke(obj, fieldValue);
                } else {

                    boolean accessible = objectField.isAccessible();
                    if (!accessible) {
                        objectField.setAccessible(true);
                    }
                    objectField.set(obj, fieldValue);
                    if (!accessible) {
                        objectField.setAccessible(accessible);
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
        //}
    }

    public Object getObjectFieldValue(Object obj) {
        try {
            if (getFieldValueFunc != null) {
                return getFieldValueFunc.invoke(obj);
            } else {
                return objectField.get(obj);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
