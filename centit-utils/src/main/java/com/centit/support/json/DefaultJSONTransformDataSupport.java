package com.centit.support.json;

import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.compiler.VariableFormula;
import com.centit.support.compiler.VariableTranslate;

import java.util.ArrayList;
import java.util.List;

public class DefaultJSONTransformDataSupport
    implements VariableTranslate, JSONTransformDataSupport{
    private Object data;
    private int stackLength;
    private List<Object> stack;

    public DefaultJSONTransformDataSupport(Object obj){
        this.data = obj;
        this.stackLength = 0;
        this.stack = new ArrayList<>(10);
    }

    @Override
    public Object attainExpressionValue(String expression) {
        if (expression == null) {
            return null;
        }
        VariableFormula variableFormula = new VariableFormula();
        variableFormula.setTrans(this);
        variableFormula.setFormula(expression);
        return variableFormula.calcFormula();
    }

    @Override
    public Object getVarValue(String labelName) {
        if(labelName.startsWith("/")){
            return ReflectionOpt.attainExpressionValue(data,
                labelName.substring(1));
        } else if(labelName.startsWith("..")){
            return ReflectionOpt.attainExpressionValue(
                peekStackValue(1),
                labelName.substring(2));
        } else if(labelName.startsWith(".")){
            return ReflectionOpt.attainExpressionValue(
                currentValue(),
                labelName.substring(1));
        } else {
            return ReflectionOpt.attainExpressionValue(
                currentValue(),
                labelName);
        }
    }

    @Override
    public String mapTemplateString(String templateString) {
        return templateString;
    }

    private Object currentValue(){
        return stackLength>0? stack.get(stackLength-1): data;
    }

    private Object peekStackValue(){
        return stackLength>0? stack.get(stackLength-1): null;
    }
    // n>0
    private Object peekStackValue(int n){
        return stackLength>n? stack.get(stackLength-n-1): null;
    }

    @Override
    public void pushStackValue(Object value){
        if(stack.size()>stackLength){
            stack.set(stackLength, value);
        } else {
            stack.add(value);
        }
        stackLength++;
    }

    @Override
    public Object popStackValue(){
        Object obj = stackLength>0? stack.get(stackLength-1): null;
        if(stackLength>0){
            stackLength-- ;
        }
        return obj;
    }
}
