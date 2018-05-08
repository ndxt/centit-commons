package com.centit.support.database.ddl;

import com.centit.support.database.metadata.TableField;
import com.centit.support.database.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;

public class DB2DDLOperations extends GeneralDDLOperations{

    public DB2DDLOperations() {

    }

    public DB2DDLOperations(Connection conn) {
        super(conn);
    }

    @Override
    public String makeCreateSequenceSql(final String sequenceName){
        return "CREATE SEQUENCE " + QueryUtils.cleanSqlStatement(sequenceName) +
                "  AS INTEGER START WITH 1 INCREMENT BY 1";
    }

    @Override
    public String makeModifyColumnSql(final String tableCode, final TableField oldColumn, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);

        if(! StringUtils.equalsIgnoreCase(oldColumn.getColumnType(), column.getColumnType())
                || oldColumn.getMaxLength() != column.getMaxLength()
                || oldColumn.getPrecision() != column.getPrecision() ){
            sbsql.append(" alter column ");
            sbsql.append(column.getColumnName());
            sbsql.append(" set data type ");
            appendColumnTypeSQL(column, sbsql);
        }

        if( oldColumn.isMandatory() != column.isMandatory()){
            sbsql.append(" alter column ");
            sbsql.append(column.getColumnName());
            sbsql.append(column.isMandatory() ? " set not null" : " drop not null");
        }

        return sbsql.toString();
    }
}
