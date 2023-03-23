package com.centit.support.extend;

import com.alibaba.fastjson2.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class JSRuntimeContext {
    protected static final Logger logger = LoggerFactory.getLogger(JSRuntimeContext.class);
    private ScriptEngine scriptEngine;

    public JSRuntimeContext(){
        ScriptEngineManager sem = new ScriptEngineManager();
        scriptEngine = sem.getEngineByName("js");
        // "nashorn" 等价与 “js”, "JavaScript"
        // "graal.js"
    }

    public JSRuntimeContext compileScript(String js){
        try {
            scriptEngine.eval(js);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return this;
    }

    public JSRuntimeContext compileScriptFile(String jsFileName){
        try {
            FileReader reader = new FileReader(new File(jsFileName));
            scriptEngine.eval(reader);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return this;
    }

    public JSRuntimeContext compileScriptStream(InputStream is){
        try {
            InputStreamReader reader = new InputStreamReader(is);
            scriptEngine.eval(reader);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return this;
    }

    public static Object checkArrayObject(Object object){
        if(object instanceof Map){
            Map<?,?> objMap = (Map<?,?>) object;
            JSONArray objArray = new JSONArray();
            boolean isArray = true;
            for(Map.Entry<?,?> ent : objMap.entrySet()){
                if(StringUtils.isNumeric(ent.getKey().toString())){
                    objArray.add(ent.getValue());
                } else {
                    isArray = false;
                    break;
                }
            }
            if(isArray && objArray.size()>0){
                return objArray;
            }
        }
        return object;
    }

    public Object callJsFunc(String funcName, Object... args) throws
        ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) scriptEngine;
        return checkArrayObject(invocable.invokeFunction(funcName, args));
    }

    public Object getJsObject(String objName){
        return scriptEngine.get(objName);
    }

    public Object getJsObjectProperty(String objName, String propertyName)
        throws ScriptException {
       return checkArrayObject(scriptEngine.eval(objName+"."+propertyName));
    }

    public Object callJsObjectMethod(Object jsObject, String methodName, Object... args)
        throws ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) scriptEngine;
        return checkArrayObject(invocable.invokeMethod(jsObject, methodName, args));
    }
}
