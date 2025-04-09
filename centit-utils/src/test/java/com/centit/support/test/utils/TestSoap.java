package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.centit.support.file.FileIOOpt;
import com.centit.support.network.HttpExecutor;
import com.centit.support.network.HttpExecutorContext;
import com.centit.support.network.SoapWsdlParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import java.io.IOException;
import java.util.Map;

public class TestSoap {
    public static void main(String arg[]) {
        try {
            String wsdl = HttpExecutor.simpleGet(HttpExecutorContext.create(),
                "http://192.168.132.70/WebService/DataExchange.asmx?wsdl");
            //String wsdl = FileIOOpt.readStringFromFile("/Users/codefan/Downloads/cmcc_mas_wbs.xml");
            Document doc = DocumentHelper.parseText(wsdl);
            Map<String, String> stringMap = SoapWsdlParser.getSoapActionParams(doc.getRootElement(),"GetData");
            System.out.println(JSON.toJSONString(stringMap));
            System.out.println( SoapWsdlParser.getSoapActionInputName(doc.getRootElement(),"GetData"));
        } catch (IOException | DocumentException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Done!");
    }
}
