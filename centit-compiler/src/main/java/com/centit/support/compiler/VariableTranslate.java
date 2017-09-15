package com.centit.support.compiler;

public interface VariableTranslate {
    /**变量名 -》变量值的转变
        *变量 是用 ${变量名}
        *如果这个变量不存在，返回空字符串 "''"
     * @param varName 变量
     * @return 值
     */
    String getVarValue(String varName);
    /**标识符名-》标识符值的转变
     *标识符 是以 字母和下划线开头的 字符串
     *如果变量列表中不存在这个标识符，返回标识本身的名字
     * @param labelName 标识符
     * @return 值
     */
    String getLabelValue(String labelName);
}
