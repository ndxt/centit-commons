package com.centit.test;

import com.alibaba.fastjson.JSONArray;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.DbcpConnectPools;
import com.centit.support.database.utils.QueryUtils;

import java.sql.Connection;

public class TestJDBC {

    public  static void  main(String[] args)   {
        //System.out.println(DBType.valueOf("Oracle"));
        testJDBCMetadata();
    }
  
    public  static void testJDBCMetadata(){
         DataSourceDescription dbc = new DataSourceDescription();
          dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
          dbc.setUsername("fdemo2");
          dbc.setPassword("fdemo2");
          try {
            Connection conn= DbcpConnectPools.getDbcpConnect(dbc);
            JSONArray ja = DatabaseAccess.fetchResultSetToJSONArray(
                    conn.getMetaData().getTables(null, "FDEMO2","F_USERINFO", null) , null);
            System.out.println(ja.toJSONString());

            ja = DatabaseAccess.fetchResultSetToJSONArray(
                    conn.getMetaData().getColumns(null, "FDEMO2", "F_USERINFO", null), null);
            System.out.println(ja.toJSONString());

            System.out.println(conn.getMetaData().getDatabaseProductName());
            System.out.println(conn.getMetaData().getDriverName());
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("done!");
    }

    public  static void testDataSource(){
        DataSourceDescription dbc = new DataSourceDescription();
        dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
        dbc.setUsername("fdemo2");
        dbc.setPassword("fdemo2");
        /* String sql = "select loginName,userName from f_userinfo " +
                "where [:(creepforin)userCodes| usercode in (:userCodes)]";*/

        String sql = "select loginName,userName from f_userinfo " +
                "where usercode in (:userCodes)";
        /*QueryAndParams qp = QueryAndParams.createFromQueryAndNamedParams(sql,
             QueryUtils.createSqlParamsMap("userCodes",new Object[]{"U0000041","U0001013"}));*/
        try {
             Connection conn= DbcpConnectPools.getDbcpConnect(dbc);
             JSONArray ja = DatabaseAccess.findObjectsByNamedSqlAsJSON(conn, sql,  QueryUtils.createSqlParamsMap("userCodes",new Object[]{"U0000041","U0001013"}));
             conn.close();
             System.out.println(ja.toJSONString());
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
}
