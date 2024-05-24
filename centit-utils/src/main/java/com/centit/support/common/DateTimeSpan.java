package com.centit.support.common;

import java.util.Date;

/**
 * 工作时间差值，
 * dayWorkHours 每日工作时间，如果这个值为24就是自然时间
 * @author codefan@sina.com
 */
@SuppressWarnings("unused")
public class DateTimeSpan extends Number implements java.io.Serializable, Comparable<Number> {

    public static final long DAY_MILLISECONDS = 86400000;
    public static final long HOUR_MILLISECONDS = 3600000;
    public static final long MINUTE_MILLISECONDS = 60000;
    public static final long SECOND_MILLISECONDS = 1000;
    private static final long serialVersionUID = 1L;

    /**
     * 这个值是保存时间差的，单位毫秒
     * 这个时间差是自然时间差，就是每天 86400000 秒，如果要获取工作时间差需要调用 toNumber* 转换
     */
    private long timeSpan;

    public DateTimeSpan() {
        timeSpan = 0;
    }

    @Override
    public int intValue() {
        return Long.valueOf(timeSpan).intValue() ;
    }

    @Override
    public long longValue() {
        return timeSpan;
    }

    @Override
    public float floatValue() {
        return (float)timeSpan;
    }

    @Override
    public double doubleValue() {
        return (double)timeSpan;
    }

    public DateTimeSpan(Date beginDate, Date endDate) {
        this.fromDatatimeSpan(beginDate, endDate);
    }

    public DateTimeSpan(String sTimeSpan) {
        this.fromString(sTimeSpan);
    }

    public DateTimeSpan(long lMillisecond)  {
        this.fromNumberAsMillisecond(lMillisecond);
    }

    public DateTimeSpan(String sign, long days, long hours,
                        long minutes, long second, long millisecond) {
        timeSpan = days * DAY_MILLISECONDS +
            hours * HOUR_MILLISECONDS +
            minutes * MINUTE_MILLISECONDS +
            second * SECOND_MILLISECONDS +
            millisecond;
        if ("-".equals(sign))
            timeSpan = 0 - timeSpan;
    }

    public DateTimeSpan(long days, long hours,
                        long minutes, long second, long millisecond) {
        this("", days, hours, minutes, second, millisecond);
    }

    public DateTimeSpan(String sign, long days, long hours, long minutes) {
        this(sign, days, hours, minutes, 0, 0);
    }

    public DateTimeSpan(long days, long hours, long minutes) {
        this("",  days, hours, minutes, 0, 0);
    }


    /**
     * 计算两个日期之间的时间差
     *
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return 时间间隔
     */
    public static DateTimeSpan calcDateTimeSpan(Date beginDate, Date endDate) {
        DateTimeSpan wrokTimeSpan = new DateTimeSpan();
        wrokTimeSpan.setTimeSpan(endDate.getTime() - beginDate.getTime());
        return wrokTimeSpan;
    }

    /**
     * 时间间隔精确到毫秒
     *
     * @return 时间间隔
     */
    public long getTimeSpan() {
        return timeSpan;
    }

    /**
     * 时间间隔精确到毫秒
     *
     * @param timeSpan 时间间隔
     */
    public void setTimeSpan(long timeSpan) {
        this.timeSpan = timeSpan;
    }

    public DateTimeSpan fromDatatimeSpan(Date beginDate, Date endDate) {
        this.setTimeSpan(beginDate.getTime() - endDate.getTime());
        return this;
    }

    //默认值为 分钟
    public void fromString(String sTimeSpan) {
        int sign = 1;
        long nDays = 0;
        long nHours = 0;
        long nMinutes = 0;
        long nSecond = 0;
        long nMillisecond = 0;

        if (sTimeSpan == null || "".equals(sTimeSpan))
            return;

        char[] sc = sTimeSpan.toCharArray();
        int sl = sTimeSpan.length();
        int sp = 0;
        while (sp < sl && sc[sp] == ' ') sp++;
        if (sc[sp] == '-') {
            sp++;
            sign = -1;
        }

        while (sp < sl) {
            while (sp < sl && !Character.isDigit(sc[sp])) sp++; // 去除非数字
            if (sp >= sl) break;
            int nb = sp;
            while (sp < sl && Character.isDigit(sc[sp]))
                sp++;
            String digits = sTimeSpan.substring(nb, sp);
            while (sp < sl && !Character.isLetter(sc[sp]) && !Character.isDigit(sc[sp])) sp++; // 去除非空格
            if (sp >= sl) {
                if (nDays == 0)
                    nDays = Long.parseLong(digits);
                break;
            }
            if (Character.isDigit(sc[sp]))
                continue;

            switch (sc[sp]) {
                case 'D':
                case 'd':
                    nDays = Long.parseLong(digits);
                    break;
                case 'H':
                case 'h':
                    nHours = Long.parseLong(digits);
                    break;
                case 'M':
                case 'm':
                    nMinutes = Long.parseLong(digits);
                    break;
                case 'S':
                    nSecond = Long.parseLong(digits);
                    break;
                case 's':
                    nMillisecond = Long.parseLong(digits);
                    break;
                default:
                    break;
            }
        }

        timeSpan = sign * (
            nDays * DAY_MILLISECONDS +
                nHours * HOUR_MILLISECONDS +
                nMinutes * MINUTE_MILLISECONDS +
                nSecond * SECOND_MILLISECONDS +
                nMillisecond);

    }

    /**
     * 返回时间中文描述
     *
     * @return 时间中文描述
     */
    public String getTimeSpanDesc() {
        return getSignString() + (getDays() != 0 ? getDays() + "天" : "") +
            (getHours() != 0 ? getHours() + "小时" : "") +
            (getMinutes() != 0 ? getMinutes() + "分" : "");
    }

