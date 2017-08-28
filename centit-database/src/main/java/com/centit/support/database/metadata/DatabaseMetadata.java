package com.centit.support.database.metadata;


import com.centit.support.database.utils.DBType;
import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("unused")
public interface DatabaseMetadata {
	void setDBConfig(Connection dbc);
	SimpleTableInfo getTableMetadata(String tabName);
	String getDBSchema();
	void setDBSchema(String schema);

	static DatabaseMetadata createDatabaseMetadata(final DBType dbtype)
			throws SQLException {
		switch (dbtype){
			case Oracle:
				return new OracleMetadata();
			case DB2:
				return new DB2Metadata();
			case SqlServer:
				return new SqlSvrMetadata();
			case MySql:
			case Access:
			case H2:
			default:
				return new JdbcMetadata();
		}
	}

}
