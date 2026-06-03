# centit-report-utils 模块

> Maven 坐标: `com.centit.support:centit-report-utils:JDK21-SNAPSHOT`
> 根包: `com.centit.support.report`
> 基于 Apache POI、JXLS、XDocReport、XChart 的报表工具库。

---

## 架构总览

```
com.centit.support.report
├── ExcelExportUtil.java      -- Excel 导出（直接/模板/追加）
├── ExcelImportUtil.java      -- Excel 导入（Bean/Map/数组）
├── ExcelReportUtil.java      -- JXLS 模板 Excel 报表
├── ExcelTypeEnum.java        -- Excel 文件类型枚举
├── LargeExcelImportUtil.java -- 大文件流式导入（SAX）
├── ChartImageUtils.java      -- 图表生成（柱状图/折线图/饼图）
├── WordReportUtil.java       -- Word 报表（Freemarker 模板）
├── SmartDocxContext.java     -- 智能文档上下文
├── JsonDocxContext.java      -- JSON 文档上下文
└── JxlsCommand/
    └── AutoRowHeightCommand.java  -- JXLS 自动行高指令
```

---

## 1. ExcelTypeEnum

Excel 文件类型枚举。

| 枚举值 | 描述 |
|--------|------|
| `HSSF` | Excel 2003 (.xls) |
| `XSSF` | Excel 2007+ (.xlsx) |
| `NOTEXCEL` | 非 Excel 文件 |

| 方法 | 描述 |
|------|------|
| `static ExcelTypeEnum checkFileExcelType(byte[] headerBytes)` | 通过文件头字节判断类型 |
| `static ExcelTypeEnum checkFileExcelType(InputStream inputStream)` | 通过流判断类型 |
| `static ExcelTypeEnum checkFileExcelType(String filePath)` | 通过路径判断（优先扩展名，其次文件头） |

---

## 2. ExcelExportUtil — Excel 导出

Excel 导出工具（抽象类），使用 `SXSSFWorkbook` 流式处理大数据量。

### 生成 Excel 到 OutputStream (XSSF)

| 方法 | 描述 |
|------|------|
| `static void generateExcel(OutputStream os, String sheetName, List<?> objLists, String[] header, String[] property)` | 对象列表导出 |
| `static void generateExcel(OutputStream os, List<?> objLists, String[] header, String[] property)` | 默认 sheet |
| `static void generateExcel(OutputStream os, String sheetName, List<Object[]> objLists, String[] header)` | 二维数组导出 |
| `static void generateExcel(OutputStream os, String sheetName, List<?> objLists, Class<?> objType)` | 通过类自动提取属性 |
| `static void generateExcel(OutputStream os, List<?> objLists, Class<?> objType)` | 默认 sheet + 自动属性 |

### 生成 Excel 2003 (HSSF)

| 方法 | 描述 |
|------|------|
| `static void generateExcel2003(...)` | 与 XSSF 版本参数一致，生成 .xls 格式 |

### 生成 Excel InputStream

| 方法 | 描述 |
|------|------|
| `static InputStream generateExcelStream(...)` | 返回 InputStream（XSSF/HSSF 各一组） |
| `static InputStream generateExcel2003Stream(...)` | 返回 Excel 2003 InputStream |

### Sheet 级别操作

| 方法 | 描述 |
|------|------|
| `static void generateExcelSheet(Sheet sheet, List<?> objLists, Class<?> objType)` | 写入已有 Sheet |
| `static void generateExcelSheet(Sheet sheet, List<?> objLists, String[] header, String[] property)` | 对象列表写入 |
| `static void generateExcelSheet(Sheet sheet, List<Object[]> objLists, String[] header)` | 二维数组写入 |

### 模板导出

