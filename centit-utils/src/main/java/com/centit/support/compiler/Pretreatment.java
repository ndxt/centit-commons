package com.centit.support.compiler;

import com.centit.support.algorithm.GeneralAlgorithm;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.network.UrlOptUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 字符串模板转换
 */
public abstract class Pretreatment {

    private Pretreatment() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * mapTemplateString
     * 变量 形式如 {变量名} 注意这个和上面的不一，变量必须放在{}中
     *
     * @param template      模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param varTrans      变量解释其
     * @param nullValue     找不到变量时的值
     * @param canOmitDollar 是否可以忽略 { 前面的 $ 符号
     * @return 新的表达式
     */
    private static String innerMapTemplateString(String template, VariableTranslate varTrans,
                                                 String nullValue, boolean canOmitDollar, boolean asFormula, boolean asUrl) {
        if (StringUtils.isBlank(template)) {
            return nullValue;
        }
        Lexer varTemplate = new Lexer();
        varTemplate.setFormula(template);
        StringBuilder mapString = new StringBuilder();
        int nlen = template.length();
        int bp = 0;
        int prePos = 0;
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
                } else if ("$".equals(aword)) {
                    aword = varTemplate.getARawWord();
                    if ("{".equals(aword) || StringUtils.isBlank(aword)) {
                        break;
                    }
                } else if ((canOmitDollar && "{".equals(aword)) || StringUtils.isBlank(aword)) {
                    break;
                }
                prePos = varTemplate.getCurrPos();
                aword = varTemplate.getARawWord();
            }
            if (!"{".equals(aword))
                break;

            int ep = varTemplate.getCurrPos();
            if (prePos > bp) {
                mapString.append(template, bp, prePos);
            }

            varTemplate.seekToRightBrace();
            bp = varTemplate.getCurrPos();
            if (bp - 1 > ep) {
                String valueName = template.substring(ep, bp - 1);
                if (asFormula) {
                    if (asUrl){
                        mapString.append(UrlOptUtils.objectToUrlString(
                            GeneralAlgorithm.nvl(VariableFormula.calculate(valueName, varTrans), nullValue) )) ;
                    } else {
                        mapString.append(StringBaseOpt.castObjectToString(
                            VariableFormula.calculate(valueName, varTrans), nullValue));
                    }
                } else {
                    if (asUrl){
                        mapString.append(UrlOptUtils.objectToUrlString(
                            GeneralAlgorithm.nvl(varTrans.getVarValue(valueName), nullValue) )) ;
                    } else {
                        mapString.append(StringBaseOpt.castObjectToString(
                            varTrans.getVarValue(valueName), nullValue));
                    }
                }
                /*ReflectionOpt.attainExpressionValue(object,valueName)*/
            }
        }
        if (bp < nlen) {
            mapString.append(template.substring(bp));
        }
        return mapString.toString();
    }

    /**
     * mapTemplateString
     * 变量 形式如 {变量名} 注意这个和上面的不一，变量必须放在{}中
     *
     * @param template      模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param object        变量解释其
     * @param nullValue     找不到变量时的值
     * @param canOmitDollar 是否可以忽略 { 前面的 $ 符号
     * @return 新的表达式
     */
    public static String mapTemplateString(String template, Object object, String nullValue, boolean canOmitDollar) {
        if (object instanceof VariableTranslate) {
            return innerMapTemplateString(template, (VariableTranslate) object, nullValue, canOmitDollar, false, false);
        }
        return innerMapTemplateString(template, new ObjectTranslate(object), nullValue, canOmitDollar, false, false);
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
        return mapTemplateString(template, object, nullValue, true);
    }

    /**
     * mapTemplateString
     * 变量 形式如 {变量名} 注意这个和上面的不一，变量必须放在{}中
     *
     * @param template      模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param object        传入的对象，可以是一个Map 、JSON 或者Pojo
     * @param canOmitDollar 是否可以忽略 { 前面的 $ 符号
     * @return 新的表达式
     */
    public static String mapTemplateString(String template, Object object, boolean canOmitDollar) {
        return mapTemplateString(template, object, "", canOmitDollar);
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
        return mapTemplateString(template, object, "", true);
    }

    /**
     * mapTemplateStringAsFormula
     * 表达式 形式如 {表达式} 注意这个和上面的不一，表达式必须放在{}中
     *
     * @param template      模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param object        变量解释其
     * @param nullValue     找不到变量时的值
     * @param canOmitDollar 是否可以忽略 { 前面的 $ 符号
     * @return 新的表达式
     */
    public static String mapTemplateStringAsFormula(String template, Object object, String nullValue, boolean canOmitDollar) {
        if (object instanceof VariableTranslate) {
            return innerMapTemplateString(template, (VariableTranslate) object, nullValue, canOmitDollar, true, false);
        }

        return innerMapTemplateString(template,
            new ObjectTranslate(object), nullValue, canOmitDollar, true, false);
    }


    /**
     * mapTemplateStringAsFormula
     * 表达式 形式如 {表达式} 注意这个和上面的不一，表达式必须放在{}中
     *
     * @param template  模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param object    传入的对象，可以是一个Map 、JSON 或者Pojo
     * @param nullValue 找不到变量时的值
     * @return 新的表达式
     */
    public static String mapTemplateStringAsFormula(String template, Object object, String nullValue) {
        return mapTemplateStringAsFormula(template, object, nullValue, true);
    }

    /**
     * mapTemplateStringAsFormula
     * 表达式 形式如 {表达式} 注意这个和上面的不一，表达式必须放在{}中
     *
     * @param template      模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param object        传入的对象，可以是一个Map 、JSON 或者Pojo
     * @param canOmitDollar 是否可以忽略 { 前面的 $ 符号
     * @return 新的表达式
     */
    public static String mapTemplateStringAsFormula(String template, Object object, boolean canOmitDollar) {
        return mapTemplateStringAsFormula(template, object, "", canOmitDollar);
    }

    /**
     * mapTemplateStringAsFormula
     * 表达式 形式如 {表达式} 注意这个和上面的不一，表达式必须放在{}中
     *
     * @param template 模板，比如： 你的姓名是{usreCode} , 传入有userCode建的map或者有userCode属性的对象
     * @param object   传入的对象，可以是一个Map 、JSON 或者Pojo
     * @return 新的表达式
     */
    public static String mapTemplateStringAsFormula(String template, Object object) {
        return mapTemplateStringAsFormula(template, object, "", true);
    }

    public static String mapUrlTemplateAsFormula(String template, Object object) {
        if (object instanceof VariableTranslate) {
            return innerMapTemplateString(template, (VariableTranslate) object, "", true, true, true);
        }
        return innerMapTemplateString(template, new ObjectTranslate(object), "", true, true, true);
    }

    public static String mapUrlTemplate(String template, Object object) {
        if (object instanceof VariableTranslate) {
            return innerMapTemplateString(template, (VariableTranslate) object, "", true, false, true);
        }
        return innerMapTemplateString(template, new ObjectTranslate(object), "", true, false, true);
    }
}
