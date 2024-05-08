package com.centit.support.compiler;

import com.centit.support.algorithm.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class VariableFormula {

    private Lexer lex;
    private VariableTranslate trans;
    private Map<String, Function<Object[], Object>> extendFuncMap;


    public VariableFormula() {
        lex = new Lexer();
        extendFuncMap = null;
        //m_preTreat.setVariableTranslate(new SimpleTranslate("1"));
    }

    public static int getOptID(String sOptName) {
        int sl = sOptName.length();
        if (sl == 0) return -1;
        char sp = sOptName.charAt(0), sp2 = '\0';
        if (sl > 1) sp2 = sOptName.charAt(1);
        switch (sp) {
            case '=':
                if (sp2 == '=') {
                    return ConstDefine.OP_EQ;//m_iPreIsn = ConstDefine.OP_EQ ;return ConstDefine.OP_EQ;
                }
                return ConstDefine.OP_EVALUATE;//    m_iPreIsn = ConstDefine.OP_EVALUATE;return ConstDefine.OP_EVALUATE;
            case '+': {
                if (sl == 1) {
                    return ConstDefine.OP_ADD; //{ m_iPreIsn = ConstDefine.OP_ADD ; return ConstDefine.OP_ADD ;}
                }
                break;
            }
            case '-': {
                if (sl == 1) {
                    return ConstDefine.OP_SUB;//{ m_iPreIsn = ConstDefine.OP_SUB ; return ConstDefine.OP_SUB;}
                }
                break;
            }
            case '*':
                return ConstDefine.OP_MUL;//m_iPreIsn = ConstDefine.OP_MUL; return ConstDefine.OP_MUL;
            case '/':
                return ConstDefine.OP_DIV;//m_iPreIsn = ConstDefine.OP_DIV; return ConstDefine.OP_DIV;
            case '%':
                return ConstDefine.OP_MOD;
            case '^':
                return ConstDefine.OP_POWER;//m_iPreIsn = ConstDefine.OP_POWER; return ConstDefine.OP_POWER;
            case '>':
                if (sp2 == '=') {
                    return ConstDefine.OP_EB;//    return ConstDefine.OP_EB;    m_iPreIsn = ConstDefine.OP_EB ;
                }
                if (sp2 == '>')
                    return ConstDefine.OP_RMOV;
                return ConstDefine.OP_BG;//m_iPreIsn = ConstDefine.OP_BG ;    return ConstDefine.OP_BG ;
            case '<':
                if (sp2 == '=') {
                    return ConstDefine.OP_EL;//return ConstDefine.OP_EL; m_iPreIsn = ConstDefine.OP_EL;
                }
                if (sp2 == '>') {
                    return ConstDefine.OP_NE;//    return ConstDefine.OP_NE;    m_iPreIsn = ConstDefine.OP_NE ;
                }
                if (sp2 == '<') {
                    return ConstDefine.OP_LMOV;//    return ConstDefine.OP_NE;    m_iPreIsn = ConstDefine.OP_NE ;
                }
                return ConstDefine.OP_LT;//m_iPreIsn = ConstDefine.OP_LT ;return ConstDefine.OP_LT ;
            case '&':
                if (sp2 == '&') {
                    return ConstDefine.OP_AND;//return ConstDefine.OP_EL; m_iPreIsn = ConstDefine.OP_EL;
                }
                return ConstDefine.OP_BITAND;//m_iPreIsn = ConstDefine.OP_AND ; return  ConstDefine.OP_AND;
            case '|':
                if (sp2 == '|') {
                    return ConstDefine.OP_OR;//return ConstDefine.OP_EL; m_iPreIsn = ConstDefine.OP_EL;
                }
                return ConstDefine.OP_BITOR;//m_iPreIsn = ConstDefine.OP_OR; return  ConstDefine.OP_OR;
            case '!':
                if (sp2 == '=') {
                    return ConstDefine.OP_NE;//return ConstDefine.OP_NE;    m_iPreIsn = ConstDefine.OP_NE;
                }
                return ConstDefine.OP_NOT;//m_iPreIsn = ConstDefine.OP_NOT; return  ConstDefine.OP_NOT;
        }
        if (sOptName.equalsIgnoreCase("LIKE"))
            return ConstDefine.OP_LIKE;
        if (sOptName.equalsIgnoreCase("AND"))
            return ConstDefine.OP_LOGICAND;
        if (sOptName.equalsIgnoreCase("OR"))
            return ConstDefine.OP_LOGICOR;
        if (sOptName.equalsIgnoreCase("NOT"))
            return ConstDefine.OP_NOT;
        if (sOptName.equalsIgnoreCase("IN"))
            return ConstDefine.OP_IN;
        if (sOptName.equalsIgnoreCase("DIV"))
            return ConstDefine.OP_DIV;
        if (sOptName.equalsIgnoreCase("MOD"))
            return ConstDefine.OP_MOD;
        if (sOptName.equalsIgnoreCase("DBMOD"))
            return ConstDefine.OP_DBMOD;
        if (sOptName.equalsIgnoreCase("XOR"))
            return ConstDefine.OP_XOR;
        if (sOptName.equalsIgnoreCase("BETWEEN"))
            return ConstDefine.OP_BETWEEN;
        return -1;
    }

    public static boolean isKeyWord(String sWord) {
        if (sWord == null || sWord.length() == 0)
            return false;
        char firstChar = sWord.charAt(0);
        if ((firstChar == ',') || (firstChar == '(') || (firstChar == ')'))
            return true;
        return getOptID(sWord) > 0;
    }

    private static boolean isBinocularOperator(String sWord) {
        if (sWord == null || sWord.length() == 0)
            return false;
        char firstChar = sWord.charAt(0);
        if (firstChar == ',')
            return true;
        if ((firstChar == '!' && sWord.length() == 1) ||
            "not".equalsIgnoreCase(sWord)) {
            return false;
        }
        return getOptID(sWord) > 0;
    }

    public static Object calculate(String szExpress) {
        VariableFormula formula = new VariableFormula();
        formula.setFormula(szExpress);
        return formula.calcFormula();
    }

    public static Object calculate(String szExpress, VariableTranslate varTrans, Map<String, Function<Object[], Object>> extendFuncMap) {
        VariableFormula formula = new VariableFormula();
        formula.setExtendFuncMap(extendFuncMap);
        formula.setFormula(szExpress);
        formula.setTrans(varTrans);
        return formula.calcFormula();
    }

    public static Object calculate(String szExpress, VariableTranslate varTrans) {
        return calculate(szExpress, varTrans, null);
    }

    public static Object calculate(String szExpress, Object varMap) {
        return calculate(szExpress, new ObjectTranslate(varMap), null);
    }

    /**
     * @param szExpress 表达式
     * @return 返回出错的位置，0 表示表达式格式检查通过
     */
    public static int checkFormula(String szExpress) {
        VariableFormula formula = new VariableFormula();
        formula.setFormula(szExpress);
        return formula.checkFormula();
    }

    public static Set<String> attainFormulaVariable(String szExpress, Map<String, Function<Object[], Object>> extendFuncMap) {
        VariableFormula formula = new VariableFormula();
        formula.setExtendFuncMap(extendFuncMap);
        formula.setFormula(szExpress);
        DummyTranslate translate = new DummyTranslate();
        formula.setTrans(translate);
        formula.calcFormula();
        return translate.getVariableSet();
    }

    public static Set<String> attainFormulaVariable(String szExpress) {
        return attainFormulaVariable(szExpress , null);
    }

    public void setTrans(VariableTranslate trans) {
        this.trans = trans;
    }

    public void setFormula(String formula) {
        lex.setFormula(formula);
    }

    public void setExtendFuncMap(Map<String, Function<Object[], Object>> extendFuncMap) {
        this.extendFuncMap = extendFuncMap;
    }

    public void addExtendFunc(String funcName, Function<Object[], Object> extendFunc) {
        if (extendFuncMap == null) {
            extendFuncMap = new HashMap<>(16);
        }
        this.extendFuncMap.put(funcName, extendFunc);
    }

    private Object calcItem() {
        String str = lex.getAWord();
        if (str == null || str.length() == 0) return null;
        // 表达式结束
        if (str.charAt(0) == ')' || str.charAt(0) == ',' || str.charAt(0) == ';') {
            lex.writeBackAWord(str);
            return null;
        }
        /*if("-".equals(str)){
            String numb = lex.getAWord();
            if(StringRegularOpt.isNumber(numb)) {
                return GeneralAlgorithm.subtractTwoObject(0,
                 NumberBaseOpt.castObjectToNumber(numb));
            }else{
                return null;
            }
        } else*/
        if (str.charAt(0) == '(') {
            Object resStr = calcFormula();
            // 添加集合属性 （,,)
            str = lex.getAWord();
            if(",".equals(str)) {
                List<Object> retSet = new ArrayList<>();
                retSet.add(resStr);
                while (",".equals(str)) {
                    Object nextObj = calcFormula();
                    retSet.add(nextObj);
                    str = lex.getAWord();
                }
                if (str == null || str.length() == 0 || str.charAt(0) != ')') return null;
                return retSet;
            }  else {
                if (str == null || str.length() == 0 || str.charAt(0) != ')') return null;
                return resStr;
            }
        } else if ((str.charAt(0) == '!') || str.equalsIgnoreCase("NOT")) {
            Object obj = calcItem();
            return !BooleanBaseOpt.castObjectToBoolean(obj, false);
        } else if (str.charAt(0) == '$') {
            str = lex.getAWord();
            if (str.equals("{")) {
                str = lex.getStringUntil("}");
                if (trans != null) {
                    return trans.getVarValue(str);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else if (str.charAt(0) == '[') {
            List<Object> slOperand = new ArrayList<>();
            while (true) {
                Object item = calcFormula();
                slOperand.add(item);
                str = lex.getAWord();
                if (str == null || str.length() == 0 || (!str.equals(",") && !str.equals("]")))
                    return null;
                if (str.equals("]")) {
                    return slOperand;
                }
            }
        }

        if(Lexer.isConstValue(str)){
            if (StringRegularOpt.isNumber(str)) {
                return NumberBaseOpt.castObjectToNumber(str);
            }
            return StringRegularOpt.trimString(str);
        }

        if(StringUtils.equalsAnyIgnoreCase(str, "true", "false")){
            return BooleanBaseOpt.castObjectToBoolean(str);
        }

        if(StringUtils.equalsIgnoreCase(str, "null")){
            return null;
        }

        if (extendFuncMap != null) {
            Function<Object[], Object> func = extendFuncMap.get(str);
            if (func != null) {
                String nextWord = lex.getAWord();
                if ("(".equals(nextWord)) {
                    return calcExtendFunc(func);
                }
                lex.writeBackAWord(nextWord);
            }
        }

        int funcNo = EmbedFunc.getFuncNo(str);
        if (funcNo != -1) {
            String nextWord = lex.getAWord();
            if ("(".equals(nextWord)) {
                return calcFunc(funcNo);
            }
            lex.writeBackAWord(nextWord);
        }

        if (trans != null) {
            return trans.getVarValue(str);
        }

        return StringRegularOpt.trimString(str);
    }

    private Object calcOperate(Object operand, Object operand2, int optID) {
        switch (optID) {
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

            case ConstDefine.OP_XOR: {
                if ((BooleanBaseOpt.isBoolean(operand) || NumberBaseOpt.isNumber(operand))
                    && (BooleanBaseOpt.isBoolean(operand2) || NumberBaseOpt.isNumber(operand2))) {
                    return ! (BooleanBaseOpt.castObjectToBoolean(operand).equals(
                        BooleanBaseOpt.castObjectToBoolean(operand2)));
                }
                return StringBaseOpt.concat(operand, operand2);
            }

            case ConstDefine.OP_ADD: {
                return GeneralAlgorithm.addTwoObject(operand, operand2);
            }
            case ConstDefine.OP_MUL: {
                return GeneralAlgorithm.multiplyTwoObject(operand, operand2);
            }
            case ConstDefine.OP_EQ: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) == 0;
            }
            case ConstDefine.OP_BG: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) > 0;
            }

            case ConstDefine.OP_LT: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) < 0;
            }
            case ConstDefine.OP_EL: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) <= 0;
            }
            case ConstDefine.OP_EB: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) >= 0;

            }
            case ConstDefine.OP_NE: {
                return GeneralAlgorithm.compareTwoObject(operand, operand2) != 0;
            }
            case ConstDefine.OP_LMOV:

                if (NumberBaseOpt.isNumber(operand2)) {
                    int nP2 = NumberBaseOpt.castObjectToInteger(operand2);

                    if (NumberBaseOpt.isNumber(operand)) {
                        int nP = NumberBaseOpt.castObjectToInteger(operand);
                        return nP << nP2;
                    }
                    String str1 = StringBaseOpt.objectToString(operand);

                    if (nP2 >= 0 && str1.length() > nP2) {
                        return str1.substring(nP2);
                    }
                }
                return null;
            case ConstDefine.OP_RMOV:
                if (NumberBaseOpt.isNumber(operand2)) {
                    int nP2 = NumberBaseOpt.castObjectToInteger(operand2);
                    if (NumberBaseOpt.isNumber(operand)) {
                        int nP = NumberBaseOpt.castObjectToInteger(operand);
                        return nP >> nP2;
                    }
                    String str1 = StringBaseOpt.objectToString(operand);

                    if (nP2 >= 0 && str1.length() > nP2) {
                        return str1.substring(0, str1.length() - nP2);
                    }
                }
                return null;
            case ConstDefine.OP_LIKE:
                return StringRegularOpt.isMatch(StringBaseOpt.objectToString(operand2),
                    StringBaseOpt.objectToString(operand), 2);
            case ConstDefine.OP_SUB:
                return GeneralAlgorithm.subtractTwoObject(operand, operand2);
            case ConstDefine.OP_DIV: {
                return GeneralAlgorithm.divideTwoObject(operand, operand2);
            }
            case ConstDefine.OP_MOD: {
                Long dbop2 = NumberBaseOpt.castObjectToLong(operand2);
                if (dbop2 == null || dbop2 == 0)
                    return null;
                Long dbop = NumberBaseOpt.castObjectToLong(operand);
                return dbop % dbop2;
            }
            case ConstDefine.OP_DBMOD: {
                BigDecimal dbop2 = NumberBaseOpt.castObjectToBigDecimal(operand2);
                if (dbop2 == null || dbop2.compareTo(BigDecimal.ZERO) == 0)
                    return null;
                return GeneralAlgorithm.modTwoObject(operand, operand2);
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

    /**
     * 暴露Lexer的 四个基础方法
     * @return 当前单词
     */
    //用于跳过个分隔符比如“，”
    public String skipAWord(){
        return lex.getAWord();
    }

    public void writeBackAWord(String preWord) {
        lex.writeBackAWord(preWord);
    }

    public void skipAOperand() {
        lex.skipAOperand();
    }

    public boolean seekToRightBracket(){
        return lex.seekToRightBracket();
    }

    private Boolean calcOperatorIn(Object operand){
        Boolean bInRes = false;
        String str = lex.getAWord();
        if (str == null || str.isEmpty() || !str.equals("(")) return null;

        while (true) {
            Object item = calcFormula();
            // 需要展开 数组
            if (item instanceof Object[]) {
                Object[] objs = (Object[]) item;
                for (int i = 0; i < objs.length; i++) {
                    if (GeneralAlgorithm.compareTwoObject(operand, objs[i]) == 0) {
                        bInRes = true;
                        break;
                    }
                }
            } else if (item instanceof Collection) {
                for (Object obj : (Collection) item) {
                    if (GeneralAlgorithm.compareTwoObject(operand, obj) == 0) {
                        bInRes = true;
                        break;
                    }
                }
            } else {
                if (GeneralAlgorithm.compareTwoObject(operand, item) == 0) {
                    bInRes = true;
                }
            }
            str = lex.getAWord();
            if (str == null || str.length() == 0 || (!str.equals(",") && !str.equals(")"))) return null;
            if (str.equals(")")) {
                lex.writeBackAWord(str);
                break;
            }
        }

        lex.seekToRightBracket();
        return bInRes;
    }

    public Object calcFormula() {
        OptStack optStack = new OptStack();
        Stack<Object> slOperand = new Stack<>();
        String str;
        while (true) {
            Object item = calcItem();
            slOperand.push(item);
            str = lex.getAWord();
            if (str == null || str.isEmpty())
                break;

            int optID = VariableFormula.getOptID(str);
            if (optID == -1) {
                lex.writeBackAWord(str);
                break;
            }
            //--------run OP_IN----------------------------------
            if (optID == ConstDefine.OP_IN) { // Specail Opt For In multi operand
                Object operand = slOperand.pop();
                Boolean bInRes = calcOperatorIn(operand);
                if (bInRes == null) return null;
                slOperand.push(bInRes);
                str = lex.getAWord();
                optID = VariableFormula.getOptID(str);
                if (optID == -1) {
                    lex.writeBackAWord(str);
                    break;
                }
            } else if (optID == ConstDefine.OP_NOT) { // Specail Opt For not In multi operand
                str = lex.getAWord();
                if("in".equalsIgnoreCase(str)){ // NOT_IN
                    Object operand = slOperand.pop();
                    Boolean bInRes = calcOperatorIn(operand);
                    if (bInRes == null) return null;
                    slOperand.push(! bInRes);// not in
                    str = lex.getAWord();
                    optID = VariableFormula.getOptID(str);
                    if (optID == -1) {
                        lex.writeBackAWord(str);
                        break;
                    }
                } else {
                    lex.writeBackAWord(str);
                }
            } else if (optID == ConstDefine.OP_BETWEEN) { // Specail Opt For between operand
                Object operand = slOperand.pop();
                // between中的值 不支持表达式，因为 现在表达式中无法 区分 and 的 特殊性 如果需要表达式需要用括号
                Object obj1 = calcItem();
                str = lex.getAWord();
                if(!str.equalsIgnoreCase("and")){
                    return null;
                }
                Object obj2 = calcItem();
                Boolean bBetweenRes = GeneralAlgorithm.betweenTwoObject(operand, obj1, obj2);
                slOperand.push(bBetweenRes);// not in
                str = lex.getAWord();
                optID = VariableFormula.getOptID(str);
                if (optID == -1) {
                    lex.writeBackAWord(str);
                    break;
                }
            }
            //----------end opt in--------------------------------
            for (int op = optStack.pushOpt(optID); op != 0; op = optStack.pushOpt(optID)) {
                Object operand2 = slOperand.pop();
                Object operand = slOperand.pop();
                slOperand.push(calcOperate(operand, operand2, op));
            }
        }

        for (int op = optStack.popOpt(); op != 0; op = optStack.popOpt()) {
            Object operand2 = slOperand.pop();
            Object operand = slOperand.pop();
            slOperand.push(calcOperate(operand, operand2, op));
        }
        return slOperand.peek();
    }

    private Object calcFunc(int nFuncNo) {
        String str;
        // IF 语句单独处理
        if (EmbedFunc.functionsList[nFuncNo].nFuncID == ConstDefine.FUNC_IF) {
            Object sCondition = calcFormula();
            if (sCondition == null) return null;

            str = lex.getAWord();

            if(!",".equals(str)){
                return null;
            }

            if (BooleanBaseOpt.castObjectToBoolean(sCondition, false)) {
                Object objRes = calcFormula();
                /*str = lex.getAWord();
                if (str == null || str.length() == 0 || (!str.equals(",") && !str.equals(")"))) return null;
                if (str.equals(")"))
                    return objRes;*/
                // 特殊处理的地方就在这儿
                lex.seekToRightBracket();
                /*lex.skipAOperand();
                str = lex.getAWord();
                if(!")".equals(str)){
                    return null;
                }*/
                //if (str == null || str.length() == 0 || !str.equals(")")) return null;
                return objRes;
            } else {
                // 特殊处理的地方就在这儿
                lex.skipAOperand();
                str = lex.getAWord();
                if(!",".equals(str)){
                    return null;
                }
                //if (str == null || str.length() == 0 || !str.equals(",") /*&& !str.equals(")")*/) return null;
                //if (str.equals(")")) return null;
                Object objRes = calcFormula();
                str = lex.getAWord();
                if(!")".equals(str)){
                    return null;
                }
                //if (str == null || str.length() == 0 || !str.equals(")")) return null;
                return objRes;
            }
            //return sRes;
        }

        List<Object> slOperand = new ArrayList<>(5);
        int prmNo = 0;
        while (true) {
            str = lex.getAWord();
            if (str.equals(")")) {
                break;
            }
            lex.writeBackAWord(str);
            Object item = calcFormula();
            slOperand.add(item);
            prmNo++;
            str = lex.getAWord();
            if (!",".equals(str)) break;
        }
        if (!")".equals(str)) {
            return null;
        }

        if (EmbedFunc.functionsList[nFuncNo].nFuncID == ConstDefine.FUNC_EVAL) {
            if(slOperand.isEmpty()){
                return null;
            }
            String valuePath = StringBaseOpt.castObjectToString(slOperand.get(0));
            return VariableFormula.calculate(valuePath, this.trans, this.extendFuncMap);
        }

        if (EmbedFunc.functionsList[nFuncNo].nPrmSum != -1
            //&& prmNo != m_sFunctionList[nFuncNo].nPrmSum) return null;
            && prmNo < EmbedFunc.functionsList[nFuncNo].nPrmSum) return null;
        return EmbedFunc.runFuncWithObject(slOperand, EmbedFunc.functionsList[nFuncNo].nFuncID);
    }

    private Object calcExtendFunc(Function<Object[], Object> func) {
        List<Object> slOperand = new ArrayList<>(5);
        String str;
        while (true) {
            str = lex.getAWord();
            if (str.equals(")")) {
                break;
            }
            lex.writeBackAWord(str);
            Object item = calcFormula();
            //if (item != null) {
            // 外部函数不可以传入 null 数值参数
            //FIX: 2024-4-1 不能传入null是没有道理的
                slOperand.add(item);
            //}
            str = lex.getAWord();
            if (!",".equals(str)) break;
        }
        if (!")".equals(str)) {
            return null;
        }
        return func.apply(CollectionsOpt.listToArray(slOperand, Object.class));
    }

    public Object calcFormula(String szExpress) {
        this.setFormula(szExpress);
        return this.calcFormula();
    }

    /**
     * @return 返回出错的位置，0 表示表达式格式检查通过
     */
    public int checkFormula() {
        boolean endWithBracket = false;
        boolean endWithOpt = true;
        int nBrackets = 0;
        //lex.setFormula(szExpress);
        String sWord;
        sWord = lex.getAWord();
        while (!StringBaseOpt.isNvl(sWord)) {
            boolean isOpt = VariableFormula.isBinocularOperator(sWord);
            if (isOpt && endWithOpt) {
                return lex.getCurrPos() + 1;
            }

            if ("(".equals(sWord)) {
                if (endWithBracket) {
                    return lex.getCurrPos() + 1;
                }
                nBrackets++;
            } else if (")".equals(sWord)) {
                if (endWithOpt) {
                    return lex.getCurrPos() + 1;
                }
                nBrackets--;
            }

            if (",".equals(sWord)) {
                if (nBrackets == 0) {
                    return lex.getCurrPos() + 1;
                }
            }

            endWithBracket = ")".equals(sWord);
            endWithOpt = isOpt;

            if (nBrackets < 0)
                return lex.getCurrPos() + 1;
            sWord = lex.getAWord();
        }
        if (nBrackets == 0)
            return 0;
        else
            return lex.getCurrPos() + 1;
    }


    public static List<Object> calcMultiFormula(String szExpress, VariableTranslate varTrans,
                                               Map<String, Function<Object[], Object>> extendFuncMap) {
        VariableFormula formula = new VariableFormula();
        formula.setExtendFuncMap(extendFuncMap);
        formula.setFormula(szExpress);
        formula.setTrans(varTrans);
        List<Object> objects = new ArrayList<>();
        while(true) {
            Object obj = formula.calcFormula();
            objects.add(obj);
            String separatorString = formula.skipAWord();
            while(StringUtils.equalsAny(separatorString, ",",";",")")){
                separatorString = formula.skipAWord();
            }
            if(StringUtils.isBlank(separatorString)){
                break;
            }
            formula.writeBackAWord(separatorString);
        }
        return objects;
    }
}
