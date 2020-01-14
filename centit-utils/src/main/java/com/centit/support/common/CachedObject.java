package com.centit.support.common;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.function.Supplier;

public class CachedObject<T> extends AbstractCachedObject<T> {

    private static Log logger = LogFactory.getLog(CachedObject.class);

    private Supplier<T> refresher;
    private long freshPeriod;

    public CachedObject() {
    }

    public CachedObject(Supplier<T> refresher) {
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        this.freshPeriod = ICachedObject.NOT_REFRESH_PERIOD;
    }

    /**
     * @param refresher   重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为秒
     */
    public CachedObject(Supplier<T> refresher, long freshPeriod) {
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }


    public CachedObject(Supplier<T> refresher, AbstractCachedObject<?> parentCache) {
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        parentCache.addDeriveCache(this);
        this.freshPeriod = ICachedObject.NOT_REFRESH_PERIOD;
    }

    public CachedObject(Supplier<T> refresher, AbstractCachedObject<?>[] parentCaches) {
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        for (AbstractCachedObject<?> parentCache : parentCaches) {
            parentCache.addDeriveCache(this);
        }
        this.freshPeriod = ICachedObject.NOT_REFRESH_PERIOD;
    }

    /**
     * @param freshPeriod 刷新周期 单位秒
     */
    public void setFreshPeriod(long freshPeriod) {
        this.freshPeriod = freshPeriod;
    }

    private synchronized void refreshData() {
        //刷新派生缓存
        evictDerivativeCahce();
        T tempTarget = null;
        try {
            tempTarget = refresher.get();
        } catch (RuntimeException re) {
            logger.error(re.getMessage(), re);
        }
        setRefreshDataAndState(tempTarget, freshPeriod, true);
    }

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
