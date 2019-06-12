package com.centit.support.database.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

public enum DBType {
    Unknown,SqlServer,Oracle,DB2,Access,MySql,H2,PostgreSql;
    protected static final Logger logger = LoggerFactory.getLogger(DBType.class);

    public static DBType valueOf(int ordinal){

        switch(ordinal){
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
          default:
              return Unknown;
      }
    }


    public static DBType mapDBType(String connurl){
        if(connurl==null)
            return Unknown;
        if("oracle".equalsIgnoreCase(connurl)
            ||    connurl.startsWith("jdbc:oracle"))
            return Oracle;
        if("db2".equalsIgnoreCase(connurl)
            ||    connurl.startsWith("jdbc:db2"))
            return DB2;
        if("sqlserver".equalsIgnoreCase(connurl)
            ||    connurl.startsWith("jdbc:sqlserver"))
            return SqlServer;
        if("h2".equalsIgnoreCase(connurl)
            ||    connurl.startsWith("jdbc:h2"))
            return H2;
        if("mysql".equalsIgnoreCase(connurl)
                ||    connurl.startsWith("jdbc:mysql"))
            return MySql;
        if("access".equalsIgnoreCase(connurl)
                ||    connurl.startsWith("jdbc:ucanaccess"))
            return Access;
        if("postgresql".equalsIgnoreCase(connurl)
                ||    connurl.startsWith("jdbc:postgresql"))
            return PostgreSql;
        return Unknown;
    }

    public static DBType mapDBType(Connection conn){
        try {
            return mapDBType(conn.getMetaData().getURL());
        }catch (Exception e){
            logger.error(e.getMessage(),e);//e.printStackTrace();
            return Unknown;
        }
    }

    public static DBType mapDialectToDBType(String dialectName){
        if(dialectName==null)
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
        return Unknown;
    }

    public static Set<DBType> allValues()
    {
        //DBType.values();
        Set<DBType> dbtypes = new HashSet<>();
        dbtypes.add(Oracle);
        dbtypes.add(DB2);
        dbtypes.add(SqlServer);
        dbtypes.add(MySql);
        dbtypes.add(Access);
        dbtypes.add(H2);
        dbtypes.add(PostgreSql);
        return dbtypes;
    }

    public static String getDbDriver(DBType dt)
    {
        switch(dt){
            case Oracle:
                return "oracle.jdbc.driver.OracleDriver";
            case DB2:
                return "com.ibm.db2.jdbc.app.DB2Driver";
            case SqlServer:
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case Access:
                return "net.ucanaccess.jdbc.UcanaccessDriver";
            case MySql:
                return "com.mysql.jdbc.Driver";
            case H2:
                return "org.h2.Driver";
            case PostgreSql:
                return "org.postgresql.Driver";
            default:
                return "";
        }
    }

    public static String getDBTypeName(DBType type){
        //return type.toString();
        switch(type){
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
            default:
                return "unknown";
        }
    }

    @Override
    public String toString(){
        return DBType.getDBTypeName(this);
    }
}
