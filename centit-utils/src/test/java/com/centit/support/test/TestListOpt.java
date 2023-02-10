package com.centit.support.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by codefan on 2017/11/3.
 *
 * @author codefan
 */
public class TestListOpt {
    public static void main(String arg[]) {
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
