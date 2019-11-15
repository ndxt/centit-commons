package com.centit.support.database.jsonmaptable;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DatabaseAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class PostgreSqlJsonObjectDao extends GeneralJsonObjectDao {

    public PostgreSqlJsonObjectDao(){

    }

    public PostgreSqlJsonObjectDao(Connection conn) {
        super(conn);
    }

    public PostgreSqlJsonObjectDao(TableInfo tableInfo) {
        super(tableInfo);
    }

    public PostgreSqlJsonObjectDao(Connection conn, TableInfo tableInfo) {
        super(conn,tableInfo);
    }

    // nextval currval
    @Override
    public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        Object object = DatabaseAccess.getScalarObjectQuery(
                 getConnect(),
                 "SELECT nextval('" + sequenceName + "')");
        return NumberBaseOpt.castObjectToLong(object);
    }


}
