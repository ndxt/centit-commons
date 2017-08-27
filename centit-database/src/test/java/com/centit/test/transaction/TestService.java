package com.centit.test.transaction;

import com.alibaba.fastjson.JSONObject;
import com.centit.test.transaction.DBConnect;
import com.centit.test.transaction.Transactional;

import java.sql.SQLException;

public interface TestService {
	@Transactional
	public void insertUser(DBConnect conn, JSONObject userInfo) throws SQLException;
}
