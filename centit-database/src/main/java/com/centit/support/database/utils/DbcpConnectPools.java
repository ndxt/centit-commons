package com.centit.support.database.utils;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class DbcpConnectPools {
	private DbcpConnectPools() {
		throw new IllegalAccessError("Utility class");
	}
	protected static final Logger logger = LoggerFactory.getLogger(DbcpConnectPools.class);
	private static final
		Map<DataSourceDescription,BasicDataSource> dbcpDataSourcePools
		 = new HashMap<DataSourceDescription,BasicDataSource >();
	
	private static synchronized BasicDataSource addDataSource(DataSourceDescription dsDesc){
		
		//BasicDataSourceFactory.createDataSource(properties)
		BasicDataSource ds = new BasicDataSource();  
		ds.setDriverClassName(dsDesc.getDriver());  
		ds.setUsername(dsDesc.getUsername());  
		ds.setPassword(dsDesc.getPassword());  
		ds.setUrl(dsDesc.getConnUrl());  
		ds.setInitialSize(dsDesc.getInitialSize()); // 初始的连接数；  
		ds.setMaxTotal(dsDesc.getMaxTotal());  
		ds.setMaxIdle(dsDesc.getMaxIdle());  
		ds.setMaxWaitMillis(dsDesc.getMaxWaitMillis());  
		ds.setMinIdle(dsDesc.getMinIdle());  
		//ds.setTestOnReturn(false);
		dbcpDataSourcePools.put(dsDesc, ds);
		return ds;
	}
	
	public static synchronized BasicDataSource getDataSource(DataSourceDescription dsDesc){
		BasicDataSource ds = dbcpDataSourcePools.get(dsDesc);
		if(ds==null)
			ds = addDataSource(dsDesc);
		return ds;
	}
	
	public static synchronized Connection getDbcpConnect(DataSourceDescription dsDesc) throws SQLException{
		BasicDataSource ds = dbcpDataSourcePools.get(dsDesc);
		if(ds==null)
			ds = addDataSource(dsDesc);
		Connection conn = null;
		conn = ds.getConnection();  
        conn.setAutoCommit(false);  
        ///*dsDesc.getUsername(),dsDesc.getDbType(),*/
        return conn;
	}
	
	/* 获得数据源连接状态 */
    public static Map<String, Integer> getDataSourceStats(DataSourceDescription dsDesc){  
        BasicDataSource bds = dbcpDataSourcePools.get(dsDesc);
        if(bds==null)
        	return null;
        Map<String, Integer> map = new HashMap<String, Integer>(2);  
        map.put("active_number", bds.getNumActive());  
        map.put("idle_number", bds.getNumIdle());  
        return map;  
    }  
  
    /** 关闭数据源 */  
    public static synchronized void shutdownDataSource(){  
    	for(Map.Entry<DataSourceDescription,BasicDataSource> dbs : dbcpDataSourcePools.entrySet()){
    		try {
				dbs.getValue().close();
			} catch (SQLException e) {
				logger.error(e.getMessage(),e);//e.printStackTrace();
			}
    	}
    }  
    
    public static synchronized boolean testDataSource(DataSourceDescription dsDesc){
		BasicDataSource ds = new BasicDataSource();  
		ds.setDriverClassName(dsDesc.getDriver());  
		ds.setUsername(dsDesc.getUsername());  
		ds.setPassword(dsDesc.getPassword());  
		ds.setUrl(dsDesc.getConnUrl());  
		ds.setInitialSize(dsDesc.getInitialSize()); // 初始的连接数；  
		ds.setMaxTotal(dsDesc.getMaxTotal());  
		ds.setMaxIdle(dsDesc.getMaxIdle());  
		ds.setMaxWaitMillis(dsDesc.getMaxWaitMillis());  
		ds.setMinIdle(dsDesc.getMinIdle());
		boolean connOk = false;
		try {
			//Class.forName(dsDesc.getDriver());
			//Connection conn = DriverManager.getConnection(dsDesc.getConnUrl(),
					//dsDesc.getUsername(),dsDesc.getPassword());
			Connection conn = ds.getConnection();
			if(conn != null){
				connOk = true;
				conn.close();
			}
			ds.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);//e.printStackTrace();
		}finally {
			try {
				ds.close();
			} catch (SQLException e1) {
				logger.error(e1.getMessage(),e1);//e1.printStackTrace();
			}
		}
		return connOk;
	}
    
    public static void closeConnect(Connection conn){
    	if(conn!=null){			
			try {
				conn.close();				
			} catch (SQLException e) {
				logger.error(e.getMessage(),e);//e.printStackTrace();
			}
		}
    }
}
