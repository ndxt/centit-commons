package com.centit.support.database.ddl;

import java.sql.Connection;

import com.centit.support.database.metadata.TableField;
import com.centit.support.database.metadata.TableInfo;

public class MySqlDDLOperations extends GeneralDDLOperations implements DDLOperations {
	
	
	public MySqlDDLOperations(){
		
	}
	
	public MySqlDDLOperations(Connection conn) {
		super(conn);
	}	
	
	@Override
	protected void appendPkSql(final TableInfo tableInfo, StringBuilder sbCreate) {		
		if (tableInfo.getPkColumns() !=null && tableInfo.getPkColumns().size()>0){
			sbCreate.append(" primary key ");
			appendPkColumnSql(tableInfo, sbCreate);			
		}		
	}
	
	@Override
	public String makeRenameColumnSql(final String tableCode, final String columnCode, final TableField column){
		StringBuilder sbsql = new StringBuilder("alter table ");
		sbsql.append(tableCode);
		sbsql.append(" CHANGE ");
		sbsql.append(columnCode);
		sbsql.append(" ");
		sbsql.append(column.getColumnName());
		sbsql.append(" ");
		sbsql.append(column.getColumnType());
		if (column.getMaxLength() > 0)
			sbsql.append("(").append(column.getMaxLength()).append(")");
		else if (column.getPrecision() > 0) {
			sbsql.append("(").append(column.getPrecision());
			if (column.getScale() > 0)
				sbsql.append(",").append(column.getScale());
			sbsql.append(")");
		}
		return sbsql.toString();			
	}
	
}
