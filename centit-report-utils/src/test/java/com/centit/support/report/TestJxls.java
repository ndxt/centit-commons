package com.centit.support.report;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.support.file.FileIOOpt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestJxls {
    public static void main(String[] args) {
        try (InputStream is = TestJxls.class
            .getResourceAsStream("/template.xlsx");
             FileOutputStream out = new FileOutputStream(
                 //"/Users/codefan/projects/centit/centit-commons/centit-report-utils/src/test/resources/out.xlsx")) {
                 "C:\\projects\\java\\centit\\centit-commons\\centit-report-utils\\src\\test\\resources\\out.xlsx")) {
            InputStream in = TestJxls.class
                .getResourceAsStream("/employees.json");
            JSONObject object =(JSONObject) JSON.parse(FileIOOpt.readStringFromInputStream(in));
            ExcelReportUtil.exportExcel(is, out, object);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
