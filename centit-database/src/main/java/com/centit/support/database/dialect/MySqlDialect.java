package com.centit.support.database.dialect;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.utils.DatabaseAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by codefan on 17-8-29.
 */
public class MySqlDialect implements Dialect{

    public Connection getConnection() {
        return conn;
    }

    protected Connection conn;

    public MySqlDialect(){

    }

    public MySqlDialect(Connection conn) {
        this.conn = conn;
    }

    public void setConnect(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Long getSequence(String sequenceName) throws IOException, SQLException {
        return NumberBaseOpt.castObjectToLong(
                DatabaseAccess.getScalarObjectQuery(conn,
                        "SELECT sequence_nextval('"+ sequenceName+"');"));
    }
}

