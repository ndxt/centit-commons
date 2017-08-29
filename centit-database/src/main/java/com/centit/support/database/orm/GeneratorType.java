package com.centit.support.database.orm;

/**
 * Created by codefan on 17-8-29.
 */
public enum GeneratorType {

    SEQUENCE, //数据库序列

    UUID, //uuid 32bit

    CONSTANT, //常量

    FUNCTIION // 函数，比如 当前日期
}
