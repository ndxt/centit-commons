package com.centit.support.database.ddl;

import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.utils.FieldType;
import com.centit.support.database.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;

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
    public String makeModifyColumnSql(final String tableCode, final TableField oldColumn, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" modify ").append(column.getColumnName()).append(" ");
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
    public String makeAddColumnSql(final String tableCode, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" add ");
        appendColumnSQL(column,sbsql);
        return sbsql.toString();
    }

    @Override
    public List<String> makeReconfigurationColumnSqls(final String tableCode, final String columnCode, final TableField column){
        List<String> sqls = new ArrayList<>();
        SimpleTableField tempColumn = new SimpleTableField();
        tempColumn.setColumnName(columnCode+"_1");
        sqls.add(makeRenameColumnSql(tableCode, columnCode, tempColumn));
        sqls.add(makeAddColumnSql(tableCode, column));

        if(FieldType.STRING.equals(column.getFieldType()) || FieldType.TEXT.equals(column.getFieldType())
           || FieldType.FILE_ID.equals(column.getFieldType())) {
            sqls.add("update " + tableCode + " set " + column.getColumnName() + " = to_char(" + columnCode + "_1)");
        } else if(FieldType.DATE.equals(column.getFieldType()) ) {
            sqls.add("update " + tableCode + " set " + column.getColumnName() + " = to_date(" + columnCode + "_1,\'yyyy-mm-dd\')");
        } else if(FieldType.TIMESTAMP.equals(column.getFieldType()) || FieldType.DATETIME.equals(column.getFieldType())) {
            sqls.add("update " + tableCode + " set " + column.getColumnName() + " = to_date(" + columnCode + "_1,\'yyyy-mm-dd hh24:mi:ss\')");
        } else if(FieldType.LONG.equals(column.getFieldType()) || FieldType.DOUBLE.equals(column.getFieldType())
            || FieldType.INTEGER.equals(column.getFieldType()) || FieldType.FLOAT.equals(column.getFieldType())) {
            sqls.add("update " + tableCode + " set " + column.getColumnName() + " = to_number(" + columnCode + "_1)");
        }
        sqls.add(makeDropColumnSql(tableCode, columnCode+"_1"));
        return sqls;
    }
}
