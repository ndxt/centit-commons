package com.centit.support.office;

import com.centit.support.algorithm.DatetimeOpt;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("unused")
public abstract class Watermark4Pdf {
    private Watermark4Pdf() {
        throw new IllegalAccessError("Utility class");
    }

    public static final boolean runFlag = false;

    protected static Logger logger = LoggerFactory.getLogger(Watermark4Pdf.class);

    /**
     * 为文档添加水印：目前只支持给pdf、word、excel、ppt增加水印，并且输出只能是Pdf文件。
     *
     * @param inputFile       源文件路径及文件 名称
     * @param outputFile      输出文件路径及文件名称
     * @param waterMarkStr    水印字符串
     * @param opacity         文字透明度(1-10)
     * @param rotation        旋转度数(-90 ~ 90)
     * @param fontSize        字体大小(1-1)
     * @param isRepeat        水印是否重复
     * @return boolean        目前 不支持位置自定义：因设置了文字大小、倾斜度后不好计算水印文字的长宽数据。
     */
    public static boolean addWatermark4Pdf(InputStream inputFile,
                                           OutputStream outputFile,
                                           String waterMarkStr,
                                           float opacity,
                                           float rotation,
                                           float fontSize,
                                           boolean isRepeat) {
        PdfGState gs = new PdfGState();
        PdfReader pdfReader = null;
        PdfStamper pdfStamper = null;
        float cosRotation = (float) Math.cos(rotation/180*Math.PI);
        float sinRotation = (float) Math.sin(rotation/180*Math.PI);
        int strSize = waterMarkStr.length() / 2 + (waterMarkStr.getBytes().length - waterMarkStr.length()) / 4 + 2;
        try{
            pdfReader =new PdfReader(inputFile);
            pdfStamper = new PdfStamper(pdfReader, outputFile);

            BaseFont base = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H",
                BaseFont.NOT_EMBEDDED);
            if (base == null) {
                return false;
            }
            // 设置透明度为0.4
            while (opacity >1f){
                opacity = opacity / 10;
            }
            gs.setFillOpacity(opacity);
            gs.setStrokeOpacity(opacity);
            int toPage = pdfStamper.getReader().getNumberOfPages();
            for (int i = 1; i <= toPage; i++) {
                Rectangle pageRect = pdfStamper.getReader().getPageSizeWithRotation(i);
                // 获得PDF最顶层
                PdfContentByte content = pdfStamper.getOverContent(i);
                content.saveState();
                // set Transparency
                content.setGState(gs);
                content.beginText();
                content.setColorFill(BaseColor.GRAY);
                content.setFontAndSize(base, fontSize);
                // 水印文字成45度角倾斜
                if(isRepeat) {
                    // 水印文字成45度角倾斜
                    int endLine = (int) (pageRect.getHeight() / (3 * fontSize)) + 1;
                    if (endLine < 1) {
                        endLine = 1;
                    }
                    int beginLine = (int) (0 - pageRect.getWidth() / (3 * fontSize)) - 1;
                    if (beginLine > 0) {
                        beginLine = 0;
                    }
                    if(rotation<0){
                        endLine -= beginLine;
                        beginLine = 0;
                    }
                    int repeat = (int) (pageRect.getWidth() / cosRotation / (strSize * fontSize)) + 1;
                    if (repeat < 1) {
                        repeat = 1;
                    }

                    for (int j = beginLine; j < endLine; j++) {
                        for (int k = 0; k < repeat; k++) {
                            // 计算水印X,Y坐标
                            float l = strSize * fontSize * (k + 0.5f);
                            float y = fontSize * (3 * j + 2) + l * sinRotation;
                            float x = l * cosRotation;
                            if (y > 0 && y < pageRect.getHeight() && x < pageRect.getWidth()) {
                                content.showTextAligned(Element.ALIGN_CENTER, waterMarkStr, x,
                                    y, rotation);
                            }
                        }
                    }
                } else {
                    content.showTextAligned(Element.ALIGN_CENTER, waterMarkStr, pageRect.getWidth() / 2,
                        pageRect.getHeight() / 2, rotation);
                }

                content.endText();
            }

        } catch (IOException | DocumentException e1) {
            logger.error(e1.getMessage(),e1);//e1.printStackTrace();
            return false;
        } finally {
            try {
                if(pdfStamper != null) {
                    pdfStamper.close();
                }
                if(pdfReader != null) {
                    pdfReader.close();
                }
            } catch (DocumentException | IOException e) {
                logger.error(e.getMessage(),e);//logger.error(e.getMessage(), e);
            }
        }
        return true;
    }

    public static boolean addWatermark4Pdf(String inputFile,
                                           String outputFile,
                                           String waterMarkStr,
                                           float opacity,
                                           float rotation,
                                           float fontSize,
                                           boolean isRepeat) {
        try {
            return addWatermark4Pdf(Files.newInputStream(Paths.get(inputFile)),
                    Files.newOutputStream(Paths.get(outputFile)),
                waterMarkStr,
                opacity,
                rotation,
                fontSize, isRepeat);

        }catch(IOException e) {
            logger.error(e.getMessage(),e);//logger.error(e.getMessage(), e);
            return false;
        }
    }


    /**
     * 为文档添加水印：目前只支持给pdf、word、excel、ppt增加水印，并且输出只能是Pdf文件。
     *
     * @param inputFile       源文件路径及文件
     * @param waterMarkStr    水印字符串
     * @param suffix          前缀
     * @param isRepeat        水印是否重复
     * @return 是否成功
     */
    public static boolean addWatermark4Word(String inputFile, String waterMarkStr, String suffix, boolean isRepeat) {
        //将源office文件转换为pdf
        //String suffix = inputFile.substring(inputFile.lastIndexOf("."));
        String tmpPdfFile = inputFile.substring(0,inputFile.lastIndexOf("."))+
            DatetimeOpt.convertDateToString(DatetimeOpt.currentUtilDate(), "yyyyMMddHHmmssSSS")+".pdf";
        String wartermarkFile = inputFile.substring(0,inputFile.lastIndexOf("."))+ ".pdf";
        if(! OfficeToPdf.word2Pdf(inputFile,tmpPdfFile,suffix)) {
            return false;
        }

        return  addWatermark4Pdf(tmpPdfFile,
            wartermarkFile,
            waterMarkStr,
            0.4f,
            45f,
            60f, isRepeat);
    }

    private static void adjustImagePositionAndSize(Image image, Rectangle pageSize,
                                                   final float xx, final float yy, final float ww, final float hh){
        float x = xx, y = yy, w = ww, h = hh;
        if(x < 1.f && x > -1.f) {
            x = pageSize.getWidth() * x;
        }
        if(y < 1.f && y > -1.f) {
            y = pageSize.getHeight() * y;
        }
        image.setAbsolutePosition(x, y);
        if(w < 0.f){
            w = image.getWidth();
        } else if(w<1.0f){
            w = pageSize.getWidth() * w;
        }
        if(h < 0.f){
            h = image.getHeight();
        } else if(h<1.0f){
            h = pageSize.getHeight() * h;
        }
        image.scaleToFit(new Rectangle(w,h));
    }
    public static void addImage2Pdf(InputStream inputFile,
                                       OutputStream outputFile,
                                       int page,
                                       Image image,
                                       float opacity,
                                       float x,  float y, float w, float h) throws DocumentException, IOException { // 图章路径
        PdfReader pdfReader = new PdfReader(inputFile);
        //Image image = Image.getInstance(imageFile);
        int pdfNumber = pdfReader.getNumberOfPages();
        if(page>pdfNumber || pdfNumber<1) { // 没有对应的页面
            return ;
        }
        PdfStamper pdfStamper = new PdfStamper(pdfReader, outputFile);
        PdfContentByte pdfContentByte = null;
        PdfGState pdfGState = new PdfGState();
        //设置透明度
        while(opacity>1){
            opacity = opacity/10;
        }
        pdfGState.setFillOpacity(opacity);
        if(page<0) {
            for (int i = 1; i <= pdfNumber; i++) {
                //在内容下方加水印OverContent
                adjustImagePositionAndSize(image, pdfReader.getPageSizeWithRotation(i), x, y, w, h);
                pdfContentByte = pdfStamper.getOverContent(i);
                pdfContentByte.setGState(pdfGState);
                pdfContentByte.addImage(image);
            }
        } else {
            adjustImagePositionAndSize(image, pdfReader.getPageSizeWithRotation(page), x, y, w, h);
            pdfContentByte = pdfStamper.getOverContent(page);
            pdfContentByte.setGState(pdfGState);
            pdfContentByte.addImage(image);
        }
        pdfStamper.close();
        pdfReader.close();

    }

    public static void addImage2Pdf(String inputFile,
                                       String outputFile,
                                       int page,
                                       String imageFile,
                                       float opacity,
                                       float x, float y, float w, float h) throws DocumentException, IOException { // 图章路径
            Image image = Image.getInstance(imageFile);
            addImage2Pdf(Files.newInputStream(Paths.get(inputFile)),
                Files.newOutputStream(Paths.get(outputFile)),
                page,
                image, opacity,
                x, y, w, h);
    }

    public static void addImage2Pdf(String inputFile,
                                    String outputFile,
                                    int page,
                                    String imageFile,
                                    float opacity,
                                    float x, float y) throws DocumentException, IOException { // 图章路径
        Image image = Image.getInstance(imageFile);
        addImage2Pdf(Files.newInputStream(Paths.get(inputFile)),
            Files.newOutputStream(Paths.get(outputFile)),
            -1,
            image, opacity,
            x, y, -1, -1);
    }
    public static Image createPdfImage(byte [] imageBytes) throws BadElementException, IOException {
        return Image.getInstance(imageBytes);
    }

}
