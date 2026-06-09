package com.centit.support.test.json;

import com.alibaba.fastjson2.JSONPath;

public class TestJPath {
    public static void main(String[] args) {
        String json = "{\"store\":{\"book\":[{\"title\":\"Java\"},{\"title\":\"Fastjson2\"}]}}";

// 1. 提取匹配的数据
        String path = "$.store.book[0].title";
        Object result = JSONPath.extract(json, path);
        System.out.println(result); // 输出: Java

// 2. 静态求值
        Object evalResult = JSONPath.eval(json, path);
        System.out.println(evalResult);
    }
}
