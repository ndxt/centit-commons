package com.centit.test;

/**
 * Created by codefan on 2017/11/3.
 */
public class DemoClass implements DemoInterface {
    private String name;

    public DemoClass(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
