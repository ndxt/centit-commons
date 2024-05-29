package com.centit.support.test.extend;

import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.extend.PythonRuntimeContext;

import javax.script.ScriptException;

public class TestPython {
    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        // 创建一个ScriptEngineManager实例
        PythonRuntimeContext pythonRuntimeContext = new PythonRuntimeContext();
        pythonRuntimeContext.compileScript("def add2num(y, t):\n" +
            "    print \"hello\", y+t \n" +
            "    return y + t\n");
        Object obj = pythonRuntimeContext.callJsFunc("add2num", 1, 2);
        System.out.println(StringBaseOpt.castObjectToString(obj));
    }
}
