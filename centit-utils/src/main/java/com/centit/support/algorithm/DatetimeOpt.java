package com.centit.support.algorithm;

import com.centit.support.common.LeftRightPair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 几乎每一个框架都有重写这个类，这个类中的方法比较多，可以归纳为一下几类：
 * 1. create* 这类方法式创建一个日期类，不同的方法有不同的参数
 * 2. current* 获取当前时间，不同的方法返回不同的类型，可能是 utils.Date、sql.Data、timeStamp等等
 * 3. convert* 在土工日期类型、日期和时间 等等之间进行转换
 * 4. equal* 判断两个日期在不同时间精度上是否相等，比如是否是同一月、同一天、同一分钟等等。
 * 5. calc* 一组关于日期的计算函数，比如：计算两个日期之间的时机差calcSpanDays，
 * 计算两个日期之间的工作日 calcWeekDays， 计算两个日期之间的周末天数 calcWeekendDays，
 * 计算一周的第一天和最后一天等等。
 * 6. add* 给日期做加减计算，参数为负数就是减。
 * 7. get* 获得日期的属性，比如星期几、是一年的第几天、当前时分秒等等。
 * 8. seek* 移动到这个月的最后一天、这年的最后一天等等。
 * 9. truncate* 截取日期到天、周、月等等，和seek*操作相对。
 * 10. smartPraseDate 和 castObjectToDate 前者将字符串转换为日期，后者将object转换为日期
 *
 * @author codefan
 */
@SuppressWarnings("unused")
public abstract class DatetimeOpt {

