package com.centit.support.json;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.JdbcSupport;
import com.centit.support.algorithm.*;
import com.centit.support.json.config.LobSerializer;
import com.centit.support.json.config.SqlDateDeserializer;
import com.centit.support.json.config.SqlTimestampDeserializer;
import com.centit.support.json.config.UtilDateDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 对JSON对象进行操作，目前只能对一维数组进行操作，
 * 但这不意味只能有一维数据，你可以给一维数组中的变量设置为任意值，包括数值，只是不能再次操作这个数值中的值
 *
 * @author codefan
 * @version $Rev$ <br>
 * $Id$
 */
@SuppressWarnings("unused")
public abstract class JSONOpt {
    protected static final Logger logger = LoggerFactory.getLogger(JSONOpt.class);

    private JSONOpt() {
        throw new IllegalAccessError("Utility class");
    }

    public static void fastjsonGlobalConfig(){
        JSON.config(JSONReader.Feature.AllowUnQuotedFieldNames);
        //允许null值，默认是忽略null值
        //JSON.config(JSONReader.Feature.IgnoreSetNullValue, false);
        //JSON.config(JSONWriter.Feature.WriteNullStringAsEmpty);

        JSON.register(java.util.Date.class, UtilDateDeserializer.instance);
        JSON.register(java.sql.Date.class, SqlDateDeserializer.instance);
        JSON.register(java.sql.Timestamp.class, SqlTimestampDeserializer.instance);

        /* JSON.register(java.util.Date.class, UtilDateSerializer.instance);
        JSON.register(java.sql.Date.class, SqlDateSerializer.instance);
        JSON.register(java.sql.Timestamp.class, SqlTimestampSerializer.instance);*/
        JSON.register(java.sql.Timestamp.class, JdbcSupport.createTimestampWriter(
            java.sql.Timestamp.class, DatetimeOpt.timestampPattern));

        JSON.register(java.sql.Blob.class, LobSerializer.instance);
        JSON.register(java.sql.Clob.class, JdbcSupport.createClobWriter(java.sql.Clob.class));
    }

    private static JsonDifferent mapDiff(String jsonPath, Map<String, Object> mapA, Map<String, Object>  mapB, String ... arrayKeys){
        List<JsonDifferent> differents = new ArrayList<>();
        for(Map.Entry<String, Object> ent : mapA.entrySet()){
            Object objA = ent.getValue();
            Object objB = mapB.get(ent.getKey());
            JsonDifferent fieldDiff = objectDiff(ent.getKey(),objA, objB, arrayKeys);
            if(fieldDiff!=null)
                differents.add(fieldDiff);
        }

        for(Map.Entry<String, Object> ent : mapB.entrySet()){
            if(!mapA.containsKey(ent.getKey())){
                differents.add(new JsonDifferent(ent.getKey(),JsonDifferent.JSON_DIFF_TYPE_ADD, null, ent.getValue()));
            }
        }
        if(differents.isEmpty()){
            return null;
        }

        Map<String, Object> objectId = fetchObjectId(mapA, arrayKeys);
        if(objectId!=null || differents.size()>1){
            JsonDifferent updateDiff = new JsonDifferent(jsonPath, JsonDifferent.JSON_DIFF_TYPE_UPDATE, null, null);
            updateDiff.setDiffChildren(differents);
            updateDiff.setObjectId(objectId);
            return updateDiff;
        }

        JsonDifferent updateDiff = differents.get(0);
        if(StringUtils.isNotBlank(jsonPath)) {
            updateDiff.setJsonPath(jsonPath+"."+updateDiff.getJsonPath());
        }
        return updateDiff;
    }

    private static Map<String, Object> fetchObjectId(Map<String, Object> data1,  String[] fields) {
        if (data1 == null || fields == null) {
            return null;
        }
        Map<String, Object> objectId = new HashMap<>();
        for (String field : fields) {
            Object idA = ReflectionOpt.attainExpressionValue(data1, field);
            if(idA !=null ){
                objectId.put(field, idA);
            }
        }
        return objectId.isEmpty() ? null : objectId;
    }

