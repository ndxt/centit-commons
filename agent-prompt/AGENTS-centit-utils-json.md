# centit-utils / json 子包

> 包路径: `com.centit.support.json`
> JSON 处理工具，基于 fastjson2。

---

## JSONOpt

JSON 对象操作工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static void fastjsonGlobalConfig()` | 全局 fastjson 配置（日期格式等） |
| `static JsonDifferent compareTwoJson(Object jsonA, Object jsonB, String... arrayKeys)` | JSON 差异对比 |
| `static Object getAttribute(JSONObject jsonObj, String expression)` | 表达式获取 JSON 属性 |
| `static void setAttribute(JSONObject jsonObj, String expression, Object value)` | 表达式设置 JSON 属性 |

---

## JSONTransformer

JSON 数据转换器，基于模板将源 JSON 转换为目标 JSON。

| 方法 | 描述 |
|------|------|
| `static Object transformer(Object templateObj, JSONTransformDataSupport dataSupport)` | 模板转换，支持 `@常量`、`#循环`、`=表达式`、`引用` |

---

## JSONTransformDataSupport (接口)

JSON 转换数据支持接口。

| 方法 | 描述 |
|------|------|
| `Object attainExpressionValue(String labelName)` | 计算表达式值 |
| `String mapTemplateString(String templateString)` | 模板字符串替换 |

---

## DefaultJSONTransformDataSupport

默认 JSON 转换数据实现（实现 JSONTransformDataSupport）。支持嵌套循环（`__row_index`、`__row_count`、`..` 上级引用）。

---

## JsonDifferent

JSON 差异结果。包含路径、差异类型、新旧数据。

| 属性 | 描述 |
|------|------|
| 差异类型 | `+`(新增)、`deleted`(删除)、`*`(更新)、`update`(更新) |
| 路径 | JSON 路径表达式 |
| 旧数据/新数据 | 变更前后的值 |

---

## JsonExcludeFieldsFilters

fastjson 属性排除过滤器。

| 方法 | 描述 |
|------|------|
| `void addExclude(Class<?> clazz, String... excludeFields)` | 添加排除字段 |
| `static JsonExcludeFieldsFilters create(Class<?> clazz, String... excludeFields)` | 快速创建过滤器 |

---

## 序列化配置类 (`com.centit.support.json.config`)

| 类 | 描述 |
|-----|------|
| `UtilDateSerializer` / `UtilDateDeserializer` | java.util.Date 序列化/反序列化 |
| `SqlDateSerializer` / `SqlDateDeserializer` | java.sql.Date 序列化/反序列化 |
| `SqlTimestampSerializer` / `SqlTimestampDeserializer` | Timestamp 序列化/反序列化 |
| `LobSerializer` | BLOB/CLOB 序列化 |
