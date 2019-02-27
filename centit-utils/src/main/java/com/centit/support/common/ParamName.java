package com.centit.support.common;

import java.lang.annotation.*;

/**
 * 为了兼容java8 以前的版本 在反射时无法获得参数的名称而天际的一个参数名称
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})

public @interface ParamName {
    /**
     * @return 参数名称
     */
    String value();

    /**
     * 是否可以为空
     * @return true可以为空 false 不能为空
     */
    boolean nullable() default true;
}
