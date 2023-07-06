package com.centit.support.test;

import com.centit.support.security.DesensitizeOptUtils;

public class TestSecurity {
    public static void main(String[] args) {
        System.out.println(DesensitizeOptUtils.idCardNum("320828197810231836"));
        System.out.println(DesensitizeOptUtils.chineseName("杨淮生", true));
        System.out.println(DesensitizeOptUtils.mobilePhone("18602554255"));
        System.out.println(DesensitizeOptUtils.fixedPhone("02587207500"));
        System.out.println(DesensitizeOptUtils.fixedPhone("87207500"));
    }
}
