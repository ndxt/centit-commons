package com.centit.support.database.orm;

/**
 * Created by codefan on 17-8-29.
 */
public enum GeneratorType {
    /**
     *  这个其实什么都不做，就是依赖数据库自动增长
     */
    AUTO,
    /**
     * 数据库序列, 序列名称保存在value中
     */
    SEQUENCE,
    /**
     * uuid 32bit
     */
    UUID,
    /**
     * uuid 22bit base64 编码
     */
    UUID22,
    /**
     * 常量 , 保存在value中
     */
    CONSTANT,
    /**
     *  函数，比如 当前日期
     *  这个调用compiler中的表达式运行，可以将同一个对象中的其他字段作为参数
     */
    FUNCTION,
    /**
     * 流水号； 代码（sequence）：模板 function  no:序列号
     */
    LSH
}
