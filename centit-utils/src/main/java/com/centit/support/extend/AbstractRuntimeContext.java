package com.centit.support.extend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class AbstractRuntimeContext {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractRuntimeContext.class);
    protected ScriptEngine scriptEngine;

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

   /*public static Object checkArrayObject(Object object){
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
    }*/

    public Object callFunc(String funcName, Object... args) throws
        ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeFunction(funcName, args);
    }

    public Object getObject(String objName){
        return scriptEngine.get(objName);
    }

    public Object getObjectProperty(String objName, String propertyName)
        throws ScriptException {
       return scriptEngine.eval(objName+"."+propertyName);
    }

    public Object callObjectMethod(Object jsObject, String methodName, Object... args)
        throws ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeMethod(jsObject, methodName, args);
    }
}