    private static int compareTwoRow(Map<String, Object> data1, Map<String, Object> data2, String[] fields) {
        if ((data1 == null && data2 == null)|| fields == null) {
            return 0;
        }

        if (data1 == null) {
            return -1;
        }

        if (data2 == null) {
            return 1;
        }
        for (String field : fields) {
            Object idA = ReflectionOpt.attainExpressionValue(data1, field);
            Object idB = ReflectionOpt.attainExpressionValue(data2, field);
            int cr = GeneralAlgorithm.compareTwoObject(idA, idB, true);
            if (cr != 0) {
                return cr;
            }
        }
        return 0;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private static JsonDifferent listDiff(String jsonPath, List<Object> listA, List<Object> listB, String ... arrayKeys){
        List<JsonDifferent> different = new ArrayList<>();
        int sizeA = listA.size(), sizeB = listB.size();
        if(sizeA==0 && sizeB==0){
            return null;
        }
        if(sizeA==0){
            return new JsonDifferent(jsonPath, JsonDifferent.JSON_DIFF_TYPE_ADD, null, listB);
        }

        if(sizeB==0){
            return new JsonDifferent(jsonPath, JsonDifferent.JSON_DIFF_TYPE_DELETE, listA, null);
        }

        int minSize = sizeA>sizeB? sizeB : sizeA;
        //通过ID排序; 对于数组中的Map来说 必须有id， id可以是多个字段， 如果不同的map id不一样，也可以同时提供
        if(listA.get(0) instanceof Map && listB.get(0) instanceof Map && arrayKeys !=null && arrayKeys.length>0){
            listA.sort( (o1, o2) -> compareTwoRow((Map)o1, (Map)o2, arrayKeys));
            listB.sort( (o1, o2) -> compareTwoRow((Map)o1, (Map)o2, arrayKeys));
            int i=0; int j=0;
            while(i<sizeA && j<sizeB){
                int c = compareTwoRow((Map)listA.get(i), (Map)listB.get(j), arrayKeys);
                if(c<0){
                    different.add(new JsonDifferent("["+i+"]", JsonDifferent.JSON_DIFF_TYPE_DELETE, listA.get(i), null));
                    i++;
                } else if(c>0){
                    different.add(new JsonDifferent("["+j+"]", JsonDifferent.JSON_DIFF_TYPE_ADD, null, listB.get(j)));
                    j++;
                } else {
                    JsonDifferent itemDiff = objectDiff( "["+i+"]", listA.get(i), listB.get(j), arrayKeys);
                    if(itemDiff!=null)
                        different.add(itemDiff);
                    i++;
                    j++;
                }
            }

            while(i<sizeA){
                different.add(new JsonDifferent("["+i+"]", JsonDifferent.JSON_DIFF_TYPE_DELETE, listA.get(i), null));
                i++;
            }
            while(j<sizeB){
                different.add(new JsonDifferent("["+j+"]", JsonDifferent.JSON_DIFF_TYPE_ADD, null, listB.get(j)));
                j++;
            }

        } //判断一下是否是多维数组，如果是多维数组只能按照序号对比
        else if(listA.get(0) instanceof List && listB.get(0) instanceof List){
            for(int i=0; i<minSize; i++){
                JsonDifferent itemDiff = objectDiff( "["+i+"]", listA.get(i), listB.get(i), arrayKeys);
                if(itemDiff!=null)
                    different.add(itemDiff);
            }
            for(int i=minSize; i<sizeA; i++){
                different.add(new JsonDifferent("["+i+"]", JsonDifferent.JSON_DIFF_TYPE_DELETE, listA.get(i), null));
            }
            for(int i=minSize; i<sizeB; i++) {
                different.add(new JsonDifferent("["+i+"]", JsonDifferent.JSON_DIFF_TYPE_ADD, null, listB.get(i)));
            }
        } // 作为标量处理； 全部用字符串对比的方式
        else {
            Set<String> stringsA = new HashSet<>(sizeA+2);
            Set<String> stringsB = new HashSet<>(sizeB+2);
            for(int i=0; i<sizeA; i++){
                stringsA.add(StringBaseOpt.castObjectToString(listA.get(i)));
            }
            for(int i=0; i<sizeB; i++){
                stringsB.add(StringBaseOpt.castObjectToString(listB.get(i)));
            }
            for(int i=0; i<minSize; i++){
                String sA = StringBaseOpt.castObjectToString(listA.get(i));
                if(!stringsB.contains(sA)){
                    different.add(new JsonDifferent("["+i+"]", JsonDifferent.JSON_DIFF_TYPE_DELETE, listA.get(i), null));
                }
                String sB = StringBaseOpt.castObjectToString(listB.get(i));
                if(!stringsA.contains(sB)){
                    different.add(new JsonDifferent("["+i+"]", JsonDifferent.JSON_DIFF_TYPE_ADD, null, listB.get(i)));
                }
            }
            for(int i=minSize; i<sizeA; i++){
                String sA = StringBaseOpt.castObjectToString(listA.get(i));
                if(!stringsB.contains(sA)){
                    different.add(new JsonDifferent("["+i+"]", JsonDifferent.JSON_DIFF_TYPE_DELETE, listA.get(i), null));
                }
            }
            for(int i=minSize; i<sizeB; i++) {
                String sB = StringBaseOpt.castObjectToString(listB.get(i));
                if(!stringsA.contains(sB)){
                    different.add(new JsonDifferent("["+i+"]", JsonDifferent.JSON_DIFF_TYPE_ADD, null, listB.get(i)));
                }
            }
        }

        if(different.isEmpty()){
            return null;
        }
        if(different.size()==1){
            JsonDifferent updateDiff = different.get(0);
            updateDiff.setJsonPath(jsonPath+updateDiff.getJsonPath());
            return updateDiff;
        }
        JsonDifferent updateDiff = new JsonDifferent(jsonPath, JsonDifferent.JSON_DIFF_TYPE_UPDATE, null, null);
        updateDiff.setDiffChildren(different);
        return updateDiff;
    }

    @SuppressWarnings("unchecked")
    public static JsonDifferent objectDiff(String jsonPath, Object objectA, Object objectB, String ... arrayKeys){
        if(objectA instanceof Map && objectB instanceof Map){
            return mapDiff(jsonPath, (Map<String, Object>)objectA, (Map<String, Object>)objectB, arrayKeys);
        }

        if(objectA instanceof List && objectB instanceof List){
            return listDiff(jsonPath, (List<Object>)objectA, (List<Object>)objectB, arrayKeys);
        }

        if(GeneralAlgorithm.equals(objectA, objectB)) {
            return null;
        }

        if(objectA == null ){
            return new JsonDifferent(jsonPath, JsonDifferent.JSON_DIFF_TYPE_ADD, null, objectB);
        } else if(objectB == null ){
            return new JsonDifferent(jsonPath, JsonDifferent.JSON_DIFF_TYPE_DELETE, objectA, null);
        } else {
            String strA = StringBaseOpt.objectToString(objectA);
            String strB = StringBaseOpt.objectToString(objectB);
            String[] stringsA = strA.split("\\n");
            String[] stringsB = strB.split("\\n");
            if(stringsA.length>1 && stringsB.length>1){
                JsonDifferent updateDiff = listDiff(jsonPath, CollectionsOpt.arrayToList(stringsA), CollectionsOpt.arrayToList(stringsB) , arrayKeys);
                if(updateDiff==null){
                    return null;
                }
                if(updateDiff.getDiffChildren() == null || updateDiff.getDiffChildren().isEmpty()){
                    updateDiff.setNewData("多行字符串对比没有差异，应该是行的顺序不一致！");
                }
                return updateDiff;
            }else
                return new JsonDifferent(jsonPath, JsonDifferent.JSON_DIFF_TYPE_UPDATE, objectA, objectB);
        }
    }

    public static JsonDifferent diff(Object objectA, Object objectB, String ... arrayKeys){
        return objectDiff("", objectA, objectB, arrayKeys);
    }
    /* 目前只支持一维数值
     * @return
     */
    private static JSONKey praseJosnKey(String skey) {
        JSONKey key = new JSONKey();
        key.ind = -1;
        int nL = skey.indexOf('[');
        if (nL > 0) {
            key.skey = skey.substring(0, nL);
            int nR = skey.indexOf(']');
            if (nR > nL + 1)
                key.ind = Integer.parseInt(skey.substring(nL + 1, nR));
            else
                key.ind = 0;
        } else
            key.skey = skey;
        return key;
    }

    private static JSONPath findJsonObject(JSONObject objJson, int depth, String[] skeys) {
        JSONPath p = new JSONPath();
        //p.found = false;
        //p.pathPos = 0;
        int nLast = 0;
        JSONObject lastKeyJson = objJson;
        while (nLast < depth) {
            JSONKey key = praseJosnKey(skeys[nLast]);

            if (!lastKeyJson.containsKey(key.skey))
                break;
            Object obj = lastKeyJson.get(key.skey);
            if (obj instanceof JSONObject) {
                if (key.ind >= 0)
                    break;
                lastKeyJson = (JSONObject) obj;
                nLast++;
            } else if (obj instanceof JSONArray) {
                if (key.ind < 0)
                    break;
                JSONArray jarray = (JSONArray) obj;
                if (key.ind >= jarray.size())
                    break;
                Object obj2 = jarray.get(key.ind);
                if (obj2 instanceof JSONObject) {
                    lastKeyJson = (JSONObject) obj2;
                    nLast++;
                } else
                    break;
            } else
                break;
        }
        p.found = true;
        p.pathPos = nLast;
        p.objJson = lastKeyJson;
        return p;
    }

    private static JSONObject innerCreateJsonObject(String[] skeys, int beginPos, Object value) {
        int depth = skeys.length;
        if (depth == 0)
            return null;
        int nLast = beginPos;

        JSONKey key = praseJosnKey(skeys[depth - 1]);
        JSONObject leafKey = new JSONObject();
        if (key.ind < 0) {
            leafKey.put(key.skey, value);
        } else {
            JSONArray jarray = new JSONArray();
            for (int i = 0; i < key.ind; i++)
                jarray.add(i, null);
            jarray.add(key.ind, value);
            leafKey.put(key.skey, jarray);
        }
        depth = depth - 2;
        while (depth >= nLast) {
            key = praseJosnKey(skeys[depth]);
            JSONObject tempKey = new JSONObject();
            if (key.ind < 0)
                tempKey.put(key.skey, leafKey);
            else {
                JSONArray jarray = new JSONArray();
                for (int i = 0; i < key.ind; i++)
                    jarray.add(i, null);
                jarray.add(key.ind, leafKey);
                tempKey.put(key.skey, jarray);
            }
            leafKey = tempKey;
            depth--;
        }
        return leafKey;
    }

    /*
     * 给设置json对象属性值
     * @param objJson
     * @param path 用'.'隔开的 属性路径 比如 a.b
     * @param value
     */
    @SuppressWarnings("unchecked")
    public static void setAttribute(JSONObject objJson, String path, Object value) {
        String[] skeys = path.split("\\x2E");
/*        if(skeys==null)
            return;*/
        int depth = skeys.length;
        if (depth == 0)
            return;
        JSONPath jpath = findJsonObject(objJson, depth - 1, skeys);
        Object jsonValue = value;
        if (jpath.pathPos < depth - 1)
            jsonValue = innerCreateJsonObject(skeys, jpath.pathPos + 1, value);

        JSONKey key = praseJosnKey(skeys[jpath.pathPos]);
        if (key.ind < 0) {
            /* Map（JSONObject） 也必须作为一个变量设置，不能作为添加属性的方式。
            if( jpath.pathPos == depth-1 && jsonValue instanceof Map){
                for(Map.Entry<Object,Object> ent : ((Map<Object,Object>)value).entrySet() ){
                    jpath.objJson.element(ent.getLeft().toString(), ent.getRight());
                }
            }else */
            jpath.objJson.put(key.skey, jsonValue);
        } else {
            //判断是否有节点，如果有设置对应的变量
            if (jpath.objJson.containsKey(key.skey)) {
                Object leafJson = jpath.objJson.get(key.skey);
                //如果变量已经是数组，设置对应的值
                if (leafJson instanceof JSONArray) {
                    JSONArray jarray = ((JSONArray) leafJson);
                    //对应位置的数值不存在
                    if (jarray.size() < key.ind + 1) {
                        for (int i = jarray.size(); i < key.ind; i++)
                            jarray.add(i, null);
                        jarray.add(key.ind, jsonValue);
                    } else {//对应位置的数值不存在，设置相关的变量
                        if (jpath.pathPos == depth - 1 && jsonValue instanceof Map) {
                            Object obj = jarray.get(key.ind);
                            if (obj instanceof JSONObject)
                                for (Map.Entry<Object, Object> ent : ((Map<Object, Object>) value).entrySet())
                                    ((JSONObject) obj).put(ent.getKey().toString(), ent.getValue());
                            else
                                jarray.set(key.ind, jsonValue);
                        } else
                            jarray.set(key.ind, jsonValue);
                    }
                } else {//不是数组，设置为数据
                    JSONArray jarray = new JSONArray();
                    for (int i = 0; i < key.ind; i++)
                        jarray.add(i, null);
                    jarray.add(key.ind, jsonValue);
                    jpath.objJson.put(key.skey, jarray);
                }
            } else {//没有键值，创建键值
                JSONArray jarray = new JSONArray();
                for (int i = 0; i < key.ind; i++)
                    jarray.add(i, null);
                jarray.add(key.ind, jsonValue);
                jpath.objJson.put(key.skey, jarray);
            }

        }
    }

    /**
     * 给设置json对象属性 添加新值，如果没有这个属性和设置属性一样，如果已经有属性值 则属性转换为数组
     *
     * @param objJson objJson
     * @param path    用'.'隔开的 属性路径 比如 a.b
     * @param value   value
     */
    public static void appendData(JSONObject objJson, String path, Object value) {
        String[] skeys = path.split("\\x2E");
/*        if(skeys==null)
            return;*/
        int depth = skeys.length;
        if (depth == 0)
            return;
        JSONPath jpath = findJsonObject(objJson, depth - 1, skeys);
        Object jsonValue = value;
        if (jpath.pathPos < depth - 1) {
            jsonValue = innerCreateJsonObject(skeys, jpath.pathPos + 1, value);
            JSONKey key = praseJosnKey(skeys[jpath.pathPos]);
            if (key.ind < 0) {
                jpath.objJson.put(key.skey, jsonValue);
            } else {
                JSONArray jarray = new JSONArray();
                for (int i = 0; i < key.ind; i++)
                    jarray.add(i, null);
                jarray.add(key.ind, jsonValue);
                jpath.objJson.put(key.skey, jarray);
            }
        } else {
            JSONKey key = praseJosnKey(skeys[depth - 1]);
            if (jpath.objJson.containsKey(key.skey))
                jpath.objJson.put(key.skey, value);
            else {
                if (key.ind < 0) {
                    jpath.objJson.put(key.skey, value);
                } else {
                    JSONArray jarray = new JSONArray();
                    for (int i = 0; i < key.ind; i++)
                        jarray.add(i, null);
                    jarray.add(key.ind, value);
                    jpath.objJson.put(key.skey, jarray);
                }
            }
        }
    }

    /**
     * 给设置json对象属性 添加多个新值
     *
     * @param objJson objJson
     * @param path    path
     * @param values  values
     */
    public static void batchAppendData(JSONObject objJson, String path, Object[] values) {
        for (Object obj : values) {
            appendData(objJson, path, obj);
        }
    }

    /**
     * 给设置json对象属性 添加多个新值
     *
     * @param objJson objJson
     * @param path    path
     * @param values  values
     */
    public static void batchAppendData(JSONObject objJson, String path, Collection<Object> values) {
        for (Object obj : values)
            appendData(objJson, path, obj);
    }

    public static String objectToJSONString(Object obj) {
        return objectToJSONString(obj, false, false);
    }

    public static String objectToJSONString(Object obj, boolean methodOnly, boolean fieldOnly) {
        return objectToJSONString(obj, methodOnly, fieldOnly, false);
    }

    public static String objectToJSONString(Object obj, boolean methodOnly, boolean fieldOnly, boolean includePrivateField) {
        if (obj == null)
            return null;

        if (ReflectionOpt.isScalarType(obj.getClass())) {
            //StringBaseOpt.objectToString(obj);
            return obj.toString();
        }

        if (obj instanceof JSONObject || obj instanceof JSONArray)
            return obj.toString();

        if (ReflectionOpt.isArray(obj))
            return  arrayToJSONArray(obj, methodOnly, fieldOnly, includePrivateField).toString();

        return objectToJSONObject(obj, methodOnly, fieldOnly, includePrivateField).toString();
    }

    /**
     * @param object 对象
     * @return map
     * @see com.centit.support.algorithm.CollectionsOpt  objectToMap
     */
    @Deprecated
    public static Map<String, Object> objectToMap(Object object) {
        return CollectionsOpt.objectToMap(object);
    }

    /**
     * 将一个对象转换为JSON, 如果是 数值 或者 实现collect接口 则转换为 JSONArray否则转换为JSONObject
     *
     * @param obj 将一个对象转换为JSON
     * @return 转换为 JSONArray否则转换为JSONObject
     */
    public static Object objectToJSON(Object obj) {
        return objectToJSON(obj, false, false);
    }

    /*  将一个对象转换为JSON, 如果是 数值 或者 实现collect接口 则转换为 JSONArray否则转换为JSONObject
     * methodOnly 和 fieldOnly 不能同时为 true
     * @param obj
     * @param methodOnly
     * @param fieldOnly
     * @param includePrivateField 包括私有属性，methodOnly 为true是这个参数无效
     * @return
     */
    public static Object objectToJSON(Object obj, boolean methodOnly, boolean fieldOnly, boolean includePrivateField) {

        if (obj instanceof JSONObject || obj instanceof JSONArray)
            return obj;

        if (ReflectionOpt.isArray(obj))
            return arrayToJSONArray(obj, methodOnly, fieldOnly, includePrivateField);
        else
            return objectToJSONObject(obj, methodOnly, fieldOnly, includePrivateField);
    }

    /*  将一个对象转换为JSON, 如果是 数值 或者 实现collect接口 则转换为 JSONArray否则转换为JSONObject
     * methodOnly 和 fieldOnly 不能同时为 true
     * @param obj
     * @param methodOnly
     * @param fieldOnly
     * @return
     */
    public static Object objectToJSON(Object obj, boolean methodOnly, boolean fieldOnly) {
        return objectToJSON(obj, methodOnly, fieldOnly, false);
    }

    /*
     * 将一个对象转换为JSON对象，method方法优先
     * @param obj
     * @return
     */
    public static JSONObject objectToJSONObject(Object obj) {
        return objectToJSONObject(obj, false, false, false);
    }

    /*
     * 将一个对象转换为 JSON 值， 如果是简单类型则转换为一个 String
     * @param value
     * @param methodOnly
     * @param fieldOnly
     * @return
     */
    private static Object makeJSONValue(Object value, boolean methodOnly, boolean fieldOnly) {
        if (value == null)
            return null;//"";
        else if (ReflectionOpt.isScalarType(value.getClass()))
            return value;// String.valueof(value)
        else {
            return objectToJSON(value, methodOnly, fieldOnly);
        }
    }

    /* 将一个对象转换为JSON对象
     * methodOnly 和 fieldOnly 不能同时为 true
     * @param obj
     * @param methodOnly
     * @param fieldOnly
     * @param includePrivateField 包括私有属性，methodOnly 为true是这个参数无效
     * @return
     */
    public static JSONObject objectToJSONObject(Object obj, boolean methodOnly, boolean fieldOnly, boolean includePrivateField) {
        if (obj == null)
            return null;
        if (obj instanceof JSONObject)
            return (JSONObject) obj;

        JSONObject jObj = new JSONObject();

        if (ReflectionOpt.isScalarType(obj.getClass())) {
            jObj.put("value", obj.toString());
            return jObj;
        }
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) obj;
            for (Map.Entry<Object, Object> ent : map.entrySet()) {

                jObj.put(ent.getKey().toString(),
                    makeJSONValue(ent.getValue(), methodOnly, fieldOnly));
            }
            return jObj;
        }

