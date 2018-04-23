package com.centit.support.database.ddl;

import com.centit.support.database.metadata.TableField;
import com.centit.support.database.utils.QueryUtils;

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
    public String makeModifyColumnSql(final String tableCode, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" alter column ");
        sbsql.append(column.getColumnName());
        sbsql.append(" set data type ");
        sbsql.append(column.getColumnType());
        if (column.getMaxLength() > 0)
            sbsql.append("(").append(column.getMaxLength()).append(")");
        else if (column.getPrecision() > 0) {
            sbsql.append("(").append(column.getPrecision());
            if (column.getScale() > 0)
                sbsql.append(",").append(column.getScale());
            sbsql.append(")");
        }
        return sbsql.toString();
    }
}