| 方法 | 描述 |
|------|------|
| `static void generateExcelByTemplate(String templatePath, String outPath, String sheetName, List<?> objects, Map<Integer, String> fieldDesc, int beginRow, boolean createRow)` | 基于模板导出 |
| `static void generateExcelByTemplate(String templatePath, String outPath, int sheetIndex, ...)` | 按 sheet 索引 |
| `static void generateExcelByTemplate(..., List<Object[]> objects, int beginCol, int beginRow, boolean createRow)` | 二维数组模板导出 |

### 追加数据

| 方法 | 描述 |
|------|------|
| `static void appendDataToExcelSheet(String excelFilePath, String sheetName, List<?> objLists, String[] header, String[] property)` | 追加对象数据 |
| `static void appendDataToExcelSheet(String excelFilePath, String sheetName, List<Object[]> objLists, String[] header)` | 追加数组数据 |

### 工具方法

| 方法 | 描述 |
|------|------|
| `static void setCellValue(Cell cell, Object value)` | 通用单元格值设置 |
| `static void copyRow(Workbook workbook, Sheet sheet, int from, int to, int copyRowNum, Integer colNum)` | 复制行 |
| `static void copyCell(Workbook workbook, Cell src, Cell dist)` | 复制单元格 |
| `static void copyCellStyle(CellStyle from, CellStyle to, Workbook workbook)` | 复制样式 |
| `static Set<Integer> praiseColsRangeDesc(String mergeColCellDesc)` | 解析列范围描述（"A-C,E,F"） |

---

## 3. ExcelImportUtil — Excel 导入

Excel 导入工具（抽象类）。支持对象列表、Map列表、字符串数组三种导入方式。

### 列索引映射

| 方法 | 描述 |
|------|------|
| `static int mapColumnIndex(String column)` | 列标识转索引（"A"->0, "AA"->26） |
| `static String mapIndexColumn(int ind)` | 索引转列标识 |
| `static Map<Integer, String> mapColumnIndex(Map<String, String> fieldDesc)` | 字符串映射转数值映射 |

### 单元格操作

| 方法 | 描述 |
|------|------|
| `static Object getCellValue(Cell cell)` | 获取原始值 |
| `static String getCellString(Cell cell)` | 获取字符串值 |
| `static Cell getCell(Sheet sheet, int row, int col)` | 获取 Cell |

### 导入为 Java Bean 列表

| 方法 | 描述 |
|------|------|
| `static <T> List<T> loadObjectFromExcel(InputStream excelFile, ExcelTypeEnum excelType, String sheetName, Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow, int endRow)` | 流 + sheet名 + 属性映射 |
| `static <T> List<T> loadObjectFromExcel(String filePath, String sheetName, Class<T> beanType, Map<Integer, String> fieldDesc, int beginRow, int endRow)` | 路径版 |
| `static <T> List<T> loadObjectFromExcel(..., int sheetIndex, ...)` | 通过 sheet 索引定位 |
| （多个重载） | 支持流/路径 x sheet名/索引 x 有/无endRow 共8个变体 |

### 导入为二维字符串数组

| 方法 | 描述 |
|------|------|
| `static List<String[]> loadDataFromExcel(InputStream excelFile, ExcelTypeEnum excelType, int sheetIndex, int[] columnList, int[] rowList)` | 指定列和行数组 |
| `static List<String[]> loadDataFromExcel(..., int beginCol, int endCol, int beginRow, int endRow)` | 指定行列范围 |
| （多个重载） | 支持流/路径 x sheet名/索引 x 各种范围组合 |

### 导入为 Map 列表（自动表头）

| 方法 | 描述 |
|------|------|
| `static List<Map<String, Object>> loadMapFromExcelSheet(String filePath, String sheetName)` | 第一行作表头 |
| `static List<Map<String, Object>> loadMapFromExcelSheet(String filePath, int sheetIndex, int headerRow, int beginRow, int endRow)` | 指定表头行和起止行 |
| `static List<Map<String, Object>> loadMapFromExcelSheet(InputStream excelFile, int sheetIndex, int headerRow, int beginRow, int endRow, int beginColumn, int endColumn)` | 完整参数版 |

