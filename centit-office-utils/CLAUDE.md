# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建和测试

**构建模块：**
```bash
mvn clean compile
```

**运行测试：**
```bash
mvn test
# 运行单个测试类
mvn test -Dtest=TestToPdf
mvn test -Dtest=TestPdfSign
mvn test -Dtest=TestToHtml
```

**打包：**
```bash
mvn clean package
```

**安装到本地仓库：**
```bash
mvn clean install
```

## 模块架构

这是 centit-commons 多模块项目中的 Office 文档处理工具模块，专门用于处理 Office 文档（Word、Excel、PPT）与 PDF 的转换、操作和处理。

### 核心功能组件

**文档转换工具:**
- `OfficeToPdf`: Office 文档转 PDF
  - `word2Pdf()`: 支持 DOC/DOCX 转 PDF，包含中文字体处理
  - `excel2Pdf()`: Excel 转 PDF，支持多工作表
  - `ppt2Pdf()`: PowerPoint 转 PDF

- `OfficeToHtml`: Office 文档转 HTML
  - `word2Html()`: DOC/DOCX 转 HTML，支持图片提取
  - `excel2Html()`: Excel 转 HTML
  - `ppt2Html()`: PPT 转 HTML

**PDF 处理工具:**
- `PdfSignatureUtil`: PDF 数字签名
  - `sign()`: 使用证书对 PDF 进行数字签名
  - 支持签名位置、外观自定义

- `Watermark4Pdf`: PDF 水印处理
  - `addWatermark4Pdf()`: 添加文字水印，支持重复、透明度、旋转
  - `addImage2Pdf()`: 添加图片水印/印章

- `ImagesToPdf`: 图片转 PDF
  - `imagesToA4SizePdf()`: 多张图片合并为 A4 PDF
  - `bufferedImagesToA4SizePdf()`: BufferedImage 列表转 PDF

**文档格式处理:**
- `XlsxTransformXls`: Excel 格式转换
- `OfdUtils`: OFD 文档处理（中国版式文档标准）
- `DocOptUtil`: Word 文档操作工具

**公共组件 (commons/):**
- `CommonUtils`: 通用工具，路径处理、XML 转换器创建
- `PowerPointUtils`: PPT 转换核心实现
- `Excel2PdfUtils`: Excel 转 PDF 核心实现
- `SignatureInfo`: 数字签名配置信息
- `PDFPageEvent`: PDF 页面事件处理

### 技术依赖

**主要外部库:**
- Apache POI: Office 文档读写（poi-scratchpad）
- iText: PDF 处理（itextpdf + itext-asian 中文支持）
- Apache PDFBox: PDF 操作
- OpenSagres XDocReport: Office 转换
- OFDRW: OFD 文档处理
- Docx4J: DOCX 处理
- BouncyCastle: 数字签名加密支持

**字体资源:**
- 内置中文字体文件：`simfang.ttf`（仿宋）、`simhei.ttf`（黑体）、`simkai.ttf`（楷体）、`simsun.ttf`（宋体）

### 开发约定

1. **工具类设计**: 所有主要类都是抽象类，提供静态方法，构造器抛出 IllegalAccessError
2. **异常处理**: 使用 slf4j 记录错误日志，方法返回 boolean 表示成功/失败
3. **资源管理**: 使用 try-with-resources 确保流正确关闭
4. **路径处理**: 使用 `CommonUtils.mapWidowsPathIfNecessary()` 处理跨平台路径
5. **中文支持**: Office 转 PDF 时特别处理中文字体映射

### 测试结构

测试类位于 `src/test/java/com/centit/test/`：
- `TestToPdf`: PDF 转换测试
- `TestToHtml`: HTML 转换测试  
- `TestPdfSign`: PDF 签名测试
- `TestPdfUtils`: PDF 工具测试
- `TestAddImage`: 图片添加测试

测试资源位于 `src/test/resources/template/`，包含测试用的 PDF、图片、证书文件。

## 模块依赖

- 依赖 `centit-utils` 模块的基础工具类
- 父项目：`com.centit.support:centit-commons`
- JDK 版本要求：最低 17，目标 21