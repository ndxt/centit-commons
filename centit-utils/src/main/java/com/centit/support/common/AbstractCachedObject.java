package com.centit.support.common;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractCachedObject<T> implements ICachedObject<T> {


    protected boolean evicted;

    protected Set<ICachedObject<?>> deriveCaches;

    public AbstractCachedObject(){
    }

    protected void evictDerivativeCahce(){
        if(deriveCaches != null){
            for(ICachedObject eriveCache : deriveCaches){
                eriveCache.evictCahce();
            }
        }
    }

    public void evictCahce(){
        evicted = true;
        evictDerivativeCahce();
    }

    public void addDeriveCache(ICachedObject<?> eriveCache){
        if(eriveCache==null){
            return;
        }

        if(deriveCaches==null){
            deriveCaches = new HashSet<>(6);
        }

        deriveCaches.add(eriveCache);
    }

}
