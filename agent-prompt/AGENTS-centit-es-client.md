# centit-es-client 模块

> Maven 坐标: `com.centit.support:centit-es-client:JDK21-SNAPSHOT`
> 根包: `com.centit.search`
> 基于 Elasticsearch Java Client 7.17.29 的全文搜索客户端库。

---

## 架构总览

```
com.centit.search
├── annotation/          -- 注解层：ES 字段和类型映射
│   ├── ESType           -- 类注解：定义索引名、分片、副本
│   └── ESField          -- 字段注解：定义字段类型、检索、高亮、分词器
├── document/            -- 文档层：可索引文档的数据模型
│   ├── ESDocument       -- 文档接口
│   ├── ObjectDocument   -- 业务对象文档（索引名: objects）
│   ├── FileDocument     -- 文件文档（索引名: files）
│   └── DocumentUtils    -- 注解解析：生成索引名和 mapping JSON
├── service/             -- 服务层：核心索引和搜索功能
│   ├── Indexer          -- 索引器接口
│   ├── Searcher         -- 搜索器接口
│   ├── ElasticConfig    -- ES 连接配置
│   ├── ElasticsearchClientFactory    -- ES 客户端工厂
│   ├── IndexerSearcherFactory        -- 核心：缓存管理 Indexer/Searcher
│   └── Impl/
│       ├── ESIndexer    -- Indexer 实现
│       └── ESSearcher   -- Searcher 实现
└── utils/               -- 工具层：文本提取和处理
    ├── ObjectTextExtractor  -- Java 对象递归文本提取
    ├── TikaTextExtractor    -- Tika 文档文本提取
    └── IndexContentUtil     -- 索引内容智能截断
```

---

## 1. annotation 包 — 注解层

### ESType (类注解)

标记 Java 类对应的 Elasticsearch 索引类型。

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `indexName` | String | (必填) | ES 索引名称 |
| `replicas` | int | 1 | 副本数 |
| `shards` | int | 1 | 分片数 |

### ESField (字段注解)

标记 Java Bean 属性对应的 ES 字段映射。

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `type` | String | "" | ES 字段类型 (keyword/text/date 等) |
| `index` | boolean | true | 是否可检索 |
| `store` | boolean | true | 是否存储 |
| `query` | boolean | false | true=全文检索字段，false=精确匹配 |
| `highlight` | boolean | false | 是否高亮 |
| `analyzer` | String | "" | 分词器 |
| `indexAnalyzer` | String | "" | 索引分词器 |
| `searchAnalyzer` | String | "" | 搜索分词器 |

---

## 2. document 包 — 文档层

### ESDocument (接口)

所有可索引文档的基础接口。

| 方法 | 描述 |
|------|------|
| `String obtainDocumentId()` | 获取文档唯一标识 ID |
| `JSONObject toJSONObject()` | 将文档转换为 JSONObject |

### ObjectDocument

通用业务对象文档（索引名: `objects`，5分片，`ik_smart` 分词器）。

通过 `osId`、`optId`、`optTag` 三元组唯一定位业务对象。

| 字段 | ES类型 | 描述 |
|------|--------|------|
| `osId` | keyword | 所属系统 |
| `optId` | keyword | 所属业务 |
| `optTag` | keyword | 关联业务对象主键 |
| `optMethod` | keyword | 关联方法 |
| `optUrl` | text | 文档反向关联 URL |
| `userCode` | keyword | 所属人员 |
| `unitCode` | keyword | 所属机构 |
| `title` | text (query, highlight, ik_smart) | 对象标题 |
| `content` | text (query, highlight, ik_smart) | 文档内容 |
| `createTime` | date | 创建时间 |

| 方法 | 描述 |
|------|------|
| `obtainDocumentId()` | 生成 ID: `osId:optId:optTag`，超36字符用 MD5 |
| `contentObject(Object obj)` | 从任意 Java 对象提取文本设置为 content |

### FileDocument

文件类型文档（索引名: `files`，5分片，`ik_smart` 分词器）。

