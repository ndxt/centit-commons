package com.centit.support.algorithm;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract class StringRegularOpt {

    private StringRegularOpt() {
        throw new IllegalAccessError("Utility class");
    }

    public static String trimString(String szWord) {
        if (szWord == null)
            return "";
        String strWord = szWord.trim();

        int sl = strWord.length();
        if (sl >= 2 && ((strWord.charAt(0) == '\"' && strWord.charAt(sl - 1) == '\"') ||
            (strWord.charAt(0) == '\'' && strWord.charAt(sl - 1) == '\'')||
            (strWord.charAt(0) == '`' && strWord.charAt(sl - 1) == '`'))) { // 96 控制字符
            if (sl > 2)
                strWord = strWord.substring(1, sl - 1);
            else
                strWord = "";
        }
        return strWord;
    }

    public static String trimStringBlankAsNull(String szWord) {
        if (szWord == null)
            return null;
        String strWord = szWord.trim();
        if(StringUtils.isBlank(strWord))
            return null;
        return strWord;
    }

    public static String trimStringQuoted(String szWord) {
        return trimString(szWord);
    }

    public static String quotedString(String szWord) {
        if (szWord == null)
            return "\"\"";
        return "\"" + StringUtils.replace(szWord.trim(), "\"", "'") + "\"";
    }

    /**
     * 判断 一个字符串是否为 双子节 字符，比如 中文、日语、韩文等等
     *
     * @param letter char
     * @return true or false
     */
    public static boolean isDoubleByteChar(char letter) {
        return (letter / 0x80) != 0;
    }

    public static boolean isDigit(String pszNum) {
        return StringUtils.isNumeric(pszNum);
    }

    public static boolean isNumber(String szNum) {
        if(szNum == null){
            return false;
        }
        //String szNum = trimString(pszNum);
        int sl = szNum.length();
        if (sl < 1) return false;
        int sp = 0;
        while (sp < sl && (szNum.charAt(sp) == ' ' || szNum.charAt(sp) == 9)) sp++;
        if (sp < sl && (szNum.charAt(sp) == '-' || szNum.charAt(sp) == '+')) sp++;
        if (sp == sl || (sp + 1 == sl && szNum.charAt(sp) == '.')) return false;//
        while (sp < sl) {
            if (szNum.charAt(sp) >= '0' && szNum.charAt(sp) <= '9') {
                sp++;
                continue;
            }
            if (szNum.charAt(sp) == '.') {
                sp++;
                break;
            }
            return false;
        }
        //if ((sp==sl) && (sl>1) && (szNum.charAt(sp-1)!='.') && (szNum.charAt(0)=='0')) return false;;
        while (sp < sl) {
            if (szNum.charAt(sp) >= '0' && szNum.charAt(sp) <= '9') {
                sp++;
            } else
                return false;
        }
        return true;
    }

    public static boolean isString(String szWord) {
        if (szWord == null)
            return false;
        String strWord = szWord.trim();
        int sl = strWord.length();
        return (sl >= 2 && ((strWord.charAt(0) == '\"' && strWord.charAt(sl - 1) == '\"') ||
            (strWord.charAt(0) == '\'' && strWord.charAt(sl - 1) == '\'')));
    }

    /**
     * 判断字符串是否为空(null || ""),是：true,否：false
     * 和StringUtils中的isBlank等价 建议使用 StringUtils.isBlank
     *
     * @param str str
     * @return boolean 字符串是否为空
     * @see org.apache.commons.lang3.StringUtils
     */
    public static boolean isNvl(String str) {
        return (str == null) || "".equals(str.trim());
    }

    public static boolean isTrue(String str) {
        /**
         * //SONAR 检查不通过，但是这个是对的，式作为常量的加速判断
         */
        if (str == null) {
            return false;
        }
        if (StringUtils.equalsAnyIgnoreCase(str, "y", "yes", "t", "true", "on")) {
            return true;
        }

        if (!isNumber(str)) {
            return false;
        }
        long bRes = Math.round(Double.valueOf(str));
        return bRes != 0;
    }

    public static boolean isFalse(String str) {
        /**
         * //SONAR 检查不通过，但是这个是对的，式作为常量的加速判断
         */
        if (str == null) {
            return false;
        }
        if (StringUtils.equalsAnyIgnoreCase(str, "n", "no", "f", "false", "off")) {
            return true;
        }
        if (!isNumber(str)) {
            return false;
        }
        long bRes = Math.round(Double.valueOf(str));
        return bRes == 0;
    }

    public static boolean isDatetime(String szTime, Calendar t_time) {
        if (szTime == null)
            return false;
        //t_time.setTime(time)
        int sl = szTime.length();
        int sp = 0;
        int s = 0;
        String c = "";
        int y, m, d, h, min, sec;
        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 4)
                    c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime[sp] == '-')
            sp++;
            break;
        }//check year
        y = Integer.valueOf(c);
        if (y < 1970 || y > 2038) return false;

        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        s = 0;
        c = "";
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2)
                    c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime.charAt(sp) == '-')
            sp++;
            break;
        }//check month
        if (s > 2) return false;
        m = Integer.valueOf(c);
        if (m < 1 || m > 12) return false;

        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        s = 0;
        c = "";
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2) c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime.charAt(sp) == '-')
            sp++;
            break;
        }//check day
        if (s > 2) return false;
        d = Integer.valueOf(c);
        if (d < 1 || d > 31) return false;

        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        s = 0;
        c = "";
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2) c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime.charAt(sp) == ':')
            sp++;
            break;
        }//check hour
        if (s > 2) return false;
        h = Integer.valueOf(c);
        if (h < 0 || h > 24) return false;

        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        s = 0;
        c = "";
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2) c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime.charAt(sp) == ':')
            sp++;
            break;
        }//check minute
        if (s > 2) return false;
        min = Integer.valueOf(c);
        if (min < 0 || min > 60) return false;
        s = 0;
        c = "";
        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2) c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            break;
        }//check second
        if (s > 2) return false;
        sec = Integer.valueOf(c);
        if (sec < 0 || sec > 60) return false;
        t_time.set(y, m, d, h, min, sec);
        return true;
    }

    public static boolean isDatetime(String szTime) {
        Calendar t_time = Calendar.getInstance();
        boolean b = isDatetime(szTime, t_time);
        return b;
    }

    public static boolean isDate(String szTime, Calendar t_time) {
        if (szTime == null)
            return false;
        //t_time.setTime(time)
        int sl = szTime.length();
        int sp = 0;
        int s = 0;
        String c = "";
        int y, m, d;
        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 4)
                    c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime[sp] == '-')
            sp++;
            break;
        }//check year
        y = Integer.valueOf(c);
        if (y < 1970 || y > 2038) return false;

        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        s = 0;
        c = "";
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2)
                    c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime.charAt(sp) == '-')
            sp++;
            break;
        }//check month
        if (s > 2) return false;
        m = Integer.valueOf(c);
        if (m < 1 || m > 12) return false;

        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        s = 0;
        c = "";
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2) c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime.charAt(sp) == '-')
            sp++;
            break;
        }//check day
        if (s > 2) return false;
        d = Integer.valueOf(c);
        if (d < 1 || d > 31) return false;

        t_time.set(y, m, d, 0, 0, 0);
        return true;
    }

    public static boolean isDate(String szTime) {
        Calendar t_time = Calendar.getInstance();
        boolean b = isDate(szTime, t_time);
        return b;
    }

    public static boolean isTime(String szTime, Calendar t_time) {
        if (szTime == null)
            return false;
        //t_time.setTime(time)
        int sl = szTime.length();
        int sp = 0;
        int s = 0;
        String c = "";
        int h, min, sec;
        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        s = 0;
        c = "";
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2) c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime.charAt(sp) == ':')
            sp++;
            break;
        }//check hour
        if (s > 2) return false;
        h = Integer.valueOf(c);
        if (h < 0 || h > 24) return false;

        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        s = 0;
        c = "";
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2) c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            //if(szTime.charAt(sp) == ':')
            sp++;
            break;
        }//check minute
        if (s > 2) return false;
        min = Integer.valueOf(c);
        if (min < 0 || min > 60) return false;
        s = 0;
        c = "";
        while (sp < sl && (szTime.charAt(sp) < '0' || szTime.charAt(sp) > '9')) sp++;
        while (sp < sl) {
            if (szTime.charAt(sp) >= '0' && szTime.charAt(sp) <= '9') {
                if (s < 2) c += szTime.charAt(sp);
                sp++;
                s++;
                continue;
            }
            break;
        }//check second
        if (s > 2) return false;
        sec = Integer.valueOf(c);
        if (sec < 0 || sec > 60) return false;
        t_time.set(2010, 10, 10, h, min, sec);
        return true;
    }

    public static boolean isTime(String szTime) {
        Calendar t_time = Calendar.getInstance();
        boolean b = isTime(szTime, t_time);
        return b;
    }

    private static boolean appendDatePart(StringBuilder sDate, StringBuilder sDatePart, int part){
        if(sDatePart.length()<1) {
            return false;
        }
        if (part > 0 && part < 3)
            sDate.append('-');
        else if (part == 3)
            sDate.append(' ');
        else if (part > 3 && part < 6)
            sDate.append(':');
        else if (part == 6)
            sDate.append('.');

        if (sDatePart.length() == 1) {
            sDate.append('0');
        }
        sDate.append(sDatePart);
        return true;
    }
    public static String trimDateString(String szDateStr) {
        if (szDateStr == null)
            return null;
        int sl = szDateStr.length();
        StringBuilder sB = new StringBuilder(25);
        StringBuilder datePart = new StringBuilder(5);
        int nPart = 0;
        boolean bDot = false;
        boolean hasYearPart = false;
        for (int j = 0; j < sl; j++) {
            if (szDateStr.charAt(j) >= '0' && szDateStr.charAt(j) <= '9') {
                if (bDot) {
                    if (appendDatePart(sB, datePart, nPart)) {
                        nPart++;
                    }
                    bDot = false;
                    datePart.setLength(0);
                }
                if (nPart > 6)
                    break;
                datePart.append(szDateStr.charAt(j));
                int partLen = datePart.length();
                if( ! hasYearPart && nPart <= 3 && partLen == 4){
                    hasYearPart = true;
                    bDot = true;
                } else if( (hasYearPart || nPart>3) && nPart <6 && partLen == 2){
                    bDot = true;
                } else if( nPart ==6 && partLen == 3){
                    bDot = true;
                }
            } else { //if(! sTmp2.equals(""))
                bDot = true;
            }
        }
        appendDatePart(sB, datePart, nPart);

        return sB.toString();
    }

    public static String trimDigits(String szDigits) {
        if (szDigits == null)
            return null;
        int sl = szDigits.length();
        StringBuilder sTmp2 = new StringBuilder("");
        for (int j = 0; j < sl; j++) {
            if (szDigits.charAt(j) >= '0' && szDigits.charAt(j) <= '9')
                sTmp2.append(szDigits.charAt(j));
        }
        return sTmp2.toString();
    }

    public static String trimNumber(String szNumber) {
        if (szNumber == null)
            return null;
        int sp = 0;
        int sl = szNumber.length();
        boolean canBeSign = true;
        StringBuilder sTmp2 = new StringBuilder();
        for (int j = 0; j < sl; j++) {
            if (canBeSign && (szNumber.charAt(j) == '-' || szNumber.charAt(j) == '+')) {
                sTmp2.append(szNumber.charAt(j));
                canBeSign = false;
            }
            if (szNumber.charAt(j) >= '0' && szNumber.charAt(j) <= '9') {
                sTmp2.append(szNumber.charAt(j));
                canBeSign = false;
            } else if (sp == 0 && szNumber.charAt(j) == '.') {
                sTmp2.append(szNumber.charAt(j));
                canBeSign = false;
                sp = 1;
            }
        }
        return sTmp2.toString();
    }

    /**
     * 字符串模式匹配
     * @param szTempl 模式
     * @param szValue 字符串
     * @param wildcardType 通配符类型  0 windowsType * ？ 1 sqlType % _  2 both
     * @return 是否匹配
     */
    public static boolean isMatch(String szTempl, String szValue, int wildcardType) {
        if (szValue == null || szTempl == null) return false;
        int nLV = szValue.length();
        int nLT = szTempl.length();
        if (nLV == 0 && nLT == 0) return true;
        if (szValue.equals(szTempl)) return true;
        szValue = trimString(szValue);
        szTempl = trimString(szTempl);
        if(wildcardType == 0 || wildcardType == 2){
            String tpl = "^" + szTempl.replaceAll("\\*", "\\\\S*")
                .replaceAll("\\?", "\\\\S") + "$";
            boolean isMath = Pattern.matches(tpl, szValue);
            if(isMath || wildcardType == 0){
                return isMath;
            }
        }
        //if(wildcardType == 1 || wildcardType == 2){
        String tpl = "^" + szTempl.replaceAll("%", "\\\\S*")
            .replaceAll("_", "\\\\S") + "$";
        return Pattern.matches(tpl, szValue);
        //}
    }

    // _  % 是通配符
    public static boolean isSqlMatch(String szTempl, String szValue) {
        return isMatch(szTempl, szValue, 1);
    }

    // ?  * 是通配符
    public static boolean isMatch(String szTempl, String szValue) {
        return isMatch(szTempl, szValue, 0);
    }

    /**
     * 判断是否为中文
     *
     * @param c 中文
     * @return 判断是否为中文
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为中文，剔除标点符号
     *
     * @param c 中文
     * @return 判断是否为中文
     */
    public static boolean isChineseEscapeSymbol(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
            //|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            //|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
            //|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
        ) {
            return true;
        }
        return false;
    }

    /**
     * 获得首个中文位置
     *
     * @param strName strName
     * @return -1 没有中文
     */
    public static int getFirstChinesePos(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获得首个中文位置 ，标点符号不算
     *
     * @param strName strName
     * @return -1 没有中文
     */
    public static int getFirstChinesePosEscapeSymbol(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChineseEscapeSymbol(c)) {
                return i;
            }
        }
        return -1;
    }

}
