package com.centit.support.compiler;

import com.centit.support.algorithm.*;
import com.centit.support.common.LeftRightPair;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class EmbedFunc {
    public static final int functionsSum = 66;
    protected static final FunctionInfo functionsList[] = {
        new FunctionInfo("getat", -1, ConstDefine.FUNC_GET_AT, ConstDefine.TYPE_ANY),//求数组中的一个值  getat (0,"2","3")= "2"  getat (0,2,3)= 2
        new FunctionInfo("byte", 2, ConstDefine.FUNC_BYTE, ConstDefine.TYPE_NUM),    //求位值  byte (4321.789,0)=1
        //          byte (4321.789,-2)=8
        //          byte ("4321.789",2)=3
        new FunctionInfo("capital", 1, ConstDefine.FUNC_CAPITAL, ConstDefine.TYPE_STR),  // capital (123.45)="一百二十三点四五"
        new FunctionInfo("if", 3, ConstDefine.FUNC_IF, ConstDefine.TYPE_ANY),      // if (1,2,3)= 2  if (0,"2","3")= "3"
        new FunctionInfo("case", 2, ConstDefine.FUNC_CASE, ConstDefine.TYPE_ANY),      // case(1,2,3)= null  case(1,2,3,1,"5")= "5"  case(0,1,"2","3")= "3"
        new FunctionInfo("match", 2, ConstDefine.FUNC_MATCH, ConstDefine.TYPE_NUM), //匹配*?为通配符 match ("abcd","a??d")=1
        //             match ("abcd","a*d")=1
        new FunctionInfo("regexmatch", 2, ConstDefine.FUNC_REG_MATCH, ConstDefine.TYPE_NUM), //正则表达式 regexMatch
        new FunctionInfo("regexmatchvalue", 2, ConstDefine.FUNC_REG_MATCH_VALUES, ConstDefine.TYPE_ANY), //正则表达式匹配部分
        new FunctionInfo("max", -1, ConstDefine.FUNC_MAX, ConstDefine.TYPE_ANY),   // 求最大值 max (1,2,3,5,4) = 5
        new FunctionInfo("min", -1, ConstDefine.FUNC_MIN, ConstDefine.TYPE_ANY),    // 求最小值 min (1,2,3,5,4) = 1
        new FunctionInfo("ave", -1, ConstDefine.FUNC_AVE, ConstDefine.TYPE_NUM),    //求均值  ave (1,2,3)=2
        new FunctionInfo("count", -1, ConstDefine.FUNC_COUNT, ConstDefine.TYPE_NUM),    // 计数 count(1,"2",3,"5",1,1,4) = 7
        new FunctionInfo("countnotnull", -1, ConstDefine.FUNC_COUNTNOTNULL, ConstDefine.TYPE_NUM),    // 计数 非空参数 countnotnull(1,,"2",,,,1,1,4) = 5
        new FunctionInfo("countnull", -1, ConstDefine.FUNC_COUNTNULL, ConstDefine.TYPE_NUM),    // 计数空参数  countnull(1,,"2",,,,1,1,4) = 4
        new FunctionInfo("sum", -1, ConstDefine.FUNC_SUM, ConstDefine.TYPE_NUM),    // 求和 sum (1,2,3,4,5) = 15
        new FunctionInfo("stddev", -1, ConstDefine.FUNC_STDDEV, ConstDefine.TYPE_NUM),    // 求标准偏差

        new FunctionInfo("round", -1, ConstDefine.FUNC_ROUND, ConstDefine.TYPE_NUM),    // 四舍五入
        new FunctionInfo("floor", -1, ConstDefine.FUNC_FLOOR, ConstDefine.TYPE_NUM),    // 四舍五入
        new FunctionInfo("ceil", -1, ConstDefine.FUNC_CEIL, ConstDefine.TYPE_NUM),    // 四舍五入
        new FunctionInfo("concat", -1, ConstDefine.FUNC_STRCAT, ConstDefine.TYPE_STR),    // 连接字符串 concat ("12","34","56")="123456"
        new FunctionInfo("strcat", -1, ConstDefine.FUNC_STRCAT, ConstDefine.TYPE_STR),    // 连接字符串 strcat ("12","34","56")="123456"
        new FunctionInfo("isempty", 1, ConstDefine.FUNC_ISEMPTY, ConstDefine.TYPE_NUM),    // 判断参数是否为空 isempty("")=1
        new FunctionInfo("isnotempty", 1, ConstDefine.FUNC_NOTEMPTY, ConstDefine.TYPE_NUM),    // 判断参数是否为空 notempty("")=0


        new FunctionInfo("log", 1, ConstDefine.FUNC_LOG, ConstDefine.TYPE_NUM),    // 求以10为底的对数
        new FunctionInfo("ln", 1, ConstDefine.FUNC_LN, ConstDefine.TYPE_NUM),        // 求自然对数
        new FunctionInfo("sin", 1, ConstDefine.FUNC_SIN, ConstDefine.TYPE_NUM),    // 求正弦
        new FunctionInfo("cos", 1, ConstDefine.FUNC_COS, ConstDefine.TYPE_NUM),    // 求余弦
        new FunctionInfo("tan", 1, ConstDefine.FUNC_TAN, ConstDefine.TYPE_NUM),    // 求正切
        new FunctionInfo("ctan", 1, ConstDefine.FUNC_CTAN, ConstDefine.TYPE_NUM),    // 求余切
        new FunctionInfo("exp", 1, ConstDefine.FUNC_EXP, ConstDefine.TYPE_NUM),    // 求以e为底的指数
        new FunctionInfo("sqrt", 1, ConstDefine.FUNC_SQRT, ConstDefine.TYPE_NUM),    // 求平方根
        //字符串函数
        new FunctionInfo("upcase", 1, ConstDefine.FUNC_UPCASE, ConstDefine.TYPE_STR), // 字符串大写
        new FunctionInfo("lowcase", 1, ConstDefine.FUNC_LOWCASE, ConstDefine.TYPE_STR), // 字符串小写
        new FunctionInfo("substr", 2, ConstDefine.FUNC_SUBSTR, ConstDefine.TYPE_STR), // 求字符串子串 substr ("123456",2,3)="345"
        new FunctionInfo("lpad", 1, ConstDefine.FUNC_LPAD, ConstDefine.TYPE_STR), // 左侧补充字符串
        new FunctionInfo("rpad", 1, ConstDefine.FUNC_RPAD, ConstDefine.TYPE_STR), // 右侧补充字符串
        new FunctionInfo("find", 2, ConstDefine.FUNC_FIND, ConstDefine.TYPE_NUM),  //求子串位置 find ("123456","34")=2  find ("123456","35")=-1
        new FunctionInfo("frequence", 2, ConstDefine.FUNC_FREQUENCE, ConstDefine.TYPE_NUM), // 求子串个数 find ("12345236","23")=2
        new FunctionInfo("split", 2, ConstDefine.FUNC_SPLIT_STR, ConstDefine.TYPE_STR),

        new FunctionInfo("int", 1, ConstDefine.FUNC_INT, ConstDefine.TYPE_NUM), // 求整数部分 int (12.34)=12 int -12.34)=-12
        new FunctionInfo("integer", 1, ConstDefine.FUNC_INT, ConstDefine.TYPE_NUM), // 求整数部分 integer (12.34)=12 int (-12.34)=-12
        new FunctionInfo("frac", 1, ConstDefine.FUNC_FRAC, ConstDefine.TYPE_NUM), // 求小数部分 frac (12.34)=0.34 frac (-12.34)=-0.34

        new FunctionInfo("today", -1, ConstDefine.FUNC_CURRENT_DATETIME, ConstDefine.TYPE_DATE),//当前日期
        new FunctionInfo("currentDate", -1, ConstDefine.FUNC_CURRENT_DATE, ConstDefine.TYPE_DATE),//当前日期
        new FunctionInfo("currentDatetime", -1, ConstDefine.FUNC_CURRENT_DATETIME, ConstDefine.TYPE_DATE),//当前时间
        new FunctionInfo("currentTimestamp", -1, ConstDefine.FUNC_CURRENT_TIMESTAMP, ConstDefine.TYPE_DATE),//当前时间
        new FunctionInfo("day", -1, ConstDefine.FUNC_DAY, ConstDefine.TYPE_STR),//日期函数
        new FunctionInfo("month", -1, ConstDefine.FUNC_MONTH, ConstDefine.TYPE_STR),//日期函数
        new FunctionInfo("year", -1, ConstDefine.FUNC_YEAR, ConstDefine.TYPE_STR),//日期函数
        new FunctionInfo("week", -1, ConstDefine.FUNC_WEEK, ConstDefine.TYPE_STR),// 第几周
        new FunctionInfo("weekday", -1, ConstDefine.FUNC_WEEK_DAY, ConstDefine.TYPE_STR),// 星期几， 取日期的星期几，周日为0，周一~六为1~6
        new FunctionInfo("formatdate", -1, ConstDefine.FUNC_FORMAT_DATE, ConstDefine.TYPE_STR),// 格式化日期
        new FunctionInfo("dateinfo", -1, ConstDefine.FUNC_DATE_INFO, ConstDefine.TYPE_STR),// 日期信息

        new FunctionInfo("dayspan", -1, ConstDefine.FUNC_DAY_SPAN, ConstDefine.TYPE_NUM),//日期函数  求两日期之间的天数
        new FunctionInfo("datespan", -1, ConstDefine.FUNC_DATE_SPAN, ConstDefine.TYPE_NUM),//日期函数  求两日期之间的天数
        //new FunctionInfo("monthspan",-1,ConstDefine.FUNC_MONTH_SPAN,ConstDefine.TYPE_NUM),//日期函数   求两日期之间的月数
        //new FunctionInfo("yearspan",-1,ConstDefine.FUNC_YEAR_SPAN,ConstDefine.TYPE_NUM),//日期函数   求两日期之间的年数

        new FunctionInfo("adddate", 2, ConstDefine.FUNC_ADD_DATE, ConstDefine.TYPE_ANY),//日期函数  加天数
        new FunctionInfo("adddays", 2, ConstDefine.FUNC_ADD_DAYS, ConstDefine.TYPE_ANY),//日期函数  加天数
        new FunctionInfo("addmonths", 2, ConstDefine.FUNC_ADD_MONTHS, ConstDefine.TYPE_ANY),//日期函数  加月数
        new FunctionInfo("addyears", 2, ConstDefine.FUNC_ADD_YEARS, ConstDefine.TYPE_ANY),//日期函数   加年数
        new FunctionInfo("truncdate", -1, ConstDefine.FUNC_TRUNC_DATE, ConstDefine.TYPE_ANY),//日期函数   截断日期  第二个参数  Y ，M , D 分别返回一年、月的第一天 ，或者一日的零点
        new FunctionInfo("lastofmonth", -1, ConstDefine.FUNC_LAST_OF_MONTH, ConstDefine.TYPE_ANY),//日期函数   求这个月的第一天

        new FunctionInfo("toDate", 1, ConstDefine.FUNC_TO_DATE, ConstDefine.TYPE_DATE),// 转换为日期
        new FunctionInfo("toString", 1, ConstDefine.FUNC_TO_STRING, ConstDefine.TYPE_STR),//转换为String
        new FunctionInfo("toNumber", 1, ConstDefine.FUNC_TO_NUMBER, ConstDefine.TYPE_NUM),//转换为数字
        new FunctionInfo("singleton", -1, ConstDefine.FUNC_SINGLETON, ConstDefine.TYPE_ANY),//返回集合，去重

        //new FunctionInfo("getsysstr",1,ConstDefine.FUNC_GET_STR,ConstDefine.TYPE_STR),//取系统字符串
        new FunctionInfo("getpy", 1, ConstDefine.FUNC_GET_PY, ConstDefine.TYPE_STR)//取汉字拼音
    };
    private static double COMPARE_MIN_DOUBLE = 0.0000001;
    private EmbedFunc() {
        throw new IllegalAccessError("Utility class");
    }

    public static int getFuncNo(String sFuncName) {
        for (int i = 0; i < functionsSum; i++) {
            if (sFuncName.equalsIgnoreCase(functionsList[i].sName))
                return i;
        }
        return -1;
    }

    private static LeftRightPair<Integer, List<Object>> flatOperands(List<Object> slOperand) {
        int nCount = 0;
        List<Object> ret = new ArrayList<>();
        if (slOperand != null && slOperand.size() > 0) {
            for (Object obj : slOperand) {
                if (obj instanceof Object[]) {
                    Object[] objs = (Object[]) obj;
                    for (Object obj1 : objs) {
                        ret.add(obj1);
                        nCount++;
                    }
                } else if (obj instanceof Collection) {
                    ret.addAll((Collection) obj);
                    nCount += ((Collection) obj).size();
                } else {
                    ret.add(obj);
                    nCount++;
                }
            }
        }
        return new LeftRightPair<>(nCount, ret);
    }

    private static LeftRightPair<Date, Object> fetchDateOpt(int nOpSum, List<Object> slOperand) {
        Date dt = null;
        Object ti = null;
        if (nOpSum == 1) {
            dt = DatetimeOpt.currentUtilDate();
            if (NumberBaseOpt.isNumber(slOperand.get(0))) {
                ti = slOperand.get(0);
            }
        } else if (nOpSum > 1) {
            dt = DatetimeOpt.castObjectToDate(slOperand.get(0));
            if (NumberBaseOpt.isNumber(slOperand.get(1))) {
                ti = slOperand.get(1);
            }
        }
        return new LeftRightPair<>(dt, ti);
    }

    public static Object runFuncWithObject(List<Object> slOperand, int funcID) {
        int nOpSum = (slOperand == null) ? 0 : slOperand.size();
        double dbtemp = 0.0;
        switch (funcID) {
            case ConstDefine.FUNC_AVE:// 100
            {
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                nOpSum = 0;
                for (int i = 0; i < opt.getLeft(); i++) {
                    Double db = NumberBaseOpt.castObjectToDouble(
                        opt.getRight().get(i));
                    if (db != null) {
                        dbtemp += db;
                        nOpSum++;
                    }
                }
                if (nOpSum > 0)
                    return dbtemp / nOpSum;//"%f",
                else
                    return null;
            }
            case ConstDefine.FUNC_SINGLETON: {
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                List<Object> retObjs = new ArrayList<>();
                for (Object obj : opt.getRight()) {
                    if (obj == null || retObjs.contains(obj)) {
                        continue;
                    }
                    retObjs.add(obj);
                }
                return retObjs;
            }
            case ConstDefine.FUNC_GET_AT: {//148
                if (nOpSum < 2)
                    return null;
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                Object objTemp = slOperand.get(0);
                if (NumberBaseOpt.isNumber(objTemp)) {
                    Integer nbit = NumberBaseOpt.castObjectToInteger(objTemp);
                    if (nbit != null) {
                        if (nbit < 0) {
                            nbit = opt.getLeft() - 1 + nbit;
                        }

                        if (nbit >= 0 && nbit < opt.getLeft() - 1) {
                            return opt.getRight().get(nbit + 1);
                        }
                    }
                }
                return null;
            }
            case ConstDefine.FUNC_BYTE:// 101
                if (nOpSum < 2 || !NumberBaseOpt.isNumber(slOperand.get(1)))
                    return null;
                Object objTemp = slOperand.get(0);
                int nbit = NumberBaseOpt.castObjectToInteger(slOperand.get(1));
                if (NumberBaseOpt.isNumber(objTemp)) {
                    return String.valueOf(
                        NumberBaseOpt.getNumByte(
                            StringBaseOpt.objectToString(objTemp), nbit));
                } else if (objTemp != null) {
                    String tempstr = StringBaseOpt.objectToString(objTemp);
                    int sl = tempstr.length();
                    if (nbit >= 0 && nbit < sl) {
                        return String.valueOf(tempstr.charAt(nbit));
                    }
                }
                return null;

            case ConstDefine.FUNC_MATCH:
                if (nOpSum < 2)
                    return false;
                return StringRegularOpt.isMatch(
                    StringBaseOpt.objectToString(slOperand.get(0)),
                    StringBaseOpt.objectToString(slOperand.get(1)));
            case ConstDefine.FUNC_REG_MATCH:
                if (nOpSum < 2)
                    return false;
                return Pattern.matches(
                    StringBaseOpt.objectToString(slOperand.get(0)),
                    StringBaseOpt.objectToString(slOperand.get(1)));
            case ConstDefine.FUNC_REG_MATCH_VALUES: {
                if (nOpSum < 2)
                    return false;
                String sValues = StringBaseOpt.objectToString(slOperand.get(1));
                Pattern p = Pattern.compile(StringBaseOpt.objectToString(slOperand.get(0)));
                Matcher m = p.matcher(sValues); // 获取 matcher 对象
                List<String> matchValues = new ArrayList<>();
                while (m.find()) {
                    matchValues.add(sValues.substring(m.start(), m.end()));
                }
                return matchValues;
            }
            case ConstDefine.FUNC_CAPITAL:// 102
            {
                if (nOpSum < 1) return null;
                boolean nT = false;
                if (nOpSum > 1)
                    nT = BooleanBaseOpt.castObjectToBoolean(slOperand.get(1), false);
                if (!NumberBaseOpt.isNumber(slOperand.get(0)))
                    return StringUtils.upperCase(
                        StringBaseOpt.objectToString(slOperand.get(0)));
                return NumberBaseOpt.capitalization(
                    StringBaseOpt.objectToString(slOperand.get(0)), nT);
            }
            case ConstDefine.FUNC_MAX: {// 103
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                return GeneralAlgorithm.maxObject(opt.getRight());
            }

            case ConstDefine.FUNC_MIN: {// 104
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                return GeneralAlgorithm.minObject(opt.getRight());
            }

            case ConstDefine.FUNC_COUNT: {// 112
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                return opt.getLeft();
            }
            case ConstDefine.FUNC_COUNTNOTNULL:// 145
            {
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                int nc = 0;
                for (Object obj : opt.getRight()) {
                    if (obj != null) {
                        String s = StringBaseOpt.objectToString(obj);
                        if (StringUtils.isNotBlank(s) &&
                            !"''".equals(s) && !"\"\"".equals(s))
                            nc++;
                    }
                }
                return nc;
            }
            case ConstDefine.FUNC_COUNTNULL:// 144
            {
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                int nc = 0;
                for (Object obj : opt.getRight()) {
                    if (obj == null) {
                        nc++;
                    } else {
                        String s = StringBaseOpt.objectToString(obj);
                        if (StringUtils.isBlank(s) ||
                            "''".equals(s) || "\"\"".equals(s))
                            nc++;
                    }
                }
                return nc;
            }
            case ConstDefine.FUNC_SUM: {// 105
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                return GeneralAlgorithm.sumObjects(opt.getRight());
            }
            case ConstDefine.FUNC_STDDEV:// 133
            {
                LeftRightPair<Integer, List<Object>> opt = flatOperands(slOperand);
                if (opt.getLeft() < 2)
                    return 0;
                int numberSum = 0;
                for (int i = 0; i < opt.getLeft(); i++) {
                    if (NumberBaseOpt.isNumber(opt.getRight().get(i))) {
                        numberSum++;
                        dbtemp += NumberBaseOpt.castObjectToDouble(opt.getRight().get(i), 0.0);
                    }
                }
                if (numberSum < 2) {
                    return 0;
                }
                double dbAvg = dbtemp / numberSum;
                dbtemp = 0.0;
                for (int i = 0; i < opt.getLeft(); i++)
                    if (NumberBaseOpt.isNumber(opt.getRight().get(i))) {
                        double dtp = NumberBaseOpt.castObjectToDouble(opt.getRight().get(i), 0.0)
                            - dbAvg;
                        dbtemp += dtp * dtp;
                    }

                return Math.sqrt(dbtemp / (numberSum - 1));
            }
            case ConstDefine.FUNC_STRCAT:// 106
            {
                if (nOpSum < 1)
                    return null;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < nOpSum; i++) {
                    sb.append(StringBaseOpt.castObjectToString(slOperand.get(i), ""));
                }
                return sb.toString();
            }
            case ConstDefine.FUNC_SUBSTR: {
                if (nOpSum < 1)
                    return null;
                if (nOpSum < 2)
                    return slOperand.get(0);
                if (slOperand.get(0) == null)
                    return null;
                int nStart = 0, nLength;
                if (NumberBaseOpt.isNumber(slOperand.get(1)))
                    nStart = NumberBaseOpt.castObjectToInteger(slOperand.get(1));
                String tempStr = StringBaseOpt.objectToString(slOperand.get(0));
                if (nOpSum > 2 && NumberBaseOpt.isNumber(slOperand.get(2)))
                    nLength = NumberBaseOpt.castObjectToInteger(slOperand.get(2));
                else
                    nLength = tempStr.length();

                if (nLength <= 0)
                    nLength = 1;

                return tempStr.substring(nStart, nStart + nLength);
            }
            case ConstDefine.FUNC_LPAD: {
                if (nOpSum < 1)
                    return null;
                if (nOpSum < 2)
                    return slOperand.get(0);
                int nLength = NumberBaseOpt.castObjectToInteger(slOperand.get(1), 0);
                if (nOpSum > 2) {
                    String padChar = StringBaseOpt.castObjectToString(slOperand.get(2));
                    return StringUtils.leftPad(
                        StringBaseOpt.castObjectToString(slOperand.get(0)), nLength, padChar);
                } else {
                    return StringUtils.leftPad(
                        StringBaseOpt.castObjectToString(slOperand.get(0)), nLength);
                }
            }

            case ConstDefine.FUNC_RPAD: {
                if (nOpSum < 1)
                    return null;
                if (nOpSum < 2)
                    return slOperand.get(0);
                int nLength = NumberBaseOpt.castObjectToInteger(slOperand.get(1), 0);
                if (nOpSum > 2) {
                    String padChar = StringBaseOpt.castObjectToString(slOperand.get(2));
                    return StringUtils.rightPad(
                        StringBaseOpt.castObjectToString(slOperand.get(0)), nLength, padChar);
                } else {
                    return StringUtils.rightPad(
                        StringBaseOpt.castObjectToString(slOperand.get(0)), nLength);
                }
            }

            case ConstDefine.FUNC_FIND: { //index
                if (nOpSum < 2)
                    return -1;
                int nStart = 0;
                if (nOpSum > 2 && NumberBaseOpt.isNumber(slOperand.get(2)))
                    nStart = NumberBaseOpt.castObjectToInteger(slOperand.get(2));
                Object obj = slOperand.get(0);
                if (obj instanceof Collection) {
                    return ((Collection) obj).contains(slOperand.get(1));
                }
                String tempStr = StringBaseOpt.objectToString(obj);
                return tempStr.indexOf(
                    StringBaseOpt.objectToString(slOperand.get(1)), nStart);
            }

            case ConstDefine.FUNC_UPCASE://upcase
            {
                if (nOpSum < 1) return null;
                return StringUtils.upperCase(StringBaseOpt.objectToString(slOperand.get(0)));
            }
            case ConstDefine.FUNC_LOWCASE://lowcase
            {
                if (nOpSum < 1) return null;
                return StringUtils.lowerCase(StringBaseOpt.objectToString(slOperand.get(0)));
            }
            case ConstDefine.FUNC_FREQUENCE: {
                if (nOpSum < 2) return -1;
                if (slOperand.get(0) == null || slOperand.get(1) == null)
                    return 0;
                String tempStr = StringBaseOpt.objectToString(slOperand.get(0));
                String str = StringBaseOpt.objectToString(slOperand.get(1));
                int nSt = 0, sl = str.length(), nC = 0;
                if (sl == 0) {
                    return 0;
                }
                nSt = tempStr.indexOf(str, nSt);
                while (nSt >= 0) {
                    nC++;
                    nSt += sl;
                    nSt = tempStr.indexOf(str, nSt);
                }
                return nC;
            }

            case ConstDefine.FUNC_SPLIT_STR:{ // 分割字符串
                if (nOpSum < 1) return null;
                String str = StringBaseOpt.castObjectToString(slOperand.get(0));
                String splitStr = nOpSum > 1 ? StringBaseOpt.castObjectToString(slOperand.get(0)," "):",";
                return str.split(splitStr);
            }

            case ConstDefine.FUNC_INT: { //取整
                if (nOpSum < 1)
                    return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0)))
                    return slOperand.get(0);
                return NumberBaseOpt.castObjectToInteger(slOperand.get(0));
            }

            case ConstDefine.FUNC_ROUND: {
                if (nOpSum < 1)
                    return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0)))
                    return slOperand.get(0);
                Double tempDouble = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                int pos = nOpSum > 1 ?
                    NumberBaseOpt.castObjectToInteger(slOperand.get(1), 0)
                    : 0;
                if (pos != 0) {
                    return NumberBaseOpt.round(tempDouble, pos);
                }
                return Math.round(tempDouble);
            }
            case ConstDefine.FUNC_FLOOR: {
                if (nOpSum < 1)
                    return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0)))
                    return slOperand.get(0);
                Double tempDouble = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                int pos = nOpSum > 1 ?
                    NumberBaseOpt.castObjectToInteger(slOperand.get(1), 0)
                    : 0;
                if (pos != 0) {
                    return NumberBaseOpt.floor(tempDouble, pos);
                }
                //四舍五入
                return Double.valueOf(Math.floor(tempDouble)).longValue();
            }
            case ConstDefine.FUNC_CEIL: {
                if (nOpSum < 1)
                    return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0)))
                    return slOperand.get(0);
                Double tempDouble = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                int pos = nOpSum > 1 ?
                    NumberBaseOpt.castObjectToInteger(slOperand.get(1), 0)
                    : 0;
                if (pos != 0) {
                    return NumberBaseOpt.ceil(tempDouble, pos);
                }
                return Double.valueOf(Math.ceil(tempDouble)).longValue();
            }

            case ConstDefine.FUNC_ISEMPTY: //判断参数是否为空
                if (nOpSum < 1 || slOperand.get(0) == null)
                    return true;
                return StringUtils.isBlank(StringBaseOpt.objectToString(slOperand.get(0)));

            case ConstDefine.FUNC_NOTEMPTY: //判断参数是否为空
                if (nOpSum < 1 || slOperand.get(0) == null)
                    return false;
                return StringUtils.isNotBlank(StringBaseOpt.objectToString(slOperand.get(0)));

            case ConstDefine.FUNC_LN: {
                if (nOpSum < 1) return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0))) return null;
                double af = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                return Math.log(af);
            }
            case ConstDefine.FUNC_LOG: {
                if (nOpSum < 1) return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0))) return null;
                double af = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                return Math.log10(af);
            }
            case ConstDefine.FUNC_SIN: {//sin
                if (nOpSum < 1) return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0))) return null;
                double af = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                return Math.sin(af);
            }
            case ConstDefine.FUNC_COS: {//cos
                if (nOpSum < 1) return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0))) return null;
                double af = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                return Math.cos(af);
            }
            case ConstDefine.FUNC_TAN: {//tan
                if (nOpSum < 1) return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0))) return null;
                double af = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                return Math.tan(af);

            }
            case ConstDefine.FUNC_CTAN: {//ctan
                if (nOpSum < 1) return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0))) return null;
                double af = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                return Math.atan(af);
            }

            case ConstDefine.FUNC_FRAC: {//取小数
                if (nOpSum < 1) return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0))) return null;
                Double af = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                return af - af.intValue();
            }
            case ConstDefine.FUNC_EXP: {
                if (nOpSum < 1) return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0))) return null;
                double af = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                return Math.exp(af);
            }
            case ConstDefine.FUNC_SQRT: {
                if (nOpSum < 1) return null;
                if (!NumberBaseOpt.isNumber(slOperand.get(0))) return null;
                double af = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                return Math.sqrt(af);
            }

            case ConstDefine.FUNC_IF: {// 108
                if (nOpSum < 2) return null;
                if (BooleanBaseOpt.castObjectToBoolean(slOperand.get(0), false)) {
                    return slOperand.get(1);
                } else {
                    if (nOpSum > 2)
                        return slOperand.get(2);
                    else
                        return null;
                }
            }

            case ConstDefine.FUNC_CASE: {// 116
                if (nOpSum < 2) return null;
                String tempStr = StringBaseOpt.objectToString(slOperand.get(0));
                int MatchType = 0;
                if (tempStr.equalsIgnoreCase("true"))
                    MatchType = 1;
                else {
                    if (NumberBaseOpt.isNumber(slOperand.get(0))) {
                        dbtemp = NumberBaseOpt.castObjectToDouble(slOperand.get(0));
                        MatchType = 2;
                    }
                }
                int i = 1;
                for (; i + 1 < nOpSum; i += 2) {
                    if (MatchType == 1) {
                        if (BooleanBaseOpt.castObjectToBoolean(slOperand.get(i), false))
                            return slOperand.get(i + 1);
                    } else if (MatchType == 2) {
                        if (NumberBaseOpt.isNumber(slOperand.get(i))) {
                            if (Math.abs(dbtemp -
                                NumberBaseOpt.castObjectToDouble(slOperand.get(i))) < COMPARE_MIN_DOUBLE)
                                return slOperand.get(i + 1);
                        }
                    } else {
                        if (tempStr.equals(StringBaseOpt.objectToString(slOperand.get(i))))
                            return slOperand.get(i + 1);
                    }
                }
                if (nOpSum % 2 == 0)
                    return slOperand.get(nOpSum - 1);
                return null;
            }
            case ConstDefine.FUNC_CURRENT_DATE: {//
                return DatetimeOpt.truncateToDay(DatetimeOpt.currentUtilDate());
            }

            case ConstDefine.FUNC_CURRENT_DATETIME: { // 包括时间
                return DatetimeOpt.currentUtilDate();
            }

            case ConstDefine.FUNC_CURRENT_TIMESTAMP: { // 包括时间
                return DatetimeOpt.currentSqlTimeStamp();
            }

            case ConstDefine.FUNC_DAY: {//
                Date dt = (nOpSum > 0) ? DatetimeOpt.castObjectToDate(slOperand.get(0)) : null;
                if (dt == null)
                    dt = DatetimeOpt.currentUtilDate();
                return DatetimeOpt.getDay(dt);
            }
            case ConstDefine.FUNC_MONTH: {//
                Date dt = (nOpSum > 0) ? DatetimeOpt.castObjectToDate(slOperand.get(0)) : null;
                if (dt == null)
                    dt = DatetimeOpt.currentUtilDate();
                return DatetimeOpt.getMonth(dt);
            }
            case ConstDefine.FUNC_YEAR: {//
                Date dt = (nOpSum > 0) ? DatetimeOpt.castObjectToDate(slOperand.get(0)) : null;
                if (dt == null)
                    dt = DatetimeOpt.currentUtilDate();
                return DatetimeOpt.getYear(dt);
            }

            case ConstDefine.FUNC_WEEK: {//
                Date dt = (nOpSum > 0) ? DatetimeOpt.castObjectToDate(slOperand.get(0)) : null;
                if (dt == null)
                    dt = DatetimeOpt.currentUtilDate();

                return DatetimeOpt.getWeekOfYear(dt);
            }

            case ConstDefine.FUNC_WEEK_DAY: {//
                Date dt = (nOpSum > 0) ? DatetimeOpt.castObjectToDate(slOperand.get(0)) : null;
                if (dt == null)
                    dt = DatetimeOpt.currentUtilDate();
                return DatetimeOpt.getDayOfWeek(dt);
            }

            case ConstDefine.FUNC_FORMAT_DATE: {//
                if (nOpSum < 1) return null;
                String dateFormat = StringBaseOpt.castObjectToString(slOperand.get(0));
                Date dt = (nOpSum > 1) ? DatetimeOpt.castObjectToDate(slOperand.get(1)) : null;
                if (dt == null)
                    dt = DatetimeOpt.currentUtilDate();

                return DatetimeOpt.convertDateToString(dt, dateFormat);
            }

            case ConstDefine.FUNC_DATE_INFO: {//
                if (nOpSum < 1) return null;
                int field = NumberBaseOpt.castObjectToInteger(slOperand.get(0), 0);
                Date dt = (nOpSum > 1) ? DatetimeOpt.castObjectToDate(slOperand.get(1)) : null;
                if (dt == null)
                    dt = DatetimeOpt.currentUtilDate();
                Calendar cal = new GregorianCalendar();
                cal.setTime(dt);
                return cal.get(field);
            }

            case ConstDefine.FUNC_DAY_SPAN: {//
                if (nOpSum < 2) return null;
                Date dt = DatetimeOpt.castObjectToDate(slOperand.get(0));
                Date dt2 = DatetimeOpt.castObjectToDate(slOperand.get(1));
                if (dt == null || dt2 == null)
                    return null;
                return DatetimeOpt.calcSpanDays(dt, dt2);
            }
            case ConstDefine.FUNC_DATE_SPAN: {//
                if (nOpSum < 2) return null;
                Date dt = DatetimeOpt.castObjectToDate(slOperand.get(0));
                Date dt2 = DatetimeOpt.castObjectToDate(slOperand.get(1));
                if (dt == null || dt2 == null)
                    return null;
                return DatetimeOpt.calcDateSpan(dt, dt2);
            }
            case ConstDefine.FUNC_ADD_DATE: {//
                LeftRightPair<Date, Object> dateOpt = fetchDateOpt(nOpSum, slOperand);
                if (dateOpt.getLeft() == null || dateOpt.getRight() == null)
                    return null;
                return DatetimeOpt.addDays(dateOpt.getLeft(),
                    NumberBaseOpt.castObjectToFloat(dateOpt.getRight()));
            }
            case ConstDefine.FUNC_ADD_DAYS: {//
                LeftRightPair<Date, Object> dateOpt = fetchDateOpt(nOpSum, slOperand);
                if (dateOpt.getLeft() == null || dateOpt.getRight() == null)
                    return null;
                return DatetimeOpt.addDays(dateOpt.getLeft(),
                    NumberBaseOpt.castObjectToInteger(dateOpt.getRight()));
            }
            case ConstDefine.FUNC_ADD_MONTHS: {//
                LeftRightPair<Date, Object> dateOpt = fetchDateOpt(nOpSum, slOperand);
                if (dateOpt.getLeft() == null || dateOpt.getRight() == null)
                    return null;
                return DatetimeOpt.addMonths(dateOpt.getLeft(),
                    NumberBaseOpt.castObjectToInteger(dateOpt.getRight()));
            }
            case ConstDefine.FUNC_ADD_YEARS: {//
                LeftRightPair<Date, Object> dateOpt = fetchDateOpt(nOpSum, slOperand);
                if (dateOpt.getLeft() == null || dateOpt.getRight() == null)
                    return null;
                return DatetimeOpt.addYears(dateOpt.getLeft(),
                    NumberBaseOpt.castObjectToInteger(dateOpt.getRight()));
            }

            case ConstDefine.FUNC_TRUNC_DATE: {//
                Date dt = null;
                Object ti = null;
                if (nOpSum == 1) {
                    dt = DatetimeOpt.currentUtilDate();
                    ti = slOperand.get(0);
                } else if (nOpSum > 1) {
                    dt = DatetimeOpt.castObjectToDate(slOperand.get(0));
                    if (dt == null) {
                        dt = DatetimeOpt.currentUtilDate();
                    }
                    ti = slOperand.get(1);
                }
                String tempStr = StringBaseOpt.objectToString(ti);
                if ("M".equalsIgnoreCase(tempStr))
                    return DatetimeOpt.truncateToMonth(dt);
                else if ("Y".equalsIgnoreCase(tempStr))
                    return DatetimeOpt.truncateToYear(dt);
                else
                    return DatetimeOpt.truncateToDay(dt);
            }

            case ConstDefine.FUNC_LAST_OF_MONTH: {//
                Date dt = (nOpSum > 0) ? DatetimeOpt.castObjectToDate(slOperand.get(0)) : null;
                if (dt == null)
                    dt = DatetimeOpt.currentUtilDate();
                return DatetimeOpt.seekEndOfMonth(dt);
            }

            case ConstDefine.FUNC_TO_DATE: {//
                Object dt = (nOpSum > 0) ? slOperand.get(0) : null;
                if (dt == null) {
                    return DatetimeOpt.currentUtilDate();
                }
                if (nOpSum > 1) {
                    return DatetimeOpt.convertStringToDate(
                        StringBaseOpt.castObjectToString(slOperand.get(0)),
                        StringBaseOpt.castObjectToString(slOperand.get(1))
                    );
                } else {
                    return DatetimeOpt.castObjectToDate(slOperand.get(0));
                }
            }

            case ConstDefine.FUNC_TO_STRING: {//
                if (nOpSum < 1) {
                    return null;
                }
                String svalue = StringBaseOpt.castObjectToString(slOperand.get(0));
                if (StringUtils.isBlank(svalue) && nOpSum > 1) {
                    return slOperand.get(1);
                }
                return svalue;
            }

            case ConstDefine.FUNC_TO_NUMBER: {//
                if (nOpSum < 1) {
                    return null;
                }
                Number num = NumberBaseOpt.castObjectToNumber(slOperand.get(0));
                if (num == null && nOpSum > 1) {
                    return slOperand.get(1);
                }
                return num;
            }

            case ConstDefine.FUNC_GET_PY://
            {
                if (nOpSum < 1) return null;
                return StringBaseOpt.getFirstLetter(
                    StringBaseOpt.objectToString(slOperand.get(0)));
            }
            default:
                break;
        }
        return null;
    }
}
