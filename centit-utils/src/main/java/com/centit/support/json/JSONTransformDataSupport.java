package com.centit.support.json;

public interface JSONTransformDataSupport {

    Object attainExpressionValue(String labelName);

    String mapTemplateString(String templateString);

    default void pushStackValue(Object value){

    }

    default Object popStackValue(){
        return null;
    }
}
