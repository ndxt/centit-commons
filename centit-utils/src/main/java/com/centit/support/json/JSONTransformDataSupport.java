package com.centit.support.json;

public interface JSONTransformDataSupport {

    Object attainExpressionValue(String labelName);

    default void pushStackValue(Object value){

    }

    default Object popStackValue(){
        return null;
    }
}
