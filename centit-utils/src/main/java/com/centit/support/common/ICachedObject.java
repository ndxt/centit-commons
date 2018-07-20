package com.centit.support.common;

public interface ICachedObject<T> {
    //单位分钟，表示一个月
    long NOT_REFRESH_PERIOD = 43200L;

    void evictCahce();

    //获取原始缓存数据
    T getRawTarget();
}
