package com.centit.support.office.commons;

import com.centit.support.file.FileIOOpt;
import com.centit.support.office.OfficeToPdf;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.hslf.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PowerPointUtils {

    private static final Logger logger = LoggerFactory.getLogger(OfficeToPdf.class);

    private static PDRectangle mediaBox = PDRectangle.LETTER;
    private static boolean landscape = false;
    private static boolean autoOrientation = true;
    private static boolean resize = true;

    private static void createPDFFromImages(List<ByteArrayOutputStream> imageFilenames, String targetFilePath) throws IOException {
        PDDocument doc = new PDDocument();
        for (ByteArrayOutputStream imageFileName : imageFilenames) {
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imageFileName.toByteArray(), "");

            PDRectangle actualMediaBox = mediaBox;
            if ((autoOrientation && pdImage.getWidth() > pdImage.getHeight()) || landscape) {
                actualMediaBox = new PDRectangle(mediaBox.getHeight(), mediaBox.getWidth());
            }
            PDPage page = new PDPage(actualMediaBox);
            doc.addPage(page);

            PDPageContentStream contents = new PDPageContentStream(doc, page);
            if (resize) {
                contents.drawImage(pdImage, 0, 0, actualMediaBox.getWidth(), actualMediaBox.getHeight());
            } else {
                contents.drawImage(pdImage, 0, 0, pdImage.getWidth(), pdImage.getHeight());
            }
            contents.close();
        }
        doc.save(targetFilePath);
        doc.close();
    }

    private static void createHTMLFromImages(List<ByteArrayOutputStream> imageFilenames, String targetFilePath) throws IOException {
        StringBuffer sb = new StringBuffer();
        for (ByteArrayOutputStream imageFileName : imageFilenames) {
            String base64 = Base64.getEncoder().encodeToString(imageFileName.toByteArray());
            sb.append("<br><img src=\"data:image/png;base64,").append(base64).append("\">");
        }
        FileIOOpt.writeStringToFile(sb.toString(), targetFilePath);
    }

    public static String pptToPdfUseImage(String sourceFilePath, String targetFileName, String suffix) {
        File pptFile = new File(sourceFilePath);
        if (pptFile.exists()) {
            try {
                if ("ppt".equalsIgnoreCase(suffix)) {
                    List<ByteArrayOutputStream> htmlStr = toImage2003(sourceFilePath);
                    createPDFFromImages(htmlStr, targetFileName);
                    return "ok";
                } else if ("pptx".equalsIgnoreCase(suffix)) {
                    List<ByteArrayOutputStream> htmlStr = toImage2007(sourceFilePath);
                    createPDFFromImages(htmlStr, targetFileName);
                    return "ok";
                } else {
                    logger.error("ppt转换为pdf,源文件={}不是ppt文件", sourceFilePath);
                    return null;
                }

            } catch (Exception e) {
                logger.error("ppt文档转换为pdf,发生异常,源文件={},", sourceFilePath, e);
                return null;
            }
        } else {
            logger.error("ppt文档转换为pdf,源文件={}不存在", sourceFilePath);
            return null;
        }
    }

    public static String pptToHtmlUseImage(String sourceFilePath, String targetFileName, String suffix) {
        File pptFile = new File(sourceFilePath);
        if (pptFile.exists()) {
            try {
                if ("ppt".equalsIgnoreCase(suffix)) {
                    List<ByteArrayOutputStream> htmlStr = toImage2003(sourceFilePath);
                    createHTMLFromImages(htmlStr, targetFileName);
                    return "ok";
                } else if ("pptx".equalsIgnoreCase(suffix)) {
                    List<ByteArrayOutputStream> htmlStr = toImage2007(sourceFilePath);
                    createHTMLFromImages(htmlStr, targetFileName);
                    return "ok";
                } else {
                    logger.error("ppt转换为html,源文件={}不是ppt文件", sourceFilePath);
                    return null;
                }

            } catch (Exception e) {
                logger.error("ppt文档转换为html,发生异常,源文件={},", sourceFilePath, e);
                return null;
            }
        } else {
            logger.error("ppt文档转换为html,源文件={}不存在", sourceFilePath);
            return null;
        }
    }

    private static List<ByteArrayOutputStream> toImage2007(String sourcePath) throws Exception {
        List<ByteArrayOutputStream> htmlStr = new ArrayList<>();
        FileInputStream is = new FileInputStream(sourcePath);
        XMLSlideShow ppt = new XMLSlideShow(is);
        is.close();
        Dimension pgsize = ppt.getPageSize();
        for (int i = 0; i < ppt.getSlides().size(); i++) {
            try {
                for (XSLFShape shape : ppt.getSlides().get(i).getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape tsh = (XSLFTextShape) shape;
                        for (XSLFTextParagraph p : tsh) {
                            for (XSLFTextRun r : p) {
                                r.setFontFamily("宋体");
                            }
                        }
                    }
                }
                BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                // clear the drawing area
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
                // render
                ppt.getSlides().get(i).draw(graphics);
                // save the output
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(img, "png", out);
                out.close();
                htmlStr.add(out);
            } catch (Exception e) {
                logger.error("ppt转换为pdf,发生异常,源文件={}", sourcePath, e);
                System.out.println("第" + i + "张ppt转换出错");
                return null;
            }
        }
        return htmlStr;
    }

    private static List<ByteArrayOutputStream> toImage2003(String sourcePath) {
        List<ByteArrayOutputStream> htmlStr = new ArrayList<>();
        try {
            HSLFSlideShow ppt = new HSLFSlideShow(new HSLFSlideShowImpl(sourcePath));
            Dimension pgsize = ppt.getPageSize();
            for (int i = 0; i < ppt.getSlides().size(); i++) {
                for (HSLFShape shape : ppt.getSlides().get(i).getShapes()) {
                    if (shape instanceof HSLFTextShape) {
                        HSLFTextShape tsh = (HSLFTextShape) shape;
                        for (HSLFTextParagraph p : tsh) {
                            for (HSLFTextRun r : p) {
                                r.setFontFamily("宋体");
                            }
                        }
                    }
                }
                BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                // clear the drawing area
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
                // render
                ppt.getSlides().get(i).draw(graphics);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(img, "png", out);
                out.close();
                htmlStr.add(out);
            }
        } catch (Exception e) {
            logger.error("ppt转换为Pdf,发生异常,源文件={}", sourcePath, e);
            return null;
        }

        return htmlStr;
    }
}
