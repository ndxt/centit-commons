package com.centit.support.common;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractCachedObject<T> implements ICachedObject<T> {

    protected T target;
    boolean evicted;
    Date refreshTime;
    private Set<ICachedObject<?>> deriveCaches;

    boolean isTargetOutOfDate(long freshPeriod) {
        return this.evicted ||
            System.currentTimeMillis() > refreshTime.getTime() + freshPeriod * 1000L;
    }

    void evictDerivativeCahce() {
        if (deriveCaches != null) {
            for (ICachedObject eriveCache : deriveCaches) {
                eriveCache.evictCahce();
            }
        }
    }

    void setRefreshDataAndState(T tempTarget, long freshPeriod, final boolean delayIfNull) {
        // 如果获取失败 继续用以前的缓存
        if (tempTarget != null) {
            this.target = CollectionsOpt.unmodifiableObject(tempTarget);
            this.refreshTime = DatetimeOpt.currentUtilDate();
            this.evicted = false;
        } // 刷新失败延时刷新，每次最多延时5秒
        else if (this.target != null && delayIfNull) {
            this.refreshTime =
                new Date(System.currentTimeMillis()
                    - freshPeriod * 1000L + (freshPeriod / 2 > 5 ? 5 : freshPeriod / 2) * 1000L);
            this.evicted = false;
        }
    }

    public void evictCahce() {
        evicted = true;
        evictDerivativeCahce();
    }

    public void addDeriveCache(ICachedObject<?> eriveCache) {
        if (eriveCache == null) {
            return;
        }

        if (deriveCaches == null) {
            deriveCaches = new HashSet<>(6);
        }

        deriveCaches.add(eriveCache);
    }

}
