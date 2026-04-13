# centit-commons AI Agent API 索引

> **用途**: 本文件是 AI Agent 快速定位 API 的入口。根据用户需求查找对应的模块和文件，按需加载详细文档。
> **总计**: 5 个模块、16 个文档文件、约 130 个类、96KB 文档。

---

## 项目速查

| 项目 | 值 |
|------|-----|
| 坐标 | `com.centit.support:centit-commons:JDK21-SNAPSHOT` |
| JDK | 17+（目标 21） |
| 包前缀 | `com.centit.support.*` |
| 构建 | `mvn clean compile` / `mvn test` / `mvn clean install` |
| 异常 | 统一 `ObjectException` |
| JSON | fastjson2 |
| 测试 | JUnit，类名 `Test` 开头 |

---

## 需求 → 文件定位

**根据用户需求直接找到应加载的文件：**

| 需求关键词 | 加载文件 | 核心类 |
|-----------|---------|--------|
| 日期、时间、农历、日历 | `AGENTS-centit-utils-algorithm.md` | `DatetimeOpt`, `Lunar` |
| 字符串编码、Base64、压缩、拼音 | `AGENTS-centit-utils-algorithm.md` | `StringBaseOpt`, `StringRegularOpt` |
| 数字转换、大写金额、四舍五入 | `AGENTS-centit-utils-algorithm.md` | `NumberBaseOpt` |
| 集合、树形排序、列表对比 | `AGENTS-centit-utils-algorithm.md` | `CollectionsOpt` |
| 反射、属性取值、表达式取值 | `AGENTS-centit-utils-algorithm.md` | `ReflectionOpt` |
| 布尔/字节/枚举转换 | `AGENTS-centit-utils-algorithm.md` | `BooleanBaseOpt`, `ByteBaseOpt`, `EnumBaseOpt` |
| 雪花ID、UUID、随机字符串 | `AGENTS-centit-utils-algorithm.md` | `Snowflake`, `UuidOpt` |
| 排列、组合 | `AGENTS-centit-utils-algorithm.md` | `Mathematics` |
| 通用加减乘除、对象比较、空值判断 | `AGENTS-centit-utils-algorithm.md` | `GeneralAlgorithm` |
| ZIP 压缩解压 | `AGENTS-centit-utils-algorithm.md` | `ZipCompressor` |
| 表达式计算、四则运算、公式 | `AGENTS-centit-utils-compiler.md` | `VariableFormula`, `EmbedFunc` |
| 模板引擎、变量替换 | `AGENTS-centit-utils-compiler.md` | `Pretreatment` |
| 词法分析、分词 | `AGENTS-centit-utils-compiler.md` | `Lexer` |
| 金额大写(元角分)、格式化日期 | `AGENTS-centit-utils-compiler.md` | `EmbedFuncUtils` |
| 异常处理、错误码 | `AGENTS-centit-utils-common.md` | `ObjectException` |
| 缓存、TTL、异步刷新 | `AGENTS-centit-utils-common.md` | `CachedObject`, `AsyncCachedObject`, `CachedMap` |
| 树节点数据结构 | `AGENTS-centit-utils-common.md` | `TreeNode` |
| JavaBean 反射元数据 | `AGENTS-centit-utils-common.md` | `JavaBeanMetaData` |
| 二元组 | `AGENTS-centit-utils-common.md` | `LeftRightPair` |
| JavaScript/Python 脚本执行 | `AGENTS-centit-utils-extend.md` | `JSRuntimeContext`, `PythonRuntimeContext` |
| 系统进程调用 | `AGENTS-centit-utils-extend.md` | `CallSystemProcess` |
| 文件读写、流操作 | `AGENTS-centit-utils-file.md` | `FileIOOpt` |
| 文件查找、复制、删除 | `AGENTS-centit-utils-file.md` | `FileSystemOpt` |
| CSV 读写 | `AGENTS-centit-utils-file.md` | `CsvFileIO` |
| 文件类型检测 | `AGENTS-centit-utils-file.md` | `FileType` |
| 文件 MD5 | `AGENTS-centit-utils-file.md` | `FileMD5Maker` |
| INI/Properties 配置读取 | `AGENTS-centit-utils-file.md` | `IniReader`, `PropertiesReader` |
| 验证码生成 | `AGENTS-centit-utils-image.md` | `CaptchaImageUtil` |
| 二维码生成 | `AGENTS-centit-utils-image.md` | `QrCodeGenerator`, `QrCodeConfig` |
| 图片缩略图、圆角、截图 | `AGENTS-centit-utils-image.md` | `ImageOpt` |
| SVG 安全处理 | `AGENTS-centit-utils-image.md` | `SvgUtils` |
| JSON 操作、属性读写 | `AGENTS-centit-utils-json.md` | `JSONOpt` |
| JSON 差异对比 | `AGENTS-centit-utils-json.md` | `JSONOpt.compareTwoJson()` → `JsonDifferent` |
| JSON 数据转换/映射 | `AGENTS-centit-utils-json.md` | `JSONTransformer` |
| fastjson 字段过滤 | `AGENTS-centit-utils-json.md` | `JsonExcludeFieldsFilters` |
| HTTP GET/POST 请求 | `AGENTS-centit-utils-network.md` | `HttpExecutor` |
| URL 编解码、参数解析 | `AGENTS-centit-utils-network.md` | `UrlOptUtils` |
| HTML 转义/反转义 | `AGENTS-centit-utils-network.md` | `HtmlFormUtils` |
| 本机 IP、MAC、PID | `AGENTS-centit-utils-network.md` | `HardWareUtils` |
| SOAP/WSDL | `AGENTS-centit-utils-network.md` | `SoapWsdlParser` |
| AES 加密解密 | `AGENTS-centit-utils-security.md` | `AESSecurityUtils` |
| RSA 加密签名 | `AGENTS-centit-utils-security.md` | `RSASecurityUtils` |
| 国密 SM2/SM3/SM4 | `AGENTS-centit-utils-security.md` | `SM2Util`, `SM3Util`, `SM4Util` |
| MD5/SHA1 哈希 | `AGENTS-centit-utils-security.md` | `Md5Encoder`, `Sha1Encoder` |
| 数据脱敏(手机号/身份证/邮箱) | `AGENTS-centit-utils-security.md` | `DesensitizeOptUtils` |
| XSS 过滤 | `AGENTS-centit-utils-security.md` | `HTMLFilter` |
| XML 解析、序列化 | `AGENTS-centit-utils-xml.md` | `XmlUtils`, `XMLObject` |
| XML Schema 验证 | `AGENTS-centit-utils-xml.md` | `XMLSchemaValidationUtil` |
| 防XXE攻击 | `AGENTS-centit-utils-xml.md` | `IgnoreDTDEntityResolver` |
| Elasticsearch 索引/搜索 | `AGENTS-centit-es-client.md` | `ESIndexer`, `ESSearcher` |
| ES 注解映射 | `AGENTS-centit-es-client.md` | `@ESType`, `@ESField` |
| ES 文档模型 | `AGENTS-centit-es-client.md` | `ObjectDocument`, `FileDocument` |
| ES 连接配置/工厂 | `AGENTS-centit-es-client.md` | `ElasticConfig`, `IndexerSearcherFactory` |
| 文档文本提取(PDF/Word等) | `AGENTS-centit-es-client.md` | `TikaTextExtractor` |
| Word/Excel/PPT 转 PDF | `AGENTS-centit-office-utils.md` | `OfficeToPdf` |
| Word/Excel/PPT 转 HTML | `AGENTS-centit-office-utils.md` | `OfficeToHtml` |
| PDF 合并、高亮关键词、转图片 | `AGENTS-centit-office-utils.md` | `PdfUtil` |
| PDF 水印、图片印章 | `AGENTS-centit-office-utils.md` | `Watermark4Pdf` |
| PDF 数字签名 | `AGENTS-centit-office-utils.md` | `SignatureUtil`, `SignatureInfo` |
| OFD 转换 | `AGENTS-centit-office-utils.md` | `OfdUtil` |
| 图片合成 PDF | `AGENTS-centit-office-utils.md` | `ImagesToPdf` |
| XLSX 转 XLS | `AGENTS-centit-office-utils.md` | `XlsxTransformXls` |
| Excel 导出 | `AGENTS-centit-report-utils.md` | `ExcelExportUtil` |
| Excel 导入 | `AGENTS-centit-report-utils.md` | `ExcelImportUtil` |
| 大文件 Excel 导入(>1万行) | `AGENTS-centit-report-utils.md` | `LargeExcelImportUtil` |
| Excel 类型检测 | `AGENTS-centit-report-utils.md` | `ExcelTypeEnum` |
| JXLS 模板 Excel 报表 | `AGENTS-centit-report-utils.md` | `ExcelReportUtil` |
| Word 报表(Freemarker模板) | `AGENTS-centit-report-utils.md` | `WordReportUtil` |
| 图表生成(柱状图/折线图/饼图) | `AGENTS-centit-report-utils.md` | `ChartImageUtils` |
| Docx 模板上下文 | `AGENTS-centit-report-utils.md` | `JsonDocxContext`, `SmartDocxContext` |
| 定时任务(创建/删除) | `AGENTS-centit-quartz-extend.md` | `QuartzJobUtils` |
| 自定义 Quartz Job | `AGENTS-centit-quartz-extend.md` | `AbstractQuartzJob` |
| 调用系统命令 | `AGENTS-centit-quartz-extend.md` | `CallProcessJob` |
| 调用 Spring Bean 方法 | `AGENTS-centit-quartz-extend.md` | `JavaBeanJob` |

