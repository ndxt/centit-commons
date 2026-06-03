# centit-utils / common 子包

> 包路径: `com.centit.support.common`
> 通用数据结构和工具类。

---

## ObjectException

统一异常类，继承 RuntimeException，携带异常码和对象数据。

### 异常码常量

| 常量 | 值 | 描述 |
|------|-----|------|
| `UNKNOWN_EXCEPTION` | 601 | 未知异常 |
| `DATABASE_SQL_EXCEPTION` | 622 | 数据库SQL异常 |
| `LOGICAL_RULE_ERROR` | 609 | 逻辑规则错误 |
| `DATA_NOT_FOUND_EXCEPTION` | 404 | 数据未找到 |
| `DATA_ALREADY_EXIST_EXCEPTION` | 405 | 数据已存在 |
| `NO_PERMISSION_EXCEPTION` | 403 | 无权限异常 |
| `PARAMETER_FORMAT_ERROR` | 602 | 参数格式错误 |
| `PARAMETER_REQUIREMENT_ERROR` | 603 | 参数要求错误 |

### 方法

| 方法 | 描述 |
|------|------|
| 多种构造函数 | `ObjectException(String msg)`, `ObjectException(int code, String msg)`, `ObjectException(Object obj, String msg)` 等 |
| `int getExceptionCode()` | 获取异常码 |
| `Object getObjectData()` | 获取关联对象 |
| `String toJSONString()` | 转为 JSON |
| `static String extortExceptionMessage(Throwable ex)` | 提取异常堆栈信息 |

---

## CachedObject<T>

带过期时间的对象缓存，支持派生缓存。

| 方法/属性 | 描述 |
|-----------|------|
| 构造: `CachedObject(Supplier<T> refresher, long freshPeriod)` | 指定数据刷新器和过期周期（毫秒） |
| `T getCachedTarget()` | 获取缓存对象（过期自动刷新） |
| `T getFreshTarget()` | 强制刷新并获取 |
| `void setFreshData(T freshData)` | 手动设置缓存数据 |

---

## CachedMap<K,V>

带 TTL 的 Map 缓存。

---

## AbstractCachedObject<T>

缓存基类，支持派生缓存级联刷新。

---

## AsyncCachedObject<T>

异步刷新缓存。当缓存过期时，后台线程异步刷新数据，调用者仍可获取旧数据（避免阻塞）。

---

## DerivativeCachedMap<K,V>

派生缓存 Map，当主缓存刷新时自动级联刷新。

---

## ICachedObject (接口)

缓存对象接口，定义默认刷新周期。

---

## TreeNode<T>

泛型树节点，支持构建树形结构。

| 属性 | 描述 |
|------|------|
| `T data` | 节点数据 |
| `List<TreeNode<T>> children` | 子节点列表 |

---

## JavaBeanMetaData

JavaBean 元数据，通过反射获取 Bean 的字段和 getter/setter 方法。

| 方法 | 描述 |
|------|------|
| `static JavaBeanMetaData createBeanMetaDataFromType(Class<?> javaType)` | 创建 Bean 元数据（缓存） |
| `Object createBeanObject()` | 创建实例 |
| `Object createBeanObjectFromMap(Map<String,Object> properties)` | 从 Map 创建 Bean |
| `void setObjectFieldValue(Object object, String fieldName, Object newValue)` | 设置字段值 |
| `Object getObjectFieldValue(Object object, String fieldName)` | 获取字段值 |

---

## JavaBeanField

JavaBean 字段元数据，描述单个字段的 getter/setter 方法信息。

---

## LeftRightPair<L,R>

二元组，泛型数据结构。

| 属性 | 描述 |
|------|------|
| `L left` | 左值 |
| `R right` | 右值 |

---

## DoubleAspect<T>

双方面回调，支持设置两个回调处理器。

---

## ListAppendMap<K,V>

值自动追加为 List 的 Map。调用 `put(key, value)` 时，如果 key 已存在，将值追加到现有 List 中而非覆盖。

---

## ParamName (注解)

方法参数名注解，用于在运行时保留参数名信息。

---

## TimeInterval

时间间隔数据结构。

---

## DateTimeSpan

日期时间跨度数据结构。
