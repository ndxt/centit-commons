package com.centit.support.database.transaction;

import com.centit.support.database.utils.DataSourceDescription;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectThreadHolder extends ThreadLocal<ConnectThreadWrapper> {
    private static ThreadLocal<ConnectThreadWrapper> threadLocal = new ThreadLocal<>();

    private ConnectThreadHolder() {
        super();
    }

    public static void setConnectThreadWrapper(ConnectThreadWrapper wrapper) {
        threadLocal.set(wrapper);
    }

    public static ConnectThreadWrapper getConnectThreadWrapper() {
        ConnectThreadWrapper wrapper = threadLocal.get();
        if(wrapper==null){
            wrapper = new ConnectThreadWrapper();
            threadLocal.set(wrapper);
        }
        return wrapper;
    }

    public static Connection fetchConnect(DataSourceDescription description) throws SQLException{
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        return wrapper.fetchConnect(description);
    }

    public static void commit() throws SQLException{
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        wrapper.commitAllWork();
    }

    public static void release(){
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        wrapper.releaseAllConnect();
    }

    public static void commitAndRelease() throws SQLException{
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        try {
            wrapper.commitAllWork();
        } finally {
            wrapper.releaseAllConnect();
        }
    }
}