| 字段 | ES类型 | 描述 |
|------|--------|------|
| `osId/optId/optTag/optMethod/optUrl/userCode/unitCode` | 同 ObjectDocument | |
| `fileName` | text (query, highlight) | 文档名称 |
| `fileSummary` | text (query, highlight) | 文档摘要 |
| `fileId` | keyword | 文档 ID |
| `fileMD5` | keyword | 文档 MD5 |
| `content` | text (query, highlight) | 文档内容 |
| `keywords` | text[] (query) | 文档关键字 |
| `createTime` | date | 创建时间 |

### DocumentUtils

文档映射工具（抽象类），通过反射读取注解生成 ES mapping。

| 方法 | 描述 |
|------|------|
| `static String obtainDocumentIndexName(Class<?> objType)` | 从 @ESType 获取索引名 |
| `static JSONObject obtainDocumentMapping(Class<?> objType)` | 生成 ES mapping JSON |

---

## 3. service 包 — 服务层

### Indexer (接口)

文档索引器接口。

| 方法 | 描述 |
|------|------|
| `String saveNewDocument(ESDocument document)` | 新建文档，返回文档 ID |
| `boolean deleteDocument(ESDocument document)` | 删除文档（按对象） |
| `boolean deleteDocument(String docId)` | 删除文档（按 ID） |
| `int updateDocument(ESDocument document)` | 更新文档 |
| `int updateDocument(String docId, ESDocument document)` | 按 ID 更新文档 |
| `String mergeDocument(ESDocument document)` | 合并文档（upsert），返回 ID |

### Searcher (接口)

文档搜索器接口。

| 常量 | 值 | 描述 |
|------|-----|------|
| `SEARCH_FRAGMENT_SIZE` | 200 | 高亮片段大小 |
| `SEARCH_FRAGMENT_NUM` | 2 | 高亮片段数量 |

| 方法 | 返回类型 | 描述 |
|------|----------|------|
| `search(String queryWord, int pageNo, int pageSize)` | `Pair<Long, List<Map>>` | 全局关键字搜索 |
| `search(Map fieldFilter, String queryWord, int pageNo, int pageSize)` | `Pair<Long, List<Map>>` | 带字段过滤搜索 |
| `searchOpt(String optId, String queryWord, int pageNo, int pageSize)` | `Pair<Long, List<Map>>` | 按业务ID过滤搜索 |
| `searchOwner(String owner, String queryWord, int pageNo, int pageSize)` | `Pair<Long, List<Map>>` | 按人员搜索 |
| `searchOwner(String owner, String optId, String queryWord, int pageNo, int pageSize)` | `Pair<Long, List<Map>>` | 按人员+业务搜索 |
| `searchUnits(String[] units, String queryWord, int pageNo, int pageSize)` | `Pair<Long, List<Map>>` | 按机构搜索 |
| `searchUnits(String[] units, String optId, String queryWord, int pageNo, int pageSize)` | `Pair<Long, List<Map>>` | 按机构+业务搜索 |
| `getDocumentById(String idFieldName, String docId)` | `JSONObject` | 按 ID 精确获取文档 |

### ElasticConfig

ES 连接配置类（@Data）。

| 字段 | 描述 |
|------|------|
| `serverHostIp` | ES 主机 IP |
| `serverHostPort` | ES 端口 |
| `clusterName` | 集群名称 |
| `username` | 认证用户名（自动解密） |
| `password` | 认证密码（自动解密） |
| `usingSSL` | 是否使用 SSL |
| `minScore` | 最小相关度评分阈值 |

### ElasticsearchClientFactory

ES 客户端工厂（抽象类）。

| 方法 | 描述 |
|------|------|
| `static ElasticsearchClient createClient(ElasticConfig config)` | 创建 ES 客户端（最大连接500，每路由100） |
| `static void closeClient(ElasticsearchClient client)` | 关闭 ES 客户端 |

### IndexerSearcherFactory

核心工厂类（抽象类），缓存管理 Indexer/Searcher 实例。

