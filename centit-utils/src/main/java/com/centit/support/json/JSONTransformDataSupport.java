package com.centit.support.json;

import com.centit.support.compiler.VariableTranslate;

public interface JSONTransformDataSupport extends VariableTranslate {

    Object attainExpressionValue(String labelName);

    /**
     * 从JSONPath中提取数据, 需要将对象转换为字符串，效率比较低
     * @param jsonPath JSONPath
     * @return 获取的数据
     */
    Object extractJSONPathValue(String jsonPath);

    String mapTemplateString(String templateString);

    default void pushStackValue(Object value, int rowIndex, int rowCount) {
    }

    default Object popStackValue(){
        return null;
    }
}
