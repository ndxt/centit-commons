package com.centit.support.database.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JdbcMetadata implements DatabaseMetadata {
    protected static final Logger logger = LoggerFactory.getLogger(JdbcMetadata.class);
    private Connection dbc;

    @Override
    public void setDBConfig(Connection dbc) {
        this.dbc = dbc;
    }


    public List<SimpleTableInfo> listAllTable() {
        List<SimpleTableInfo> tables = new ArrayList<>(100);
        try {

            DatabaseMetaData dbmd = dbc.getMetaData();

            ResultSet rs = dbmd.getTables(null, dbc.getSchema(), null, null);
            if(rs.next()) {
                SimpleTableInfo tab = new SimpleTableInfo();
                tab.setSchema(dbc.getSchema().toUpperCase());
                tab.setTableName(rs.getString("TABLE_NAME") );
                tab.setTableLabelName(rs.getString("REMARKS"));
                String tt = rs.getString("TABLE_TYPE");
                if("view".equalsIgnoreCase(tt) || "table".equalsIgnoreCase(tt) ) {
                    fetchTableDetail(tab, dbmd);
                    tables.add(tab);
                }
            }
            rs.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }
        return tables;
    }


    private void fetchTableDetail(SimpleTableInfo tab ,DatabaseMetaData dbmd  ) {
        String tabName = tab.getTableName();
        try {

            ResultSet rs = dbmd.getTables(dbc.getCatalog(), dbc.getSchema(), tabName, null);
            while (rs.next()) {
                SimpleTableField field = new SimpleTableField();
                field.setColumnName(rs.getString("COLUMN_NAME"));
                field.setColumnType(rs.getString("TYPE_NAME"));
                field.setMaxLength(rs.getInt("COLUMN_SIZE"));
                field.setPrecision(rs.getInt("DECIMAL_DIGITS"));
                field.setScale(rs.getInt("COLUMN_SIZE"));
                field.setNullEnable(rs.getString("NULLABLE"));
                field.setColumnComment( rs.getString("REMARKS"));
                field.mapToMetadata();
                tab.getColumns().add(field);
            }
            rs.close();
            rs = dbmd.getPrimaryKeys(dbc.getCatalog(),dbc.getSchema(), tabName);
            while (rs.next()) {
                tab.getPkColumns().add(rs.getString("COLUMN_NAME"));
                tab.setPkName(rs.getString("PK_NAME"));
            }
            rs.close();

            rs = dbmd.getExportedKeys(dbc.getCatalog(),dbc.getSchema(), tabName);
            Map<String , SimpleTableReference> refs = new HashMap<String , SimpleTableReference>();
            while (rs.next()) {
                String fkTableName = rs.getString("FKTABLE_NAME");
                SimpleTableReference ref= refs.get(fkTableName);
                if(ref==null){
                    ref = new SimpleTableReference();
                    ref.setTableName(fkTableName);
                    ref.setParentTableName(tabName);
                    ref.setReferenceCode(rs.getString("FK_NAME"));
                }
                SimpleTableField field = new SimpleTableField();
                field.setColumnName(rs.getString("FKCOLUMN_NAME"));
                ref.getReferenceColumns().put(rs.getString("PKCOLUMN_NAME"),
                        field.getColumnName());
                ref.getFkColumns().add(field);
            }
            rs.close();

            for(Map.Entry<String , SimpleTableReference> entry:refs.entrySet()){
                tab.getReferences().add(entry.getValue());
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }
    }

    /**
     * 没有获取外键
     */
    @Override
    public SimpleTableInfo getTableMetadata(String tabName) {
        SimpleTableInfo tab = new SimpleTableInfo(tabName);
        try {
            tab.setSchema(dbc.getSchema().toUpperCase());
            DatabaseMetaData dbmd = dbc.getMetaData();

            ResultSet rs = dbmd.getTables(null, dbc.getSchema(), tabName, null);
            if(rs.next()) {
                tab.setTableLabelName(rs.getString("REMARKS"));
            }
            rs.close();

            fetchTableDetail(tab, dbmd);

        } catch (SQLException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }
        return tab;
    }

    @Override
    public String getDBSchema() {
        try {
            return dbc.getSchema();
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setDBSchema(String schema) {

    }
}
