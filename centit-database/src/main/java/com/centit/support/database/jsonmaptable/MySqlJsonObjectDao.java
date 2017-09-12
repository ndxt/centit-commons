package com.centit.support.database.jsonmaptable;

import com.alibaba.fastjson.JSONArray;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.database.metadata.TableInfo;
import com.centit.support.database.utils.DatabaseAccess;
import com.centit.support.database.utils.QueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MySqlJsonObjectDao extends GeneralJsonObjectDao {
	
	public MySqlJsonObjectDao(){
		
	}	
	
	public MySqlJsonObjectDao(Connection conn) {
		super(conn);
	}	
	
	public MySqlJsonObjectDao(TableInfo tableInfo) {
		super(tableInfo);
	}
	
	public MySqlJsonObjectDao(Connection conn,TableInfo tableInfo) {
		super(conn,tableInfo);
	}
	
	@Override
	public JSONArray listObjectsByProperties(final Map<String, Object> properties,
			final int startPos,final int maxSize)
			throws SQLException, IOException {		
		TableInfo tableInfo = this.getTableInfo();		
		Pair<String,String[]> q = buildFieldSqlWithFieldName(tableInfo,null);
		String filter = buildFilterSql(tableInfo,null,properties.keySet());
		String sql = "select " + q.getLeft() +" from " +tableInfo.getTableName();
		if(StringUtils.isNotBlank(filter))
			sql = sql + " where " + filter;
		if(StringUtils.isNotBlank(tableInfo.getOrderBy()))
			sql = sql + " order by " + tableInfo.getOrderBy();
		return DatabaseAccess.findObjectsByNamedSqlAsJSON(
					getConnect(),
					QueryUtils.buildMySqlLimitQuerySQL(
							sql,
							startPos, maxSize, false),
				 properties,
				 q.getRight());
	}

	/**
	 * 要使用这个函数首先需要在数据库中创建一下表和存储过程
	 *
	 DROP TABLE IF EXISTS f_mysql_sequence;

	 CREATE TABLE  f_mysql_sequence (
	 name varchar(50) NOT NULL,
	 currvalue int(11) NOT NULL,
	 increment int(11) NOT NULL DEFAULT '1',
	 primary key (name)
	 ) ENGINE=MyISAM DEFAULT CHARSET=utf8 CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC COMMENT='序列表，命名s_[table_name]';

	 DROP FUNCTION IF EXISTS sequence_currval;

	 DELIMITER //

	 CREATE  FUNCTION sequence_currval(seq_name VARCHAR(50)) RETURNS int(11)
	 READS SQL DATA
	 DETERMINISTIC
	 BEGIN
	 DECLARE cur_value INTEGER;
	 SET cur_value = 0;
	 SELECT currvalue INTO cur_value FROM f_mysql_sequence WHERE NAME = seq_name;
	 RETURN cur_value;
	 END//

	 DELIMITER ;

	 DROP FUNCTION IF EXISTS sequence_nextval;

	 DELIMITER //

	 CREATE  FUNCTION sequence_nextval(seq_name VARCHAR(50)) RETURNS int(11)
	 DETERMINISTIC
	 BEGIN
	 DECLARE cur_value INTEGER;
	 UPDATE f_mysql_sequence SET currvalue = currvalue + increment WHERE NAME = seq_name;
	 SELECT currvalue INTO cur_value FROM f_mysql_sequence WHERE NAME = seq_name;
	 RETURN cur_value;
	 END//

	 DELIMITER ;

	 DROP FUNCTION IF EXISTS sequence_setval;

	 DELIMITER //

	 CREATE  FUNCTION sequence_setval(seq_name VARCHAR(50),seq_value int(11)) RETURNS int(11)
	 DETERMINISTIC
	 BEGIN
	 UPDATE f_mysql_sequence SET currvalue = seq_value WHERE NAME = seq_name;
	 RETURN seq_value;
	 END//

	 DELIMITER ;

	 * @param sequenceName 序列名称
	 * @return  返回当前序列
	 * @throws SQLException SQLException
	 * @throws IOException IOException
	 */
	@Override
	public Long getSequenceNextValue(final String sequenceName) throws SQLException, IOException {
		//return getSimulateSequenceNextValue(sequenceName);
		Object object = DatabaseAccess.getScalarObjectQuery(
				 getConnect(),
				 "SELECT sequence_nextval ('"+ sequenceName+"')");
		return NumberBaseOpt.castObjectToLong(object);
	}
	
	@Override
	public List<Object[]> findObjectsBySql(final String sSql, final Object[] values, 
			final int pageNo, final int pageSize)
			throws SQLException, IOException {
		int startPos=pageNo>1?(pageNo-1)*pageSize:0;
		return DatabaseAccess.findObjectsBySql(
				getConnect(),
				QueryUtils.buildMySqlLimitQuerySQL(
						sSql,
						startPos, pageSize,false),
				values);
	}	

	@Override
	public List<Object[]> findObjectsByNamedSql(final String sSql, final Map<String, Object> values, 
			final int pageNo,final int pageSize) throws SQLException, IOException {
		int startPos=pageNo>1?(pageNo-1)*pageSize:0;
		return DatabaseAccess.findObjectsByNamedSql(
				getConnect(),
				QueryUtils.buildMySqlLimitQuerySQL(
						sSql,
						startPos, pageSize,false),
				values);
	}

	@Override
	public JSONArray findObjectsAsJSON(final String sSql, final Object[] values, final String[] fieldnames,
			final int pageNo, final int pageSize)
			throws SQLException, IOException {
		int startPos=pageNo>1?(pageNo-1)*pageSize:0;
		return DatabaseAccess.findObjectsAsJSON(
				getConnect(),
				QueryUtils.buildMySqlLimitQuerySQL(
						sSql,
						startPos, pageSize,false),
				values,fieldnames);
	}

	@Override
	public JSONArray findObjectsByNamedSqlAsJSON(final String sSql, final Map<String, Object> values,
			final String[] fieldnames, final int pageNo, final int pageSize) throws SQLException, IOException {
		int startPos=pageNo>1?(pageNo-1)*pageSize:0;
		return DatabaseAccess.findObjectsByNamedSqlAsJSON(
				getConnect(),
				QueryUtils.buildMySqlLimitQuerySQL(
						sSql,
						startPos, pageSize,false),
				values,fieldnames);
	}
}
