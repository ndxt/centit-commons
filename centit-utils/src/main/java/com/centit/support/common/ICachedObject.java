package com.centit.support.common;

public interface ICachedObject<T> {
    //单位分钟，表示一个月
    int NOT_REFRESH_PERIOD = 43200;

    void evictCahce();

    //获取原始缓存数据
    T getRawTarget();
}
