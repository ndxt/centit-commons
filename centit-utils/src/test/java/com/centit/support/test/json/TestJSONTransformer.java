package com.centit.support.test.json;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.centit.support.json.JSONOpt;
import com.centit.support.json.JSONTransformer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestJSONTransformer {
    public static void main(String arg[]) throws IOException {
        JSONOpt.fastjsonGlobalConfig();
        Object template =  JSON.parseObject( TestJSONTransformer.class
            .getResourceAsStream("/template.json"),  StandardCharsets.UTF_8);
        Object data =  JSON.parseObject( TestJSONTransformer.class
            .getResourceAsStream("/output.json"),  StandardCharsets.UTF_8);
        Object object1 = JSONTransformer.transformer(template, data);
        System.out.println(JSON.toJSONString(object1, JSONWriter.Feature.PrettyFormat));
    }
}
