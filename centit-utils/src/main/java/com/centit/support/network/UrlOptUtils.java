package com.centit.support.network;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.file.FileIOOpt;
import com.centit.support.security.Md5Encoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class UrlOptUtils {
    protected static final Logger logger = LoggerFactory.getLogger(UrlOptUtils.class);

    private UrlOptUtils() {
        throw new IllegalAccessError("Utility class");
    }
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

    public static Map<String, String> splitUrlParamter(
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
                    value = URLDecoder.decode(value, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
                }
                params.put(name, value);
                break;
            } else {
                String value = szUrlParameter.substring(n + 1, n2);
                try {
                    value = URLDecoder.decode(value, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
                }
                params.put(name, value);
                bpos = n2 + 1;
            }
        }
        return params;
    }

    /**
     * 根据URL 获取域名
     *
     * @param curl url
     * @return 返回域名
     */
    public static String getUrlDomain(String curl) {
        try {
            return new URL(curl).getHost();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String fetchFilenameFromUrl(String curl) {
        int nBpos = curl.lastIndexOf('/') + 1;
        int nEpos = curl.indexOf('?');
        if(nEpos<0)
            return curl.substring(nBpos);
        return curl.substring(nBpos, nEpos);
    }

    public static String makeParamsToUrl(Map<String, Object> queryParam) {
        StringBuilder urlBuilder = new StringBuilder();
        int n = 0;
        for (Map.Entry<String, Object> ent : queryParam.entrySet()) {
            if (n > 0)
                urlBuilder.append('&');
            n++;
            urlBuilder.append(ent.getKey()).append('=').append(
                urlEncode(//urlEncodeShareNotDuplicate(
                    StringBaseOpt.castObjectToString(ent.getValue(), ""))
            );
        }
        return urlBuilder.toString();
    }

    public static String appendParamToUrl(String uri, String queryUrl) {
        if (StringUtils.isBlank(queryUrl))
            return uri;
        return (uri.endsWith("?") || uri.endsWith("&")) ? uri + queryUrl :
            (uri.indexOf('?') == -1 ? uri + '?' + queryUrl : uri + '&' + queryUrl);
    }

    public static String appendParamsToUrl(String uri, Map<String, Object> queryParam) {
        if (queryParam == null || queryParam.isEmpty()) {
            return uri;
        }
        return appendParamToUrl(uri, makeParamsToUrl(queryParam));
    }

    public static String urlDecode(String urlParam){
        if(StringUtils.isBlank(urlParam))
            return urlParam;
        try {
            return URLDecoder.decode(urlParam, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return urlParam;
        }
    }

    public static String urlEncode(String urlParam){
        if(StringUtils.isBlank(urlParam))
            return urlParam;
        try {
            return URLEncoder.encode(urlParam,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return urlParam;
        }
    }

    public static String appendParamToUrl(String uri, String paramName, Object paramValue) {
        return (uri.endsWith("?") || uri.endsWith("&")) ?
            uri + paramName + "=" + urlEncode(StringBaseOpt.castObjectToString(paramValue, "")):
            uri + (uri.indexOf('?') == -1 ? '?' : '&')
                + paramName + "=" +  urlEncode(//urlEncodeShareNotDuplicate(//StringEscapeUtils.escapeHtml4(
                StringBaseOpt.castObjectToString(paramValue, ""));
    }

    /**
     * 简化的url压缩算法，算法如下：
     * 1. 对Url进行md5编码
     * 2. 对md5码进行base64编码，长度为22
     * 3. 剔除base64码中的‘+’和‘/’， 取前面的一段，
     * 4. 如果位数不够，用base64码加上url再进行一次md5，用这个补齐，
     * 5. 循环4直到位数满足短码的长度需求
     * 说明一般短码的长度在6～10之间，一次就可以了。解决冲突的方法也简单，可以取长一点，比如目标是8位，可以取16位，如果发现0～7冲突，就取1～8 以此类推。
     *
     * @param longUrl   原始url
     * @param urlLength 输出url长度
     * @return 压缩后的rul
     */

    public static String shortenCodeUrl(String longUrl, int urlLength) {
        if (urlLength < 4) {
            urlLength = 8;// defalut length
        }
        StringBuilder sbBuilder = new StringBuilder(urlLength + 2);
        String md5Hex = "";
        int nLen = 0;
        while (nLen < urlLength) {
            md5Hex = Md5Encoder.encodeBase64(md5Hex + longUrl, true);
            int md5Len = md5Hex.length();
            int copylen = md5Len < urlLength - nLen ? md5Len : urlLength - nLen;
            sbBuilder.append(md5Hex, 0, copylen);
            nLen += copylen;
            if (nLen == urlLength) {
                break;
            }
        }
        return sbBuilder.toString();
    }

    public static String objectToUrlString(Object objValue) {
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
                    String objStr = objectToUrlString(Array.get(objValue, i));
                    if (i > 0) {
                        if(objStr.indexOf('=')>=0)
                            sb.append('&');
                        else
                            sb.append(',');
                    }
                    sb.append(objStr);
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
                    String objStr = objectToUrlString(ov);
                    if (vc > 0) {
                        if(objStr.indexOf('=')>=0)
                            sb.append('&');
                        else
                            sb.append(',');
                    }
                    sb.append(objStr);
                    vc++;
                }
            }
            return sb.toString();
        } else {
            Object object = JSON.toJSON(objValue);
            if(object instanceof JSONObject){
                return makeParamsToUrl((JSONObject)object);
            }else{
                return JSON.toJSONString(object);
            }
        }
    }

}
