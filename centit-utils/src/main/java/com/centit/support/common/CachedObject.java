package com.centit.support.common;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static java.lang.Thread.sleep;

public class CachedObject<T> extends AbstractCachedObject<T> {

    private static Log logger = LogFactory.getLog(CachedObject.class);

    private Supplier<T> refresher;
    protected long freshPeriod;
    private ReentrantLock freshLock;

    /**
     * @param refresher   重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为秒
     */
    public CachedObject(Supplier<T> refresher, long freshPeriod) {
        this.target = null;
        this.evicted = true;
        this.freshLock=new ReentrantLock();

        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }

    public CachedObject() {
        this(null, ICachedObject.DEFAULT_REFRESH_PERIOD);
    }


    public CachedObject(Supplier<T> refresher) {
        this(refresher, ICachedObject.DEFAULT_REFRESH_PERIOD);
    }

    public CachedObject(Supplier<T> refresher, AbstractCachedObject<?> parentCache) {
        this(refresher, ICachedObject.DEFAULT_REFRESH_PERIOD);
        parentCache.addDeriveCache(this);
    }

    public CachedObject(Supplier<T> refresher, AbstractCachedObject<?>[] parentCaches) {
        this(refresher, ICachedObject.DEFAULT_REFRESH_PERIOD);
        for (AbstractCachedObject<?> parentCache : parentCaches) {
            parentCache.addDeriveCache(this);
        }
    }

    /**
     * @param freshPeriod 刷新周期 单位秒
     */
    public void setFreshPeriod(long freshPeriod) {
        this.freshPeriod = freshPeriod;
    }

    public void refreshData() {
        if(freshLock.isLocked()) {
            try {
                //等待其他县城同步好，直接退出
                while (freshLock.isLocked() && this.target == null) {
                    sleep(20);
                }
            }catch (InterruptedException e){
                logger.error(e.getMessage());
            }
            return;
        }

        try {
            freshLock.lock();
            //刷新派生缓存
            evictDerivativeCahce();
            T tempTarget = refresher.get();
            setRefreshDataAndState(tempTarget, freshPeriod, true);
        } finally {
            freshLock.unlock();
        }
    }

    @Override
    public T getCachedTarget() {
        if (this.target == null || isTargetOutOfDate(freshPeriod)) {
            refreshData();
        }
        return target;
    }

    public T getFreshTarget() {
        refreshData();
        return target;
    }

    public T getRawTarget() {
        return target;
    }

    public void setRefresher(Supplier<T> refresher) {
        this.refresher = refresher;
    }

    public void setFreshData(T freshData) {
        this.target = CollectionsOpt.unmodifiableObject(freshData);
        this.refreshTime = DatetimeOpt.currentUtilDate();
        this.evicted = false;
    }

}
