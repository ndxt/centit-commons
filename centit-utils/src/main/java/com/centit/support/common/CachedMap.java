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
     *
     * @param refresher 重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为分钟
     */
    public CachedMap(Function<K, T> refresher, long freshPeriod , int initialCapacity){
        this.targetMap = new ConcurrentHashMap<>(initialCapacity);
        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }

    public CachedMap(Function<K, T> refresher){
        this(refresher, 43200L,16);
    }

    public CachedMap(Function<K, T> refresher, int initialCapacity){
        this(refresher, 43200L,initialCapacity);
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
                    new CachedIdentifiedObject(refresher, target, freshPeriod));
        }
        return target;
    }

}
