package com.centit.support.database.jsonmaptable;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DatabaseAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class OracleJsonObjectDao extends GeneralJsonObjectDao {

    public OracleJsonObjectDao() {

    }

    public OracleJsonObjectDao(Connection conn) {
        super(conn);
    }

    public OracleJsonObjectDao(TableInfo tableInfo) {
        super(tableInfo);
    }

    public OracleJsonObjectDao(Connection conn, TableInfo tableInfo) {
        super(conn, tableInfo);
    }


    @Override
    public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        Object object = DatabaseAccess.getScalarObjectQuery(
            getConnect(),
            "SELECT " + sequenceName + ".nextval from dual");
        return NumberBaseOpt.castObjectToLong(object);
    }

}
