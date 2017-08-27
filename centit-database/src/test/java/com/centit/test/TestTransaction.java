package com.centit.test;

import java.sql.SQLException;

import com.alibaba.fastjson.JSONObject;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DbcpConnect;
import com.centit.support.database.utils.DbcpConnectPools;
import com.centit.test.transaction.TransactionInvocationHandler;

public class TestTransaction {
	public  static void  main(String[] args)   {
		 testJDBCMetadata();
	}
 
	public  static void testJDBCMetadata(){
		DataSourceDescription dbc = new DataSourceDescription();	  
		dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
		dbc.setUsername("fdemo2");
		dbc.setPassword("fdemo2");
		try {
			DbcpConnect conn= DbcpConnectPools.getDbcpConnect(dbc);
			JSONObject object = new JSONObject();
			object.put("id", 3);
			object.put("userName", "yang huaisheng");
			object.put("userPhone", "18602554255");
			//TestService test = new TestServiceImpl();
			TestService test  = (TestService)
					TransactionInvocationHandler.getProxyInstanceFactory(new TestServiceImpl());
			test.insertUser(conn, object);
			conn.close();
		} catch (SQLException e) {
			//e.printStackTrace();
		}
	    System.out.println("done!");
	}	
}
