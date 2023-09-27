package com.centit.support.test;

import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.Mathematics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by codefan on 2017/11/3.
 *
 * @author codefan
 */
public class TestListOpt {
    public static void main(String arg[]) {
        List<Integer> sortData = CollectionsOpt.arrayToList(new Integer[]{3,2,1,4});
        int c =0 ;
        while (Mathematics.nextPermutation(sortData, (a,b)-> a-b)) {
            System.out.println(sortData);
            c++;
            if(c>50)
                break;
        }
        System.out.println(c);
    }
    public static void main2(String arg[]) {
        JSONObject strMap = new JSONObject();
        strMap.put("a", "B");
        strMap.put("c", "D");
        strMap.put("e", null);
        strMap.put("f", null);
        strMap.put("a", null);
        strMap.remove("a");
        strMap.remove("hi");

        JSONObject jsonObject = JSONObject.from(strMap);
        System.out.println(jsonObject.toString());

        List<DemoInterface> strs = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            strs.add(new DemoClass("name" + i));
        }
        //DemoInterface[] strarray = CollectionsOpt.listToArray(strs);
        String [] objs = strs.stream().map(DemoInterface::getName).toArray(String[]::new);
        for (int i = 0; i < 10; i++) {
            System.out.println(objs[i]);
        }
    }
}