    private static Logger log = LoggerFactory.getLogger(DatetimeOpt.class);
    private static String defaultDatePattern = "yyyy-MM-dd";
    private static String timePattern = "HH:mm";
    public final static String timeWithSecondPattern = "HH:mm:ss";
    //private static String datetimeWithoutYearPattern = "MM-dd HH:mm";
    //private static String datetimePattern = "yyyy-MM-dd HH:mm";
    public final static String datetimePattern = "yyyy-MM-dd HH:mm:ss";
    public final static String timestampPattern = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static String gmtDatePattern = "zone:en:GMT yyyy-MM-dd HH:mm:ss.SSS zzz";
    private DatetimeOpt() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 获得当前日期的字符串 ，格式为 "yyyy-MM-dd" 示例 2015-08-24
     *
     * @return 当前时间`
     */
    public static String currentDate() { // 取系统当前日期
        return DatetimeOpt.convertDateToString(
            new Date(System.currentTimeMillis()), defaultDatePattern);
    }

    public static String currentTime() { // 取系统当前时间
        return DatetimeOpt.convertDateToString(
            new Date(System.currentTimeMillis()), timePattern);
    }

    public static String currentTimeWithSecond() { // 取系统当前时间
        return DatetimeOpt.convertDateToString(
            new Date(System.currentTimeMillis()), timeWithSecondPattern);
    }

    /**
     * 获得当前日期的字符串 ，格式为 "yyyy-MM-dd HH:mm:ss"
     *
     * @return 当前时间
     */
    public static String currentDatetime() { // 取系统当前日期
        return DatetimeOpt.convertDateToString(
            new Date(System.currentTimeMillis()), datetimePattern);
    }

    public static java.util.Date createUtilDate(int year, int month, int date,
                                                int hourOfDay, int minute, int second, int milliSecond) {
        Calendar cal = new GregorianCalendar();
        cal.set(year, month - 1, date,
            hourOfDay, minute, second);
        cal.set(Calendar.MILLISECOND, milliSecond);
        return cal.getTime();
    }

    /**
     * 根据 年、月、日、时、分、秒  创建一个日期 类型为 java.util.Date
     *
     * @param year      年
     * @param month     月
     * @param date      日
     * @param hourOfDay 时 （24小时制）
     * @param minute    分
     * @param second    秒
     * @return 时间
     */
    public static java.util.Date createUtilDate(int year, int month, int date,
                                                int hourOfDay, int minute, int second) {
        return createUtilDate(year, month, date,
            hourOfDay, minute, second, 0);
    }


    /**
     * 根据 年、月、日、时、分  创建一个日期 类型为 java.util.Date
     *
     * @param year      年
     * @param month     月
     * @param date      日
     * @param hourOfDay 时 （24小时制）
     * @param minute    分
     * @return 时间
     */
    public static java.util.Date createUtilDate(int year, int month, int date,
                                                int hourOfDay, int minute) {
        return createUtilDate(year, month, date,
            hourOfDay, minute, 0, 0);
    }

    /**
     * 根据 年、月、日  创建一个日期 类型为 java.util.Date
     *
     * @param year  年
     * @param month 月
     * @param date  日
     * @return 时间
     */
    public static java.util.Date createUtilDate(int year, int month, int date) {
        return createUtilDate(year, month, date, 0, 0, 0, 0);
    }

    /**
     * 日期类型转换 从 java.sql.Date 转换为 java.util.Date 这个会截断时间
     *
     * @param date 时间
     * @return 时间
     */
    public static java.util.Date convertToUtilDate(java.sql.Date date) {
        return date;
    }

    /**
     * 日期类型转换 从 java.util.Date 转换为 java.sql.Date 这个会截断时间
     *
     * @param date 时间
     * @return 时间
     */
    public static java.sql.Date convertToSqlDate(java.util.Date date) {
        if (date == null)
            return null;
        if (date instanceof java.sql.Date)
            return (java.sql.Date) date;

        return new java.sql.Date(date.getTime());
    }

    public static java.sql.Date castObjectToSqlDate(Object date) {
        if (date == null)
            return null;
        if (date instanceof java.sql.Date)
            return (java.sql.Date) date;
        if (date instanceof java.util.Date)
            return new java.sql.Date(((java.util.Date) date).getTime());
        java.util.Date dt = DatetimeOpt.castObjectToDate(date);
        if (dt == null)
            return null;
        return new java.sql.Date(dt.getTime());
    }

    /**
     * 日期类型转换 从 java.util.Date 转换为 java.sql.Timestamp
     *
     * @param date 时间
     * @return 时间
     */
    public static java.sql.Timestamp convertToSqlTimestamp(java.util.Date date) {
        if (date == null)
            return null;
        if (date instanceof java.sql.Timestamp)
            return (java.sql.Timestamp) date;
        return new java.sql.Timestamp(date.getTime());
    }

    public static java.sql.Timestamp castObjectToSqlTimestamp(Object date) {
        if (date == null)
            return null;
        if (date instanceof java.sql.Timestamp)
            return (java.sql.Timestamp) date;
        if (date instanceof java.util.Date)
            return new java.sql.Timestamp(((java.util.Date) date).getTime());
        java.util.Date dt = DatetimeOpt.castObjectToDate(date);
        if (dt == null)
            return null;
        return new java.sql.Timestamp(dt.getTime());
    }

    /*
     * 取系统当前日期和时间 ，返回 类型 java.sql.Date
     */
    public static java.sql.Date currentSqlDate() {
        return new java.sql.Date(System.currentTimeMillis());
    }

    /*
     * 取系统当前日期和时间，返回类型 java.util.Date
     */
    public static java.util.Date currentUtilDate() {
        return new java.util.Date(System.currentTimeMillis());
    }

    public static Timestamp currentSqlTimeStamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /*
     * 取系统当前日期和时间，返回类型 java.util.Calendar
     */
    public static Calendar currentCalendarDate() {
        java.util.Date today = currentUtilDate();
        Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        return cal;
    }

    /**
     * 返回常量字符串  "yyyy-MM-dd"
     *
     * @return a string representing the date pattern on the UI
     */
    public static String getDatePattern() {
        return defaultDatePattern;
    }

    /**
     * @return 返回常量字符串  "yyyy-MM-dd HH:mm:ss"
     */
    public static String getDateTimePattern() {
        return datetimePattern;
    }

    public static Locale fetchLangLocal(String lang) {
        Locale[] locales = Locale.getAvailableLocales();
        for(Locale locale : locales) {
            if (StringUtils.equals(locale.getCountry(), lang)) {
                return locale;
            }
        }
        for(Locale locale : locales) {
            if (StringUtils.equals(locale.getLanguage(), lang)) {
                return locale;
            }
        }
        return Locale.SIMPLIFIED_CHINESE;
    }

    /**
     * 时区转换
     * @param zone 长度为3的时区
     * @return TimeZone Zone offect
     */
    public static TimeZone fetchTimeZone(String zone) {
        if(zone.length()==3) {
            switch (zone) {
                //-------------------------------------------------------------
                case "-11": return TimeZone.getTimeZone("Pacific/Midway");
                case "-10": return TimeZone.getTimeZone("Pacific/Honolulu");
                case "-09": return TimeZone.getTimeZone("America/Anchorage");
                case "-08": return TimeZone.getTimeZone("America/Los_Angeles");
                case "-07": return TimeZone.getTimeZone("America/Denver");
                case "-06": return TimeZone.getTimeZone("America/Chicago");
                case "-05": return TimeZone.getTimeZone("America/New_York");
                case "-04": return TimeZone.getTimeZone("America/Barbados");
                case "-03": return TimeZone.getTimeZone("America/Montevideo");
                case "-02": return TimeZone.getTimeZone("Atlantic/South_Georgia");
                case "-01": return TimeZone.getTimeZone("Atlantic/Azores");
                case "+00": return TimeZone.getTimeZone("Europe/London");
                case "+01": return TimeZone.getTimeZone("Europe/Amsterdam");
                case "+02": return TimeZone.getTimeZone("Europe/Athens");
                case "+03": return TimeZone.getTimeZone("Europe/Moscow");
                case "+04": return TimeZone.getTimeZone("Asia/Yerevan");
                case "+05": return TimeZone.getTimeZone("Asia/Karachi");
                case "+06": return TimeZone.getTimeZone("Asia/Rangoon");
                case "+07": return TimeZone.getTimeZone("Asia/Bangkok");
                case "CST": // 28800000   |   中国标准时间   |   中国标准时间
                            // ZoneInfoFile 中的 CST 为 "CST": "America/Chicago",
                case "+08": return TimeZone.getTimeZone("Asia/Shanghai");
                case "+09": return TimeZone.getTimeZone("Asia/Tokyo");
                case "+10": return TimeZone.getTimeZone("Australia/Sydney");//**
                case "+11": return TimeZone.getTimeZone("Asia/Magadan");
                case "+12": return TimeZone.getTimeZone("Pacific/Auckland"); //**
                default: break;
            }
        }
        return TimeZone.getTimeZone(zone);
    }

    private static LeftRightPair<SimpleDateFormat, String> createDateFormat(String sMask, String strDate){
        SimpleDateFormat df;
        if(sMask.startsWith("lang")){
            Locale local = fetchLangLocal(sMask.substring(5,7));
            df = new SimpleDateFormat(sMask.substring(7).trim(), local);
        } else if(sMask.startsWith("zone")){
            Locale local = fetchLangLocal(sMask.substring(5,7));
            String zone = sMask.substring(8,11);
            df = new SimpleDateFormat(sMask.substring(11).trim(), local);
            if("END".equals(zone) || StringUtils.isNotBlank(strDate)){
                int lastInd = strDate.lastIndexOf(' ');
                zone = strDate.substring(lastInd+1);
                strDate = strDate.substring(0, lastInd);
            }
            df.setTimeZone(DatetimeOpt.fetchTimeZone(zone));
        }  else {
            df = new SimpleDateFormat(sMask);
        }
        return new LeftRightPair<>(df, strDate);
    }
    /**
     * This method generates a string representation of a date/time in the
     * format you specify on input
     *
     * @param sMask   the date pattern the string is in
     * @param strDate a string representation of a date
     * @return a converted Date object
     * @see java.text.SimpleDateFormat 的说明
     */
    public static Date convertStringToDate(String strDate, String sMask) {
        if (StringUtils.isBlank(strDate))
            return null;

        if (StringUtils.isBlank(sMask))
            return smartPraseDate(strDate);

        try {
            LeftRightPair<SimpleDateFormat, String> dfAndStr = createDateFormat(sMask , strDate);
            return dfAndStr.getLeft().parse(dfAndStr.getRight());

        } catch (ParseException pe) {
            log.error("converting '" + strDate + "' to date with mask '"
                + sMask + "'" + pe.getMessage());
            return null;
            //throw new ParseException(pe.getMessage(), pe.getErrorOffset());
        }
    }

    private static Date convertStringToDate(String strDate, String sMask, String timeZone) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(sMask);
            if(StringUtils.isNotBlank(timeZone))
                df.setTimeZone(DatetimeOpt.fetchTimeZone(timeZone));
            return df.parse(strDate);
        } catch (ParseException pe) {
            log.error("converting '" + strDate + "' to date with mask '"
                + sMask + "'" + pe.getMessage());
            return null;
        }
    }
    /**
     * This method generates a string representation of a date's date/time in
     * the format you specify on input
     *
     * @param aMask the date pattern the string is in
     * @param aDate a date object
     * @return a formatted string representation of the date
     * @see java.text.SimpleDateFormat 的说明
     */
    public static String convertDateToString(Date aDate, String aMask) {
        if(aDate==null){
            return null;
        }
        String sMask = (aMask == null || "".equals(aMask)) ? "yyyy-MM-dd" : aMask;
        LeftRightPair<SimpleDateFormat, String> dfAndStr = createDateFormat(sMask , null);
        return dfAndStr.getLeft().format(aDate);
    }

    /**
     * This method generates a string representation of a date based on the
     * System Property 'dateFormat' in the format you specify on input
     *
     * @param aDate A date to convert
     * @return a string representation of the date
     */
    public static String convertTimeToString(Date aDate) {
        return convertDateToString(aDate, timePattern);
    }

    /*
     * 返回时间 字符串
     */
    public static String convertTimeWithSecondToString(Date aDate) {
        return convertDateToString(aDate, timeWithSecondPattern);
    }

    /**
     * 返回日期字符串  "yyyy-MM-dd"
     *
     * @param aDate 时间
     * @return 字符串
     */
    public static String convertDateToString(Date aDate) {
        return convertDateToString(aDate, defaultDatePattern);
    }

    public static String convertDateToSmartString(Date aDate) {
        return convertDateToSmartString(aDate, false);
    }

    public static String convertDateToSmartString(Date aDate, boolean withSecond) {
        if(aDate==null){
            return null;
        }
        Date currentDay = currentUtilDate();
        long today = currentDay.getTime() / 86400000L;
        long compareDay = aDate.getTime() / 86400000L;
        if(today == compareDay){
            return "今天 " + convertDateToString(aDate,
                withSecond? timeWithSecondPattern : timePattern);
        }

        if(today == compareDay+1){
            return "昨天 " + convertDateToString(aDate,
                withSecond? timeWithSecondPattern : timePattern);
        }

        if(today == compareDay+2){
            return "前天 " + convertDateToString(aDate,
                withSecond? timeWithSecondPattern : timePattern);
        }

        if(today +1 == compareDay){
            return "明天 " + convertDateToString(aDate,
                withSecond? timeWithSecondPattern : timePattern);
        }

        if(today +2 == compareDay){
            return "后天 " + convertDateToString(aDate,
                withSecond ? timeWithSecondPattern : timePattern);
        }

        if( getYear(aDate) == getYear(currentDay)){
            return convertDateToString(aDate,
                withSecond ? "MM-dd HH:mm:ss" : "MM-dd HH:mm");
        } else {
            return convertDateToString(aDate,
                withSecond ? datetimePattern : "yyyy-MM-dd HH:mm");
        }
    }
    /**
     * 返回日期字符串  toUTCString( ) 和 javaScript 一致
     * // d MMM yyyy HH:mm:ss 'GMT'
     *
     * @param aDate 时间
     * @return 字符串
     */
    public static String convertDateToGMTString(Date aDate) {
        return convertDateToString(aDate, gmtDatePattern);
    }

    /**
     * 返回日期字符串  "yyyy-MM-dd HH:mm:ss"
     *
     * @param aDate 时间
     * @return 字符串
     */
    public static String convertDatetimeToString(Date aDate) {
        return convertDateToString(aDate, datetimePattern);
    }

    public static String convertTimestampToString(Date aDate) {
        return convertDateToString(aDate, timestampPattern);
    }

    /**
     * 获得当前时间字符串，格式为   "yyyy-MM-dd HH:mm:ss"
     *
     * @return string
     */
    public static String getNowDateTime4String() {
        return convertDateToString(currentUtilDate(), getDateTimePattern());
    }

    /**
     * This method converts a String to a date using the datePattern
     *
     * @param strDate the date to convert (in format yyyy-mm-dd)
     * @return a date object
     */
    public static Date convertStringToDate(String strDate){
        return convertStringToDate(strDate, getDatePattern());
    }

    /**
     * 获得 年月日 对应的日期 是星期几  星期日 到星期六 为 0-6
     *
     * @param y 年
     * @param m 月
     * @param d 日
     * @return 星期几
     */
    public static int getDayOfWeek(int y, int m, int d) {
        int y0 = y - (14 - m) / 12;
        int x = y0 + y0 / 4 - y0 / 100 + y0 / 400;
        int m0 = m + 12 * ((14 - m) / 12) - 2;
        return (d + x + (31 * m0) / 12) % 7;
    }

    /**
     * 获得 指定日期 是星期几  星期日 到星期六 为 0-6
     *
     * @param date 日期
     * @return 星期几
     */
    public static int getDayOfWeek(java.util.Date date) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * 获得 指定日期 是星期几  星期日 到星期六 为 "星期日","星期一","星期二","星期三","星期四","星期五","星期六"
     *
     * @param date 日期
     * @return 星期几
     */
    public static String getDayOfWeekCN(java.util.Date date) {
        String[] weeklist = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "",};
        return weeklist[getDayOfWeek(date)];
    }

    public static int getSecond(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.SECOND);
    }

    public static int getMilliSecond(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.MILLISECOND);
    }

    public static int getMinute(java.util.Date date) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.MINUTE);
    }

    public static int getHour(java.util.Date date) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }


    public static int getWeekOfYear(java.util.Date date) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    public static int getWeekOfMonth(java.util.Date date) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.WEEK_OF_MONTH);
    }

    public static int getDay(Date date) {

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }


    public static int getDayOfYear(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    public static int getMonth(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getYear(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    private static void resetToZeroPoint(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public static java.util.Date truncateToDay(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        resetToZeroPoint(cal);
        return cal.getTime();
        //createUtilDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));
    }

    public static java.util.Date truncateToMonth(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DATE, 1);
        resetToZeroPoint(cal);
        return cal.getTime();
        //return createUtilDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,1);
    }

    public static java.util.Date truncateToYear(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DATE, 1);
        resetToZeroPoint(cal);
        return cal.getTime();
        //return createUtilDate(cal.get(Calendar.YEAR),1,1);
    }

    //跳转到年的最后一天
    public static java.util.Date seekEndOfYear(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DATE, 31);
        resetToZeroPoint(cal);
        return cal.getTime();
        //return createUtilDate(cal.get(Calendar.YEAR),12,31);
    }

    /**
     * 获取一周第一天（周一凌晨一点）
     *
     * @param date 输入时间
     * @return 周一凌晨一点
     */
    public static java.util.Date truncateToWeek(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        resetToZeroPoint(cal);
        int weekDay = cal.get(Calendar.DAY_OF_WEEK);
        if (weekDay > 2) {
            cal.setTimeInMillis(cal.getTimeInMillis() -
                Double.valueOf((weekDay - 2) * 86400000.0).longValue());
        } else if (weekDay == 1) {
            cal.setTimeInMillis(cal.getTimeInMillis() -
                Double.valueOf(6 * 86400000.0).longValue());
        }
        return cal.getTime();
    }

    /**
     * 获取一周最后一天（周日凌晨一点）
     *
     * @param date 输入时间
     * @return 周日凌晨一点
     */
    public static java.util.Date seekEndOfWeek(java.util.Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        resetToZeroPoint(cal);
        int weekDay = cal.get(Calendar.DAY_OF_WEEK);
        if (weekDay > 1) {
            cal.setTimeInMillis(cal.getTimeInMillis() +
                Double.valueOf((8 - weekDay) * 86400000.0).longValue());
        }
        return cal.getTime();
        //return createUtilDate(cal.get(Calendar.YEAR),12,31);
    }

    //跳转到月的最后一天
    public static java.util.Date seekEndOfMonth(java.util.Date date) {
        return addDays(truncateToMonth(addMonths(date, 1)), -1);
    }

    public static java.util.Date addSeconds(java.util.Date date, int nSeconds) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.SECOND, nSeconds);
        return cal.getTime();
    }

    public static java.util.Date addMinutes(java.util.Date date, int nMinutes) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, nMinutes);
        return cal.getTime();
    }

    public static java.util.Date addHours(java.util.Date date, int nHours) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.HOUR, nHours);
        return cal.getTime();
    }

    public static java.util.Date addDays(java.util.Date date, int nDays) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, nDays);
        return cal.getTime();
    }

    public static java.util.Date addDays(java.util.Date date, float nDays) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(date.getTime() +
            Double.valueOf(nDays * 86400000.0).longValue());
        return cal.getTime();
    }

    public static java.util.Date addMonths(java.util.Date date, int nMonths) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.MONTH, nMonths);
        return cal.getTime();
    }

    public static java.util.Date addYears(java.util.Date date, int nYears) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.YEAR, nYears);
        return cal.getTime();
    }

    /**
     * @param beginDate beginTime
     * @param endDate   endTime
     * @return 计算这个周期中的天数, 包括 beginTime，endTime
     */
    public static int calcSpanDays(java.util.Date beginDate, java.util.Date endDate) {
        //System.out.println(beginDate.getTime());
        //System.out.println(endDate.getTime());
        java.util.Date bD = (beginDate.getTime() > endDate.getTime()) ? truncateToDay(endDate) : truncateToDay(beginDate);
        java.util.Date eD = (beginDate.getTime() > endDate.getTime()) ? beginDate : endDate;
        return (int) ((eD.getTime() - bD.getTime()) / 86400000 + 1);
    }

    /**
     * @param beginDate beginTime
     * @param endDate   endTime
     * @return 计算这个周期中的天数, 不足一天用小数表示
     */
    public static float calcDateSpan(java.util.Date beginDate, java.util.Date endDate) {
        //System.out.println(beginDate.getTime());
        //System.out.println(endDate.getTime());
        java.util.Date bD = (beginDate.getTime() > endDate.getTime()) ? truncateToDay(endDate) : truncateToDay(beginDate);
        java.util.Date eD = (beginDate.getTime() > endDate.getTime()) ? beginDate : endDate;
        return Double.valueOf((eD.getTime() - bD.getTime()) / 86400000.0).floatValue();
    }

    /*
     * 计算周的第一天始日期
     */
    public static java.util.Date calcWeek1stDay(int nYear, int nWeekNo) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.YEAR, nYear);
        cal.set(Calendar.WEEK_OF_YEAR, nWeekNo);
        return cal.getTime();
    }

    /*
     * 计算周的最后一天日期
     */
    public static java.util.Date calcWeekLastDay(int nYear, int nWeekNo) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.YEAR, nYear);
        cal.set(Calendar.WEEK_OF_YEAR, nWeekNo);
        return cal.getTime();
    }

    /**
     * 计算这个周期中的周六和周日的天数，周末的天数
     *
     * @param beginDate beginTime
     * @param endDate   endTime
     * @return 计算这个周期中的周六和周日的天数, 包括 beginTime，endTime
     */
    public static int calcWeekendDays(java.util.Date beginDate, java.util.Date endDate) {
        int nWeekDay = getDayOfWeek(beginDate);
        int m = calcSpanDays(beginDate, endDate);
        int weekEnds = (m + nWeekDay) / 7;
        int nWeekDay2 = (m + nWeekDay) % 7;
        int days = weekEnds * 2 - (nWeekDay == 0 ? 0 : 1) + (nWeekDay2 > 0 ? 1 : 0);
        return days;
    }

    /**
     * 计算这个周期中 工作日的天数,不包括 周六和周日，但是因为不知道其他的假期，所以只是去掉周末
     *
     * @param beginDate beginTime
     * @param endDate   endTime
     * @param weekDay   0~6 "星期日","星期一","星期二","星期三","星期四","星期五","星期六"
     * @return 计算这个周期中 某个工作日的天数, 包括 beginTime，endTime
     */
    public static int calcWeekDays(java.util.Date beginDate, java.util.Date endDate, int weekDay) {
        int nWeekDay = getDayOfWeek(beginDate);
        int m = calcSpanDays(beginDate, endDate);
        return m / 7 + (((weekDay >= nWeekDay && nWeekDay + m % 7 > weekDay) || (weekDay + 7 >= nWeekDay && nWeekDay + m % 7 > weekDay + 7)) ? 1 : 0);
    }

    /*
     * 判断两个时间是否相等，精确到秒
     */
    public static boolean equalOnSecond(java.util.Date oneDate, java.util.Date otherDate) {
        if (oneDate == null || otherDate == null)
            return false;
        return oneDate.getTime() / 1000 == otherDate.getTime() / 1000;
    }

    /*
     * 判断两个时间是否相等，精确到分
     */
    public static boolean equalOnMinute(java.util.Date oneDate, java.util.Date otherDate) {
        if (oneDate == null || otherDate == null)
            return false;
        return oneDate.getTime() / 60000 == otherDate.getTime() / 60000;
    }

    /*
     * 判断两个时间是否相等，精确到时
     */
    public static boolean equalOnHour(java.util.Date oneDate, java.util.Date otherDate) {
        if (oneDate == null || otherDate == null)
            return false;
        return oneDate.getTime() / 3600000 == otherDate.getTime() / 3600000;
    }

    /*
     * 判断两个时间是否相等，精确到天
     */
    public static boolean equalOnDay(java.util.Date oneDate, java.util.Date otherDate) {
        if (oneDate == null || otherDate == null)
            return false;
        return oneDate.getTime() / 86400000 == otherDate.getTime() / 86400000;
    }

    /**
     * 比较两个日期大小 ,避免 发生 NullPointerException 异常
     * Compares two Dates for ordering.
     *
     * @param oneDate   the <code>Date</code> to be compared.
     * @param otherDate the <code>Date</code> to be compared.
     * @return the value <code>0</code> if the argument otherDate is equal to
     * oneDate ; a value less than <code>0</code> if this Date
     * is before the Date argument; and a value greater than
     * if oneDate is after the otherDate.
     */
    public static int compareTwoDate(java.util.Date oneDate, java.util.Date otherDate) {
        if (oneDate == null && otherDate == null)
            return 0;
        if (oneDate == null)
            return -1;
        if (otherDate == null)
            return 1;
        return oneDate.compareTo(otherDate);
    }

    /*
     * 将一个字符串转换为日期
     */
    public static java.util.Date smartPraseDate(String sDate) {
        if (sDate == null || "".equals(sDate))
            return null;
        if(sDate.length()>8 && sDate.length()<14 && Pattern.matches("\\d+", sDate)) {
            return new java.util.Date(Long.parseLong(sDate));
        }

        String sTD = StringRegularOpt.trimDateString(sDate);
        String timeZone = null;
        int sl = sDate.length();
        int tzb = sDate.indexOf("GMT");
        if(tzb>0){
            timeZone = sDate.substring(tzb);
        } else {
            tzb = sl - 1;
            while (tzb > 0) {
                char c = sDate.charAt(tzb);
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '/')
                    tzb--;
                else
                    break;
            }
            if (sl - tzb > 3) {
                timeZone = sDate.substring(tzb + 1);
            }
        }

        sl = sTD.length();
        switch (sl) {
            case 5:
                return convertStringToDate(sTD, "yy-MM", timeZone);
            case 7:
                return convertStringToDate(sTD, "yyyy-MM", timeZone);
            case 8:
                return convertStringToDate(sTD, "yy-MM-dd", timeZone);
            case 10:
                if(sTD.indexOf('-')==2)
                    return convertStringToDate(sTD, "MM-dd-yyyy", timeZone);
                else
                    return convertStringToDate(sTD, "yyyy-MM-dd", timeZone);
            case 11:
                return convertStringToDate(sTD, "yy-MM-dd HH", timeZone);
            case 13:
                return convertStringToDate(sTD, "yyyy-MM-dd HH", timeZone);
            case 14:
                return convertStringToDate(sTD, "yy-MM-dd HH:mm", timeZone);
            case 16:
                return convertStringToDate(sTD, "yyyy-MM-dd HH:mm", timeZone);
            case 17:
                return convertStringToDate(sTD, "yy-MM-dd HH:mm:ss", timeZone);
            case 19:
                return convertStringToDate(sTD, "yyyy-MM-dd HH:mm:ss", timeZone);
            case 20:
            case 21:
            case 22:
            case 23:
                java.util.Date date = convertStringToDate(sTD, "yyyy-MM-dd HH:mm:ss.SSS", timeZone);
                //加上时区
                if(date !=null && sDate.charAt(10)=='T'){
                    date.setTime(date.getTime() + TimeZone.getDefault().getRawOffset() );
                }
                return date;
            default:
                return null;
        }
    }

    /*
     * 将一个Object转换为 Date
     */
    public final static java.util.Date castObjectToDate(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof java.util.Date)
            return (java.util.Date) obj;

        //if (obj instanceof Long)
        //    return new java.util.Date((Long) obj);

        if (obj instanceof Number)
            return new java.util.Date(((Number) obj).longValue());

        if (obj instanceof LocalDateTime){
            LocalDateTime ldt = (LocalDateTime)obj;
            return java.util.Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        }

        if (obj instanceof LocalDate){
            LocalDate ldt = (LocalDate)obj;
            return Date.from(ldt.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }

        String str = StringBaseOpt.objectToString(obj);
        if (StringUtils.isBlank(str)) {
            return null;
        } else {
            return DatetimeOpt.smartPraseDate(str);
        }
    }
}
