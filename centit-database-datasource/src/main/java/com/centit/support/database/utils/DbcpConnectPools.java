package com.centit.support.database.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.centit.support.database.metadata.IDatabaseInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class DbcpConnectPools {
    private static final Logger logger = LoggerFactory.getLogger(DbcpConnectPools.class);
    private static final
    ConcurrentHashMap<DataSourceDescription, DruidDataSource> dbcpDataSourcePools
        = new ConcurrentHashMap<>();
    private DbcpConnectPools() {
        throw new IllegalAccessError("Utility class");
    }

    private static DruidDataSource mapDataSource(DataSourceDescription dsDesc) {
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName(dsDesc.getDriver());
        ds.setUsername(dsDesc.getUsername());
        ds.setPassword(dsDesc.getPassword());
        ds.setUrl(dsDesc.getConnUrl());
        ds.setInitialSize(dsDesc.getInitialSize());
        ds.setMaxActive(dsDesc.getMaxTotal());
        //ds.setMaxIdle(dsDesc.getMaxIdle());
        ds.setMaxWait(dsDesc.getMaxWaitMillis());
        ds.setMinIdle(dsDesc.getMinIdle());
        String validationQuery = DBType.getDBValidationQuery(dsDesc.getDbType());
        if(StringUtils.isNotBlank(validationQuery)) {
            ds.setValidationQuery(validationQuery);
            ds.setTestWhileIdle(true);
        }
        return ds;
    }

    public static synchronized DruidDataSource getDataSource(DataSourceDescription dsDesc) {
        DruidDataSource ds = dbcpDataSourcePools.get(dsDesc);
        if (ds == null) {
            ds = mapDataSource(dsDesc);
            dbcpDataSourcePools.put(dsDesc, ds);
        }
        return ds;
    }

    public static synchronized Connection getDbcpConnect(DataSourceDescription dsDesc) throws SQLException {
        DruidDataSource ds = getDataSource(dsDesc);
        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        ///*dsDesc.getUsername(),dsDesc.getDbType(),*/
        return conn;
    }

    public static DruidDataSource getDataSource(IDatabaseInfo dbinfo) {
        return DbcpConnectPools.getDataSource(DataSourceDescription.valueOf(dbinfo));
    }

    public static Connection getDbcpConnect(IDatabaseInfo dbinfo) throws SQLException {
        return DbcpConnectPools.getDbcpConnect(DataSourceDescription.valueOf(dbinfo));//.getConn();
    }

    /* 获得数据源连接状态 */
    public static Map<String, Object> getDataSourceStats(DataSourceDescription dsDesc) {
        DruidDataSource bds = dbcpDataSourcePools.get(dsDesc);
        if (bds == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("activeCount", bds.getActiveCount());
        map.put("poolingCount", bds.getPoolingCount());
        map.put("resetCount", bds.getResetCount());
        map.put("errorCount", bds.getErrorCount());
        map.put("discardCount", bds.getDiscardCount());
        return map;
    }

    /**
     * 关闭数据源
     */
    public static synchronized void shutdownDataSource() {
        for (Map.Entry<DataSourceDescription, DruidDataSource> dbs : dbcpDataSourcePools.entrySet()) {
            dbs.getValue().close();
        }
        //dbcpDataSourcePools.clear();
    }

    public static synchronized boolean testDataSource(DataSourceDescription dsDesc) {
        DruidDataSource ds = mapDataSource(dsDesc);
        boolean connOk = false;
        try {
            //Class.forName(dsDesc.getDriver());
            //Connection conn = DriverManager.getConnection(dsDesc.getConnUrl(),
            //dsDesc.getUsername(),dsDesc.getPassword());
            Connection conn = ds.getConnection();
            if (conn != null) {
                connOk = true;
                conn.close();
            }
            ds.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        } finally {
            ds.close();
        }
        return connOk;
    }

    public static void closeConnect(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);//e.printStackTrace();
            }
        }
    }
}
