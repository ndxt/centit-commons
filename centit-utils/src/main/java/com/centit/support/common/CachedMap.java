package com.centit.support.common;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 用 ConcurrentHashMap 缓存对象，每个对象 自己维护缓存策略
 * @param <K> id 键值类型
 * @param <T> target 缓存对象的类型
 */
public class CachedMap<K,T> extends AbstractCachedObject<Map<K,T>>  {
    private static Log logger = LogFactory.getLog(CachedMap.class);

    class CachedIdentifiedObject extends AbstractCachedObject<T> {

        private Date refreshTime;
        private T target;

        CachedIdentifiedObject(T target){
            this.target = CollectionsOpt.unmodifiableObject(target);
            this.evicted = false;
            this.refreshTime =  DatetimeOpt.currentUtilDate();
        }
        /**
         */
        CachedIdentifiedObject(){
            this.target = null;
            this.evicted = true;
        }

        synchronized void refreshData(K key){
            //刷新派生缓存
            evictDerivativeCahce();

            T tempTarget = null;
            try{
                tempTarget = refresher.apply(key);
            }catch (RuntimeException re){
                logger.error(re.getLocalizedMessage());
            }
            // 如果获取失败 继续用以前的缓存
            if(tempTarget != null) {
                this.target = CollectionsOpt.unmodifiableObject(tempTarget);
                this.refreshTime = DatetimeOpt.currentUtilDate();
                this.evicted = false;
            }
        }

        T getCachedTarget(K key){
            if(this.target == null || this.evicted ||
                    System.currentTimeMillis() > refreshTime.getTime() + freshPeriod * 1000L){
                refreshData(key);
            }
            return target;
        }

        T getFreshTarget(K key){
            refreshData(key);
            return target;
        }

        @Override
        public T getRawTarget() {
            return target;
        }

        void setFreshtDate(T freshData){
            this.target = CollectionsOpt.unmodifiableObject(freshData);
            this.refreshTime = DatetimeOpt.currentUtilDate();
            this.evicted = false;
        }
    }

    private ConcurrentMap<K, CachedIdentifiedObject> targetMap;
    private long freshPeriod;
    private Function<K, T> refresher;

    public CachedMap(){
        this.targetMap = new ConcurrentHashMap<>(16);
        this.refresher = null;
        this.freshPeriod = ICachedObject.NOT_REFRESH_PERIOD;
    }

    /**
     * 构造函数
     * @param refresher 重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为秒；也是重新刷新时间
     *                    它的意思不是每隔一段时间就刷新，而是在获取数据是检查是否超时，如果超时则刷新
     * @param initialCapacity The implementation performs internal
     * sizing to accommodate this many elements.
     */
    public CachedMap(Function<K, T> refresher, long freshPeriod, int initialCapacity){
        this.targetMap = new ConcurrentHashMap<>(initialCapacity);
        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }

    public CachedMap(Function<K, T> refresher,  AbstractCachedObject<?> parentCache , int initialCapacity){
        this.targetMap = new ConcurrentHashMap<>(initialCapacity);
        this.refresher = refresher;
        parentCache.addDeriveCache(this);
        this.freshPeriod = ICachedObject.NOT_REFRESH_PERIOD;
    }
    /**
     * 构造函数
     * @param freshPeriod 保鲜时间，单位为秒；也是重新刷新时间
     *                    它的意思不是每隔一段时间就刷新，而是在获取数据是检查是否超时，如果超时则刷新
     * @param refresher 重新获取代码的接口
     */
    public CachedMap(Function<K, T> refresher, long freshPeriod){
        this(refresher, freshPeriod,16);
    }

    public CachedMap(Function<K, T> refresher,  AbstractCachedObject<?> parentCache ){
        this(refresher, parentCache,16);
    }

    public void setRefresher(Function<K, T> refresher) {
        this.refresher = refresher;
    }

    public void setFreshPeriod(int freshPeriod) {
        this.freshPeriod = freshPeriod;
    }

    public void evictIdentifiedCache (K key){
        CachedIdentifiedObject identifiedObject =  targetMap.get(key);
        if(identifiedObject!=null){
            identifiedObject.evictCahce();
        }
    }

    @Override
    public void evictCahce(){
        targetMap.clear();
        super.evictCahce();
    }

    public AbstractCachedObject<T> getCachedObject(K key){
        CachedIdentifiedObject  identifiedObject =  targetMap.get(key);
        if(identifiedObject == null){
            identifiedObject = new CachedIdentifiedObject();
            T target = identifiedObject.getFreshTarget(key);
            if(target != null) {
                targetMap.put(key,identifiedObject);
            }else{
                identifiedObject = null;
            }
        }
        return identifiedObject;
    }

    public T getCachedValue(K key){
        CachedIdentifiedObject  identifiedObject =  targetMap.get(key);
        if(identifiedObject != null){
            return identifiedObject.getCachedTarget(key);
        }

        identifiedObject = new CachedIdentifiedObject();
        T target = identifiedObject.getFreshTarget(key);
        if(target != null) {
            targetMap.put(key,identifiedObject);
        }
        return target;
    }

    public T getFreshValue(K key){
        CachedIdentifiedObject  identifiedObject =  targetMap.get(key);
        if(identifiedObject != null){
            return identifiedObject.getFreshTarget(key);
        }

        identifiedObject = new CachedIdentifiedObject();
        T target = identifiedObject.getFreshTarget(key);
        if(target != null) {
            targetMap.put(key,identifiedObject);
        }
        return target;
    }

    public boolean isFreshData(K key){
        CachedIdentifiedObject  identifiedObject =  targetMap.get(key);
        if(identifiedObject != null){
            return identifiedObject.evicted;
        }
        return false;
    }

    public void setFreshDataPair(K key, T freshData){
        CachedIdentifiedObject  identifiedObject =  targetMap.get(key);
        if(identifiedObject != null){
            identifiedObject.setFreshtDate(freshData);
        }else{
            identifiedObject = new CachedIdentifiedObject(freshData);
            targetMap.put(key,identifiedObject);
        }
    }

    public Map<K,T> getRawTarget(){
        if(targetMap == null){
            return null;
        }
        Map<K,T> rawTargetMap = new HashMap<>( targetMap.size() +1);
        for(Map.Entry<K,CachedIdentifiedObject> ent : targetMap.entrySet()){
            rawTargetMap.put(ent.getKey(), ent.getValue().getRawTarget());
        }
        return rawTargetMap;
    }

    public void setFreshtDate(K key, T freshData){
        CachedIdentifiedObject identifiedObject =  targetMap.get(key);
        if(identifiedObject != null){
            identifiedObject.setFreshtDate(freshData);
        }else{
            targetMap.put(key,
                    new CachedIdentifiedObject(freshData));
        }
    }
}
