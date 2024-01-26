package com.centit.support.extend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Predicate;

public class JSRuntimeContext {
    protected static final Logger logger = LoggerFactory.getLogger(JSRuntimeContext.class);
    private ScriptEngine scriptEngine;

    public JSRuntimeContext(){
        ScriptEngineManager sem = new ScriptEngineManager();
        // "nashorn" 等价与 “js”, "JavaScript"
        // "graal.js"
        scriptEngine = sem.getEngineByName("graal.js");
        /**
         如此之后，就可以支持nashorn里的一些特性了：
         Java.type()
         Java.from, Java.to
         Java.extend, Java.super
         */
        var bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowAllAccess",true);
        bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);
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

    public Object callJsFunc(String funcName, Object... args) throws
        ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeFunction(funcName, args);
    }

    public Object getJsObject(String objName){
        return scriptEngine.get(objName);
    }

    public Object getJsObjectProperty(String objName, String propertyName)
        throws ScriptException {
       return scriptEngine.eval(objName+"."+propertyName);
    }

    public Object callJsObjectMethod(Object jsObject, String methodName, Object... args)
        throws ScriptException, NoSuchMethodException {
        Invocable invocable = (Invocable) scriptEngine;
        return invocable.invokeMethod(jsObject, methodName, args);
    }
}
