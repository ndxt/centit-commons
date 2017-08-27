package com.centit.support.database.metadata;


import java.sql.Connection;

public interface DatabaseMetadata {
	public void setDBConfig(Connection dbc);
	public SimpleTableInfo getTableMetadata(String tabName);
	public String getDBSchema();
	public void setDBSchema(String schema);	
}
