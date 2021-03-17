package com.centit.support.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ListAppendMap<T> {
    private List<T> listData;
    private Map<String, T> appendMap;

    public ListAppendMap(List<T> listData, Function<T, String> func){
        if(listData!=null){
            this.listData = listData;//Collections.unmodifiableList(listData);
            //this.appendMap = new HashMap<>();
            this.appendMap = new HashMap<>(listData.size());
            for(T d : listData){
                this.appendMap.put(func.apply(d), d);
            }
            //this.appendMap = Collections.unmodifiableMap(this.appendMap);
        } else {
            this.listData = Collections.emptyList();
            this.appendMap = Collections.emptyMap();
        }
    }

    public List<T> getListData() {
        return listData;
    }

    public Map<String, T> getAppendMap() {
        return appendMap;
    }
}
