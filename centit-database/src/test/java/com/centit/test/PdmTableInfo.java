package com.centit.test;

import java.util.List;

import com.centit.support.database.metadata.SimpleTableField;
import org.apache.commons.lang3.tuple.Pair;

import com.centit.support.database.metadata.PdmReader;
import com.centit.support.database.metadata.SimpleTableInfo;

public class PdmTableInfo{
	public  static void  main(String[] args)   {
		SimpleTableField field = new SimpleTableField();
		field.setJavaType(byte[].class);
		System.out.println(field.getJavaType());
		//System.out.println(DBType.valueOf("Oracle"));
		/*List<Pair<String, String>> tables = listTablesInPdm("D:/temp/im.pdm");
		for(Pair<String, String> t : tables )
			System.out.println("table "+ t.getKey() +" name "+ t.getValue());
		System.out.println("Done!");*/
	}
	public static List<Pair<String, String>> listTablesInPdm(String pdmFilePath) {
		PdmReader pdmReader = new PdmReader();
		if(!pdmReader.loadPdmFile(pdmFilePath))
			return null;
		
		return pdmReader.getAllTableCode();
	}

	public static SimpleTableInfo importTableFromPdm(String pdmFilePath, String tableCode) {
		PdmReader pdmReader = new PdmReader();
		if(!pdmReader.loadPdmFile(pdmFilePath))
			return null;
		return pdmReader.getTableMetadata(tableCode);
	}

}
