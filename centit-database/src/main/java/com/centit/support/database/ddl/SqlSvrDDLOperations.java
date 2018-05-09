package com.centit.support.database.ddl;

import com.centit.support.database.metadata.TableField;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;

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
    public String makeModifyColumnSql(final String tableCode, final TableField oldColumn, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" ALTER COLUMN ").append(column.getColumnName());
        if(! StringUtils.equalsIgnoreCase(oldColumn.getColumnType(), column.getColumnType())
                || oldColumn.getMaxLength() != column.getMaxLength()
                || oldColumn.getPrecision() != column.getPrecision() ){
            appendColumnTypeSQL(column, sbsql);
        }

        if( oldColumn.isMandatory() != column.isMandatory()){
            sbsql.append(column.isMandatory()?" not null": " null");
        }
        return sbsql.toString();
    }
}
