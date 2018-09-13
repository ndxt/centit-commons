package com.centit.support.compiler;

import com.centit.support.algorithm.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VariableFormula {

    private Lexer lex;
    private VariableTranslate trans;
    public VariableFormula() {
        lex = new Lexer();
        //m_preTreat.setVariableTranslate(new SimpleTranslate("1"));
    }

    public void setTrans(VariableTranslate trans) {
        this.trans = trans;
    }

    public void setFormula(String formula) {
        lex.setFormula(formula);
    }

    public static int getOptID(String sOptName) {
        int sl = sOptName.length();
        if(sl == 0) return -1;
        char sp = sOptName.charAt(0), sp2= '\0';
        if(sl>1) sp2 = sOptName.charAt(1);
        switch(sp){
            case '=':
                if(sp2 == '=') {
                    return(ConstDefine.OP_EQ);//m_iPreIsn = ConstDefine.OP_EQ ;return ConstDefine.OP_EQ;
                }
                return(ConstDefine.OP_EVALUATE);//    m_iPreIsn = ConstDefine.OP_EVALUATE;return ConstDefine.OP_EVALUATE;
            case '+': return(ConstDefine.OP_ADD); //{ m_iPreIsn = ConstDefine.OP_ADD ; return ConstDefine.OP_ADD ;}
            case '-': return(ConstDefine.OP_SUB);//{ m_iPreIsn = ConstDefine.OP_SUB ; return ConstDefine.OP_SUB;}
            case '*': return(ConstDefine.OP_MUL);//m_iPreIsn = ConstDefine.OP_MUL; return ConstDefine.OP_MUL;
            case '/': return(ConstDefine.OP_DIV);//m_iPreIsn = ConstDefine.OP_DIV; return ConstDefine.OP_DIV;
            case '%': return(ConstDefine.OP_MOD);
            case '^': return(ConstDefine.OP_POWER);//m_iPreIsn = ConstDefine.OP_POWER; return ConstDefine.OP_POWER;
            case '>':
                if(sp2 == '=') {
                    return(ConstDefine.OP_EB);//    return ConstDefine.OP_EB;    m_iPreIsn = ConstDefine.OP_EB ;
                }
                if(sp2 == '>')
                    return (ConstDefine.OP_RMOV);
                return(ConstDefine.OP_BG);//m_iPreIsn = ConstDefine.OP_BG ;    return ConstDefine.OP_BG ;
            case '<':
                if(sp2 == '=') {
                    return(ConstDefine.OP_EL);//return ConstDefine.OP_EL; m_iPreIsn = ConstDefine.OP_EL;
                }
                if(sp2 == '>') {
                    return(ConstDefine.OP_NE);//    return ConstDefine.OP_NE;    m_iPreIsn = ConstDefine.OP_NE ;
                }
                if(sp2 == '<') {
                    return(ConstDefine.OP_LMOV);//    return ConstDefine.OP_NE;    m_iPreIsn = ConstDefine.OP_NE ;
                }
                return(ConstDefine.OP_LT);//m_iPreIsn = ConstDefine.OP_LT ;return ConstDefine.OP_LT ;
            case '&':
                if(sp2 == '&') {
                    return(ConstDefine.OP_AND);//return ConstDefine.OP_EL; m_iPreIsn = ConstDefine.OP_EL;
                }
                return(ConstDefine.OP_BITAND);//m_iPreIsn = ConstDefine.OP_AND ; return  ConstDefine.OP_AND;
            case '|':
                if(sp2 == '|') {
                    return(ConstDefine.OP_OR);//return ConstDefine.OP_EL; m_iPreIsn = ConstDefine.OP_EL;
                }
                return(ConstDefine.OP_BITOR);//m_iPreIsn = ConstDefine.OP_OR; return  ConstDefine.OP_OR;
            case '!':
                if(sp2 == '=') {
                    return(ConstDefine.OP_NE);//return ConstDefine.OP_NE;    m_iPreIsn = ConstDefine.OP_NE;
                }
                return(ConstDefine.OP_NOT);//m_iPreIsn = ConstDefine.OP_NOT; return  ConstDefine.OP_NOT;
        }
        if(sOptName.equalsIgnoreCase("LIKE"))
            return(ConstDefine.OP_LIKE);
        if(sOptName.equalsIgnoreCase("AND"))
            return(ConstDefine.OP_LOGICAND);
        if(sOptName.equalsIgnoreCase("OR"))
            return(ConstDefine.OP_LOGICOR);
        if(sOptName.equalsIgnoreCase("NOT"))
            return(ConstDefine.OP_NOT);
        if(sOptName.equalsIgnoreCase("IN"))
            return(ConstDefine.OP_IN);
        if(sOptName.equalsIgnoreCase("DIV"))
            return(ConstDefine.OP_DIV);
        if(sOptName.equalsIgnoreCase("MOD"))
            return(ConstDefine.OP_MOD);
        if(sOptName.equalsIgnoreCase("DBMOD"))
            return(ConstDefine.OP_DBMOD);
        return -1;
    }

    public static final boolean isKeyWord(String sWord) {
        if (sWord == null || sWord.length()==0)
            return false;
        char firstChar = sWord.charAt(0);
        if ((firstChar == ',') || (firstChar == '(') || (firstChar == ')'))
            return true;
        if ((firstChar =='-') && (sWord.length()>1)) return false;
        return getOptID(sWord)>0;
    }

    private Object calcItem() {
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

        int funcNo = EmbedFunc.getFuncNo(str);
        if( funcNo != -1) {
            String nextWord = lex.getAWord();
            if("(".equals(nextWord)) {
                return calcFunc(funcNo, str);
            }
            lex.setPreword(nextWord);
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

    private Object calcOperate(Object operand, Object operand2, int optID) {
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

            case ConstDefine.OP_MOD:
            {
                Long dbop2 = NumberBaseOpt.castObjectToLong(operand2);
                if(dbop2==null || dbop2 == 0)
                    return null;
                Long dbop = NumberBaseOpt.castObjectToLong(operand);
                return dbop % dbop2;
            }

            case ConstDefine.OP_DBMOD:
            {
                BigDecimal dbop2 = NumberBaseOpt.castObjectToBigDecimal(operand2);
                if(dbop2==null || dbop2.compareTo(BigDecimal.ZERO)==0)
                    return null;
                return GeneralAlgorithm.modTwoObject(operand,operand2);
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

    public Object calcFormula() {
        List<Object> slOperand = new ArrayList<>();
        OptStack  optStack = new OptStack();

        String str;
        while(true){
            Object item = calcItem();
            slOperand.add(0,item);
            str = lex.getAWord();
            if( str==null || str.length()==0)
                break;

            int optID = VariableFormula.getOptID(str);
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
                optID = VariableFormula.getOptID(str);
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

    private Object calcFunc(int nFuncNo, String funcName) {
        String str;
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
        if(/* str==null || str.length()==0 || */ !str.equals(")") ){
            return null;
        }
        if( EmbedFunc.functionsList[nFuncNo].nPrmSum != -1
            //&& prmNo != m_sFunctionList[nFuncNo].nPrmSum) return null;
            && prmNo < EmbedFunc.functionsList[nFuncNo].nPrmSum) return null;
        return  EmbedFunc.runFuncWithRaw(slOperand,EmbedFunc.functionsList[nFuncNo].nFuncID);
    }

    public static Object calculate(String szExpress) {
        VariableFormula formula = new VariableFormula();
        formula.setFormula(szExpress);
        return formula.calcFormula();
    }


    public static Object calculate(String szExpress,VariableTranslate varTrans) {
        VariableFormula formula = new VariableFormula();
        formula.setFormula(szExpress);
        formula.setTrans(varTrans);
        return formula.calcFormula();
    }

    public static Object calculate(String szExpress,Object varMap) {
        return calculate(szExpress,new ObjectTranslate(varMap));
    }

    /**
     *
     * @param szExpress 表达式
     * @return 返回出错的位置，0 表示表达式格式检查通过
     */
    public int checkFormula(String szExpress) {
        /*if(hasPreTreat)
            szExpress=preTreat.runPretreatment(szExpress);*/
        int nNextType = 1;
        int nBrackets = 0;

        lex.setFormula(szExpress);
        String sWord;
        sWord =    lex.getAWord();
        while(!StringBaseOpt.isNvl(sWord)){
            boolean bKW = VariableFormula.isKeyWord(sWord);
            if(nNextType == 1){
                if(bKW){
                    if ("(".equals(sWord) )
                        nBrackets ++;
                        //else if (")".equals(sWord))
                        //    nBrackets --;
                    else if ((!sWord.equalsIgnoreCase("NOT"))
                            && (! "!".equals(sWord))) // sWord!="!"
                        return lex.getCurrPos()+1;
                }else nNextType = 0;
            }else{
                if(bKW){
                    if (")".equals(sWord))
                        nBrackets --;
                    else if ("(".equals(sWord)){
                        nBrackets ++;
                        nNextType = 1;
                    }
                    else nNextType = 1;
                }else
                    return lex.getCurrPos()+1;
            }
            if(nBrackets<0)
                return lex.getCurrPos()+1;
            sWord =    lex.getAWord();
        }
        if(nBrackets ==0)
            return 0;
        else
            return lex.getCurrPos()+1;
    }
}
