package com.centit.support.office;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfOptUtil {
    public static void mergePdfFiles(String outputPath, List<String> inputPaths) throws IOException {
        /*PdfDocument pdfDoc = new PdfDocument();
        new PdfWriter(pdfDoc , outputPath);
        PdfDocument sourcePdf;
        PdfPage page;
        int pageNumber = 1;

        for (String inputPath : inputPaths) {
            sourcePdf = new PdfDocument(new PdfReader(inputPath));
            for (int i = 1; i <= sourcePdf.getNumberOfPages(); i++) {
                page = sourcePdf.getPage(i);
                PdfCanvas canvas = new PdfCanvas(pdfDoc.addNewPage(
                    new PdfPageParameters(page.getPageSize())).newContentStreamBefore(),
                    pdfDoc, new PdfNumber(pageNumber));
                canvas.addTemplate(page, 0, 0);
                pageNumber++;
            }
            sourcePdf.close();
        }
        pdfDoc.close();*/
        String[] pdfs = new String[] {
            "C:\\Users\\test\\Desktop\\tmp\\001\\20241014-001\\0001.pdf",
            "C:\\Users\\test\\Desktop\\tmp\\001\\20241014-001\\0002.pdf"};

        String outputPdf = "C:\\Users\\test\\Desktop\\tmp\\001\\20241014-001\\MergedPDF333-.pdf"; // 合并后的PDF文件

        try {
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputPdf));
            document.open();

            for (String pdf : pdfs) {
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    document.newPage();
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }

            document.close();
            System.out.println("PDFs merged successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
