package com.centit.support.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.ReflectionOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    /**
     * 获取一个json对象的属性，skeys是json属性数组，a.b 这样的属性对应的数组为{'a','b'}
     *
     * @param objJson objJson
     * @param skeys   json属性数组，a.b 这样的属性对应的数组为{'a','b'}
     * @return 获取一个json对象的属性
     */
    private static JSONObject findJsonObject(JSONObject objJson, String[] skeys) {
        int depth = skeys.length;
        if (depth < 1)
            return null;
        JSONPath path = findJsonObject(objJson, depth, skeys);
        if (path.pathPos != depth)
            return null;
        return path.objJson;
    }

    /**
     * 获取一个json对象的属性，path是json属性 可以有多层，用'.' 分隔 ，比如 a.b
     *
     * @param objJson objJson
     * @param path    json属性 可以有多层，用'.' 分隔 ，比如 a.b
     * @return 获取一个json对象的属性
     */
    public static JSONObject findJsonObject(JSONObject objJson, String path) {
        String[] skeys = path.split("\\x2E");
/*        if(skeys==null)
            return null;*/
        return findJsonObject(objJson, skeys);
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
        if (obj instanceof JSON)
            return ((JSON) obj).toJSONString();

        if (ReflectionOpt.isArray(obj))
            return arrayToJSONArray(obj, methodOnly, fieldOnly, includePrivateField).toJSONString();

        return objectToJSONObject(obj, methodOnly, fieldOnly, includePrivateField).toJSONString();
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
    public static JSON objectToJSON(Object obj) {
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
    public static JSON objectToJSON(Object obj, boolean methodOnly, boolean fieldOnly, boolean includePrivateField) {

        if (obj instanceof JSON)
            return (JSON) obj;

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
    public static JSON objectToJSON(Object obj, boolean methodOnly, boolean fieldOnly) {
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

        List<String> methodNames = new ArrayList<String>();
        List<Method> getMethods = ReflectionOpt.getAllGetterMethod(obj.getClass());
        if (!fieldOnly && getMethods != null) {
            for (Method m : getMethods) {
                String fieldName = ReflectionOpt.methodNameToField(m.getName());
                methodNames.add(fieldName);
                Object value = null;
                try {
                    value = m.invoke(obj);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);//e.printStackTrace();
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

        if ((fls == null || fls.length < 1) && (getMethods == null || getMethods.size() < 1)) {
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

    /**
     * @param objs 参数必须是 string object string object ....
     * @return Map &lt; String,Object &gt;
     * @see com.centit.support.algorithm.CollectionsOpt  createHashMap
     * 参数必须是 string object string object ....
     */
    @Deprecated
    public static Map<String, Object> createHashMap(Object... objs) {
        return CollectionsOpt.createHashMap(objs);
    }

    /*
     * 参数必须是 string object string object ....
     * @param objs
     * @return
     */
    public static JSONObject createJSONObject(Object... objs) {
        if (objs == null || objs.length < 2)
            return null;
        JSONObject paramsMap = new JSONObject(objs.length);
        for (int i = 0; i < objs.length / 2; i++) {
            paramsMap.put(String.valueOf(objs[i * 2]), objs[i * 2 + 1]);
        }
        return paramsMap;
    }

    public static JSONArray createJSONOArray(Object... objs) {
        if (objs == null || objs.length < 1)
            return null;
        JSONArray ja = new JSONArray(objs.length);
        for (int i = 0; i < objs.length; i++) {
            ja.add(objectToJSON(objs[i]));
        }
        return ja;
    }

    /*
     * 合并两个json 如果有相同属性，以json2 为准
     * @param json1
     * @param json2
     * @return
     */
    public static JSONObject mergeJSONObjectt(final JSONObject json1, final JSONObject json2) {
        JSONObject json = new JSONObject();
        json.putAll(json1);
        json.putAll(json2);
        return json;
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

}
