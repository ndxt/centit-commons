package com.centit.support.json;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JSONTransformer {
    protected static final Logger logger = LoggerFactory.getLogger(JSONTransformer.class);

    public static void putObjectToJson(JSONObject jobj, String key, Object value){
        if(value == null){
            jobj.remove(key);
            return;
        }
        /*if(value instanceof Map && ((Map<?,?>)value).size()==0){
            jobj.remove(key);
            return
        }*/

        jobj.put(key, value);
    }

    public static void addObjectToJson(JSONArray jArray, Object value){
        if(value != null){
            jArray.add(value);
        }
    }
    /**
     * value | key:value
     * value : 非字符串常量 | string 常量  "@" + 常量 | 引用 ref
     *        value分两种形式，默认的是表达式，
     * ref : /rootPath | .currentPath | ..ParentPath | path == currentPath
     *      ref是一个对应的表达式，用于指向原始json中的具体的属性，或者多个属性计算值
     * key : @ noKey | # loop | key 常量
     *      '@' 开头的key表示要用value中的值替换当前的 key内容，不保留key，所以后面的名称没有实际意义
     *      '#' 开头表示根据引用的数据进行循环，重复生成数组
     *      普通的key就是在目标json中保留对应的key
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
                return dataSupport.mapTemplateString(value.substring(1));
            } else {
                return dataSupport.attainExpressionValue(value);
            }
        } else if(templateObj instanceof Map){
            Map<String, Object> tempMap = CollectionsOpt.objectToMap(templateObj);
            JSONObject jobj = new JSONObject();
            for(Map.Entry<String, Object> ent : tempMap.entrySet()){
                String skey = ent.getKey();
                if(skey.startsWith("@")){ // 替换当前属性，这个必须返回 Map
                    Object value = transformer(ent.getValue(), dataSupport);
                    if(value instanceof Map){
                        jobj.putAll(CollectionsOpt.objectToMap(value));
                    } else {
                        putObjectToJson(jobj, skey.substring(1), value);
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
                        if(StringUtils.isBlank(String.valueOf(ent.getValue())) || ".".equals(String.valueOf(ent.getValue()))){
                            addObjectToJson(array, ld);
                        }else {
                            addObjectToJson(array, transformer(ent.getValue(), dataSupport));
                        }
                        dataSupport.popStackValue();
                    }
                    return array.size()==0 ? null : array;
                } else {
                    putObjectToJson(jobj, skey, transformer(ent.getValue(), dataSupport));
                }
            }
            return jobj.size()==0 ? null : jobj;
        } else if(templateObj instanceof Collection) {
            JSONArray array = new JSONArray();
            Collection<?> valueList = (Collection<?>) templateObj;
            for (Object ov : valueList) {
                if (ov != null) {
                    Object transValue = transformer(ov, dataSupport);
                    // #loop 的数据集 自动展开
                    if(!(ov instanceof Collection) && transValue instanceof Collection){
                        array.addAll((Collection<?>)transValue);
                    } else { //如果 ov 是数值，这边就会生成二维数据
                        addObjectToJson(array, transValue);
                    }
                }
            }
            return array.size()==0 ? null : array;
        } else {
            Class<?> clazz = templateObj.getClass();
            if (clazz.isArray()) {
                JSONArray array = new JSONArray();
                int len = Array.getLength(templateObj);
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        //Array.get(templateObj, i) 这个不可能一个复杂结构，应该是字符串或者数值
                        addObjectToJson(array, transformer(Array.get(templateObj, i), dataSupport));
                    }
                }
                return array.size()==0 ? null : array;
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
