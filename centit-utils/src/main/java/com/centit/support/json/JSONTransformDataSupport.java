package com.centit.support.json;

import com.centit.support.compiler.VariableTranslate;

public interface JSONTransformDataSupport extends VariableTranslate {

    Object attainExpressionValue(String labelName);

    String mapTemplateString(String templateString);

    default void pushStackValue(Object value, int rowIndex, int rowCount) {

    }

    default Object popStackValue(){
        return null;
    }
}
