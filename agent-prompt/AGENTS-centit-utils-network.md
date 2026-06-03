# centit-utils / network 子包

> 包路径: `com.centit.support.network`
> 网络相关工具，基于 Apache HttpClient 5。

---

## HttpExecutor

HTTP 请求执行器（基于 Apache HttpClient）。

### 创建请求

| 方法 | 描述 |
|------|------|
| `static HttpExecutor simpleGet(String url)` | 创建 GET 请求 |
| `static HttpExecutor simplePost(String url)` | 创建 POST 请求 |

### 设置参数

| 方法 | 描述 |
|------|------|
| `HttpExecutor addParameter(String name, Object value)` | 添加请求参数 |
| `HttpExecutor setJsonBody(Object body)` | 设置 JSON body |

### 执行请求

| 方法 | 描述 |
|------|------|
| `String execute()` | 执行请求返回字符串 |
| `<T> T execute(Class<T> clazz)` | 执行并反序列化为指定类型 |

---

## UrlOptUtils

URL 操作工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String urlEncode(String url)` | URL 编码 |
| `static String urlDecode(String url)` | URL 解码 |
| `static String objectToUrlString(Object obj)` | 对象转 URL 参数字符串 |
| `static Map<String,String> queryStringToMap(String queryString)` | URL 参数字符串转 Map |

---

## HtmlFormUtils

HTML 表单模拟提交（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String escapeHtml(String text)` | HTML 转义 |
| `static String unescapeHtml(String text)` | HTML 反转义 |

---

## HardWareUtils

硬件信息工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static InetAddress getLocalhost()` | 获取本机 IP |
| `static byte[] getHardwareAddress(InetAddress ia)` | 获取 MAC 地址 |
| `static long getPid()` | 获取当前进程 PID |

---

## HttpExecutorContext

HTTP 执行器上下文配置（超时、代理、SSL 等）。

---

## SoapWsdlParser

SOAP WSDL 解析器。解析 WSDL 文件获取服务信息。

---

## InputStreamResponseHandler

HTTP 响应处理器，返回 InputStream。

---

## StringResponseHandler

HTTP 响应处理器，返回 String。
