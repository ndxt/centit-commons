package com.centit.search.utils;

import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.common.JavaBeanField;
import com.centit.support.common.JavaBeanMetaData;
import lombok.Setter;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public abstract class ObjectTextExtractor {
    private static String FIELD_SPLIT_STR = ";";
    private static String ARRAY_ITEM_SPLIT_STR = ",";
    private static String KEY_VALUE_SPLIT_STR = ":";
    private static String CYCLE_REF_LABEL = "-@-";

    public static class TextExtractContent{
        @Setter
        Map<Class<?>, String[]> includes;
        @Setter
        Map<Class<?>, String[]> excludes;
        @Setter
        boolean omitNumber;

        @Setter
        boolean omitSymbol;

        ArrayList<Object> hasExtracted;
        StringBuilder   textBuilder;
        boolean includeName;


        public TextExtractContent includePropertyName(boolean includeKey){
            includeName = includeKey;
            return this;
        }

        public TextExtractContent includeProperties(Class<?> clazz, String[] properties){
            if(includes==null){
                includes = new HashMap<>(10);
            }
            includes.put(clazz, properties);
            return this;
        }

        public TextExtractContent excludeProperties(Class<?> clazz, String[] properties){
            if(excludes==null){
                excludes = new HashMap<>(10);
            }
            excludes.put(clazz, properties);
            return this;
        }

        public TextExtractContent(){
            includeName = false;
            omitNumber = false;
            omitSymbol = false;
            hasExtracted = new ArrayList<>(100);
            textBuilder = new StringBuilder();
        }

        boolean hasExtracted(Object object){
            return hasExtracted.contains(object);
        }

        void addExtractedObject(Object object){
            hasExtracted.add(object);
        }

        String[] getExcludeFields(Class<?> clazz){
            if(excludes!=null) {
                for (Map.Entry<Class<?>, String[]> clz : excludes.entrySet()) {
                    if (ClassUtils.isAssignable(clazz, clz.getKey())) {
                        return clz.getValue();
                    }
                }
            }
            return null;
        }

        String[] getIncludeFields(Class<?> clazz){
            if(includes != null){
                for (Map.Entry<Class<?>, String[]> clz : includes.entrySet()) {
                    if (ClassUtils.isAssignable(clazz, clz.getKey())) {
                        return clz.getValue();
                    }
                }
            }
            return null;
        }
    }
    public static TextExtractContent createContent(){
        return new TextExtractContent();
    }

    public static String extractText(Object object, TextExtractContent content) {
        innerExtractText(object, content);
        return content.textBuilder.toString();
    }

    public static String extractText(Object object, boolean omitNumber, boolean omitSymbol) {
        TextExtractContent content = createContent();
        content.setOmitNumber(omitNumber);
        content.setOmitSymbol(omitSymbol);
        innerExtractText(object, content);
        return content.textBuilder.toString();
    }

    public static String extractText(Object object) {
        return extractText(object, false, false);
    }

    private static void innerExtractText(Object object, TextExtractContent content) {
        if(object==null){
            return;
        }
        Class<?> objClazz = object.getClass();
        if(ReflectionOpt.isScalarType(objClazz)){

            if(content.omitNumber && ( object instanceof Number || object instanceof Boolean
                    ||(object instanceof String && StringRegularOpt.isNumber((String)object))))
                return ;
            content.textBuilder.append(object);
            return;
        }
        if (objClazz.isEnum()) {
            content.textBuilder.append(((Enum<?>) object).name());
        }

        if(object instanceof byte[]){
            content.textBuilder.append(new String((byte[])object));
            return;
        }
        // 检查对象 循环引用的 问题
        if(content.hasExtracted(object)){
            if(!content.omitSymbol)
                content.textBuilder.append(CYCLE_REF_LABEL);//object.getClass().getName() );
            return;
        }
        content.addExtractedObject(object);

        if(objClazz.isArray()){
            int len = Array.getLength(object);
            for (int i = 0; i < len; ++i) {
                if(i>0 && !content.omitSymbol) {
                    content.textBuilder.append(ARRAY_ITEM_SPLIT_STR);
                }
                Object item = Array.get(object, i);
                innerExtractText(item, content);
            }
            return;
        }
        int i=0;
        if(object instanceof Collection){
            Collection<Object> collection = (Collection<Object>) object;
            for (Object item : collection) {
                if(i>0 && !content.omitSymbol) {
                    content.textBuilder.append(ARRAY_ITEM_SPLIT_STR);
                }
                i++;
                innerExtractText(item, content);

            }
            return;
        }

        if(object instanceof Map){
            Map<Object, Object> map = (Map<Object, Object>) object;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                if(i>0 && !content.omitSymbol) {
                    content.textBuilder.append(FIELD_SPLIT_STR);
                }
                i++;
                if(content.includeName) {
                    innerExtractText(entry.getKey(), content);
                    if(!content.omitSymbol)
                        content.textBuilder.append(KEY_VALUE_SPLIT_STR);
                }
                innerExtractText(entry.getValue(), content);
            }
            return;
        }

        JavaBeanMetaData metadata = JavaBeanMetaData.createBeanMetaDataFromType(objClazz, 1);
        Map<String, JavaBeanField> fileds = metadata.getFileds();
        String[] excludeFields = content.getExcludeFields(objClazz);
        if(excludeFields!=null){
            for(String skey : excludeFields){
                fileds.remove(skey);
            }
        }

        String[] includeFields = content.getIncludeFields(objClazz);
        if(includeFields!=null){
            for(String skey : includeFields){
                JavaBeanField field = fileds.get(skey);
                if(field != null){
                    Object item = field.getObjectFieldValue(object);
                    if(item!=null) {
                        if (i > 0 && !content.omitSymbol) {
                            content.textBuilder.append(FIELD_SPLIT_STR);
                        }
                        i++;
                        if (content.includeName) {
                            content.textBuilder.append(skey);
                            if(!content.omitSymbol)
                                content.textBuilder.append(KEY_VALUE_SPLIT_STR);
                        }
                        innerExtractText(item, content);
                    }
                }
            }
            return;
        }

        for (Map.Entry<String, JavaBeanField> entry : fileds.entrySet()) {
            Object item = entry.getValue().getObjectFieldValue(object);
            if(item!=null) {
                if (i > 0 && !content.omitSymbol) {
                    content.textBuilder.append(FIELD_SPLIT_STR);
                }
                i++;
                if (content.includeName) {
                    content.textBuilder.append(entry.getKey());
                    if(!content.omitSymbol)
                        content.textBuilder.append(KEY_VALUE_SPLIT_STR);
                }
                innerExtractText(item, content);
            }
        }
    }

}
