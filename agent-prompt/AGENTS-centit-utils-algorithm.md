# centit-utils / algorithm 子包

> 包路径: `com.centit.support.algorithm`
> 核心算法工具类库，提供数据转换、反射、日期、字符串、集合等基础操作。

---

## BooleanBaseOpt

布尔类型转换与判断工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static Boolean castObjectToBoolean(Object obj)` | 将任意对象转为 Boolean，支持 "y/yes/t/true/on" -> true, "n/no/f/false/off" -> false, 数字非0为 true |
| `static Boolean castObjectToBoolean(Object obj, Boolean defaultValue)` | 同上，转换失败返回默认值 |
| `static boolean isBoolean(Object obj)` | 判断对象是否可转为布尔值 |

---

## ByteBaseOpt

字节操作工具（抽象类），提供对象与字节数组互转，以及大端序基本类型的字节读写。

### 字节读取

| 方法 | 描述 |
|------|------|
| `static byte[] castObjectToBytes(Object obj)` | 将任意对象（byte[]/InputStream/BufferedImage/Long/Integer/Date/Float/Double/String等）转为 byte[] |
| `static long readInt64(byte[] buf, int offset)` | 从字节数组读取 8 字节 long |
| `static int readInt32(byte[] buf, int offset)` | 读取 4 字节 int |
| `static short readInt16(byte[] buf, int offset)` | 读取 2 字节 short |
| `static float readFloat(byte[] buf, int offset)` | 读取 float |
| `static double readDouble(byte[] buf, int offset)` | 读取 double |
| `static String readString(byte[] buf, int length, int offset)` | 读取字符串 |
| `static Date readDate(byte[] buf, int offset)` | 读取 Date（8字节时间戳） |
| `static Date readDateAsInt32(byte[] buf, int offset)` | 读取 Date（YYYYMMDD整数编码） |
| `static Date readDatetimeAsInt64(byte[] buf, int offset)` | 读取 Date（YYYYMMDDHHmmss编码） |

### 字节写入

| 方法 | 描述 |
|------|------|
| `static int writeInt64(byte[] buf, long data, int offset)` | 写入 8 字节 long |
| `static int writeInt32(byte[] buf, int data, int offset)` | 写入 4 字节 int |
| `static int writeInt16(byte[] buf, short data, int offset)` | 写入 2 字节 short |
| `static int writeFloat(byte[] buf, float data, int offset)` | 写入 float |
| `static int writeDouble(byte[] buf, double data, int offset)` | 写入 double |
| `static int writeString(byte[] buf, String data, int offset)` | 写入字符串 |
| `static int writeDate(byte[] buf, Date data, int offset)` | 写入 Date |

---

## ClassScannerOpt

类扫描工具（抽象类），扫描指定包下带有特定注解的类。

| 方法 | 描述 |
|------|------|
| `static List<Class<?>> getClassList(String pkgName, boolean isRecursive, Class<? extends Annotation> annotation)` | 查找指定包中带有某注解的类，支持文件系统和 JAR |

---

## CollectionsOpt

集合操作工具（抽象类），核心功能包括树形排序、列表对比、集合克隆与转换。

### 列表操作

| 方法 | 描述 |
|------|------|
| `static <T> void moveListItem(List<T> list, int item, int pos)` | 移动 List 中元素位置 |
| `static <T> void changeListItem(List<T> list, int p1, int p2)` | 交换 List 中两个元素位置 |

### 树形操作

| 方法 | 描述 |
|------|------|
| `static <T> void sortAsTree(List<T> list, ParentChild<? super T> c)` | 树形排序（深度优先），将孩子元素排到父元素之后 |
| `static <T,U> void sortAsTree(List<T> list, Function<? super T,? extends U> pkExtractor, Function<? super T,? extends U> parentPkExtractor)` | 树形排序（lambda版） |
| `static <T> JSONArray treeToJSONArray(List<T> treeList, ParentChild<? super T> c, String childrenPropertyName)` | 已排序树转为 JSONArray |
| `static <T> JSONArray sortAsTreeAndToJSON(List<T> treeList, ParentChild<? super T> c, String childrenPropertyName)` | 排序并转 JSON 一步完成 |
| `static <T> List<TreeNode<T>> storedAsTree(List<T> list, ParentChild<? super T> c)` | 将列表存为 TreeNode 树结构 |
| `static void depthFirstTraverseTree(Object rootObject, List<Object> expendTree, String childrenPropertyName)` | 深度优先遍历树 |
| `static List<Object> breadthFirstTraverseForest(Collection<?> treeObjects, String childrenPropertyName)` | 广度优先遍历森林 |

### 列表对比与转换

| 方法 | 描述 |
|------|------|
| `static <T> Triple<List<T>, List<Pair<T,T>>, List<T>> compareTwoList(List<T> oldList, List<T> newList, Comparator<T> compare)` | 列表差异对比，返回 (新增, 更新, 删除) 三组数据 |
| `static <T> T[] listToArray(Collection<T> listObj, Class<T> classType)` | List 转 Array |
| `static <T> List<T> arrayToList(T[] arrayObj)` | Array 转 List |

### Map 操作

| 方法 | 描述 |
|------|------|
| `static Map<String, Object> createHashMap(Object... objs)` | 快速创建 Map（key1,val1,key2,val2...） |
| `static <K,V> Map<K,V> unionTwoMap(Map<K,V> map1, Map<K,V> map2)` | 合并两个 Map，map1 优先 |
| `static <T,R> List<R> mapCollectionToList(Collection<T> array, Function<T,R> func)` | 集合属性提取 |
| `static <D,K,V> Map<K,V> mapCollectionToMap(Collection<D> array, Function<D,K> keyFunc, Function<D,V> valueFunc)` | 集合转 Map |
| `static Map<String, Object> objectToMap(Object object)` | 任意对象转 Map |

### 函数式接口

| 接口 | 描述 |
|------|------|
| `ParentChild<T>` | 判断两个对象是否为父子关系的函数式接口 |

---

## DatetimeOpt

日期时间操作工具（抽象类），功能全面覆盖日期创建、转换、比较、计算、截断。

### 当前日期

| 方法 | 描述 |
|------|------|
| `static String currentDate()` | 当前日期字符串 "yyyy-MM-dd" |
| `static String currentDatetime()` | 当前日期时间字符串 "yyyy-MM-dd HH:mm:ss" |

### 创建与转换

| 方法 | 描述 |
|------|------|
| `static Date createUtilDate(int year, int month, int date, int hour, int minute, int second, int milliSecond)` | 创建 Date |
| `static java.sql.Date convertToSqlDate(java.util.Date date)` | util.Date -> sql.Date |
| `static Timestamp convertToSqlTimestamp(java.util.Date date)` | util.Date -> Timestamp |
| `static Date convertStringToDate(String strDate, String sMask)` | 按格式解析字符串为日期 |
| `static String convertDateToString(Date aDate, String aMask)` | 日期按格式转为字符串 |
| `static String convertDateToSmartString(Date aDate, boolean withSecond)` | 智能日期字符串（今天/昨天/前天） |
| `static Date smartPraseDate(String sDate)` | 智能日期解析，自动识别多种格式 |
| `static Date castObjectToDate(Object obj)` | 任意对象转 Date |

### 日期属性

| 方法 | 描述 |
|------|------|
| `static int getDayOfWeek(Date date)` | 获取星期几 (0=周日) |
| `static int getYear(Date date)` / `getMonth` / `getDay` / `getHour` / `getMinute` / `getSecond` | 获取日期各属性 |

### 日期截断与跳转

| 方法 | 描述 |
|------|------|
| `static Date truncateToDay(Date date)` / `truncateToMonth` / `truncateToYear` / `truncateToWeek` | 截断日期到指定精度 |
| `static Date seekEndOfMonth(Date date)` / `seekEndOfYear` / `seekEndOfWeek` | 跳转到周期末尾 |

### 日期加减

| 方法 | 描述 |
|------|------|
| `static Date addDays(Date date, int nDays)` / `addMonths` / `addYears` / `addHours` / `addMinutes` / `addSeconds` | 日期加减运算 |

### 日期比较

| 方法 | 描述 |
|------|------|
| `static int calcSpanDays(Date beginDate, Date endDate)` | 计算两日期天数差 |
| `static int calcWeekendDays(Date beginDate, Date endDate)` | 计算周末天数 |
| `static boolean equalOnSecond(Date one, Date other)` / `equalOnMinute` / `equalOnHour` / `equalOnDay` | 不同精度的日期相等比较 |
| `static int compareTwoDate(Date one, Date other)` | 安全的日期比较（避免 NPE） |
| `static TimeZone fetchTimeZone(String zone)` | 时区字符串转 TimeZone |

---

## EnumBaseOpt

枚举类型操作工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static <T> T ordinalToEnum(Class<T> enumType, int ordinal)` | 序号转枚举 |
| `static <T> T stringToEnum(Class<T> enumType, String name, boolean ignoreCase)` | 字符串转枚举 |
| `static int enumToOrdinal(Object enumObj)` | 枚举转序号 |
| `static String enumToString(Object enumObj)` | 枚举转字符串 |

