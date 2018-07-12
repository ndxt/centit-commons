package com.centit.support.common;

import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.function.Supplier;

public class CachedObject<T> {

    private static Log logger = LogFactory.getLog(CachedObject.class);

    public static final int NOT_REFRESH_PERIOD = 43200;
    private T target;
    private boolean evicted;
    private Date refreshTime;
    //分钟
    private int freshPeriod;
    private Supplier<T> refresher;

    public  CachedObject(Supplier<T> refresher){
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        //默认时间一个月
        this.freshPeriod = NOT_REFRESH_PERIOD;
    }

    /**
     *
     * @param refresher 重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为分钟
     */
    public  CachedObject(Supplier<T> refresher, int freshPeriod){
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }

    /**
     * @param freshPeriod 刷新周期 单位分钟
     */
    public void setFreshPeriod(int freshPeriod) {
        this.freshPeriod = freshPeriod;
    }

    public synchronized void evictObject(){
        evicted = true;
    }

    private synchronized void refreshData(){
        T tempTarget = null;
        try{
            tempTarget = refresher.get();
        }catch (RuntimeException re){
            logger.error(re.getLocalizedMessage());
        }
        // 如果获取失败 继续用以前的缓存
        if(tempTarget != null) {
            this.target = tempTarget;
            this.refreshTime = DatetimeOpt.currentUtilDate();
            this.evicted = false;
        }
    }

    public T getCachedObject(){
        if(this.target == null || this.evicted ||
                System.currentTimeMillis() > refreshTime.getTime() + freshPeriod * 60000 ){
            refreshData();
        }
        return target;
    }

    public void setRefresher(Supplier<T> refresher) {
        this.refresher = refresher;
    }

    public synchronized void setFreshtDate(T freshData){
        //T oldTarget =  this.target;
        this.target = freshData;
        this.refreshTime = DatetimeOpt.currentUtilDate();
        this.evicted = false;
        //return oldTarget;
    }
}
