package com.centit.support.database.metadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.centit.support.database.DBConnect;

public class OracleMetadata implements DatabaseMetadata {
	
	protected static final Logger logger = LoggerFactory.getLogger(OracleMetadata.class);
	
	private final static String sqlGetTabColumns=
		"select a.COLUMN_NAME,a.DATA_TYPE, a.DATA_LENGTH," +
		   "nvl(a.DATA_PRECISION,a.DATA_LENGTH) as DATA_PRECISION,NVL(a.DATA_SCALE,0) as DATA_SCALE,a.NULLABLE " +
		"from user_tab_columns a "+
		"where a.TABLE_NAME=?";
	
	private final static String sqlPKName=
		"select CONSTRAINT_NAME "+
		"from user_constraints " +
		"where TABLE_NAME=? and CONSTRAINT_TYPE='P'";

	private final static String sqlPKColumns=
		"select a.COLUMN_NAME "+
		"from USER_CONS_COLUMNS a join user_tab_columns b on (a.table_name=b.table_name and a.COLUMN_NAME=b.COLUMN_NAME) "+
		"where /*a.OWNER=? and*/ CONSTRAINT_NAME=? order by POSITION";

	private final static String sqlFKNames=
		"select TABLE_NAME,CONSTRAINT_NAME "+
		"from user_constraints " +
		"where /*a.OWNER=? and*/ R_CONSTRAINT_NAME=? and CONSTRAINT_TYPE='R'";

	private final static String sqlFKColumns=
		"select a.COLUMN_NAME,b.DATA_TYPE,b.DATA_LENGTH," +
		   "nvl(b.DATA_PRECISION,b.DATA_LENGTH) as DATA_PRECISION,NVL(b.DATA_SCALE,0) as DATA_SCALE,b.NULLABLE " +
		"from USER_CONS_COLUMNS a join user_tab_columns b on (a.table_name=b.table_name and a.COLUMN_NAME=b.COLUMN_NAME) "+
		"where /*a.OWNER=? and*/ CONSTRAINT_NAME=? order by POSITION";

	
	private String sDBSchema ;
	private DBConnect dbc;
	
	@Override
	public void setDBConfig(DBConnect dbc){
		this.dbc=dbc;
	}

	public String getDBSchema() {
		return sDBSchema;
	}

	public void setDBSchema(String schema) {
		sDBSchema = schema;
	}
	
	public SimpleTableInfo getTableMetadata(String tabName) {
		SimpleTableInfo tab = new SimpleTableInfo(tabName);
		PreparedStatement pStmt= null;
		ResultSet rs = null;
		
		try {
			Connection conn = dbc.getConn();
			tab.setSchema( dbc.getSchema().toUpperCase());
			// get columns
			pStmt= conn.prepareStatement(sqlGetTabColumns);
			pStmt.setString(1, tabName);
			rs = pStmt.executeQuery();
			while (rs.next()) {
				SimpleTableField field = new SimpleTableField();
				field.setColumnName(rs.getString("COLUMN_NAME"));
				field.setColumnType(rs.getString("DATA_TYPE"));
				field.setMaxLength(rs.getInt("DATA_LENGTH"));
				field.setPrecision(rs.getInt("DATA_PRECISION"));
				field.setScale(rs.getInt("DATA_SCALE"));
				field.setNullEnable(rs.getString("NULLABLE"));
				field.mapToMetadata();
				
				tab.getColumns().add(field);
			}
			rs.close();
			pStmt.close();
			// get primary key
			pStmt= conn.prepareStatement(sqlPKName);
			pStmt.setString(1, tabName);
			rs = pStmt.executeQuery();
			if (rs.next()) {
				tab.setPkName(rs.getString("CONSTRAINT_NAME"));
			}
			rs.close();
			pStmt.close();
			
			pStmt= conn.prepareStatement(sqlPKColumns);
			pStmt.setString(1, tab.getPkName());
			rs = pStmt.executeQuery();
			while (rs.next()) {
				tab.getPkColumns().add(rs.getString("COLUMN_NAME"));
			}
			rs.close();
			pStmt.close();
			// get reference info 
			 
			pStmt= conn.prepareStatement(sqlFKNames);
			pStmt.setString(1, tab.getPkName());
			rs = pStmt.executeQuery();
			while (rs.next()) {
				SimpleTableReference ref = new SimpleTableReference();
				ref.setParentTableName(tabName);
				ref.setTableName(rs.getString("TABLE_NAME"));
				ref.setReferenceCode(rs.getString("CONSTRAINT_NAME"));
				tab.getReferences().add(ref );
			}
			rs.close();
			pStmt.close();
			// get reference detail
			for(Iterator<SimpleTableReference> it= tab.getReferences().iterator();it.hasNext(); ){
				SimpleTableReference ref = it.next();
				pStmt= conn.prepareStatement(sqlFKColumns);
				pStmt.setString(1,ref.getReferenceCode());
				rs = pStmt.executeQuery();
				while (rs.next()) {
					SimpleTableField field = new SimpleTableField();
					field.setColumnName(rs.getString("COLUMN_NAME"));
					field.setColumnType(rs.getString("DATA_TYPE"));
					field.setMaxLength(rs.getInt("DATA_LENGTH"));
					field.setPrecision(rs.getInt("DATA_PRECISION"));
					field.setScale(rs.getInt("DATA_SCALE"));
					field.setNullEnable(rs.getString("NULLABLE"));
					field.mapToMetadata();
					
					ref.getFkColumns().add(field);
				}
				rs.close();
				pStmt.close();
			}
			//conn.close();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);//e.printStackTrace();
		} finally{
			try{
				if(pStmt!=null)
					pStmt.close();
				if(rs!=null)
					pStmt.close();
			} catch (Exception e) {
				logger.error(e.getMessage(),e);//e.printStackTrace();
			} 
		}
		return tab;
	}
	
}
