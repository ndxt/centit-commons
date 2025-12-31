package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.network.HttpExecutor;
import com.centit.support.security.SecurityOptUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TestHttpSSE2 {

    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://cloud.centit.com/locode/api/llvm/chat/completions");
            request.setHeader("Content-Type", HttpExecutor.applicationJSONHead);
            request.setHeader("authorization", SecurityOptUtils.decodeSecurityString("aescbc:yxc6oviLUXbWgWqz4us9rA=="));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("model", "qwq");
            jsonObject.put("messages", CollectionsOpt.createList(
                CollectionsOpt.createHashMap("role", "system",
                    "content", "#《正整数拆分》\n ## 题目描述\n" +
                        "将一个大于2的正整数n拆分成若干个不相等的正整数之和，问一共有多少拆分方法。\n" +
                        "比如：当n=6时有：6=1+5，6=1+2+3，6=2+4 一共3种（顺序不同视为同一种）。\n" +
                        "## 输入\n" +
                        "一行一个正整数 n（3<=n<=100); 比如：\n" +
                        "```\n" +
                        "6\n" +
                        "```\n" +
                        "## 输出\n" +
                        "一行一个整数，对应上面的输入示例：\n" +
                        "```\n" +
                        "3\n" +
                        "```\n") ,
                CollectionsOpt.createHashMap("role", "user",
                    "content", "《正整数拆分》的解题思路是什么？/no_think")
            ));
            jsonObject.put("stream", true);


            StringEntity entity = new StringEntity(jsonObject.toString(), StandardCharsets.UTF_8);
            request.setEntity(entity);
            ClassicHttpResponse response = httpClient.execute(request);
            InputStream inputStream = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 处理每一行数据，通常是 JSON 格式的数据事件
                if ("event: error".equals(line)) { // 处理错误事件，如果有需要的话
                    break; // 可以根据需要决定是否退出循环或继续处理错误情况
                }
            }
            /* int n;
            char [] buff = new char[128];
            while ( (n=reader.read(buff)) > 0) {
                System.out.print(String.valueOf(buff).substring(0,n)); // 处理每一行数据，通常是 JSON 格式的数据事件
                //if ("event: error".equals(line)) { // 处理错误事件，如果有需要的话
                //    break; // 可以根据需要决定是否退出循环或继续处理错误情况
                //}
            }
            */
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
