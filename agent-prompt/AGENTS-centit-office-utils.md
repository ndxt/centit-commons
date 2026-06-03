# centit-office-utils 模块

> Maven 坐标: `com.centit.support:centit-office-utils:JDK21-SNAPSHOT`
> 根包: `com.centit.support.office`
> Office 文档格式转换和处理工具库。

---

## 架构总览

```
com.centit.support.office
├── OfficeToPdf.java         -- Word/Excel/PPT 转 PDF
├── OfficeToHtml.java        -- Word/Excel/PPT 转 HTML
├── PdfUtil.java             -- PDF 工具（合并/高亮/水印/图片提取）
├── Watermark4Pdf.java       -- PDF 水印（文字+图片印章）
├── SignatureUtil.java       -- PDF 数字签名（PKI证书）
├── ImagesToPdf.java         -- 图片合成 PDF
├── OfdUtil.java             -- OFD 转图片/PDF
├── XlsxTransformXls.java    -- XLSX 转 XLS
└── commons/
    ├── Excel2PdfUtils.java      -- Excel 解析为 PDF 表格
    ├── WordTableToPdfUtils.java -- Word 表格提取
    ├── PowerPointUtils.java     -- PPT 渲染为图片/PDF/HTML
    ├── SignatureInfo.java       -- 签名配置（Builder模式）
    ├── PDFPageEvent.java        -- 中文页码事件
    ├── CellFormatConstants.java -- Excel 日期格式常量
    └── CommonUtils.java         -- 路径/Transformer 工具
```

---

## 1. OfficeToPdf — Office 转 PDF

Word/Excel/PPT 转 PDF（抽象类）。

### Word 转 PDF

| 方法 | 描述 |
|------|------|
| `static boolean word2Pdf(InputStream inWordStream, OutputStream outPdfStream, String suffix)` | Word流转PDF；DOCX用XDocReport+中文字体，DOC用HWPF+iText |
| `static boolean word2Pdf(String inWordFile, String outPdfFile, String suffix)` | 文件路径版 |
| `static boolean word2Pdf(String inWordFile, String outPdfFile)` | 自动检测扩展名 |

### Excel 转 PDF

| 方法 | 描述 |
|------|------|
| `static boolean excel2Pdf(InputStream inExcelStream, OutputStream outPdfStream)` | Excel流转PDF（横向A4，含工作表索引链接） |
| `static boolean excel2Pdf(String inExcelFile, String outPdfFile)` | 文件路径版 |

### PPT 转 PDF

| 方法 | 描述 |
|------|------|
| `static boolean ppt2Pdf(String inPptFile, String outPdfFile, String suffix)` | PPT/PPTX转PDF（渲染每页为图片后合成） |
| `static boolean ppt2Pdf(String inPptFile, String outPdfFile)` | 自动检测扩展名 |

---

## 2. OfficeToHtml — Office 转 HTML

Word/Excel/PPT 转 HTML（抽象类）。

### Word 转 HTML

| 方法 | 描述 |
|------|------|
| `static boolean word2Html(InputStream inWordStream, OutputStream outHtmlStream, String imagePath, String suffix)` | Word流转HTML；DOC用POI HWPF，DOCX用Docx4J |
| `static boolean word2Html(String inWordFile, String outHtmlFile)` | 文件路径版 |

### Excel 转 HTML

| 方法 | 描述 |
|------|------|
| `static boolean excel2Html(InputStream inExcelStream, OutputStream outPdfStream, String suffix)` | Excel流转HTML；XLS直接转换，XLSX先转HSSF |
| `static boolean excel2Html(String inExcelFile, String outHtmlFile, String suffix)` | 文件路径版 |
| `static boolean excel2Html(String inExcelFile, String outPdfFile)` | 自动检测扩展名 |

### PPT 转 HTML

| 方法 | 描述 |
|------|------|
| `static boolean ppt2Html(String inPptFile, String outPdfFile, String suffix)` | PPT转HTML（渲染为Base64内嵌图片） |
| `static boolean ppt2Html(String inPptFile, String outPdfFile)` | 自动检测扩展名 |

---

## 3. PdfUtil — PDF 工具

PDF 综合工具类。合并、高亮、扫描检测、转图片、提取图片。

### PDF 合并

| 方法 | 描述 |
|------|------|
| `static void mergePdfFiles(String outputPath, List<String> inputPaths)` | 合并多个 PDF 文件 |
| `static void mergePdfFiles(OutputStream fos, List<InputStream> osPdfs)` | 流方式合并 |

