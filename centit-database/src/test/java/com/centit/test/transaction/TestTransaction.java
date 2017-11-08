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
        /**
         * 假设这个对象是你要保存的; 如果调用 OrmDaoUtils.saveNewObject 成功，这个对象上必须有jpa注解
         * 有jpa注解就不用自己写sql语句了，否则自己写insert语句也是可以的
         */
        Object userInfo = new Object();
        try {
            Integer ret = TransactionHandler.executeInTransaction(dbc, (conn) -> {
                /**
                 * 这两个操作是在一个事物中的
                 */
                DatabaseAccess.doExecuteSql(conn, "delete from table where a=? and b=?",
                        new Object[]{"a",5});
                return OrmDaoUtils.saveNewObject(conn, userInfo);
            });
            System.out.println(ret);
        }catch (SQLException e){
            System.out.println(e.getLocalizedMessage());
        }
    }

}
