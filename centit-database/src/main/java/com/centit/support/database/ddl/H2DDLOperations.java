package com.centit.support.database.ddl;

import com.centit.support.database.utils.QueryUtils;

import java.sql.Connection;

/**
 * 使用H2数据库时请打开MySql兼容模式
 * jdbc:h2: &lt;url&gt;;MODE=&lt;databaseType&gt;
 * MODE=MySQL：兼容模式，H2兼容多种数据库，
 * 该值可以为：DB2、Derby、HSQLDB、MSSQLServer、MySQL、Oracle、PostgreSQL
 * <p>
 * http://www.h2database.com/html/features.html#compatibility
 */
public class H2DDLOperations extends MySqlDDLOperations {


    public H2DDLOperations() {

    }

    public H2DDLOperations(Connection conn) {
        super(conn);
    }


    @Override
    public String makeCreateSequenceSql(final String sequenceName) {
        return "create sequence " + QueryUtils.cleanSqlStatement(sequenceName);
    }

}
