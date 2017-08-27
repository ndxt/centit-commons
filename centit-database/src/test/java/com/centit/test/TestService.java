package com.centit.test;

import java.sql.SQLException;

import com.alibaba.fastjson.JSONObject;
import com.centit.support.database.utils.DBConnect;
import com.centit.test.transaction.Transactional;

public interface TestService {
	@Transactional
	public void insertUser(DBConnect conn,JSONObject userInfo) throws SQLException;
}
