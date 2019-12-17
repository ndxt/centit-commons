package com.centit.support.database.metadata;


import com.centit.support.database.utils.DBType;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("unused")
public interface DatabaseMetadata {
    static DatabaseMetadata createDatabaseMetadata(final DBType dbtype)
        throws SQLException {
        switch (dbtype) {
            case Oracle:
            case DM:
            case KingBase:
            case Oscar:
                return new OracleMetadata();
            case DB2:
                return new DB2Metadata();
            case SqlServer:
                return new SqlSvrMetadata();
            case PostgreSql:
            case MySql:
            case Access:
            case H2:
            case GBase:
            default:
                return new JdbcMetadata();
        }
    }

    void setDBConfig(Connection dbc);

    SimpleTableInfo getTableMetadata(String tabName);

    String getDBSchema();

    void setDBSchema(String schema);

}
