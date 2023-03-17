package com.centit.support.report;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-9-23.
 */
public class TestExceIExport {

    public static void main(String[] args) throws Exception {
        //"/home/codefan/temp/2018.03.01-2018.04.30.xlsx",
        // "/home/codefan/下载/北京.xlsx"
        InputStream s = new FileInputStream(new File("/Users/codefan/Documents/temp/source.xlsx"));
        List<Map<String, Object>> ss = ExcelImportUtil.loadMapFromExcelSheetUseMergeCell(s,
            0,2,3,0,0,0);

        s = new FileInputStream(new File("/Users/codefan/Documents/temp/source.xlsx"));
        List<Map<String, Object>> sheet2Objects = ExcelImportUtil.loadMapFromExcelSheetUseMergeCell(s,
            1,0,1,0,0,0);
        //System.out.println(JSON.toJSONString(ss));

        InputStream template = new FileInputStream(new File("/Users/codefan/Documents/temp/template.xlsx"));
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(template);
        XSSFSheet sheet = xssfWorkbook.getSheetAt(0);

        Map<Integer, String> mapInfo = new HashMap<>();
        mapInfo.put(0, "地区");
        mapInfo.put(1, "机构名称");
        mapInfo.put(2, "机构性质");
        mapInfo.put(3, "机构性质备注");
        mapInfo.put(4, "隶属关系");
        mapInfo.put(5, "行政编制");
        mapInfo.put(6, "事业编制");

        ExcelExportUtil.saveObjectsToExcelSheet(sheet, ss, mapInfo, 3, true, -1);

        Map<Integer, String> mapInfo2 = new HashMap<>();
        mapInfo2.put(0, "地区");
        mapInfo2.put(1, "内设机构");
        mapInfo2.put(2, "机构性质");
        mapInfo2.put(3, "行政级别");
        mapInfo2.put(4, "级别备注");
        mapInfo2.put(5, "工作职责");
        mapInfo2.put(6, "编制数");
        sheet = xssfWorkbook.getSheetAt(1);
        ExcelExportUtil.saveObjectsToExcelSheet(sheet, sheet2Objects, mapInfo2, 1, true, 1);

        OutputStream os = new FileOutputStream( new File("/Users/codefan/Documents/temp/dest.xlsx"));
        xssfWorkbook.write(os);

        System.out.println("done!");
    }

}
