package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.xml.XMLObject;

public class TestXmlObject {

    public static void main(String[] args) {

       String xmlBoday = XMLObject.objectToXMLString("getData", "act", "http://tempuri.org",
           CollectionsOpt.createHashMap("funName", "string1", "userName",
               "string2", "password", "string3",
               "paramXml", "string4"), false, false);

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