---

## GeneralAlgorithm

通用算法（抽象类），提供对象比较、四则运算、聚合函数、空值判断。

### 通用操作

| 方法 | 描述 |
|------|------|
| `static <T> T nvl(T obj, T obj2)` | 空值替换（类似 SQL NVL） |
| `static boolean equals(Object a, Object b, boolean allNullAsTrue)` | 安全的对象相等比较 |
| `static boolean isEmpty(Object obj)` | 递归判断对象是否为空（支持集合/数组/Map/字符串） |

### 比较

| 方法 | 描述 |
|------|------|
| `static int compareTwoObject(Object a, Object b, boolean nullAsFirst)` | 通用对象比较（支持数字、字符串中文排序） |
| `static int compareTwoObjectByField(Object data1, Object data2, String[] fields, boolean nullAsFirst)` | 按字段比较两个对象 |

### 四则运算

| 方法 | 描述 |
|------|------|
| `static Object addTwoObject(Object a, Object b, boolean nullSensitive)` | 通用加法（数字相加、日期+天数、字符串连接、集合合并） |
| `static Object subtractTwoObject(Object a, Object b)` | 通用减法 |
| `static Object multiplyTwoObject(Object a, Object b)` | 通用乘法 |
| `static Object divideTwoObject(Object a, Object b)` | 通用除法 |

