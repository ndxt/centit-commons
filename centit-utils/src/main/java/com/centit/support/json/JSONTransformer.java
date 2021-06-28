package com.centit.support.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JSONTransformer {
    protected static final Logger logger = LoggerFactory.getLogger(JSONTransformer.class);
    /**
     * value | key:value
     * value : 非字符串常量 | string 常量  "@" + 常量 | 引用 ref
     * ref : /rootPath | .currentPath | ..ParentPath | path == currentPath
     * key : @ noKey | # loop | key 常量
     *
     * @param templateObj 模板
     * @param dataSupport 元数据
     * @return 结果数据
     */
    public static Object transformer(Object templateObj,
                                     JSONTransformDataSupport dataSupport){
        if(templateObj instanceof String){
            String value = (String)templateObj;
            if(value.startsWith("@")){
                return value.substring(1);
            } else {
                return dataSupport.attainExpressionValue(value);
            }
        } else if(templateObj instanceof Map){
            Map<String, Object> tempMap = CollectionsOpt.objectToMap(templateObj);
            JSONObject jobj = new JSONObject();
            for(Map.Entry<String, Object> ent : tempMap.entrySet()){
                String skey = ent.getKey();
                if(skey.startsWith("@")){ // 这个必须返回 Map
                    Object value = transformer(ent.getValue(), dataSupport);
                    if(value instanceof Map){
                        jobj.putAll(CollectionsOpt.objectToMap(value));
                    } else {
                        jobj.put(skey.substring(1), value);
                    }
                } else if(skey.startsWith("#")) { //数组迭代； 并且只能是 单独的 一个key
                    Object obj = dataSupport.attainExpressionValue(skey.substring(1));
                    if(! (obj instanceof Collection)){
                        logger.warn(skey.substring(1) + "对应的数据不是数组");
                    }
                    JSONArray array = new JSONArray();
                    List<Object> loopData = CollectionsOpt.objectToList(obj);
                    for(Object ld : loopData){
                        dataSupport.pushStackValue(ld);
                        if(StringBaseOpt.isNvl(String.valueOf(ent.getValue()))){
                            array.add(ld);
                        }else {
                            array.add(transformer(ent.getValue(), dataSupport));
                        }
                        dataSupport.popStackValue();
                    }
                    return array;
                } else {
                    jobj.put(skey, transformer(ent.getValue(), dataSupport));
                }
            }
            return jobj;
        } else if(templateObj instanceof Collection) {
            JSONArray array = new JSONArray();
            Collection<?> valueList = (Collection<?>) templateObj;
            for (Object ov : valueList) {
                if (ov != null) {
                    array.add(transformer(ov, dataSupport));
                }
            }
            return array;
        } else {
            Class<?> clazz = templateObj.getClass();
            if (clazz.isArray()) {
                JSONArray array = new JSONArray();
                int len = Array.getLength(templateObj);
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        array.add(transformer(Array.get(templateObj, i), dataSupport));
                    }
                }
                return array;
            } else {
                return templateObj;
            }
        }
    }

    public static Object transformer(Object templateObj,
                                     Object dataSupport){
        return transformer(templateObj, new DefaultJSONTransformDataSupport(dataSupport));
    }
}
