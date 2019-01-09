package com.centit.support.common;

import com.centit.support.algorithm.ReflectionOpt;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * java 的内省机制实现了 javaBean 的信息获取，后面希望通过这套jdk标准的方法重写这部分内容。
 * Created by codefan on 17-9-22.
 * @see java.beans.BeanInfo
 * @see java.beans.Introspector
 */
public class JavaBeanMetaData {
    private Class<?> javaType;
    private Map<String, JavaBeanField> fileds;
    private JavaBeanMetaData(){}

    private JavaBeanMetaData(Class<?> javaType){
        this.javaType = javaType;
        this.fileds = new HashMap<>(30);
    }

    public static JavaBeanMetaData createBeanMetaDataFromType(Class<?> javaType){
        JavaBeanMetaData metaData = new JavaBeanMetaData(javaType);
        Field[] objFields = javaType.getDeclaredFields();
        for(Field field :objFields){
            metaData.getFileds().put(field.getName(), new JavaBeanField(field));
        }

        List<Method> setters = ReflectionOpt.getAllSetterMethod(javaType);
        if(setters!=null){
            for(Method md : setters) {
                String fieldName = ReflectionOpt.mapGetter2Field(md);
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

    public Object createBeanObject() throws IllegalAccessException, InstantiationException {
        return javaType.newInstance();
    }

    public Object createBeanObjectFromMap(Map<String,Object> properties) throws IllegalAccessException, InstantiationException {
        Object object = javaType.newInstance();
        for(Map.Entry<String, Object> pro : properties.entrySet() ){
            setObjectFieldValue(object, pro.getKey(), pro.getValue());
        }
        return object;
    }

    public void setObjectFieldValue(Object object, String fieldName, Object newValue){
        JavaBeanField field = this.getFiled(fieldName);
        if(field==null)
            return ;
        field.setObjectFieldValue(object,newValue);
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
