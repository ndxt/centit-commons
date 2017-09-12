package com.centit.support.algorithm;

/**
 * Created by codefan on 17-9-7.
 */
@SuppressWarnings("unused")
public abstract  class GeneralAlgorithm {

    public static <T> T nvl(T obj, T obj2){
        return obj==null ? obj2 : obj;
    }

    public static <T> T nvl2(Object obj, T obj2, T obj3){
        return obj==null ? obj3 : obj2;
    }
}
