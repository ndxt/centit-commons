package com.centit.support.algorithm;

import com.centit.support.common.DateTimeSpan;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
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

    private static final Logger log = LoggerFactory.getLogger(DatetimeOpt.class);
    public final static String defaultDatePattern = "yyyy-MM-dd";
    public final static String timePattern = "HH:mm";
    public final static String timeWithSecondPattern = "HH:mm:ss";
    public final static String datetimePattern = "yyyy-MM-dd HH:mm:ss";
    public final static String timestampPattern = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static String gmtDatePattern = "zone:en:GMT yyyy-MM-dd HH:mm:ss";
    private DatetimeOpt() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 获得当前日期的字符串 ，格式为 "yyyy-MM-dd" 示例 2015-08-24
     *
     * @return 当前时间`
     */
    public static String currentDate() { // 取系统当前日期
        return DatetimeOpt.convertDateToString(currentUtilDate(), defaultDatePattern);
    }

    public static String currentTime() { // 取系统当前时间
        return DatetimeOpt.convertDateToString(currentUtilDate(), timePattern);
    }

    public static String currentTimeWithSecond() { // 取系统当前时间
        return DatetimeOpt.convertDateToString(currentUtilDate(), timeWithSecondPattern);
    }

    /**
     * 获得当前日期的字符串 ，格式为 "yyyy-MM-dd HH:mm:ss"
     *
     * @return 当前时间
     */
    public static String currentDatetime() { // 取系统当前日期
        return DatetimeOpt.convertDateToString(currentUtilDate(), datetimePattern);
    }

    public static java.util.Date createUtilDate(int year, int month, int date,
                                                int hourOfDay, int minute, int second, int milliSecond) {
        return Date.from(LocalDateTime.of(year, month, date, hourOfDay, minute, second, milliSecond * 1_000_000)
            .atZone(ZoneId.systemDefault()).toInstant());
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
        if (date instanceof java.sql.Date sqlDate)
            return sqlDate;

        return new java.sql.Date(date.getTime());
    }

    public static java.sql.Date castObjectToSqlDate(Object date) {
        if (date == null)
            return null;
        if (date instanceof java.sql.Date sqlDate)
            return sqlDate;
        if (date instanceof java.util.Date utilDate)
            return new java.sql.Date(utilDate.getTime());
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
        if (date instanceof java.sql.Timestamp ts)
            return ts;
        return new java.sql.Timestamp(date.getTime());
    }

    public static java.sql.Timestamp castObjectToSqlTimestamp(Object date) {
        if (date == null)
            return null;
        if (date instanceof java.sql.Timestamp ts)
            return ts;
        if (date instanceof java.util.Date utilDate)
            return new java.sql.Timestamp(utilDate.getTime());
        java.util.Date dt = DatetimeOpt.castObjectToDate(date);
        if (dt == null)
            return null;
        return new java.sql.Timestamp(dt.getTime());
    }

    /*
     * 取系统当前日期和时间 ，返回 类型 java.sql.Date
     */
    public static java.sql.Date currentSqlDate() {
        return java.sql.Date.valueOf(LocalDate.now());
    }

    /*
     * 取系统当前日期和时间，返回类型 java.util.Date
     */
    public static java.util.Date currentUtilDate() {
        return Date.from(Instant.now());
    }

    public static Timestamp currentSqlTimeStamp() {
        return Timestamp.from(Instant.now());
    }

    /*
     * 取系统当前日期和时间，返回类型 java.util.Calendar
     */
    public static Calendar currentCalendarDate() {
        return Calendar.getInstance();
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
        return Arrays.stream(Locale.getAvailableLocales())
            .filter(l -> lang.equals(l.getCountry()))
            .findFirst()
            .orElseGet(() -> Arrays.stream(Locale.getAvailableLocales())
                .filter(l -> lang.equals(l.getLanguage()))
                .findFirst()
                .orElse(Locale.SIMPLIFIED_CHINESE));
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
            SimpleDateFormat df;
            if(sMask.startsWith("lang")){
                Locale local = fetchLangLocal(sMask.substring(5,7));
                df = new SimpleDateFormat(sMask.substring(7).trim(), local);
            } else if(sMask.startsWith("zone")){
                //格式示例zone:en+07yyyy-MM-dd HH:mm:ss
                Locale local = fetchLangLocal(sMask.substring(5,7));
                String zone = sMask.substring(7,10);
                df = new SimpleDateFormat(sMask.substring(10).trim(), local);
                // 只有当 zone 为 "END" 时，才从日期字符串末尾提取时区
                if("END".equals(zone)){
                    int lastInd = strDate.lastIndexOf(' ');
                    if(lastInd > 0){
                        zone = strDate.substring(lastInd+1);
                        strDate = strDate.substring(0, lastInd);
                    }
                }
                df.setTimeZone(DatetimeOpt.fetchTimeZone(zone));
            }  else {
                df = new SimpleDateFormat(sMask);
            }
            return df.parse(strDate);

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
        String sMask = (aMask == null || aMask.isEmpty()) ? "yyyy-MM-dd" : aMask;
        boolean isGmt = false;
        SimpleDateFormat df;
        if(sMask.startsWith("lang")){
            Locale local = fetchLangLocal(sMask.substring(5,7));
            df = new SimpleDateFormat(sMask.substring(7).trim(), local);
        } else if(sMask.startsWith("zone")){
            Locale local = fetchLangLocal(sMask.substring(5,7));
            String zone = sMask.substring(8,11);
            if("GMT".equals(zone)){
                if(sMask.contains("zzz")){
                    sMask = sMask.replace("zzz", "").trim();
                }
                df = new SimpleDateFormat(sMask.substring(11).trim(), local);
                isGmt = true;
            } else {
                df = new SimpleDateFormat(sMask.substring(11).trim(), local);
                df.setTimeZone(DatetimeOpt.fetchTimeZone(zone));
            }
        }  else {
            df = new SimpleDateFormat(sMask);
        }
        String sDate = df.format(aDate);
        if(isGmt){
            int offset = TimeZone.getDefault().getRawOffset() / 3600000;
            sDate = sDate +" GMT" + (offset>=0? "+"+offset : offset);
        }
        return sDate;
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
        return LocalDate.of(y, m, d).getDayOfWeek().getValue() % 7;
    }

    /**
     * 获得 指定日期 是星期几  星期日 到星期六 为 0-6
     *
     * @param date 日期
     * @return 星期几
     */
    public static int getDayOfWeek(java.util.Date date) {
        return toLocalDateTime(date).getDayOfWeek().getValue() % 7;
    }

    /**
     * 获得 指定日期 是星期几  星期日 到星期六 为 "星期日","星期一","星期二","星期三","星期四","星期五","星期六"
     *
     * @param date 日期
     * @return 星期几
     */
    public static String getDayOfWeekCN(java.util.Date date) {
        String[] weekList = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        return weekList[getDayOfWeek(date)];
    }

    public static int getSecond(java.util.Date date) {
        return toLocalDateTime(date).getSecond();
    }

    public static int getMilliSecond(java.util.Date date) {
        return (int)(date.getTime() % 1000);
    }

    public static int getMinute(java.util.Date date) {
        return toLocalDateTime(date).getMinute();
    }

    public static int getHour(java.util.Date date) {
        return toLocalDateTime(date).getHour();
    }

    public static int getWeekOfYear(java.util.Date date) {
        return toLocalDateTime(date).get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public static int getWeekOfMonth(java.util.Date date) {
        return toLocalDateTime(date).get(WeekFields.ISO.weekOfMonth());
    }

    public static int getDay(Date date) {
        return toLocalDateTime(date).getDayOfMonth();
    }

    public static int getDayOfYear(java.util.Date date) {
        return toLocalDateTime(date).getDayOfYear();
    }

    public static int getMonth(java.util.Date date) {
        return toLocalDateTime(date).getMonthValue();
    }

    public static int getYear(java.util.Date date) {
        return toLocalDateTime(date).getYear();
    }

    private static LocalDateTime toLocalDateTime(java.util.Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static java.util.Date truncateToSecond(java.util.Date date) {
        return new java.util.Date((date.getTime() / 1000) * 1000);
    }

    public static java.util.Date truncateToDay(java.util.Date date) {
        return Date.from(toLocalDateTime(date).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static java.util.Date truncateToMonth(java.util.Date date) {
        return Date.from(toLocalDateTime(date).withDayOfMonth(1).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static java.util.Date truncateToYear(java.util.Date date) {
        return Date.from(toLocalDateTime(date).withMonth(1).withDayOfMonth(1).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    //跳转到年的最后一天
    public static java.util.Date seekEndOfYear(java.util.Date date) {
        return Date.from(toLocalDateTime(date).withMonth(12).withDayOfMonth(31).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取一周第一天（周一凌晨零点）
     *
     * @param date 输入时间
     * @return 周一凌晨0点
     */
    public static java.util.Date truncateToWeek(java.util.Date date) {
        LocalDateTime ldt = toLocalDateTime(date).truncatedTo(ChronoUnit.DAYS);
        return Date.from(ldt.minusDays(ldt.getDayOfWeek().getValue() - 1)
            .atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取一周最后一天（周日凌晨零点）
     *
     * @param date 输入时间
     * @return 周日凌晨零点
     */
    public static java.util.Date seekEndOfWeek(java.util.Date date) {
        LocalDateTime ldt = toLocalDateTime(date).truncatedTo(ChronoUnit.DAYS);
        return Date.from(ldt.plusDays(7 - ldt.getDayOfWeek().getValue())
            .atZone(ZoneId.systemDefault()).toInstant());
    }

    //跳转到月的最后一天
    public static java.util.Date seekEndOfMonth(java.util.Date date) {
        return addDays(truncateToMonth(addMonths(date, 1)), -1);
    }

    public static java.util.Date addSeconds(java.util.Date date, int nSeconds) {
        return Date.from(date.toInstant().plusSeconds(nSeconds));
    }

    public static java.util.Date addMinutes(java.util.Date date, int nMinutes) {
        return Date.from(date.toInstant().plusSeconds((long) nMinutes * 60));
    }

    public static java.util.Date addHours(java.util.Date date, int nHours) {
        return Date.from(date.toInstant().plusSeconds((long) nHours * 3600));
    }

    public static java.util.Date addDays(java.util.Date date, int nDays) {
        return Date.from(date.toInstant().plusSeconds((long) nDays * 86400));
    }

    public static java.util.Date addDays(java.util.Date date, float nDays) {
        return new java.util.Date(date.getTime() + (long)(nDays * 86400000.0));
    }

    public static java.util.Date addTimeSpan(java.util.Date date, Object timeSpan) {
        DateTimeSpan dts = DateTimeSpan.from(timeSpan);
        if (dts != null) {
            return new java.util.Date(date.getTime() + dts.longValue());
        }
        return date;
    }

    public static java.util.Date subTimeSpan(java.util.Date date, Object timeSpan) {
        DateTimeSpan dts = DateTimeSpan.from(timeSpan);
        if (dts != null) {
            return new java.util.Date(date.getTime() - dts.longValue());
        }
        return date;
    }

    public static java.util.Date addMonths(java.util.Date date, int nMonths) {
        return Date.from(toLocalDateTime(date).plusMonths(nMonths)
            .atZone(ZoneId.systemDefault()).toInstant());
    }

    public static java.util.Date addYears(java.util.Date date, int nYears) {
        return Date.from(toLocalDateTime(date).plusYears(nYears)
            .atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @param beginDate beginTime
     * @param endDate   endTime
     * @return 计算这个周期中的天数, 包括 beginTime，endTime
     */
    public static int calcSpanDays(java.util.Date beginDate, java.util.Date endDate) {
        java.util.Date bD = (beginDate.getTime() > endDate.getTime()) ? truncateToDay(endDate) : truncateToDay(beginDate);
        java.util.Date eD = (beginDate.getTime() > endDate.getTime()) ? beginDate : endDate;
        return (int) (( eD.getTime() - bD.getTime() - 1L) / 86400000 + 1);
    }

    /**
     * @param beginDate beginTime
     * @param endDate   endTime
     * @return 计算这个周期中的天数, 不足一天用小数表示
     */
    public static double calcDateSpan(java.util.Date beginDate, java.util.Date endDate) {
        return (beginDate.getTime() - endDate.getTime()) / 86400000.0;
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
        if (StringUtils.isBlank(sDate))
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
    public static java.util.Date castObjectToDate(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof java.util.Date d)
            return d;

        if (obj instanceof Number n)
            return new java.util.Date(n.longValue());

        if (obj instanceof LocalDateTime ldt)
            return java.util.Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());

        if (obj instanceof LocalDate ld)
            return java.util.Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        return DatetimeOpt.smartPraseDate(StringBaseOpt.objectToString(obj));
    }
}
