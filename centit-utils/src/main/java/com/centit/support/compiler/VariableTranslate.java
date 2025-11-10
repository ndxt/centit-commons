package com.centit.support.compiler;

public interface VariableTranslate {
    String THE_DATA_SELF_LABEL = "__";
    /**
     * 标识符名-》标识符值的转变
     * 标识符 是以 字母和下划线开头的 字符串
     * 如果变量列表中不存在这个标识符，返回标识本身的名字
     *
     * @param labelName 标识符 变量
     * @return 值
     */
    Object getVarValue(String labelName);
}
