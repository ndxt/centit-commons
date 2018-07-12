package com.centit.support.common;

import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static Log logger = LogFactory.getLog(CachedMap.class);

    class CachedIdentifiedObject {
        //private K key;
        private T target;
        private boolean evicted;
        private Date refreshTime;

        public CachedIdentifiedObject(T target){
            //assert target!=null :"输入的 target 值不能为null ";
            //this.key = key;
            this.target = target;
            this.evicted = false;
            this.refreshTime = DatetimeOpt.currentUtilDate();
        }
        /**
         */
        public CachedIdentifiedObject(){
            //this.key = null;
            this.target = null;
            this.evicted = true;
        }

        public synchronized void evictObject(){
            evicted = true;
        }

        private synchronized void refreshData(K key){
            T tempTarget = null;
            try{
                tempTarget = refresher.apply(key);
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

        public T getCachedObject(K key){
            if(this.target == null || this.evicted ||
                    System.currentTimeMillis() > refreshTime.getTime() + freshPeriod * 60000 ){
                refreshData(key);
            }
            return target;
        }

        public synchronized void setFreshtDate(T freshData){
            this.target = freshData;
            this.refreshTime = DatetimeOpt.currentUtilDate();
            this.evicted = false;
        }
    }

    private ConcurrentMap<K, CachedIdentifiedObject> targetMap;
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

    public void evictObject(K key){
        CachedIdentifiedObject identifiedObject =  targetMap.get(key);
        if(identifiedObject!=null){
            identifiedObject.evictObject();
        }
    }

    public void evictAll(){
        targetMap.clear();
    }


    public T getCachedObject(K key){
        CachedIdentifiedObject  identifiedObject =  targetMap.get(key);
        if(identifiedObject != null){
            return identifiedObject.getCachedObject(key);
        }

        identifiedObject = new CachedIdentifiedObject();
        T target = identifiedObject.getCachedObject(key);
        if(target != null) {
            targetMap.put(key,identifiedObject);
        }
        return target;
    }

    public void setRefresher(Function<K, T> refresher) {
        this.refresher = refresher;
    }

    public synchronized void setFreshtDate(K key, T freshData){
        CachedIdentifiedObject identifiedObject =  targetMap.get(key);
        if(identifiedObject != null){
            identifiedObject.setFreshtDate(freshData);
        }else{
            targetMap.put(key,
                    new CachedIdentifiedObject(freshData));
        }
    }
}
