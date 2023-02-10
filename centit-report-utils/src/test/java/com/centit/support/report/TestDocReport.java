package com.centit.support.report;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by codefan on 17-9-23.
 */
public class TestDocReport {

    public static void main(String[] args) throws Exception {
        Map<String, Object> project = new HashMap<>();
        Map<String, Object> reportData = new HashMap<>();
        project.put("name", "赵四");
        project.put("unitName", "江苏南大先腾信息产业股份有限公司");
        reportData.put("project",project);
        WordReportUtil.reportDocxWithFreemarker(reportData,
            "D:/Projects/RunData/demo_home/XDocReport.docx",
            "D:/Projects/RunData/demo_home/XDocReport2.docx");
        System.out.println("done!");
        System.exit(0);
    }
}
