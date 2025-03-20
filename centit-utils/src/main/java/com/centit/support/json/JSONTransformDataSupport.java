package com.centit.support.json;

public interface JSONTransformDataSupport {

    Object attainExpressionValue(String labelName);

    String mapTemplateString(String templateString);

    default void pushStackValue(Object value, int rowIndex, int rowCount) {

    }

    default Object popStackValue(){
        return null;
    }
}
