package com.centit.support.image;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
@SuppressWarnings("unused")
public abstract class ImageOpt {
	private ImageOpt() {
	}

	/**
	 * 创建图片的缩略图 ，算法来做网络，测试通过
	 * 
	 * @param filename  文件名称
	 * @param thumbWidth 缩略图宽度
	 * @param thumbHeight 缩略图高度
	 * @param quality 缩略图质量
	 * @param outFilename 输出文件名
	 * @throws InterruptedException  分析异常
	 * @throws FileNotFoundException 文件读取异常
	 * @throws IOException IO
	 */
	public static void createThumbnail(String filename, int thumbWidth, int thumbHeight, int quality,
			String outFilename) throws InterruptedException, FileNotFoundException, IOException {
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
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename));
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
		quality = Math.max(0, Math.min(quality, 100));
		param.setQuality((float) quality / 100.0f, false);
		encoder.setJPEGEncodeParam(param);
		encoder.encode(thumbImage);
		out.close();
	}

	/**
	 * 抓屏程序 算法来做网络，测试通过
	 * 
	 * @param fileName  文件名称
	 * @throws AWTException  异常
	 * @throws IOException  异常
	 */
	public static void captureScreen(String fileName) throws AWTException, IOException  {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle screenRectangle = new Rectangle(screenSize);
		Robot robot = new Robot();
		BufferedImage image = robot.createScreenCapture(screenRectangle);
		ImageIO.write(image, "png", new File(fileName));
	}

	/**
	 * 对图像进行
	 * @param image 图片
	 * @param subImageBounds 图片区域
	 * @return 截图
	 * @throws IOException 异常
	 */
	public static BufferedImage saveSubImage(BufferedImage image, Rectangle subImageBounds)
			throws IOException {

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
	
	public static List<BufferedImage> splitImage(BufferedImage image, int divisions)
			throws IOException {
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		if(divisions<2){
			images.add(image);
			return images;
		}
		int imgWidth = image.getWidth();
		int subImgWidth = imgWidth / divisions;
		int imgHeight = image.getHeight();
		for(int i=0;i<divisions;i++){
			BufferedImage subImage = new BufferedImage(subImgWidth,imgHeight, 1);
			Graphics g = subImage.getGraphics();
			g.drawImage(
						image.getSubimage(imgWidth*i/divisions,0, subImgWidth,imgHeight),
						0, 0, null);
			g.dispose();
			images.add(subImage);
		}
		return images;
	}
	
	public static int[] getRGB(BufferedImage image) {
		int width = image.getWidth();
        int height = image.getHeight();
        
        int[] inPixels = new int[width*height];
        return getRGB( image, 0, 0, width, height, inPixels );
	}
	
	public static int[] getRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			return (int [])image.getRaster().getDataElements( x, y, width, height, pixels );
		return image.getRGB( x, y, width, height, pixels, 0, width );
    }
	
}
