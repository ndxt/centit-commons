package com.centit.support.database.utils;

import java.sql.SQLException;

public class DatabaseAccessException extends SQLException {
	
	private static final long serialVersionUID = 1L;

	public DatabaseAccessException(String sql , SQLException e){
		 super(sql +" raise "+ e.getMessage(), e.getSQLState(), e.getErrorCode(),e.getCause());
		 this.setNextException(e.getNextException());
		 this.setStackTrace(e.getStackTrace());		
	}
}