### 聚合函数

| 方法 | 描述 |
|------|------|
| `static Object maxObject(Collection<Object> ar)` | 集合最大值 |
| `static Object minObject(Collection<Object> ar)` | 集合最小值 |
| `static Object sumObjects(Collection<Object> ar)` | 集合求和 |

### 类型转换

| 方法 | 描述 |
|------|------|
| `static Object castObjectToType(Object obj, Class<?> type)` | 对象类型转换 |

---

## Lunar

中国农历计算类。支持公历转农历、天干地支、生肖。

| 方法/属性 | 描述 |
|-----------|------|
| 构造: `Lunar(Date dt)` / `Lunar(Calendar cal)` | 从公历日期创建农历对象 |
| `String toString()` | 返回农历字符串，如 "甲子年正月初一" |
| `String animalsYear()` | 生肖 |
| `String cyclical()` | 天干地支 |
| `String getLunarDay()` / `getLunarMonth()` / `getLunarYear()` | 农历日/月/年 |
| `static String getChinaDayString(int day)` | 中文日期字符串 |

---

## Mathematics

数学算法：排列、组合（非递归实现）。

| 方法 | 描述 |
|------|------|
| `static <T> boolean nextPermutation(List<T> list, Comparator<? super T> comparable)` | 下一个排列（类似 C++ STL） |
| `static <T> boolean prevPermutation(List<T> list, Comparator<? super T> comparable)` | 上一个排列 |
| `static <T> void permutation(List<T> listSource, Comparator<? super T> comparable, Consumer<List<T>> consumer)` | 非递归全排列（自动去重） |
| `static <T> void combination(List<T> listSource, int selected, Comparator<? super T> comparable, Consumer<List<T>> consumer)` | 非递归组合（自动去重） |
| `static <T> void permutationAndCombination(...)` | 排列与组合 |

