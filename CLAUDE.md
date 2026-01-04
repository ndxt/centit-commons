# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建和测试

**构建整个项目：**
```bash
mvn clean compile
```

**运行测试：**
```bash
mvn test
# 运行特定模块的测试
mvn test -pl centit-utils
# 运行单个测试类
mvn test -Dtest=TestDateTimeOpt
```

**打包：**
```bash
mvn clean package
```

**安装到本地仓库：**
```bash
mvn clean install
```

## 项目架构

这是一个 Java 多模块工具类库项目，作为 apache-commons 的补充，包含常用的工具类和扩展功能。

### 模块结构

**centit-utils** - 核心工具类库
- **algorithm/**: 算法相关工具类
  - `DatetimeOpt`: 日期时间操作工具，包括中国农历算法
  - `StringBaseOpt`: 字符串操作工具，支持中文大写、金额大写等
  - `NumberBaseOpt`: 数字操作工具
  - `CollectionsOpt`: 集合操作，支持树形排序、列表对比等
  - `ReflectionOpt`: 反射操作工具
  - `UuidOpt`: UUID生成工具
  - `Snowflake`: 分布式ID生成器

- **compiler/**: 简易编译系统
  - `Lexer`: 词法分析器
  - `VariableFormula`: 表达式运算程序，支持四则运算、逻辑运算和内置函数
  - `EmbedFunc`: 内置函数支持

- **security/**: 安全相关工具
  - `AESSecurityUtils`: AES加密工具
  - `RSASecurityUtils`: RSA加密工具
  - `SM2Util`, `SM3Util`, `SM4Util`: 国密算法工具
  - `Md5Encoder`, `Sha1Encoder`: 摘要算法

- **network/**: 网络相关工具
  - `HttpExecutor`: HTTP请求执行器
  - `HtmlFormUtils`: HTML表单模拟提交
  - `UrlOptUtils`: URL操作工具

- **json/**: JSON处理工具
  - `JSONOpt`: JSON操作工具类
  - `JSONTransformer`: JSON数据转换器
  - `JsonDifferent`: JSON对比工具

- **file/**: 文件操作工具
  - `FileIOOpt`: 文件IO操作
  - `CsvFileIO`: CSV文件读写
  - `FileSystemOpt`: 文件系统操作

- **image/**: 图像处理工具
  - `CaptchaImageUtil`: 验证码生成
  - `QrCodeGenerator`: 二维码生成
  - `ImageOpt`: 图像操作工具

**centit-es-client** - Elasticsearch 客户端库
- 基于 Elasticsearch Java Client 7.17.29
- 提供文档索引、搜索功能
- 支持多种文档格式的文本提取

**centit-office-utils** - Office文档处理工具
- Office文档转换（PDF、HTML等）
- PDF签名、水印功能
- 图片处理工具

**centit-report-utils** - 报表工具
- `ExcelExportUtil`: Excel导出，支持模板和大数据量流式导出
- `ExcelImportUtil`: Excel导入，支持类型转换和合并单元格处理  
- `LargeExcelImportUtil`: 大文件Excel流式导入处理
- `WordReportUtil`: Word报表生成，支持Freemarker模板和PDF转换
- `ChartImageUtils`: 图表生成（柱状图、折线图、饼图）

**centit-quartz-extend** - Quartz扩展
- 任务调度扩展功能
- 支持Java Bean任务、进程调用任务

### 技术特点

1. **多模块设计**: 按功能域划分模块，便于按需引入
2. **工具类设计**: 静态方法为主，便于直接调用
3. **Maven管理**: 统一版本管理，支持独立发布
4. **JDK 17+**: 最低支持JDK 17，目标版本JDK 21
5. **测试完善**: 每个模块都有对应的测试类

### 开发约定

1. **包命名**: 统一使用 `com.centit.support.*` 前缀
2. **工具类命名**: 以 `Opt` 或 `Utils` 结尾
3. **静态方法**: 工具方法优先使用静态方法
4. **异常处理**: 使用 `ObjectException` 作为统一异常类型
5. **日志**: 使用 slf4j 作为日志接口

## 依赖管理

项目使用父POM管理依赖版本：
- 父项目：`com.centit.framework:centit-framework-dependencies`
- 版本：JDK21-SNAPSHOT
- 最低JDK要求：17

主要外部依赖：
- Apache Commons系列
- Jackson (JSON处理)
- HttpClient (网络请求)
- Tika (文档解析)
- Elasticsearch Java Client
- Apache POI (Office文档)

## 测试和验证

大部分工具类都有对应的测试类，位于各模块的 `src/test/java` 目录下。测试类命名通常以 `Test` 开头，如：
- `TestDateTimeOpt`: 日期时间工具测试
- `TestSecurity`: 安全工具测试
- `TestJsonObject`: JSON工具测试

**运行特定模块测试：**
```bash
# 运行各模块测试
mvn test -pl centit-utils
mvn test -pl centit-report-utils
mvn test -pl centit-office-utils
mvn test -pl centit-es-client
mvn test -pl centit-quartz-extend

# 运行具体测试类示例
mvn test -Dtest=TestDateTimeOpt
mvn test -pl centit-report-utils -Dtest=TestExcelExport
mvn test -pl centit-office-utils -Dtest=TestPdfUtils
```

## 常用开发模式

**Excel处理：**
- 小文件使用 `ExcelImportUtil`/`ExcelExportUtil`
- 大文件（>1万行）使用 `LargeExcelImportUtil` 流式处理
- 复杂模板使用 `ExcelReportUtil` + JXLS模板

**文档生成：**
- Word报表使用 `WordReportUtil` + Freemarker模板
- 图表生成使用 `ChartImageUtils` 配合 XChart

**文件类型检测：**
- 使用 `ExcelTypeEnum.getExcelType()` 自动检测Excel版本

## 版本信息

- **目标 JDK 版本**: JDK 21
- **最低 JDK 要求**: JDK 17
- **当前版本**: JDK21-SNAPSHOT
- **父 POM**: `com.centit.framework:centit-framework-dependencies`