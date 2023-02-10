package com.centit.support.compiler;

import java.util.HashSet;
import java.util.Set;

public class DummyTranslate implements VariableTranslate {

    public Set<String> getVariableSet() {
        return variableSet;
    }

    private Set<String> variableSet;

    //public
    public DummyTranslate() {
        variableSet = new HashSet<>(16);
    }

    /**
     * 默认返回业务模型对象的属性值 , request 队形的参数
     * /**变量名 -》变量值的转变
     * 变量 是用 ${变量名}
     * 如果这个变量不存在，返回空字符串 "''"
     *
     * @param varName 变量
     * @return 值
     */
    @Override
    public Object getVarValue(String varName) {
        variableSet.add(varName);
        return varName;
    }

}

