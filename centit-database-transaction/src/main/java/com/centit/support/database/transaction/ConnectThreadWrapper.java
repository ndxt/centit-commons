package com.centit.support.database.transaction;

import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DbcpConnectPools;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectThreadWrapper implements Serializable {
    private final
    Map<DataSourceDescription, Connection> connectPools;

    public ConnectThreadWrapper() {
        this.connectPools = new ConcurrentHashMap<>(4);
    }

    public Connection fetchConnect(DataSourceDescription description) throws SQLException {
        Connection conn = connectPools.get(description);
        if (conn == null) {
            conn = DbcpConnectPools.getDbcpConnect(description);
            connectPools.put(description, conn);
        }
        return conn;
    }

    public void commitAllWork() throws SQLException {
        if (connectPools.size() == 0) {
            return;
        }
        for (Connection conn : connectPools.values()) {
            conn.commit();
        }
    }

    public void rollbackAllWork() throws SQLException {
        if (connectPools.size() == 0) {
            return;
        }
        for (Connection conn : connectPools.values()) {
            conn.rollback();
        }
    }

    public void releaseAllConnect() {
        if (connectPools.size() == 0) {
            return;
        }
        for (Connection conn : connectPools.values()) {
            DbcpConnectPools.closeConnect(conn);
        }
        connectPools.clear();
    }
}

