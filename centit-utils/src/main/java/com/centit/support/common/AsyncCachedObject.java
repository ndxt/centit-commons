package com.centit.support.common;

import java.util.Date;
import java.util.function.Supplier;

public class  AsyncCachedObject<T> extends CachedObject<T>  {

    public AsyncCachedObject(Supplier<T> refresher){
        super(refresher);
        //asyncRefreshData();
    }

    /**
     *
     * @param refresher 重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为秒
     */
    public AsyncCachedObject(Supplier<T> refresher, long freshPeriod){
        super(refresher, freshPeriod);
        //asyncRefreshData();
    }
    // 这个地方可能有锁定的问题
    private void asyncRefreshData(){
        this.evicted = false;
        this.refreshTime =
            new Date(System.currentTimeMillis()
                - freshPeriod * 1000L + (freshPeriod / 2 > 5 ? 5 : freshPeriod / 2) * 1000L);
        Thread t = new Thread(this::refreshData);
        //t.sleep(2000);
        t.start();
    }

    public T ensureGetCachedTarget(){
        if(this.target == null){// || isTargetOutOfDate(freshPeriod)){
            refreshData();
        } else if(isTargetOutOfDate(freshPeriod)){
            asyncRefreshData();
        }
        return target;
    }


    @Override
    public T getCachedTarget(){
        if(this.target == null || isTargetOutOfDate(freshPeriod)){
            asyncRefreshData();
        }
        return target;
    }

}
