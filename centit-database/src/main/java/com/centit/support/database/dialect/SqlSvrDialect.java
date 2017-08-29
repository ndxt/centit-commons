package com.centit.support.database.dialect;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.QueryUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by codefan on 17-8-29.
 */
public class SqlSvrDialect implements Dialect{
    protected Connection conn;

    public SqlSvrDialect(){

    }

    public SqlSvrDialect(Connection conn) {
        this.conn = conn;
    }

    public void setConnect(Connection conn) {
        this.conn = conn;
    }

    public Connection getConnection() {
        return conn;
    }

    /**
     * 创建序列
     * 用表来模拟sequence
     * create table simulate_sequence (seqname varchar(100) not null primary key,
     * currvalue integer, increment integer);
     * "INSERT INTO simulate_sequence (seqname, currvalue , increment)"
     + " VALUES ("+ QueryUtils.buildStringForQuery(sequenceName)+", 0, 1)";
     * @param sequenceName 序列
     * @return Long
     */
    @Override
    public Long getSequence(String sequenceName) throws IOException, SQLException {
        DatabaseAccess.doExecuteSql(conn,"update simulate_sequence " +
                "set currvalue = currvalue + increment  where seqname = ?",
                new Object[]{sequenceName} );
        return NumberBaseOpt.castObjectToLong(
                DatabaseAccess.getScalarObjectQuery(conn,
                        "SELECT currvalue from simulate_sequence where seqname=?",sequenceName));
    }
}