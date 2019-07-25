package com.centit.support.database.ddl;

import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.TableField;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DBType;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class GeneralDDLOperations implements DDLOperations {


    protected Connection conn;

    public GeneralDDLOperations(){

    }

    public GeneralDDLOperations(Connection conn) {
        this.conn = conn;
    }

    public void setConnect(Connection conn) {
        this.conn = conn;
    }

    public static GeneralDDLOperations createDDLOperations(final DBType dbtype)
            throws SQLException {
        switch (dbtype){
            case Oracle:
            case DM:
            case KingBase:
            case GBase:
                return new OracleDDLOperations();
            case DB2:
                return new DB2DDLOperations();
            case SqlServer:
                return new SqlSvrDDLOperations();
            case MySql:
                return new MySqlDDLOperations();
            case H2:
                return new H2DDLOperations();
            case PostgreSql:
                return new PostgreSqlDDLOperations();
            case Access:
            default:
                throw new  SQLException("不支持的数据库类型："+dbtype.toString());
        }
    }

    public static GeneralDDLOperations createDDLOperations(final Connection conn)
            throws SQLException {
        DBType dbtype = DBType.mapDBType(conn.getMetaData().getURL());
        GeneralDDLOperations dllOperations = createDDLOperations(dbtype);
        dllOperations.setConnect(conn);
        return dllOperations;
    }
    /**
     * checkLabelName 判断一个字符串是否符合标识符，是否可以作为字段名或者表名
     * @param seq CharSequence
     * @return boolean
     */
    public static final boolean checkLabelName(final CharSequence seq) {
        if (seq == null || seq.length() == 0) {
            return false;
        }
        final int strLen = seq.length();
        char c = seq.charAt(0);
        if( c != '_' &&
                ( c < 'a' || c >'z') &&
                ( c < 'A' || c > 'Z')
             )
            return false;

        for (int i = 1; i < strLen; i++) {
             c = seq.charAt(i);
             if( c != '_' &&
                     ( c < 'a' || c >'z') &&
                     ( c < 'A' || c > 'Z')  &&
                     ( c < '0' || c > '9')
                  )
                 return false;
        }
        return true;
    }

    /**
     * 返回格式检查结果
     * @param tableInfo 表
     * @return Pair
     */
    public static final Pair<Integer, String> checkTableWellDefined(final TableInfo tableInfo) {
        if(! checkLabelName(tableInfo.getTableName())) {
            return new ImmutablePair<>(-1, "表名" + tableInfo.getTableName() + "格式不正确！");
        }
        for(TableField field : tableInfo.getColumns()){
            if(! checkLabelName(field.getColumnName())) {
                return new ImmutablePair<>(-2, "字段名" + field.getColumnName() + "格式不正确！");
            }
            if(StringUtils.isBlank(field.getColumnType())) {
                return new ImmutablePair<>(-3, "字段" + field.getColumnName() + "没有指定类型！");
            }
        }

        if( tableInfo.getPkColumns()==null || tableInfo.getPkColumns().size()==0) {
            return new ImmutablePair<>(-4, "没有定义主键！");
        }

        return new ImmutablePair<>(0,"ok！");
    }

    /**
     * 创建序列
      * 用表来模拟sequence
     * create table simulate_sequence (seqname varchar(100) not null primary key,
     * currvalue integer, increment integer);
     *
     * @param sequenceName 序列
     * @return sql语句
     */
    @Override
    public String makeCreateSequenceSql(final String sequenceName){
        return "INSERT INTO simulate_sequence (seqname, currvalue , increment)"
                + " VALUES ("+ QueryUtils.buildStringForQuery(sequenceName)+", 0, 1)";
    }

    @Override
    public String makeCreateTableSql(final TableInfo tableInfo){
        StringBuilder sbCreate = new StringBuilder("create table ");
        sbCreate.append(tableInfo.getTableName()).append(" (");
        appendColumnsSQL(tableInfo, sbCreate);
        appendPkSql(tableInfo, sbCreate);
        sbCreate.append(")");
        return sbCreate.toString();
    }

    protected void appendPkSql(final TableInfo tableInfo, StringBuilder sbCreate) {
        if (tableInfo.getPkColumns() !=null && tableInfo.getPkColumns().size()>0){
            sbCreate.append(" constraint ");
            if (StringUtils.isBlank(tableInfo.getPkName())){
               sbCreate.append("pk_"+tableInfo.getTableName());
            }else{
                sbCreate.append(tableInfo.getPkName());
            }
            sbCreate.append(" primary key ");
            appendPkColumnSql(tableInfo, sbCreate);
        }
    }

    protected void appendPkColumnSql(final TableInfo tableInfo, StringBuilder sbCreate) {
        sbCreate.append("(");
        int i=0;
        for(String pkfield : tableInfo.getPkColumns()){
            if(i>0) {
                sbCreate.append(", ");
            }
            sbCreate.append(pkfield);
            i++;
        }
        sbCreate.append(")");
    }

    protected void appendColumnsSQL(final TableInfo tableInfo, StringBuilder sbCreate) {
        for(TableField field : tableInfo.getColumns()){
            appendColumnSQL(field,sbCreate);
            if(StringUtils.isNotBlank(field.getDefaultValue())) {
                sbCreate.append(" default ").append(field.getDefaultValue());
            }
            sbCreate.append(",");
        }
    }

    protected void appendColumnTypeSQL(final TableField field,StringBuilder sbCreate) {
        sbCreate.append(field.getColumnType());
        //StringUtils.equalsIgnoreCase(str1, str2)
        if ("varchar".equalsIgnoreCase(field.getColumnType()) || "varchar2".equalsIgnoreCase(field.getColumnType())) {
            if (field.getMaxLength() > 0) {
                sbCreate.append("(").append(field.getMaxLength()).append(")");
            } else {
                sbCreate.append("(64)");
            }
        } else if ("number".equalsIgnoreCase(field.getColumnType()) || "decimal".equalsIgnoreCase(field.getColumnType())) {
            if (field.getPrecision() > 0) {
                sbCreate.append("(").append(field.getPrecision());
            } else {
                sbCreate.append("(").append(24);
            }
            if (field.getScale() > 0) {
                sbCreate.append(",").append(field.getScale());
            }
            sbCreate.append(")");
        }
    }

    protected void appendColumnSQL(final TableField field,StringBuilder sbCreate) {
        sbCreate.append(field.getColumnName())
                .append(" ");
        appendColumnTypeSQL(field, sbCreate);
        if(field.isMandatory()) {
            sbCreate.append(" not null");
        }
    }

    @Override
    public String makeDropTableSql(final String tableCode){
        return "drop table " + tableCode;
    }

    @Override
    public String makeAddColumnSql(final String tableCode, final TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" add column ");
        appendColumnSQL(column,sbsql);
        return sbsql.toString();
    }

    @Override
    public String makeDropColumnSql(final String tableCode, final String columnCode){
        return  "alter table " + tableCode +" drop COLUMN " + columnCode;

    }

    @Override
    public String makeRenameColumnSql(final String tableCode, final String columnCode, TableField column){
        StringBuilder sbsql = new StringBuilder("alter table ");
        sbsql.append(tableCode);
        sbsql.append(" rename COLUMN ");
        sbsql.append(columnCode);
        sbsql.append(" to ");
        sbsql.append(column.getColumnName());
        return  sbsql.toString();
    }

    @Override
    public List<String> makeReconfigurationColumnSqls(final String tableCode, final String columnCode, final TableField column){
        List<String> sqls = new ArrayList<String>();
        SimpleTableField tempColumn = new SimpleTableField();
        tempColumn.setColumnName(columnCode+"_1");
        sqls.add(makeRenameColumnSql(tableCode, columnCode, tempColumn));
        sqls.add(makeAddColumnSql(tableCode, column));
        sqls.add("update tableCode set "+ column.getColumnName() + " = " + columnCode);
        sqls.add(makeDropColumnSql(tableCode, columnCode+"_1"));
        return sqls;
    }

    @Override
    public void createSequence(final String sequenceName)throws SQLException{
        DatabaseAccess.doExecuteSql(conn, makeCreateSequenceSql(sequenceName));
    }

    @Override
    public void createTable(TableInfo tableInfo) throws SQLException {
        DatabaseAccess.doExecuteSql(conn, makeCreateTableSql(tableInfo));
    }

    @Override
    public void dropTable(String tableCode) throws SQLException {
        DatabaseAccess.doExecuteSql(conn, makeDropTableSql(tableCode));
    }

    @Override
    public void addColumn(String tableCode, TableField column) throws SQLException {
        DatabaseAccess.doExecuteSql(conn, makeAddColumnSql(tableCode,column));
    }

    @Override
    public void modifyColumn(String tableCode, TableField oldColumn, TableField column) throws SQLException {
        DatabaseAccess.doExecuteSql(conn, makeModifyColumnSql(tableCode,oldColumn, column));
    }

    @Override
    public void dropColumn(String tableCode, String columnCode) throws SQLException {
        DatabaseAccess.doExecuteSql(conn, makeDropColumnSql(tableCode,columnCode));
    }

    @Override
    public void renameColumn(String tableCode, String columnCode, TableField column) throws SQLException {
        DatabaseAccess.doExecuteSql(conn, makeRenameColumnSql(tableCode,columnCode,column));
    }

    @Override
    public void reconfigurationColumn(String tableCode, String columnCode, TableField column) throws SQLException {
        List<String> sqList = makeReconfigurationColumnSqls(tableCode, columnCode, column);
        if(sqList==null)
            return;
        for(String sql:sqList){
            DatabaseAccess.doExecuteSql(conn, sql);
        }
    }

}
