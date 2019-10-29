package com.centit.support.database.utils;

import com.centit.support.common.ObjectException;

import java.io.IOException;
import java.sql.SQLException;

@SuppressWarnings("unused")
public class PersistenceException extends ObjectException {

    private static final long serialVersionUID = 4050482305178810162L;

    public static final int DATABASE_OPERATE_EXCEPTION = 620;
    public static final int DATABASE_OUT_SYNC_EXCEPTION = 621;

    public static final int DATABASE_SQL_EXCEPTION = 622;
    public static final int DATABASE_IO_EXCEPTION = 623;
    public static final int NOSUCHFIELD_EXCEPTION = 624;
    public static final int INSTANTIATION_EXCEPTION = 625;
    public static final int ILLEGALACCESS_EXCEPTION = 626;
    public static final int ORM_METADATA_EXCEPTION  = 627;
    /**
     * Constructor for UserExistsException.
     * @param exceptionCode 异常码
     * @param message 异常信息
     * @param exception 异常信息
     */
    public PersistenceException(int exceptionCode, String message, Throwable exception) {
        super(message,exception);
        this.exceptionCode = exceptionCode;
    }

    /**
     * Constructor for UserExistsException.
     * @param exceptionCode 异常码
     * @param message 异常信息
     */
    public PersistenceException(int exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    /**
     *
     * @param exceptionCode 异常码
     * @param exception 异常信息
     */
    public PersistenceException(int exceptionCode, Throwable exception) {
        super(exception);
        this.exceptionCode = exceptionCode;
    }

    /**
     *
     * @param exception Throwable
     */
    public PersistenceException(Throwable exception) {
        super(exception);
        this.exceptionCode = UNKNOWN_EXCEPTION;
    }

    /**
     *
     * @param exception Throwable
     */
    public PersistenceException(SQLException exception) {
        super(exception);
        this.exceptionCode = DATABASE_SQL_EXCEPTION;
    }


    public PersistenceException(String sql , SQLException e){
        super(sql +" raise "+ e.getMessage(), e.getCause());
        //this.setNextException(e.getNextException());
        //this.setStackTrace(e.getStackTrace());
        this.exceptionCode = DATABASE_SQL_EXCEPTION;
    }

    /**
     *
     * @param exception Throwable
     */
    public PersistenceException(IOException exception) {
        super(exception);
        this.exceptionCode = DATABASE_IO_EXCEPTION;
    }
    /**
     *
     * @param message String
     */
    public PersistenceException(String message) {
        super(message);
        this.exceptionCode = UNKNOWN_EXCEPTION;
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

}
