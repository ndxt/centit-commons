package com.centit.test.transaction;

import com.alibaba.fastjson.JSONObject;
import com.centit.support.database.orm.OrmDaoUtils;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.DbcpConnectPools;
import com.centit.support.database.utils.TransactionHandler;

import java.sql.SQLException;

public class TestTransaction {
    public  static void  main(String[] args)   {
        runInTransaction();
    }
 
    public  static void testJDBCMetadata(){
        DataSourceDescription dbc = new DataSourceDescription();
        dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
        dbc.setUsername("fdemo2");
        dbc.setPassword("fdemo2");
        try {
            DbcpConnect conn = new DbcpConnect(
                    DbcpConnectPools.getDbcpConnect(dbc));
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

    public  static void runInTransaction()  {
        DataSourceDescription dbc = new DataSourceDescription();
        dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
        dbc.setUsername("fdemo2");
        dbc.setPassword("fdemo2");
        Object userInfo = new Object();
        try {
            Integer ret = TransactionHandler.executeInTransaction(dbc, (conn) -> {
                DatabaseAccess.doExecuteSql(conn, "delete * from table");
                return OrmDaoUtils.saveNewObject(conn, userInfo);
            });
            System.out.println(ret);
        }catch (SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
    }

}
