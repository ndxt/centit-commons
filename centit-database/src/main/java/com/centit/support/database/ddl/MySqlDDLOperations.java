package com.centit.support.database.ddl;

import com.centit.support.database.metadata.TableField;
import com.centit.support.database.metadata.TableInfo;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;

public class MySqlDDLOperations extends GeneralDDLOperations {


    public MySqlDDLOperations(){

    }

    public MySqlDDLOperations(Connection conn) {
        super(conn);
    }

    @Override
    protected void appendPkSql(final TableInfo tableInfo, StringBuilder sbCreate) {
        if (tableInfo.getPkColumns() !=null && tableInfo.getPkColumns().size()>0){
            sbCreate.append(" primary key ");
            appendPkColumnSql(tableInfo, sbCreate);
        }
    }

    @Override
    public String makeModifyColumnSql(final String tableCode, final TableField oldColumn, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" MODIFY COLUMN  ").append(column.getColumnName());
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

    @Override
    public String makeRenameColumnSql(final String tableCode, final String columnCode, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" CHANGE ");
        sbsql.append(columnCode);
        sbsql.append(" ");
        sbsql.append(column.getColumnName());
        sbsql.append(" ");
        appendColumnTypeSQL(column, sbsql);
        return sbsql.toString();
    }

}
