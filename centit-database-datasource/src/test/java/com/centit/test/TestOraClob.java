package com.centit.test;

import com.alibaba.fastjson.JSONArray;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.DbcpConnectPools;

import java.sql.*;

public class TestOraClob {

  public  static void  main(String[] args)   {
      testFetchClob();
  }
  public  static void testFetchClob(){
      DataSourceDescription dbc = new DataSourceDescription();
      dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
      dbc.setUsername("fdemo2");
      dbc.setPassword("fdemo2");

      try {
          Connection conn= DbcpConnectPools.getDbcpConnect(dbc);
        String sSql =
        "select T.VC_ID as id, T.VC_DUETYPE as vcDuetype, T.VC_OPINION as vcOpinion "+
        "from WP_REQUEST_DEP T "+
        "where T.VC_ID in (select min(B.VC_ID) "+
                    " FROM WP_REQUEST A "+
                    "INNER JOIN WP_REQUEST_DEP B "+
                       "ON B.VC_REQUEST_ID = A.VC_REQUEST_ID "+
                    "WHERE A.VC_PROJECT_ID = '402808ec535fda5a0153600660950004' "+
                    "group by B.VC_DUETYPE)";
        JSONArray ja = DatabaseAccess.findObjectsAsJSON(conn, sSql,null,null);
        conn.close();
        System.out.println(ja.toJSONString());
    } catch (Exception e) {
        //e.printStackTrace();
    }

   }

  public  static void testCentitLob(){
      DataSourceDescription dbc = new DataSourceDescription();

      dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
      dbc.setUsername("fdemo2");
      dbc.setPassword("fdemo2");

      try (
          Connection conn= DbcpConnectPools.getDbcpConnect(dbc);
        PreparedStatement pStmt= conn.prepareStatement(
        "select NO, internal_no,item_id,STUFF , length(stuff),CENTIT_LOB.ClobToBlob(stuff) as bstuff " +
        "from inf_apply where  no='JS000000HD0000000481' ");
          ResultSet rs = pStmt.executeQuery()){
            if (rs.next()) {
                Clob stuff = rs.getClob("STUFF");
                Blob bstuff = rs.getBlob("bstuff");
                String internal_no = rs.getString("internal_no");
                String item_id = rs.getString("item_id");
                try(PreparedStatement pStmt2= conn.prepareStatement(
                        "begin DataTranslate.InsertAnnex(?,?,?); end;")){

                    pStmt2.setClob(1, stuff);
                    pStmt2.setString(2, internal_no);
                    pStmt2.setString(3, item_id);

                    pStmt2.execute();
                }
                System.out.println("Clob len :" + stuff.length());//12560267
                System.out.println("Blob len :" + bstuff.length());
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
   }

}
