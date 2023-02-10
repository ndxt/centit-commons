package com.centit.support.report;

/**
 * Created by codefan on 17-9-23.
 */
public class TestExcelImport {

    public static void main(String[] args) throws Exception {
        //"/home/codefan/temp/2018.03.01-2018.04.30.xlsx",
        // "/home/codefan/下载/北京.xlsx"
        //InputStream s=new FileInputStream(new File("d:\\出差.xlsx"));
        // List<Map<String, Object>> ss=ExcelImportUtil.loadMapFromExcelSheet(s,0);
        System.out.println(ExcelImportUtil.mapColumnIndex("A"));
        System.out.println(ExcelImportUtil.mapColumnIndex("AA"));
        System.out.println(ExcelImportUtil.mapColumnIndex("AAA"));
        System.out.println(ExcelImportUtil.mapColumnIndex("ZZ"));
        System.out.println(ExcelImportUtil.mapColumnIndex("CAA"));
        System.out.println(ExcelImportUtil.mapColumnIndex("BZZ"));
        System.out.println(ExcelImportUtil.mapColumnIndex("BAAA"));
        System.out.println(ExcelImportUtil.mapColumnIndex("AZZZ"));
    }

}
