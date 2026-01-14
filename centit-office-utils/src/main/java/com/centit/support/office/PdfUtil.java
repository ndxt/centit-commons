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
import lombok.Getter;
import org.apache.pdfbox.Loader;
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
 * &lt;dependency&gt;
 * &lt;groupId&gt;org.ofdrw&lt;/groupId&gt;
 * &lt;artifactId&gt;ofdrw-tool&lt;/artifactId&gt;
 * &lt;version&gt;2.3.5&lt;/version&gt;
 * &lt;/dependency&gt;
 */

public class PdfUtil {
    private static final Logger logger = LoggerFactory.getLogger(PdfUtil.class);

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
        try (PDDocument document = Loader.loadPDF(new File(pdfFilePath))){
            String cosName = document.getDocument().getTrailer().toString();
            if (cosName.contains("COSName{JS}")) {
                return true;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 检测 PDF 是否为扫描件（主要是图片，无有效文本层）
     *
     * @param inputStream PDF 文件流
     * @return true 表示是扫描件，false 表示有文本层
     */
    public static boolean isScannedPdf(InputStream inputStream) {
        try (PDDocument document = loadPDFDocument(inputStream)) {
            return isScannedPdf(document);
        } catch (IOException e) {
            logger.error("检测扫描件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检测 PDF 是否为扫描件的内部实现
     * 综合判断：图片数量 + 文本质量
     */
    private static boolean isScannedPdf(PDDocument document) {
        try {
            // 1. 检查图片数量（扫描件通常每页都有图片）
            int totalImages = 0;
            int totalPages = document.getNumberOfPages();

            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources != null) {
                    int pageImages = countImages(resources);
                    totalImages += pageImages;
                }
            }

            // 2. 提取文本并分析质量
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = stripper.getText(document).trim();

            // 3. 综合判断
            // 如果图片数量 >= 页数，且文本质量差，判定为扫描件
            boolean hasManyImages = totalImages >= totalPages;

            // 计算有效字符比例
            int totalChars = text.length();
            if (totalChars < 20) {
                // 文本太少，结合图片判断
                return hasManyImages;
            }

            int validChars = 0;
            for (char c : text.toCharArray()) {
                if (c >= '\u4e00' && c <= '\u9fff') { // 中文
                    validChars++;
                } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) { // 英文
                    validChars++;
                } else if (c >= '0' && c <= '9') { // 数字
                    validChars++;
                } else if (Character.isWhitespace(c)) { // 空格
                    validChars++;
                }
            }

            double validRatio = (double) validChars / totalChars;

            // 判断逻辑：
            // 1. 有大量图片 + 文本质量差 = 扫描件（隐藏文本层通常是乱码）
            // 2. 文本质量极差（< 10%）= 扫描件
            // 3. 其他情况 = 普通文本 PDF
            if (hasManyImages && validRatio < 0.9) {
                return true;
            }
            return validRatio < 0.1;

        } catch (IOException e) {
            logger.error("提取 PDF 文本失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 递归统计资源中的图片数量
     */
    private static int countImages(PDResources resources) throws IOException {
        int count = 0;
        for (COSName name : resources.getXObjectNames()) {
            PDXObject xobject = resources.getXObject(name);
            if (xobject instanceof PDImageXObject) {
                count++;
            } else if (xobject instanceof PDFormXObject form) {
                if (form.getResources() != null) {
                    count += countImages(form.getResources());
                }
            }
        }
        return count;
    }

    public static void pdfHighlightKeywords(InputStream inputPath, OutputStream outputPath, List<String> keywords, java.awt.Color color) throws IOException {
        // 将输入流缓存到字节数组，因为需要读取两次（检测和高亮）
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputPath.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] pdfBytes = buffer.toByteArray();

        // 检测是否是扫描件
        boolean isScanned;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes)) {
            isScanned = isScannedPdf(bais);
        }

        logger.info("PDF 检测结果: {}", isScanned ? "扫描件" : "有文本层");

        if (isScanned) {
            // 扫描件：使用 newContentStreamAfter() 确保高亮不被图片覆盖
            pdfHighlightKeywordsOnScanned(new ByteArrayInputStream(pdfBytes), outputPath, keywords, color);
        } else {
            // 有文本层：使用正常的文本高亮方式
            pdfHighlightKeywordsOnTextLayer(new ByteArrayInputStream(pdfBytes), outputPath, keywords, color);
        }
    }

    /**
     * 对有文本层的 PDF 进行关键词高亮
     */
    private static void pdfHighlightKeywordsOnTextLayer(InputStream inputPath, OutputStream outputPath, List<String> keywords, java.awt.Color color) throws IOException {
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

            // 使用增强的关键词提取策略
            AdvancedKeywordTextExtractionStrategy strategy = new AdvancedKeywordTextExtractionStrategy(keywords);

            PdfCanvasProcessor parser = new PdfCanvasProcessor(strategy);
            parser.processPageContent(page);

            // 获取处理后的高亮矩形
            List<Rectangle> highlightRects = strategy.getHighlightRectangles();

            if (!highlightRects.isEmpty()) {
                // 使用 newContentStreamBefore() 在内容流之前绘制高亮
                PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(),
                    page.getResources(), pdfDoc);

                canvas.saveState();

                // 创建扩展图形状态设置透明度
                com.itextpdf.kernel.pdf.extgstate.PdfExtGState gState =
                    new com.itextpdf.kernel.pdf.extgstate.PdfExtGState();
                gState.setFillOpacity(0.3f); // 30% 透明度
                canvas.setExtGState(gState);

                canvas.setFillColor(highlightColor);

                for (Rectangle rect : highlightRects) {
                    canvas.rectangle(rect.getLeft(), rect.getBottom(),
                        rect.getWidth(), rect.getHeight());
                    canvas.fill();
                }
                canvas.restoreState();

                logger.debug("页面 {} 添加了 {} 个高亮矩形", i, highlightRects.size());
            } else {
                logger.debug("页面 {} 未找到匹配的关键词", i);
            }
        }
        pdfDoc.close();
    }

