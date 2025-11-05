package com.centit.support.image;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;
import com.centit.support.security.Md5Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class ImageOpt {

    private ImageOpt() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 创建图片的缩略图 ，算法来做网络，测试通过
     * @param filename    文件名称
     * @param thumbWidth  缩略图宽度
     * @param thumbHeight 缩略图高度
     * @param quality     缩略图质量
     * @param outFilename 输出文件名
     * @throws InterruptedException  分析异常
     * @throws IOException           IO
     */
    public static void createThumbnail(String filename, int thumbWidth, int thumbHeight, int quality,
                                       String outFilename) throws InterruptedException, IOException {
        // load image from filename
        Image image = Toolkit.getDefaultToolkit().getImage(filename);
        MediaTracker mediaTracker = new MediaTracker(new Container());
        mediaTracker.addImage(image, 0);
        mediaTracker.waitForID(0);
        // use this to test for errors at this point:
        // System.out.println(mediaTracker.isErrorAny());
        // determine thumbnail size from WIDTH and HEIGHT
        double thumbRatio = (double) thumbWidth / (double) thumbHeight;
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        double imageRatio = (double) imageWidth / (double) imageHeight;
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        } else {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }

        // draw original image to thumbnail image object and
        // scale it to the new size on-the-fly
        BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

        // save thumbnail image to outFilename
        saveBufferedImage(outFilename, thumbImage, quality);
    }

    public static void saveBufferedImage(String filename, BufferedImage image,/*暂时没有用起来*/ int quality)
        throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(Paths.get(filename)))) {
            ImageIO.write(image, "jpg", out);
        }
    }


    /**
     * 抓屏程序 算法来做网络，测试通过
     * @param fileName 文件名称
     * @throws AWTException 异常
     * @throws IOException  异常
     */
    public static void captureScreen(String fileName) throws AWTException, IOException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        ImageIO.write(image, "png", new File(fileName));
    }

    /**
     * 对图像进行 裁剪
     * @param image          图片
     * @param subImageBounds 图片区域
     * @return 截图
     */
    public static BufferedImage saveSubImage(BufferedImage image, Rectangle subImageBounds) {
        BufferedImage subImage = new BufferedImage(subImageBounds.width, subImageBounds.height, 1);
        Graphics g = subImage.getGraphics();

        if ((subImageBounds.width > image.getWidth()) || (subImageBounds.height > image.getHeight())) {
            int left = subImageBounds.x;
            int top = subImageBounds.y;
            if (image.getWidth() < subImageBounds.width)
                left = (subImageBounds.width - image.getWidth()) / 2;
            if (image.getHeight() < subImageBounds.height)
                top = (subImageBounds.height - image.getHeight()) / 2;
            g.setColor(Color.white);
            g.fillRect(0, 0, subImageBounds.width, subImageBounds.height);
            g.drawImage(image, left, top, null);
            return image;
        } else {
            g.drawImage(
                image.getSubimage(subImageBounds.x, subImageBounds.y, subImageBounds.width, subImageBounds.height),
                0, 0, null);
        }
        g.dispose();
        return subImage;
    }

    public static List<BufferedImage> splitImage(BufferedImage image, int divisions) {
        List<BufferedImage> images = new ArrayList<>();
        if (divisions < 2) {
            images.add(image);
            return images;
        }
        int imgWidth = image.getWidth();
        int subImgWidth = imgWidth / divisions;
        int imgHeight = image.getHeight();
        for (int i = 0; i < divisions; i++) {
            BufferedImage subImage = new BufferedImage(subImgWidth, imgHeight, 1);
            Graphics g = subImage.getGraphics();
            g.drawImage(
                image.getSubimage(imgWidth * i / divisions, 0, subImgWidth, imgHeight),
                0, 0, null);
            g.dispose();
            images.add(subImage);
        }
        return images;
    }

    /**
     * 合并图片
     * @param imageList 这些图片大小需要一致
     * @param imagesPreRow 每行几个
     * @param whiteSpace 空白占位符
     * @return BufferedImage 返回图片
     */
    public static BufferedImage mergeImages(List<BufferedImage> imageList, int imagesPreRow, int whiteSpace){
        BufferedImage image = imageList.get(0);
        int w = image.getWidth();
        int h = image.getHeight();
        int imgWidth = whiteSpace + (whiteSpace + w) * imagesPreRow;
        int imgHeight = whiteSpace + (whiteSpace + h) * ( (imageList.size() - 1) / imagesPreRow + 1 );
        BufferedImage mergeImage = new BufferedImage(imgWidth, imgHeight, 1);
        Graphics g = mergeImage.getGraphics();
        int i = 0;
        for(BufferedImage img : imageList) {
            g.drawImage(img,whiteSpace + (whiteSpace + w) * (i % imagesPreRow),
                whiteSpace + (whiteSpace + h) * (i / imagesPreRow), null);
            i++;
        }
        g.dispose();
        return mergeImage;
    }

    public static int[] getRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] inPixels = new int[width * height];
        return getRGB(image, 0, 0, width, height, inPixels);
    }

    public static int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
            return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
        return image.getRGB(x, y, width, height, pixels, 0, width);
    }

    // ImageIO.write(image, "png", new File("/D/Projects/RunData/demo_home/images/codefan3.png"));
    public static BufferedImage createIdIcon(String id, int imageSize, int pointWidth,
                                             boolean singleColor, boolean symmetric) {
        int step = imageSize / pointWidth;
        if (step > 11) {
            step = 10;
            pointWidth = imageSize / 10;
        }
        if (imageSize % pointWidth != 0) {
            imageSize = step * pointWidth;
        }
        byte[] idMd5 = Md5Encoder.rawEncode(id.getBytes());
        if (idMd5 == null) {
            return null;
        }
        //assert (idMd5.length == 16);
        BufferedImage image = new BufferedImage(imageSize, imageSize,
            BufferedImage.TYPE_INT_RGB);
        Color showColor = new Color(idMd5[0] & 0xff, idMd5[1] & 0xff, idMd5[2] & 0xff);
        // 获取图形上下文
        Graphics g = image.createGraphics();
        // 设定图像背景色(因为是做背景，所以偏淡)
        int mid = (step - 1) / 2;
        for (int i = 0; i < step; i++) {
            for (int j = 0; j < step; j++) {
                int row = symmetric && i > mid ? step - i - 1 : i;
                int n = (row * step + j) / 8;
                int b = (row * step + j) % 8;
                boolean isColor = (idMd5[n] & (1 << b)) != 0;

                if (j == 0 || row == 0 || j == step - 1 || row == step - 1) { // 边框
                    if (isColor) {
                        g.setColor(new Color(214, 214, 214));
                    } else {
                        g.setColor(new Color(235, 235, 235));
                    }
                } else {
                    if (isColor) {
                        if (singleColor) {
                            g.setColor(showColor);
                        } else {
                            g.setColor(new Color(idMd5[n] & 0xff,
                                idMd5[(n * b + b) % 16] % 256 & 0xff,
                                idMd5[(n + b) % 16] % 256 & 0xff));
                        }
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                }
                g.fillRect(i * pointWidth, j * pointWidth, pointWidth, pointWidth);
            }
        }
        // 图象生效
        g.dispose();
        return image;
    }

    public static BufferedImage createIdIcon(String id, int imageSize, int pointWidth) {
        return createIdIcon(id, imageSize, pointWidth, true, true);
    }

    public static BufferedImage createNameIcon(String word, int imageSize, Color wordColor, boolean withEdge) {
        ColorModel cm = ColorModel.getRGBdefault();
        WritableRaster wr = cm.createCompatibleWritableRaster(imageSize, imageSize);
        BufferedImage image = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
        Graphics2D g = image.createGraphics();
        /*Graphics2D graphics2d = image.createGraphics();
        image = graphics2d.getDeviceConfiguration().createCompatibleImage(imageSize, imageSize, Transparency.TRANSLUCENT);
        */
        g.setColor(wordColor);
        int strLen = word.length()>=4 ? 4 : 1;
        int edgeWidth = withEdge?imageSize / 20:0;
        int fontSize = strLen>1 ? (imageSize / 2 - edgeWidth) * 15/ 16
             : (imageSize - edgeWidth * 2) * 15/ 16 ;
        g.setFont(new Font("楷体", Font.BOLD, fontSize));
        if(withEdge){
            g.setStroke(new BasicStroke(edgeWidth));
            g.drawRoundRect(edgeWidth/2,edgeWidth/2,
                imageSize-edgeWidth,imageSize-edgeWidth,
                imageSize / 3, imageSize / 3);
        }
        if(strLen<4) {
            g.drawString(word.substring(0,1), edgeWidth, imageSize * 15/16 - edgeWidth * 2);
        } else {
            g.drawString(word.substring(0,1), edgeWidth, imageSize / 2 - edgeWidth);
            g.drawString(word.substring(1,2), imageSize / 2, imageSize / 2 - edgeWidth);
            g.drawString(word.substring(2,3), edgeWidth, imageSize * 15/16 - edgeWidth * 2);
            g.drawString(word.substring(3,4), imageSize / 2, imageSize * 15/16 - edgeWidth * 2);
        }

        return image;
    }

    public static InputStream imageToInputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //String imagePath = UuidOpt.randomString(6) + ".jpg";
        ImageIO.write(image, "jpg", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    public static byte[] imageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //String imagePath = UuidOpt.randomString(6) + ".jpg";
        ImageIO.write(image, "jpg", os);
        return os.toByteArray();
    }
    public static byte[] imageToByteArray(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //String imagePath = UuidOpt.randomString(6) + ".jpg";
        ImageIO.write(image, formatName, os);
        return os.toByteArray();
    }

    /**
     * 根据路径图片图片
     * @param uri 本地路径 or 网络地址
     * @return 图片
     * @throws IOException 异常
     */
    public static BufferedImage loadImage(String uri) throws IOException {
        if (uri.startsWith("http")) {
            // 从网络获取logo
            return ImageIO.read(new URL(uri));
        } else {
            // 从资源目录下获取logo
            return ImageIO.read(new File(uri));
        }
    }

    /**
     * 生成圆角图片
     * @param image        原始图片
     * @param cornerRadius 圆角的弧度
     * @return 返回圆角图
     */
    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)
        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius,cornerRadius));
        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return output;
    }

    /**
     * 生成圆角图片  圆角边框
     * @param image        原图
     * @param cornerRadius 圆角的角度
     * @param size         边框的边距
     * @param color        边框的颜色
     * @return 返回带边框的圆角图
     */
    public static BufferedImage makeRoundBorder(BufferedImage image, int cornerRadius, int size, Color color) {
        // 将图片变成圆角
        image = makeRoundedCorner(image, cornerRadius);

        int borderSize = size << 1;
        int w = image.getWidth() + borderSize;
        int h = image.getHeight() + borderSize;
        BufferedImage output = new BufferedImage(w, h,
            BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color == null ? Color.WHITE : color);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius,
            cornerRadius));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, size, size, null);
        g2.dispose();

        return output;
    }

    public static Color castObjectToColor(Object obj) {
        if(obj==null)
            return null;
        if(obj instanceof Color)
            return (Color)obj;
        if(obj instanceof Number){
            return new Color( ((Number)obj).intValue());
        }

        String colorStr = StringBaseOpt.castObjectToString(obj);
        int strLen = colorStr.length();
        if(colorStr.startsWith("#")){
            int r=0, g=0, b=0, a=255;
            if(strLen>6){
                r = Integer.parseInt(colorStr.substring(1,3), 16);
                g = Integer.parseInt(colorStr.substring(3,5), 16);
                b = Integer.parseInt(colorStr.substring(5,7), 16);
                if(strLen>8)
                    a = Integer.parseInt(colorStr.substring(7,9), 16);
            } else {
                if(strLen>1) {
                    r = Integer.parseInt(colorStr.substring(1, 2), 16);
                    if(strLen>2) {
                        g = Integer.parseInt(colorStr.substring(2, 3), 16);
                        if(strLen>3) {
                            b = Integer.parseInt(colorStr.substring(3, 4), 16);
                            if (strLen > 4)
                                a = Integer.parseInt(colorStr.substring(4, 5), 16);
                        }
                    }
                }
            }
            return new Color(r, g, b, a);
        }

        if(StringRegularOpt.isDigit(colorStr)){
            if(strLen==9){
                int r = Integer.getInteger(colorStr.substring(0,3));
                int g = Integer.getInteger(colorStr.substring(3,6));
                int b = Integer.getInteger(colorStr.substring(6,9));
                return new Color( r, g, b);
            } else {
                return new Color(Integer.getInteger(colorStr));
            }
        }
        colorStr = colorStr.toLowerCase();
        switch (colorStr){
            case "white":       return Color.white;
            case "lightgray":   return Color.lightGray;
            case "gray":        return Color.gray;
            case "darkgray":    return Color.darkGray;
            case "black":       return Color.black;
            case "red":         return Color.red;
            case "pink":        return Color.pink;
            case "orange":      return Color.orange;
            case "yellow":      return Color.yellow;
            case "green":       return Color.green;
            case "magenta":     return Color.magenta;
            case "cyan":        return Color.cyan;
            case "blue":        return Color.blue;
            default:
                return null;
        }
    }

    public static Color castObjectToColor(Object obj, Color defaultColor) {
        Color cr = castObjectToColor(obj);
        return cr==null ? defaultColor : cr;
    }

    public static class ImageTextInfo {
        public float x;
        public float y;
        public String text;
        public ImageTextInfo(float x, float y, String t){
            this.x = x;
            this.y = y;
            this.text = t;
        }
    }

    public static ImageTextInfo createImageText(float x, float y, String t){
        return new ImageTextInfo(x, y ,t);
    }

    /*
     * 这个方法是一个静态方法（static method），静态方法不需要实例化类就可以调用。静态方法通常用于共享的或者工具性的代码，它们可以被同一个类下的所有实例共享使用。
     *
     * 在实际应用中，这个方法可能用于给图片添加水印，例如logo、商标或者文本信息等。水印通常用于保护原始图片的完整性和版权，避免未经授权的复制和分发。
     *
     * 需要注意的是，这个方法只是一个注释，并没有实际的实现。实际的实现需要根据具体的应用场景和需求来编写。
     */
    public static BufferedImage addTextToImage(BufferedImage image,
                                             String fontName,
                                             Color color,
                                             int size,
                                             List<ImageTextInfo> textList) {
        //BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        // 绘制原始图片
        // g2d.drawImage(image, 0, 0, width, height, null);
        // 搞不懂为什么要设置两次 Composite ，否则 文字的颜色不纯粹，不知道原因
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
        g2d.setColor(color);
        g2d.setFont(new Font(fontName, Font.PLAIN, size));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        for(ImageTextInfo iti : textList) {
            int xx = iti.x >=1.f ? (int)iti.x : (int)(image.getWidth() * iti.x);
            int yy = iti.y >=1.f ? (int)iti.y : (int)(image.getHeight() * iti.y);
            g2d.drawString(iti.text, xx, yy);
        }
        g2d.dispose();
        //ImageIO.write(watermarkedImage, "jpg", new File(outputPath));
        return image;
    }

    public static boolean addTextToImage(InputStream imageIS, String  imageType, OutputStream imageOS,
                                       String waterMark, String fontName, Color color, int size, float x, float y) {
        try {
            BufferedImage image = ImageIO.read(imageIS);
            BufferedImage markImage = ImageOpt.addTextToImage(image, fontName, color, size,
                CollectionsOpt.createList(createImageText(x, y, waterMark)));
            ImageIO.write(markImage, imageType, imageOS);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean addTextToImage(InputStream imageIS, String  imageType, OutputStream imageOS,
                                         String fontName,
                                         Color color,
                                         int size,
                                         List<ImageTextInfo> textList) {
        try {
            BufferedImage image = ImageIO.read(imageIS);
            // image.getAlphaRaster()
            BufferedImage markImage = ImageOpt.addTextToImage(image, fontName, color, size,
                textList);
            //markImage.getAlphaRaster().setPixels(0, 0, markImage.getWidth(), markImage.getHeight(),
            ImageIO.write(markImage, imageType, imageOS);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
