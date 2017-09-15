package com.centit.test;

import com.centit.support.office.OfficeToPdf;

public class TestToPdf {


    /**
     * @param args
     */
    public static void main(String[] args) {

        //OfficeToPdf.office2Pdf("D:/temp/复星集团.doc", "D:/temp/复星集团.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/复星集团.docx", "D:/temp/复星集团x.pdf");

        OfficeToPdf.office2Pdf("D:/temp/经营数据.xls", "D:/temp/经营数据.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/财务报表.xlsx", "D:/temp/财务报表.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/财务报表.xlsm", "D:/temp/财务报表m.pdf");

        //OfficeToPdf.office2Pdf("D:/temp/经营数据管理系统需求.pptx", "D:/temp/经营数据管理系统需求.pdf");
        //OfficeToPdf.office2Pdf("D:/temp/自来水公司整体业务介绍.ppt", "D:/temp/自来水公司整体业务介绍.pdf");

    }

}
