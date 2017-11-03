package com.centit.test;

import com.centit.support.algorithm.ListOpt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by codefan on 2017/11/3.
 * @author codefan
 */
public class TestListOpt {
    public static void main(String arg[]){
        List<DemoInterface> strs = new ArrayList<>(10);
        for(int i=0;i<10;i++){
            strs.add(new DemoClass("name"+i));
        }

        DemoInterface [] strarray = ListOpt.listToArray(strs);

        for(int i=0;i<10;i++){
            System.out.println(strarray[i].getName());
        }
    }
}
