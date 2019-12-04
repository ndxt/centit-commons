package com.centit.support.database.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class TransactionHandler {

    private TransactionHandler() {
        throw new IllegalAccessError("Utility class");
    }

    private static final Logger logger = LoggerFactory.getLogger(TransactionHandler.class);

    public interface TransactionWork<T> {
        T execute(Connection conn) throws SQLException;
    }

    public interface QueryWork<T> {
        T execute(Connection conn) throws SQLException, IOException;
    }

    public static <T> T executeInTransaction(DataSourceDescription dataSourceDesc, TransactionWork<T> realWork)
        throws SQLException {
        Connection conn = DbcpConnectPools.getDbcpConnect(dataSourceDesc);
        try {
            return executeInTransaction(conn, realWork);
        } finally {
            DbcpConnectPools.closeConnect(conn);
        }
    }

    public static <T> T executeInTransaction(Connection conn, TransactionWork<T> realWork)
            throws SQLException {
        try{
            T relRet = realWork.execute(conn);
            conn.commit();
            return relRet;
        } catch (SQLException e){
            logger.error("error code :" + e.getSQLState() + e.getLocalizedMessage(),e);
            conn.rollback();
            throw e;
        }
    }

    public static <T> T executeQueryInTransaction(DataSourceDescription dataSourceDesc, QueryWork<T> realWork)
        throws SQLException, IOException {
        Connection conn = DbcpConnectPools.getDbcpConnect(dataSourceDesc);
        try {
            return executeQueryInTransaction(conn, realWork);
        } finally {
            DbcpConnectPools.closeConnect(conn);
        }
    }

    public static <T> T executeQueryInTransaction(Connection conn, QueryWork<T> realWork)
        throws SQLException, IOException {
        try{
            return realWork.execute(conn);
            //conn.commit();
            //return relRet;
        } catch (SQLException e){
            logger.error("error code :" + e.getSQLState() + e.getLocalizedMessage(),e);
            //conn.rollback();
            throw e;
        }
    }
}