### PDF 安全检测

| 方法 | 描述 |
|------|------|
| `static boolean pdfContainsJSAction(String pdfFilePath)` | 检测 PDF 是否包含 JavaScript |
| `static boolean isScannedPdf(InputStream inputStream)` | 检测是否为扫描版 PDF |

### 关键词高亮

| 方法 | 描述 |
|------|------|
| `static void pdfHighlightKeywords(InputStream inputPath, OutputStream outputPath, List<String> keywords, Color color)` | 关键词高亮（30%透明度） |
| `static void pdfHighlightKeywords(..., String password)` | 支持加密PDF密码 |
| `static void pdfHighlightKeywords(String inputPath, String outputPath, ...)` | 文件路径版 |

### PDF 转图片 / 提取图片

| 方法 | 描述 |
|------|------|
| `static List<BufferedImage> pdf2Images(InputStream inPdfFile, double ppm)` | PDF每页转图片（ppm控制分辨率） |
| `static List<BufferedImage> pdf2Images(String pdfFilePath, double ppm)` | 文件路径版 |
| `static List<BufferedImage> fetchPdfImages(InputStream inPdfFile)` | 提取PDF中嵌入的图片资源 |
| `static List<BufferedImage> fetchPdfImages(String pdfFilePath)` | 文件路径版 |

### 工具方法

| 方法 | 描述 |
|------|------|
| `static PDDocument loadPDFDocument(InputStream inputStream)` | 加载PDF文档（缓冲整个流） |

---

## 4. Watermark4Pdf — PDF 水印

PDF 文字水印和图片印章（抽象类）。

### 文字水印

| 方法 | 描述 |
|------|------|
| `static boolean addWatermark4Pdf(InputStream inputFile, OutputStream outputFile, String waterMarkStr, float opacity, float rotation, float fontSize, boolean isRepeat)` | 添加文字水印到每页 |
| `static boolean addWatermark4Pdf(String inputFile, String outputFile, ...)` | 文件路径版 |
| `static boolean addWatermark4Word(String inputFile, String waterMarkStr, String suffix, boolean isRepeat)` | Word 添加水印（先转PDF再加水印） |

**参数说明**: opacity(0-1), rotation(角度), fontSize, isRepeat(是否平铺)

### 图片印章

| 方法 | 描述 |
|------|------|
| `static void addImage2Pdf(InputStream inputFile, OutputStream outputFile, int page, Image image, float opacity, float x, float y, float w, float h)` | 添加图片印章到指定页 |
| `static void addImage2Pdf(String inputFile, String outputFile, int page, String imageFile, ...)` | 文件路径版 |
| `static void addImage2Pdf(String inputFile, String outputFile, int page, String imageFile, float opacity, float x, float y)` | 简化版（所有页，原始图片尺寸） |

**坐标说明**: x/y/w/h < 1.0 视为页面比例，负值 w/h 使用图片原始尺寸

| 方法 | 描述 |
|------|------|
| `static Image createPdfImage(byte[] imageBytes)` | 从字节数组创建 iText Image |

---

## 5. SignatureUtil — PDF 数字签名

PDF 数字签名工具（基于 PKI 证书，抽象类）。

| 方法 | 描述 |
|------|------|
| `static SignatureInfo createSingInfo()` | 创建签名配置 Builder |
| `static boolean signPdf(InputStream srcStream, OutputStream targetStream, SignatureInfo signatureInfo)` | 签名 PDF（可追加签名） |
| `static boolean signPdf(String src, String target, SignatureInfo signatureInfo)` | 文件路径版 |

---

## 6. ImagesToPdf — 图片合成 PDF

多图片合成 PDF（抽象类）。

| 方法 | 描述 |
|------|------|
| `static void imagesToA4SizePdf(List<Image> imageList, OutputStream outPdfStream)` | 多图排列到 A4 页面（自动计算网格） |
| `static void bufferedImagesToA4SizePdf(List<BufferedImage> imageList, OutputStream outPdfStream)` | BufferedImage 版 |
| `static boolean imagesToPdf(List<BufferedImage> imageList, OutputStream outPdfStream, float zoom)` | 每图一页，zoom 控制缩放 |
| `static Image bufferedImageToPdfImage(BufferedImage image)` | BufferedImage 转 iText Image |

---

## 7. OfdUtil — OFD 转换

OFD（开放版式文档，中国国家标准）转图片和 PDF（抽象类）。