| 方法 | 描述 |
|------|------|
| `static ESIndexer obtainIndexer(String indexName)` | 获取已缓存的 Indexer |
| `static ESIndexer obtainIndexer(ElasticConfig config, Class<?> objType)` | 创建/获取 Indexer |
| `static ESSearcher obtainSearcher(String indexName)` | 获取已缓存的 Searcher |
| `static ESSearcher obtainSearcher(ElasticConfig config, Class<?> objType)` | 创建/获取 Searcher |
| `static ElasticConfig loadESServerConfigFormProperties(Properties properties)` | 从 Properties 加载配置 |
| `static ElasticConfig loadESServerConfigFormProperties(String propertiesFile)` | 从文件加载配置 |

配置属性映射：

| 属性键 | 对应字段 |
|--------|----------|
| `elasticsearch.server.ip` | serverHostIp |
| `elasticsearch.server.port` | serverHostPort |
| `elasticsearch.server.cluster` | clusterName |
| `elasticsearch.server.username` | username |
| `elasticsearch.server.password` | password |
| `elasticsearch.filter.minScore` | minScore (默认0.5) |

---

## 3.1 service/Impl 包 — 实现层

### ESIndexer

Indexer 接口核心实现。使用 Lambda/Builder 风格 API。首次操作自动创建索引。

| 方法 | 描述 |
|------|------|
| `fetchClient()` | 获取底层 ES 客户端 |
| `releaseClient()` | 释放 ES 客户端 |
| `saveNewDocument(ESDocument document)` | 新建文档（自动建索引） |
| `deleteDocument(String docId)` | 按 ID 删除 |
| `updateDocument(String docId, ESDocument document)` | 局部更新 |
| `mergeDocument(ESDocument document)` | Upsert：存在更新，不存在新建 |

### ESSearcher

Searcher 接口核心实现。自动识别查询字段、高亮字段、日期字段。

#### 过滤条件后缀语法

| 后缀 | 含义 |
|------|------|
| `_gt` | 大于 |
| `_ge` | 大于等于 |
| `_lt` | 小于 |
| `_le` | 小于等于 |
| `_ne` | 不等于 |
| `_ni` | 不在列表中 |
| `_lk` | like 模糊匹配 |
| `_in` | 在列表中 |
| `_eq` | 等于（默认） |

#### 核心搜索方法

| 方法 | 描述 |
|------|------|
| `esSearch(Query query, List<SortOptions> sortOptions, String[] includes, String[] excludes, int pageNo, int pageSize)` | 核心搜索：分页+排序+高亮+字段过滤 |
| `static String buildWildcardQuery(String sMatch)` | 用户输入转 ES wildcard 查询 |
| `static List<SortOptions> mapSortBuilder(Map<String, Object> filterMap)` | 解析排序选项 |

---

## 4. utils 包 — 工具层

### ObjectTextExtractor

通用对象文本提取器（抽象类）。递归遍历任意 Java 对象提取文本。

| 方法 | 描述 |
|------|------|
| `static TextExtractContent createContent()` | 创建提取上下文配置 |
| `static String extractText(Object object)` | 提取对象所有文本 |
| `static String extractText(Object object, boolean omitNumber, boolean omitSymbol)` | 可选省略数字/符号 |
| `static String extractText(Object object, TextExtractContent content)` | 自定义配置提取 |

**TextExtractContent 配置**:
- `includePropertyName(boolean)` — 是否包含属性名
- `includeProperties(Class, String[])` — 指定提取属性
- `excludeProperties(Class, String[])` — 排除属性
- `setOmitNumber(boolean)` — 省略数字
- `setOmitSymbol(boolean)` — 省略分隔符

### TikaTextExtractor

基于 Apache Tika 的文档文本提取器（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String extractInputStreamText(InputStream inputStream)` | 从流提取文本（自动检测类型） |
| `static String extractFileText(File file)` | 从文件提取文本 |
| `static String extractFileText(String filePath)` | 从路径提取文本 |
| `static String extractUrlText(String urlPath)` | 从 URL 提取文本 |

### IndexContentUtil

索引内容处理工具（抽象类）。

| 常量 | 值 | 描述 |
|------|-----|------|
| `MAX_CONTENT_LENGTH` | 32000 | 索引内容最大长度 |

| 方法 | 描述 |
|------|------|
| `static String truncateContent(String content)` | 智能截断内容（优先保留含关键词的句子） |
| `static void truncateIndexObject(Map<String, Object> indexedObject)` | 遍历 Map 截断所有字符串值 |
