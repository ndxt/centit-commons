package com.centit.support.common;

import java.io.Serializable;

public class TimeInterval implements Serializable {
    private static final long serialVersionUID = 1L;
    protected Long startTime;

    private final boolean isNano;
    /**
     * 构造
     * @param isNano 是否使用纳秒计数，false则使用毫秒
     */
    public TimeInterval(boolean isNano) {
        this.isNano = isNano;
        this.startTime = null;
    }

    public TimeInterval() {
        this(false);
    }

    /**
     * 开始计时并返回当前时间

     * @return 开始计时并返回当前时间
     */
    public long start() {
        startTime = getTime();
        return startTime;
    }

    /**
     * 重新计时并返回从开始到当前的持续时间秒<br>
     * 如果此分组下没有记录，则返回0;
     * @return 重新计时并返回从开始到当前的持续时间
     */
    public long intervalRestart() {
        final long now = getTime();
        if(startTime==null){
            startTime = now;
            return 0L;
        }
        long inter = now - startTime;
        startTime = now;
        return inter;
    }

    //----------------------------------------------------------- Interval

    /**
     * 从开始到当前的间隔时间（毫秒数）<br>
     * 如果使用纳秒计时，返回纳秒差，否则返回毫秒差<br>
     * 如果分组下没有开始时间，返回{@code null}
     * @return 从开始到当前的间隔时间（毫秒数）
     */
    public long interval() {
        if (null == startTime) {
            return 0;
        }
        return  isNano ? (getTime() - startTime) / 1000000L : getTime() - startTime;
    }

    /**
     * 获取时间的毫秒或纳秒数，纳秒非时间戳
     *
     * @return 时间
     */
    private long getTime() {
        return this.isNano ? System.nanoTime() : System.currentTimeMillis();
    }

    /**
     * 从开始到当前的间隔时间（毫秒数）

     * @return 从开始到当前的间隔时间（毫秒数）
     */
    public long intervalMs(String id) {
        return interval();
    }

    /**
     * 从开始到当前的间隔秒数，取绝对值
     *
     * @return 从开始到当前的间隔秒数，取绝对值
     */
    public long intervalSecond() {
        return interval() / 1000L;
    }

    /**
     * 从开始到当前的间隔分钟数，取绝对值
     *
     * @return 从开始到当前的间隔分钟数，取绝对值
     */
    public long intervalMinute() {
        return interval() / 60000L;
    }

    /**
     * 从开始到当前的间隔小时数，取绝对值
     * @return 从开始到当前的间隔小时数，取绝对值
     */
    public long intervalHour() {
        return interval() / 3600000L;
    }

    /**
     * 从开始到当前的间隔天数，取绝对值

     * @return 从开始到当前的间隔天数，取绝对值
     */
    public long intervalDay() {
        return interval() / (3600000L*24L);
    }

    /**
     * 从开始到当前的间隔周数，取绝对值
     * @return 从开始到当前的间隔周数，取绝对值
     */
    public long intervalWeek() {
        return interval() / (3600000L*24L*7L);
    }

}
