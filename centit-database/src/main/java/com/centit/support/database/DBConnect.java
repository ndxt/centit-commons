package com.centit.support.database;

import java.sql.Connection;

/**
 * 数据库连接信息，由于是使用的原生SQL(Native SQL)需要根据数据库类型做一些适应性操作，所有数据库类型信息
 * @author codefan
 *
 */
public interface DBConnect extends Connection {
	
	String getDatabaseCode(); 
	
	DBType getDatabaseType();
	
	Connection getConn();
	
	boolean startTransaction();
	
	boolean isInTransaction();
	
}
