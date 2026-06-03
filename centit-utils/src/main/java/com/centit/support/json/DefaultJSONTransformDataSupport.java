package com.centit.support.json;

import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.compiler.Pretreatment;
import com.centit.support.compiler.VariableFormula;
import com.centit.support.compiler.VariableTranslate;

import java.util.ArrayList;
import java.util.List;

public class DefaultJSONTransformDataSupport implements JSONTransformDataSupport{
    static class StackData{
        Object data;
        int  index;
        int  count;
        public StackData(Object data, int index, int count) {
            this.data = data;
            this.index = index;
            this.count = count;
        }
    }
    private final VariableTranslate varTrans;
    private int stackLength;
    private final List<StackData> stack;

    public DefaultJSONTransformDataSupport(VariableTranslate varTrans){
        this.varTrans = varTrans;
        this.stackLength = 0;
        this.stack = new ArrayList<>(10);
    }

    private Object getTopStackData() {
        return stack.get(stackLength-1).data;
    }
    // n>0
    private Object peekStackValue(){
        return stackLength>1? stack.get(stackLength-2).data: null;
    }

    private StackData getTopStack() {
        return stack.get(stackLength-1);
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
        if(labelName.startsWith("__.")){ // __. 代表root varTrans
            return varTrans.getVarValue(labelName.substring(3));
        } else if(labelName.startsWith("..")){
            return ReflectionOpt.attainExpressionValue(
                peekStackValue(),
                labelName.substring(2));
        } else if(labelName.startsWith(".")){
            return stackLength>0 ?
                ReflectionOpt.attainExpressionValue(getTopStackData(),  labelName.substring(1)) :
                varTrans.getVarValue(labelName.substring(1));
        } else {
            if(stackLength>0) {
                if ("__row_index".equals(labelName)) {
                    return getTopStack().index;
                }
                if ("__row_count".equals(labelName)) {
                    return getTopStack().count;
                }
            }
            return stackLength>0 ?
                ReflectionOpt.attainExpressionValue(getTopStackData(), labelName):
                varTrans.getVarValue(labelName);
        }
    }

    @Override
    public String mapTemplateString(String templateString) {
        return Pretreatment.mapTemplateStringAsFormula(templateString, this);
    }

    @Override
    public void pushStackValue(Object value, int rowIndex, int rowCount) {
        if(stack.size()>stackLength){
            stack.set(stackLength, new StackData(value, rowIndex, rowCount));
        } else {
            stack.add(new StackData(value, rowIndex, rowCount));
        }
        stackLength++;
    }

    @Override
    public Object popStackValue(){
        Object obj = stackLength>0? getTopStackData(): null;
        if(stackLength>0){
            stackLength-- ;
        }
        return obj;
    }
}
