package com.centit.support.database.metadata;

import java.util.Map;

public interface TableReference {
	/**
	 * 约束代码
	 * @return
	 */
	public String getReferenceCode();
	/**
	 * 约束名称
	 * @return
	 */
	public String getReferenceName();
	/**
	 * 表名称
	 * @return
	 */
	public String getTableName();
	
	/**
	 * 父表表名称
	 * @return
	 */
	public String getParentTableName();
	
	/*
	 * 这个只有sql server 有用，其他可以忽略
	 * @return
	 */
	//public int getObjectId() ;
	/**
	 * 主键外键对应关系
	 * @return
	 */
	public Map<String, String> getReferenceColumns() ;
	
	/**
	 * 判断某个字段是否是外键
	 * @param sCol
	 * @return
	 */
	public boolean containColumn(String sCol);
}