    /**
     * 对扫描件 PDF 进行关键词高亮
     * 使用 newContentStreamAfter() 确保高亮显示在图片上方
     */
    private static void pdfHighlightKeywordsOnScanned(InputStream inputPath, OutputStream outputPath, List<String> keywords, java.awt.Color color) throws IOException {
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

            // 使用增强的关键词提取策略
            AdvancedKeywordTextExtractionStrategy strategy = new AdvancedKeywordTextExtractionStrategy(keywords);

            PdfCanvasProcessor parser = new PdfCanvasProcessor(strategy);
            parser.processPageContent(page);

            // 获取处理后的高亮矩形
            List<Rectangle> highlightRects = strategy.getHighlightRectangles();

            if (!highlightRects.isEmpty()) {
                // 扫描件使用 newContentStreamAfter() 确保高亮显示在图片上方
                PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(),
                    page.getResources(), pdfDoc);

                canvas.saveState();

                // 创建扩展图形状态设置透明度
                com.itextpdf.kernel.pdf.extgstate.PdfExtGState gState =
                    new com.itextpdf.kernel.pdf.extgstate.PdfExtGState();
                gState.setFillOpacity(0.4f); // 40% 透明度，更明显
                canvas.setExtGState(gState);

                canvas.setFillColor(highlightColor);

                for (Rectangle rect : highlightRects) {
                    canvas.rectangle(rect.getLeft(), rect.getBottom(),
                        rect.getWidth(), rect.getHeight());
                    canvas.fill();
                }
                canvas.restoreState();

                logger.debug("扫描件页面 {} 添加了 {} 个高亮矩形", i, highlightRects.size());
            } else {
                logger.debug("扫描件页面 {} 未找到匹配的关键词", i);
            }
        }
        pdfDoc.close();
    }

    // 增强版关键词提取策略，处理一字一行的情况
    private static class AdvancedKeywordTextExtractionStrategy extends LocationTextExtractionStrategy {
        private final List<String> keywords;
        private final List<Rectangle> highlightRectangles = new ArrayList<>();

        // 存储页面上的文本信息
        private final List<TextPositionInfo> textPositions = new ArrayList<>();

        public AdvancedKeywordTextExtractionStrategy(List<String> keywords) {
            this.keywords = keywords != null ? keywords : new ArrayList<>();
        }

        @Override
        public void eventOccurred(IEventData data, EventType type) {
            if (type == EventType.RENDER_TEXT) {
                TextRenderInfo renderInfo = (TextRenderInfo) data;

                // 收集文本位置信息
                collectTextPositionInfo(renderInfo);
            }
            super.eventOccurred(data, type);
        }

        // 收集文本位置信息
        private void collectTextPositionInfo(TextRenderInfo renderInfo) {
            List<TextRenderInfo> charInfos = renderInfo.getCharacterRenderInfos();
            for (TextRenderInfo charInfo : charInfos) {
                String text = charInfo.getText();
                if (text != null && !text.trim().isEmpty()) {
                    TextPositionInfo info = new TextPositionInfo(
                        text,
                        charInfo.getBaseline().getBoundingRectangle(),
                        charInfo.getAscentLine().getBoundingRectangle(),
                        charInfo.getDescentLine().getBoundingRectangle()
                    );
                    textPositions.add(info);
                }
            }
        }


        // 在页面处理完成后执行关键词匹配
        public void processKeywords() {
            if (keywords.isEmpty() || textPositions.isEmpty()) {
                return;
            }

            // 将文本位置信息重构为连续文本
            ReconstructedTextInfo reconstructed = reconstructText();

            // 对每个关键词进行匹配
            for (String keyword : keywords) {
                if (keyword != null && !keyword.isEmpty()) {
                    findKeywordPositions(reconstructed, keyword);
                }
            }
        }

        // 重构文本，将相邻字符连接
        private ReconstructedTextInfo reconstructText() {
            StringBuilder fullText = new StringBuilder();
            List<Integer> charToPositionMap = new ArrayList<>(); // 字符索引到原始位置的映射

            for (int i = 0; i < textPositions.size(); i++) {
                TextPositionInfo info = textPositions.get(i);
                String charText = info.getText();
                fullText.append(charText);

                // 记录每个字符在重构文本中的位置对应原始位置
                for (int j = 0; j < charText.length(); j++) {
                    charToPositionMap.add(i);
                }
            }

            return new ReconstructedTextInfo(fullText.toString(), charToPositionMap);
        }

        // 查找关键词位置并生成高亮矩形
        private void findKeywordPositions(ReconstructedTextInfo reconstructed, String keyword) {
            String fullText = reconstructed.getText().toLowerCase();
            String searchKeyword = keyword.toLowerCase();

            int startIndex = 0;
            while ((startIndex = fullText.indexOf(searchKeyword, startIndex)) >= 0) {
                try {
                    // 获取关键词在原始位置列表中的起始和结束索引
                    int startPosIndex = reconstructed.getCharToPositionMap().get(startIndex);
                    int endPosIndex = reconstructed.getCharToPositionMap().get(
                        Math.min(startIndex + keyword.length() - 1, reconstructed.getCharToPositionMap().size() - 1)
                    );

                    // 获取起始和结束字符的位置信息
                    TextPositionInfo startInfo = textPositions.get(startPosIndex);
                    TextPositionInfo endInfo = textPositions.get(endPosIndex);

                    // 创建跨越多个字符的高亮矩形
                    Rectangle combinedRect = createCombinedRectangle(startInfo, endInfo);
                    highlightRectangles.add(combinedRect);

                } catch (IndexOutOfBoundsException e) {
                    logger.warn("索引越界，跳过关键词匹配: {}", e.getMessage());
                }

                startIndex++;
            }
        }

        // 创建跨越多个字符的矩形
        private Rectangle createCombinedRectangle(TextPositionInfo startInfo, TextPositionInfo endInfo) {
            float left = Math.min(startInfo.getBaseline().getLeft(), endInfo.getBaseline().getLeft());
            float right = Math.max(startInfo.getBaseline().getRight(), endInfo.getBaseline().getRight());
            float bottom = Math.min(startInfo.getDescent().getBottom(), endInfo.getDescent().getBottom());
            float top = Math.max(startInfo.getAscent().getTop(), endInfo.getAscent().getTop());

            // 添加一些边距以确保完全覆盖
            float margin = 1.0f;
            return new Rectangle(left - margin, bottom - margin,
                right - left + 2 * margin, top - bottom + 2 * margin);
        }

        public List<Rectangle> getHighlightRectangles() {
            // 在返回结果前处理关键词匹配
            processKeywords();
            return highlightRectangles;
        }
    }

    // 文本位置信息类
    @Getter
    private static class TextPositionInfo {
        // getter方法
        private final String text;
        private final Rectangle baseline;
        private final Rectangle ascent;
        private final Rectangle descent;

        public TextPositionInfo(String text, Rectangle baseline, Rectangle ascent, Rectangle descent) {
            this.text = text;
            this.baseline = baseline;
            this.ascent = ascent;
            this.descent = descent;
        }

    }

    // 重构文本信息类
    @Getter
    private static class ReconstructedTextInfo {
        private final String text;
        private final List<Integer> charToPositionMap;

        public ReconstructedTextInfo(String text, List<Integer> charToPositionMap) {
            this.text = text;
            this.charToPositionMap = charToPositionMap;
        }

    }

    public static void pdfHighlightKeywords(String inputPath, String outputPath, List<String> keywords, java.awt.Color color) throws IOException {
        pdfHighlightKeywords(Files.newInputStream(Paths.get(inputPath)), Files.newOutputStream(Paths.get(outputPath)), keywords, color);
    }

    public static PDDocument loadPDFDocument(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return Loader.loadPDF(buffer.toByteArray());
    }
    /**
     * pdf 转图片，每一页一个图片
     *
     * @param inPdfFile 输入pdf文件流
     * @param ppm       每毫米像素数量
     * @return 图片列表
     */
    public static List<BufferedImage> pdf2Images(InputStream inPdfFile, double ppm) {
        List<BufferedImage> images = new ArrayList<>();
        try (PDDocument document = loadPDFDocument(inPdfFile)) {
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
     *
     * @param pdfFilePath PDF文件路径
     * @param ppm         每毫米像素数量
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
     * 从 pdf 中获取图片
     *
     * @param inPdfFile 输入pdf文件流
     * @return pdf中的图片列表
     */
    public static List<BufferedImage> fetchPdfImages(InputStream inPdfFile) {
        List<BufferedImage> images = new ArrayList<>();
        try (PDDocument document = loadPDFDocument(inPdfFile)) {
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
     *
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
     *
     * @param resources PDF资源对象
     * @param images    图片列表（输出参数）
     */
    private static void extractImagesFromResources(PDResources resources, List<BufferedImage> images) {
        try {
            for (COSName key : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject(key);
                if (xObject instanceof PDImageXObject imageXObject) {
                    // 直接图片对象
                    BufferedImage image = imageXObject.getImage();
                    if (image != null) {
                        images.add(image);
                    }
                } else if (xObject instanceof PDFormXObject formXObject) {
                    // 表单对象，可能包含图片，递归处理
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
