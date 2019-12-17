package com.centit.support.database.jsonmaptable;

import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DatabaseAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 使用H2数据库时请打开MySql兼容模式
 * jdbc:h2: &lt;url&gt;;MODE=&lt;databaseType&gt;
 * MODE=MySQL：兼容模式，H2兼容多种数据库，
 * 该值可以为：DB2、Derby、HSQLDB、MSSQLServer、MySQL、Oracle、PostgreSQL
 * <p>
 * http://www.h2database.com/html/features.html#compatibility
 */
public class H2JsonObjectDao extends MySqlJsonObjectDao {

    public H2JsonObjectDao() {

    }

    public H2JsonObjectDao(Connection conn) {
        super(conn);
    }

    public H2JsonObjectDao(TableInfo tableInfo) {
        super(tableInfo);
    }

    public H2JsonObjectDao(Connection conn, TableInfo tableInfo) {
        super(conn, tableInfo);
    }


    /**
     * 用表来模拟sequence
     * create table simulate_sequence (seqname varchar(100) not null primary key,
     * currvalue integer, increment integer);
     *
     * @param sequenceName sequenceName
     * @return Long
     * @throws SQLException SQLException
     * @throws IOException  IOException
     */
    @Override
    public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
        Object object = DatabaseAccess.getScalarObjectQuery(
            getConnect(),
            "SELECT NEXT VALUE FOR " + sequenceName);
        return NumberBaseOpt.castObjectToLong(object);
    }

}
