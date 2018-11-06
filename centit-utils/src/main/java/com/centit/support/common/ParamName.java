package com.centit.support.common;

import java.lang.annotation.*;

/**
 * 为了兼容java8 以前的版本 在反射时无法获得参数的名称而天际的一个参数名称
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ParamName {
    String value();
}
