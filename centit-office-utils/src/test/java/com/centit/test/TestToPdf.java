package com.centit.test;

import com.centit.support.office.OfficeToPdf;

public class TestToPdf {
    /**
     * @param args
     */
    public static void main(String[] args) {
        OfficeToPdf.excel2Pdf("/D/Projects/RunData/file_home/六上课程表.xlsx",
            "/D/Projects/RunData/file_home/六上课程表.pdf");
        // OfficeToPdf.excel2Pdf("/D/Projects/RunData/demo_home/temp/case3.xlsx",
        //    "/D/Projects/RunData/demo_home/temp/case3.pdf");

        //OfficeToPdf.office2Pdf("D:/temp/复星集团.doc", "D:/temp/复星集团.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/复星集团.docx", "D:/temp/复星集团x.pdf");

        //OfficeToPdf.word2Pdf("D:/CA签名步骤.doc", "D:/CA签名步骤.pdf","doc");
        //OfficeToPdf.office2Pdf("D:/temp/财务报表.xlsx", "D:/temp/财务报表.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/财务报表.xlsm", "D:/temp/财务报表m.pdf");

        //OfficeToPdf.office2Pdf("D:/temp/经营数据管理系统需求.pptx", "D:/temp/经营数据管理系统需求.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/自来水公司整体业务介绍.ppt", "D:/temp/自来水公司整体业务介绍.pdf");

    }

}
