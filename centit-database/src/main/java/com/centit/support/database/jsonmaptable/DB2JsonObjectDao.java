package com.centit.support.database.jsonmaptable;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DatabaseAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DB2JsonObjectDao extends GeneralJsonObjectDao {

    public DB2JsonObjectDao(){

    }

    public DB2JsonObjectDao(Connection conn) {
        super(conn);
    }

    public DB2JsonObjectDao(TableInfo tableInfo) {
        super(tableInfo);
    }

    public DB2JsonObjectDao(Connection conn,TableInfo tableInfo) {
        super(conn,tableInfo);
    }


    @Override
    public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        Object object = DatabaseAccess.getScalarObjectQuery(
                 getConnect(),
                 "SELECT nextval for "
                         + sequenceName + " from sysibm.sysdummy1");
        return NumberBaseOpt.castObjectToLong(object);
    }

}
