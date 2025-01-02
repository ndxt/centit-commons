package com.centit.support.report;

import com.alibaba.fastjson2.JSONArray;
import com.centit.support.file.FileIOOpt;

import java.io.IOException;
import java.io.InputStream;

public class TestExceIExport2 {
    public static void main(String[] args) throws IOException {
        String sheetName = "test";
        String[] header = {"姓名", "年龄"};
        String[] property = {"name", "age"};
        JSONArray jsonArray = JSONArray.parse("[{\"name\":\"john\",\"age\":1},{\"name\":\"make\",\"age\":2}]");
        InputStream inputStream = ExcelExportUtil.generateExcelStream(sheetName, jsonArray, header, property);
        FileIOOpt.writeInputStreamToFile(inputStream, "/Users/codefan/Documents/temp/nameage.xlsx");
    }
}