### 导入为 Map 列表（合并单元格）

| 方法 | 描述 |
|------|------|
| `static List<Map<String, Object>> loadMapFromExcelSheetUseMergeCell(InputStream excelFile, int sheetIndex, int headerRow, int beginRow, int endRow, int beginColumn, int endColumn)` | 自动处理合并单元格 |

### 导入为 Map 列表（列标识作 Key）

| 方法 | 描述 |
|------|------|
| `static List<Map<String, Object>> loadMapFromExcelSheetUseIndexAsKey(InputStream excelFile, String sheetName, int beginRow, int endRow, boolean useUpMergeCell)` | 用 A/B/C... 作 key |

### 工具方法

| 方法 | 描述 |
|------|------|
| `static LeftRightPair<ExcelTypeEnum, InputStream> checkExcelInputStreamType(InputStream input)` | 检测流类型 |
| `static String[] loadColumnsFromExcel(String filePath, int sheetIndex)` | 读取第一行列名 |

---

## 4. LargeExcelImportUtil — 大文件流式导入

大文件 Excel 流式导入（仅 XLSX），使用 SAX 事件驱动，Consumer 回调逐行处理（抽象类）。

### 解析为对象

| 方法 | 描述 |
|------|------|
| `static <T> void parserXSSFSheet(String xlsxFile, String sheetName, int beginRow, Class<T> beanType, Map<Integer, String> fieldDesc, Consumer<T> consumer)` | 流式解析为 Bean |
| `static <T> void parserXSSFSheet(String xlsxFile, String sheetName, int beginRow, int endRow, ...)` | 指定结束行 |

### 解析为 Map

| 方法 | 描述 |
|------|------|
| `static void parserXSSFSheet(String xlsxFile, String sheetName, int beginRow, Consumer<Map<Integer, Object>> consumer)` | 列索引作 key |
| `static void parserXSSFSheet(String xlsxFile, String sheetName, int beginRow, int endRow, ...)` | 指定结束行 |

### 带表头解析

| 方法 | 描述 |
|------|------|
| `static void parserXSSFSheetWithHead(String xlsxFile, String sheetName, int headRow, int beginRow, int endRow, Consumer<Map<String, Object>> consumer)` | 表头列名作 key |
| `static void parserXSSFSheetWithHead(String xlsxFile, String sheetName, int headRow, int beginRow, ...)` | 自动到最后一行 |
| `static void parserXSSFSheetWithHead(String xlsxFile, String sheetName, int headRow, ...)` | beginRow = headRow+1 |
| `static void parserXSSFSheetWithHead(String xlsxFile, String sheetName, Consumer<Map<String, Object>> consumer)` | headRow=0, beginRow=1 |

---

## 5. ExcelReportUtil — JXLS 模板报表

基于 JXLS 模板引擎的 Excel 报表导出（抽象类）。

| 方法 | 描述 |
|------|------|
| `static void exportExcel(InputStream is, OutputStream os, Map<String, Object> model, Map<String, Object> extendFuns)` | JXLS 模板导出，内置 `utils` 命名空间指向 `EmbedFuncUtils` |
| `static void exportExcel(InputStream is, OutputStream os, Map<String, Object> model)` | 不使用扩展函数 |

模板中使用批注指令：`jx:area`, `jx:each`, `jx:autoRowHeight(lastCell="C3")`

---

## 6. ChartImageUtils — 图表生成

基于 XChart 的图表图片生成（抽象类）。

### 常量

| 常量 | 值 | 描述 |
|------|-----|------|
| `CHART_TYPE_BAR` | "bar" | 柱状图 |
| `CHART_TYPE_LINE` | "line" | 折线图 |
| `CHART_TYPE_PIE` | "pie" | 饼图 |
| `COLORS` | 11种颜色 | 预定义颜色 |

### 方法

