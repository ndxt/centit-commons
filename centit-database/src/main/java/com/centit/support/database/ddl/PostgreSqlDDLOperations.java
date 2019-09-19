package com.centit.support.database.ddl;

import com.centit.support.database.metadata.TableField;
import com.centit.support.database.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;

public class PostgreSqlDDLOperations extends GeneralDDLOperations {

    public PostgreSqlDDLOperations(){

    }

    public PostgreSqlDDLOperations(Connection conn) {
        super(conn);
    }

    @Override
    public String makeCreateSequenceSql(final String sequenceName){
        return "create sequence " + QueryUtils.cleanSqlStatement(sequenceName);
    }

    /**
     * 修改列定义 ，比如 修改 varchar 的长度
     *
     * @param tableCode 表代码
     * @param oldColumn 老的字段
     * @param column    字段
     * @return sql语句
     */
    @Override
    public String makeModifyColumnSql(String tableCode, TableField oldColumn, TableField column) {
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" ALTER ").append(column.getColumnName()).append(" ");
        if(! StringUtils.equalsIgnoreCase(oldColumn.getColumnType(), column.getColumnType())
                || oldColumn.getMaxLength() != column.getMaxLength()
                || oldColumn.getPrecision() != column.getPrecision() ){
            sbsql.append(" type ");
            appendColumnTypeSQL(column, sbsql);
        }

        if( oldColumn.isMandatory() != column.isMandatory()){
            sbsql.append(column.isMandatory()?" set not null": " drop not null");
        }

        return sbsql.toString();
    }


}
