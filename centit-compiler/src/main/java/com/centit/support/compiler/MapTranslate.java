package com.centit.support.compiler;

import java.util.Map;
/**
 * Map String,Object
 * @author codefan
 *
 */
public class MapTranslate extends ObjectTranslate {

    //public
    public MapTranslate(){
        super();
    }
    public MapTranslate(Map<String,Object> varMap) {
        super(varMap);
    }

    public void setVarMap(Map<String,Object> varMap) {
        super.setVarObject(varMap);
    }
}

