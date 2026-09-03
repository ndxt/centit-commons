package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.file.FileIOOpt;
import com.centit.support.xml.XMLObject;

import java.io.IOException;
import java.util.Map;

public class TestXmlObject {

    public static void main(String[] args) throws IOException {
        //String xmlBody = FileIOOpt.readStringFromInputStream(TestXmlObject.class.getResourceAsStream("/test2.xml"));
        //Object obj = XMLObject.xmlStringToObject(xmlBody);
        Map<String, Object> obj = CollectionsOpt.createHashMap("funName", "string1", "userName",
            "", "password", null,
            "paramXml", "string4");

        // 默认参数：ignoreNullValue=false 输出 null 空标签，prettyFormat=true 格式化输出
        System.out.println("---------- 默认：ignoreNullValue=false, prettyFormat=true ----------");
        System.out.println(XMLObject.objectToXMLString("档案实体", obj, false, false, true));

        // 忽略 null 值：password 字段不生成标签
        System.out.println("---------- ignoreNullValue=true ----------");
        System.out.println(XMLObject.objectToXMLString("档案实体", obj, false, false, true, true, true));

        // 不格式化：紧凑输出为一行
        System.out.println("---------- prettyFormat=false ----------");
        System.out.println(XMLObject.objectToXMLString("档案实体", obj, false, false, true, false, false));

        // 复合属性（Map、数组）的格式化效果，addTypeAttr=true 时 null 值带 type="Null" 属性，并可解析还原
        Map<String, Object> complexObj = CollectionsOpt.createHashMap(
            "userName", "codefan",
            "role", CollectionsOpt.createHashMap("roleId", 1L, "roleName", null),
            "depts", CollectionsOpt.createList("dev", "test", null));
        String xmlBody = XMLObject.objectToXMLString("user", complexObj, true, false, false);
        System.out.println("---------- 复合对象 addTypeAttr=true ----------");
        System.out.println(xmlBody);
        System.out.println(JSON.toJSONString(XMLObject.xmlStringToObject(xmlBody)));
    }
    public static void main2(String[] args) {

       String xmlBoday = XMLObject.objectToXMLString("getData", "act", "http://tempuri.org",
           CollectionsOpt.createHashMap("funName", "string1", "userName",
               "string2", "password", "string3",
               "paramXml", "string4"), false, false, true);

       System.out.println(xmlBoday);

        xmlBoday = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "               xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "    <soap:Body>\n" +
            "        <HelloWorldResponse xmlns=\"http://tempuri.org/\">\n" +
            "            <HelloWorldResult>Hello World</HelloWorldResult>\n" +
            "        </HelloWorldResponse>\n" +
            "    </soap:Body>\n" +
            "</soap:Envelope>";
        Object obj = XMLObject.xmlStringToObject(xmlBoday);
        System.out.println(JSON.toJSONString(obj));
    }
}
