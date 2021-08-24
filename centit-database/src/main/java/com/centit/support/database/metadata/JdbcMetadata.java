package com.centit.support.database.metadata;

import org.apache.commons.lang3.StringUtils;
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
    private String dbSchema;

    @Override
    public void setDBConfig(Connection dbc) {
        this.dbc = dbc;
    }


    public List<SimpleTableInfo> listAllTable() {
        List<SimpleTableInfo> tables = new ArrayList<>(100);
        try {
            String dbSechema = this.getDBSchema();
            String dbCatalog = this.getDBCatalog();

            DatabaseMetaData dbmd = dbc.getMetaData();

            ResultSet rs = dbmd.getTables(dbCatalog, dbSechema, null, null);
            while (rs.next()) {
                SimpleTableInfo tab = new SimpleTableInfo();
                if (dbSechema != null) {
                    tab.setSchema(dbSechema.toUpperCase());
                }
                tab.setTableName(rs.getString("TABLE_NAME"));
                tab.setTableLabelName(rs.getString("REMARKS"));
                String tt = rs.getString("TABLE_TYPE");
                if ("view".equalsIgnoreCase(tt) || "table".equalsIgnoreCase(tt)) {
                    fetchTableDetail(tab, dbmd);
                    tab.setTableType("view".equalsIgnoreCase(tt) ? "V" : "T");
                    tables.add(tab);
                }
            }
            rs.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        return tables;
    }


    private void fetchTableDetail(SimpleTableInfo tab, DatabaseMetaData dbmd) {
        String tabName = tab.getTableName();
        try {
            String dbSchema = this.getDBSchema();
            String dbCatalog = this.getDBCatalog();

            ResultSet rs = dbmd.getColumns(dbCatalog, dbSchema, tabName, null);
            while (rs.next()) {
                SimpleTableField field = new SimpleTableField();
                field.setColumnName(rs.getString("COLUMN_NAME"));
                field.setColumnType(rs.getString("TYPE_NAME"));
                field.setMaxLength(rs.getInt("COLUMN_SIZE"));
                field.setPrecision(rs.getInt("COLUMN_SIZE"));
                field.setScale(rs.getInt("DECIMAL_DIGITS"));
                field.setNullEnable(rs.getString("NULLABLE"));
                field.setColumnComment(rs.getString("REMARKS"));
                field.mapToMetadata();
                tab.addColumn(field);
            }
            rs.close();
            rs = dbmd.getPrimaryKeys(dbCatalog, dbSchema, tabName);
            while (rs.next()) {
                tab.setColumnAsPrimaryKey(rs.getString("COLUMN_NAME"));
                tab.setPkName(rs.getString("PK_NAME"));
            }
            rs.close();

            rs = dbmd.getExportedKeys(dbCatalog, dbSchema, tabName);
            Map<String, SimpleTableReference> refs = new HashMap<String, SimpleTableReference>();
            while (rs.next()) {
                String fkTableName = rs.getString("FKTABLE_NAME");
                SimpleTableReference ref = refs.get(fkTableName);
                if (ref == null) {
                    ref = new SimpleTableReference();
                    ref.setTableName(fkTableName);
                    ref.setParentTableName(tabName);
                    ref.setReferenceCode(rs.getString("FK_NAME"));
                }
                ref.addReferenceColumn(rs.getString("PKCOLUMN_NAME"),
                    rs.getString("FKCOLUMN_NAME"));
            }
            rs.close();

            for (Map.Entry<String, SimpleTableReference> entry : refs.entrySet()) {
                tab.addReference(entry.getValue());
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
    }

    /**
     * 没有获取外键
     */
    @Override
    public SimpleTableInfo getTableMetadata(String tabName) {
        SimpleTableInfo tab = new SimpleTableInfo(tabName);
        try {
            String dbSechema = this.getDBSchema();
            String dbCatalog = this.getDBCatalog();
            DatabaseMetaData dbmd = dbc.getMetaData();
            ResultSet rs = dbmd.getTables(dbCatalog, dbSechema, tabName, null);
            if (rs.next()) {
                tab.setTableLabelName(rs.getString("REMARKS"));
            }
            rs.close();

            fetchTableDetail(tab, dbmd);

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        return tab;
    }

    @Override
    public String getDBSchema() {
        if (StringUtils.isNotBlank(this.dbSchema)) {
            return this.dbSchema;
        }
        try {
            return dbc.getSchema();
        } catch (AbstractMethodError error) {
            return null;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setDBSchema(String schema) {
        this.dbSchema = schema;
    }

    public String getDBCatalog() {
        try {
            return dbc.getCatalog();
        } catch (AbstractMethodError error) {
            return null;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
            return null;
        }
    }
}
