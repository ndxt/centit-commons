package com.centit.support.algorithm;

import com.alibaba.fastjson2.JSON;
import com.centit.support.file.FileIOOpt;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * String Utility Class This is used to encode passwords programmatically
 *
 * <p>
 * <a href="StringUtil.java.html"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @author 杨淮生
 */
@SuppressWarnings("unused")
public abstract class StringBaseOpt {
    protected static final Logger logger = LoggerFactory.getLogger(StringBaseOpt.class);
    private final static int[] li_SecPosValue = {1601, 1637, 1833, 2078, 2274,
        2302, 2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858,
        4027, 4086, 4390, 4558, 4684, 4925, 5249, 5590};
    // ~ Static fields/initializers
    // =============================================

    //private final static Logger log = LoggerFactory.getLogger(StringBaseOpt.class);

    // ~ Methods
    // ================================================================
    private final static String[] lc_FirstLetter = {"a", "b", "c", "d", "e",
        "f", "g", "h", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
        "t", "w", "x", "y", "z"};

    private StringBaseOpt() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Encode a string using Base64 encoding. Used when storing passwords as
     * cookies.
     * <p>
     * This is weak encoding in that anyone can use the decodeString routine to
     * reverse the encoding.
     *
     * @param str str
     * @return String
     */
    public static String encodeBase64(String str) {
        //sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        //return encoder.encodeBuffer(str.getBytes()).trim();
        return new String(Base64.encodeBase64(str.getBytes()));
    }

    /**
     * Decode a string using Base64 encoding.
     *
     * @param str String
     * @return String
     */
    public static String decodeBase64(String str) {
        //sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
        return new String(Base64.decodeBase64(str.getBytes()));
    }

    /**
     * 字符串的压缩
     *
     * @param str 待压缩的字符串
     * @return 返回压缩后的字符串
     * @throws IOException IOException
     */
    public static byte[] compress(String str) throws IOException {
        if (null == str || str.length() <= 0) {
            return null;
        }
        // 创建一个新的 byte 数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 使用默认缓冲区大小创建新的输出流
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        // 将 b.length 个字节写入此输出流
        gzip.write(str.getBytes());
        gzip.close();
        // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
        return out.toByteArray();// .toString("ISO-8859-1");
    }

