package com.centit.support.extend;

import javax.script.ScriptContext;
import java.util.function.Predicate;

public class JSRuntimeContext extends AbstractRuntimeContext{
    public JSRuntimeContext(){
        // JDK 8 "nashorn" 等价与 “js”, "JavaScript"
        // "graal.js"
        super("graal.js");
        var bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowAllAccess",true);
        bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);
    }
}
