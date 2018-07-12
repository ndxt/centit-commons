package com.centit.support.common;

import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.function.Function;

/**
 * 缓存 有 ID 的对象
 * @param <K> id 键值类型
 * @param <T> target 缓存对象的类型
 */
public class CachedIdentifiedObject<K,T> {

    private static Log logger = LogFactory.getLog(CachedIdentifiedObject.class);
    private T target;
    private boolean evicted;
    private Date refreshTime;
    private long freshPeriod;
    private Function<K, T> refresher;

    public CachedIdentifiedObject(Function<K, T> refresher, T target, long freshPeriod){
        assert target!=null :"输入的 target 值不能为null ";
        this.target = target;
        this.evicted = false;
        refreshTime = DatetimeOpt.currentUtilDate();
        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }
    /**
     *
     * @param refresher 重新获取代码的接口
     * @param freshPeriod 保鲜时间，单位为分钟
     */
    public CachedIdentifiedObject(Function<K, T> refresher, long freshPeriod){
        this.target = null;
        this.evicted = true;
        this.refresher = refresher;
        this.freshPeriod = freshPeriod;
    }

    public CachedIdentifiedObject(Function<K, T> refresher){
        //默认时间一个月
        this(refresher,43200L);
    }


    public void setFreshPeriod(int freshPeriod) {
        this.freshPeriod = freshPeriod;
    }

    public synchronized void evictObject(){
        evicted = true;
    }

    public synchronized T getCachedObject(K key){
        if(this.target == null || this.evicted ||
                System.currentTimeMillis() > refreshTime.getTime() + freshPeriod * 60000 ){

            T tempTarget = null;
            try{
                tempTarget = refresher.apply(key);
            }catch (RuntimeException re){
                logger.error(re.getLocalizedMessage());
            }

            target = tempTarget;// refresher.apply(key);
            refreshTime = DatetimeOpt.currentUtilDate();
            this.evicted = false;
        }
        return target;
    }
}