    /**
     * 字符串的解压
     *
     * @param str 对字符串解压
     * @return 返回解压缩后的字符串
     * @throws IOException IOException
     */
    public static String unCompress(byte[] str) throws IOException {
        if (null == str || str.length <= 0) {
            return "";
        }
        // 创建一个新的 byte 数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
        ByteArrayInputStream in = new ByteArrayInputStream(str);//.getBytes("ISO-8859-1"));
        // 使用默认缓冲区大小创建新的输入流
        GZIPInputStream gzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gzip.read(buffer)) >= 0) {// 将未压缩数据读入字节数组
            // 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte数组输出流
            out.write(buffer, 0, n);
        }
        // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
        return out.toString("utf-8");
    }

    /**
     * 连接字符串 null 自动忽略 不会拼接一个 'null'
     *
     * @param objs 字符串数组
     * @return 返回 链接号的字符串
     * @see org.apache.commons.lang3.StringUtils join 方法
     * 尽量用 StringUtils 的方法
     */
    public static String concat(Object... objs) {
        StringBuilder sb = new StringBuilder();
        if (objs != null && objs.length > 0) {
            for (int i = 0; i < objs.length; i++) {
                if (objs[i] != null) {
                    sb.append(StringBaseOpt.objectToString(objs[i]));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 连接字符串 null 自动忽略 不会拼接一个 'null'
     *
     * @param objs      字符串数组
     * @param separator 连接 分隔符
     * @return 返回 链接号的字符串
     * @see org.apache.commons.lang3.StringUtils join 方法
     * 尽量用 StringUtils 的方法
     */
    public static String concat(Object objs[], String separator) {
        StringBuilder sb = new StringBuilder();
        int objInd = 0;
        if (objs != null && objs.length > 0) {
            for (int i = 0; i < objs.length; i++) {
                if (objs[i] != null) {
                    if (objInd > 0)
                        sb.append(separator);
                    objInd++;
                    sb.append(StringBaseOpt.objectToString(objs[i]));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 连接字符串 null 自动忽略 不会拼接一个 'null'
     *
     * @param objs      字符串数组
     * @param separator 连接 分隔符
     * @return 返回 链接号的字符串
     * @see org.apache.commons.lang3.StringUtils join 方法
     * 尽量用 StringUtils 的方法
     */
    public static String concat(Collection<Object> objs, String separator) {
        StringBuilder sb = new StringBuilder();
        if (objs != null && objs.size() > 0) {
            int i = 0;
            for (Object obj : objs) {
                if (obj != null) {
                    if (i > 0)
                        sb.append(separator);
                    i++;
                    sb.append(StringBaseOpt.objectToString(obj));
                }
            }
        }
        return sb.toString();
    }

    /**
     * @param strs strs
     * @param str  str
     * @return 如果字符串str在数组strs返回true
     */
    public static boolean contains(String strs[], String str) {

        if (strs == null)
            return false;

        for (String str1 : strs) {
            if (str1.contains(str))
                return true;
        }
        return false;
    }

    /**
     * copyProperties(),删除备份条件的后缀,如"value_CODE"过滤成"value"
     *
     * @param str   源串
     * @param quote 待过滤串
     * @return 删除备份条件的后缀, 如"value_CODE"过滤成"value"
     */
    public static String deleteStringByQuote(String str, String quote) {
        if (null == str || "".equals(str)) {
            return "";
        }
        return StringUtils.replace(str.trim(), "_" + quote, "");
    }

    /**
     * 返回字符串在数组中的第一次出现的位置,找不到返回-1
     *
     * @param strs strs
     * @param str  str
     * @return int
     */
    public static int indexOf(String strs[], String str) {
        int index = -1;
        if (null != strs) {
            for (int i = 0; i < strs.length; i++)
                if (strs[i].contains(str)) {
                    index = i;
                    break;
                }
        }
        return index;
    }

    /**
     * 判断字符串是否为空(null || ""),是：true,否：false
     *
     * @param str 判断字符串是否为空
     * @return boolean
     */
    public static boolean isNvl(String str) {
        return StringUtils.isBlank(str);
        //return str == null || "".equals(str.trim());
    }

    /**
     * 如果输入的字符串为null返回""
     *
     * @param str 字符串
     * @return 如果输入的字符串为null返回""
     */
    public static String nvlAsBlank(String str) {
        return str == null ? "" : str;
    }

    /**
     * 如果字符串str为null返回""，返回 emptyValue 否则返回 str
     *
     * @param str        字符串
     * @param emptyValue 字符串
     * @return 如果输入的字符串为null返回""
     */
    public static String emptyValue(String str, String emptyValue) {
        return StringUtils.isBlank(str) ? emptyValue : str;
    }

    /**
     * 用"0"填补string
     *
     * @param str  字符串
     * @param size size
     * @return 用"0"填补string
     */
    public static String fillZeroForString(String str, int size) {
        return StringUtils.leftPad(str, size, '0');
    }

    /**
     * 在中间添加 字符
     *
     * @param str     字符串
     * @param size    pad后长度
     * @param prefix  前缀
     * @param padChar 添加的字符
     * @return 返回值
     */
    public static String midPad(String str, int size, String prefix, char padChar) {
        return prefix == null ?
            StringUtils.leftPad(str, size, padChar) :
            prefix + StringUtils.leftPad(str, size - prefix.length(), padChar);
    }

    public static String midPad(String str, int size, String prefix, String padChar) {
        return prefix == null ?
            StringUtils.leftPad(str, size, padChar) :
            prefix + StringUtils.leftPad(str, size - prefix.length(), padChar);
    }

    /**
     * 用"0"填补string
     *
     * @param str  str
     * @param size size
     * @return 用"0"填补string
     */
    public static String multiplyString(String str, int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++)
            sb.append(str);
        return sb.toString();
    }

    /**
     * 文号、档案号、规则生成算法，
     *
     * @param templet 规则模板 $N16$表示生成 16位的流水号 左侧补零
     * @param currNo  流水号
     * @param params  用户自定义参数
     * @return 文号、档案号、规则生成算法，
     * 参见类 com.centit.support.compiler.Pretreatment 的 mapTemplateString 方法
     */
    @Deprecated
    public static String clacDocumentNo(String templet, long currNo, Map<String, String> params) {
        if (StringRegularOpt.isNvl(templet))
            return String.valueOf(currNo);

        String sDocNo = templet;
        if (sDocNo.contains("$N")) {
            int firstBegin = sDocNo.indexOf("$N");
            int firstEnd = firstBegin + 2;
            int secondBegin = sDocNo.indexOf("$", firstEnd);
            int nunber = 0;
            if (secondBegin > firstEnd)
                nunber = Integer.parseInt(sDocNo.substring(firstEnd,
                    secondBegin));

            sDocNo = sDocNo.substring(0, firstBegin) +
                fillZeroForString(String.valueOf(currNo), nunber)
                + sDocNo.substring(secondBegin + 1);
        }

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                sDocNo = sDocNo.replaceAll("\\$" + param.getKey() + "\\$",
                    param.getValue());
            }
        }

        sDocNo = sDocNo.replaceAll("\\$year\\$",
            String.valueOf(DatetimeOpt.getYear(DatetimeOpt.currentUtilDate())));
        sDocNo = sDocNo.replaceAll("\\$Y2\\$",
            String.valueOf(DatetimeOpt.getYear(DatetimeOpt.currentUtilDate())).substring(2, 4));

        return sDocNo;
    }

    /**
     * 寻找比它大一个字符串 nextCode("0000200")=="0000201"
     * nextCode("000AZZZ")=="000BAAA"
     *
     * @param sCode 一个字符串
     * @return 寻找比它大一个字符串
     */
    public static String nextCode(String sCode) {
        int nSL = sCode.length();
        String sRes = "";
        int i = nSL;
        while (i > 0) {
            i--;
            char c = sCode.charAt(i);
            if (c == '9') {
                sRes = '0' + sRes;
            } else if (c == 'z') {
                sRes = 'a' + sRes;
            } else if (c == 'Z') {
                sRes = 'A' + sRes;
            } else {
                c += 1;
                sRes = c + sRes;
                break;
            }
        }
        if (i > 0)
            sRes = sCode.substring(0, i) + sRes;
        return sRes;
    }

    private static char getPinYinShenMu(char ch) {
        String[] res1 = PinyinHelper.toHanyuPinyinStringArray(ch);
        if (res1.length > 0) {
            return res1[0].charAt(0);
        }
        return ' ';
    }
    /*
    private static String getPinYin(String hanzi) {
        try {
            String chinese = new String(hanzi.getBytes("GB2312"), "ISO8859-1");
            if (chinese.length() > 1) {
                int li_SectorCode = (int) chinese.charAt(0); // 汉字区码
                int li_PositionCode = (int) chinese.charAt(1); // 汉字位码
                li_SectorCode = li_SectorCode - 160;
                li_PositionCode = li_PositionCode - 160;
                int li_SecPosCode = li_SectorCode * 100 + li_PositionCode; // 汉字区位码
                if (li_SecPosCode > 1600 && li_SecPosCode < 5590) {
                    for (int i = 0; i < 23; i++) {
                        if (li_SecPosCode >= li_SecPosValue[i]
                            && li_SecPosCode < li_SecPosValue[i + 1]) {
                            chinese = lc_FirstLetter[i];
                            break;
                        }
                    }
                }
                return chinese;
            } else {
                return hanzi;
            }
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }*/

    /**
     * 获取一个汉字的拼音首字母。 GB码两个字节分别减去160，转换成10进制码组合就可以得到区位码
     * 例如汉字“你”的GB码是0xC4/0xE3，分别减去0xA0（160）就是0x24/0x43
     * 0x24转成10进制就是36，0x43是67，那么它的区位码就是3667，在对照表中读音为‘n’
     *
     * @param oriStr 输入字符串
     * @return 输出的首字母
     */
    public static String getFirstLetter(String oriStr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < oriStr.length(); i++) {
            //String pinyin = getPinYin(String.valueOf(oriStr.charAt(i)));
            sb.append(getPinYinShenMu(oriStr.charAt(i)));
        }
        return sb.toString();
    }

    public static String readFileToBuffer(String sFileName) {
        //一次性全部读出
        try (FileInputStream in = new FileInputStream(sFileName)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            byte[] readBytes = out.toByteArray();
            return new String(readBytes);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        return null;
    }

    public static String readJarResourceToBuffer(Class<?> clazz, String sResourceName) {
        //一次性全部读出
        StringBuilder buffer = new StringBuilder();
        String line; // 用来保存每行读取的内容
        InputStream in;
        try {
            //URL fileURL=clazz.getResource(sResourceName);
            //log.debug(fileURL.getFile());
            in = clazz.getResourceAsStream(sResourceName);

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            line = br.readLine(); // 读取第一行
            while (line != null) { // 如果 line 为空说明读完了
                buffer.append(line); // 将读到的内容添加到 buffer 中
                buffer.append("\r\n"); // 添加换行符
                line = br.readLine(); // 读取下一行
            }
            br.close();
            in.close();
            return buffer.toString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        return null;
    }

    /**
     * 浅层次的 非递归
     *
     * @param objValue 对象
     * @return 字符串
     */
    public static String objectToString(Object objValue) {
        if (objValue == null)
            return null;
        if (objValue instanceof String) {
            return (String) objValue;
        }
        if (objValue instanceof byte[]) {
            return new String((byte[]) objValue);
        }
        if (objValue instanceof java.util.Date){
            return DatetimeOpt.convertTimestampToString((java.util.Date) objValue);
        }
        if (objValue instanceof InputStream){
            try {
                return FileIOOpt.readStringFromInputStream((InputStream)objValue);
            } catch (IOException e) {
                return "";
            }
        }
        Class<?> clazz = objValue.getClass();

        if (clazz.isEnum()) {
            return ((Enum<?>) objValue).name();
        }

        if (ReflectionOpt.isScalarType(clazz)){
            return objValue.toString();
        }

        if (clazz.isArray()) {
            int len = Array.getLength(objValue);
            StringBuilder sb = new StringBuilder();
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    if (i > 0)
                        sb.append(',');
                    sb.append(objectToString(Array.get(objValue, i)));
                }
                return sb.toString();
            } else {
                return null;
            }
        } else if (objValue instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            int vc = 0;
            Collection<?> valueList = (Collection<?>) objValue;
            for (Object ov : valueList) {
                if (ov != null) {
                    if (vc > 0)
                        sb.append(",");
                    sb.append(objectToString(ov));
                    vc++;
                }
            }
            return sb.toString();
        } else {
            return JSON.toJSONString(objValue);
        }
    }

    public static String[] objectToStringArray(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof byte[]) {
            String[] stringList = new String[1];
            stringList[0] = StringBaseOpt.castObjectToString(object);
            return stringList;
        } else if (object.getClass().isArray()) {
            int len = Array.getLength(object);
            String[] stringList = new String[len];
            for (int i = 0; i < len; i++) {
                Object obj = Array.get(object, i);
                stringList[i++] = StringBaseOpt.castObjectToString(obj);
            }
            return stringList;
        } else if (object instanceof Collection) {
            String[] stringList = new String[((Collection<?>) object).size()];
            int i = 0;
            for (Object po : (Collection<?>) object) {
                stringList[i++] = StringBaseOpt.castObjectToString(po);
            }
            return stringList;
        } else if (object instanceof String) {
            return ((String) object).split(",");
        }
        String[] stringList = new String[1];
        stringList[0] = StringBaseOpt.castObjectToString(object);
        return stringList;
        //}
    }

    /**
     * 将对象转换为 string list
     *
     * @param object 对象
     * @return stringList
     */
    public static List<String> objectToStringList(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof byte[]) {
            List<String> stringList = new ArrayList<>(1);
            stringList.add(StringBaseOpt.castObjectToString(object));
            return stringList;
        } else if (object.getClass().isArray()) {
            int len = Array.getLength(object);
            List<String> stringList = new ArrayList<>(len + 1);
            for (int i = 0; i < len; i++) {
                Object obj = Array.get(object, i);
                stringList.add(StringBaseOpt.castObjectToString(obj));
            }
            return stringList;
        } else if (object instanceof Collection) {
            List<String> stringList = new ArrayList<>(((Collection<?>) object).size() + 1);
            for (Object po : (Collection<?>) object) {
                stringList.add(StringBaseOpt.castObjectToString(po));
            }
            return stringList;
        } else if (object instanceof String) {
            String[] ss = ((String) object).split(",");
            List<String> stringList = new ArrayList<>(ss.length);
            stringList.addAll(Arrays.asList(ss));
            return stringList;
        }

        List<String> stringList = new ArrayList<>(1);
        stringList.add(StringBaseOpt.castObjectToString(object));
        return stringList;
    }

    /**
     * 将对象转换为 string list
     *
     * @param object 对象
     * @return stringList
     */
    public static Set<String> objectToStringSet(Object object) {
        if (object == null) {
            return null;
        } else if (object.getClass().isArray()) {
            int len = Array.getLength(object);
            Set<String> stringSet = new HashSet<>(len + 1);
            for (int i = 0; i < len; i++) {
                Object obj = Array.get(object, i);
                stringSet.add(StringBaseOpt.castObjectToString(obj));
            }
            return stringSet;
        } else if (object instanceof Collection) {
            Set<String> stringSet = new HashSet<>(((Collection<?>) object).size() + 1);
            for (Object po : (Collection<?>) object) {
                stringSet.add(StringBaseOpt.castObjectToString(po));
            }
            return stringSet;
        } else if (object instanceof String) {
            String[] ss = ((String) object).split(",");
            Set<String> stringList = new HashSet<>(ss.length);
            for (String s : ss) {
                stringList.add(s);
            }
            return stringList;
        }
        Set<String> stringSet = new HashSet<>(1);
        stringSet.add(StringBaseOpt.castObjectToString(object));
        return stringSet;
    }

    public static Map<String, Set<String>> objectToMapStrSet(Object object) {
        Map<String, Object> objMap = CollectionsOpt.objectToMap(object);
        Map<String, Set<String>> strMap = new HashMap<>(objMap.size() + 1);
        for (Map.Entry<String, Object> ent : objMap.entrySet()) {
            strMap.put(ent.getKey(), objectToStringSet(ent.getValue()));
        }
        return strMap;
    }

    public static Map<String, List<String>> objectToMapStrArray(Object object) {
        Map<String, Object> objMap = CollectionsOpt.objectToMap(object);
        Map<String, List<String>> strMap = new HashMap<>(objMap.size() + 1);
        for (Map.Entry<String, Object> ent : objMap.entrySet()) {
            strMap.put(ent.getKey(), objectToStringList(ent.getValue()));
        }
        return strMap;
    }

    public static String castObjectToString(Object obj) {
        return objectToString(obj);
    }

    public static String castObjectToString(Object obj, String defaultValue) {
        return GeneralAlgorithm.nvl(objectToString(obj), defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> T stringToScalarData(String sdata, Class<T> clazz) {
        if (StringUtils.isBlank(sdata))
            return null;
        if (clazz == java.lang.Integer.class || clazz == int.class) {
            return (T) java.lang.Integer.valueOf(sdata);
        } else if (clazz == java.lang.Long.class || clazz == long.class) {
            return (T) java.lang.Long.valueOf(sdata);
        } else if (clazz == java.lang.Double.class || clazz == double.class) {
            return (T) java.lang.Double.valueOf(sdata);
        } else if (clazz == java.lang.Boolean.class || clazz == boolean.class) {
            return (T) java.lang.Boolean.valueOf(sdata);
        } else if (clazz == java.lang.Float.class || clazz == float.class) {
            return (T) java.lang.Float.valueOf(sdata);
        } else if (clazz == java.lang.String.class) {
            return (T) sdata;
        } else if (clazz == java.util.Date.class) {
            return (T) DatetimeOpt.smartPraseDate(sdata);
        } else if (clazz == java.util.UUID.class) {
            return (T) java.util.UUID.fromString(sdata);
        } else
            return null;
    }
}