| 方法 | 描述 |
|------|------|
| `static Chart<?,?> createChart(String chartType, String title, int width, int height, JSONObject data, Map<String,Object> style)` | 通用图表创建 |
| `static CategoryChart createBarChart(String title, int width, int height, JSONObject data, Map<String,Object> style)` | 柱状图 |
| `static XYChart createLineChart(String title, int width, int height, JSONObject data, Map<String,Object> style)` | 折线图 |
| `static PieChart createPieChart(String title, int width, int height, JSONObject data, Map<String,Object> style)` | 饼图 |
| `static BufferedImage createChartImage(String chartType, String title, int width, int height, JSONObject data, Map<String,Object> style)` | 创建图表图片 |

### 数据格式

```json
// 折线图/柱状图
{"xAxisTitle":"月份", "yAxisTitle":"销售额", "xData":[1,2,3,4,5,6],
 "series":[{"name":"A","data":[100,200,300,400,500,600]}]}

// 饼图
{"series":[{"name":"A","value":40},{"name":"B","value":30}]}
```

---

## 7. WordReportUtil — Word 报表

基于 Freemarker 模板的 Word 报表生成（抽象类）。

| 方法 | 描述 |
|------|------|
| `static void convertDocxToPdf(String docxFilePath, String pdfFilePath)` | Docx 转 PDF |
| `static void reportDocxWithFreemarker(Object params, String templateName, String outputFileName)` | Freemarker 模板 + JsonDocxContext（支持图片/集合） |
| `static void reportListDocxWithFreemarker(Object params, String templateName, String outputFileName)` | Freemarker 模板 + 标准 IContext（扁平键值对） |
| `static void reportSmartDocxWithFreemarker(Object params, String templateName, String outputFileName)` | Freemarker 模板 + SmartDocxContext（支持 NP_ 非空判断） |

---

## 8. JsonDocxContext

JSON 文档上下文（实现 IContext），用于 XDocReport 模板。

| 方法 | 描述 |
|------|------|
| 构造: `JsonDocxContext()` / `JsonDocxContext(Object object)` | 从 Map 或 Bean 创建 |
| `Object get(String key)` | 支持点号表达式、`img_` 前缀自动处理图片、集合 Map 自动包装 |
| `Object put(String key, Object value)` | 注册键值对 |
| `void putMap(Map<String, Object> contextMap)` | 合并 Map |
| `Map<String, Object> getContextMap()` | 获取底层数据 |

---

## 9. SmartDocxContext

智能文档上下文（实现 IContext），支持 `NP_` 非空前缀。

| 方法 | 描述 |
|------|------|
| 构造: `SmartDocxContext()` / `SmartDocxContext(Object object)` | 从 Map 或 Bean 创建 |
| `Object get(String key)` | `NP_` 前缀：值为 null 时返回空 SmartDocxContext 而非 null |
| `Object put(String key, Object value)` | 注册键值对 |

---

## 10. AutoRowHeightCommand

JXLS 自定义指令，设置自动行高。

在 Excel 模板批注中使用：`jx:autoRowHeight(lastCell="C3")`

| 方法 | 描述 |
|------|------|
| `String getName()` | 返回 "autoRowHeight" |
| `Size applyAt(CellRef cellRef, Context context)` | 执行指令，设置行高为 -1（自动） |

---

## 典型使用场景

| 场景 | 推荐方法 |
|------|---------|
| 小文件导出 | `ExcelExportUtil.generateExcel()` |
| 小文件导入 | `ExcelImportUtil.loadObjectFromExcel()` / `loadMapFromExcelSheet()` |
| 大文件导入 (>1万行) | `LargeExcelImportUtil.parserXSSFSheet()` + Consumer |
| Excel 模板报表 | `ExcelReportUtil.exportExcel()` + JXLS 模板 |
| Word 报表 | `WordReportUtil.reportDocxWithFreemarker()` |
| 图表 | `ChartImageUtils.createChartImage()` |
| 数据追加 | `ExcelExportUtil.appendDataToExcelSheet()` |
