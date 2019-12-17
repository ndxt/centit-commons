package com.centit.support.database.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public enum DBType {
    Unknown, SqlServer, Oracle, DB2, Access, MySql, H2, PostgreSql, DM, KingBase, GBase, Oscar;
    protected static final Logger logger = LoggerFactory.getLogger(DBType.class);
    private static HashMap<DBType, String> dbDrivers = new HashMap<DBType, String>() {
        {
            put(Oracle, "oracle.jdbc.driver.OracleDriver");
            put(DB2, "com.ibm.db2.jdbc.app.DB2Driver");
            put(SqlServer, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
            put(Access, "net.ucanaccess.jdbc.UcanaccessDriver");
            put(MySql, "com.mysql.jdbc.Driver");
            put(H2, "org.h2.Driver");
            put(PostgreSql, "org.postgresql.Driver");
            put(DM, "dm.jdbc.driver.DmDriver");
            put(KingBase, "com.kingbase.Driver");
            put(GBase, "com.gbasedbt.jdbc.IfxDriver");
            put(Oscar, "com.oscar.Driver");
        }
    };

    public static DBType valueOf(int ordinal) {
        switch (ordinal) {
            case 1:
                return SqlServer;
            case 2:
                return Oracle;
            case 3:
                return DB2;
            case 4:
                return Access;
            case 5:
                return MySql;
            case 6:
                return H2;
            case 7:
                return PostgreSql;
            case 8:
                return DM;
            case 9:
                return KingBase;
            case 10:
                return GBase;
            case 11:
                return Oscar;
            default:
                return Unknown;
        }
    }
    /*
    public static DBType valueOf(String dbname) {
        switch (dbname.toLowerCase()) {
            case "sqlserver":
                return SqlServer;
            case "oracle":
                return Oracle;
            case "db2":
                return DB2;
            case "access":
                return Access;
            case "mysql":
                return MySql;
            case "h2":
                return H2;
            case "postgresql":
                return PostgreSql;
            case "dm":
                return DM;
            case "kingbase":
                return KingBase;
            default:
                return Unknown;
        }
    }*/

    public static DBType mapDBType(String connurl) {
        if (StringUtils.isBlank(connurl))
            return Unknown;
        if (connurl.startsWith("jdbc:oracle")
            || "oracle".equalsIgnoreCase(connurl))
            return Oracle;
        if (connurl.startsWith("jdbc:db2")
            || "db2".equalsIgnoreCase(connurl))
            return DB2;
        if (connurl.startsWith("jdbc:sqlserver")
            || "sqlserver".equalsIgnoreCase(connurl))
            return SqlServer;
        if (connurl.startsWith("jdbc:h2")
            || "h2".equalsIgnoreCase(connurl))
            return H2;
        if (connurl.startsWith("jdbc:mysql")
            || "mysql".equalsIgnoreCase(connurl))
            return MySql;
        if (connurl.startsWith("jdbc:ucanaccess")
            || "access".equalsIgnoreCase(connurl))
            return Access;
        if (connurl.startsWith("jdbc:postgresql")
            || "postgresql".equalsIgnoreCase(connurl))
            return PostgreSql;
        if (connurl.startsWith("jdbc:dm")
            || "dm".equalsIgnoreCase(connurl))
            return DM;
        if (connurl.startsWith("jdbc:kingbase")
            || "kingbase".equalsIgnoreCase(connurl))
            return KingBase;
        if (connurl.startsWith("jdbc:gbasedbt-sqli")
            || "gbasedbt-sqli".equalsIgnoreCase(connurl))
            return GBase;
        if (connurl.startsWith("jdbc:oscar")
            || "oscar".equalsIgnoreCase(connurl))
            return Oscar;
        return Unknown;
    }

    public static DBType mapDBType(Connection conn) {
        try {
            return mapDBType(conn.getMetaData().getURL());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
            return Unknown;
        }
    }

    public static DBType mapDialectToDBType(String dialectName) {
        if (dialectName == null)
            return Unknown;
        if (dialectName.contains("Oracle"))
            return Oracle;
        if (dialectName.contains("DB2"))
            return DB2;
        if (dialectName.contains("SQLServer"))
            return SqlServer;
        if (dialectName.contains("MySQL"))
            return MySql;
        //hibernate 不再对Access支持了
        if (dialectName.contains("Access"))
            return Access;
        if (dialectName.contains("H2"))
            return H2;
        if (dialectName.contains("PostgreSQL"))
            return PostgreSql;
        if (dialectName.contains("Dm"))
            return DM;
        if (dialectName.contains("KingBase"))
            return KingBase;
        if (dialectName.contains("GBase"))
            return GBase;
        if (dialectName.contains("Oscar"))
            return Oscar;
        return Unknown;
    }

    public static Set<DBType> allValues() {
        //DBType.values();
        Set<DBType> dbtypes = new HashSet<>();
        dbtypes.add(Oracle);
        dbtypes.add(DB2);
        dbtypes.add(SqlServer);
        dbtypes.add(MySql);
        dbtypes.add(Access);
        dbtypes.add(H2);
        dbtypes.add(PostgreSql);
        dbtypes.add(DM);
        dbtypes.add(KingBase);
        dbtypes.add(GBase);
        dbtypes.add(Oscar);
        return dbtypes;
    }

    public static String getDbDriver(DBType dt) {
        return dbDrivers.get(dt);
    }

    public static void setDbDriver(DBType dt, String driverClassName) {
        dbDrivers.put(dt, driverClassName);
    }

    public static String getDBTypeName(DBType type) {
        //return type.toString();
        switch (type) {
            case Oracle:
                return "oracle";
            case DB2:
                return "db2";
            case SqlServer:
                return "sqlserver";
            case Access:
                return "access";
            case MySql:
                return "mysql";
            case H2:
                return "h2";
            case PostgreSql:
                return "postgresql";
            case DM:
                return "dm";
            case KingBase:
                return "kingbase";
            case GBase:
                return "gbase";
            case Oscar:
                return "shentong";
            default:
                return "unknown";
        }
    }

    @Override
    public String toString() {
        return DBType.getDBTypeName(this);
    }

    public boolean isMadeInChina() {
        return DBType.DM.equals(this)
            || DBType.KingBase.equals(this)
            || DBType.GBase.equals(this)
            || DBType.Oscar.equals(this);
    }
}
