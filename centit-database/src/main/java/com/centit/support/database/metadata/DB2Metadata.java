package com.centit.support.database.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;

public class DB2Metadata implements DatabaseMetadata {

    protected static final Logger logger = LoggerFactory.getLogger(DB2Metadata.class);
    private final static String sqlGetTabColumns=
        "select a.name,a.coltype,a.length, a.scale, a.nulls "+
        "from sysibm.systables b , sysibm.syscolumns a "+
        "where a.tbcreator= ? and a.tbname= ? "+
              "and b.name=a.tbname and b.creator=a.tbcreator";

    private final static String sqlPKInfo=
        "select constname, colname "+
        "from sysibm.syskeycoluse "+
        "where tbcreator=? and tbname=? "+
        "order by colseq";

    private final static String sqlFKInfo=
        "select tbname, relname, colcount, fkcolnames, pkcolnames "+
        "from sysibm.sysrels "+
        "where refkeyname= ?";

    private final static String sqlFKColumn=
        "select a.name,a.coltype,a.length, a.scale, a.nulls "+
        "from sysibm.systables b , sysibm.syscolumns a "+
        "where a.tbcreator= ? and a.tbname= ? and a.name= ? "+
              "and b.name=a.tbname and b.creator=a.tbcreator";

    private String sDBSchema ;

    private Connection dbc;

    @Override
    public void setDBConfig(Connection dbc){
        this.dbc=dbc;
    }
    public String getDBSchema() {
        return sDBSchema;
    }

    public void setDBSchema(String schema) {
        if(schema !=null)
            sDBSchema = schema.toUpperCase();
    }

    public SimpleTableInfo getTableMetadata(String tabName) {
        SimpleTableInfo tab = new SimpleTableInfo(tabName);
        PreparedStatement pStmt= null;
        ResultSet rs = null;
        try {
            tab.setSchema( dbc.getSchema().toUpperCase());
            // get columns
            pStmt= dbc.prepareStatement(sqlGetTabColumns);
            pStmt.setString(1, sDBSchema);
            pStmt.setString(2, tabName);
            rs = pStmt.executeQuery();
            while (rs.next()) {
                SimpleTableField field = new SimpleTableField();
                field.setColumnName(rs.getString("name"));
                field.setColumnType(rs.getString("coltype"));
                field.setMaxLength(rs.getInt("length"));
                field.setPrecision(field.getMaxLength());
                field.setScale(rs.getInt("scale"));
                field.setNullEnable(rs.getString("nulls"));
                field.mapToMetadata();

                tab.getColumns().add(field);
            }
            rs.close();
            pStmt.close();
            // get primary key
            pStmt= dbc.prepareStatement(sqlPKInfo);
            pStmt.setString(1, sDBSchema);
            pStmt.setString(2, tabName);
            rs = pStmt.executeQuery();
            while (rs.next()) {
                tab.setPkName(rs.getString("constname"));
                tab.getPkColumns().add(rs.getString("colname"));
            }
            rs.close();
            pStmt.close();
            // get reference info

            pStmt= dbc.prepareStatement(sqlFKInfo);
            pStmt.setString(1, tab.getPkName());
            rs = pStmt.executeQuery();
            while (rs.next()) {
                SimpleTableReference ref = new SimpleTableReference();
                ref.setParentTableName(tabName);
                ref.setTableName(rs.getString("tbname"));
                ref.setReferenceCode(rs.getString("relname"));
                int nColCount = rs.getInt("colcount");
                String sFColNames = rs.getString("fkcolnames").trim();
                String [] p = sFColNames.split("\\s+");
                String sPColNames = rs.getString("pkcolnames").trim();
                String [] pK = sPColNames.split("\\s+");
                if(nColCount != p.length){
                    System.out.println("外键"+ref.getReferenceCode()+"字段分隔出错！");
                }
                for(int i=0;i<p.length;i++){
                    SimpleTableField field = new SimpleTableField();
                    field.setColumnName(p[i]);
                    ref.getFkColumns().add(field);
                    if(i<pK.length)
                        ref.getReferenceColumns().put(pK[i], p[i]);
                }
                tab.getReferences().add(ref );
            }
            rs.close();
            pStmt.close();
            // get reference detail
            for(Iterator<SimpleTableReference> it= tab.getReferences().iterator();it.hasNext(); ){
                SimpleTableReference ref = it.next();
                for(Iterator<SimpleTableField> it2= ref.getFkColumns().iterator();it2.hasNext(); ){
                    SimpleTableField field = it2.next();
                    pStmt= dbc.prepareStatement(sqlFKColumn);
                    pStmt.setString(1,sDBSchema);
                    pStmt.setString(2,ref.getTableName());
                    pStmt.setString(3,field.getColumnName());
                    rs = pStmt.executeQuery();
                    if (rs.next()) {
                        field.setColumnType(rs.getString("coltype"));
                        field.setMaxLength(rs.getInt("length"));
                        field.setPrecision(field.getMaxLength());
                        field.setScale(rs.getInt("scale"));
                        field.setNullEnable(rs.getString("nulls"));
                        field.mapToMetadata();
                    }
                    rs.close();
                    pStmt.close();
                }
            }
            //conn.close();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        } finally{
            try{
                if(pStmt!=null)
                    pStmt.close();
                if(rs!=null)
                    rs.close();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);//e.printStackTrace();
            }
        }
        return tab;
    }

}
