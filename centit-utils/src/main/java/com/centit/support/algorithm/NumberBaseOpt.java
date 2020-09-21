package com.centit.support.algorithm;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * 1. capitalization 将数据转换为大写，用户发票中的大写输出。
 * 2. uppercaseCN 将数据转换为中文，比如 2017 - 二〇一七。
 * 3. getNumByte 获取数据字符串中某一位上的数值，比如获取十位上的数据参数就传入 2
 * 4. castObjectTo* 一组类型转换算法
 * 5. compareTwo* 数据之间比较，主要是为了避免 NullPointerException。
 * 6. parse* 转换字符串为数值类型， 主要是为了避免 NullPointerException。
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class NumberBaseOpt {
    final static private String CNum[] = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    final static private String CNum2[] = {"〇", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    final static private String CBit[] = {"", "拾", "佰", "仟"};
    final static private String CBit2[] = {"", "十", "百", "千"};
    private NumberBaseOpt() {
        throw new IllegalAccessError("Utility class");
    }

    /*
     * 获得某一位上的数值，如果 nBit<0 则获得小数点后面的位数
     */
    static public char getNumByte(String szNum, int nBit) {
        int sl = szNum.length();
        int nPos = 0;
        while (nPos < sl && szNum.charAt(nPos) != '.') nPos++;
        if (nBit < 0)
            nPos = nPos - nBit;
        else
            nPos = nPos - nBit - 1;
        if (nPos < 0 || nPos >= sl) return '0';
        return szNum.charAt(nPos);
    }

    //拾佰仟万拾佰仟亿拾佰仟萬
    /*
     * 将数值大写
     */
    static String capitalization(String szNum, String cnNum[], String cnBit[]) {
        StringBuilder resstr = new StringBuilder();
        String tmpstr = szNum.trim();
        int sl = tmpstr.length();
        int sp = 0;
        int dotpos = tmpstr.indexOf('.');
        if (dotpos != -1) {
            while (sl > 1 && tmpstr.charAt(sl - 1) == '0') sl--;
            if (tmpstr.charAt(sl - 1) == '.') sl--;
            if (sl != tmpstr.length()) {
                tmpstr = tmpstr.substring(0, sl);
            }
        } else dotpos = sl;
        if (sl < 1) return cnNum[0];
        if (tmpstr.charAt(0) == '-') {
            resstr.append("负");
            sp = 1;
        }
        String integerNum = tmpstr.substring(sp, dotpos - sp);
        String decimalNum = "";
        if (dotpos + 1 < sl) decimalNum = tmpstr.substring(dotpos + 1);
        sl = integerNum.length();
        sp = 0;
        while (sp < sl && integerNum.charAt(sp) == '0') sp++;
        if (sp > 0) integerNum = integerNum.substring(sp);
        int inl = integerNum.length();
        if (inl > 0) {
            int h = (inl - 1) % 4;
            int j = (inl - 1) / 4 + 1;
            sp = 0;
            boolean allzero = false;
            boolean preallzero = false;
            for (; j > 0; j--) {
                int k = h;
                h = 3;
                boolean preiszero = allzero;
                allzero = true;
                for (; k >= 0; k--, sp++) {
                    if (integerNum.charAt(sp) == '0')
                        preiszero = true;
                    else {
                        allzero = false;
                        if (preiszero)
                            resstr.append("零");
                        preiszero = false;
                        resstr.append(cnNum[(byte) (integerNum.charAt(sp)) - 48]).append(cnBit[k]);
                    }
                }// end for k
                if (/*j!=0 &&*/ j % 2 == 0) {
                    if (!allzero)
                        resstr.append("万");
                } else {
                    if (!allzero || !preallzero) {
                        int repyi = j / 2;
                        for (int i = 0; i < repyi; i++)
                            resstr.append("亿");
                    }
                }
                preallzero = allzero;
            }//end for j
        } else
            resstr.append("零");

        int dnl = decimalNum.length();
        if (dnl > 0) {
            resstr.append("点");
            for (int i = 0; i < dnl; i++) {
                resstr.append(cnNum[(byte) (decimalNum.charAt(i)) - 48]);
            }
        }
        return resstr.toString();
    }

    public static String capitalization(String szNum) {
        return capitalization(szNum, CNum, CBit);
    }

    public static String capitalizationCN(String szNum) {
        String sValue = capitalization(szNum, CNum2, CBit2);
        if(sValue.startsWith("一十") && sValue.length()==3){
            return sValue.substring(1);
        }
        return sValue;
    }
    /*
     * 仅仅是把 0~9 转换为 "〇","一","二","三","四","五","六","七","八","九"
     */
    public static String uppercaseCN(String szNum) {
        StringBuilder resstr = new StringBuilder();
        String tmpstr = szNum.trim();
        int sl = tmpstr.length();
        int sp = 0;

        if (sl < 1) return CNum2[0];
        for (; sp < sl; sp++)
            if (tmpstr.charAt(sp) >= '0' && tmpstr.charAt(sp) <= '9')
                resstr.append(CNum2[tmpstr.charAt(sp) - '0']);
            else
                resstr.append(tmpstr.charAt(sp));
        return resstr.toString();
    }


    public static String capitalization(String szNum, final boolean isSimple) {
        if (isSimple) {
            return uppercaseCN(szNum);
        }
        return capitalization(szNum, CNum, CBit);
    }

    static public boolean isNumber(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Number)
            return true;
        return StringRegularOpt.isNumber(StringBaseOpt.objectToString(obj));
    }

    /*
     * 这个仅仅是对Long.parseLong进行简单的封装避免重复的输入try catch
     */
    static public Long parseLong(String sNum, Long errorValue) {
        Long lValue;
        try {
            lValue = Long.parseLong(StringRegularOpt.trimString(sNum));
        } catch (NumberFormatException e) {
            lValue = errorValue;
        }
        return lValue;
    }


    static public Long parseLong(String sNum) {
        return parseLong(sNum, null);
    }

    /*
     * 这个仅仅是对Integer.parseInteger进行简单的封装避免重复的输入try catch
     */
    static public Integer parseInteger(String sNum, Integer errorValue) {
        Integer lValue;
        try {
            lValue = Integer.parseInt(StringRegularOpt.trimString(sNum));
        } catch (NumberFormatException e) {
            lValue = errorValue;
        }
        return lValue;
    }


    /*
     * 这个仅仅是对Integer.parseInteger进行简单的封装避免重复的输入try catch
     */
    static public Integer parseInteger(String sNum) {
        return parseInteger(sNum, null);
    }

    /*
     * 这个仅仅是对Double.parseDouble进行简单的封装避免重复的输入try catch
     */
    public static Double parseDouble(String sNum, Double errorValue) {
        Double lValue;
        try {
            lValue = Double.parseDouble(StringRegularOpt.trimString(sNum));
        } catch (NumberFormatException e) {
            lValue = errorValue;
        }
        return lValue;
    }

    /*
     * 这个仅仅是对Float.parseDouble进行简单的封装避免重复的输入try catch
     */
    public static Float parseFloat(String sNum, Float errorValue) {
        Float lValue;
        try {
            lValue = Float.parseFloat(StringRegularOpt.trimString(sNum));
        } catch (NumberFormatException e) {
            lValue = errorValue;
        }
        return lValue;
    }


    static public Double parseDouble(String sNum) {
        return parseDouble(sNum, null);
    }

    /*
     * 将一个Object转换为 long
     */
    public static Long castObjectToLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof Date) {
            return ((Date) obj).getTime();
        }
        if (obj instanceof String) {
            if (StringUtils.isBlank((String) obj)) {
                return null;
            }
            return parseLong((String) obj, null);
        }
        return parseLong(StringBaseOpt.objectToString(obj), null);
    }

    public static Long castObjectToLong(Object obj, Long defaultValue) {
        return GeneralAlgorithm.nvl(castObjectToLong(obj), defaultValue);
    }

    public static Integer castObjectToInteger(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof Integer)
            return (Integer) obj;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof Date) {
            return Long.valueOf(((Date) obj).getTime()).intValue();
        }
        if (obj instanceof String) {
            if (StringUtils.isBlank((String) obj)) {
                return null;
            }
            return parseInteger((String) obj, null);
        }
        return parseInteger(StringBaseOpt.objectToString(obj), null);
    }

    public static Integer castObjectToInteger(Object obj, Integer defaultValue) {
        return GeneralAlgorithm.nvl(castObjectToInteger(obj), defaultValue);
    }

    /*
     * 将一个Object转换为 Float
     */
    public static Float castObjectToFloat(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof Float)
            return (Float) obj;
        if (obj instanceof Number)
            return ((Number) obj).floatValue();
        if (obj instanceof String) {
            if (StringUtils.isBlank((String) obj)) {
                return null;
            }
            return parseFloat((String) obj, null);
        }
        return parseFloat(StringBaseOpt.objectToString(obj), null);
    }

    public static Float castObjectToFloat(Object obj, Float defaultValue) {
        return GeneralAlgorithm.nvl(castObjectToFloat(obj), defaultValue);
    }

    /*
     * 将一个Object转换为 Double
     */
    public static Double castObjectToDouble(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof Double)
            return (Double) obj;
        /*if (obj instanceof Integer)
            return ((Integer) obj).doubleValue();
        if (obj instanceof Long)
            return ((Long) obj).doubleValue();
        if (obj instanceof Float)
            return ((Float) obj).doubleValue();*/
        if (obj instanceof Number)
            return ((Number) obj).doubleValue();
        if (obj instanceof String) {
            if (StringUtils.isBlank((String) obj)) {
                return null;
            }
            return parseDouble((String) obj, null);
        }
        return parseDouble(StringBaseOpt.objectToString(obj), null);
    }

    public static Double castObjectToDouble(Object obj, Double defaultValue) {
        return GeneralAlgorithm.nvl(castObjectToDouble(obj), defaultValue);
    }

    public static BigInteger castObjectToBigInteger(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof BigInteger)
            return (BigInteger) obj;
        return new BigInteger(StringBaseOpt.objectToString(obj));
    }

    public static BigInteger castObjectToBigInteger(Object obj, BigInteger defaultValue) {
        return GeneralAlgorithm.nvl(castObjectToBigInteger(obj), defaultValue);
    }

    public static BigDecimal castObjectToBigDecimal(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof BigDecimal)
            return (BigDecimal) obj;
        if (obj instanceof BigInteger)
            return new BigDecimal((BigInteger) obj);
        /*if (obj instanceof Double){
            return new BigDecimal((Double)obj);
        }*/
        return new BigDecimal(obj.toString());
    }

    public static BigDecimal castObjectToBigDecimal(Object obj, BigDecimal defaultValue) {
        return GeneralAlgorithm.nvl(castObjectToBigDecimal(obj), defaultValue);
    }

    public static Number castObjectToNumber(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof Number) {
            return (Number) obj;
        }
        if (obj instanceof Date) {
            return ((Date) obj).getTime();
        }

        String strNum = StringRegularOpt.trimNumber(StringBaseOpt.castObjectToString(obj));
        if (strNum.indexOf('.') < 0) {
            return castObjectToLong(strNum);
        } else {
            return castObjectToBigDecimal(strNum);
        }
    }

    public static Number round(Number number, int pos) {
        double power = Math.pow(10, pos);
        long value = Math.round(number.doubleValue() * power);
        BigDecimal bd = BigDecimal.valueOf(value, pos);
        return pos < 0 ? bd.longValue() : bd;
    }

    public static Number floor(Number number, int pos) {
        double power = Math.pow(10, pos);
        double value = Math.floor(number.doubleValue() * power);
        BigDecimal bd = BigDecimal.valueOf(Double.valueOf(value).longValue(), pos);
        return pos < 0 ? bd.longValue() : bd;
    }

    public static Number ceil(Number number, int pos) {
        double power = Math.pow(10, pos);
        double value = Math.ceil(number.doubleValue() * power);
        BigDecimal bd = BigDecimal.valueOf(Double.valueOf(value).longValue(), pos);
        return pos < 0 ? bd.longValue() : bd;
    }
}
