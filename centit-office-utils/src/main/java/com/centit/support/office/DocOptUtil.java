package com.centit.support.office;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
/**
 * 未归类的文档操作，比如：文档合并
 * OFD 操作指南
 * http://www.kler.cn/a/414463.html?action=onClick
 * &lt;dependency&gt;
 *     &lt;groupId&gt;org.ofdrw&lt;/groupId&gt;
 *     &lt;artifactId&gt;ofdrw-tool&lt;/artifactId&gt;
 *     &lt;version&gt;2.3.5&lt;/version&gt;
 * &lt;/dependency&gt;
 */

public class DocOptUtil {
    private static final Logger logger = LoggerFactory.getLogger(DocOptUtil.class);

    public static void mergePdfFiles(String outputPath, List<String> inputPaths) {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, fos);
            document.open();
            for (String pdf : inputPaths) {
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    document.newPage();
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
            document.close();
            //System.out.println("PDFs merged successfully.");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void mergePdfFiles(OutputStream fos, List<InputStream> osPdfs) {
        try {
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, fos);
            document.open();
            for (InputStream pdf : osPdfs) {
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    document.newPage();
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
            document.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static boolean pdfContainsJSAction(String pdfFilePath) {
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))){
            String cosName = document.getDocument().getTrailer().toString();
            if(cosName.contains("COSName{JS}")){
                return true;
            }
        } catch (IOException  e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static void pdfHighlightKeywords(InputStream inputPath, OutputStream outputPath, List<String> keywords, java.awt.Color color) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(
            new com.itextpdf.kernel.pdf.PdfReader(inputPath),
            new PdfWriter(outputPath)
        );
        DeviceRgb highlightColor = new DeviceRgb(
            color.getRed() / 255f,
            color.getGreen() / 255f,
            color.getBlue() / 255f);

        for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
            PdfPage page = pdfDoc.getPage(i);
            List<Rectangle> highlightRects = new ArrayList<>();

            LocationTextExtractionStrategy strategy = new LocationTextExtractionStrategy() {
                @Override
                public void eventOccurred(IEventData data, EventType type) {
                    if (type == EventType.RENDER_TEXT) {
                        TextRenderInfo renderInfo = (TextRenderInfo) data;
                        String text = renderInfo.getText();

                        for (String keyword : keywords) {
                            if (text.contains(keyword)) {
                                int index = text.indexOf(keyword);
                                while (index >= 0) {
                                    try {
                                        List<TextRenderInfo> charInfos = renderInfo.getCharacterRenderInfos();
                                        if (index + keyword.length() <= charInfos.size()) {
                                            TextRenderInfo firstChar = charInfos.get(index);
                                            TextRenderInfo lastChar = charInfos.get(index + keyword.length() - 1);
                                            Rectangle firstBase = firstChar.getBaseline().getBoundingRectangle();
                                            Rectangle lastBase = lastChar.getBaseline().getBoundingRectangle();
                                            Rectangle firstAscent = firstChar.getAscentLine().getBoundingRectangle();
                                            Rectangle firstDescent = firstChar.getDescentLine().getBoundingRectangle();

                                            Rectangle keywordRect = new Rectangle(
                                                firstBase.getLeft(),
                                                firstDescent.getBottom(),
                                                lastBase.getRight() - firstBase.getLeft(),
                                                firstAscent.getTop() - firstDescent.getBottom()
                                            );

                                            highlightRects.add(keywordRect);
                                        }
                                    } catch (Exception e) {
                                        logger.warn("Error processing keyword highlight: {}", e.getMessage());
                                    }

                                    index = text.indexOf(keyword, index + 1);
                                }
                            }
                        }
                    }
                    super.eventOccurred(data, type);
                }
            };

            PdfCanvasProcessor parser = new PdfCanvasProcessor(strategy);
            parser.processPageContent(page);

            if (!highlightRects.isEmpty()) {
                PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(),
                    page.getResources(), pdfDoc);

                canvas.saveState();
                canvas.setFillColor(highlightColor);
                for (Rectangle rect : highlightRects) {
                    canvas.rectangle(rect.getLeft(), rect.getBottom(),
                        rect.getWidth(), rect.getHeight());
                    canvas.fill();
                }
                canvas.restoreState();
            }
        }
        pdfDoc.close();
    }
    public static void pdfHighlightKeywords(String inputPath, String outputPath, List<String> keywords, java.awt.Color color) throws IOException {
        pdfHighlightKeywords(Files.newInputStream(Paths.get(inputPath)), Files.newOutputStream(Paths.get(outputPath)), keywords, color);
    }

    /**
     * pdf 转图片，每一页一个图片
     * @param inPdfFile 输入pdf文件流
     * @param ppm 每毫米像素数量
     * @return 图片列表
     */
    public static List<BufferedImage> pdf2Images(InputStream inPdfFile, double ppm) {
        List<BufferedImage> images = new ArrayList<>();
        try (PDDocument document = PDDocument.load(inPdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            // 将 ppm (每毫米像素数) 转换为 dpi (每英寸像素数)
            // 1 英寸 = 25.4 毫米
            float dpi = (float) (ppm * 25.4);

            for (int page = 0; page < pageCount; page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi);
                images.add(image);
            }
        } catch (IOException e) {
            logger.error("PDF转图片失败: {}", e.getMessage(), e);
        }
        return images;
    }

    /**
     * pdf 转图片，每一页一个图片
     * @param pdfFilePath PDF文件路径
     * @param ppm 每毫米像素数量
     * @return 图片列表
     */
    public static List<BufferedImage> pdf2Images(String pdfFilePath, double ppm) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(pdfFilePath))) {
            return pdf2Images(inputStream, ppm);
        } catch (IOException e) {
            logger.error("PDF文件读取失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     *  从 pdf 中获取图片
     * @param inPdfFile 输入pdf文件流
     * @return pdf中的图片列表
     */
    public static List<BufferedImage> fetchPdfImages(InputStream inPdfFile) {
        List<BufferedImage> images = new ArrayList<>();
        try (PDDocument document = PDDocument.load(inPdfFile)) {
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources != null) {
                    extractImagesFromResources(resources, images);
                }
            }
        } catch (IOException e) {
            logger.error("从PDF提取图片失败: {}", e.getMessage(), e);
        }
        return images;
    }

    /**
     * 从 pdf 中获取图片
     * @param pdfFilePath PDF文件路径
     * @return pdf中的图片列表
     */
    public static List<BufferedImage> fetchPdfImages(String pdfFilePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(pdfFilePath))) {
            return fetchPdfImages(inputStream);
        } catch (IOException e) {
            logger.error("PDF文件读取失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 从PDF资源中提取图片（递归处理，支持表单对象中的图片）
     * @param resources PDF资源对象
     * @param images 图片列表（输出参数）
     */
    private static void extractImagesFromResources(PDResources resources, List<BufferedImage> images) {
        try {
            for (COSName key : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject(key);
                if (xObject instanceof PDImageXObject) {
                    // 直接图片对象
                    PDImageXObject imageXObject = (PDImageXObject) xObject;
                    BufferedImage image = imageXObject.getImage();
                    if (image != null) {
                        images.add(image);
                    }
                } else if (xObject instanceof PDFormXObject) {
                    // 表单对象，可能包含图片，递归处理
                    PDFormXObject formXObject = (PDFormXObject) xObject;
                    PDResources formResources = formXObject.getResources();
                    if (formResources != null) {
                        extractImagesFromResources(formResources, images);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("提取图片资源失败: {}", e.getMessage(), e);
        }
    }

}
