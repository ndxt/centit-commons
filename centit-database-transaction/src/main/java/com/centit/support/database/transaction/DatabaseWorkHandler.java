package com.centit.support.database.transaction;

import com.centit.support.database.utils.DataSourceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DatabaseWorkHandler {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseWorkHandler.class);

    private DatabaseWorkHandler() {
        throw new IllegalAccessError("Utility class");
    }

    public static <T> T executeInTransaction(DataSourceDescription dataSourceDesc, ExecuteWork<T> realWork)
        throws SQLException {
        Connection conn = ConnectThreadHolder.fetchConnect(dataSourceDesc);
        return realWork.execute(conn);
    }

    public static <T> T executeQueryInTransaction(DataSourceDescription dataSourceDesc, QueryWork<T> realWork)
        throws SQLException, IOException {
        Connection conn = ConnectThreadHolder.fetchConnect(dataSourceDesc);
        return realWork.execute(conn);
    }

    public interface ExecuteWork<T> {
        T execute(Connection conn) throws SQLException;
    }


    public interface QueryWork<T> {
        T execute(Connection conn) throws SQLException, IOException;
    }

}
