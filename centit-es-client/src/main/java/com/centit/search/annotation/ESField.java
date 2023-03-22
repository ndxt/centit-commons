package com.centit.search.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by codefan on 17-6-27.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ESField {
    /**
     * @return 表示 字段对应的ES类别名，比如 date,string
     */
    String type() default "";

    /**
     * @return 是否可以检索
     */
    boolean index() default true;

    /**
     * @return  true or false
     */
    boolean store() default true;

    /**
     * @return true 作为全文检索字段
     *          false  精确匹配
     */
    boolean query() default false;

    /**
     * @return 作为高亮字段返回
     */
    boolean highlight() default false;

    /**
     * @return 指定 analyzer , "" 为默认的
     */
    String analyzer() default "";

    /**
     * @return  指定 indexAnalyzer , "" 为默认的
     */
    String indexAnalyzer() default "";

    /**
     * @return 指定search_analyzer , "" 为默认的
     */
    String searchAnalyzer() default "";
}
