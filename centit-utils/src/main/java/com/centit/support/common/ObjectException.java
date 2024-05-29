package com.centit.support.common;


import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;

/**
 * An exception that is thrown by classes wanting to trap unique
 * constraint violations.  This is used to wrap Spring's
 * DataIntegrityViolationException so it's checked in the web layer.
 *
 * @author <a href="mailto:codefan@sina.com">codefan</a>
 */
public class ObjectException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public static final int UNKNOWN_EXCEPTION = 601;
    public static final int NULL_EXCEPTION = 602;
    public static final int BLANK_EXCEPTION = 603;
    public static final int DATA_NOT_FOUND_EXCEPTION = 604;
    public static final int EMPTY_RESULT_EXCEPTION = 605;
    public static final int FORMAT_DATE_EXCEPTION = 606;
    public static final int FORMAT_NUMBER_EXCEPTION = 607;
    public static final int FORMULA_GRAMMAR_ERROE = 608;
    //业务逻辑错误
    public static final int LOGICAL_RULE_ERROE = 609;
    //数据不完整
    public static final int DATA_NOT_INTEGRATED = 610;
    //数据校验不通过，不合法
    public static final int DATA_VALIDATE_ERROR = 611;
    //系统配置错误
    public static final int SYSTEM_CONFIG_ERROR = 612;
    // 不支持的方法
    public static final int FUNCTION_NOT_SUPPORT = 613;
    // 参数配置/设置不正确。
    public static final int PARAMETER_NOT_CORRECT = 614;
    // 运行函数不正确。
    public static final int RUN_FUNC_NOT_CORRECT = 615;
    // 系统调用不正确。
    public static final int SYSTEM_CALL_NOT_CORRECT = 615;

    public static final int DATABASE_OPERATE_EXCEPTION = 620;
    public static final int DATABASE_OUT_SYNC_EXCEPTION = 621;
    public static final int DATABASE_SQL_EXCEPTION = 622;
    public static final int DATABASE_IO_EXCEPTION = 623;
    public static final int NOSUCHFIELD_EXCEPTION = 624;
    public static final int INSTANTIATION_EXCEPTION = 625;
    public static final int ILLEGALACCESS_EXCEPTION = 626;
    public static final int ORM_METADATA_EXCEPTION = 627;

    protected int exceptionCode;
    private Object objectData;
    /**
     * Constructor for UserExistsException.
     *
     * @param exceptionCode 异常码
     * @param message       异常信息
     */
    public ObjectException(int exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    /**
     * @param exceptionCode 异常码
     * @param exception     异常信息
     */
    public ObjectException(int exceptionCode, Throwable exception) {
        super(exception);
        this.exceptionCode = exceptionCode;
    }

    /**
     * Constructor for UserExistsException.
     *
     * @param exceptionCode 异常码
     * @param message       异常信息
     * @param exception     异常信息
     */
    public ObjectException(int exceptionCode, String message, Throwable exception) {
        super(message, exception);
        this.exceptionCode = exceptionCode;
    }

    /**
     * @param exception Throwable
     */
    public ObjectException(Throwable exception) {
        super(exception);
        this.exceptionCode = UNKNOWN_EXCEPTION;
    }

    /**
     * @param exception SQLException
     */
    public ObjectException(SQLException exception) {
        this(DATABASE_SQL_EXCEPTION, exception);
    }


    public ObjectException(String sql, SQLException e) {
        this(DATABASE_SQL_EXCEPTION, sql + " raise " + e.getMessage(), e.getCause());
    }

    /**
     * @param message String
     */
    public ObjectException(String message) {
        super(message);
        this.exceptionCode = UNKNOWN_EXCEPTION;
    }

    /**
     * @param obj           Object
     * @param exceptionCode 异常码
     * @param message       异常信息
     */
    public ObjectException(Object obj, int exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
        this.objectData = obj;
    }

    /**
     * @param obj           Object
     * @param exceptionCode 异常码
     * @param exception     异常信息
     */
    public ObjectException(Object obj, int exceptionCode, Throwable exception) {
        super(exception);
        this.exceptionCode = exceptionCode;
        this.objectData = obj;
    }

    /**
     * @param obj     Object
     * @param message String 异常信息
     */
    public ObjectException(Object obj, String message) {
        super(message);
        this.exceptionCode = UNKNOWN_EXCEPTION;
        this.objectData = obj;
    }

    /**
     * @param obj       Object
     * @param exception Throwable
     */
    public ObjectException(Object obj, Throwable exception) {
        super(exception);
        this.exceptionCode = UNKNOWN_EXCEPTION;
        this.objectData = obj;
    }

    public static String extortExceptionOriginMessage(Throwable ex){
        String originErrMessage = ex.getMessage();
        return StringUtils.isBlank(originErrMessage) ?
            "未知错误("+ex.getClass().getName()+")。":originErrMessage;
    }

    public static String extortExceptionTraceMessage(Throwable ex, int maxStacks) {
        StringBuilder errorMsg = new StringBuilder(2048);
        StackTraceElement[] traces = ex.getStackTrace();
        if (traces != null) {
            int len = traces.length > maxStacks ? maxStacks : traces.length;
            for (int i = 0; i < len; i++) {
                errorMsg.append(traces[i].toString()).append("\r\n");
                /*.append("class: ").append(traces[i].getClassName()).append(",")
                .append("method: ").append(traces[i].getMethodName()).append(",")
                .append("line: ").append(traces[i].getLineNumber()).append(".");*/
            }
        }
        return errorMsg.toString();
    }

    public static String extortExceptionMessage(Throwable ex, int maxStacks) {
        return extortExceptionOriginMessage(ex) +"\r\n"
            + extortExceptionTraceMessage(ex, maxStacks);
    }


    public static String extortExceptionTraceMessage(Throwable ex) {
        return extortExceptionTraceMessage(ex, 15);
    }


    public static String extortExceptionMessage(Throwable ex) {
        return extortExceptionMessage(ex, 15);
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public Object getObjectData() {
        return objectData;
    }

    public void setObjectData(Object objectData) {
        this.objectData = objectData;
    }

}
