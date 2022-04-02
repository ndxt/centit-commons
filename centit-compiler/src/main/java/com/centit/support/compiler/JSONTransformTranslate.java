package com.centit.support.compiler;

import com.centit.support.json.JSONTransformDataSupport;

public class JSONTransformTranslate implements VariableTranslate {

    private JSONTransformDataSupport dataSupport;

    public JSONTransformTranslate(){

    }

    public JSONTransformTranslate(JSONTransformDataSupport dataSupport){
        this.dataSupport = dataSupport;
    }
    @Override
    public Object getVarValue(String labelName) {
        return dataSupport.attainExpressionValue(labelName);
    }

    public static JSONTransformTranslate create(JSONTransformDataSupport dataSupport){
        return new JSONTransformTranslate(dataSupport);
    }

}
