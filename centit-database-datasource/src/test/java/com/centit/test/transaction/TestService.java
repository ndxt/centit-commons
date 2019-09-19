package com.centit.test.transaction;

import com.alibaba.fastjson.JSONObject;

import java.sql.SQLException;

public interface TestService {
    @Transactional
    public void insertUser(DBConnect conn, JSONObject userInfo) throws SQLException;
}
