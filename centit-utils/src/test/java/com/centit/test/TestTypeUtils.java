package com.centit.test;

import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;

public class TestTypeUtils {

    public static void main(String[] args) {
        System.out.println(StringRegularOpt.isFalse("  "));
        System.out.println(BooleanBaseOpt.castObjectToBoolean("  ", true));
        System.out.println(NumberBaseOpt.parseInteger("1", null));
        /*List<String> ls = new ArrayList<>();
        ls.add("hello");
        ls.add("world");
        ls.add("ok!");

        String [] as = CollectionsOpt.listToArray(ls);
        for(String s:as)
            System.out.println(s);*/

    }

}
