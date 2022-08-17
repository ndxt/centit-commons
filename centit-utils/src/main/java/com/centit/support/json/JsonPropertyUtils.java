package com.centit.support.json;

import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sx
 * Date: 14-11-26
 * Time: 下午4:19
 * Json转换时需要包含或排除属性的工具类
 */
@SuppressWarnings("unused")
public abstract class JsonPropertyUtils {

    private JsonPropertyUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static SerializeFilter getIncludePropPreFilter(Class<?> clazz, String... field) {
        if (ArrayUtils.isNotEmpty(field) && null != clazz) {
            return new SimplePropertyPreFilter(clazz, field);
        }

        return null;
    }

    public static SerializeFilter getIncludePropPreFilter(String[] field) {
        if (ArrayUtils.isNotEmpty(field)) {
            return new SimplePropertyPreFilter(field);
        }

        return null;
    }

    public static SerializeFilter getExcludePropPreFilter(Class<?> clazz, String... field) {
        if (ArrayUtils.isNotEmpty(field) && null != clazz) {
            SimplePropertyPreFilter jsonPropertyPreFilter = new SimplePropertyPreFilter(clazz);
            for (String s : field) {
                jsonPropertyPreFilter.getExcludes().add(s);
            }
            return jsonPropertyPreFilter;
        }

        return null;
    }

    public static SerializeFilter getExcludePropPreFilter(Map<Class<?>, String[]> excludes) {
        if (excludes == null || excludes.isEmpty()) {
            return null;
        }

        JsonPropertyPreFilters jsonPropertyPreFilter = new JsonPropertyPreFilters(excludes.keySet().toArray(new Class[excludes.keySet().size()]));
        for (Map.Entry<Class<?>, String[]> classEntry : excludes.entrySet()) {
            for (String field : classEntry.getValue()) {
                jsonPropertyPreFilter.addExclude(classEntry.getKey(), field);
            }
        }
        return jsonPropertyPreFilter;

    }

    public static SerializeFilter getExcludePropPreFilter(String[] field) {
        if (ArrayUtils.isNotEmpty(field)) {
            SimplePropertyPreFilter jsonPropertyPreFilter = new SimplePropertyPreFilter();
            for (String s : field) {
                jsonPropertyPreFilter.getExcludes().add(s);
            }
            return jsonPropertyPreFilter;
        }
        return null;
    }
}
