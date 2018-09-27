package com.centit.support.network;

import com.centit.support.algorithm.ByteBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.security.Md5Encoder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class UrlOptUtils {
    private UrlOptUtils() {
        throw new IllegalAccessError("Utility class");
    }

    protected static final Logger logger = LoggerFactory.getLogger(UrlOptUtils.class);
    private static final String ALLOWED_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";

    public static final String getUrlParamter(String szUrl) {
        String sQuery;
        try {
            java.net.URL url = new java.net.URL(szUrl);
            sQuery = url.getQuery();
        } catch (MalformedURLException e) {
            int n = szUrl.indexOf('?');
            int nM = szUrl.lastIndexOf('#');
            if (nM > 0 && nM > n) {
                if (n > 0)
                    sQuery = szUrl.substring(n + 1, nM);
                else
                    sQuery = szUrl.substring(0, nM);
            } else if (n > 0)
                sQuery = szUrl.substring(n + 1);
            else
                sQuery = "";
        }
        return sQuery;
    }

    public static final Map<String, String> splitUrlParamter(
            String szUrlParameter) {
        Map<String, String> params = new HashMap<>();
        int bpos = 0;
        while (true) {
            int n = szUrlParameter.indexOf('=', bpos);
            if (n < 0)
                break;
            String name = szUrlParameter.substring(bpos, n);
            int n2 = szUrlParameter.indexOf('&', n + 1);
            if (n2 < 0) {
                String value = szUrlParameter.substring(n + 1);
                try {
                    value = java.net.URLDecoder.decode(value, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage(),e);//e.printStackTrace();
                }
                params.put(name, value);
                break;
            } else {
                String value = szUrlParameter.substring(n + 1, n2);
                try {
                    value = java.net.URLDecoder.decode(value, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage(),e);//e.printStackTrace();
                }
                params.put(name, value);
                bpos = n2 + 1;
            }
        }
        return params;
    }


    public static String encodeURIComponent(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }

        int l = input.length();
        StringBuilder o = new StringBuilder(l * 3);
        try {
            for (int i = 0; i < l; i++) {
                String e = input.substring(i, i + 1);
                if (ALLOWED_CHARS.indexOf(e) == -1) {
                    byte[] b = e.getBytes("utf-8");
                    o.append(getHex(b));
                    continue;
                }
                o.append(e);
            }
            return o.toString();
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
        }
        return input;
    }

    private static String getHex(byte buf[]) {
        StringBuilder o = new StringBuilder(buf.length * 3);
        for (int i = 0; i < buf.length; i++) {
            int n = (int) buf[i] & 0xff;
            o.append("%");
            if (n < 0x10) {
                o.append("0");
            }
            o.append(Long.toString(n, 16).toUpperCase());
        }
        return o.toString();
    }

    /**
     * 根据URL 获取域名
     * @param curl url
     * @return 返回域名
     */
    public static String getUrlDomain(String curl){
        try{
            return new URL(curl).getHost();
        }catch(Exception e){
            logger.error(e.getMessage(),e);//e.printStackTrace();
            return null;
        }
    }

    public static String appendParamsToUrl(String uri, Map<String,Object> queryParam){
        StringBuilder urlBuilder = new StringBuilder(uri);
        if(queryParam!=null){
            if(!uri.endsWith("?") && !uri.endsWith("&")){
                if(uri.indexOf('?') == -1 )
                    urlBuilder.append('?');
                else
                    urlBuilder.append('&');
            }
            int n=0;
            for(Map.Entry<String,Object> ent : queryParam.entrySet() ){
                if(n>0)
                    urlBuilder.append('&');
                n++;
                urlBuilder.append(ent.getKey()).append('=').append(
                        StringEscapeUtils.escapeHtml4(
                                StringBaseOpt.objectToString(ent.getValue()))
                );
            }
        }
        return urlBuilder.toString();
    }

    public static String appendParamToUrl(String uri, String queryParam){
        if (queryParam == null || "".equals(queryParam))
            return uri;
        return (uri.endsWith("?") || uri.endsWith("&")) ? uri + queryParam :
                (uri.indexOf('?') == -1 ?  uri+'?'+queryParam :  uri+'&'+queryParam );
    }

    public static String appendParamToUrl(String uri, String paramName, Object paramValue){
        return (uri.endsWith("?") || uri.endsWith("&")) ?
                uri + paramName +"="+ StringBaseOpt.objectToString(paramValue):
                uri + (uri.indexOf('?') == -1 ? '?':'&')
                        + paramName +"="+ StringBaseOpt.objectToString(paramValue);
    }

    /*
     * 这个没有实际意义，不如直接用uuid
     * 简化的url压缩算法，对url进行md5映射，得到16个byte的编码，然后base64编码得到22个字符
     * 网络上的算法可以压缩到6～8个字符，效果比这个好，但是算法比较复杂
     * @param uri 原始url
     * @return 压缩后的rul
     */
    /*public static String shortCodeUrl(String uri){
        MessageDigest MD5;
        try {
            MD5 = MessageDigest.getInstance("MD5");
            MD5.update( uri.getBytes("utf8"), 0, uri.length());
            //将 + 替换成 - / 替换成 _
            //换成url非保留字符
            byte [] md5Code = Base64.encodeBase64(MD5.digest());
            for(int i=0;i<24;i++){
                if(md5Code[i] == '+'){
                    md5Code[i] = '-';
                }else if(md5Code[i] == '/'){
                    md5Code[i] = '_';
                }
            }
            return new String(md5Code,0,22);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException  e) {
            logger.error(e.getMessage(),e);//e.printStackTrace();
            return null;
        }
    }*/

    public static String shortenCodeUrl(String longUrl, int urlLength) {
        if (urlLength < 0 ) {
            urlLength = 8;// defalut length
        }
        StringBuilder sbBuilder = new StringBuilder(24);
        String md5Hex = "";
        int nLen = 0;
        while (nLen < urlLength) {
            md5Hex = Md5Encoder.encodeBase64(md5Hex + longUrl);
            for(int i=0;i<md5Hex.length();i++){
                char c = md5Hex.charAt(i);
                if(c != '/' && c != '+'){
                    sbBuilder.append(c);
                    nLen ++;
                }
                if(nLen == urlLength){
                    break;
                }
            }
        }
        return sbBuilder.toString();
    }
}
