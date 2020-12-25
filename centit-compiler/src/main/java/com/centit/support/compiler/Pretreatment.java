package com.centit.support.compiler;

import com.centit.support.algorithm.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public abstract class Pretreatment {

    private Pretreatment() {
        throw new IllegalAccessError("Utility class");
    }


    public static String objectToFormulaString(Object objValue) {
        if (objValue == null)
            return "\"\"";

        if (objValue instanceof Object[]) {
            Object[] objs = (Object[]) objValue;

            if (objs.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < objs.length; i++) {
                    if (i > 0)
                        sb.append(',');
                    if (objs[i] != null) {
                        if (ReflectionOpt.isNumberType(objs[i].getClass())) {
                            sb.append(objs[i].toString());
                        } else if (objs[i] instanceof java.util.Date) {
                            sb.append(StringRegularOpt.quotedString(
                                DatetimeOpt.convertDatetimeToString((java.util.Date) objs[i])));
                        } /*else if(objs[i] instanceof java.sql.Date) {
                            sb.append(StringRegularOpt.quotedString(DatetimeOpt.convertDatetimeToString(
                                    (java.sql.Date) objs[i])));
                        }*/ else {
                            sb.append(StringRegularOpt.quotedString(objs[i].toString()));
                        }
                    }
                }
                return sb.toString();
            } else {
                return "\"\"";
            }
        } else if (objValue instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            int vc = 0;
            Collection<?> valueList = (Collection<?>) objValue;
            for (Object ov : valueList) {
                if (ov != null) {
                    if (vc > 0)
                        sb.append(",");
                    if (ReflectionOpt.isNumberType(ov.getClass()))
                        sb.append(ov.toString());
                    else if (ov instanceof java.util.Date)
                        sb.append(
                            StringRegularOpt.quotedString(
                                DatetimeOpt.convertDatetimeToString((java.util.Date) ov)));
                    /*else if(ov instanceof java.sql.Date)
                        sb.append(
                                StringRegularOpt.quotedString(DatetimeOpt.convertDatetimeToString(
                                                (java.sql.Date) ov)));*/
                    else
                        sb.append(StringRegularOpt.quotedString(ov.toString()));
                    vc++;
                }
            }
            if (vc == 0)
                return "\"\"";
            return sb.toString();
        } else if (objValue instanceof java.util.Date) {
            return StringRegularOpt.quotedString(
                DatetimeOpt.convertDatetimeToString((java.util.Date) objValue));
        }/*else if(objValue instanceof java.sql.Date){
            return StringRegularOpt.quotedString( DatetimeOpt.convertDatetimeToString(
                    DatetimeOpt.convertToUtilDate(
                            (java.sql.Date) objValue)));
        }*/ else
            return StringRegularOpt.quotedString(objValue.toString());
    }

    /**
     * get value and label from formula and translate
     * 变量 形式如 ${变量名}
     * 标识符名-》标识符值的转变
     * 标识符 是以 字母和下划线开头的 占位符
     *
     * @param szFormula 表达式
     * @param varTrans  解释器
     * @return 新的表达式
     */
    public static String runPretreatment(String szFormula, VariableTranslate varTrans) {
        Lexer varMorp = new Lexer();
        varMorp.setFormula(szFormula);
        StringBuilder sDesFormula = new StringBuilder();
        String sWord = varMorp.getAWord();

        while (sWord != null && !sWord.equals("")) {
            if (sWord.equals("$")) {
                sWord = varMorp.getAWord();
                if (sWord.equals("{")) {
                    sWord = varMorp.getStringUntil("}");
                    sDesFormula.append(objectToFormulaString(varTrans.getVarValue(sWord)))
                        .append(" ");
                } else
                    sDesFormula.append("$" + sWord + " ");
            } else if (Lexer.isLabel(sWord) && !VariableFormula.isKeyWord(sWord) && EmbedFunc.getFuncNo(sWord) == -1) {
                sDesFormula.append(objectToFormulaString(varTrans.getVarValue(sWord)))
                    .append(" ");
            } else
                sDesFormula.append(sWord).append(" ");

            sWord = varMorp.getAWord();
        }
        return sDesFormula.toString();
    }


    /**
     * mapTemplateString
     * 变量 形式如 {变量名} 注意这个和上面的不一，变量必须放在{}中
     *
     * @param template  模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param varTrans  变量解释其
     * @param nullValue 找不到变量时的值
     * @return 新的表达式
     */
    public static String mapTemplateString(String template, VariableTranslate varTrans, String nullValue) {
        if (StringUtils.isBlank(template)) {
            return nullValue;
        }
        Lexer varTemplate = new Lexer();
        varTemplate.setFormula(template);
        StringBuilder mapString = new StringBuilder();
        int nlen = template.length();
        int bp = 0;
        while (true) {
            String aword = varTemplate.getARawWord();
            while (true) {
                // 检查转义符
                if ("\\".equals(aword)) {
                    int ep = varTemplate.getCurrPos();
                    mapString.append(template, bp, ep - 1);
                    //获取 \\ 后面的一个字符
                    mapString.append(template.charAt(ep));
                    varTemplate.setPosition(ep + 1);
                    bp = varTemplate.getCurrPos();
                    //aword = varTemplate.getAWord();
                } else if ("{".equals(aword) || aword == null || "".equals(aword)) {
                    break;
                }
                aword = varTemplate.getARawWord();
            }
            if (!"{".equals(aword))
                break;

            int ep = varTemplate.getCurrPos();
            if (ep - 1 > bp) {
                mapString.append(template.substring(bp, ep - 1));
            }

            varTemplate.seekToRightBrace();
            bp = varTemplate.getCurrPos();
            if (bp - 1 > ep) {
                String valueName = template.substring(ep, bp - 1);
                mapString.append( StringBaseOpt.castObjectToString(
                        varTrans.getVarValue(valueName), nullValue));
                /*ReflectionOpt.attainExpressionValue(object,valueName)*/
            }
        }
        if (bp < nlen)
            mapString.append(template.substring(bp));
        return mapString.toString();
    }

    /**
     * mapTemplateString
     * 变量 形式如 {变量名} 注意这个和上面的不一，变量必须放在{}中
     *
     * @param template  模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param object    传入的对象，可以是一个Map 、JSON 或者Pojo
     * @param nullValue 找不到变量时的值
     * @return 新的表达式
     */
    public static String mapTemplateString(String template, Object object, String nullValue) {
        return mapTemplateString(template, new ObjectTranslate(object), nullValue);
    }

    /**
     * mapTemplateString
     * 变量 形式如 {变量名} 注意这个和上面的不一，变量必须放在{}中
     *
     * @param template 模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param object   传入的对象，可以是一个Map 、JSON 或者Pojo
     * @return 新的表达式
     */
    public static String mapTemplateString(String template, Object object) {
        if(object instanceof VariableTranslate){
            return mapTemplateString(template, (VariableTranslate) object, "");
        }
        return mapTemplateString(template, new ObjectTranslate(object), "");
    }
}
