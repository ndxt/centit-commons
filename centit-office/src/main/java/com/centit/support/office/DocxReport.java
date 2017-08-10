package com.centit.support.office;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by codefan on 17-8-10.
 */
@SuppressWarnings("unused")
public abstract class DocxReport {

    public static void convertDocxToPdf(String docxFilePath,String pdfFilePath) throws Exception{

        // 1) Load docx with POI XWPFDocument
        XWPFDocument document = new XWPFDocument( new FileInputStream( new File(docxFilePath)));

        // 2) Convert POI XWPFDocument 2 PDF with iText
        File outFile = new File( pdfFilePath);
        outFile.getParentFile().mkdirs();

        OutputStream out = new FileOutputStream( outFile );
        PdfOptions options = PdfOptions.create()/*.fontEncoding( "UTF-8" )*/;
        PdfConverter.getInstance().convert( document, out, options );
    }


}
