package com.centit.support.test;

import com.centit.support.security.DesensitizeOptUtils;

public class TestSecurity {
    public static void main(String[] args) {
        System.out.println(DesensitizeOptUtils.idCardNum("320828197810231836"));
        System.out.println(DesensitizeOptUtils.chineseName("杨淮生先生", 3));
        System.out.println(DesensitizeOptUtils.phone("18602554255"));
        System.out.println(DesensitizeOptUtils.phone("02584207500"));
        System.out.println(DesensitizeOptUtils.phone("84207500"));
    }
}
