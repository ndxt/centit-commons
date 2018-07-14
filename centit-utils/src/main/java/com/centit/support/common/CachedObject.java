package com.centit.support.common;

import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.function.Supplier;

public class CachedObject<T> extends AbstractCachedObject<T>  {

    private static Log logger = LogFactory.getLog(CachedObject.class);

    private Supplier<T> refresher;
    private Date refreshTime;
    private int freshPeriod;
    private T target;

    public  CachedObject(){
    }

    public  CachedObject(Supplier<T> refresher){
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        this.freshPeriod = ICachedObject.NOT_REFRESH_PERIOD;
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


    public  CachedObject(Supplier<T> refresher, AbstractCachedObject<?> parentCache ){
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        parentCache.addDeriveCache(this);
        this.freshPeriod = ICachedObject.NOT_REFRESH_PERIOD;
    }

    /**
     * @param freshPeriod 刷新周期 单位分钟
     */
    public void setFreshPeriod(int freshPeriod) {
        this.freshPeriod = freshPeriod;
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

    public T getCachedTarget(){
        if(this.target == null || this.evicted ||
                System.currentTimeMillis() > refreshTime.getTime() + freshPeriod * 60000 ){
            refreshData();
        }
        return target;
    }

    public T getFreshTarget(){
        refreshData();
        return target;
    }

    public T getRawTarget(){
        return target;
    }

    public void setRefresher(Supplier<T> refresher) {
        this.refresher = refresher;
    }

    public synchronized void setFreshtDate(T freshData){
        this.target = freshData;
        this.refreshTime = DatetimeOpt.currentUtilDate();
        this.evicted = false;
    }

}