---

## NumberBaseOpt

数字操作工具（抽象类），包括大写转换、类型转换、数值解析。

### 大写转换

| 方法 | 描述 |
|------|------|
| `static String capitalization(String szNum)` | 数字大写（财务用: 壹贰叁） |
| `static String capitalizationCN(String szNum)` | 数字中文大写（一二三） |
| `static String uppercaseCN(String szNum)` | 数字转中文数字（二〇一七） |

### 类型转换

| 方法 | 描述 |
|------|------|
| `static boolean isNumber(Object obj)` | 判断对象是否为数字 |
| `static Long castObjectToLong(Object obj)` | 对象转 Long |
| `static Integer castObjectToInteger(Object obj)` | 对象转 Integer |
| `static Double castObjectToDouble(Object obj)` | 对象转 Double |
| `static Float castObjectToFloat(Object obj)` | 对象转 Float |
| `static BigDecimal castObjectToBigDecimal(Object obj)` | 对象转 BigDecimal |
| `static Number castObjectToNumber(Object obj)` | 对象转 Number（自动判断整数/浮点） |

### 数值操作

| 方法 | 描述 |
|------|------|
| `static Number round(Number number, int pos)` | 四舍五入到指定小数位 |
| `static Number floor(Number number, int pos)` | 向下取整 |
| `static Number ceil(Number number, int pos)` | 向上取整 |
| `static char getNumByte(String szNum, int nBit)` | 获取某位上的数字字符 |

---

## ReflectionOpt

反射操作工具（抽象类），提供字段访问、方法调用、类型判断等。

### 字段操作

| 方法 | 描述 |
|------|------|
| `static Field getDeclaredField(Object object, String propertyName)` | 循环向上转型获取 Field |
| `static Object getFieldValue(Object obj, String fieldName)` | 通过 getter 获取属性值 |
| `static boolean setFieldValue(Object object, String fieldName, Object newValue)` | 通过 setter 设置属性值 |
| `static boolean setAttributeValue(Object object, String attributeName, Object newValue)` | 设置嵌套属性（支持 a.b.c） |
| `static Object forceGetProperty(Object object, String propertyName)` | 强制获取属性（忽略 private） |
| `static void forceSetProperty(Object object, String propertyName, Object newValue)` | 强制设置属性 |
| `static Object attainExpressionValue(Object sourceObj, String expression)` | 表达式取值，如 `a.b[0].c` |

### 方法操作

| 方法 | 描述 |
|------|------|
| `static List<Method> getAllGetterMethod(Class<?> type)` | 获取所有 getter 方法 |
| `static List<Method> getAllSetterMethod(Class<?> type)` | 获取所有 setter 方法 |
| `static Object invokeMethod(Object demander, String smethod, Object... params)` | 反射调用方法 |

### 类型判断

| 方法 | 描述 |
|------|------|
| `static boolean isScalarType(Class<?> tp)` | 判断是否为标量类型 |
| `static boolean isArray(Object obj)` | 判断是否为数组/Collection |
| `static Class<?> getSuperClassGenricType(Class<?> clazz, int index)` | 获取父类泛型类型 |

---

## Snowflake

分布式 ID 生成器（雪花算法），支持时钟回拨容忍。

