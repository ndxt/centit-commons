package com.centit.support.common;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static java.lang.Thread.sleep;

/**
 * 用 ConcurrentHashMap 缓存对象，每个对象 自己维护缓存策略
 *
 * @param <K> id 键值类型
 * @param <T> target 缓存对象的类型
 */
public class CachedMap<K, T> extends AbstractCachedObject<Map<K, T>> {
    private static Log logger = LogFactory.getLog(CachedMap.class);
    private ConcurrentMap<K, CachedIdentifiedObject> targetMap;
    private long freshPeriod;
    private Function<K, T> refresher;

    /**
     * 构造函数
     *
     * @param refresher       重新获取代码的接口
     * @param freshPeriod     保鲜时间，单位为秒；也是重新刷新时间
     *                        它的意思不是每隔一段时间就刷新，而是在获取数据是检查是否超时，如果超时则刷新
     * @param initialCapacity The implementation performs internal
     *                        sizing to accommodate this many elements.
     */
    public CachedMap(Function<K, T> refresher, long freshPeriod, int initialCapacity) {
        this.targetMap = new ConcurrentHashMap<>(initialCapacity);
        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }

    public CachedMap() {
        this(null,  ICachedObject.DEFAULT_REFRESH_PERIOD, 16);
    }

    public CachedMap(Function<K, T> refresher, AbstractCachedObject<?> parentCache, int initialCapacity) {
        this(refresher,  ICachedObject.DEFAULT_REFRESH_PERIOD, initialCapacity);
        parentCache.addDeriveCache(this);
    }

    /**
     * 构造函数
     *
     * @param freshPeriod 保鲜时间，单位为秒；也是重新刷新时间
     *                    它的意思不是每隔一段时间就刷新，而是在获取数据是检查是否超时，如果超时则刷新
     * @param refresher   重新获取代码的接口
     */
    public CachedMap(Function<K, T> refresher, long freshPeriod) {
        this(refresher, freshPeriod, 16);
    }

    public CachedMap(Function<K, T> refresher, AbstractCachedObject<?> parentCache) {
        this(refresher, parentCache, 16);
    }

    public void setRefresher(Function<K, T> refresher) {
        this.refresher = refresher;
    }

    public void setFreshPeriod(long freshPeriod) {
        this.freshPeriod = freshPeriod;
    }

    public void evictIdentifiedCache(K key) {
        CachedIdentifiedObject identifiedObject = targetMap.get(key);
        if (identifiedObject != null) {
            identifiedObject.evictCache();
        }
    }

    @Override
    public void evictCache() {
        targetMap.clear();
        super.evictCache();
    }

    public AbstractCachedObject<T> getCachedObject(K key) {
        CachedIdentifiedObject identifiedObject = targetMap.get(key);
        if (identifiedObject == null) {
            identifiedObject = new CachedIdentifiedObject(key);
            T target = identifiedObject.getFreshTarget();
            if (target != null) {
                targetMap.put(key, identifiedObject);
            } else {
                identifiedObject = null;
            }
        }
        return identifiedObject;
    }

    public T getCachedValue(K key) {
        CachedIdentifiedObject identifiedObject = targetMap.get(key);
        if (identifiedObject != null) {
            return identifiedObject.getCachedTarget();
        }

        identifiedObject = new CachedIdentifiedObject(key);
        T target = identifiedObject.getFreshTarget();
        if (target != null) {
            targetMap.put(key, identifiedObject);
        }
        return target;
    }

    @Override
    public Map<K, T> getCachedTarget() {
        throw new ObjectException("CachedMap 不支持这个方法");
        //return null;
    }

    public T getFreshValue(K key) {
        CachedIdentifiedObject identifiedObject = targetMap.get(key);
        if (identifiedObject != null) {
            return identifiedObject.getFreshTarget();
        }

        identifiedObject = new CachedIdentifiedObject(key);
        T target = identifiedObject.getFreshTarget();
        if (target != null) {
            targetMap.put(key, identifiedObject);
        }
        return target;
    }

    public boolean isFreshData(K key) {
        CachedIdentifiedObject identifiedObject = targetMap.get(key);
        if (identifiedObject != null) {
            return identifiedObject.evicted;
        }
        return false;
    }

    @Override
    public Map<K, T> getRawTarget() {
        if (targetMap == null) {
            return null;
        }
        Map<K, T> rawTargetMap = new HashMap<>(targetMap.size() + 1);
        for (Map.Entry<K, CachedIdentifiedObject> ent : targetMap.entrySet()) {
            rawTargetMap.put(ent.getKey(), ent.getValue().getRawTarget());
        }
        return rawTargetMap;
    }

    public void setFreshData(K key, T freshData) {
        CachedIdentifiedObject identifiedObject = targetMap.get(key);
        if (identifiedObject != null) {
            identifiedObject.setFreshData(freshData);
        } else {
            targetMap.put(key,
                new CachedIdentifiedObject(key, freshData));
        }
    }

    class CachedIdentifiedObject extends AbstractCachedObject<T> {
        private K key;
        private ReentrantLock freshLock;
        CachedIdentifiedObject(K key, T target) {
            this.key = key;
            this.target = CollectionsOpt.unmodifiableObject(target);
            this.refreshTime = DatetimeOpt.currentUtilDate();
            this.freshLock = new ReentrantLock();
            this.evicted = target == null;
        }

        CachedIdentifiedObject(K key) {
            this(key, null);
        }

        /*synchronized*/ void refreshData() {
            //刷新派生缓存
            if(freshLock.isLocked()) {
                try {
                    //等待其他县城同步好，直接退出
                    while (freshLock.isLocked() && this.target == null) {
                        sleep(15);
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
                T tempTarget = null;
                try {
                    tempTarget = refresher.apply(this.key);
                } catch (RuntimeException re) {
                    logger.error(re.getLocalizedMessage());
                }
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

        T getFreshTarget() {
            refreshData();
            return target;
        }

        @Override
        public T getRawTarget() {
            return target;
        }

        void setFreshData(T freshData) {
            this.target = CollectionsOpt.unmodifiableObject(freshData);
            this.refreshTime = DatetimeOpt.currentUtilDate();
            this.evicted = false;
        }
    }
}