### OFD 转图片

| 方法 | 描述 |
|------|------|
| `static List<BufferedImage> ofd2Images(InputStream inOfdFile, double ppm)` | OFD流转图片列表 |
| `static List<BufferedImage> ofd2Images(String ofdPath, double ppm)` | 文件路径版 |
| `static List<BufferedImage> ofd2Images(String ofdPath)` | 默认 600 DPI |
| `static List<BufferedImage> ofd2Images(InputStream inputStream)` | 流版，默认 600 DPI |

### OFD 转 PDF

| 方法 | 描述 |
|------|------|
| `static boolean ofd2Pdf(String ofdPath, String pdfPath)` | OFD转PDF（经图片中转） |
| `static boolean ofd2Pdf(InputStream inOfdFile, OutputStream outPdfFile)` | 流版 |
| `static boolean ofdToPdf(InputStream inOfdFile, OutputStream outPdfFile)` | 直接转换（高质量，OFDRW） |
| `static boolean ofdToPdf(String inOfdFile, String outPdfFile)` | 文件路径版 |

---

## 8. XlsxTransformXls — XLSX 转 XLS

将 Apache POI `XSSFWorkbook` (XLSX) 转为 `HSSFWorkbook` (XLS)。

| 方法 | 描述 |
|------|------|
| `void transformXSSF(XSSFWorkbook workbookOld, HSSFWorkbook workbookNew)` | 顶层转换，保留sheet名、值、样式、字体、合并区域、列宽、行高 |

---

## 9. commons 包 — 内部工具

### SignatureInfo

PDF 签名配置（@Data，Builder 模式）。

| 字段 | 类型 | 描述 |
|------|------|------|
| `reasonDesc` | String | 签名原因 |
| `locationDesc` | String | 签名位置 |
| `digestAlgorithm` | String | 摘要算法（默认 SHA-1） |
| `signImage` | Image | 印章图片 |
| `fieldName` | String | 表单字段名 |
| `chain` | Certificate[] | 证书链 |
| `pk` | PrivateKey | 私钥 |
| `certificationLevel` | int | 认证级别 (0-3) |
| `renderingMode` | RenderingMode | 渲染模式（默认 GRAPHIC） |
| `signRect` | Rectangle | 印章位置 |
| `signPage` | int | 签名页码 |

**Builder 方法**: `reason()`, `location()`, `algorithm()`, `field()`, `certificate()`, `privateKey()`, `certificateLevel()`, `renderingMode()`, `image()`, `page()`, `rect()`

### Excel2PdfUtils

Excel 解析为 iText PdfPTable 的核心引擎。

| 方法 | 描述 |
|------|------|
| `static PdfPTable toParseContent(Workbook wb, Sheet sheet, int sheetIndex)` | 核心方法：解析整个 Sheet 为 PdfPTable |
| `static String getCellString(Cell cell)` | 获取单元格字符串值（处理所有类型） |
| `static void toCreateContentIndexes(Document document, int sheetSize)` | 创建工作表目录页 |

### WordTableToPdfUtils

Word (DOC) 表格/文本提取为 PDF 元素。

| 方法 | 描述 |
|------|------|
| `static List<Element> extractContentFromDoc(HWPFDocument doc, BaseFont baseFont)` | 提取所有内容（文本+表格，保持文档顺序） |

### PowerPointUtils

PPT/PPTX 渲染为图片、PDF、HTML。

| 方法 | 描述 |
|------|------|
| `static String pptToPdfUseImage(String sourceFilePath, String targetFileName, String suffix)` | PPT转PDF（渲染为图片后合成） |
| `static String pptToHtmlUseImage(String sourceFilePath, String targetFileName, String suffix)` | PPT转HTML（Base64图片） |

### PDFPageEvent

中文页码事件处理器。自动在每页右下角添加"第X页"。

| 成员 | 描述 |
|------|------|
| `static BaseFont BASE_FONT_CHINESE` | 共享中文字体 (STSongStd-Light) |

### CellFormatConstants

Excel 日期/时间格式模式常量接口。包含所有已知的 Excel 日期格式索引和字符串模式。

### CommonUtils

跨平台路径和 XML Transformer 工具。

| 方法 | 描述 |
|------|------|
| `static String mapWidowsPathIfNecessary(String filePath)` | Windows 路径分隔符转换 |
| `static Transformer createTransformer()` | 创建 HTML 输出的 Transformer |
