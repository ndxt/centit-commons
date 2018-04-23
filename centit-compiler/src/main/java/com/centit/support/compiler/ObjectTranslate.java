package com.centit.support.compiler;

import com.centit.support.algorithm.ReflectionOpt;

public class ObjectTranslate implements VariableTranslate {

    private Object varObj;
    //public
    public ObjectTranslate(){
        varObj = null;
    }
    public ObjectTranslate(Object varObj) {
        this.varObj = varObj;
    }

     /**
     * 默认返回业务模型对象的属性值 , request 队形的参数
      /**变量名 -》变量值的转变
      *变量 是用 ${变量名}
      *如果这个变量不存在，返回空字符串 "''"
      * @param varName 变量
      * @return 值
      */
    @Override
    public Object getVarValue(String varName) {
        if(varObj==null)
            return null;
        return ReflectionOpt.attainExpressionValue
                /*ReflectionOpt.forceGetProperty*/(varObj, varName);
    }    

    @Override
    public Object getLabelValue(String labelName) {
        return getVarValue(labelName);
        /*if("''".equals(res))//res == null ||
            return labelName;
        return res;*/
    }

    public void setVarObject(Object varObj) {
        this.varObj = varObj;
    }

}

