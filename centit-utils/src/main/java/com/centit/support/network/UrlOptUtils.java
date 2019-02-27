package com.centit.support.network;

import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.security.Md5Encoder;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class UrlOptUtils {
    private UrlOptUtils() {
        throw new IllegalAccessError("Utility class");
    }

    protected static final Logger logger = LoggerFactory.getLogger(UrlOptUtils.class);
    /*private static final String ALLOWED_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";*/

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
        if(queryParam == null) {
          return uri;
        }
        StringBuilder urlBuilder = new StringBuilder(uri);
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
        return urlBuilder.toString();
    }

    public static String appendParamToUrl(String uri, String queryUrl){
        if (queryUrl == null || "".equals(queryUrl))
            return uri;
        return (uri.endsWith("?") || uri.endsWith("&")) ? uri + queryUrl :
                (uri.indexOf('?') == -1 ?  uri+'?'+queryUrl :  uri+'&'+queryUrl );
    }

        public static String appendParamToUrl(String uri, String paramName, Object paramValue){
            return (uri.endsWith("?") || uri.endsWith("&")) ?
                    uri + paramName +"="+ StringBaseOpt.objectToString(paramValue):
                    uri + (uri.indexOf('?') == -1 ? '?':'&')
                            + paramName +"="+ StringEscapeUtils.escapeHtml4(
                                    StringBaseOpt.objectToString(paramValue));
        }

    /**
     * 简化的url压缩算法，算法如下：
     * 1. 对Url进行md5编码
     * 2. 对md5码进行base64编码，长度为22
     * 3. 剔除base64码中的‘+’和‘/’， 取前面的一段，
     * 4. 如果位数不够，用base64码加上url再进行一次md5，用这个补齐，
     * 5. 循环4直到位数满足短码的长度需求
     * 说明一般短码的长度在6～10之间，一次就可以了。解决冲突的方法也简单，可以取长一点，比如目标是8位，可以取16位，如果发现0～7冲突，就取1～8 以此类推。
     * @param longUrl 原始url
     * @param urlLength 输出url长度
     * @return 压缩后的rul
     */

    public static String shortenCodeUrl(String longUrl, int urlLength) {
        if (urlLength < 4 ) {
            urlLength = 8;// defalut length
        }
        StringBuilder sbBuilder = new StringBuilder(urlLength + 2);
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
