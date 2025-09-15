package com.centit.support.test.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestHttpSSE {

    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://192.168.133.15:8080/apis/practice/prompt/O1JYI6OySbGWq_TX1466zQ/1");
            HttpResponse response = httpClient.execute(request);
            InputStream inputStream = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            int n;
            char [] buff = new char[128];
            while ( (n=reader.read(buff)) > 0) {
                System.out.print(String.valueOf(buff).substring(0,n)); // 处理每一行数据，通常是 JSON 格式的数据事件
                //if ("event: error".equals(line)) { // 处理错误事件，如果有需要的话
                //    break; // 可以根据需要决定是否退出循环或继续处理错误情况
                //}
            }
            /*String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 处理每一行数据，通常是 JSON 格式的数据事件
                if ("event: error".equals(line)) { // 处理错误事件，如果有需要的话
                    break; // 可以根据需要决定是否退出循环或继续处理错误情况
                }
            }*/
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
