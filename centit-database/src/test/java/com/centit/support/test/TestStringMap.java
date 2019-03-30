package com.centit.support.test;

import com.centit.support.database.utils.FieldType;

public class TestStringMap {
    public static void main(String[] args) {
        System.out.println(FieldType.mapPropName("F_OPT_INFO"));
        System.out.println(FieldType.mapClassName("F_OPT_INFO"));

        System.out.println(FieldType.mapPropName("__F__OPT_F__INFO"));
        System.out.println(FieldType.mapClassName("_F_P_D_OPT_INFO"));

        System.out.println(FieldType.mapPropName("______"));
        System.out.println(FieldType.mapClassName("F"));
        System.out.println(FieldType.mapPropName("F_"));
    }
}
