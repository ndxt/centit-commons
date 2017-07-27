package com.centit.support.database;

import java.util.HashSet;
import java.util.Set;

public enum DBType {
	Unknown,SqlServer,Oracle,DB2,Access,MySql,H2;
	
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
	  	default:
	  		return Unknown;
	  }
	}
	
	public static DBType mapDBType(String connurl){
		if(connurl==null)
			return Unknown;
		if("oracle".equalsIgnoreCase(connurl)  
			||	connurl.startsWith("jdbc:oracle"))
			return Oracle;
		if("db2".equalsIgnoreCase(connurl)
			||	connurl.startsWith("jdbc:db2"))
			return DB2;
		if("sqlserver".equalsIgnoreCase(connurl)
			||	connurl.startsWith("jdbc:sqlserver"))
			return SqlServer;
		if("h2".equalsIgnoreCase(connurl)
			||	connurl.startsWith("jdbc:h2"))
			return H2;
		if("mysql".equalsIgnoreCase(connurl)
				||	connurl.startsWith("jdbc:mysql"))
			return MySql;
		if("access".equalsIgnoreCase(connurl)
				||	connurl.startsWith("jdbc:odbc:driver"))
			return Access;
		
		return Unknown;
	}
	
	public static DBType mapDialectToDBType(String dialectName){
		if(dialectName==null)
			return Unknown;
		if (dialectName.indexOf("Oracle")>=0)
				return Oracle;
		if (dialectName.indexOf("DB2")>=0)
			return DB2;
		if (dialectName.indexOf("SQLServer")>=0)
			return SqlServer;
		if (dialectName.indexOf("MySQL")>=0)
			return MySql;
		//hibernate 不再对Access支持了
		if (dialectName.indexOf("Access")>=0)
			return Access;
		if (dialectName.indexOf("H2")>=0)
			return H2;
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
	  		return "sun.jdbc.odbc.JdbcOdbcDriver";
	  	case MySql:
	  		return "com.mysql.jdbc.Driver";
	    case H2:
		    return "org.h2.Driver";
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
		default:
	  		return "unknown";
	  }
	}
}
