package com.centit.search.document;

import com.alibaba.fastjson2.JSONObject;
import com.centit.search.annotation.ESField;
import com.centit.search.annotation.ESType;
import com.centit.support.algorithm.CollectionsOpt;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * Created by codefan on 17-6-27.
 */
public abstract class DocumentUtils {

    public static String obtainDocumentIndexName(Class<?> objType){
        if(objType.isAnnotationPresent(ESType.class)){
            ESType et = objType.getAnnotation(ESType.class);
            return et.indexName();
        }
        return null;
    }

    public static JSONObject obtainDocumentMapping(Class<?> objType) {
        Field[] objFields = objType.getDeclaredFields();
        JSONObject fieldsMap = new JSONObject();

        for(Field field :objFields){
            if(field.isAnnotationPresent(ESField.class)){
                ESField esType =
                        field.getAnnotation(ESField.class);

                JSONObject fieldConfig = new JSONObject(CollectionsOpt.createHashMap(
                    "type", StringUtils.isBlank(esType.type())?
                                      field.getType().getSimpleName() : esType.type(),
                        "index", esType.index(),// ? "analyzed" : "not_analyzed",
                        "store", esType.store()));

                if(esType.query()) {
                    if (StringUtils.isNotBlank(esType.analyzer())) {
                        fieldConfig.put("analyzer", esType.analyzer());
                    } else {
                        fieldConfig.put("analyzer", "standard");
                    }

                    if (StringUtils.isNotBlank(esType.indexAnalyzer())) {
                        fieldConfig.put("index_analyzer", esType.indexAnalyzer());
                    }

                    if (StringUtils.isNotBlank(esType.searchAnalyzer())) {
                        fieldConfig.put("search_analyzer", esType.searchAnalyzer());
                    }
                }
                fieldsMap.put(field.getName(),fieldConfig);
            }
        }//end of for
        JSONObject jsonObject = new JSONObject();
        //jsonObject.put("type", sobjType);
        //jsonObject.put("message", "type:" + objType.getName());
        jsonObject.put("properties",fieldsMap);
        return jsonObject;
    }

}

