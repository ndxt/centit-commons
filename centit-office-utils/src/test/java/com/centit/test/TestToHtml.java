package com.centit.test;

import com.centit.support.office.OfficeToHtml;

public class TestToHtml {

    public static void main(String[] args) {
        OfficeToHtml.excel2Html("/D/Projects/RunData/file_home/六上课程表.xlsx",
            "/D/Projects/RunData/file_home/六上课程表.html");
        OfficeToHtml.excel2Html("/D/Projects/RunData/file_home/centit-info.xlsx",
            "/D/Projects/RunData/file_home/centit-info.html");
        //POIPptToHtmlUtils.pptToHtml("D:/diagnosis/file/temp//ppt2007.pptx", "D:/diagnosis/file/temp/", "test5.html");
       // OfficeToHtml.ppt2Html("C:\\Users\\zhf\\Postman\\files\\2.pptx", "C:\\Users\\zhf\\Postman\\files\\tmp\\2.html","pptx");
    }

}
