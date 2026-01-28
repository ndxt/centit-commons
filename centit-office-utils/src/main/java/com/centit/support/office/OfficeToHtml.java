package com.centit.support.office;

import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.centit.support.office.commons.CommonUtils;
import com.centit.support.office.commons.PowerPointUtils;
import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;

/**
 * Created by codefan on 2017/10/9.
 */

public abstract class OfficeToHtml {
    private static final Logger logger = LoggerFactory.getLogger(OfficeToHtml.class);

    private OfficeToHtml() {
        throw new IllegalAccessError("Utility class");
    }

    public static boolean ppt2Html(String inPptFile, String outPdfFile, String suffix) {
        String inputFile = CommonUtils.mapWidowsPathIfNecessary(inPptFile);
        String pdfFile = CommonUtils.mapWidowsPathIfNecessary(outPdfFile);

        if ("ok".equals(PowerPointUtils.pptToHtmlUseImage(inputFile, pdfFile, suffix))) {
            return true;
        }
        return false;
    }
    public static boolean ppt2Html(String inPptFile, String outPdfFile) {
        return ppt2Html(inPptFile, outPdfFile, FileType.getFileExtName(inPptFile));
    }

    public static boolean excel2Html(InputStream inExcelStream, OutputStream outPdfStream, String suffix) {
        try {
            HSSFWorkbook excelBook = new HSSFWorkbook();
            if ("xls".equalsIgnoreCase(suffix)) {
                excelBook = new HSSFWorkbook(inExcelStream);
            } else if ("xlsx".equalsIgnoreCase(suffix)) {
                XlsxTransformXls xls = new XlsxTransformXls();
                XSSFWorkbook workbookOld = new XSSFWorkbook(inExcelStream);
                xls.transformXSSF(workbookOld, excelBook);
            } else {
                return false;
            }

            ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            excelToHtmlConverter.setOutputColumnHeaders(false);
            //去掉Excel行号
            excelToHtmlConverter.setOutputRowNumbers(false);
            excelToHtmlConverter.processWorkbook(excelBook);
            Transformer serializer = CommonUtils.createTransformer();
            serializer.transform(new DOMSource(excelToHtmlConverter.getDocument()),
                new StreamResult(outPdfStream));
            return true;
        } catch (TransformerException | IOException |ParserConfigurationException e){
            logger.error(e.getMessage());
            return false;
        }
    }

    public static boolean excel2Html(String inExcelFile, String outHtmlFile, String suffix) {
        String inFilePath = CommonUtils.mapWidowsPathIfNecessary(inExcelFile);
        String outFilePath = CommonUtils.mapWidowsPathIfNecessary(outHtmlFile);

        try(InputStream inWordStream = new FileInputStream(new File(inFilePath));
            OutputStream outPdfStram = new FileOutputStream(new File(outFilePath))) {
            return excel2Html(inWordStream, outPdfStram, suffix);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }

    }

    public static boolean excel2Html(String inExcelFile, String outPdfFile) {
        return excel2Html(inExcelFile, outPdfFile, FileType.getFileExtName(inExcelFile));
    }

    //仅仅支持doc hwpf
    public static boolean word2Html(InputStream inWordStream, OutputStream outHtmlStream, String imagePath, String suffix) {
        try {
            if("doc".equalsIgnoreCase(suffix)) {
                HWPFDocument wordDocument = new HWPFDocument(inWordStream);
                WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                    XMLHelper.getDocumentBuilderFactory().newDocumentBuilder()
                        .newDocument());
                wordToHtmlConverter.setPicturesManager(new PicturesManager() {
                    @Override
                    public String savePicture(byte[] paramArrayOfByte,
                                              PictureType paramPictureType, String paramString,
                                              float paramFloat1, float paramFloat2) {
                        //设定图片路径
                        return imagePath+File.separator+paramString;
                    }
                });
                wordToHtmlConverter.processDocument(wordDocument);
                List<Picture> pics = wordDocument.getPicturesTable().getAllPictures();;
                for(Picture pic:pics){
                    try (FileOutputStream fos = new FileOutputStream(imagePath+File.separator+pic.suggestFullFileName())) {
                        pic.writeImageContent(fos);
                    }
                }
                Transformer serializer = CommonUtils.createTransformer();
                serializer.transform(new DOMSource(wordToHtmlConverter.getDocument()),
                    new StreamResult(outHtmlStream));
                return true;
            } else if("docx".equalsIgnoreCase(suffix)) {
                WordprocessingMLPackage wordMLPackage= Docx4J.load(inWordStream);

                HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
                htmlSettings.setImageDirPath(imagePath);
                htmlSettings.setImageTargetUri(FileSystemOpt.extractFileName(imagePath));
                htmlSettings.setWmlPackage(wordMLPackage);

                String userCSS = "html, body, div, span, h1, h2, h3, h4, h5, h6, p, a, img,  ol, ul, li, table, caption, tbody, tfoot, thead, tr, th, td " +
                    "{ margin: 0; padding: 0; border: 0;}" +
                    "body {line-height: 1;} ";
                htmlSettings.setUserCSS(userCSS);
                Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
                Docx4jProperties.setProperty("docx4j.Log4j.Configurator.disabled", true);
                Docx4J.toHTML(htmlSettings, outHtmlStream, Docx4J.FLAG_EXPORT_PREFER_XSL);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public static boolean word2Html(String inWordFile, String outHtmlFile) {
        String inFilePath = CommonUtils.mapWidowsPathIfNecessary(inWordFile);
        String outFilePath = CommonUtils.mapWidowsPathIfNecessary(outHtmlFile);

        try(InputStream inWordStream = new FileInputStream(new File(inFilePath));
            OutputStream outPdfStram = new FileOutputStream(new File(outFilePath))) {
            return word2Html(inWordStream, outPdfStram,
                outFilePath.substring(0,outFilePath.lastIndexOf('.')),
                FileType.getFileExtName(inWordFile));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }

    }
}
