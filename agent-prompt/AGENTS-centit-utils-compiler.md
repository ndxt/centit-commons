# centit-utils / compiler 子包

> 包路径: `com.centit.support.compiler`
> 简易编译系统，包含词法分析器、表达式引擎、模板引擎。

---

## Lexer

词法分析器，支持 Java/SQL/默认三种注释风格。

| 方法/属性 | 描述 |
|-----------|------|
| 构造: `Lexer(String sFormula)` / `Lexer(String sFormula, int langType)` | langType: 0=DEFAULT, 1=JAVA, 2=SQL |
| `String getAWord()` | 获取下一个单词（自动跳过注释） |
| `String getARawWord()` | 获取下一个原始单词（不过滤注释） |
| `void writeBackAWord(String preWord)` | 回退一个单词 |
| `boolean seekToRightBracket()` | 跳到右括号 |
| `static boolean isConstValue(CharSequence seq)` | 判断是否为常量值 |
| `static Object toConstValue(String seq)` | 将常量字符串转为实际值 |
| `static boolean isLabel(CharSequence seq)` | 判断是否为合法标识符 |
| `static List<String> splitByWord(String sourceString, String splitWord)` | 按单词分割（忽略括号/引号内） |
| `int findWord(String aword, boolean caseSensitives, boolean skipAnnotate)` | 查找单词位置 |

---

## VariableFormula

表达式运算引擎，支持四则运算、逻辑运算、内置函数、变量引用。

### 核心方法

| 方法 | 描述 |
|------|------|
| `static Object calculate(String szExpress)` | 计算纯表达式 |
| `static Object calculate(String szExpress, VariableTranslate varTrans)` | 带变量翻译计算 |
| `static Object calculate(String szExpress, Object varMap)` | 用 Map/Pojo 作为变量上下文计算 |
| `static int checkFormula(String szExpress)` | 检查表达式语法（0=通过） |
| `static Set<String> attainFormulaVariable(String szExpress)` | 提取表达式中的变量名 |
| `void setFormula(String formula)` | 设置表达式 |
| `void setTrans(VariableTranslate trans)` | 设置变量翻译器 |
| `void addExtendFunc(String funcName, ExtendFunc extendFunc)` | 添加扩展函数 |
| `Object calcFormula()` | 计算当前表达式 |

### 支持的运算符

```
+ - * / % ^        算术运算
== != > < >= <=     比较运算
&& || ! & |         逻辑运算
<< >>               位运算
LIKE IN NOT IN      SQL 风格
BETWEEN...AND       范围判断
IS [NOT] NULL       空值判断
```

---

## EmbedFunc

80+ 个内置函数，供 VariableFormula 调用（抽象类）。

### 数学函数
`max, min, ave, sum, count, round, floor, ceil, sqrt, sin, cos, tan, exp, ln, log, int, frac, stddev, distinct`

### 字符串函数
`concat/strcat, strlen, substr, lpad, rpad, find, split, replace, upcase, lowcase, frequence`

### 日期函数
`today, currentDate, currentDatetime, currentTimestamp, day, month, year, week, weekday, formatDate, dateinfo, dayspan, datespan, addDate/addDays, addMonths, addYears, truncDate, lastOfMonth, toDate`

### 转换函数
`toString, toJsonString, toUrlString, toObject, toNumber, toByteArray`

### 逻辑函数
`if, nvl, case, isempty, isnotempty`

### 其他函数
`match, regexMatch, regexMatchValue, capital, getat, byte, attr, setAttr, getpy, random, hash, eval, encode, decode`

---

## Pretreatment

字符串模板引擎（抽象类），支持 `{varName}` 和 `${varName}` 格式的变量替换。

| 方法 | 描述 |
|------|------|
| `static String mapTemplateString(String template, Object object)` | 变量模板替换 |
| `static String mapTemplateString(String template, Object object, String nullValue, boolean canOmitDollar)` | 带空值和 $ 符号控制的模板替换 |
| `static String mapTemplateStringAsFormula(String template, Object object)` | 公式模板替换（`{}` 中为表达式） |
| `static String mapUrlTemplate(String template, Object object)` | URL 模板替换（自动编码） |
| `static String mapUrlTemplateAsFormula(String template, Object object)` | URL 公式模板替换 |

---

## VariableTranslate (接口)

变量翻译接口。

| 方法 | 描述 |
|------|------|
| `Object getVarValue(String labelName)` | 根据变量名获取值 |

---

## ObjectTranslate

使用反射从对象获取属性值作为变量值（实现 VariableTranslate）。

---

## DummyTranslate

不真正翻译，仅收集变量名集合（用于分析表达式变量，实现 VariableTranslate）。

---

## ExtendFunc (接口)

扩展函数接口。

| 方法 | 描述 |
|------|------|
| `Object execute(VariableTranslate trans, Object[] args)` | 执行扩展函数 |

---

## ConstDefine (抽象类)

运算符和函数 ID 常量定义。

---

## FunctionInfo (类)

内置函数元信息（名称、参数数量、函数ID、返回类型）。

---

## OptStack (类)

运算符优先级栈，用于表达式解析。

---

## EmbedFuncUtils

内置函数的辅助方法（供 JXLS 模板中使用）。

| 方法 | 描述 |
|------|------|
| `String formatDate(Object... slOperand)` | 格式化日期 |
| `String capital(Object object)` | 数字大写 |
| `String capitalRMB(Object object)` | RMB 金额大写（元角分） |
| `String capitalRmbYJF(Object object)` | RMB 金额大写（元角分详细） |
| `Object ifElse(Object... slOperand)` | 三元表达式 |
