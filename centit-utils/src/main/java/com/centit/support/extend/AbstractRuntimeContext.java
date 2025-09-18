package com.centit.support.extend;

import com.alibaba.fastjson2.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRuntimeContext {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractRuntimeContext.class);
    private ScriptEngine scriptEngine;

    public AbstractRuntimeContext(String engineName){
        ScriptEngineManager sem = new ScriptEngineManager();
        scriptEngine = sem.getEngineByName(engineName);
        // "nashorn" 等价与 “js”, "JavaScript"
        // "graal.js"
    }

    public AbstractRuntimeContext compileScript(String js){
        try {
            scriptEngine.eval(js);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return this;
    }

    public AbstractRuntimeContext compileScriptFile(String jsFileName){
        try {
            FileReader reader = new FileReader(jsFileName);
            scriptEngine.eval(reader);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return this;
    }

    public AbstractRuntimeContext compileScriptStream(InputStream is){
        try {
            InputStreamReader reader = new InputStreamReader(is);
            scriptEngine.eval(reader);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return this;
    }

    /**
     * 检查js返回的对象是否是数组
     * @param object js对象
     * @return js对象
     */
    public static Object checkArrayObject(Object object){
        if(object instanceof Map){
            Map<?,?> objMap = (Map<?,?>) object;
            JSONArray objArray = new JSONArray();
            Map<Object,  Object> despMap = new HashMap<>();
            boolean isArray = true;
            for(Map.Entry<?,?> ent : objMap.entrySet()){
                Object key = ent.getKey();
                if (key != null) {
                    if(isArray){
                        if(StringUtils.isNumeric(key.toString())) {
                            objArray.add(checkArrayObject(ent.getValue()));
                        } else {
                            isArray = false;
                        }
                    }
                    despMap.put(key, checkArrayObject(ent.getValue()));
                }
            }
            if(isArray && !objArray.isEmpty()){
                return objArray;
            } else {
                return despMap;
            }
        }
        return object;
    }

    public Object callFunc(String funcName, Object... args) throws
        ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) scriptEngine;
        return checkArrayObject(invocable.invokeFunction(funcName, args));
    }

    public Object getObject(String objName){
        return scriptEngine.get(objName);
    }

    public Object getObjectProperty(String objName, String propertyName)
        throws ScriptException {
       return checkArrayObject(scriptEngine.eval(objName+"."+propertyName));
    }

    public Object callObjectMethod(Object jsObject, String methodName, Object... args)
        throws ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) scriptEngine;
        return checkArrayObject(invocable.invokeMethod(jsObject, methodName, args));
    }
}
