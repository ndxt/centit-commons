package com.centit.support.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.database.utils.DBType;
import com.centit.support.database.utils.FieldType;

import java.util.Map;

public class TestStringMap {
    public static void main(String[] args) {
        Boolean b = true;
        System.out.println(JSON.class.isAssignableFrom(JSONObject.class));
        System.out.println(JSON.class.isAssignableFrom(JSONArray.class));
        System.out.println(Map.class.isAssignableFrom(JSONObject.class));
        System.out.println(JSON.class.isAssignableFrom(Map.class));

        byte[] a= new byte[23];
        System.out.println(FieldType.mapToFieldType(a.getClass()));

        System.out.println(DBType.valueOf("Oracle"));
        System.out.println(FieldType.mapPropName("F_OPT_INFO"));
        System.out.println(FieldType.mapPropName("abcAdafCde"));

        System.out.println(FieldType.mapPropName("F_OPT_INFO"));
        System.out.println(FieldType.mapClassName("F_OPT_INFO"));

        System.out.println(FieldType.mapPropName("__F__OPT_F__INFO"));
        System.out.println(FieldType.mapPropName("_F_P_D_OPT_INFO"));

        System.out.println(FieldType.mapPropName("______"));
        System.out.println(FieldType.mapClassName("F"));
        System.out.println(FieldType.mapPropName("F_"));
    }
}
