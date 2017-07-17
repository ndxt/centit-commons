package com.centit.support.database.metadata;

import java.util.Map;

public interface TableReference {
	/**
	 *
	 * @return 约束代码
	 */
	String getReferenceCode();
	/**
	 *
	 * @return 约束名称
	 */
	String getReferenceName();
	/**
	 *
	 * @return 表名称
	 */
	String getTableName();
	
	/**
	 *
	 * @return 父表表名称
	 */
	String getParentTableName();
	
	/*
	 *
	 * @return 这个只有sql server 有用，其他可以忽略
	 */
	//int getObjectId() ;
	/**
	 *
	 * @return 主键外键对应关系
	 */
	Map<String, String> getReferenceColumns() ;
	
	/**
	 * 判断某个字段是否是外键
	 * @param sCol 字段
	 * @return  是否是外键
	 */
	boolean containColumn(String sCol);
}
