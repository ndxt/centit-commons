package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.Lunar;
import com.centit.support.algorithm.ReflectionOpt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.function.Supplier;

public class TestReflectionOpt {

    public static class MapSupplier implements Supplier<Map<String, Object>> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        @Override
        public Map<String, Object> get() {
            return CollectionsOpt.createHashMap("a",1,"b",2,"c","hello world");
        }
    }

    public static void main(String arg[]) {

        Map<String, Object> map = CollectionsOpt.createHashMap(
            "adb",1,"b",2, "d", new MapSupplier(), "f",(Supplier<String>)()->"hello");
        Object[] objects = new Object[]{map, map, map};
        System.out.println(
            JSON.toJSONString(ReflectionOpt.attainExpressionValue(CollectionsOpt.createHashMap("abc", objects),
                "abc[1].b")));

        //System.out.println(ReflectionOpt.isScalarType(strings.getClass()));
        System.out.println(String.class.getPackage().getName());

        for (Field field : Lunar.class.getDeclaredFields()) {
            System.out.print(field.getName() + ": ");
            System.out.println(boolean.class.equals(field.getType()));
        }
    }

    public static void getMethodType() {
        Method[] methods = Lunar.class.getDeclaredMethods();
        for (Method md : methods) {
            System.out.println(md.getName());
            Parameter[] params = md.getParameters();
            if (params != null) {
                for (Parameter p : params) {
                    System.out.println("   " + p.getName());
                }
            }
            System.out.println();
        }
    }

    public static void getTypeMatch() {
        int[] objArray = new int[10];
        if (int[].class.isAssignableFrom(objArray.getClass())) {

            System.out.println("OK!");
        }
        System.out.println(java.util.Date.class.isAssignableFrom(java.sql.Date.class));
        System.out.println(java.util.Date.class.isAssignableFrom(java.util.Date.class));
        System.out.println(java.sql.Date.class.isAssignableFrom(java.util.Date.class));

        for (Method md : ReflectionOpt.getAllGetterMethod(String.class)) {
            System.out.println(md.getName());

        }
    }
}
