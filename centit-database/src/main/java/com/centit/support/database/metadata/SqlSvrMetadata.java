package com.centit.support.database.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;


public class SqlSvrMetadata implements DatabaseMetadata {
    protected static final Logger logger = LoggerFactory.getLogger(SqlSvrMetadata.class);

    private final static String sqlGetTabColumns=
        "SELECT  a.name, c.name AS typename, a.length , a.xprec, a.xscale, isnullable "+
        "FROM syscolumns a INNER JOIN "+
              "sysobjects b ON a.id = b.id INNER JOIN "+
              "systypes c ON a.xtype = c.xtype "+
        "WHERE b.xtype = 'U' and b.name = ? "+
        "ORDER BY a.colorder";

    private final static String sqlPKName=
        "select a.name,a.object_id, a.parent_object_id ,a.unique_index_id  "+
        "from sys.key_constraints a , sysobjects b " +
        "where a.type='PK' and " +
            " a.parent_object_id=b.id and b.xtype = 'U' and b.name = ? ";

    private final static String sqlPKColumns=
        "select a.name "+
        "from sys.index_columns b join sys.columns a on(a.object_id=b.object_id and a.column_id=b.column_id) "+
        "where b.object_id=? and b.index_id=? "+
        "order by b.key_ordinal";
    //两个参数 均是 integer 对应上面的 parent_object_id 和 unique_index_id


    //foreign_keys
    private final static String sqlFKNames=
        "select a.name,a.object_id,a.parent_object_id , b.name as tabname "+
        "from sys.foreign_keys a join sysobjects b ON a.parent_object_id = b.id "+
        "where referenced_object_id = ? ";
        //参数对应与上面的 parent_object_id 也就是 主表的ID

    //foreign_key_columns
    private final static String sqlFKColumns=
        "SELECT  a.name, c.name AS typename, a.length , a.xprec, a.xscale, isnullable "+
        "FROM syscolumns a INNER JOIN "+
               "sys.foreign_key_columns b ON a.id = b.parent_object_id  and b.parent_column_id=a.colid JOIN "+
              "systypes c ON a.xtype = c.xtype "+
        "WHERE b.constraint_object_id=? "+
        "ORDER BY b.constraint_column_id";
    //参数对应与上面的 object_id

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
        sDBSchema = schema;
    }

    public SimpleTableInfo getTableMetadata(String tabName) {
        SimpleTableInfo tab = new SimpleTableInfo(tabName);
        int tableId = 0, pkIndId=0;

        try (PreparedStatement pStmt = dbc.prepareStatement(sqlGetTabColumns)) {
            tab.setSchema(dbc.getSchema().toUpperCase());
            // get columns
            pStmt.setString(1, tabName);
            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    // a.name, c.name AS typename, a.length , a.xprec, a.xscale, isnullable
                    SimpleTableField field = new SimpleTableField();
                    field.setColumnName(rs.getString("name"));
                    field.setColumnType(rs.getString("typename"));
                    field.setMaxLength(rs.getInt("length"));
                    field.setPrecision(rs.getInt("xprec"));
                    field.setScale(rs.getInt("xscale"));
                    field.setNullEnable(rs.getString("isnullable"));
                    field.mapToMetadata();

                    tab.addColumn(field);
                }
            }
        } catch (SQLException e1) {
            logger.error(e1.getLocalizedMessage(),e1);
        }

        // get primary key
        try (PreparedStatement pStmt = dbc.prepareStatement(sqlPKName)) {
            pStmt.setString(1, tabName);
            try (ResultSet rs = pStmt.executeQuery()) {
                if (rs.next()) {
                    tab.setPkName(rs.getString("name"));
                    tableId = rs.getInt("parent_object_id");
                    //pk_id = rs.getInt("object_id");
                    pkIndId = rs.getInt("unique_index_id");
                }
            }
        } catch (SQLException e1) {
            logger.error(e1.getLocalizedMessage(),e1);
        }

        try (PreparedStatement pStmt = dbc.prepareStatement(sqlPKColumns)) {
            pStmt.setInt(1, tableId);
            pStmt.setInt(2, pkIndId);
            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    tab.setColumnAsPrimaryKey(rs.getString("name"));
                }
            }
        } catch (SQLException e1) {
            logger.error(e1.getLocalizedMessage(),e1);
        }
            // get reference info
        try (PreparedStatement pStmt = dbc.prepareStatement(sqlFKNames)) {
            pStmt.setInt(1, tableId);
            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    SimpleTableReference ref = new SimpleTableReference();
                    ref.setParentTableName(tabName);
                    //"select a.name,a.object_id,a.parent_object_id , b.name as tabname "+
                    ref.setTableName(rs.getString("tabname"));
                    ref.setReferenceCode(rs.getString("name"));
                    ref.setObjectId(rs.getInt("object_id"));
                    tab.addReference(ref);
                }
            }
        } catch (SQLException e1) {
            logger.error(e1.getLocalizedMessage(),e1);
        }
            // get reference detail
        for(Iterator<SimpleTableReference> it= tab.getReferences().iterator();it.hasNext(); ){
            SimpleTableReference ref = it.next();
            try (PreparedStatement pStmt = dbc.prepareStatement(sqlFKColumns)) {
                pStmt.setInt(1, ref.getObjectId());
                try (ResultSet rs = pStmt.executeQuery()) {
                    while (rs.next()) {
                        //"select a.name,a.object_id,a.parent_object_id , b.name as tabname "+
                        SimpleTableField field = new SimpleTableField();
                        field.setColumnName(rs.getString("name"));
                        field.setColumnType(rs.getString("typename"));
                        field.setMaxLength(rs.getInt("length"));
                        field.setPrecision(rs.getInt("xprec"));
                        field.setScale(rs.getInt("xscale"));
                        field.setNullEnable(rs.getString("isnullable"));
                        field.mapToMetadata();

                        ref.addFkColumn(field);
                    }
                }
            } catch (SQLException e1) {
                logger.error(e1.getLocalizedMessage(),e1);
            }
        }
        return tab;
    }

}
