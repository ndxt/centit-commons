package com.centit.test.transaction;

import com.alibaba.fastjson.JSONObject;
import com.centit.support.database.jsonmaptable.JsonObjectDao;
import com.centit.support.database.jsonmaptable.OracleJsonObjectDao;
import com.centit.support.database.metadata.SimpleTableField;
import com.centit.support.database.metadata.SimpleTableInfo;

import java.sql.SQLException;

public class TestServiceImpl implements TestService{
	
	protected SimpleTableInfo tableInfo;

	public TestServiceImpl(){
		tableInfo = new SimpleTableInfo("TEST_TABLE");
		SimpleTableField field = new SimpleTableField();
		field.setColumnName("ID");
		field.setColumnType("Number(10)");
		field.setPrecision(10);
		field.setScale(0);
		field.setMandatory(true);
		field.setPropertyName("id");
		tableInfo.getColumns().add(field);
		
		field = new SimpleTableField();
		field.setColumnName("USER_NAME");
		field.setColumnType("varchar2");
		field.setPropertyName("userName");
		field.setMaxLength(50);
		tableInfo.getColumns().add(field);
		
		field = new SimpleTableField();
		field.setColumnName("USER_PHONE");
		field.setColumnType("varchar2");
		field.setMaxLength(20);
		field.setPropertyName("userPhone");
		field.setDefaultValue("'110'");
		tableInfo.getColumns().add(field);			
		
		tableInfo.getPkColumns().add("ID");
	}
	
	@Override	
	public void insertUser(DBConnect conn, JSONObject userInfo) throws SQLException {
		JsonObjectDao dao = new OracleJsonObjectDao(conn,tableInfo);
		dao.saveNewObject(userInfo);
	}

}
