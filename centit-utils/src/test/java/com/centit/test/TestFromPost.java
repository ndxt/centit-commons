package com.centit.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.centit.support.network.HttpExecutor;

public class TestFromPost {
	
    public static void main(String[] args) {

    	testUploadFile();
    	
    }
    public static void testUploadFile(){	
    	CloseableHttpClient httpClient = HttpClients.createDefault();
	    Map<String, File> files = new HashMap<>();
	    files.put("file",new File("D:/temp/server-productsvr.cer"));
	    Map<String,Object> params = new HashMap<>();
	    params.put("osId", "FILE_SVR");
	    params.put("optId","LOCAL_FILE");
	    String jsonStr;
		try {
			jsonStr = HttpExecutor.formPostWithFileUpload(httpClient,
			        "http://codefanpc:8180/product-file/service/upload/file",
			        params,
			        files);
			System.out.println(jsonStr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROE");
		}
	    
    }
    
	public static void testSession(){	


        try {


        	CloseableHttpClient httpClient = HttpClients.createDefault();
        	
       
           String s = HttpExecutor.simpleGet(httpClient, null, 
                    "http://codefanbook:8180/TestSession/TestSession",(String)null);
            
            System.out.println(s);
            
            s = HttpExecutor.simpleGet(httpClient, null, 
                    "http://codefanbook:8180/TestSession/TestSession",(String)null);
            
            System.out.println(s);            
            
            s = HttpExecutor.simpleGet(httpClient, null, 
                    "http://codefanbook:8180/TestSession/TestSession",(String)null);
            
            System.out.println(s);
            
        } catch (Exception e) {
            //e.printStackTrace();
        }
        
	}
	    
	
    
public static void testLogin(){	

    
    //CloseableHttpClient client2 = HttpClients.createDefault();
	HttpClientContext context = HttpClientContext.create(); 
   
    CloseableHttpClient httpClient = HttpExecutor.createHttpClient();

    try {
    	        	
        Map<String,String> params = new HashMap<String,String>();
        params.put("j_username", "admin");
        params.put("j_password", "000000");
        params.put("remember", "true");            
        
        String s = HttpExecutor.formPost(httpClient,context,  
                "http://codefanbook:8180/framework-sys-module/j_spring_security_check?ajax=true", 
                params,true);
        System.out.println(s);
        
                
        //ResponseJSON rj = ResponseJSON.valueOfJson(s);
        //String jsessionid = rj.getDataAsScalarObject(String.class);            
        //context.setAttribute("JSESSIONID", jsessionid);
       
        s = HttpExecutor.simpleGet(httpClient,context,  
                "http://codefanbook:8180/framework-sys-module/service/sys/currentuser",(String)null);
        
        System.out.println(s);
        
        s = HttpExecutor.simpleGet(httpClient,context,  
                "http://codefanbook:8180/framework-sys-module/service/sys/currentuser",(String)null);
        
        System.out.println(s);
        
       /* params.put("j_password", "111111");
        s = HttpExecutor.formPost(client2, null, 
                "http://codefanbook:8180/framework-sys-module/j_spring_security_check?ajax=true", 
                params,true);
        System.out.println(s);
        
        s = HttpExecutor.simpleGet(client2, null, 
                "http://codefanbook:8180/framework-sys-module/service/sys/currentuser",null);
        System.out.println(s);*/
    } catch (Exception e) {
        //e.printStackTrace();
    }
    
}

}
