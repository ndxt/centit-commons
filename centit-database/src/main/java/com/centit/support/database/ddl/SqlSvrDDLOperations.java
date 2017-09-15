package com.centit.support.database.ddl;

import java.sql.Connection;

import com.centit.support.database.metadata.TableField;

public class SqlSvrDDLOperations extends GeneralDDLOperations {


    public SqlSvrDDLOperations(){

    }

    public SqlSvrDDLOperations(Connection conn) {
        super(conn);
    }

    @Override
    public String makeRenameColumnSql(final String tableCode, final String columnCode, final TableField column){
/*        dropColumn(tableCode, columnCode);
        column.setColumnName(newColumnCode);
        addColumn(tableCode, column);*/
        return "exec sp_rename ' "+tableCode +"." + columnCode +"','"+ column.getColumnName() +"','COLUMN'";
    }

    @Override
    public String makeModifyColumnSql(final String tableCode, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" ALTER COLUMN ");
        appendColumnSQL(column,sbsql);
        return sbsql.toString();
    }

}
