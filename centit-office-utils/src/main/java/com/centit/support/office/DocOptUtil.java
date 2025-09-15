package com.centit.support.office;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
/**
 * 未归类的文档操作，比如：文档合并
 * OFD 操作指南
 * http://www.kler.cn/a/414463.html?action=onClick
 * &lt;dependency&gt;
 *     &lt;groupId&gt;org.ofdrw&lt;/groupId&gt;
 *     &lt;artifactId&gt;ofdrw-tool&lt;/artifactId&gt;
 *     &lt;version&gt;2.3.5&lt;/version&gt;
 * &lt;/dependency&gt;
 */

public class DocOptUtil {
    private static final Logger logger = LoggerFactory.getLogger(DocOptUtil.class);

    public static void mergePdfFiles(String outputPath, List<String> inputPaths) {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, fos);
            document.open();
            for (String pdf : inputPaths) {
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    document.newPage();
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
            document.close();
            //System.out.println("PDFs merged successfully.");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void mergePdfFiles(OutputStream fos, List<InputStream> osPdfs) {
        try {
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, fos);
            document.open();
            for (InputStream pdf : osPdfs) {
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    document.newPage();
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
            document.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static boolean pdfContainsJSAction(String pdfFilePath) {
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))){
            String cosName = document.getDocument().getTrailer().toString();
            if(cosName.contains("COSName{JS}")){
                return true;
            }
        } catch (IOException  e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }
}
