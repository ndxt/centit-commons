package com.centit.search.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by codefan on 17-6-27.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESType {
    /**
     * @return 索引名称
     */
    String indexName();

    /**
     * @return 数据备份数
     */
    int replicas() default 1;

    /**
     * @return 数据分片数
     */
    int shards() default 1;
}
