package com.centit.test.transaction;

import com.centit.support.algorithm.UuidOpt;
import com.centit.support.database.utils.DBType;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接信息，由于是使用的原生SQL(Native SQL)需要根据数据库类型做一些适应性操作，所有数据库类型信息
 * @author codefan
 *
 */
public interface DBConnect extends Connection{

    String getDatabaseCode();

    DBType getDatabaseType();

    Connection getConn();

    boolean startTransaction();

    boolean isInTransaction();

    static DBConnect valueOf(Connection conn){
        if( conn instanceof DBConnect){
            return (DBConnect)conn;
        }
        String dataCode;
        try {
            dataCode = conn.getSchema();
        } catch (SQLException e) {
            dataCode = UuidOpt.getUuidAsString32();
        }
        DbcpConnect dbcpConn = new DbcpConnect(dataCode, conn );
        return dbcpConn;
    }
}
