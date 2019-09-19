package com.centit.support.database.transaction;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Aspect
@Component
public class JdbcTransactionAspect {

    protected static final Logger logger = LoggerFactory.getLogger(JdbcTransactionAspect.class);

    /**
     * 注册 注入点
     */
    @Pointcut("@annotation(com.centit.support.database.transaction.JdbcTransaction)")
    public void transactionAspect(){}

    /**
     * 执行错误时记录错误日志
     * @param joinPoint joinPoint 切入点
     * @param transaction  JdbcTransaction 注解
     * @param ex 如果为null没有异常说明执行成功，否在记录异常信息
     */
    @AfterThrowing(pointcut = "transactionAspect() && @annotation(transaction)", throwing = "ex")
    public void doAfterThrowing(JoinPoint joinPoint, JdbcTransaction transaction, Throwable ex) {
        if (ex instanceof RuntimeException){
            try {
                ConnectThreadHolder.rollbackAndRelease();
            } catch (SQLException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * 正常完成时记录日志
     * @param joinPoint joinPoint 切入点
     * @param transaction  JdbcTransaction 注解
     * param returningValue Object 函数返回的结果
     */
    @AfterReturning(pointcut = "transactionAspect() && @annotation(transaction)")
    public void doAfterReturning(JoinPoint joinPoint, JdbcTransaction transaction) {
        try {
            ConnectThreadHolder.commitAndRelease();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage());
        }
    }
}
