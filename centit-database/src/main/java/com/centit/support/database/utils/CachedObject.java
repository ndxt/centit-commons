package com.centit.support.database.utils;

import com.centit.support.algorithm.DatetimeOpt;

import java.util.Date;

public class CachedObject<T> {
    public interface IRefreshObject<T> {
        T refresh();
    }
    private T target;
    private boolean evicted;
    private Date refreshTime;
    private int freshPeriod;

    IRefreshObject<T> refresher;

    /**
     *
     * @param refresher 重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为分钟
     */
    public  CachedObject(IRefreshObject<T> refresher, int freshPeriod){
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }

    public void setFreshPeriod(int freshPeriod) {
        this.freshPeriod = freshPeriod;
    }

    public synchronized void evict(){
        evicted = true;
    }

    public synchronized T getCachedObject(){
        if(this.target == null || this.evicted ||
                System.currentTimeMillis() > refreshTime.getTime() + freshPeriod * 60000 ){
            target = refresher.refresh();
            refreshTime = DatetimeOpt.currentUtilDate();
            this.evicted = false;
        }
        return target;
    }
}
