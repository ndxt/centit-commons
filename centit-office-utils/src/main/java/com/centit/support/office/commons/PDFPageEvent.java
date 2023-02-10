package com.centit.support.office.commons;

import com.itextpdf.text.Document;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.*;

public class PDFPageEvent extends PdfPageEventHelper {
    protected PdfTemplate template;
    public BaseFont baseFont;

    protected static BaseFont BASE_FONT_CHINESE;
    static {
        try {
            BASE_FONT_CHINESE = BaseFont.createFont("simsun.ttf", com.lowagie.text.pdf.BaseFont.IDENTITY_H, com.lowagie.text.pdf.BaseFont.NOT_EMBEDDED);
            // 搜尋系統,載入系統內的字型(慢)
            FontFactory.registerDirectories();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        try{
            this.template = writer.getDirectContent().createTemplate(100, 100);
            this.baseFont = new Font(BASE_FONT_CHINESE , 8, Font.NORMAL).getBaseFont();
        } catch(Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        //在每页结束的时候把“第x页”信息写道模版指定位置
        PdfContentByte byteContent = writer.getDirectContent();
        String text = "第" + writer.getPageNumber() + "页";
        float textWidth = this.baseFont.getWidthPoint(text, 8);
        float realWidth = document.right() - textWidth;
        //
        byteContent.beginText();
        byteContent.setFontAndSize(this.baseFont , 10);
        byteContent.setTextMatrix(realWidth , document.bottom());
        byteContent.showText(text);
        byteContent.endText();
        byteContent.addTemplate(this.template , realWidth , document.bottom());
    }
}

