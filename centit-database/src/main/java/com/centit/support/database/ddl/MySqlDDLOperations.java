package com.centit.support.database.ddl;

import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.FieldType;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class MySqlDDLOperations extends GeneralDDLOperations {


    public MySqlDDLOperations() {

    }

    public MySqlDDLOperations(Connection conn) {
        super(conn);
    }

    @Override
    protected void appendPkSql(final TableInfo tableInfo, StringBuilder sbCreate) {
        if (tableInfo.hasParmaryKey()) {
            sbCreate.append(" primary key ");
            appendPkColumnSql(tableInfo, sbCreate);
        }
    }

    @Override
    protected void appendColumnSQL(final TableField field, StringBuilder sbCreate) {
        sbCreate.append(field.getColumnName())
            .append(" ");
        appendColumnTypeSQL(field, sbCreate);
        if (field.isMandatory()) {
            sbCreate.append(" not null");
        }
        sbCreate.append(" comment \'"+ field.getFieldLabelName()+"\'");
    }
    @Override
    public String makeModifyColumnSql(final String tableCode, final TableField oldColumn, final TableField column) {
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        Boolean modify=false;
        sbsql.append(" MODIFY COLUMN  ").append(column.getColumnName()).append(" ");
        if (!StringUtils.equalsIgnoreCase(oldColumn.getColumnType(), column.getColumnType())
            || !oldColumn.getMaxLength().equals(column.getMaxLength())
            || !oldColumn.getPrecision().equals(column.getPrecision())) {
            appendColumnTypeSQL(column, sbsql);
            modify=true;
        }

        if (oldColumn.isMandatory() != column.isMandatory()) {
            if (!modify) {
                appendColumnTypeSQL(column, sbsql);
            }
            sbsql.append(column.isMandatory() ? " not null" : " null");
        }
        if (oldColumn.getFieldLabelName() != column.getFieldLabelName()) {
            if (!modify) {
                appendColumnTypeSQL(column, sbsql);
            }
            sbsql.append(" comment \'"+ column.getFieldLabelName()+"\'");
        }
        return sbsql.toString();
    }

    @Override
    public String makeRenameColumnSql(final String tableCode, final String columnCode, final TableField column) {
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

    @Override
    public List<String> makeReconfigurationColumnSqls(final String tableCode, final String columnCode, final TableField column) {
        List<String> sqls = new ArrayList<>();
        SimpleTableField tempColumn = new SimpleTableField();
        tempColumn.setColumnName(columnCode + "_1");
        sqls.add(makeRenameColumnSql(tableCode, columnCode, tempColumn));
        sqls.add(makeAddColumnSql(tableCode, column));

        if (FieldType.STRING.equals(column.getFieldType()) || FieldType.TEXT.equals(column.getFieldType())
            || FieldType.FILE_ID.equals(column.getFieldType())) {
            sqls.add("update " + tableCode + " set " + column.getColumnName() + " = cast(" + columnCode + "_1 as char)");
        } else if (FieldType.DATE.equals(column.getFieldType())) {
            sqls.add("update " + tableCode + " set " + column.getColumnName() + " = cast(" + columnCode + "_1 as date)");
        } else if (FieldType.TIMESTAMP.equals(column.getFieldType()) || FieldType.DATETIME.equals(column.getFieldType())) {
            sqls.add("update " + tableCode + " set " + column.getColumnName() + " = cast(" + columnCode + "_1 as datetime)");
        } else if (FieldType.LONG.equals(column.getFieldType()) || FieldType.INTEGER.equals(column.getFieldType())) {
            sqls.add("update " + tableCode + " set " + column.getColumnName() + " = cast(" + columnCode + "_1 as signed)");
        } else if (FieldType.FLOAT.equals(column.getFieldType()) || FieldType.DOUBLE.equals(column.getFieldType())) {
            sqls.add("update " + tableCode + " set " + column.getColumnName() + " = cast(" + columnCode + "_1 as decimal(" + column.getMaxLength()
                + "," + column.getScale() + ")");
        }
        sqls.add(makeDropColumnSql(tableCode, columnCode + "_1"));
        return sqls;
    }
}
