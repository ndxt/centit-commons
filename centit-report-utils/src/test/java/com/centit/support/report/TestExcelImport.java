package com.centit.support.report;

import com.alibaba.fastjson2.JSON;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-9-23.
 */
public class TestExcelImport {

    public static void main(String[] args) throws Exception {
        //"/home/codefan/temp/2018.03.01-2018.04.30.xlsx",
        // "/home/codefan/下载/北京.xlsx"
        InputStream s = new FileInputStream(new File("/Users/codefan/Documents/temp/ceshi.xlsx"));
        List<Map<String, Object>> ss = ExcelImportUtil.loadMapFromExcelSheetUseMergeCell(s,
            0,1,2,100,1,7);
        System.out.println(JSON.toJSONString(ss));
        /*System.out.println(ExcelImportUtil.mapColumnIndex("A"));
        System.out.println(ExcelImportUtil.mapColumnIndex("AA"));
        System.out.println(ExcelImportUtil.mapColumnIndex("AAA"));
        System.out.println(ExcelImportUtil.mapColumnIndex("ZZ"));
        System.out.println(ExcelImportUtil.mapColumnIndex("CAA"));
        System.out.println(ExcelImportUtil.mapColumnIndex("BZZ"));
        System.out.println(ExcelImportUtil.mapColumnIndex("BAAA"));
        System.out.println(ExcelImportUtil.mapColumnIndex("AZZZ"));*/
    }

}
