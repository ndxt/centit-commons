package com.centit.support.database;

import java.util.HashSet;
import java.util.Set;

public enum DBType {
	Unknown,SqlServer,Oracle,DB2,Access,MySql;
	
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
		if("mysql".equalsIgnoreCase(connurl)
			||	connurl.startsWith("jdbc:mysql"))
			return MySql;
		if("access".equalsIgnoreCase(connurl))
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
		return Unknown;
	}
	
	public static Set<DBType> allValues()
	{
		//DBType.values();
		Set<DBType> dbtypes = new HashSet<DBType>();
		dbtypes.add(Oracle);
		dbtypes.add(DB2);
		dbtypes.add(SqlServer);
		dbtypes.add(MySql);
		dbtypes.add(Access);		
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
	  		return "org.gjt.mm.mysql.Driver";
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
	  	default:
	  		return "unknown";
	  }
	}
}