---

## 文件加载索引

**按文件大小从小到大排列，agent 可评估加载代价：**

| 文件 | 大小 | 行数 | 包含的核心类 |
|------|------|------|-------------|
| `AGENTS-centit-utils-extend.md` | 0.6KB | 32 | JSRuntimeContext, PythonRuntimeContext, CallSystemProcess |
| `AGENTS-centit-utils-xml.md` | 1KB | 45 | XmlUtils, XMLObject, XMLSchemaValidationUtil |
| `AGENTS-centit-utils-image.md` | 1.8KB | 63 | CaptchaImageUtil, QrCodeGenerator, ImageOpt, SvgUtils |
| `AGENTS-centit-utils-network.md` | 2KB | 91 | HttpExecutor, UrlOptUtils, HtmlFormUtils, HardWareUtils |
| `AGENTS-centit-utils-json.md` | 2.4KB | 78 | JSONOpt, JSONTransformer, JsonDifferent, JsonExcludeFieldsFilters |
| `AGENTS-centit-utils-security.md` | 2.5KB | 121 | AESSecurityUtils, RSASecurityUtils, SM2Util, SM3Util, SM4Util, Md5Encoder, DesensitizeOptUtils |
| `AGENTS-centit-utils-file.md` | 3KB | 101 | FileIOOpt, FileSystemOpt, CsvFileIO, FileType, FileMD5Maker |
| `AGENTS-centit-utils-common.md` | 3.5KB | 148 | ObjectException, CachedObject, CachedMap, AsyncCachedObject, TreeNode, JavaBeanMetaData |
| `AGENTS-centit-utils-compiler.md` | 5KB | 157 | VariableFormula, EmbedFunc, Pretreatment, Lexer, EmbedFuncUtils |
| `AGENTS-centit-quartz-extend.md` | 6.6KB | 190 | QuartzJobUtils, AbstractQuartzJob, JavaBeanJob, CallProcessJob |
| `AGENTS-centit-es-client.md` | 11KB | 297 | ESIndexer, ESSearcher, ElasticConfig, ObjectDocument, FileDocument, TikaTextExtractor |
| `AGENTS-centit-office-utils.md` | 11KB | 289 | OfficeToPdf, OfficeToHtml, PdfUtil, Watermark4Pdf, SignatureUtil, OfdUtil |
| `AGENTS-centit-report-utils.md` | 14KB | 314 | ExcelExportUtil, ExcelImportUtil, LargeExcelImportUtil, WordReportUtil, ChartImageUtils |
| `AGENTS-centit-utils-algorithm.md` | 18KB | 427 | DatetimeOpt, StringBaseOpt, NumberBaseOpt, CollectionsOpt, ReflectionOpt, Snowflake, UuidOpt, Lunar, GeneralAlgorithm |

**不包含类的索引文件**（仅包含子文件链接，无需加载除非查看结构）：

| 文件 | 用途 |
|------|------|
| `AGENTS-centit-utils.md` | centit-utils 的 10 个子文件链接索引 |

---

## 模块依赖关系

```
centit-utils (核心，无外部模块依赖)
    ↑
    ├── centit-es-client
    ├── centit-office-utils
    ├── centit-report-utils
    └── centit-quartz-extend
```

所有扩展模块仅依赖 centit-utils，彼此之间无依赖。

---

## 开发约定

| 约定 | 说明 |
|------|------|
| 工具类模式 | 抽象类 + 静态方法（`abstract class XxxOpt`），不可实例化 |
| 命名规范 | 工具类以 `Opt` 或 `Utils` 结尾 |
| 统一异常 | `ObjectException`，携带异常码和对象数据 |
| 日志 | SLF4J |
| JSON | fastjson2（`JSONObject`, `JSONArray`） |
