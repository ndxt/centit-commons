package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.centit.support.network.HttpExecutor;
import com.centit.support.network.HttpExecutorContext;
import com.centit.support.network.UrlOptUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TestTcpip {
    public static void main(String arg[]) {
        System.out.println(UrlOptUtils.fetchFilenameFromUrl("abcedfg.pdf"));
        System.out.println(UrlOptUtils.fetchFilenameFromUrl("abcedfg.pdf?ajax=true"));
        System.out.println(UrlOptUtils.fetchFilenameFromUrl("http://baidu.com/abcedfg.pdf?ajax=true"));
        System.out.println(UrlOptUtils.fetchFilenameFromUrl("http://baidu.com/abcedfg.pdf"));
    }
    public static void testPosts() {
        Map<String, Object> formData = new HashMap<String, Object>();
        formData.put("catalogCode", "TestData");
        formData.put("catalogStyle", "U");
        formData.put("catalogType", "L");
        formData.put("catalogName", "测试  PUT");
        formData.put("catalogDesc", "测试修改！");
        formData.put("fieldDesc", " 字典字段描述");
        formData.put("needCache", "1");
        formData.put("optId", "MGH");


        try {
            Object json = JSON.toJSON(formData);
            String uri = "http://192.168.133.11:8180/centit/service/sys/testText";
            String sRet = HttpExecutor.simplePost(HttpExecutorContext.create(), uri,
                json.toString(), false);
            System.out.println(sRet);
        } catch (IOException e) {

            //logger.error(e.getMessage(), e);
        }
    }

    public static void testPsts() {

        String uri = "http://192.168.133.11:8180/centit/service/sys/datacatalog/TestData";

        Map<String, Object> formData = new HashMap<String, Object>();
        formData.put("catalogCode", "TestData");
        formData.put("catalogStyle", "U");
        formData.put("catalogType", "L");
        formData.put("catalogName", "测试  PUT");
        formData.put("catalogDesc", "测试修改！");
        formData.put("fieldDesc", " 字典字段描述");
        formData.put("needCache", "1");
        formData.put("optId", "MGH");

        try {
            String sRet = HttpExecutor.formPost(HttpExecutorContext.create(), uri,
                formData, true);
            System.out.println(sRet);
        } catch (IOException e) {

            //logger.error(e.getMessage(), e);
        }

    }
}
