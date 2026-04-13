# centit-utils 模块概览

> Maven 坐标: `com.centit.support:centit-utils:JDK21-SNAPSHOT`
> 根包: `com.centit.support`
> 本文件为 centit-utils 模块的索引文件，详细 API 文档按子包拆分在独立文件中。

## 模块简介

centit-utils 是 centit-commons 项目的核心基础工具库，作为 apache-commons 的补充。包含 10 个子包、约 80 个类/接口，覆盖企业级 Java 开发中常用的工具需求。所有工具类均为抽象类 + 静态方法模式，不可实例化。

## 子包索引

| 子包 | 文件 | 类数量 | 核心功能 |
|------|------|--------|----------|
| `algorithm` | [AGENTS-centit-utils-algorithm.md](AGENTS-centit-utils-algorithm.md) | ~16 | 日期时间、字符串、数字、集合、反射、雪花ID、UUID、农历、排列组合、通用算法、ZIP |
| `compiler` | [AGENTS-centit-utils-compiler.md](AGENTS-centit-utils-compiler.md) | ~12 | 词法分析器、表达式引擎、80+内置函数、模板引擎 |
| `common` | [AGENTS-centit-utils-common.md](AGENTS-centit-utils-common.md) | ~15 | 统一异常、缓存体系、树节点、Bean元数据、数据结构 |
| `extend` | [AGENTS-centit-utils-extend.md](AGENTS-centit-utils-extend.md) | ~4 | JS/Python运行时上下文、系统进程调用 |
| `file` | [AGENTS-centit-utils-file.md](AGENTS-centit-utils-file.md) | ~8 | 文件IO、文件系统、CSV、文件类型检测、MD5校验、配置读取 |
| `image` | [AGENTS-centit-utils-image.md](AGENTS-centit-utils-image.md) | ~5 | 验证码、二维码、图像操作、SVG工具 |
| `json` | [AGENTS-centit-utils-json.md](AGENTS-centit-utils-json.md) | ~9 | JSON操作、数据转换器、差异对比、序列化配置 |
| `network` | [AGENTS-centit-utils-network.md](AGENTS-centit-utils-network.md) | ~7 | HTTP请求、URL操作、HTML表单、硬件信息、SOAP |
| `security` | [AGENTS-centit-utils-security.md](AGENTS-centit-utils-security.md) | ~11 | AES/RSA/国密加密、摘要算法、数据脱敏、HTML过滤 |
| `xml` | [AGENTS-centit-utils-xml.md](AGENTS-centit-utils-xml.md) | ~5 | XML解析、对象互转、Schema验证、防XXE |

## 主要外部依赖

| 依赖 | 用途 |
|------|------|
| Apache Commons (lang3, io, codec, collections4) | 基础工具扩展 |
| fastjson2 | JSON 序列化/反序列化 |
| Apache HttpClient 5 | HTTP 请求 |
| Bouncy Castle | 国密算法、加密 |
| ZXing | 二维码生成 |
| SLF4J | 日志接口 |
| Apache Tika (可选) | 文档文本提取 |
