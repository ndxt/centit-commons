package com.centit.test;

import java.sql.Connection;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.DbcpConnectPools;
import com.centit.support.database.utils.QueryUtils;
import com.centit.support.database.ddl.DB2DDLOperations;
import com.centit.support.database.ddl.DDLOperations;
import com.centit.support.database.ddl.GeneralDDLOperations;
import com.centit.support.database.ddl.MySqlDDLOperations;
import com.centit.support.database.ddl.OracleDDLOperations;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.SimpleTableInfo;

public class TestJDBCDDL {
	
	public  static void  main(String[] args)   {
		//testJDBCMetadata();
		//testDB2JDBCMetadata();
		//testMysqlJDBCMetadata();
		System.out.println(GeneralDDLOperations.checkLabelName("hello_world"));
		System.out.println(GeneralDDLOperations.checkLabelName("9hello"));
		System.out.println(GeneralDDLOperations.checkLabelName("_hell_o234"));
		System.out.println(GeneralDDLOperations.checkLabelName("hello$abc"));
		System.out.println(GeneralDDLOperations.checkLabelName("hello abc"));
		System.out.println(GeneralDDLOperations.checkLabelName("你好"));
	}
  
	public  static void testJDBCMetadata(){
		 DataSourceDescription dbc = new DataSourceDescription();	  
		  dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
		  dbc.setUsername("fdemo2");
		  dbc.setPassword("fdemo2");
		  try {
			  Connection conn= DbcpConnectPools.getDbcpConnect(dbc);
			JSONArray ja = DatabaseAccess.fetchResultSetToJSONArray(
					conn.getMetaData().getTables(null, "FDEMO2","F_USERINFO", null) , null);			
			System.out.println(ja.toJSONString());
			
			ja = DatabaseAccess.fetchResultSetToJSONArray(
					conn.getMetaData().getColumns(null, "FDEMO2", "F_USERINFO", null), null);			
			System.out.println(ja.toJSONString());
						
			System.out.println(conn.getMetaData().getDatabaseProductName());
			System.out.println(conn.getMetaData().getDriverName());
			
			SimpleTableInfo tableinfo = new SimpleTableInfo("ddltemp");
			List<SimpleTableField> tf = tableinfo.getColumns();
			
			SimpleTableField tablfile = new SimpleTableField();
			tablfile.setColumnName("c1");
			tablfile.setColumnType("varchar");
			tablfile.setMaxLength(20);
			tablfile.setNullEnable("0");
			tf.add(tablfile);
			
			SimpleTableField tablfiletp = new SimpleTableField();
			tablfiletp.setColumnName("d1");
			tablfiletp.setColumnType("DATE");
			tablfile.setNullEnable("0");
			tf.add(tablfiletp);
			
			SimpleTableField tablfilef = new SimpleTableField();
			tablfilef.setColumnName("f1");
			tablfilef.setColumnType("DECIMAL");
            tablfilef.setPrecision(8);
            tablfilef.setScale(2);  
			tf.add(tablfilef);			
			tableinfo.getPkColumns().add("c1");
			tableinfo.getPkColumns().add("d1");
			DDLOperations ddl = new OracleDDLOperations(conn);
			ddl.dropTable("ddltemp");
			ddl.createTable(tableinfo);	
			
			SimpleTableField tablfilef2 = new SimpleTableField();
			tablfilef2.setColumnName("f2");
			tablfilef2.setColumnType("DECIMAL");
			tablfilef2.setPrecision(8);
			tablfilef2.setScale(2);  			
			ddl.addColumn("ddltemp", tablfilef2);
			ddl.dropColumn("ddltemp", "f1");
			tablfilef2.setPrecision(10);
			tablfilef2.setScale(2);  			
			ddl.modifyColumn("ddltemp", tablfilef2);
			
			tablfilef2.setColumnType("varchar");
			tablfilef2.setMaxLength(200);			
			ddl.reconfigurationColumn("ddltemp", "f2", tablfilef2);
			conn.close();
			
		} catch (Exception e) {
			//e.printStackTrace();
		}
	    System.out.println("done!");
	}
	
	public  static void testDataSource(){
	  DataSourceDescription dbc = new DataSourceDescription();	  
	  dbc.setConnUrl("jdbc:oracle:thin:@192.168.131.81:1521:orcl");
	  dbc.setUsername("fdemo2");
	  dbc.setPassword("fdemo2");
	  /* String sql = "select loginName,userName from f_userinfo " +
		        "where [:(creepforin)userCodes| usercode in (:userCodes)]";*/
	  
	  String sql = "select loginName,userName from f_userinfo " +
		        "where usercode in (:userCodes)";
	  /*QueryAndParams qp = QueryAndParams.createFromQueryAndNamedParams(sql,
			 QueryUtils.createSqlParamsMap("userCodes",new Object[]{"U0000041","U0001013"}));*/
	  try {
		  Connection conn= DbcpConnectPools.getDbcpConnect(dbc);
			JSONArray ja = DatabaseAccess.findObjectsByNamedSqlAsJSON(conn, sql,  QueryUtils.createSqlParamsMap("userCodes",new Object[]{"U0000041","U0001013"}));
			conn.close();
			System.out.println(ja.toJSONString());
		} catch (Exception e) {
			//e.printStackTrace();
		}
	  }
	
