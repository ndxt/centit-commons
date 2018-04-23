package com.centit.support.database.ddl;

import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.utils.QueryUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class OracleDDLOperations extends GeneralDDLOperations {

    public OracleDDLOperations(){

    }

    public OracleDDLOperations(Connection conn) {
        super(conn);
    }

    @Override
    public String makeCreateSequenceSql(final String sequenceName){
        return "create sequence " + QueryUtils.cleanSqlStatement(sequenceName);
    }

    @Override
    public String makeAddColumnSql(final String tableCode, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" add ");
        appendColumnSQL(column,sbsql);
        return sbsql.toString();
    }

    @Override
    public List<String> makeReconfigurationColumnSqls(final String tableCode, final String columnCode, final TableField column){
        List<String> sqls = new ArrayList<String>();
        SimpleTableField tempColumn = new SimpleTableField();
        tempColumn.setColumnName(columnCode+"_1");
        sqls.add(makeRenameColumnSql(tableCode, columnCode, tempColumn));
        sqls.add(makeAddColumnSql(tableCode, column));

        if("String".equals(column.getJavaType()))
            sqls.add("update "+tableCode+" set "+ column.getColumnName() + " = to_char(" + columnCode+")");
        if("Date".equals(column.getJavaType()))
            sqls.add("update "+tableCode+" set "+ column.getColumnName() + " = to_date(" + columnCode+")");
        if("Long".equals(column.getJavaType()) || "Double".equals(column.getJavaType()))
            sqls.add("update "+tableCode+" set "+ column.getColumnName() + " = to_number(" + columnCode+")");

        sqls.add(makeDropColumnSql(tableCode, columnCode+"_1"));
        return sqls;
    }
}
