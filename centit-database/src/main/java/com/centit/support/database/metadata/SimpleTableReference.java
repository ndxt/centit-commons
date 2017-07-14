package com.centit.support.database.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTableReference implements TableReference{
	
	private String  parentTableName;
	private String  sTableName;
	private String  sReferenceName;
	private String  sReferenceCode;
	private List<SimpleTableField> fkColumns;
	
	private Map<String, String> referenceColumns;
	private int nObjectId; //only used by sqlserver
	
	public int getObjectId() {
		return nObjectId;
	}
	public void setObjectId(int objectId) {
		nObjectId = objectId;
	}
	
	public String getTableName() {
		return sTableName;
	}
	public void setTableName(String tableName) {
		sTableName = tableName;
	}
	public String getReferenceName() {
		return sReferenceName;
	}
	public void setReferenceCode(String referenceCode) {
		sReferenceCode = referenceCode;
	}
	
	public String getReferenceCode() {
		return sReferenceCode;
	}
	public void setReferenceName(String referenceName) {
		sReferenceName = referenceName;
	}
	public List<SimpleTableField> getFkColumns() {
		if(fkColumns==null)
			fkColumns = new ArrayList<SimpleTableField>();
		return fkColumns;
	}
	public void setFkColumns(List<SimpleTableField> fkcolumns) {
		this.fkColumns = fkcolumns;
	}
	
	public boolean containColumn(String sCol) {
		if(sCol==null || fkColumns==null || fkColumns.size() == 0)
			return false;
		for(SimpleTableField tf : fkColumns){
			if(sCol.equalsIgnoreCase(tf.getColumnName()))
				return true;
		}
		return false;
	}	
	
	public String getClassName() {
		String sClassName = SimpleTableField.mapPropName(sTableName);
		return sClassName.substring(0,1).toUpperCase() + 
				sClassName.substring(1);
	}
	@Override
	public Map<String, String> getReferenceColumns() {
		if(this.referenceColumns==null)
			this.referenceColumns = new HashMap<String, String>();
		return this.referenceColumns;
	}
	@Override
	public String getParentTableName() {
		return this.parentTableName;
	}
	public void setParentTableName(String parentTableName) {
		this.parentTableName = parentTableName;
	}
	public void setReferenceColumns(Map<String, String> referenceColumns) {
		this.referenceColumns = referenceColumns;
	}
}
