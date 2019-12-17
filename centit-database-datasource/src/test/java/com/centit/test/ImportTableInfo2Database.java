package com.centit.test;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.SimpleTableInfo;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.DbcpConnectPools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.util.List;

public class ImportTableInfo2Database {

    public static void main(String[] args) {
        DataSourceDescription dbc = new DataSourceDescription();
        dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
        dbc.setUsername("metaform");
        dbc.setPassword("metaform");
        String pdmFilePath =
            "D:/Projects/framework3/centit-meta-form/document/内部经营数据管理系统文档/公司经营数据管理（一期）物理模型设计.pdm";

        List<Pair<String, String>> tables = PdmTableInfo.listTablesInPdm(pdmFilePath);
        if (tables == null) {
            System.out.println("读取文件出错!");
            return;
        }
        try {
            Connection conn = DbcpConnectPools.getDbcpConnect(dbc);
            for (Pair<String, String> t : tables) {
                try {
                    Long tableId = NumberBaseOpt.castObjectToLong(DatabaseAccess.getScalarObjectQuery(
                        conn,
                        "SELECT SEQ_PENDINGTABLEID.nextval from dual"));
                    SimpleTableInfo metaTable = PdmTableInfo.importTableFromPdm(pdmFilePath, t.getLeft());
                    DatabaseAccess.doExecuteSql(conn, "insert into F_PENDING_META_TABLE"
                            + "(Table_ID,Database_Code,Table_Name,Table_Label_Name,table_type,table_state,table_Comment) "
                            + "values(?,'1',?,?,'T','N',?)",
                        new Object[]{tableId, metaTable.getTableName(), metaTable.getTableLabelName(),
                            StringUtils.substring(metaTable.getTableComment(), 0, 120)});
                    for (SimpleTableField col : metaTable.getColumns()) {
                        DatabaseAccess.doExecuteSql(conn, "insert into F_PENDING_META_COLUMN"
                                + "(Table_ID,column_Name,field_Label_Name,column_Type,access_type,"
                                + "max_Length,scale,column_state,column_Comment) "
                                + "values(?,?,?,?,'N', ?,?,'N',?)",
                            new Object[]{tableId, col.getColumnName(), col.getFieldLabelName(), col.getColumnType(),
                                col.getMaxLength(), col.getScale(), StringUtils.substring(col.getColumnComment(), 0, 120)});
                    }
                    conn.commit();
                    System.out.println("导入表:" + t.getRight() + "(" + t.getLeft() + ")成功!");
                } catch (Exception e) {
                    conn.rollback();
                    System.out.println("导入表:" + t.getRight() + "(" + t.getLeft() + ")失败!" + e.getMessage());
                    e.printStackTrace();
                }
            }
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("done!");
    }

}
