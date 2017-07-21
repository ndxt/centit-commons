package com.centit.support.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;

/**
 * Created with IntelliJ IDEA.
 * User: sx
 * Date: 14-11-26
 * Time: 下午3:54
 * 重新实现Json格式化时属性过滤，添加排除属性，且排除属性优先级高于包含属性。
 * 可以同时过滤多个对象的多个属性
 */
@SuppressWarnings("unused")
public class JsonPropertyPreFilters implements PropertyPreFilter {

    private Set<Class<?>> clazzs;
    private Map<Class<?>, Set<String>> excludes = new HashMap<Class<?>, Set<String>>();


    public JsonPropertyPreFilters(Class<?>[] clazzs) {
        super();
        this.clazzs = new HashSet<Class<?>>();
        
        for (Class<?> clazz : clazzs) {
            this.clazzs.add(clazz);
        }
    }

    public JsonPropertyPreFilters() {
    }

    @Override
    public boolean apply(JSONSerializer serializer, Object object, String name) {
        if (object == null) {
            return true;
        }
        
        for (Class<?> clazz : clazzs) {
            if (clazz.isInstance(object)) {
                if (this.excludes.get(clazz).contains(name)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void addExclude(Class<?> clazz, String exclude) {
        if (this.excludes.get(clazz) == null) {
            Set<String> fields = new HashSet<String>();
            this.excludes.put(clazz, fields);
        }
        
        Set<String> fields = this.excludes.get(clazz);
        fields.add(exclude);
    }
}
