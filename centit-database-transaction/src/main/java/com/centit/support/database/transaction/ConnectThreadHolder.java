package com.centit.support.database.transaction;

import com.centit.support.database.metadata.IDatabaseInfo;
import com.centit.support.database.utils.DataSourceDescription;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectThreadHolder{
    private static ConnectThreadLocal threadLocal = new ConnectThreadLocal();

    private ConnectThreadHolder() {
        super();
    }

    public static void setConnectThreadWrapper(ConnectThreadWrapper wrapper) {
        threadLocal.set(wrapper);
    }

    public static ConnectThreadWrapper getConnectThreadWrapper() {
        ConnectThreadWrapper wrapper = threadLocal.get();
        if(wrapper == null){
            wrapper = new ConnectThreadWrapper();
            threadLocal.set(wrapper);
        }
        return wrapper;
    }

    public static Connection fetchConnect(DataSourceDescription description) throws SQLException{
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        return wrapper.fetchConnect(description);
    }

    public static Connection fetchConnect(IDatabaseInfo description) throws SQLException{
        return fetchConnect(DataSourceDescription.valueOf(description));
    }

    public static void commit() throws SQLException{
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        wrapper.commitAllWork();
    }

    public static void rollback() throws SQLException{
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        wrapper.rollbackAllWork();
    }

    public static void release(){
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        wrapper.releaseAllConnect();
        threadLocal.superRemove();
    }

    public static void commitAndRelease() throws SQLException{
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        try {
            wrapper.commitAllWork();
        } finally {
            wrapper.releaseAllConnect();
            threadLocal.superRemove();
        }
    }

    public static void rollbackAndRelease() throws SQLException{
        ConnectThreadWrapper wrapper = getConnectThreadWrapper();
        try {
            wrapper.rollbackAllWork();
        } finally {
            wrapper.releaseAllConnect();
            threadLocal.superRemove();
        }
    }
}