        List<String> methodNames = new ArrayList<>();
        List<Method> getMethods = ReflectionOpt.getAllGetterMethod(obj.getClass());
        if (!fieldOnly && getMethods != null) {
            for (Method m : getMethods) {
                String fieldName = ReflectionOpt.methodNameToField(m.getName());
                methodNames.add(fieldName);
                Object value = null;
                try {
                    value = m.invoke(obj);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
                }
                jObj.put(fieldName,
                    makeJSONValue(value, methodOnly, fieldOnly));
            }
        }
        //obj.getClass().getDeclaredFields();
        Field[] fls = ReflectionOpt.getFields(obj);
        if (!methodOnly && fls != null) {
            for (Field fl : fls) {
                if (methodNames.contains(fl.getName()))
                    continue;

                if (includePrivateField || Modifier.isPublic(fl.getModifiers())) {
                    //fl.isAccessible()
                    Object value = ReflectionOpt.forceGetFieldValue(obj, fl);
                    jObj.put(fl.getName(),
                        makeJSONValue(value, methodOnly, fieldOnly));
                }
            }
        }

        if ((fls == null || fls.length < 1) && (getMethods == null || getMethods.isEmpty())) {
            jObj.put("value", obj.toString());
        }
        return jObj;
    }

    public static JSONObject objectToJSONObject(Object obj, boolean methodOnly, boolean fieldOnly) {
        return objectToJSONObject(obj, methodOnly, fieldOnly, false);
    }

    /*
     * 将一个对象转换为JSON对象，method方法优先
     * @param obj
     * @return
     */
    public static JSONArray arrayToJSONArray(Object obj) {
        return arrayToJSONArray(obj, false, false, false);
    }

    /**
     * 将一个对象转换为JSON对象
     * methodOnly 和 fieldOnly 不能同时为 true
     *
     * @param objArray            objArray
     * @param methodOnly          methodOnly
     * @param fieldOnly           fieldOnly
     * @param includePrivateField 包括私有属性，methodOnly 为true是这个参数无效
     * @return 将一个对象转换为JSON对象
     */
    public static JSONArray arrayToJSONArray(Object objArray, boolean methodOnly, boolean fieldOnly, boolean includePrivateField) {
        if (objArray == null)
            return null;
        if (objArray instanceof JSONArray)
            return (JSONArray) objArray;

        JSONArray jArray = new JSONArray();
        if (objArray instanceof Object[]) {
            Object[] objList = (Object[]) objArray;
            for (Object obj : objList) {
                jArray.add(makeJSONValue(obj, methodOnly, fieldOnly));
            }
        } else if (objArray instanceof Collection<?>) {
            @SuppressWarnings("unchecked")
            Collection<Object> objList = (Collection<Object>) objArray;
            for (Object obj : objList) {
                jArray.add(makeJSONValue(obj, methodOnly, fieldOnly));
            }
        } else {
            //System.out.println( objArray.getClass().getName());
            if (int[].class.equals(objArray.getClass())) {
                int[] objList = (int[]) objArray;
                for (int obj : objList) {
                    jArray.add(obj);//String.valueOf
                }
            } else if (long[].class.equals(objArray.getClass())) {//是J不是L没有写错。
                long[] objList = (long[]) objArray;
                for (long obj : objList) {
                    jArray.add(obj);
                }
            } else if (float[].class.equals(objArray.getClass())) {
                float[] objList = (float[]) objArray;
                for (float obj : objList) {
                    jArray.add(obj);
                }
            } else if (double[].class.equals(objArray.getClass())) {
                double[] objList = (double[]) objArray;
                for (double obj : objList) {
                    jArray.add(obj);
                }
            } else if (char[].class.equals(objArray.getClass())) {
                char[] objList = (char[]) objArray;
                for (char obj : objList) {
                    jArray.add(obj);
                }
            }
        }

        return jArray;
    }

    public static JSONArray arrayToJSONArray(Object objArray, boolean methodOnly, boolean fieldOnly) {
        return arrayToJSONArray(objArray, methodOnly, fieldOnly, false);
    }

    static class JSONPath {
        JSONObject objJson;
        String path;
        boolean found;
        int pathPos;
    }

    static class JSONKey {
        String skey;
        int ind;
    }

    public static Object parseJsonString(String jsonStr) {
        if(StringUtils.isNotBlank(jsonStr) && ( jsonStr.startsWith("{") || jsonStr.startsWith("[")))
            return JSON.parse(jsonStr);
        return jsonStr;
    }

}
