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
     *   Auto 数据库自动增长、 SEQUENCE 序列 value中保存序列名称 、
     *   UUID 、 CONSTANT 常量 value中保存常量、
     *   FUNCTION 公式 value中保存公式，是一个四则运算表达式，可以通过变量引用这个对象的其他属性
     * @return GeneratorType
     */
    GeneratorType strategy() default GeneratorType.AUTO;

    /**
     * 数值生成时机 NEW （insert） UPDATE （update） READ （select）
     * @return GeneratorTime
     */
    GeneratorTime occasion() default GeneratorTime.NEW_UPDATE;

    /**
     * 生成条件 IFNULL 数值为空时生成 ALWAYS 总是生成，会覆盖已有的值
     * @return GeneratorCondition
     */
    GeneratorCondition condition() default GeneratorCondition.IFNULL;

    /**
     * 具体生成参数 对应 GeneratorType 不同有不用的意思
     * @return 具体生成参数
     */
    String value() default "";
}
