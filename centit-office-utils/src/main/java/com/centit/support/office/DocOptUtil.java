package com.centit.support.office;

import com.itextpdf.kernel.colors.ColorConstants;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static boolean containsAnyKeyWords(final String cs, final List<String> keywords) {
        if (StringUtils.isBlank(cs) || keywords== null || keywords.isEmpty()) {
            return false;
        }
        for(String keyword : keywords){
            if(cs.contains( keyword)) return true;
        }
        return false;
    }

    public static void pdfHighlightKeywords(InputStream inputPath, OutputStream outputPath, List<String> keywords) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(
            new com.itextpdf.kernel.pdf.PdfReader(inputPath),
            new PdfWriter(outputPath)
        );

        for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
            PdfPage page = pdfDoc.getPage(i);
            List<Rectangle> highlightRects = new ArrayList<>();

            LocationTextExtractionStrategy strategy = new LocationTextExtractionStrategy() {
                @Override
                public void eventOccurred(IEventData data, EventType type) {
                    if (type == EventType.RENDER_TEXT) {
                        TextRenderInfo renderInfo = (TextRenderInfo) data;
                        String text = renderInfo.getText();

                        if (containsAnyKeyWords(text, keywords)) {
                            Rectangle baseRect = renderInfo.getBaseline().getBoundingRectangle();
                            Rectangle ascentRect = renderInfo.getAscentLine().getBoundingRectangle();
                            Rectangle descentRect = renderInfo.getDescentLine().getBoundingRectangle();

                            Rectangle fullRect = new Rectangle(
                                baseRect.getLeft(),
                                descentRect.getBottom(),
                                baseRect.getWidth(),
                                ascentRect.getTop() - descentRect.getBottom()
                            );

                            highlightRects.add(fullRect);
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
                canvas.setFillColor(ColorConstants.YELLOW);

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
    public static void pdfHighlightKeywords(String inputPath, String outputPath, List<String> keywords) throws IOException {
        pdfHighlightKeywords(Files.newInputStream(Paths.get(inputPath)), Files.newOutputStream(Paths.get(outputPath)), keywords);
    }
}
