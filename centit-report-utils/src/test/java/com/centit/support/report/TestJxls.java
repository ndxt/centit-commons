package com.centit.support.report;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;

import java.io.*;

public class TestJxls {
    public static void main(String[] args) {
        try (InputStream is = TestJxls.class
            .getResourceAsStream("/template.xlsx");
             FileOutputStream out = new FileOutputStream(
                 "/Users/codefan/projects/centit/centit-commons/centit-report-utils/src/test/resources/out.xlsx")) {

            ExcelReportUtil.exportExcel(is, out,
                CollectionsOpt.createHashMap("employees", CollectionsOpt.createList(
                    CollectionsOpt.createHashMap("name","userName", "birthDate", DatetimeOpt.currentUtilDate(), "payment", 10000, "bonus", 200 ),
                    CollectionsOpt.createHashMap("name","userName", "birthDate", DatetimeOpt.currentUtilDate(), "payment", 10200, "bonus", 2020 )
                ),"nowdate", DatetimeOpt.currentUtilDate()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
