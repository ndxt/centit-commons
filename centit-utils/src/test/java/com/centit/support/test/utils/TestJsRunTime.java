package com.centit.support.test.utils;

import com.alibaba.fastjson2.JSON;
import com.centit.support.extend.JSRuntimeContext;

import javax.script.ScriptException;
import java.io.InputStream;

public class TestJsRunTime {

    public static void main(String[] args) {
        try {
            JSRuntimeContext jsRuntimeContext = new JSRuntimeContext();
            InputStream in = TestJsRunTime.class
                .getResourceAsStream("/testJs.js");
            jsRuntimeContext.compileScriptStream(in);

            Object obj = jsRuntimeContext.callFunc("runOpt", new Object());
            System.out.println(JSON.toJSONString(obj));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
