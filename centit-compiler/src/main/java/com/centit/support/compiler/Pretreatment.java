package com.centit.support.compiler;

public abstract class Pretreatment {

    private Pretreatment() {
        throw new IllegalAccessError("Utility class");
    }

    /**get value and label from formula and translate
     * 变量 形式如 ${变量名}
     * 标识符名-》标识符值的转变
     * 标识符 是以 字母和下划线开头的 占位符
     *
     * @param szFormula 表达式
     * @param varTrans 解释器
     * @return 新的表达式
     */
    public static String runPretreatment(String szFormula,VariableTranslate varTrans){
        Lexer varMorp = new Lexer();
        varMorp.setFormula(szFormula);
        StringBuilder sDesFormula= new StringBuilder();
        String sWord = varMorp.getAWord();

        while( sWord!=null && ! sWord.equals("") ){
            if( sWord.equals("$")){
                sWord = varMorp.getAWord();
                if(sWord.equals("{")){
                    sWord = varMorp.getStringUntil("}");
                    sDesFormula.append(varTrans.getVarValue(sWord)).append(" ");
                }else
                    sDesFormula.append( "$"+sWord+" ");
            }else if(Lexer.isLabel(sWord) && !Formula.isKeyWord(sWord) && EmbedFunc.getFuncNo(sWord) == -1 ){
                sDesFormula.append(varTrans.getLabelValue(sWord)).append(" ");
            }else
                sDesFormula.append(sWord).append(" ");;

            sWord = varMorp.getAWord();
        }
        return sDesFormula.toString();
    }
}
