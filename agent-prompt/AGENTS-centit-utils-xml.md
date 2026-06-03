# centit-utils / xml 子包

> 包路径: `com.centit.support.xml`
> XML 处理工具。

---

## XmlUtils

XML 通用工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String xmlToString(Node node)` | XML Node 转字符串 |
| `static Document stringToXml(String xmlString)` | 字符串解析为 XML Document |
| `static String getNodeText(Node node)` | 获取节点文本 |

---

## XMLObject

XML 与对象互转工具。

| 方法 | 描述 |
|------|------|
| `static Object xmlStringToObject(String xmlString)` | XML 字符串转对象 |
| `static String objectToXMLString(Object obj)` | 对象转 XML 字符串 |

---

## XMLSchemaValidationUtil

XML Schema 验证工具。根据 XSD Schema 验证 XML 文档的合法性。

---

## IgnoreDTDEntityResolver

忽略 DTD 的 EntityResolver。防止 XXE（XML External Entity）攻击，在解析 XML 时忽略外部 DTD 引用。

---

## XMLErrorHandler

XML 错误处理器。收集 XML 解析过程中的错误、警告信息。
