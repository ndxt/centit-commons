package com.centit.support.office;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class ImagesToPdf {

    public static Image bufferedImageToPdfImage(BufferedImage image){
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "JPG", outputStream);
            return Image.getInstance(outputStream.toByteArray());
        } catch (IOException | BadElementException e) {
            return null;
        }
    }

    public static void bufferedImagesToA4SizePdf(List<BufferedImage> imageList, OutputStream outPdfStram){
        List<Image> images = new ArrayList<>(imageList.size());
        for(BufferedImage image : imageList){
            Image img = bufferedImageToPdfImage(image);
            if(img != null){
                images.add(img);
            }
        }
        imagesToA4SizePdf(images, outPdfStram);
    }

    /**
     * 二维码写入pdf
     * @param imageList 多个图像列表
     * @param outPdfStram 合并为同一个pdf文件
     */

    public static void imagesToA4SizePdf(List<Image> imageList, OutputStream outPdfStram){
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        try{
            PdfWriter.getInstance(document, outPdfStram);
            document.open();

            float a4Width = document.getPageSize().getWidth();
            float a4Height = document.getPageSize().getHeight();
            float imageWidth = imageList.get(0).getWidth();
            float imageHeight = imageList.get(0).getHeight();

            //每行能放几个二维码
            int rowTotalCount = (int)(a4Width/imageWidth);
            //一页a4一共能放几行
            int totalRowCount = (int)(a4Height/imageHeight);

            //当前行的第几个二维码
            int columnCount = 0;

            //记录当前页写到第几行 默认第一行开始
            int rowCount = 1;

            for (int i = 0; i < imageList.size(); i++) {
                com.lowagie.text.Image image = imageList.get(i);
                image.setAlignment(com.lowagie.text.Image.MIDDLE);
                float x  , y;
                //当行的个数等于每行只能写入的总个数时进行换行
                if( columnCount   >= rowTotalCount){
                    rowCount ++;
                    columnCount = 0;
                }
                //超过一页时重新创建新的一页
                if (totalRowCount == 1 || rowCount > totalRowCount){
                    document.setPageSize(new Rectangle(a4Width,a4Height));
                    document.newPage();
                    rowCount = 1;
                    columnCount = 0;
                }

                //每行x轴方向空白总宽度
                float xBlankTotalWidth = a4Width - imageWidth * rowTotalCount;
                //根据每行二维码个数平分空白
                float  xBlankWidth =  xBlankTotalWidth / (rowTotalCount + 1);
                //左边偏移量 始终保证二维码处于居中的位置
                x = columnCount > 0 ? (xBlankWidth * (columnCount + 1)) + (imageWidth  * columnCount++)
                    : xBlankWidth  + (imageWidth  * columnCount++);

                //y轴空白总高度
                float yBlankTotalHeight = a4Height - imageHeight * totalRowCount;
                //每行的间隔距离
                float  yBlankHeight =  yBlankTotalHeight / (totalRowCount + 1);

                y = a4Height - (imageHeight * rowCount) - (yBlankHeight * rowCount);

                image.setAbsolutePosition( x , y );
                document.add(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            document.close();
        }
    }

}
