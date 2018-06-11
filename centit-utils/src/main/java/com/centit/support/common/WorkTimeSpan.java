package com.centit.support.common;

import java.util.Date;

/**
 * 工作时间差值，
 * dayWorkHours 每日工作时间，如果这个值为24就是自然时间
 * @author codefan
 *
 */
@SuppressWarnings("unused")
public class WorkTimeSpan implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 默认每日工作时间
     */
    
    public static final long DEFAULT_DAY_WORK_MILLISECONDS = 28800000;
    public static final long DEFAULT_DAY_WORK_MINUTES = 480;
    public static final long DAY_MILLISECONDS = 86400000;
    public static final long HOUR_MILLISECONDS = 3600000;
    public static final long MINUTE_MILLISECONDS = 60000;
    public static final long SECOND_MILLISECONDS =  1000;

    /**
     * 每日工作时间(毫秒) 
     */
    private long dayWorkMilliseconds;
    /**
     * 这个值是保存时间差的，单位毫秒
     * 这个时间差是自然时间差，就是每天 86400000 秒，如果要获取工作时间差需要调用 toNumber* 转换
     */
    private long timeSpan;       


    public WorkTimeSpan()
    {
        dayWorkMilliseconds = DEFAULT_DAY_WORK_MILLISECONDS;
        timeSpan = 0;
    }
    
    public WorkTimeSpan(Date beginDate,Date endDate,long dayWorkMinutes){
        dayWorkMilliseconds = dayWorkMinutes*MINUTE_MILLISECONDS;
        this.fromDatatimeSpan(beginDate,endDate);
    }
    
    public WorkTimeSpan(Date beginDate,Date endDate) {
        this(beginDate,endDate,DEFAULT_DAY_WORK_MINUTES);
    }
    
    public WorkTimeSpan(String sTimeSpan){
        dayWorkMilliseconds = DEFAULT_DAY_WORK_MILLISECONDS;
        this.fromString(sTimeSpan);
    }
    
    public WorkTimeSpan(String sign, long dayWorkMinutes, long days,long hours,
                        long minutes,long second,long millisecond){
        this.dayWorkMilliseconds = dayWorkMinutes*MINUTE_MILLISECONDS;
        timeSpan =     days*DAY_MILLISECONDS  +
                    hours * HOUR_MILLISECONDS +
                    minutes * MINUTE_MILLISECONDS +
                    second * SECOND_MILLISECONDS +
                    millisecond;
        if("-".equals(sign))
            timeSpan = 0-timeSpan;
    }
    
    public WorkTimeSpan(long dayWorkMinutes,  long days,long hours,
                        long minutes,long second,long millisecond){
        this("", dayWorkMinutes,days, hours, minutes, second, millisecond);
    }

    public WorkTimeSpan(String sign,  long days,long hours,long minutes,long second,long millisecond){
        this(sign, DEFAULT_DAY_WORK_MINUTES,days, hours, minutes, second, millisecond);
    }
    
    public WorkTimeSpan(String sign,long dayWorkMinutes, long days,long hours,long minutes){
        this(sign, dayWorkMinutes,days, hours, minutes, 0, 0);
    }
    
    public WorkTimeSpan(long dayWorkMinutes, long days,long hours,long minutes) {
        this("", dayWorkMinutes,days, hours, minutes, 0, 0);
    }
    
    public WorkTimeSpan(String sign, long days,long hours,long minutes){
        this(sign, DEFAULT_DAY_WORK_MINUTES,days, hours, minutes, 0, 0);
    }
    
    public WorkTimeSpan( long days,long hours,long minutes){
        this("", DEFAULT_DAY_WORK_MINUTES,days, hours, minutes, 0, 0);
    }

    public WorkTimeSpan(long dayWorkMinutes){
        this.dayWorkMilliseconds = dayWorkMinutes*MINUTE_MILLISECONDS;
        timeSpan = 0;
    }

    /**
     * 时间间隔精确到毫秒
     * @return 时间间隔
     */
    public long getTimeSpan() {
        return timeSpan;
    }

    /**
     *  时间间隔精确到毫秒
     * @param timeSpan 时间间隔
     */
    public void setTimeSpan(long timeSpan) {
        this.timeSpan = timeSpan;
    }

    /**
     * 
     * 每日工作时间(毫秒)
     * 当这个值为24时,这个worktimespan就是datetimespan
     * @return 每日工作时间
     */
    public long getDayWorkMilliseconds() {
        return dayWorkMilliseconds;
    }
    /**
     * 每日工作时间(毫秒)
     * 当这个值为24时,这个worktimespan就是datetimespan
     * @param dayWorkMilliseconds 每日工作时间
     */
    public void setDayWorkMilliseconds(long dayWorkMilliseconds) {
        this.dayWorkMilliseconds = dayWorkMilliseconds;
    }

      /**
     * 
     * 每日工作时间(秒)
     * 当这个值为24时,这个worktimespan就是datetimespan
     * @return 每日工作时间
     */
    public long getDayWorkSeconds() {
        return dayWorkMilliseconds / SECOND_MILLISECONDS;
    }
    /**
     * 每日工作时间(秒)
     * 当这个值为24时,这个worktimespan就是datetimespan
     * @param dayWorkSeconds 每日工作时间
     */
    public void setDayWorkSeconds(long dayWorkSeconds) {
        this.dayWorkMilliseconds = dayWorkSeconds * SECOND_MILLISECONDS;
    }

      /**
     * 
     * 每日工作时间(分钟)
     * 当这个值为24时,这个worktimespan就是datetimespan
     * @return 每日工作时间
     */
    public long getDayWorkMinutes() {
        return dayWorkMilliseconds / MINUTE_MILLISECONDS;
    }
    /**
     * 每日工作时间(分钟)
     * 当这个值为24时,这个worktimespan就是datetimespan
     * @param dayWorkMinutes 每日工作时间
     */
    public void setDayWorkMinutes(long dayWorkMinutes) {
        this.dayWorkMilliseconds = dayWorkMinutes*MINUTE_MILLISECONDS;
    }

      /**
     * 
     * 每日工作时间(小时)
     * 当这个值为24时,这个worktimespan就是datetimespan
     * @return 每日工作时间
     */
    public long getDayWorkHours() {
        return dayWorkMilliseconds / HOUR_MILLISECONDS;
    }
    /**
     * 每日工作时间(小时)
     * 当这个值为24时,这个worktimespan就是datetimespan
     * @param dayWorkHours 每日工作时间
     */
    public void setDayWorkHours(long dayWorkHours) {
        this.dayWorkMilliseconds = dayWorkHours * HOUR_MILLISECONDS;
    }

    public  WorkTimeSpan fromDatatimeSpan(Date beginDate,Date endDate){
        this.setTimeSpan(beginDate.getTime() - endDate.getTime());
        return this;
    }

    public void fromString(String sTimeSpan){
        int sign=1;
        long nDays =0;
        long nHours =0;
        long nMinutes = 0;
        long nSecond = 0;
        long nMillisecond = 0;
        
        if(sTimeSpan==null  || "".equals(sTimeSpan))
            return;

        char[] sc= sTimeSpan.toCharArray();
        int sl = sTimeSpan.length();
        int sp=0 ;
        while(sp<sl && sc[sp]==' ') sp++;            
        if(sc[sp]=='-'){
            sp++;
            sign = -1;
        }
       
        while(sp<sl){
           while(sp<sl && !Character.isDigit(sc[sp])) sp++; // 去除非数字
           if(sp>=sl) break;
           int nb=sp;
           while(sp<sl && Character.isDigit(sc[sp]))
               sp++;
           String digits = sTimeSpan.substring(nb,sp);
           while(sp<sl && !Character.isLetter(sc[sp]) && !Character.isDigit(sc[sp])) sp++; // 去除非空格
           if(sp>=sl){
               if(nDays==0)
                   nDays=Long.parseLong(digits);
               break;
           }
           if(Character.isDigit(sc[sp]))
               continue;
           
           switch (sc[sp]) {
           case 'D':
           case 'd':
                nDays=Long.parseLong(digits); 
                break;
           case 'H':
           case 'h':
               nHours=Long.parseLong(digits);
               break;
           case 'M':             
           case 'm':
               nMinutes=Long.parseLong(digits);
                  break;
           case 'S':             
               nSecond=Long.parseLong(digits);
               break;
           case 's':             
               nMillisecond=Long.parseLong(digits);
               break;
           default:
               break;
           }          
       }
       timeSpan = sign * (
                nDays*DAY_MILLISECONDS  +
                nHours * HOUR_MILLISECONDS +
                nMinutes * MINUTE_MILLISECONDS +
                nSecond * SECOND_MILLISECONDS +
                nMillisecond);
   
    }
    
    /**
     * 返回时间中文描述
     * @return 时间中文描述
     */
    public String getTimeSpanDesc(){
        return getSignString() + (getDays()!=0? getDays()+"天":"") +
                (getHours()!=0?getHours()+"小时":"")+
                (getMinutes()!=0?getMinutes()+"分":"");
    }
    
    public String getTimeSpanDescAsSecond(){
        return getSignString() + (getDays()!=0? getDays()+"天":"") +
                (getHours()!=0?getHours()+"小时":"")+
                (getMinutes()!=0?getMinutes()+"分":"")+
                (getSeconds()!=0?getSeconds()+"秒":"");
    }
    
    public String getTimeSpanDescAsMillisecond(){
        return getSignString() + (getDays()!=0? getDays()+"天":"") +
                (getHours()!=0?getHours()+"小时":"")+
                (getMinutes()!=0?getMinutes()+"分":"")+
                (getSeconds()!=0?getSeconds()+"秒":"")+
                (getMilliseconds()!=0?getMilliseconds()+"毫秒":"");
    }
    
    /**
     * 默认到分钟
     */
    @Override
    public String toString(){
        return getSignString()+(getDays()!=0? getDays() +"D":"") +
                (getHours()!=0?getHours()+"H":"")+
                (getMinutes()!=0?getMinutes()+"M":"");
    }
    
    public String toStringAsSecond(){
        return getSignString()+(getDays()!=0? getDays() +"D":"") +
                (getHours()!=0?getHours()+"H":"")+
                (getMinutes()!=0?getMinutes()+"M":"")+
                (getMinutes()!=0?getMinutes()+"S":"");
    } 
    
    public String toStringAsMillisecond(){
        return getSignString()+(getDays()!=0? getDays() +"D":"") +
                (getHours()!=0?getHours()+"H":"")+
                (getMinutes()!=0?getMinutes()+"M":"")+
                (getSeconds()!=0?getSeconds()+"S":"")+
                (getMilliseconds()!=0?getMilliseconds()+"s":"");
    }
    
   
    private long toAbsNumberAsMillisecond(){
        return     this.getDays() * dayWorkMilliseconds +
                this.getHours() * HOUR_MILLISECONDS +
                this.getMinutes() * MINUTE_MILLISECONDS +
                this.getSeconds() * SECOND_MILLISECONDS +
                this.getMilliseconds() ;
    }
    
    public long toNumberAsMillisecond(){
        return this.getSign() * toAbsNumberAsMillisecond();
    }
    
    /**
     * 默认单位分钟
     * @return 分钟
     */
    public long toNumber(){
        return this.getSign() * ( toAbsNumberAsMillisecond() / MINUTE_MILLISECONDS);
    }
    
    public long toNumberAsSecond(){
        return this.getSign() * ( toAbsNumberAsMillisecond() / SECOND_MILLISECONDS);
    }
    
    
    /*
     * 默认单位分钟
     */
    public void fromNumber(long lSpan)
    {
        fromNumberAsMillisecond(lSpan * MINUTE_MILLISECONDS);
    }
    
    public void fromNumberAsHour(long lSpan)
    {
        fromNumberAsMillisecond(lSpan * HOUR_MILLISECONDS);
    }
    
    public void fromNumberAsSecond(long lSpan)
    {
        fromNumberAsMillisecond(lSpan * SECOND_MILLISECONDS);
    }
    
    public void fromNumberAsMillisecond(long lSpan){
        long tempSpan;
        long nSign ;
        if(lSpan>0){
            tempSpan = lSpan;
            nSign = 1;
        }else{
            tempSpan = 0-lSpan;
            nSign = -1;
        }
        long nDays = tempSpan / dayWorkMilliseconds;
        long nRemainder = tempSpan % dayWorkMilliseconds;
        this.timeSpan = nSign * (nDays * DAY_MILLISECONDS + nRemainder);
    }
    
    public long getSign() {
        return timeSpan>0?1:-1;
    }
    
    public String getSignString() {
        return timeSpan>0?"":"-";
    }
    
    public WorkTimeSpan changeSign() {
        timeSpan = 0-timeSpan;
        return this;
    }
     
    public WorkTimeSpan addDays(long ndays) {
        fromNumberAsMillisecond(
                toNumberAsMillisecond()
                + ndays * dayWorkMilliseconds);
        return this;
    }

    public WorkTimeSpan addHours(long nHours) {
        fromNumberAsMillisecond(
                toNumberAsMillisecond()
                + nHours * HOUR_MILLISECONDS);
        return this;  
    }
    
    public WorkTimeSpan addMinutes(long nMinutes) {
        fromNumberAsMillisecond(
                toNumberAsMillisecond()
                + nMinutes * MINUTE_MILLISECONDS);
        return this;
    }

    public WorkTimeSpan addSeconds(long nSeconds) {
        fromNumberAsMillisecond(
                toNumberAsMillisecond()
                + nSeconds * SECOND_MILLISECONDS);
        return this;
    }
  
    public WorkTimeSpan addMilliseconds(long nMilliseconds) {
        fromNumberAsMillisecond(
                toNumberAsMillisecond()
                + nMilliseconds);
        return this;
    }
  
    public long getDays() {
        return timeSpan>0?  timeSpan / DAY_MILLISECONDS 
                : (0-timeSpan) / DAY_MILLISECONDS;
    }

    private long getRemainderMilliseconds(){
        return timeSpan>0?  timeSpan % DAY_MILLISECONDS
                : (0-timeSpan) % DAY_MILLISECONDS;
    }
    
    public long getHours() {
        return getRemainderMilliseconds() / HOUR_MILLISECONDS;
    }

    public long getMinutes() {
        return (getRemainderMilliseconds() % HOUR_MILLISECONDS)
                / MINUTE_MILLISECONDS;
    }

    public long getSeconds() {
        return (getRemainderMilliseconds() % MINUTE_MILLISECONDS) / SECOND_MILLISECONDS  ;
    }
  
    public long getMilliseconds() {
        return getRemainderMilliseconds() % SECOND_MILLISECONDS;
    }
    
     
    /**
     * 计算两个日期之间的时间差
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 时间间隔
     */
    public static WorkTimeSpan calcDatatimeSpan(Date beginDate,Date endDate){
        WorkTimeSpan wrokTimeSpan = new WorkTimeSpan();
        wrokTimeSpan.setDayWorkMilliseconds(DAY_MILLISECONDS);
        wrokTimeSpan.setTimeSpan(beginDate.getTime() - endDate.getTime());
        return wrokTimeSpan;
    }
    
    public static WorkTimeSpan calcWorkTimeSpan(Date beginDate,Date endDate,Long dayWorkMinutes){
        WorkTimeSpan wrokTimeSpan = new WorkTimeSpan();
        wrokTimeSpan.setDayWorkMilliseconds(dayWorkMinutes * MINUTE_MILLISECONDS);
        wrokTimeSpan.setTimeSpan(beginDate.getTime() - endDate.getTime());
        return wrokTimeSpan;
    }
    
    public static WorkTimeSpan calcWorkTimeSpan(Date beginDate,Date endDate){
        WorkTimeSpan wrokTimeSpan = new WorkTimeSpan();
        wrokTimeSpan.setDayWorkMilliseconds(DEFAULT_DAY_WORK_MILLISECONDS);
        wrokTimeSpan.setTimeSpan(beginDate.getTime() - endDate.getTime());
        return wrokTimeSpan;
    }
}