| 方法/属性 | 描述 |
|-----------|------|
| 构造: `Snowflake()` / `Snowflake(long workerId, long dataCenterId)` | 默认或指定 worker/dataCenter |
| `synchronized long nextId()` | 生成下一个 long 型 ID |
| `String nextIdStr()` | 字符串形式 ID |
| `String nextIdHex()` | 十六进制 ID |
| `String nextIdBase64()` | Base64 编码的 ID（22字符） |
| `static long makeWorkerId(long datacenterId, long maxWorkerId)` | 根据 MAC+PID 自动生成 workerId |
| `static long makeDataCenterId(long maxDatacenterId)` | 根据 MAC 地址自动生成 dataCenterId |
| `long getWorkerId(long id)` / `getDataCenterId(long id)` / `getGenerateDateTime(long id)` | 从 ID 中提取信息 |

---

## StringBaseOpt

字符串操作工具（抽象类），包含编码、压缩、拼音、对象转换等。

### 编码与压缩

| 方法 | 描述 |
|------|------|
| `static String encodeBase64(String str)` | Base64 编码 |
| `static String decodeBase64(String str)` | Base64 解码 |
| `static byte[] compress(String str)` | GZIP 压缩字符串 |
| `static String unCompress(byte[] str)` | GZIP 解压 |

### 字符串操作

| 方法 | 描述 |
|------|------|
| `static String concat(Object... objs)` | 连接字符串（自动忽略 null） |
| `static String concat(Object[] objs, String separator)` | 带分隔符连接 |
| `static String nextCode(String sCode)` | 字符串自增（"0000200" -> "0000201"） |
| `static String prevCode(String sCode)` | 字符串自减 |

### 中文处理

| 方法 | 描述 |
|------|------|
| `static String getFirstLetter(String oriStr)` | 获取汉字拼音首字母 |
| `static String getPinYin(String oriStr)` | 获取汉字全拼 |

### 对象转换

| 方法 | 描述 |
|------|------|
| `static String objectToString(Object objValue)` | 任意对象转字符串 |
| `static String[] objectToStringArray(Object object)` | 对象转字符串数组 |
| `static <T> T stringToScalarData(String sdata, Class<T> clazz)` | 字符串转标量数据 |

### 文件读取

| 方法 | 描述 |
|------|------|
| `static String readFileToBuffer(String sFileName)` | 读文件为字符串 |
| `static String readJarResourceToBuffer(Class<?> clazz, String sResourceName)` | 读 JAR 资源文件 |

---

## StringRegularOpt

字符串正则/模式匹配工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String trimString(String szWord)` | 去除字符串引号包裹 |
| `static boolean isNumber(String szNum)` | 判断是否为数字字符串 |
| `static boolean isDigit(String pszNum)` | 判断是否为纯数字 |
| `static boolean isString(String szWord)` | 判断是否为引号字符串 |
| `static boolean isTrue(String str)` / `isFalse(String str)` | 判断是否为真/假字符串 |
| `static boolean isDatetime(String szTime)` / `isDate` / `isTime` | 判断是否为日期/时间字符串 |
| `static String trimDateString(String szDateStr)` | 智能清理日期字符串 |
| `static boolean isMatch(String szTempl, String szValue, int wildcardType)` | 通配符匹配（支持 Windows `*?`、SQL `%_`） |
| `static boolean isChinese(char c)` | 判断是否为中文字符 |
| `static int getFirstChinesePos(String strName)` | 获取首个中文位置 |

---

## UuidOpt

UUID 生成与格式转换工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String getUuidAsString36()` | 36 字符标准 UUID |
| `static String getUuidAsString32()` | 32 字符无横线 UUID |
| `static String getUuidAsBase64String()` | 22 字符 Base64 UUID |
| `static String randomString(int length)` | 指定长度随机字符串 |

---

## ZipCompressor

ZIP 压缩/解压工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static void compress(String zipFilePathName, String srcPathName)` | 压缩文件/目录 |
| `static void compressFiles(String zipFilePathName, String[] srcPathNames)` | 压缩多个文件 |
| `static void release(String zipPath, String dirPath)` | 解压到指定目录 |
| `static ZipArchiveOutputStream convertToZipOutputStream(OutputStream os)` | OutputStream 转 ZipOutputStream |
