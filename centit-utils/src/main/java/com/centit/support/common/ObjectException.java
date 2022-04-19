package com.centit.support.common;


import org.apache.commons.lang3.StringUtils;

/**
 * An exception that is thrown by classes wanting to trap unique
 * constraint violations.  This is used to wrap Spring's
 * DataIntegrityViolationException so it's checked in the web layer.
 *
 * @author <a href="mailto:codefan@sina.com">codefan</a>
 */
public class ObjectException extends RuntimeException {
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
    private static final long serialVersionUID = 4050482305178810162L;
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

    public static String extortExceptionTraceMessage(Throwable ex) {
        return extortExceptionTraceMessage(ex, 15);
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
