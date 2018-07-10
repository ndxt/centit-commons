package com.centit.support.common;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 用 ConcurrentHashMap 缓存对象，每个对象 自己维护缓存策略
 * @param <K> id 键值类型
 * @param <T> target 缓存对象的类型
 */
public class CachedMap<K,T> {

    private ConcurrentMap<K, CachedIdentifiedObject<K,T>> targetMap;
    private Date refreshTime;
    private long freshPeriod;
    private Function<K, T> refresher;

    /**
     * 构造函数
     * @param refresher 重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为分钟；也是重新刷新时间
     *                    它的意思不是每隔一段时间就刷新，而是在获取数据是检查是否超时，如果超时则刷新
     * @param initialCapacity The implementation performs internal
     * sizing to accommodate this many elements.
     */
    public CachedMap(Function<K, T> refresher, long freshPeriod, int initialCapacity){
        this.targetMap = new ConcurrentHashMap<>(initialCapacity);
        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }

    /**
     * 构造函数
     * @param freshPeriod 保鲜时间，单位为分钟；也是重新刷新时间
     *                    它的意思不是每隔一段时间就刷新，而是在获取数据是检查是否超时，如果超时则刷新
     * @param refresher 重新获取代码的接口
     */
    public CachedMap(Function<K, T> refresher, long freshPeriod){
        this(refresher, freshPeriod,16);
    }

    public void setFreshPeriod(int freshPeriod) {
        this.freshPeriod = freshPeriod;
    }

    public synchronized void evictObject(K key){
        CachedIdentifiedObject<K,T> identifiedObject =  targetMap.get(key);
        if(identifiedObject!=null){
            identifiedObject.evictObject();
        }
    }

    public synchronized T getCachedObject(K key){
        CachedIdentifiedObject<K,T> identifiedObject =  targetMap.get(key);
        if(identifiedObject != null){
            return identifiedObject.getCachedObject(key);
        }

        T target = refresher.apply(key);
        if(target != null) {
            targetMap.put(key,
                    new CachedIdentifiedObject<>(refresher, target, freshPeriod));
        }
        return target;
    }

}
