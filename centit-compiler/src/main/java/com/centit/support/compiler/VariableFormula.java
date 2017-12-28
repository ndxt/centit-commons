package com.centit.support.compiler;

import com.centit.support.algorithm.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VariableFormula {

    private Lexer lex;
    private VariableTranslate trans;
    public VariableFormula()
    {
        lex = new Lexer();
        //m_preTreat.setVariableTranslate(new SimpleTranslate("1"));
    }

    public void setTrans(VariableTranslate trans) {
        this.trans = trans;
    }

    public void setFormula(String formula) {
        lex.setFormula(formula);
    }

    private Object calcItem()
    {
        String str = lex.getAWord();
        if( str == null || str.length()==0) return null;
        if(str.charAt(0) == ')' || str.charAt(0) == ',' ){
            lex.setPreword(str);
            return null;
        }
        if(str.charAt(0) == '('){
            Object resStr = calcFormula();
            str = lex.getAWord();
            if( str == null || str.length()==0 || str.charAt(0) != ')') return null;
            return resStr;
        }else if( (str.charAt(0) == '!') || str.equalsIgnoreCase("NOT") ) {
            Object obj = calcItem();
            return ! BooleanBaseOpt.castObjectToBoolean(obj,false);
        }else if(str.charAt(0) == '$'){
            str = lex.getAWord();
            if(str.equals("{")){
                str = lex.getStringUntil("}");
                return trans.getLabelValue(str);
            }else {
                return null;
            }
        }

        int funcNo = Formula.getFuncNo(str);
        if( funcNo != -1) {
            return calcFunc(funcNo, str);
        }
        if(trans!=null && Lexer.isLabel(str)){
            return trans.getLabelValue(str);
        }

        String res = StringRegularOpt.trimString(str);
        if(StringRegularOpt.isNumber(res)){
            return NumberBaseOpt.castObjectToNumber(res);
        }
        return res;
    }

    private Object calcOperate(Object operand, Object operand2, int optID)
    {
        switch(optID) {
            case ConstDefine.OP_LOGICOR: {
                return BooleanBaseOpt.castObjectToBoolean(operand, false) ||
                        BooleanBaseOpt.castObjectToBoolean(operand2, false);
            }
            case ConstDefine.OP_AND:
            case ConstDefine.OP_LOGICAND: {
                return BooleanBaseOpt.castObjectToBoolean(operand, false) &&
                        BooleanBaseOpt.castObjectToBoolean(operand2, false);
            }

            case ConstDefine.OP_OR: {
                if ((BooleanBaseOpt.isBoolean(operand) || NumberBaseOpt.isNumber(operand))
                        && (BooleanBaseOpt.isBoolean(operand2) || NumberBaseOpt.isNumber(operand2))) {
                    return BooleanBaseOpt.castObjectToBoolean(operand) ||
                            BooleanBaseOpt.castObjectToBoolean(operand2);
                }
                return StringBaseOpt.concat(operand, operand2);
            }

            case ConstDefine.OP_ADD: {
                 return GeneralAlgorithm.addTwoObject( operand, operand2);
            }
            case ConstDefine.OP_MUL: {
                if (NumberBaseOpt.isNumber(operand)
                        && NumberBaseOpt.isNumber(operand2)) {
                    return GeneralAlgorithm.multiplyTwoObject(operand, operand2);
                }
                return StringBaseOpt.concat(operand, operand2);

            }
            case ConstDefine.OP_EQ: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) == 0 ;
            }
            case ConstDefine.OP_BG: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) > 0 ;
            }

            case ConstDefine.OP_LT: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) < 0 ;
            }
            case ConstDefine.OP_EL:{
                return GeneralAlgorithm.compareTwoObject(operand, operand2) <= 0 ;
            }
            case ConstDefine.OP_EB:{
                return GeneralAlgorithm.compareTwoObject(operand, operand2) >= 0 ;

            }
            case ConstDefine.OP_NE: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) != 0 ;
            }
            case ConstDefine.OP_LMOV:

                if( NumberBaseOpt.isNumber(operand2) ){
                    int nP2 = NumberBaseOpt.castObjectToInteger(operand2);

                    if( NumberBaseOpt.isNumber(operand) ) {
                        int nP = NumberBaseOpt.castObjectToInteger(operand);
                        return nP << nP2;
                    }
                    String str1 = StringBaseOpt.objectToString(operand);

                    if(nP2>=0 && str1.length() > nP2){
                        return str1.substring(nP2);
                    }
                }
                return null;
            case ConstDefine.OP_RMOV:

                if( NumberBaseOpt.isNumber(operand2) ){
                    int nP2 = NumberBaseOpt.castObjectToInteger(operand2);

                    if( NumberBaseOpt.isNumber(operand) ) {
                        int nP = NumberBaseOpt.castObjectToInteger(operand);
                        return nP >> nP2;
                    }
                    String str1 = StringBaseOpt.objectToString(operand);

                    if(nP2>=0 && str1.length() > nP2){
                        return str1.substring(0,str1.length() - nP2);
                    }
                }
                return null;
            case ConstDefine.OP_LIKE:
                return StringRegularOpt.isMatch(StringBaseOpt.objectToString(operand),
                        StringBaseOpt.objectToString(operand2));

            case ConstDefine.OP_SUB:
                return GeneralAlgorithm.subtractTwoObject(operand,operand2);

            case ConstDefine.OP_DIV:
            {
                BigDecimal dbop2 = NumberBaseOpt.castObjectToBigDecimal(operand2);
                if(dbop2==null || dbop2.compareTo(BigDecimal.ZERO)==0)
                    return null;
                return GeneralAlgorithm.divideTwoObject(operand,operand2);
            }

            case ConstDefine.OP_POWER: {
                Double dbop = NumberBaseOpt.castObjectToDouble(operand);
                Double dbop2 = NumberBaseOpt.castObjectToDouble(operand2);
                if (dbop == null || dbop2 == null)
                    return null;
                return Math.pow(dbop, dbop2);
            }
            default:
                break;
        }

        return null;
    }

    public Object calcFormula()
    {
        List<Object> slOperand = new ArrayList<>();
        OptStack  optStack = new OptStack();

        String str;
        while(true){
            Object item = calcItem();
            slOperand.add(0,item);
            str = lex.getAWord();
            if( str==null || str.length()==0)
                break;

            int optID = Formula.getOptID(str);
            if( optID == -1){
                lex.setPreword(str);
                break;
            }
    //--------run OP_IN----------------------------------
            if(optID == ConstDefine.OP_IN){ // Specail Opt For In multi operand
                Boolean bInRes = false;
                Object operand = slOperand.remove(0);
                str = lex.getAWord();
                if( str==null || str.length()==0 || !str.equals("(") ) return null;


                while(true)
                {
                    item = calcFormula();
                    if( GeneralAlgorithm.compareTwoObject(operand,item) == 0 ){
                        bInRes = true;
                    }
                    str = lex.getAWord();
                    if( str==null || str.length()==0 ||(  !str.equals(",")  && !str.equals(")") ) ) return null;
                    if( str.equals(")") ) {
                        lex.setPreword(str);
                        break;
                    }
                }

                lex.seekToRightBracket();

                slOperand.add(0,bInRes);
                str = lex.getAWord();
                optID = Formula.getOptID(str);
                if( optID == -1){
                    lex.setPreword(str);
                    break;
                }
            }
    //----------end opt in--------------------------------
            for(int op = optStack.pushOpt(optID); op != 0; op = optStack.pushOpt(optID)){
                Object operand2 = slOperand.remove(0);
                Object operand = slOperand.remove(0);
                slOperand.add(0, calcOperate(operand,operand2,op));
            }
        }

        for(int op = optStack.popOpt(); op != 0; op = optStack.popOpt()){
            Object operand2 = slOperand.remove(0);
            Object operand = slOperand.remove(0);
            slOperand.add(0, calcOperate(operand,operand2,op));
        }
        return  slOperand.get(0);
    }

    private Object calcFunc(int nFuncNo, String funcName)
    {
        String str = lex.getAWord();
        if( str==null || str.length()==0 || !str.equals("(") ) {
            if(str!=null && str.length()>0)
                lex.setPreword(str);
            return funcName;
        }
        int prmNo = 0;

        // IF 语句单独处理
        if( EmbedFunc.functionsList[nFuncNo].nFuncID == ConstDefine.FUNC_IF){
            Object sCondition = calcFormula();
            if(sCondition==null) return null;

            str = lex.getAWord();
            if( str==null || str.length()==0 ||  !str.equals(",") ) return null;

            if( BooleanBaseOpt.castObjectToBoolean(sCondition,false) ){
                Object objRes =  calcFormula();
                str = lex.getAWord();
                if( str==null || str.length()==0 || ( !str.equals(",") && !str.equals(")")) ) return null;
                if( str.equals(")") )
                    return objRes;
                // 特殊处理的地方就在这儿
                lex.skipAOperand();
                str = lex.getAWord();
                if( str==null || str.length()==0 || !str.equals(")") ) return null;
                return objRes;
            }else {
                // 特殊处理的地方就在这儿
                lex.skipAOperand();
                str = lex.getAWord();
                if( str==null || str.length()==0 || !str.equals(",") && !str.equals(")") ) return null;
                if( str.equals(")") ) return null;
                Object objRes = calcFormula();
                str = lex.getAWord();
                if( str==null || str.length()==0 || !str.equals(")") ) return null;
                return objRes;
            }
            //return sRes;
        }

        List<Object> slOperand = new ArrayList<>(5);

        while( true )
            //( m_sFunctionList[nFuncNo].nPrmSum == -1
            //  || prmNo < m_sFunctionList[nFuncNo].nPrmSum )
        {
            prmNo ++;
            Object item = calcFormula();
            slOperand.add(item);
            str = lex.getAWord();
            if( str==null || str.length()==0 || ( !str.equals(",") && !str.equals(")"))  )
                return null;
            if( str.equals(")") ){
                break;
            }
        }
        //str = m_lex.getAWord();
        if(/* str==null || str.length()==0 || */ !str.equals(")") ) return null;
        if( EmbedFunc.functionsList[nFuncNo].nPrmSum != -1
            //&& prmNo != m_sFunctionList[nFuncNo].nPrmSum) return null;
            && prmNo < EmbedFunc.functionsList[nFuncNo].nPrmSum) return null;
        return  EmbedFunc.runFuncWithRaw(slOperand,EmbedFunc.functionsList[nFuncNo].nFuncID);
    }

    public static Object calculate(String szExpress)
    {
        VariableFormula formula = new VariableFormula();
        formula.setFormula(szExpress);
        return formula.calcFormula();
    }


    public static Object calculate(String szExpress,VariableTranslate varTrans)
    {
        VariableFormula formula = new VariableFormula();
        formula.setFormula(szExpress);
        formula.setTrans(varTrans);
        return formula.calcFormula();
    }

    public static Object calculate(String szExpress,Object varMap)
    {
        return calculate(szExpress,new ObjectTranslate(varMap));
    }
}
