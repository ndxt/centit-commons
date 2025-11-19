# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建和测试

**构建项目:**
```bash
mvn clean compile
```

**运行测试:**
```bash
mvn test
# 运行单个测试类
mvn test -Dtest=ESTest
# 运行单个测试方法
mvn test -Dtest=ESTest#testQuery
```

**打包:**
```bash
mvn clean package
```

## 项目架构

这是一个基于 Elasticsearch 的搜索客户端库，使用 Elasticsearch 7.17.21 版本。

### 核心组件架构

**服务层 (service/):**
- `ESServerConfig`: ES服务器配置类，包含连接参数
- `Indexer`/`ESIndexer`: 文档索引接口及实现，负责文档的增删改
- `Searcher`/`ESSearcher`: 搜索接口及实现，提供多种搜索模式
- `IndexerSearcherFactory`: 工厂类，创建和管理索引器、搜索器实例
- `PooledRestClientFactory`: ES客户端连接池工厂

**文档层 (document/):**
- `ESDocument`: 基础文档接口
- `ObjectDocument`: 对象文档实现
- `FileDocument`: 文件文档实现
- `DocumentUtils`: 文档工具类

**工具层 (utils/):**
- `TikaTextExtractor`: 使用 Apache Tika 提取文档文本
- `ObjectTextExtractor`: 对象文本提取器
- `ImagePdfTextExtractor`: 图片和PDF文本提取

**注解 (annotation/):**
- `@ESField`: ES字段映射注解
- `@ESType`: ES类型映射注解

### 测试配置

测试配置文件位于 `src/test/resources/system.properties`，包含：
- Elasticsearch 服务器连接参数
- OCR 服务配置
- 其他系统配置

主要测试类：
- `ESTest`: 核心功能测试
- `DocumentSearchTest`: 文档搜索测试
- `TestTextExtract`: 文本提取测试

## 开发注意事项

1. **连接配置**: ES服务器配置通过 `ESServerConfig` 管理，支持集群模式
2. **连接池**: 使用 `PooledRestClientFactory` 管理ES客户端连接池
3. **文档映射**: 使用注解定义ES字段映射关系
4. **文本提取**: 支持多种文档格式的文本提取（PDF、Word、图片等）
5. **搜索功能**: 支持按业务、用户、机构等多维度搜索
6. **高亮显示**: 搜索结果支持高亮显示匹配内容

## 主要依赖

- Elasticsearch Java Client 7.17.29
- Apache Tika (文档解析)
- Apache Commons Pool2 (连接池)
- Jackson (JSON处理)
- centit-utils (内部工具库)

## 迁移说明

项目已从 Elasticsearch Rest High Level Client 7.17.21 迁移至 Elasticsearch Java Client 7.17.29：

**主要变更：**
1. **客户端类型**：`RestHighLevelClient` → `ElasticsearchClient`
2. **连接池**：连接池现在管理 `ElasticsearchClient` 实例
3. **API风格**：从传统API迁移到基于Builder模式的Lambda流式API
4. **查询构建**：使用新的查询DSL API
5. **响应处理**：使用新的响应类型和数据访问方式

**向后兼容性：**
- 所有公共接口保持不变
- 现有业务代码无需修改
- 配置文件格式保持一致