	public  static void testDB2JDBCMetadata(){
		  DataSourceDescription dbc = new DataSourceDescription();	  
		  dbc.setConnUrl("jdbc:db2://192.168.128.14:50000/jsmsa");
		  dbc.setUsername("ns");
		  dbc.setPassword("xxb@200914");
		  try {
			  Connection conn= DbcpConnectPools.getDbcpConnect(dbc);
						
			SimpleTableInfo tableinfo = new SimpleTableInfo("ddltemp");
			List<SimpleTableField> tf = tableinfo.getColumns();

			SimpleTableField tablfile = new SimpleTableField();
			tablfile.setColumnName("c1");
			tablfile.setColumnType("varchar");
			tablfile.setMaxLength(20);
			tablfile.setNullEnable("0");
			tf.add(tablfile);

			SimpleTableField tablfiletp = new SimpleTableField();
			tablfiletp.setColumnName("d1");
			tablfiletp.setColumnType("timestamp");
			tablfiletp.setNullEnable("0");
			tf.add(tablfiletp);

			SimpleTableField tablfilef = new SimpleTableField();
			tablfilef.setColumnName("f1");
			tablfilef.setColumnType("DECIMAL");
			tablfilef.setPrecision(8);
			tablfilef.setScale(2);
			tf.add(tablfilef);
			tableinfo.getPkColumns().add("c1");
			tableinfo.getPkColumns().add("d1");
			DDLOperations ddl = new DB2DDLOperations(conn);
			ddl.dropTable("ddltemp");
			ddl.createTable(tableinfo);
			conn.commit();
			SimpleTableField tablfilef2 = new SimpleTableField();
			tablfilef2.setColumnName("fest2");
			tablfilef2.setColumnType("DECIMAL");
			tablfilef2.setPrecision(8);
			tablfilef2.setScale(2);
			ddl.addColumn("ddltemp", tablfilef2);
			conn.commit();
			ddl.dropColumn("ddltemp", "f1");
			conn.commit();
			tablfilef2.setPrecision(10);
			tablfilef2.setScale(2);
			ddl.modifyColumn("ddltemp", tablfilef2);
			conn.commit();
			tablfilef2.setColumnType("varchar");
			tablfilef2.setMaxLength(200);
			ddl.reconfigurationColumn("ddltemp", "fest2", tablfilef2);
			conn.commit();
			tablfilef2.setColumnName("f3");
			ddl.renameColumn("ddltemp", "fest2",  tablfilef2);
			conn.commit();
			conn.close();
			
		} catch (Exception e) {
			//e.printStackTrace();
		}
	    System.out.println("done!");
	}
	
	public  static void testMysqlJDBCMetadata(){
		  DataSourceDescription dbc = new DataSourceDescription();	  
		  dbc.setConnUrl("jdbc:mysql://192.168.131.44:3306/yhgxpt");
		  dbc.setUsername("root");
		  dbc.setPassword("centit.1");
		  try {
			  Connection conn= DbcpConnectPools.getDbcpConnect(dbc);
						
			SimpleTableInfo tableinfo = new SimpleTableInfo("ddltemp");
			List<SimpleTableField> tf = tableinfo.getColumns();
			
			SimpleTableField tablfile = new SimpleTableField();
			tablfile.setColumnName("c1");
			tablfile.setColumnType("varchar");
			tablfile.setMaxLength(20);
			tablfile.setNullEnable("0");
			tf.add(tablfile);
			
			SimpleTableField tablfiletp = new SimpleTableField();
			tablfiletp.setColumnName("d1");
			tablfiletp.setColumnType("timestamp");
			tablfiletp.setNullEnable("0");
			tf.add(tablfiletp);
			
			SimpleTableField tablfilef = new SimpleTableField();
			tablfilef.setColumnName("f1");
			tablfilef.setColumnType("DECIMAL");
          tablfilef.setPrecision(8);
          tablfilef.setScale(2);  
			tf.add(tablfilef);			
			tableinfo.getPkColumns().add("c1");
			tableinfo.getPkColumns().add("d1");
			DDLOperations ddl = new MySqlDDLOperations(conn);
			ddl.dropTable("ddltemp");
			ddl.createTable(tableinfo);	
			conn.commit();
			SimpleTableField tablfilef2 = new SimpleTableField();
			tablfilef2.setColumnName("fest2");
			tablfilef2.setColumnType("DECIMAL");
			tablfilef2.setPrecision(8);
			tablfilef2.setScale(2);  			
			ddl.addColumn("ddltemp", tablfilef2);
			conn.commit();
			ddl.dropColumn("ddltemp", "f1");
			conn.commit();
			tablfilef2.setPrecision(10);
			tablfilef2.setScale(2);  			
			ddl.modifyColumn("ddltemp", tablfilef2);
			conn.commit();			
			tablfilef2.setColumnType("varchar");
			tablfilef2.setMaxLength(200);			
			ddl.reconfigurationColumn("ddltemp", "fest2", tablfilef2);	
			conn.commit();
			tablfilef2.setColumnName("F3");
			ddl.renameColumn("ddltemp", "fest2",  tablfilef2);	
			conn.commit();
			conn.close();
			
		} catch (Exception e) {
			//e.printStackTrace();
		}
	    System.out.println("done!");
	}	
}
