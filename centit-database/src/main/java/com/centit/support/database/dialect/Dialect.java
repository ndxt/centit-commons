package com.centit.support.database.dialect;

import com.centit.support.database.utils.DBType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by codefan on 17-8-29.
 */
public interface Dialect {
    Connection getConnection();

    Long getSequence(String sequenceName) throws IOException, SQLException;


    static Dialect createDialect(Connection connection) throws SQLException {
        DBType dbtype = DBType.mapDBType(connection.getMetaData().getURL());
        switch (dbtype){
            case Oracle:
                return new OracleDialect(connection);
            case DB2:
                return new DB2Dialect(connection);
            case SqlServer:
                return new SqlSvrDialect(connection);
            case MySql:
                return new MySqlDialect(connection);
            case Access:
            case H2:
            default:
                throw new SQLException("不支持的数据库类型："+dbtype.toString());
        }
    }
}
