package com.centit.support.database.jsonmaptable;

import com.centit.support.database.metadata.TableInfo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class SqlSvrJsonObjectDao extends GeneralJsonObjectDao {

    public SqlSvrJsonObjectDao(){

    }

    public SqlSvrJsonObjectDao(Connection conn) {
        super(conn);
    }

    public SqlSvrJsonObjectDao(TableInfo tableInfo) {
        super(tableInfo);
    }

    public SqlSvrJsonObjectDao(Connection conn,TableInfo tableInfo) {
        super(conn,tableInfo);
    }

    /** 用表来模拟sequence
     * create table simulate_sequence (seqname varchar(100) not null primary key,
     * currvalue integer, increment integer);
     *
     * @param sequenceName sequenceName
     * @return Long
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    @Override
    public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        return getSimulateSequenceNextValue(sequenceName);
    }

}
