# 表达式解释器内置函数说明文档

本文档描述了 `EmbedFunc` 类中实现的表达式解释器的所有内置函数。这些函数可在表达式运算中使用，涵盖数学运算、字符串处理、日期操作、类型转换、条件判断等多个功能域。

## 目录

- [数组和取值函数](#数组和取值函数)
- [数学运算函数](#数学运算函数)
- [字符串函数](#字符串函数)
- [日期时间函数](#日期时间函数)
- [条件判断函数](#条件判断函数)
- [类型转换函数](#类型转换函数)
- [聚合统计函数](#聚合统计函数)
- [加密编码函数](#加密编码函数)
- [工具函数](#工具函数)

---

## 数组和取值函数

### getat
**功能**: 获取数组或参数列表中指定位置的元素
**语法**: `getat(index, value1, value2, ...)`
**参数**:
- `index`: 索引位置（支持负数，-1表示最后一个元素）
- `value1, value2, ...`: 值列表

**示例**:
```
getat(0, "a", "b", "c")     // 返回 "a"
getat(1, 2, 3, 4)           // 返回 3
getat(-1, 10, 20, 30)       // 返回 30（最后一个元素）
```

### attr
**功能**: 获取对象的属性值
**语法**: `attr(object, attributeName)`
**参数**:
- `object`: 目标对象
- `attributeName`: 属性名称

**示例**:
```
attr(user, "name")          // 获取user对象的name属性
attr(data, "user.age")      // 获取嵌套属性
```

### setAttr
**功能**: 设置对象的属性值
**语法**: `setAttr(object, attributeName, value)`
**参数**:
- `object`: 目标对象
- `attributeName`: 属性名称
- `value`: 要设置的值

**示例**:
```
setAttr(user, "name", "张三")    // 设置user对象的name属性为"张三"
setAttr(data, "age", 25)         // 设置age属性为25
```

---

## 数学运算函数

### byte
**功能**: 获取数字或字符串指定位置的字符/数字
**语法**: `byte(value, position)`
**参数**:
- `value`: 数字或字符串
- `position`: 位置索引

**示例**:
```
byte(4321.789, 0)           // 返回 "1"（个位数）
byte("hello", 1)            // 返回 "e"（第1位字符）
```

### max/min
**功能**: 求参数中的最大值/最小值
**语法**: `max(value1, value2, ...)` / `min(value1, value2, ...)`
**参数**: 可变参数列表

**示例**:
```
max(1, 2, 3, 5, 4)          // 返回 5
min(1, 2, 3, 5, 4)          // 返回 1
max([1, 5, 3], 2, 4)        // 返回 5（支持数组展开）
```

### round/floor/ceil
**功能**: 数字四舍五入/向下取整/向上取整
**语法**: `round(number, [precision])` / `floor(number, [precision])` / `ceil(number, [precision])`
**参数**:
- `number`: 要处理的数字
- `precision`: 精度（可选，默认为0）

**示例**:
```
round(3.14159, 2)           // 返回 3.14
floor(3.14159, 2)           // 返回 3.14
ceil(3.14159, 2)            // 返回 3.15
round(123.456)              // 返回 123
```

### int/integer
**功能**: 取数字的整数部分
**语法**: `int(number)` / `integer(number)`
**参数**: `number`: 数字

**示例**:
```
int(12.34)                  // 返回 12
int(-12.34)                 // 返回 -12
```

### frac
**功能**: 取数字的小数部分
**语法**: `frac(number)`
**参数**: `number`: 数字

**示例**:
```
frac(12.34)                 // 返回 0.34
frac(-12.34)                // 返回 -0.34
```

### 三角函数
**功能**: 数学三角函数运算
**语法**: `sin(x)` / `cos(x)` / `tan(x)` / `ctan(x)`
**参数**: `x`: 角度值（弧度制）

**示例**:
```
sin(3.14159/2)              // 返回 1
cos(0)                      // 返回 1
tan(3.14159/4)              // 返回约 1
```

### 对数和指数函数
**功能**: 对数和指数运算
**语法**: `log(x)` / `ln(x)` / `exp(x)` / `sqrt(x)`
**参数**: `x`: 数值

**示例**:
```
log(100)                    // 返回 2（以10为底的对数）
ln(2.718)                   // 返回约 1（自然对数）
exp(1)                      // 返回约 2.718（e的1次方）
sqrt(16)                    // 返回 4（平方根）
```

---

## 字符串函数

### strlen
**功能**: 获取字符串长度
**语法**: `strlen(string)`
**参数**: `string`: 字符串

**示例**:
```
strlen("hello")             // 返回 5
strlen("你好")              // 返回 2
```

### concat/strcat
**功能**: 连接多个字符串
**语法**: `concat(str1, str2, ...)` / `strcat(str1, str2, ...)`
**参数**: 多个字符串参数

**示例**:
```
concat("hello", " ", "world")   // 返回 "hello world"
strcat("数字:", 123)            // 返回 "数字:123"
```

### substr
**功能**: 获取字符串子串
**语法**: `substr(string, start, [length])`
**参数**:
- `string`: 原字符串
- `start`: 起始位置
- `length`: 长度（可选）

**示例**:
```
substr("hello world", 6)        // 返回 "world"
substr("hello world", 0, 5)     // 返回 "hello"
substr("测试字符串", 2, 2)       // 返回 "字符"
```

### lpad/rpad
**功能**: 左侧/右侧填充字符串到指定长度
**语法**: `lpad(string, length, [padString])` / `rpad(string, length, [padString])`
**参数**:
- `string`: 原字符串
- `length`: 目标长度
- `padString`: 填充字符（可选，默认为空格）

**示例**:
```
lpad("123", 5, "0")         // 返回 "00123"
rpad("abc", 6, "*")         // 返回 "abc***"
lpad("hello", 10)           // 返回 "     hello"
```

### upcase/lowcase
**功能**: 字符串大小写转换
**语法**: `upcase(string)` / `lowcase(string)`
**参数**: `string`: 字符串

**示例**:
```
upcase("Hello World")       // 返回 "HELLO WORLD"
lowcase("Hello World")      // 返回 "hello world"
```

### find
**功能**: 查找子字符串位置或元素在集合中的索引
**语法**: `find(source, target, [start], [findType])`
**参数**:
- `source`: 源字符串或集合
- `target`: 要查找的子字符串或元素
- `start`: 开始位置（可选）
- `findType`: 查找类型（"C"=区分大小写，"W"=单词匹配）

**示例**:
```
find("hello world", "world")    // 返回 6
find("Hello World", "WORLD")    // 返回 6（不区分大小写）
find("Hello World", "WORLD", 0, "C")  // 返回 -1（区分大小写）
find([1,2,3], 2)                // 返回 1（数组索引）
```

### frequence
**功能**: 统计子字符串在字符串中出现的次数
**语法**: `frequence(string, substring)`
**参数**:
- `string`: 源字符串
- `substring`: 子字符串

**示例**:
```
frequence("hello world", "l")   // 返回 3
frequence("abcabc", "ab")       // 返回 2
```

### split
**功能**: 分割字符串为数组
**语法**: `split(string, [delimiter])`
**参数**:
- `string`: 要分割的字符串
- `delimiter`: 分隔符（可选，默认为","）

**示例**:
```
split("a,b,c")              // 返回 ["a", "b", "c"]
split("a;b;c", ";")         // 返回 ["a", "b", "c"]
split("a.b.c", "\\.")       // 返回 ["a", "b", "c"]（转义特殊字符）
```

### replace
**功能**: 字符串替换
**语法**: `replace(string, oldValue1, newValue1, [oldValue2, newValue2, ...])` 或 `replace(string, replaceMap)`
**参数**:
- `string`: 源字符串
- 多对替换值或替换映射表

**示例**:
```
replace("hello world", "world", "java")    // 返回 "hello java"
replace("abc", "a", "1", "b", "2")         // 返回 "12c"
replace("test", {"e":"3", "t":"7"})        // 返回 "7es7"
```

### match
**功能**: 通配符匹配
**语法**: `match(pattern, string, [wildcardType])`
**参数**:
- `pattern`: 模式字符串（支持 * 和 ? 通配符）
- `string`: 要匹配的字符串
- `wildcardType`: 通配符类型（0-2，默认2）

**示例**:
```
match("a??d", "abcd")       // 返回 true
match("test*", "testing")   // 返回 true
match("a*c", "abc")         // 返回 true
```

### regexmatch
**功能**: 正则表达式匹配
**语法**: `regexmatch(pattern, string)`
**参数**:
- `pattern`: 正则表达式
- `string`: 要匹配的字符串

**示例**:
```
regexmatch("^1[3-9]\\d{9}$", "13901390139")    // 返回 true（手机号验证）
regexmatch("\\d+", "abc123def")                 // 返回 true（包含数字）
```

### regexmatchvalue
**功能**: 正则表达式匹配并返回匹配的值
**语法**: `regexmatchvalue(pattern, string)`
**参数**:
- `pattern`: 正则表达式
- `string`: 要匹配的字符串

**示例**:
```
regexmatchvalue("\\d+", "abc123def456")    // 返回 ["123", "456"]
regexmatchvalue("(\\d+)", "price:100yuan") // 返回 "100"（捕获组）
```

### capital
**功能**: 数字大写转换（中文）
**语法**: `capital(number, [type])`
**参数**:
- `number`: 数字
- `type`: 类型（"N"=普通，"R"/"RMB"=人民币，"YJF"=元角分，"S"=简化）

**示例**:
```
capital(123.45)                 // 返回 "一百二十三点四五"
capital(123.45, "R")            // 返回 "壹佰贰拾叁圆肆角伍分"
capital(123.45, "YJF")          // 返回 "壹佰贰拾叁元肆角伍分"
capital("hello")                // 返回 "HELLO"（非数字时转大写）
```

### getpy
**功能**: 获取汉字拼音
**语法**: `getpy(chineseString, [type])`
**参数**:
- `chineseString`: 中文字符串
- `type`: 类型（"0"=首字母，"1"=全拼）

**示例**:
```
getpy("中国", "0")              // 返回 "ZG"（首字母）
getpy("中国", "1")              // 返回 "zhongguo"（全拼）
getpy("测试")                   // 返回 "CS"（默认首字母）
```

---

## 日期时间函数

### 当前时间函数
**功能**: 获取当前日期时间
**语法**: `currentDate()` / `currentDatetime()` / `currentTimestamp()` / `today()`

**示例**:
```
currentDate()               // 返回当前日期（不含时间）
currentDatetime()           // 返回当前日期时间
today()                     // 返回当前日期时间
currentTimestamp()          // 返回当前时间戳
```

### 日期部分提取
**功能**: 提取日期的年、月、日等部分
**语法**: `year([date])` / `month([date])` / `day([date])` / `week([date])` / `weekday([date])`
**参数**: `date`: 日期（可选，默认为当前日期）

**示例**:
```
year("2023-12-25")          // 返回 2023
month("2023-12-25")         // 返回 12
day("2023-12-25")           // 返回 25
week("2023-12-25")          // 返回年中第几周
weekday("2023-12-25")       // 返回星期几（0=周日，1-6=周一到周六）
```

### 日期格式化
**功能**: 格式化日期为字符串
**语法**: `formatdate(date, [pattern])`
**参数**:
- `date`: 日期
- `pattern`: 格式模式（可选）

**示例**:
```
formatdate("2023-12-25", "yyyy年MM月dd日")  // 返回 "2023年12月25日"
formatdate("2023-12-25", "yyyy-MM-dd HH:mm:ss")
```

### 日期计算
**功能**: 日期加减运算
**语法**: `adddate(date, days)` / `adddays(date, days)` / `addmonths(date, months)` / `addyears(date, years)`
**参数**:
- `date`: 基准日期（可选，默认为当前日期）
- 数值: 要加减的数量

**示例**:
```
adddays("2023-12-25", 7)        // 返回 "2024-01-01"
addmonths("2023-12-25", 1)      // 返回 "2024-01-25"
addyears("2023-12-25", 1)       // 返回 "2024-12-25"
adddate(10)                     // 当前日期加10天
```

### 日期差值
**功能**: 计算两个日期之间的差值
**语法**: `dayspan(date1, date2)` / `datespan(date1, date2)`
**参数**: 两个日期

**示例**:
```
dayspan("2023-12-31", "2023-12-25")    // 返回 6（天数差）
datespan("2024-01-01", "2023-12-25")   // 返回日期差对象
```

### 日期截断
**功能**: 将日期截断到指定单位
**语法**: `truncdate([date], unit)`
**参数**:
- `date`: 日期（可选，默认当前日期）
- `unit`: 截断单位（"Y"=年，"M"=月，"D"=日，"W"=周）

**示例**:
```
truncdate("2023-12-25", "Y")    // 返回 "2023-01-01"（年初）
truncdate("2023-12-25", "M")    // 返回 "2023-12-01"（月初）
truncdate("2023-12-25", "W")    // 返回本周周一
```

### lastofmonth
**功能**: 获取月末日期
**语法**: `lastofmonth([date])`
**参数**: `date`: 日期（可选，默认当前日期）

**示例**:
```
lastofmonth("2023-02-15")       // 返回 "2023-02-28"
lastofmonth("2024-02-15")       // 返回 "2024-02-29"（闰年）
```

### dateinfo
**功能**: 获取日期的详细信息
**语法**: `dateinfo(field, [date])`
**参数**:
- `field`: 字段常量（Calendar字段）
- `date`: 日期（可选，默认当前日期）

**示例**:
```
dateinfo(1, "2023-12-25")       // 返回年份
dateinfo(2, "2023-12-25")       // 返回月份
dateinfo(5, "2023-12-25")       // 返回日期
```

---

## 条件判断函数

### if
**功能**: 条件判断
**语法**: `if(condition, trueValue, [falseValue])`
**参数**:
- `condition`: 条件表达式
- `trueValue`: 条件为真时的返回值
- `falseValue`: 条件为假时的返回值（可选）

**示例**:
```
if(1, "是", "否")               // 返回 "是"
if(0, "是", "否")               // 返回 "否"
if(age > 18, "成年人", "未成年")
```

### nvl
**功能**: 空值替换
**语法**: `nvl(value, defaultValue)`
**参数**:
- `value`: 要检查的值
- `defaultValue`: 默认值

**示例**:
```
nvl(null, "默认值")             // 返回 "默认值"
nvl("有值", "默认值")           // 返回 "有值"
nvl("", "默认值")               // 返回 ""（空字符串不是null）
```

### case
**功能**: 多分支条件判断
**语法**: `case(condition, candidate1, value1, candidate2, value2, [defaultValue])`
**参数**:
- `condition`: 条件值
- 候选值和对应返回值对
- `defaultValue`: 默认值（可选）

**示例**:
```
case(1, 1, "一", 2, "二", "其他")     // 返回 "一"
case("A", "A", "优秀", "B", "良好", "一般")  // 返回 "优秀"
case(true, true, "是", false, "否")   // 返回 "是"
```

### isempty/isnotempty
**功能**: 判断值是否为空
**语法**: `isempty(value1, [value2, ...])` / `isnotempty(value1, [value2, ...])`
**参数**: 一个或多个值

**示例**:
```
isempty("")                     // 返回 true
isempty(null)                   // 返回 true
isempty("hello")                // 返回 false
isnotempty("hello")             // 返回 true
isempty("", null, "")           // 返回 true（所有参数都为空）
isnotempty("", "hello", null)   // 返回 true（存在非空参数）
```

---

## 类型转换函数

### toDate
**功能**: 转换为日期类型
**语法**: `toDate(value, [pattern])`
**参数**:
- `value`: 要转换的值
- `pattern`: 日期格式（可选）

**示例**:
```
toDate("2023-12-25")            // 返回日期对象
toDate("25/12/2023", "dd/MM/yyyy")  // 按指定格式转换
toDate()                        // 返回当前日期
```

### toString
**功能**: 转换为字符串
**语法**: `toString(value1, [value2, ...])`
**参数**: 一个或多个值（返回第一个非空字符串）

**示例**:
```
toString(123)                   // 返回 "123"
toString(null, "", "hello")     // 返回 "hello"
toString(true)                  // 返回 "true"
```

### toJsonString
**功能**: 转换为JSON字符串
**语法**: `toJsonString(object)`
**参数**: `object`: 要转换的对象

**示例**:
```
toJsonString({"name":"张三","age":25})  // 返回 JSON 字符串
toJsonString([1,2,3])                   // 返回 "[1,2,3]"
```

### toUrlString
**功能**: 转换为URL编码字符串
**语法**: `toUrlString(object)`
**参数**: `object`: 要转换的对象

**示例**:
```
toUrlString({"name":"张三","city":"北京"})  // 返回 URL 编码格式
```

### toObject
**功能**: 转换为对象
**语法**: `toObject(string, [type])`
**参数**:
- `string`: JSON或XML字符串
- `type`: 类型（"json"或"xml"，默认"json"）

**示例**:
```
toObject('{"name":"张三"}')                // 返回 JSON 对象
toObject('<user><name>张三</name></user>', "xml")  // 返回 XML 对象
```

### toNumber
**功能**: 转换为数字
**语法**: `toNumber(value, [defaultValue])`
**参数**:
- `value`: 要转换的值
- `defaultValue`: 默认值（可选）

**示例**:
```
toNumber("123")                 // 返回 123
toNumber("abc", 0)              // 返回 0（无法转换时返回默认值）
toNumber("3.14")                // 返回 3.14
```

### toByteArray
**功能**: 转换为字节数组
**语法**: `toByteArray(value)`
**参数**: `value`: 要转换的值

**示例**:
```
toByteArray("hello")            // 返回字符串的字节数组
toByteArray(123)                // 返回数字的字节数组
```

---

## 聚合统计函数

### count/countnotnull/countnull
**功能**: 计数函数
**语法**: `count(...)` / `countnotnull(...)` / `countnull(...)`
**参数**: 可变参数列表

**示例**:
```
count(1, "2", 3, "5", 1, 1, 4)      // 返回 7（总参数个数）
countnotnull(1, null, "2", "", null) // 返回 3（非空参数个数）
countnull(1, null, "2", "", null)    // 返回 2（空参数个数）
```

### sum/ave
**功能**: 求和/求平均值
**语法**: `sum(...)` / `ave(...)`
**参数**: 数字参数列表

**示例**:
```
sum(1, 2, 3, 4, 5)              // 返回 15
ave(1, 2, 3, 4, 5)              // 返回 3
sum([1, 2, 3], [4, 5])          // 返回 15（支持数组展开）
```

### stddev
**功能**: 计算标准偏差
**语法**: `stddev(...)`
**参数**: 数字参数列表

**示例**:
```
stddev(1, 2, 3, 4, 5)           // 返回标准偏差值
stddev([10, 12, 14, 16, 18])    // 返回标准偏差值
```

### distinct
**功能**: 去重
**语法**: `distinct(...)`
**参数**: 可变参数列表

**示例**:
```
distinct(1, 2, 2, 3, 1)         // 返回 [1, 2, 3]
distinct("a", "b", "a", "c")    // 返回 ["a", "b", "c"]
```

---

## 加密编码函数

### hash
**功能**: 计算哈希值
**语法**: `hash(data, [algorithm], [encoding/secretKey], [encoding])`
**参数**:
- `data`: 要哈希的数据
- `algorithm`: 算法（"MD5"/"SHA"/"SM3"/"HMAC-SHA1"/"HMAC-SM3"）
- `encoding`: 编码方式（"HEX"/"BASE64"/"BASE64URL"）或HMAC密钥
- `encoding`: HMAC时的编码方式

**示例**:
```
hash("hello")                           // 返回 MD5 哈希（HEX编码）
hash("hello", "SHA1")                   // 返回 SHA1 哈希
hash("hello", "MD5", "BASE64")          // 返回 BASE64 编码的 MD5
hash("hello", "HMAC-SHA1", "secret")    // 返回 HMAC-SHA1
hash("hello", "HMAC-SHA1", "secret", "BASE64")  // BASE64编码的HMAC
```

### encode/decode
**功能**: 编码/解码
**语法**: `encode(data, [type])` / `decode(data, [type])`
**参数**:
- `data`: 要编码/解码的数据
- `type`: 编码类型（"HEX"/"BASE64"/"BASE64URL"）

**示例**:
```
encode("hello", "HEX")          // 返回十六进制编码
encode("hello", "BASE64")       // 返回 BASE64 编码
decode("68656c6c6f", "HEX")     // 返回 "hello"
decode("aGVsbG8=", "BASE64")    // 返回 "hello"
```

---

## 工具函数

### random
**功能**: 生成随机数或字符串
**语法**: `random([type], [param])`
**参数**:
- 无参数: 返回0-1的随机数
- 1个参数: 返回0到指定数的随机整数
- 2个数字参数: 返回指定范围的随机整数
- "str"类型: 生成随机字符串

**示例**:
```
random()                        // 返回 0-1 的随机小数
random(100)                     // 返回 0-99 的随机整数
random(10, 20)                  // 返回 10-19 的随机整数
random("str", 8)                // 返回8位随机字符串
random("str", "uuid")           // 返回UUID字符串（32位）
random("str", "uuid22")         // 返回22位UUID
random("str", "uuid36")         // 返回36位UUID（含连字符）
```

### eval
**功能**: 动态执行表达式
**语法**: `eval(expression, [context])`
**参数**:
- `expression`: 要执行的表达式字符串
- `context`: 执行上下文（可选）

**示例**:
```
eval("1 + 2")                   // 返回 3
eval("max(a, b)", {"a":10, "b":5})  // 返回 10
```

---

## 函数使用注意事项

1. **参数类型**: 大多数函数会自动进行类型转换，但建议传入正确的参数类型
2. **空值处理**: 函数对null值有不同的处理策略，具体查看各函数说明
3. **数组展开**: 支持数组展开的函数会自动将数组参数展开为单个元素
4. **索引**: 大部分索引从0开始，负数表示从后往前计算
5. **正则表达式**: 正则函数中的特殊字符需要适当转义
6. **日期格式**: 日期函数支持多种常见的日期格式自动识别

---

## 常用函数组合示例

```
// 数据处理组合
nvl(toString(attr(user, "name")), "未知用户")
// 日期计算组合
formatdate(adddays(today(), 7), "yyyy-MM-dd")

// 条件判断组合
case(
  weekday(today()),
  1, "周一", 2, "周二", 3, "周三", 4, "周四", 5, "周五",
  "周末"
)

// 字符串处理组合
concat(upcase(substr(name, 0, 1)), lowcase(substr(name, 1)))

// 数据验证组合
if(regexmatch("^1[3-9]\\d{9}$", phone), "有效", "无效");
```

这份文档涵盖了 EmbedFunc 类中所有80个内置函数的详细说明和使用示例，可以作为表达式解释器的完整参考手册。
