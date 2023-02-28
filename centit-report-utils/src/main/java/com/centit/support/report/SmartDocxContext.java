package com.centit.support.report;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.ReflectionOpt;
import fr.opensagres.xdocreport.template.IContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by codefan on 17-9-25.
 * 测试通过 "___" 为Report的保留属性
 */
public class SmartDocxContext implements IContext{
    private Map<String,Object> docObject;

    public SmartDocxContext(){
        docObject = null;
    }

    public SmartDocxContext(Object object){
        if(object instanceof Map) {
            this.docObject = (Map<String,Object>) object;
        }else{
            this.docObject = (JSONObject) JSON.toJSON(object);
        }
    }

    /**
     * Register a Java object with the given key.
     *
     * @param key  主键
     * @param value Java model
     * @return 返回 旧值
     */
    @Override
    public Object put(String key, Object value) {
        if(docObject==null) {
            docObject = new HashMap<>();
        }
        return docObject.put(key, value);
    }

    /**
     * Get the Java object with the given key which was registered and null otherwise.
     *
     * @param key 占位符
     * @return 对应的值
     */
    @Override
    public Object get(String key) {
        Object value = null;
        boolean notNull = key.startsWith("NP_");
        String realKey = notNull?key.substring(3):key;

        if(docObject!=null) {

            value = ReflectionOpt.attainExpressionValue(docObject, realKey);
        }
        return value == null? ( (key.startsWith("___") || !notNull) ? null:new SmartDocxContext()): value;
    }

    /**
     * @param contextMap 对象map
     */
    @Override
    public void putMap(Map<String, Object> contextMap) {
        this.docObject = contextMap;
    }

    @Override
    public Map<String, Object> getContextMap() {
        return docObject;
    }

    @Override
    public String toString(){
        return docObject==null ? "" : JSON.toJSONString(docObject);
    }
}
