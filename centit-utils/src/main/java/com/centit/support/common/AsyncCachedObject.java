package com.centit.support.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.function.Supplier;

public class  AsyncCachedObject<T> extends CachedObject<T>  {

    private static Log logger = LogFactory.getLog(AsyncCachedObject.class);

    public AsyncCachedObject(Supplier<T> refresher){
        super(refresher);
        //asyncRefreshData();
    }

    /**
     *
     * @param refresher 重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为秒
     */
    public AsyncCachedObject(Supplier<T> refresher, int freshPeriod){
        super(refresher, freshPeriod);
        //asyncRefreshData();
    }

    private void asyncRefreshData(){
        this.evicted = false;
        this.refreshTime =
            new Date(System.currentTimeMillis()
                - freshPeriod * 1000L + (freshPeriod / 2 > 5 ? 5 : freshPeriod / 2) * 1000L);
        Thread t = new Thread(this::refreshData);
        //t.sleep(2000);
        t.start();
    }

    @Override
    public T getCachedTarget(){
        if(this.target == null){// || isTargetOutOfDate(freshPeriod)){
            refreshData();
        } else if(isTargetOutOfDate(freshPeriod)){
            asyncRefreshData();
        }
        return target;
    }

}
