package com.centit.test;

import com.alibaba.fastjson.JSONObject;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.DbcpConnectPools;
import com.centit.support.database.utils.TransactionHandler;

import java.io.IOException;
import java.sql.*;

public class TestOraClob {

    public static void main(String[] args) {
        testFetchClob();
    }

    public static void testFetchClob() {
        DataSourceDescription dbc = new DataSourceDescription();
        dbc.setConnUrl("jdbc:oracle:thin:@192.168.137.95:1521:orcl");
        dbc.setUsername("framework");
        dbc.setPassword("framework");

        try {
            /*byte [] lobData = FileIOOpt.readBytesFromFile(
                new File("/D/Projects/RunData/demo_home/temp/XDocReport2.docx")
            );*/
            TransactionHandler.executeInTransaction(dbc, (conn) -> {
                JSONObject json = null;
                try {
                    json = DatabaseAccess.getObjectAsJSON(conn,
                        "select ID, NAME, LOBCOL from apprflow.TEST_LOB where id='OB'" );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return DatabaseAccess.doExecuteSql(conn,
                    "insert into apprflow.TEST_LOB ( ID, NAME, LOBCOL) values(?,?,?)",
                    new Object[]{"02","second", json.get("lobcol")});
            });
            System.out.println("down");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void testCentitLob() {
        DataSourceDescription dbc = new DataSourceDescription();

        dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
        dbc.setUsername("fdemo2");
        dbc.setPassword("fdemo2");

        try (
            Connection conn = DbcpConnectPools.getDbcpConnect(dbc);
            PreparedStatement pStmt = conn.prepareStatement(
                "select NO, internal_no,item_id,STUFF , length(stuff),CENTIT_LOB.ClobToBlob(stuff) as bstuff " +
                    "from inf_apply where  no='JS000000HD0000000481' ");
            ResultSet rs = pStmt.executeQuery()) {
            if (rs.next()) {
                Clob stuff = rs.getClob("STUFF");
                Blob bstuff = rs.getBlob("bstuff");
                String internal_no = rs.getString("internal_no");
                String item_id = rs.getString("item_id");
                try (PreparedStatement pStmt2 = conn.prepareStatement(
                    "begin DataTranslate.InsertAnnex(?,?,?); end;")) {

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
