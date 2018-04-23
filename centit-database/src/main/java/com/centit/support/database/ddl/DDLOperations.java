package com.centit.support.database.ddl;

import com.centit.support.database.metadata.TableField;
import com.centit.support.database.metadata.TableInfo;

import java.sql.SQLException;
import java.util.List;

public interface DDLOperations {
    /**
     * 创建序列
     * @param sequenceName  序列
     * @return sql语句
     */
    String makeCreateSequenceSql(final String sequenceName);
    /**
     * 创建表
     * @param tableInfo 表
     * @return sql语句
     */
    String makeCreateTableSql(final TableInfo tableInfo);
    /**
     * 删除表
     * @param tableCode 表
     * @return sql语句
     */
    String makeDropTableSql(final String tableCode);
    /**
     * 添加列
     * @param tableCode 表
     * @param column 字段
     * @return sql语句
     */
    String makeAddColumnSql(final String tableCode,final TableField column);
    /**
     * 修改列定义 ，比如 修改 varchar 的长度
     * @param tableCode 表代码
     * @param column 字段
     * @return sql语句
     */
    String makeModifyColumnSql(final String tableCode, final TableField column);
    /**
     * 删除列
     * @param tableCode 表代码
     * @param columnCode 字段代码
     * @return sql语句
     */
    String makeDropColumnSql(final String tableCode, final String columnCode);
    /**
     * 重命名列
     * @param tableCode 表代码
     * @param columnCode 字段代码
     * @param column 字段
     * @return sql语句
     */
    String makeRenameColumnSql(final String tableCode, final String columnCode, final TableField column);
    /**
     * 重构列，涉及到内容格式的转换，需要新建一个列，将旧列中的数据转换到新列中，然后在删除旧列
     * @param tableCode 表代码
     * @param columnCode 字段代码
     * @param column 字段
     * @return sql语句列表
     */
    List<String> makeReconfigurationColumnSqls(final String tableCode, final String columnCode, final TableField column);


    /**
     * 创建sequence
     * @param sequenceName sequenceName
     * @throws SQLException SQLException
     */
    void createSequence(final String sequenceName)throws SQLException;
    /**
     * 创建表
     * @param tableInfo 表
     * @throws SQLException SQLException
     */
    void createTable(TableInfo tableInfo) throws SQLException;
    /**
     * 删除表
     * @param tableCode 表代码
     * @throws SQLException SQLException
     */
    void dropTable(String tableCode) throws SQLException;
    /**
     * 添加列
     * @param tableCode 表代码
     * @param column 字段
     * @throws SQLException SQLException
     */
    void addColumn(String tableCode, TableField column) throws SQLException;
    /**
     * 修改列定义 ，比如 修改 varchar 的长度
     * @param tableCode 表代码
     * @param column 字段
     * @throws SQLException SQLException
     */
    void modifyColumn(String tableCode, TableField column) throws SQLException;
    /**
     * 删除列
     * @param tableCode 表代码
     * @param columnCode 字段代码
     * @throws SQLException SQLException
     */
    void dropColumn(String tableCode, String columnCode) throws SQLException;
    /**
     * 重命名列
     * @param tableCode 表代码
     * @param columnCode 字段代码
     * @param column 字段
     * @throws SQLException SQLException
     */
    void renameColumn(String tableCode, String columnCode, TableField column) throws SQLException;
    /**
     * 重构列，涉及到内容格式的转换，需要新建一个列，将旧列中的数据转换到新列中，然后在删除旧列
     * @param tableCode 表代码
     * @param columnCode 字段代码
     * @param column 字段
     * @throws SQLException SQLException
     */
    void reconfigurationColumn(String tableCode, String columnCode, TableField column) throws SQLException;

}
