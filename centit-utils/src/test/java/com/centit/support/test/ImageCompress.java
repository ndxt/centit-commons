package com.centit.support.test;

    import java.io.*;
    import java.util.logging.Level;
    import java.util.logging.Logger;
    import java.awt.*;
//旧的jpeg处理类
//import com.sun.image.codec.jpeg.*;
    import java.awt.image.BufferedImage;

    import javax.imageio.IIOImage;
    import javax.imageio.ImageIO;
    import javax.imageio.ImageTypeSpecifier;
    import javax.imageio.metadata.IIOMetadata;
    import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
    import javax.imageio.stream.ImageOutputStream;

    import com.centit.support.image.ImageOpt;
    import org.w3c.dom.Element;

    import com.sun.imageio.plugins.jpeg.*;

/**
 * @author zhouqz
 */
public class ImageCompress {

    /**
     * log4j
     */
    private final static Logger logger = Logger.getLogger(ImageCompress.class
        .getName());

    /**
     * 图片压缩测试
     *
     * @param args
     */
    public static void main(String args[]) {
        // 图片url，压缩后的宽和高
        int w = 800;
        int h = 600;
        String url = "/Users/codefan/appdata/RunData/screen.jpg";
        String filePath = "/Users/codefan/appdata/RunData/screen2.jpg";
        //压缩
        ImgCompress(filePath, url, "screen" ,w,h, 0.3f);
    }


    public static String ImgCompress(String filePath, String url, String name,
                                     int w, int h, float JPEGcompression) {
        File file = new File(url);

        try {
            BufferedImage bufferedImage =  ImageIO.read(file);

            int new_w = w;
            int new_h = h;

            BufferedImage image_to_save = new BufferedImage(new_w, new_h,
                bufferedImage.getType());
            image_to_save.getGraphics().drawImage(
                bufferedImage.getScaledInstance(new_w, new_h, Image.SCALE_SMOOTH), 0,
                0, null);
            FileOutputStream fos = new FileOutputStream(filePath); // 输出到文件流

            saveAsJPEG(image_to_save, JPEGcompression, fos);
            //关闭输出流
            fos.close();
            //返回压缩后的图片地址
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            filePath = "/var/upload/404.jpg";
        }
        return filePath;
    }


    public static void saveAsJPEG(BufferedImage image_to_save,
                                  float JPEGcompression, FileOutputStream fos) throws IOException {
        // Image writer
        JPEGImageWriter imageWriter = (JPEGImageWriter) ImageIO
            .getImageWritersBySuffix("jpg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
        imageWriter.setOutput(ios);
        // and metadata
        IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(
            new ImageTypeSpecifier(image_to_save), null);



        if (JPEGcompression >= 0 && JPEGcompression <= 1f) {
            JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter
                .getDefaultWriteParam();
            jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(JPEGcompression);

        }

        imageWriter.write(imageMetaData,
            new IIOImage(image_to_save, null, null), null);
        ios.close();
        imageWriter.dispose();

    }

}
