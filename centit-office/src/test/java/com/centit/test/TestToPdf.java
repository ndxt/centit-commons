package com.centit.test;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class TestToPdf {
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//OfficeToPdf.office2Pdf("D:/temp/复星集团.doc", "D:/temp/复星集团.pdf");

		try
		{
			// 1) Load docx with POI XWPFDocument
			XWPFDocument document = new XWPFDocument( new FileInputStream( new File( "/D/temp/复星集团.docx")));

			// 2) Convert POI XWPFDocument 2 PDF with iText
			File outFile = new File( "/D/temp/复星集团.pdf" );
			outFile.getParentFile().mkdirs();

			OutputStream out = new FileOutputStream( outFile );
			PdfOptions options = PdfOptions.create()/*.fontEncoding( "UTF-8" )*/;
			PdfConverter.getInstance().convert( document, out, options );
		}
		catch ( Throwable e )
		{
			e.printStackTrace();
		}

	}

}
