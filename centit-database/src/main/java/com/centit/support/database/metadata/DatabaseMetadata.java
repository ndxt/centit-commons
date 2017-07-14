package com.centit.support.database.metadata;

import com.centit.support.database.DBConnect;

public interface DatabaseMetadata {
	public void setDBConfig(DBConnect dbc);
	public SimpleTableInfo getTableMetadata(String tabName);
	public String getDBSchema();
	public void setDBSchema(String schema);	
}
