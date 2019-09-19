package com.centit.test;

import com.alibaba.fastjson.JSON;
import com.centit.support.network.HttpExecutor;
import com.centit.support.network.HttpExecutorContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TestTcpip {
     public static void main(String arg[]){

         Map<String ,Object> formData = new HashMap<String ,Object>();
         formData.put("catalogCode","TestData");
         formData.put("catalogStyle","U");
         formData.put("catalogType","L");
         formData.put("catalogName","测试  PUT");
         formData.put("catalogDesc","测试修改！");
         formData.put("fieldDesc"," 字典字段描述");
         formData.put("needCache","1");
         formData.put("optId","MGH");


         try {
             JSON json = (JSON)JSON.toJSON(formData);
             String uri = "http://192.168.133.11:8180/centit/service/sys/testText";
             String sRet = HttpExecutor.simplePost(HttpExecutorContext.create(), uri,
                     json.toJSONString(),false );
             System.out.println(sRet);
        } catch (IOException e) {

            //e.printStackTrace();
        }
     }

     public static void testPsts(){

         String uri = "http://192.168.133.11:8180/centit/service/sys/datacatalog/TestData";

         Map<String ,Object> formData = new HashMap<String ,Object>();
         formData.put("catalogCode","TestData");
         formData.put("catalogStyle","U");
         formData.put("catalogType","L");
         formData.put("catalogName","测试  PUT");
         formData.put("catalogDesc","测试修改！");
         formData.put("fieldDesc"," 字典字段描述");
         formData.put("needCache","1");
         formData.put("optId","MGH");

         try {
             String sRet = HttpExecutor.formPost(HttpExecutorContext.create(), uri,
                    formData,true );
             System.out.println(sRet);
        } catch (IOException e) {

            //e.printStackTrace();
        }

     }
}
