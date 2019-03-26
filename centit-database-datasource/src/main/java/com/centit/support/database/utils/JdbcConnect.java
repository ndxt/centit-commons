package com.centit.support.database.utils;

import com.centit.support.database.metadata.IDatabaseInfo;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("unused")
public abstract class JdbcConnect {
    private JdbcConnect() {
        throw new IllegalAccessError("Utility class");
    }

    public static DataSourceDescription mapDataSource(IDatabaseInfo dbinfo){
        DataSourceDescription desc=new DataSourceDescription();
        desc.setConnUrl(dbinfo.getDatabaseUrl());
        desc.setUsername(dbinfo.getUsername());
        desc.setPassword(dbinfo.getClearPassword());
        desc.setDatabaseCode(dbinfo.getDatabaseCode());
        desc.setMaxIdle(10);
        desc.setMaxTotal(20);
        desc.setMaxWaitMillis(20000);
        return desc;
    }

    public static Connection getConn(IDatabaseInfo dbinfo) throws SQLException {
        return DbcpConnectPools.getDbcpConnect(mapDataSource(dbinfo));//.getConn();
    }
}
