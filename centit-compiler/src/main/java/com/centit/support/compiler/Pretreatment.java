package com.centit.support.compiler;

import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringRegularOpt;

import java.util.Collection;

public abstract class Pretreatment {

    private Pretreatment() {
        throw new IllegalAccessError("Utility class");
    }


    public static String objectToFormulaString(Object objValue){
        if(objValue==null)
            return "\"\"";

        if(objValue instanceof Object[]){
            Object [] objs=(Object[]) objValue;

            if(objs.length>0){
                StringBuilder sb = new StringBuilder();
                for(int i=0;i<objs.length;i++){
                    if(i>0)
                        sb.append(',');
                    if(objs[i]!=null){
                        if(ReflectionOpt.isNumberType(objs[i].getClass()))
                            sb.append( objs[i].toString());
                        else if(objs[i] instanceof java.util.Date)
                            sb.append(StringRegularOpt.quotedString(
                                    DatetimeOpt.convertDatetimeToString((java.util.Date) objs[i])));
                        else if(objs[i] instanceof java.sql.Date)
                            sb.append(StringRegularOpt.quotedString(DatetimeOpt.convertDatetimeToString(
                                    DatetimeOpt.convertUtilDate(
                                            (java.sql.Date) objs[i]))));
                        else
                            sb.append(StringRegularOpt.quotedString(objs[i].toString()));
                    }
                }
                return sb.toString();
            }else{
                return "\"\"";
            }
        }else if(objValue instanceof Collection){
            StringBuilder sb = new StringBuilder();
            int vc = 0;
            Collection<?> valueList = (Collection<?> )objValue;
            for(Object ov : valueList){
                if(ov!=null){
                    if(vc>0)
                        sb.append(",");
                    if(ReflectionOpt.isNumberType(ov.getClass()))
                        sb.append( ov.toString());
                    else if(ov instanceof java.util.Date)
                        sb.append(
                                StringRegularOpt.quotedString(
                                        DatetimeOpt.convertDatetimeToString((java.util.Date) ov)));
                    else if(ov instanceof java.sql.Date)
                        sb.append(
                                StringRegularOpt.quotedString(DatetimeOpt.convertDatetimeToString(
                                        DatetimeOpt.convertUtilDate(
                                                (java.sql.Date) ov))));
                    else
                        sb.append(StringRegularOpt.quotedString(ov.toString()));
                    vc++;
                }
            }
            if(vc==0)
                return "\"\"";
            return sb.toString();
        }else if(objValue instanceof java.util.Date){
            return StringRegularOpt.quotedString(
                    DatetimeOpt.convertDatetimeToString((java.util.Date) objValue));
        }else if(objValue instanceof java.sql.Date){
            return StringRegularOpt.quotedString( DatetimeOpt.convertDatetimeToString(
                    DatetimeOpt.convertUtilDate(
                            (java.sql.Date) objValue)));
        }else
            return StringRegularOpt.quotedString(objValue.toString());
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
                    sDesFormula.append( objectToFormulaString(varTrans.getLabelValue(sWord)))
                            .append(" ");
                }else
                    sDesFormula.append( "$"+sWord+" ");
            }else if(Lexer.isLabel(sWord) && !Formula.isKeyWord(sWord) && EmbedFunc.getFuncNo(sWord) == -1 ){
                sDesFormula.append( objectToFormulaString(varTrans.getLabelValue(sWord)))
                        .append(" ");
            }else
                sDesFormula.append(sWord).append(" ");

            sWord = varMorp.getAWord();
        }
        return sDesFormula.toString();
    }
}
