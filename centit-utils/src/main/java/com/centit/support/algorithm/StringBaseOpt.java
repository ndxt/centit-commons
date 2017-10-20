package com.centit.support.algorithm;

import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.Map;
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
    private StringBaseOpt() {
        throw new IllegalAccessError("Utility class");
    }

    protected static final Logger logger = LoggerFactory.getLogger(StringBaseOpt.class);
    // ~ Static fields/initializers
    // =============================================

    //private final static Logger log = LoggerFactory.getLogger(StringBaseOpt.class);

    // ~ Methods
    // ================================================================
    /**
     * Encode a string using Base64 encoding. Used when storing passwords as
     * cookies.
     *
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
       * @param str
       *            待压缩的字符串
       * @return    返回压缩后的字符串
       * @throws IOException  IOException
       */
      public  static byte[] compress(String str) throws IOException {
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
       * @param str
       *            对字符串解压
       * @return    返回解压缩后的字符串
       * @throws IOException   IOException
       */
      public static String unCompress( byte[] str) throws IOException {
          if (null == str || str.length <= 0) {
              return "";
          }
          // 创建一个新的 byte 数组输出流
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          // 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
          ByteArrayInputStream in = new ByteArrayInputStream(str );//.getBytes("ISO-8859-1"));
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
     * @param objs 字符串数组
     * @return 返回 链接号的字符串
     */
    public static String concat(Object ...objs) {
        StringBuilder sb = new StringBuilder();
        if (objs !=null &&  objs.length > 0) {
            for (int i = 0; i < objs.length; i++) {
                if(objs[i]!=null) {
                    sb.append(StringBaseOpt.objectToString(objs[i]));
                }
            }
        }
        return sb.toString();
    }
    /**
     * 连接字符串 null 自动忽略 不会拼接一个 'null'
     * @param objs 字符串数组
     * @param str 连接 分隔符
     * @return 返回 链接号的字符串
     */
    public static String concat(Object objs[], String str) {
        StringBuilder sb = new StringBuilder();
        int objInd=0;
        if (objs !=null &&  objs.length > 0) {
            for (int i = 0; i < objs.length; i++) {
                if(objs[i]!=null) {
                    if (objInd > 0)
                        sb.append(str);
                    objInd++;
                    sb.append(StringBaseOpt.objectToString(objs[i]));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 连接字符串 null 自动忽略 不会拼接一个 'null'
     * @param objs 字符串数组
     * @param str 连接 分隔符
     * @return 返回 链接号的字符串
     */
    public static String concat(Collection<Object> objs, String str) {
        StringBuilder sb = new StringBuilder();
        if (objs !=null && objs.size() > 0) {
            int i=0;
            for (Object obj : objs) {
                if(obj !=null) {
                    if (i > 0)
                        sb.append(str);
                    i++;
                    sb.append(StringBaseOpt.objectToString(obj));
                }
            }
        }
        return sb.toString();
    }
    /**
     * @param strs strs
     * @param str str
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
     * @param str
     *            源串
     * @param quote
     *            待过滤串
     * @return  删除备份条件的后缀,如"value_CODE"过滤成"value"
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
     * @param str str
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
     * @param str  字符串
     * @return   如果输入的字符串为null返回""
     */
    public static String nvlAsBlank(String str) {
        return str==null?"":str;
    }
    /**
     * 用"0"填补string
     *
     * @param str 字符串
     * @param size size
     * @return 用"0"填补string
     */
    public static String fillZeroForString(String str, int size) {
        if (isNvl(str) ) {
            str =  "0";
        }
        if(size==0)
            return str;

        int sl = str.length();

        for (; sl< size ;sl++) {
            str = "0"+str;
        }
        return str;
    }

    /**
     * 用"0"填补string
     *
     * @param str str
     * @param size size
     * @return 用"0"填补string
     */
    public static String multiplyString(String str, int size) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<size;i++)
            sb.append(str);
        return sb.toString();
    }

    /**
     * 文号、档案号、规则生成算法，
     * @param templet 规则模板 $N16$表示生成 16位的流水号 左侧补零
     * @param currNo 流水号
     * @param params 用户自定义参数
     * @return 文号、档案号、规则生成算法，
     */
    public static String clacDocumentNo(String templet,long currNo,Map<String,String> params){
        if( StringRegularOpt.isNvl( templet) )
            return String.valueOf(currNo);

        String sDocNo = templet;
        if (sDocNo.contains("$N")) {
             int firstBegin = sDocNo.indexOf("$N");
             int firstEnd = firstBegin + 2;
             int secondBegin = sDocNo.indexOf("$", firstEnd);
             int nunber = 0;
             if(secondBegin>firstEnd )
                 nunber = Integer.parseInt(sDocNo.substring(firstEnd,
                         secondBegin));
             
             sDocNo = sDocNo.substring(0,  firstBegin) + 
                     fillZeroForString (String.valueOf(currNo), nunber)
                     + sDocNo.substring(secondBegin+1);
        }

        if(params !=null){
            for(Map.Entry<String,String> param : params.entrySet()){
                sDocNo = sDocNo.replaceAll("\\$"+param.getKey()+"\\$",
                        param.getValue());
            }
        }

        sDocNo = sDocNo.replaceAll("\\$year\\$",
                String.valueOf(DatetimeOpt.getYear( DatetimeOpt.currentUtilDate())));
        sDocNo = sDocNo.replaceAll("\\$Y2\\$",
                String.valueOf(DatetimeOpt.getYear( DatetimeOpt.currentUtilDate())).substring(2,4));
        
        return sDocNo;
    }

    /** 寻找比它大一个字符串 nextCode("0000200")=="0000201"
     * nextCode("000AZZZ")=="000BAAA"
     * @param sCode 一个字符串
     * @return 寻找比它大一个字符串
     */
    public static String nextCode( String sCode) {
        int nSL =  sCode.length();
        String sRes="";
        int i=nSL;
        while(i>0){
            i--;
            char c = sCode.charAt(i);
            if (c=='9') {
                sRes = '0' + sRes;
            }else if (c=='z') {
                sRes = 'a' + sRes;
            }else if (c=='Z') {
                sRes = 'A' + sRes;
            }else{
                c+=1;
                sRes = c + sRes;
                break;
            }
        }
        if(i>0)
            sRes = sCode.substring(0,i) + sRes;
        return sRes;
    }

    /*private static char convertFirstLetter(byte[] bytes) {

       char result = '-';
       int secPosvalue = 0;
       int i;
       for (i = 0; i < bytes.length; i++) {
        bytes[i] -= GB_SP_DIFF;
       }
       secPosvalue = bytes[0] * 100 + bytes[1];
       for (i = 0; i < 23; i++) {
        if (secPosvalue >= secPosvalueList[i]
          && secPosvalue < secPosvalueList[i + 1]) {
         result = firstLetter[i];
         break;
        }
       }
       return result;
    }*/

    /*
    * 获取一个汉字的拼音首字母。 GB码两个字节分别减去160，转换成10进制码组合就可以得到区位码
    * 例如汉字“你”的GB码是0xC4/0xE3，分别减去0xA0（160）就是0x24/0x43
    * 0x24转成10进制就是36，0x43是67，那么它的区位码就是3667，在对照表中读音为‘n’
    */
    public static String getFirstLetter(String oriStr) {


        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < oriStr.length(); i++) {
            String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(oriStr.charAt(i));
            if(pinyin != null && pinyin.length > 0) 
                sb.append(pinyin[0].charAt(0));
            else
                sb.append(oriStr.charAt(i));
        }

        return sb.toString();
    }

    public static String readFileToBuffer( String sFileName)
    {
        /* 一行一行的读出
        StringBuffer buffer = new StringBuffer();
        String line; // 用来保存每行读取的内容
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(sFileName));

            line = reader.readLine(); // 读取第一行
            while (line != null) { // 如果 line 为空说明读完了
                buffer.append(line); // 将读到的内容添加到 buffer 中
                buffer.append("\r\n"); // 添加换行符
                line = reader.readLine(); // 读取下一行
            }
            return buffer.toString();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }
        */

        //一次性全部读出
        try(FileInputStream in= new FileInputStream(sFileName)) {
            byte[] readBytes = new byte[in.available()];
            int l = in.read(readBytes);
            if(l>0)
                return new String(readBytes);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }
        return null;
    }

    public static String readJarResourceToBuffer(Class<?> clazz, String sResourceName)
    {
        //一次性全部读出
        StringBuilder buffer = new StringBuilder();
        String line; // 用来保存每行读取的内容
        InputStream in;
        try {
            //URL fileURL=clazz.getResource(sResourceName);
            //log.debug(fileURL.getFile());
            in = clazz.getResourceAsStream(sResourceName);

            BufferedReader br=new BufferedReader(new InputStreamReader(in));
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
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }
        return null;
    }

    public static String objectToString(Object objValue){
        if(objValue==null)
            return null;
        if(objValue instanceof String){
            return (String)objValue;
        }
        if(objValue instanceof Object[]){
            Object [] objs=(Object[]) objValue;
            StringBuilder sb = new StringBuilder();
            if(objs.length>0){
                for(int i=0;i<objs.length;i++){
                    if(i>0)
                        sb.append(',');
                    if(objs[i]!=null){
                        if(objs[i] instanceof java.util.Date)
                            sb.append(DatetimeOpt.convertDatetimeToString((java.util.Date) objs[i]));
                        /*else if(objs[i] instanceof java.sql.Date)
                            sb.append(DatetimeOpt.convertDatetimeToString(
                                    DatetimeOpt.convertUtilDate(
                                            (java.sql.Date) objs[i])));
                        */else
                            sb.append(objs[i].toString());
                    }
                }
                return sb.toString();
            }else{
              return null;
            }
        }else if(objValue instanceof Collection){
            StringBuilder sb = new StringBuilder();
            int vc = 0;
            Collection<?> valueList = (Collection<?> )objValue;
            for(Object ov : valueList){
                if(ov!=null){
                    if(vc>0)
                        sb.append(",");
                    if(ov instanceof java.util.Date)
                        sb.append(DatetimeOpt.convertDatetimeToString((java.util.Date) ov));
                    /*else if(ov instanceof java.sql.Date)
                        sb.append(DatetimeOpt.convertDatetimeToString(
                                DatetimeOpt.convertUtilDate(
                                        (java.sql.Date) ov)));*/
                    else
                        sb.append(ov.toString());
                    vc++;
                }
            }
            return sb.toString();
        }else if(objValue instanceof java.util.Date){
            return DatetimeOpt.convertDatetimeToString((java.util.Date) objValue);
        }/*else if(objValue instanceof java.sql.Date){
            return DatetimeOpt.convertDatetimeToString(
                    DatetimeOpt.convertUtilDate(
                    (java.sql.Date) objValue));
        }*/else
            return objValue.toString();
    }

    public static String castObjectToString(Object obj){
        return objectToString(obj);
    }

    public static String castObjectToString(Object obj,String defaultValue){
        return GeneralAlgorithm.nvl(castObjectToString(obj),defaultValue);
    }

}
