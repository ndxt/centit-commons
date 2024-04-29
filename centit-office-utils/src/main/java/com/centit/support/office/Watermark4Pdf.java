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
     * @param rotation        旋转度数(1-170)
     * @param fontSize        字体大小(1-1)
     * @return boolean        目前 不支持位置自定义：因设置了文字大小、倾斜度后不好计算水印文字的长宽数据。
     */
    public static boolean addWatermark4Pdf(InputStream inputFile,
                                           OutputStream outputFile,
                                           String waterMarkStr,
                                           float opacity,
                                           float rotation,
                                           float fontSize) {
        PdfContentByte content = null;
        BaseFont base = null;
        Rectangle pageRect = null;
        PdfGState gs = new PdfGState();
        PdfReader pdfReader = null;
        PdfStamper pdfStamper = null;
        try{
            pdfReader =new PdfReader(inputFile);
            pdfStamper = new PdfStamper(pdfReader, outputFile);

            base = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H",
                    BaseFont.NOT_EMBEDDED);
            if (base == null) {
                return false;
            }
            // 设置透明度为0.4
            gs.setFillOpacity(opacity);
            gs.setStrokeOpacity(opacity);
            int toPage = pdfStamper.getReader().getNumberOfPages();
            for (int i = 1; i <= toPage; i++) {
                pageRect = pdfStamper.getReader().getPageSizeWithRotation(i);
                // 计算水印X,Y坐标
                float x = pageRect.getWidth() / 2;
                float y = pageRect.getHeight() / 2;
                // 获得PDF最顶层
                content = pdfStamper.getUnderContent(i);
                content.saveState();
                // set Transparency
                content.setGState(gs);
                content.beginText();
                content.setColorFill(BaseColor.GRAY);
                content.setFontAndSize(base, fontSize);
                // 水印文字成45度角倾斜
                content.showTextAligned(Element.ALIGN_CENTER, waterMarkStr, x,
                        y, rotation);
                content.endText();
            }

        } catch (IOException | DocumentException e1) {
            logger.error(e1.getMessage(),e1);//e1.printStackTrace();
            return false;
        } finally {
            content = null;
            base = null;
            pageRect = null;
            try {
                if(pdfReader!=null) {
                    pdfReader.close();
                }
                if(pdfStamper!=null) {
                    pdfStamper.close();
                }
            } catch (DocumentException | IOException e) {
                logger.error(e.getMessage(),e);//e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean addWatermark4Pdf(String inputFile,
                                           String outputFile,
                                           String waterMarkStr,
                                           float opacity,
                                           float rotation,
                                           float fontSize) {
        try {
            return addWatermark4Pdf(Files.newInputStream(Paths.get(inputFile)),
                    Files.newOutputStream(Paths.get(outputFile)),
                waterMarkStr,
                opacity,
                rotation,
                fontSize);

        }catch(IOException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
            return false;
        }
    }


    /**
     * 为文档添加水印：目前只支持给pdf、word、excel、ppt增加水印，并且输出只能是Pdf文件。
     *
     * @param inputFile       源文件路径及文件
     * @param waterMarkStr    水印字符串
     * @param suffix          前缀
     * @return 是否成功
     */
    public static boolean addWatermark4Word(String inputFile, String waterMarkStr, String suffix) {
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
            60f);
    }

    public static void addImage2Pdf(InputStream inputFile,
                                       OutputStream outputFile,
                                       int page,
                                       Image image,
                                       float opacity,
                                       float x,  float y, float w, float h) throws DocumentException, IOException { // 图章路径
            PdfReader pdfReader = new PdfReader(inputFile);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, outputFile);
            //Image image = Image.getInstance(imageFile);
            image.setAbsolutePosition(x,y);
            Rectangle rectangle = new Rectangle(0, 0,
                w<=0?image.getWidth():w, h<=0?image.getHeight():h);
            image.scaleToFit(rectangle);
            int pdfNumber = pdfReader.getNumberOfPages()+1;
            PdfContentByte pdfContentByte = null;
            PdfGState pdfGState = new PdfGState();
            //设置透明度
            pdfGState.setFillOpacity(opacity);
            if(page<0) {
                for (int i = 1; i < pdfNumber; i++) {
                    //在内容下方加水印OverContent
                    pdfContentByte = pdfStamper.getOverContent(i);
                    pdfContentByte.setGState(pdfGState);
                    pdfContentByte.addImage(image);
                }
            } else {
                pdfContentByte = pdfStamper.getOverContent(page);
                pdfContentByte.setGState(pdfGState);
                pdfContentByte.addImage(image);
            }
            pdfStamper.close();
            pdfReader.close();

    }

    public static void addImage2Pdf(String inputFile,
                                       String outputFile,
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
