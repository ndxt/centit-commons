package com.centit.support.json;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class JSONTransformer {
    protected static final Logger logger = LoggerFactory.getLogger(JSONTransformer.class);

    public static void putObjectToJson(JSONObject jObj, String key, Object value){
        if(value == null){
            jObj.remove(key);
            return;
        }
        /*if(value instanceof Map && ((Map<?,?>)value).size()==0){
            jObj.remove(key);
            return
        }*/
        jObj.put(key, value);
    }

    public static void addObjectToJson(JSONArray jArray, Object value){
        if(value != null){
            jArray.add(value);
        }
    }

    /**
     * JSON 转换模板引擎，根据模板和元数据生成目标 JSON 结构。
     * 一、value（模板值为字符串时）的前缀约定：
     *   '@' + 常量      → 字符串常量，直接返回 '@' 之后的内容，不做任何计算
     *   '=' + 表达式    → 调用 attainExpressionValue 计算表达式并返回结果
     *   '#' + JSONPath  → 调用 extractJSONPathValue 按 JSONPath 提取数据
     *   其他 默认为  模板      → 调用 mapTemplateString 做变量替换后返回
     * 二、key（模板为 Map 时，每个 entry 的 key）的前缀约定：
     *   '=' + 表达式    → 动态 key：用表达式的计算结果作为实际的 key 名，
     *                      value 正常递归转换。常与 '$' 循环合并配合将列表转为 Map
     *   '@' + 名称      → 展开/合并：若 value 递归转换后为 Map，则将其所有属性
     *                      合并到当前对象中（类似展开运算符）；否则以 '@' 后的名称为 key 正常放入
     *   '#' + 表达式    → 循环迭代：计算表达式得到集合，遍历每个元素（压入上下文栈），
     *                      对 value 递归转换，将所有结果收集为数组直接返回（替换当前 entry）。
     *                      特殊地，若 value 为空白字符串或 "."，则直接使用迭代元素本身
     *   '$' + 表达式    → 循环合并：类似 '#'，但将每次迭代的转换结果（若为 Map）
     *                      合并到当前对象中，适用于将列表数据展开为 Map 属性
     *   普通字符串      → 直接作为目标 JSON 中的 key，value 递归转换
     * 三、其他模板类型：
     *   Collection / 数组 → 逐元素递归转换，收集为 JSONArray；
     *                        若非集合元素转换后变成集合，则自动展开（扁平化）
     *   其他类型（数字、布尔等）→ 直接返回原值
     *
     * @param templateObj 模板
     * @param dataSupport 元数据
     * @return 结果数据
     */
    @SuppressWarnings("unchecked")
    public static Object transformer(Object templateObj,
                                     JSONTransformDataSupport dataSupport){
        if(templateObj == null){
            return null;
        }
        if(templateObj instanceof String value){
            if(value.isEmpty()){
                return null;
            }
            return switch (value.charAt(0)) {
                case '@' -> value.substring(1);
                case '=' -> dataSupport.attainExpressionValue(value.substring(1));
                case '#' -> dataSupport.extractJSONPathValue(value.substring(1)); // JSONPath
                default -> dataSupport.mapTemplateString(value);
            };
        } else if(templateObj instanceof Map){
            Map<String, Object> tempMap = CollectionsOpt.objectToMap(templateObj);
            JSONObject jObj = new JSONObject();
            boolean isMap = true;
            JSONArray array = new JSONArray();
            for(Map.Entry<String, Object> ent : tempMap.entrySet()){
                String sKey = ent.getKey();
                if(sKey.isEmpty()){ continue; }
                if(sKey.charAt(0) == '='){ // 替换当前属性，这个必须返回 Map
                    Object key = dataSupport.attainExpressionValue(sKey.substring(1));
                    Object value = transformer(ent.getValue(), dataSupport);
                    String keyName = key!=null? StringBaseOpt.castObjectToString(key):sKey.substring(1);
                    putObjectToJson(jObj, keyName, value);
                } else if(sKey.charAt(0) == '@'){ // 替换当前属性，这个必须返回 Map
                    Object value = transformer(ent.getValue(), dataSupport);
                    if(value instanceof Map){
                        jObj.putAll(CollectionsOpt.objectToMap(value));
                    } else {
                        putObjectToJson(jObj, sKey.substring(1), value);
                    }
                } else if(sKey.charAt(0) == '#') { //数组迭代； 如何出现# 则忽略其他的key; 返回数组 ，可以有多个#开头的key，会自动合并到一个数组中
                    Object obj = dataSupport.attainExpressionValue(sKey.substring(1));
                    if(obj==null){continue; }
                    List<Object> loopData = CollectionsOpt.objectToList(obj);
                    int loopSize = loopData.size();
                    int index = 0;
                    for(Object ld : loopData){
                        dataSupport.pushStackValue(ld, index, loopSize);
                        if(ent.getValue() instanceof String && (StringUtils.isBlank(String.valueOf(ent.getValue())) || ".".equals(String.valueOf(ent.getValue()))) ){
                            addObjectToJson(array, ld);
                        }else {
                            addObjectToJson(array, transformer(ent.getValue(), dataSupport));
                        }
                        dataSupport.popStackValue();
                        index++;
                    }
                    isMap = false;
                } else if(sKey.charAt(0) == '$') { //数组迭代； 将内容合并到上级map中； 和 =开头的key联合使用可以将数据转换为map
                    Object obj = dataSupport.attainExpressionValue(sKey.substring(1));
                    if(obj==null){ continue; }
                    List<Object> loopData = CollectionsOpt.objectToList(obj);
                    int loopSize = loopData.size();
                    int index = 0;
                    for(Object ld : loopData){
                        dataSupport.pushStackValue(ld, index, loopSize);
                        Object vObj;
                        if(ent.getValue() instanceof String && (StringUtils.isBlank(String.valueOf(ent.getValue())) || ".".equals(String.valueOf(ent.getValue()))) ){
                            vObj = ld;
                        }else {
                            vObj = transformer(ent.getValue(), dataSupport);
                        }
                        dataSupport.popStackValue();
                        if(vObj instanceof Map<?,?>){
                            jObj.putAll((Map<String, Object>)vObj);
                        }
                        index++;
                    }
                } else {
                    putObjectToJson(jObj, sKey, transformer(ent.getValue(), dataSupport));
                }
            }
            return isMap ? jObj : array;
        } else if(templateObj instanceof Collection<?> valueList) {
            JSONArray array = new JSONArray();
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
            return /*array.isEmpty() ? null :*/ array;
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
                return /*array.isEmpty() ? null :*/ array;
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
