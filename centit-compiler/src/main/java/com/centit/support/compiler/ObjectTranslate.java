package com.centit.support.compiler;

import com.centit.support.algorithm.ReflectionOpt;

public class ObjectTranslate implements VariableTranslate {

    private Object varObj;

    //public
    public ObjectTranslate() {
        varObj = null;
    }

    public ObjectTranslate(Object varObj) {
        this.varObj = varObj;
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
        if (varObj == null)
            return null;
        if(THE_DATA_SELF_LABEL.equals(varName)){
            return varObj;
        }
        return ReflectionOpt.attainExpressionValue
            /*ReflectionOpt.forceGetProperty*/(varObj, varName);
    }

    public void setVarObject(Object varObj) {
        this.varObj = varObj;
    }

}

