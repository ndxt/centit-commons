# centit-utils / file 子包

> 包路径: `com.centit.support.file`
> 文件操作工具。

---

## FileIOOpt

文件 IO 操作工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static int writeInputStreamToFile(InputStream in, String filePath)` | InputStream 写入文件 |
| `static void writeStringToFile(String strData, String fileName)` | 字符串写入文件 |
| `static void appendStringToFile(String strData, String fileName)` | 追加字符串到文件 |
| `static String readStringFromInputStream(InputStream is)` | 从 InputStream 读字符串 |
| `static byte[] readBytesFromInputStream(InputStream is)` | 从 InputStream 读字节数组 |
| `static BufferedImage readImageFile(String fileName)` | 读取图片文件 |

---

## FileSystemOpt

文件系统操作（抽象类）。

| 方法 | 描述 |
|------|------|
| `static List<File> findFilesByExt(String dir, String extName)` | 按扩展名查找文件 |
| `static List<File> findFiles(String dir, String s)` | 通配符查找文件 |
| `static void copyFile(String source, String target)` | 复制文件 |
| `static void deleteFile(File file)` | 删除文件 |
| `static void deleteDir(File dir)` | 删除目录（递归） |

---

## CsvFileIO

CSV 文件读写（抽象类）。

| 方法 | 描述 |
|------|------|
| `static List<Map<String,Object>> readDataFromInputStream(InputStream inputStream, boolean firstRowAsHeader, List<String> columnNames, String charsetType)` | 读取 CSV |
| `static void saveData2OutputStream(List<Map<String,Object>> listData, OutputStream outs, boolean firstRowAsHeader, List<String> columnNames, String charsetType)` | 写入 CSV |

---

## FileType

文件类型检测（基于文件头魔数，抽象类）。

| 方法 | 描述 |
|------|------|
| `static String getFileType(File file)` | 通过文件头检测文件类型 |
| `static String getFileType(InputStream is)` | 通过流检测文件类型 |
| `static String getFileExtName(String typeName)` | 类型名转扩展名 |

---

## FileMD5Maker

文件 MD5 校验（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String makeFileMD5(File file)` | 计算文件 MD5 |
| `static String makeFileMD5(InputStream is)` | 计算流的 MD5 |

---

## IniReader

INI 配置文件读取。

| 方法 | 描述 |
|------|------|
| 构造: `IniReader(String filename)` | 加载 INI 文件 |
| `String getValue(String section, String name)` | 获取 INI 值 |

---

## PropertiesReader

Properties 文件读取（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String getClassPathProperty(String fileName, String key)` | 读取 classpath 下属性 |
| `static String getFilePathProperty(String fileName, String key)` | 读取文件路径属性 |

---

## TxtLogFile

纯文本日志文件写入。

| 方法 | 描述 |
|------|------|
| `static void writeLog(String sLogFileName, String slog, boolean bNewLine, boolean bShowTime)` | 写日志（静态方法） |
| `boolean openLogFile(String sLogFileName)` | 打开日志文件 |
| `void writeLog(String slog)` | 写日志（成员方法） |
