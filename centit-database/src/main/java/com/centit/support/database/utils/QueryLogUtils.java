package com.centit.support.database.utils;

import com.alibaba.fastjson.JSON;
import com.centit.support.algorithm.DatetimeOpt;
import org.slf4j.Logger;

public abstract class QueryLogUtils {

    public static boolean jdbcShowSql = false;
    public static boolean userLog4jInfo = true;
    private QueryLogUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static void setUserLog4j(boolean userLog4j) {
        QueryLogUtils.userLog4jInfo = userLog4j;
    }

    public static void setJdbcShowSql(boolean jdbcShowSql) {
        QueryLogUtils.jdbcShowSql = jdbcShowSql;
    }

    public static void printSql(Logger logger, String sql) {
        if (jdbcShowSql) {
            if (userLog4jInfo) {
                logger.info(sql);
            } else {
                System.out.println(DatetimeOpt.currentDatetime()
                    + " 语句： " + sql);
            }
        }
    }

    public static void printSql(Logger logger, String sql, Object param) {
        if (jdbcShowSql) {
            if (userLog4jInfo) {
                if (param != null) {
                    logger.info(sql + ":" + JSON.toJSONString(param));
                } else {
                    logger.info(sql);
                }
            } else {
                if (param != null) {
                    System.out.println(DatetimeOpt.currentDatetime() + " 语句： " +
                        sql + "参数为： " + JSON.toJSONString(param));
                } else {
                    System.out.println(DatetimeOpt.currentDatetime() + " 语句： " +
                        sql);
                }
            }
        }
    }

}
