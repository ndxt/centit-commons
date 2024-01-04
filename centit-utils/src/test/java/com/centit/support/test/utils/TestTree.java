package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;

import java.util.List;

public class TestTree {
    public static void main(String[] args) {
        JSONObject jsonObject = JSON.parseObject("{'a': 1," +
            " 'c':[ {'a':12, 'c' : [{'a':121, 'c':[]}, {'a':122, 'c':[]},{'a':123, 'c':[]}]}, " +
              " {'a':13, 'c': [{'a':131, 'c':[]}, {'a':132, 'c':[{'a':1321, 'c':[]}, {'a':1322, 'c':[]},{'a':1323, 'c':[]}]},{'a':133, 'c':[]}]}," +
              " {'a':14, 'c':[]}," +
               "{'a':15, 'c':[{'a':151, 'c':[]}, {'a':152, 'c':[]} ]}]}");
        List<Object> objects = CollectionsOpt.breadthFirstTraverseTree(jsonObject, "c");
        for(Object object : objects){
            System.out.print( ((JSONObject) object).getInteger("a"));
            System.out.print(",");
        }
        System.out.println();
        objects = CollectionsOpt.depthFirstTraverseTree(jsonObject, "c");
        for(Object object : objects){
            System.out.print( ((JSONObject) object).getInteger("a"));
            System.out.print(",");
        }
        System.out.println();
    }
}
