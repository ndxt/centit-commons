package com.centit.support.database.orm;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by codefan on 17-8-29.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ValueGenerator {
    /**
     * 数值生成方式
     * @return GeneratorType
     */
    GeneratorType strategy() default GeneratorType.AUTO;

    /**
     * 数值生成时机 NEW （insert） UPDATE （update） READ （select）
     * @return GeneratorTime
     */
    GeneratorTime occasion() default GeneratorTime.NEW;

    /**
     * 生成条件 IFNULL 数值为空时生成 ALWAYS 总是生成，会覆盖已有的值
     * @return GeneratorCondition
     */
    GeneratorCondition condition() default GeneratorCondition.IFNULL;

    /**
     * 具体生成参数 对应 GeneratorType 不同有不用的意思
     * @return
     */
    String value() default "";
}
