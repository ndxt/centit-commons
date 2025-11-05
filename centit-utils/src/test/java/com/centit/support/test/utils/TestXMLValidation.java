package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSONObject;
import com.centit.support.xml.XMLSchemaValidationUtil;

public class TestXMLValidation {
    public static void main(String[] args) {
        JSONObject jsonObject =
        XMLSchemaValidationUtil.validate(TestXMLValidation.class.getResourceAsStream("/test.xsd"),
            TestXMLValidation.class.getResourceAsStream("/test.xml"));
        System.out.println(jsonObject.toString());
    }
}