    public String getTimeSpanDescAsSecond() {
        return getSignString() + (getDays() != 0 ? getDays() + "天" : "") +
            (getHours() != 0 ? getHours() + "小时" : "") +
            (getMinutes() != 0 ? getMinutes() + "分" : "") +
            (getSeconds() != 0 ? getSeconds() + "秒" : "");
    }

    public String getTimeSpanDescAsMillisecond() {
        return getSignString() + (getDays() != 0 ? getDays() + "天" : "") +
            (getHours() != 0 ? getHours() + "小时" : "") +
            (getMinutes() != 0 ? getMinutes() + "分" : "") +
            (getSeconds() != 0 ? getSeconds() + "秒" : "") +
            (getMilliseconds() != 0 ? getMilliseconds() + "毫秒" : "");
    }

    /**
     * 默认到分钟
     */
    @Override
    public String toString() {
        return toStringAsMinute();
    }

    public String toStringAsMinute() {
        return getSignString() + (getDays() != 0 ? getDays() + "D" : "") +
            (getHours() != 0 ? getHours() + "H" : "") +
            (getMinutes() != 0 ? getMinutes() + "M" : "");
    }

    public String toStringAsSecond() {
        return getSignString() + (getDays() != 0 ? getDays() + "D" : "") +
            (getHours() != 0 ? getHours() + "H" : "") +
            (getMinutes() != 0 ? getMinutes() + "M" : "") +
            (getMinutes() != 0 ? getMinutes() + "S" : "");
    }

    public String toStringAsMillisecond() {
        return getSignString() + (getDays() != 0 ? getDays() + "D" : "") +
            (getHours() != 0 ? getHours() + "H" : "") +
            (getMinutes() != 0 ? getMinutes() + "M" : "") +
            (getSeconds() != 0 ? getSeconds() + "S" : "") +
            (getMilliseconds() != 0 ? getMilliseconds() + "s" : "");
    }

    private long toAbsNumberAsMillisecond() {
        return this.timeSpan>0? this.timeSpan: 0 - this.timeSpan;
    }

    public long toNumberAsMillisecond() {
        return this.timeSpan;
    }

    /**
     * 默认单位分钟
     *
     * @return 分钟
     */
    public long toNumberAsMinute() {
        return this.getSign() * (toAbsNumberAsMillisecond() / MINUTE_MILLISECONDS);
    }

    public long toNumberAsSecond() {
        return this.getSign() * (toAbsNumberAsMillisecond() / SECOND_MILLISECONDS);
    }

    /*
     * 默认单位分钟
     */
    public void fromNumberAsMinute(long lSpan) {
        fromNumberAsMillisecond(lSpan * MINUTE_MILLISECONDS);
    }

    public void fromNumberAsHour(long lSpan) {
        fromNumberAsMillisecond(lSpan * HOUR_MILLISECONDS);
    }

    public void fromNumberAsSecond(long lSpan) {
        fromNumberAsMillisecond(lSpan * SECOND_MILLISECONDS);
    }

    public void fromNumberAsMillisecond(long lSpan) {
        this.timeSpan = lSpan;
    }

    public long getSign() {
        return timeSpan > 0 ? 1 : -1;
    }

    public String getSignString() {
        return timeSpan > 0 ? "" : "-";
    }

    public DateTimeSpan changeSign() {
        timeSpan = 0 - timeSpan;
        return this;
    }

    public DateTimeSpan addDays(long ndays) {
        timeSpan += ndays * DAY_MILLISECONDS;
        return this;
    }

    public DateTimeSpan addHours(long nHours) {
        timeSpan += nHours * HOUR_MILLISECONDS;
        return this;
    }

    public DateTimeSpan addMinutes(long nMinutes) {
        timeSpan += nMinutes * MINUTE_MILLISECONDS;
        return this;
    }

    public DateTimeSpan addSeconds(long nSeconds) {
        timeSpan += nSeconds * SECOND_MILLISECONDS;
        return this;
    }

    public DateTimeSpan addMilliseconds(long nMilliseconds) {
        timeSpan += nMilliseconds;
        return this;
    }

    public DateTimeSpan addDateTimeSpan(DateTimeSpan timeSpan) {
        this.timeSpan += timeSpan.timeSpan;
        return this;
    }

    public DateTimeSpan subtractDateTimeSpan(DateTimeSpan timeSpan) {
        this.timeSpan -= timeSpan.timeSpan;
        return this;
    }

    public long getDays() {
        return timeSpan > 0 ? timeSpan / DAY_MILLISECONDS
            : (0 - timeSpan) / DAY_MILLISECONDS;
    }

    private long getRemainderMilliseconds() {
        return timeSpan > 0 ? timeSpan % DAY_MILLISECONDS
            : (0 - timeSpan) % DAY_MILLISECONDS;
    }

    public long getHours() {
        return getRemainderMilliseconds() / HOUR_MILLISECONDS;
    }

    public long getMinutes() {
        return (getRemainderMilliseconds() % HOUR_MILLISECONDS)
            / MINUTE_MILLISECONDS;
    }

    public long getSeconds() {
        return (getRemainderMilliseconds() % MINUTE_MILLISECONDS) / SECOND_MILLISECONDS;
    }

    public long getMilliseconds() {
        return getRemainderMilliseconds() % SECOND_MILLISECONDS;
    }

    public boolean isPositiveTimeSpan() {
        return this.timeSpan > 0;
    }

    @Override
    public int compareTo(Number o) {
        return Long.compare(this.timeSpan, o.longValue());
    }
}
