package com.centit.support.database.orm;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by codefan on 17-8-29.
 */
public interface FetchDataWork<T> {
    T execute(ResultSet rs) throws SQLException, IOException,NoSuchFieldException,
            InstantiationException, IllegalAccessException;
}
