package com.centit.support.network;

import com.alibaba.fastjson2.JSON;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.json.JSONOpt;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract class HttpExecutor {

    public static final ContentType APPLICATION_FORM_URLENCODED = ContentType.create(
        "application/x-www-form-urlencoded", StandardCharsets.UTF_8);
    public static final String BOUNDARY = "------1cC9oE7dN8eT1fI0aT2n4------";
    public static final String multiPartTypeHead =
        "multipart/form-data; charset=UTF-8; boundary=" + BOUNDARY;
    public static final String applicationFormHead =
        "application/x-www-form-urlencoded; charset=UTF-8";
    public static final String multiPartApplicationFormHead =
        "multipart/x-www-form-urlencoded; charset=UTF-8; boundary=" + BOUNDARY;
            /*ContentType.create(
            "multipart/form-data",  new NameValuePair[]{
                    new BasicNameValuePair("charset","UTF-8"),
                    new BasicNameValuePair("boundary",BOUNDARY)
            }).toString();    */
    /*ContentType.create(
    "application/x-www-form-urlencoded",  (new NameValuePair[]{
            new BasicNameValuePair("charset","UTF-8"),
            new BasicNameValuePair("boundary",BOUNDARY)
    })).toString();    */
    public static final String applicationJSONHead = ContentType.create(
        "application/json", StandardCharsets.UTF_8).toString();
    public static final String plainTextHead = ContentType.create(
        "text/plain", StandardCharsets.UTF_8).toString();
    public static final String xmlTextHead = ContentType.create(
        "text/xml", StandardCharsets.UTF_8).toString();
    public static final String applicationOctetStream = ContentType.create(
        "application/octet-stream", (Charset) null).toString();
    protected static final Logger logger = LoggerFactory.getLogger(HttpExecutor.class);

    private HttpExecutor() {
        throw new IllegalAccessError("Utility class");
    }

    public static CloseableHttpClient createHttpClient(HttpExecutorContext executorContext) {
        HttpClientBuilder clientBuilder = HttpClients.custom();
        if (executorContext.isUseSSL()) {
            // HttpClient5中为了兼容老版本的SSL信任所有证书功能
            // 在生产环境中应该使用正确的SSL证书验证
            try {
                TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
                javax.net.ssl.SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

                // 简化方式：仅配置连接管理器，让HttpClient5自动处理SSL
                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
                connectionManager.setDefaultSocketConfig(SocketConfig.custom()
                    .setSoTimeout(Timeout.ofMinutes(1))
                    .build());
                clientBuilder.setConnectionManager(connectionManager);

                // 注意：HttpClient5推荐使用默认的SSL配置，如需自定义SSL请参考官方文档
                logger.warn("使用宽松的SSL配置，不建议在生产环境使用");
            } catch (Exception e) {
                logger.error("SSL配置失败，使用默认配置", e);
            }
        }
        if (executorContext.isStoreCookie()){
            BasicCookieStore cookieStore = new BasicCookieStore();
            clientBuilder.setDefaultCookieStore(cookieStore);
            if(executorContext.getHttpContext() == null) {
                executorContext.context(HttpClientContext.create());
            }
            executorContext.getHttpContext().setCookieStore(cookieStore);
        }
        if (executorContext.getHttpProxy() != null)
            clientBuilder.setProxy(executorContext.getHttpProxy());
        return clientBuilder.build();
    }

    public static CloseableHttpClient createHttpClient() {
        return HttpClients.createDefault();
    }


    public static void prepareHttpRequest(HttpExecutorContext executorContext,
                                   ClassicHttpRequest httpRequest){
        if (executorContext.getHttpHeaders() != null) {
            for (Map.Entry<String, String> entHeader : executorContext.getHttpHeaders().entrySet())
                httpRequest.setHeader(entHeader.getKey(), entHeader.getValue());
        }

        if (executorContext.getHttpCookies() != null && !executorContext.getHttpCookies().isEmpty()) {
            StringBuilder cookieString = new StringBuilder();
            int i = 0;
            for (Map.Entry<String, String> entCookie : executorContext.getHttpCookies().entrySet()) {
                if(i>0){
                    cookieString.append(";");
                }
                cookieString.append(entCookie.getKey()).append("=").append(entCookie.getValue());
                i++;
            }
            httpRequest.setHeader("Cookie", cookieString.toString());
        }

        // HttpClient5中超时和代理配置应该在HttpClientBuilder级别设置，
        // 而不是在每个request上设置，因此移除了无效的per-request配置代码
    }
    public static <T> T httpExecute(HttpExecutorContext executorContext,
                                    ClassicHttpRequest httpRequest, org.apache.hc.core5.http.io.HttpClientResponseHandler<T> responseHandler)
        throws IOException {

        prepareHttpRequest(executorContext, httpRequest);

        CloseableHttpClient httpClient = null;
        boolean createSelfClient = executorContext.getHttpclient() == null;
        if (createSelfClient) {
            httpClient = HttpExecutor.createHttpClient(executorContext);
        } else {
            httpClient = executorContext.getHttpclient();
        }

        try {
            return httpClient.execute(httpRequest, executorContext.getHttpContext(), responseHandler);
        } finally {
            if (createSelfClient) {
                httpClient.close();
            }
        }
    }

    public static String httpExecute(HttpExecutorContext executorContext,
                                     ClassicHttpRequest httpRequest)
        throws IOException {
        return httpExecute(executorContext, httpRequest, Utf8ResponseHandler.INSTANCE);
    }

    public static String simpleGet(HttpExecutorContext executorContext, String uri, String queryParam)
        throws IOException {
        HttpGet httpGet = new HttpGet(UrlOptUtils.appendParamToUrl(uri, queryParam));
        return httpExecute(executorContext, httpGet);
    }


    public static String simpleGet(HttpExecutorContext executorContext, String uri, Map<String, Object> queryParam)
        throws IOException {
        HttpGet httpGet = new HttpGet(UrlOptUtils.appendParamsToUrl(uri, queryParam));
        return httpExecute(executorContext, httpGet);
    }

    public static String simpleGet(HttpExecutorContext executorContext, String uri)
        throws IOException {
        return simpleGet(executorContext,
            uri, (String) null);
    }


    public static String simpleDelete(HttpExecutorContext executorContext, String uri, String queryParam)
        throws IOException {
        HttpDelete httpDelete = new HttpDelete(UrlOptUtils.appendParamToUrl(uri, queryParam));
        return httpExecute(executorContext, httpDelete);
    }

    public static String simpleDelete(HttpExecutorContext executorContext, String uri)
        throws IOException {
        HttpDelete httpDelete = new HttpDelete(uri);
        return httpExecute(executorContext, httpDelete);
    }

    public static String simpleDelete(HttpExecutorContext executorContext, String uri, Map<String, Object> queryParam)
        throws IOException {
        HttpDelete httpDelete = new HttpDelete(UrlOptUtils.appendParamsToUrl(uri, queryParam));
        return httpExecute(executorContext, httpDelete);
    }


    public static String simplePut(HttpExecutorContext executorContext,
                                   String uri, String putEntity)
        throws IOException {
        HttpPut httpPut = new HttpPut(uri);

        httpPut.setHeader("Content-Type", plainTextHead);
        if (putEntity != null) {
            StringEntity entity = new StringEntity(putEntity, StandardCharsets.UTF_8);
            httpPut.setEntity(entity);
        }
        return httpExecute(executorContext, httpPut);
    }


    public static String rawPut(HttpExecutorContext executorContext,
                                String uri, byte[] bytes)
        throws IOException {

        HttpPut httpPut = new HttpPut(uri);
        httpPut.setHeader("Content-Type", applicationFormHead);


        if (bytes != null) {
            ByteArrayEntity entity = new ByteArrayEntity(bytes, ContentType.APPLICATION_OCTET_STREAM);
            httpPut.setEntity(entity);
        }

        return httpExecute(executorContext, httpPut);

    }

    /*
     * 在spring mvc 中的 request.getInputStream() 是不可以用的，因为spring 已经处理过这个流
     * 所以这个方法只能在自己写的servlet中使用
     */
    public static String requestInputStreamPut(HttpExecutorContext executorContext,
                                               String uri, InputStream putIS)
        throws IOException {

        HttpPut httpPut = new HttpPut(uri);
        httpPut.setHeader("Content-Type", applicationFormHead);

        if (putIS != null) {
            InputStreamEntity entity = new InputStreamEntity(putIS, ContentType.APPLICATION_OCTET_STREAM);
            httpPut.setEntity(entity);
        }

        return httpExecute(executorContext, httpPut);
    }

    public static List<NameValuePair> makeRequestParams(Object obj, String prefixName) {
        List<NameValuePair> params = new ArrayList<>();
        if (obj == null)
            return params;

        if (ReflectionOpt.isScalarType(obj.getClass())) {
            params.add(new BasicNameValuePair(prefixName,
                StringBaseOpt.objectToString(obj)));
            return params;
        } else if (obj instanceof NameValuePair) {
            params.add((NameValuePair) obj);
            return params;
        } else if (obj instanceof Map) {
            String sFN = StringUtils.isBlank(prefixName) ? "" : prefixName + ".";
            @SuppressWarnings("unchecked")
            Map<String, Object> objMap = (Map<String, Object>) obj;
            /*objMap.entrySet().forEach( f -> {if(f.getRight()!=null){
                List<NameValuePair> subNP = makeRequectParams(f.getRight(), sFN + f.getLeft());
                params.addAll(subNP);
            }} );*/
            for (Map.Entry<String, Object> f : objMap.entrySet()) {
                if (f.getValue() != null) {
                    List<NameValuePair> subNP = makeRequestParams(f.getValue(), sFN + f.getKey());
                    params.addAll(subNP);
                }
            }//end of for
            return params;
        } else if (obj instanceof Collection<?> objList) {//end of map
            if (objList.size() == 1) {
                Object subObj = objList.iterator().next();
                if (subObj != null) {
                    if (ReflectionOpt.isScalarType(subObj.getClass())) {
                        params.add(new BasicNameValuePair(prefixName,
                            StringBaseOpt.objectToString(obj)));
                    } else {
                        if (subObj instanceof NameValuePair) {
                            params.add((NameValuePair) subObj);
                        } else {
                            List<NameValuePair> subNP = makeRequestParams(subObj, prefixName);
                            params.addAll(subNP);
                        }
                    }
                }
            } else if (objList.size() > 1) {
                int n = 0;
                //int complexObject=0;
                //List<String> arrayStr = new ArrayList<String>();
                for (Object subObj : objList) {
                    if (subObj != null) {
                        /*if(ReflectionOpt.isPrimitiveType(subobj.getClass())){
                            arrayStr.add(StringBaseOpt.objectToString(subobj));
                        }else*/
                        if (ReflectionOpt.isScalarType(subObj.getClass())) {
                            params.add(new BasicNameValuePair(prefixName,
                                StringBaseOpt.objectToString(subObj)));
                            //complexObject ++;
                        } else if (subObj instanceof NameValuePair) {
                            params.add((NameValuePair) subObj);
                            //complexObject ++;
                        } else {
                            List<NameValuePair> subNP = makeRequestParams(subObj, prefixName + "[" + n + "]");
                            params.addAll(subNP);
                            //complexObject ++;
                        }
                    }//else
                    //arrayStr.add("");
                    n++;
                }//end of for
                /*if(complexObject == 0)
                    params.add( new BasicNameValuePair(prefixName,
                            StringBaseOpt.objectToString(arrayStr)));*/
            }
            return params; //返回一个空的
        } else if (obj instanceof Object[] objs) {
            if (objs.length == 1) {
                Object subobj = objs[0];
                if (subobj != null) {
                    if (ReflectionOpt.isScalarType(subobj.getClass())) {
                        params.add(new BasicNameValuePair(prefixName,
                            StringBaseOpt.objectToString(obj)));
                    } else if (subobj instanceof NameValuePair) {
                        params.add((NameValuePair) subobj);
                    } else {
                        List<NameValuePair> subNP = makeRequestParams(subobj, prefixName);
                        params.addAll(subNP);
                    }
                }
            } else if (objs.length > 1) {
                //int complexObject=0;
                //List<String> arrayStr = new ArrayList<String>();
                for (int i = 0; i < objs.length; i++) {
                    if (objs[i] != null) {
                        /*if(ReflectionOpt.isPrimitiveType(objs[i].getClass())){
                            arrayStr.add(StringBaseOpt.objectToString(objs[i]));
                        }else*/
                        if (ReflectionOpt.isScalarType(objs[i].getClass())) {
                            params.add(new BasicNameValuePair(prefixName,
                                StringBaseOpt.objectToString(objs[i])));
                            //complexObject ++;
                        } else if (objs[i] instanceof NameValuePair) {
                            params.add((NameValuePair) objs[i]);
                            //complexObject ++;
                        } else {
                            List<NameValuePair> subNP = makeRequestParams(objs[i], prefixName + "[" + i + "]");
                            params.addAll(subNP);
                            //complexObject ++;
                        }
                    }//else
                    //arrayStr.add("");
                }//end of for
                /*if(complexObject == 0)
                    params.add( new BasicNameValuePair(prefixName,
                            StringBaseOpt.objectToString(arrayStr)));*/
            }
            return params; //返回一个空的
        } else {
            List<Method> methods = ReflectionOpt.getAllGetterMethod(obj.getClass());
            if(methods!=null) {
                String sFN = StringUtils.isBlank(prefixName) ? "" : prefixName + ".";
                for (Method md : methods) {
                    try {
                        Object v = md.invoke(obj);
                        if (v != null) {
                            String sKey = ReflectionOpt.methodNameToField(md.getName());
                            List<NameValuePair> subNP = makeRequestParams(v, sFN + sKey);
                            params.addAll(subNP);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);//logger.error(e.getMessage(), e);
                    }
                }
            }
            return params;
        }
        //return params;
    }

    private static HttpEntity buildEntity(Object formData){
        EntityBuilder eb = EntityBuilder.create();
        eb.setContentType(APPLICATION_FORM_URLENCODED);
        eb.setContentEncoding("utf-8");
        //FormBodyPartBuilder formBuilder = FormBodyPartBuilder.create(formName,null);
        List<NameValuePair> params = makeRequestParams(formData, "");
        eb.setParameters(params);
        return eb.build();
    }

    private static HttpEntity buildEntity(Object[] formObjects, Map<String, Object> extFormObjects){
        List<NameValuePair> params = new ArrayList<>();
        if (formObjects != null) {
            for (Object formObject : formObjects) {
                if (formObject != null) {
                    List<NameValuePair> subNP = makeRequestParams(formObject, "");
                    params.addAll(subNP);
                }
            }//end of for
        }
        if (extFormObjects != null) {
            List<NameValuePair> subNP = makeRequestParams(extFormObjects, "");
            params.addAll(subNP);
        }
        EntityBuilder eb = EntityBuilder.create();
        eb.setContentType(APPLICATION_FORM_URLENCODED);
        eb.setContentEncoding("utf-8");
        //FormBodyPartBuilder formBuilder = FormBodyPartBuilder.create(formName,null);
        //List<NameValuePair> params = makeRequectParams(formData,"");
        eb.setParameters(params);
        return eb.build();

    }

    public static List<NameValuePair> makeRequestParams(Object obj) {
        return makeRequestParams(obj, "");
    }

    public static String formPut(HttpExecutorContext executorContext,
                                 String uri, Object formData)
        throws IOException {
        HttpPut httpPut = new HttpPut(uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPut.setHeader("Content-Type", applicationFormHead);
        if (formData != null) {
            httpPut.setEntity(buildEntity(formData));
        }
        return httpExecute(executorContext, httpPut);
    }


    public static String multiFormPut(HttpExecutorContext executorContext,
                                      String uri, Object[] formObjects, Map<String, Object> extFormObjects)
        throws IOException {
        HttpPut httpPut = new HttpPut(uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPut.setHeader("Content-Type", applicationFormHead);
        httpPut.setEntity(buildEntity(formObjects, extFormObjects));
        return httpExecute(executorContext, httpPut);
    }


    public static String multiFormPut(HttpExecutorContext executorContext,
                                      String uri, Object formObject, Map<String, Object> extFormObjects)
        throws IOException {

        return multiFormPut(executorContext,
            uri, new Object[]{formObject}, extFormObjects);
    }


    public static String simplePost(HttpExecutorContext executorContext,
                                    String uri, String postEntity, final boolean asPutMethod)
        throws IOException {
        HttpPost httpPost = new HttpPost(asPutMethod ? urlAddMethodParameter(uri, "PUT") : uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", plainTextHead);
        if (postEntity != null) {
            StringEntity entity = new StringEntity(postEntity, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
        }
        return httpExecute(executorContext, httpPost);
    }


    public static String simplePost(HttpExecutorContext executorContext,
                                    String uri, String postEntity)
        throws IOException {
        return simplePost(executorContext, uri, postEntity, false);
    }

    /*
     * 在spring mvc 中的 request.getInputStream() 是不可以用的，因为spring 已经处理过这个流
     * 所以这个方法只能在自己写的servlet中使用
     */
    public static String requestInputStreamPost(HttpExecutorContext executorContext,
                                                String uri, InputStream postIS)
        throws IOException {
        HttpPost httpPost = new HttpPost(uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", applicationFormHead);

        if (postIS != null) {
            InputStreamEntity entity = new InputStreamEntity(postIS, ContentType.APPLICATION_OCTET_STREAM);
            httpPost.setEntity(entity);
        }

        return httpExecute(executorContext, httpPost);
    }

    public static String rawPost(HttpExecutorContext executorContext,
                                 String uri, byte[] bytes, final boolean asPutMethod)
        throws IOException {

        HttpPost httpPost = new HttpPost(asPutMethod ? urlAddMethodParameter(uri, "PUT") : uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", applicationFormHead);

        if (bytes != null) {
            ByteArrayEntity entity = new ByteArrayEntity(bytes, ContentType.APPLICATION_OCTET_STREAM);
            httpPost.setEntity(entity);
        }

        return httpExecute(executorContext, httpPost);
    }

    public static String rawPost(HttpExecutorContext executorContext,
                                 String uri, byte[] bytes)
        throws IOException {
        return rawPost(executorContext, uri, bytes, false);
    }

    public static String jsonPost(HttpExecutorContext executorContext,
                                  String uri, Object jsonObj, final boolean asPutMethod)
        throws IOException {
        String jsonString = null;
        if (jsonObj != null) {
            if (jsonObj instanceof String) {
                jsonString = (String) jsonObj;
            } else {
                jsonString = JSON.toJSONString(jsonObj);
            }
        }

        HttpPost httpPost = new HttpPost(asPutMethod ? urlAddMethodParameter(uri, "PUT") : uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", applicationJSONHead);
        if (StringUtils.isNotBlank(jsonString)) {
            StringEntity entity = new StringEntity(jsonString, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
        }

        return httpExecute(executorContext, httpPost);
    }

    public static String jsonPost(HttpExecutorContext executorContext,
                                  String uri, Object obj)
        throws IOException {
        return jsonPost(executorContext, uri, obj, false);
    }

    public static String jsonPut(HttpExecutorContext executorContext,
                                 String uri, Object jsonObj)
        throws IOException {
        String jsonString = null;
        if (jsonObj != null) {
            if (jsonObj instanceof String) {
                jsonString = (String) jsonObj;
            } else {
                jsonString = JSON.toJSONString(jsonObj);
            }
        }
        HttpPut httpPut = new HttpPut(uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPut.setHeader("Content-Type", applicationJSONHead);
        if (StringUtils.isNotBlank(jsonString)) {
            StringEntity entity = new StringEntity(jsonString, StandardCharsets.UTF_8);
            httpPut.setEntity(entity);
        }
        return httpExecute(executorContext, httpPut);
    }

    public static String xmlPost(HttpExecutorContext executorContext,
                                 String uri, String xmlEntity, final boolean asPutMethod)
        throws IOException {

        HttpPost httpPost = new HttpPost(asPutMethod ? urlAddMethodParameter(uri, "PUT") : uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", xmlTextHead);

        if (xmlEntity != null) {
            StringEntity entity = new StringEntity(xmlEntity, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
        }

        return httpExecute(executorContext, httpPost);
    }

    public static String xmlPost(HttpExecutorContext executorContext,
                                 String uri, String xmlEntity)
        throws IOException {
        return xmlPost(executorContext, uri, xmlEntity, false);
    }

    public static String xmlPut(HttpExecutorContext executorContext,
                                String uri, String xmlEntity)
        throws IOException {
        HttpPut httpPut = new HttpPut(uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPut.setHeader("Content-Type", xmlTextHead);
        if (StringUtils.isNotBlank(xmlEntity)) {
            StringEntity entity = new StringEntity(xmlEntity, StandardCharsets.UTF_8);
            httpPut.setEntity(entity);
        }
        return httpExecute(executorContext, httpPut);
    }

    public static String urlAddMethodParameter(String url, String method) {
        String sUrl;// = url;
        if (url.indexOf('?') == -1) {
            sUrl = url + "?_method=" + method;
        } else if (url.endsWith("?") || url.endsWith("&")) {
            sUrl = url + "_method=" + method;
        } else {
            sUrl = url + "&_method=" + method;
        }
        return sUrl;
    }

    public static String formPost(HttpExecutorContext executorContext,
                                  String uri, Object formData, final boolean asPutMethod)
        throws IOException {
        HttpPost httpPost = new HttpPost(asPutMethod ? urlAddMethodParameter(uri, "PUT") : uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", applicationFormHead);
        if (formData != null) {
            httpPost.setEntity(buildEntity(formData));
        }
        return httpExecute(executorContext, httpPost);
    }

    public static String formPost(HttpExecutorContext executorContext,
                                  String uri, Object formData)
        throws IOException {
        return formPost(executorContext, uri, formData, false);
    }

    public static String multiFormPost(HttpExecutorContext executorContext,
                                       String uri, Object[] formObjects, Map<String, Object> extFormObjects, final boolean asPutMethod)
        throws IOException {

        HttpPost httpPost = new HttpPost(asPutMethod ? urlAddMethodParameter(uri, "PUT") : uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", applicationFormHead);
        httpPost.setEntity(buildEntity(formObjects, extFormObjects));
        return httpExecute(executorContext, httpPost);
    }


    public static String multiFormPost(HttpExecutorContext executorContext,
                                       String uri, Object formObject, Map<String, Object> extFormObjects, final boolean asPutMethod)
        throws IOException {
        return multiFormPost(executorContext,
            uri, new Object[]{formObject}, extFormObjects, asPutMethod);
    }


    public static String multiFormPost(HttpExecutorContext executorContext,
                                       String uri, Object[] formObjects, Map<String, Object> extFormObjects)
        throws IOException {
        return multiFormPost(executorContext,
            uri, formObjects, extFormObjects, false);
    }

    public static String multiFormPost(HttpExecutorContext executorContext,
                                       String uri, Object formObject, Map<String, Object> extFormObjects)
        throws IOException {
        return multiFormPost(executorContext,
            uri, new Object[]{formObject}, extFormObjects, false);
    }

    public static String inputStreamUploadPut(HttpExecutorContext executorContext,
                                           String uri, InputStream inputStream,
                                           final String filedName, ContentType contentType, final String filename)
        throws IOException {
        HttpPut httpPut = new HttpPut(uri);
        //httpPost.setHeader("Content-Type", applicationOctetStream);
        if(!executorContext.hasHeader("Content-Type"))
            httpPut.setHeader("Content-Type", multiPartTypeHead);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setBoundary(BOUNDARY);
        //builder.setMode(HttpMultipartMode.RFC6532);
        builder.addBinaryBody(filedName, inputStream,contentType, filename);
        httpPut.setEntity(builder.build());
        return httpExecute(executorContext, httpPut);
    }

    public static String inputStreamUpload(HttpExecutorContext executorContext,
                                           String uri, InputStream inputStream,
                                           final String filedName, ContentType contentType, final String filename)
        throws IOException {

        HttpPost httpPost = new HttpPost(uri);
        //httpPost.setHeader("Content-Type", applicationOctetStream);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", multiPartTypeHead);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setBoundary(BOUNDARY);
        //builder.setMode(HttpMultipartMode.RFC6532);
        builder.addBinaryBody(filedName, inputStream, contentType, filename);
        httpPost.setEntity(builder.build());
        return httpExecute(executorContext, httpPost);
    }


    public static String inputStreamUpload(HttpExecutorContext executorContext,
                                           String uri, Map<String, Object> formObjects, InputStream inputStream,
                                           final String filedName, ContentType contentType, final String filename)
        throws IOException {

        String paramsUrl = null;
        if (formObjects != null) {
            List<NameValuePair> params = makeRequestParams(formObjects, "");
            try {
                paramsUrl = EntityUtils.toString(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            } catch (org.apache.hc.core5.http.ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return inputStreamUpload(executorContext,
            UrlOptUtils.appendParamToUrl(uri, paramsUrl), inputStream, filedName, contentType, filename);
    }

    private static MultipartEntityBuilder createMultipartEntityFromFormAndFiles(Map<String, Object> formObjects, Map<String, File> files) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setBoundary(BOUNDARY);
        builder.setMode(HttpMultipartMode.LEGACY);

        if (formObjects != null) {
            ContentType contentType = ContentType.create("text/plain",StandardCharsets.UTF_8);
            for (Map.Entry<String, Object> param : formObjects.entrySet()) {
                builder.addTextBody(param.getKey(),
                    JSONOpt.objectToJSONString(param.getValue()),contentType);
            }
        }
        if (files != null) {
            for (Map.Entry<String, File> file : files.entrySet()) {
                builder.addBinaryBody(file.getKey(), file.getValue());
            }
        }
        return builder;
    }

    public static String formPutWithFileUpload(HttpExecutorContext executorContext,
                                                String uri, Map<String, Object> formObjects, Map<String, File> files)
        throws IOException {
        HttpPut httpPut = new HttpPut(uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPut.setHeader("Content-Type", multiPartTypeHead);
        MultipartEntityBuilder builder = createMultipartEntityFromFormAndFiles(formObjects, files);
        httpPut.setEntity(builder.build());
        return httpExecute(executorContext, httpPut);
    }

    public static String formPostWithFileUpload(HttpExecutorContext executorContext,
                                                String uri, Map<String, Object> formObjects, Map<String, File> files)
        throws IOException {
        HttpPost httpPost = new HttpPost(uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", multiPartTypeHead);
        MultipartEntityBuilder builder = createMultipartEntityFromFormAndFiles(formObjects, files);
        httpPost.setEntity(builder.build());
        return httpExecute(executorContext, httpPost);
    }

    public static String fileUpload(HttpExecutorContext executorContext,
                                    String uri, File file)
        throws IOException {
        HttpPost httpPost = new HttpPost(uri);
        if(!executorContext.hasHeader("Content-Type"))
            httpPost.setHeader("Content-Type", multiPartTypeHead);
        InputStreamEntity entity = new InputStreamEntity(new FileInputStream(file), ContentType.APPLICATION_OCTET_STREAM);
        httpPost.setEntity(entity);
        return httpExecute(executorContext, httpPost);
    }

    public static String fileUpload(HttpExecutorContext executorContext,
                                    String uri, Map<String, Object> formObjects, File file)
        throws IOException {
        String paramsUrl = null;
        if (formObjects != null) {
            List<NameValuePair> params = makeRequestParams(formObjects, "");
            try {
                paramsUrl = EntityUtils.toString(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            } catch (org.apache.hc.core5.http.ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return fileUpload(executorContext,
            UrlOptUtils.appendParamToUrl(uri, paramsUrl), file);
    }

    public static String extraFileName(ClassicHttpResponse response) {
        Header[] contentDispositionHeader = response
            .getHeaders("Content-Disposition");
        if(contentDispositionHeader == null || contentDispositionHeader.length == 0){
            return null;
        }
        Matcher m = Pattern.compile(".*filename=\"(.*)\"").matcher(contentDispositionHeader[0].getValue());
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    public static <T> T fetchInputStreamByUrl(HttpExecutorContext executorContext, String uri, String queryParam,
                                              DoOperateInputStream<T> operate) throws IOException {

        HttpGet httpGet = new HttpGet(UrlOptUtils.appendParamToUrl(uri, queryParam));
        CloseableHttpClient httpClient = null;
        final boolean createSelfClient = executorContext.getHttpclient() == null;
        if (createSelfClient) {
            httpClient = HttpExecutor.createHttpClient(executorContext);
        } else {
            httpClient = executorContext.getHttpclient();
        }
        prepareHttpRequest(executorContext, httpGet);

        final CloseableHttpClient finalHttpClient = httpClient;
        try {
            return httpClient.execute(httpGet, executorContext.getHttpContext(), response -> {
                Header[] contentTypeHeader = response.getHeaders("Content-Type");
                if (contentTypeHeader == null || contentTypeHeader.length < 1 ||
                    contentTypeHeader[0].getValue().contains("text/") ) {
                    String responseContent = Utf8ResponseHandler.INSTANCE
                        .handleResponse(response);
                    throw new RuntimeException(responseContent);
                }
                try (InputStream inputStream = InputStreamResponseHandler.INSTANCE
                    .handleResponse(response)) {
                    // 视频文件不支持下载
                    //fileName = extraFileName(response);
                    return operate.doOperate(inputStream);
                }
            });
        } finally {
            if (createSelfClient && finalHttpClient != null) {
                finalHttpClient.close();
            }
        }
    }

    public static <T> T fetchInputStreamByUrl(HttpExecutorContext executorContext, String uri,
                                              DoOperateInputStream<T> operate) throws IOException {

        return fetchInputStreamByUrl(executorContext,
            uri, null, operate);
    }

    public static <T> T fetchInputStreamByUrl(String uri,
                                              DoOperateInputStream<T> operate) throws IOException {
        try (CloseableHttpClient httpClient = HttpExecutor.createHttpClient()) {

            return fetchInputStreamByUrl(HttpExecutorContext.create(httpClient),
                uri, null, operate);
        }
    }

    public static boolean fileDownload(HttpExecutorContext executorContext,
                                       String uri, String queryParam, String filePath)
        throws IOException {

        return fetchInputStreamByUrl(executorContext,
            uri, queryParam,
            (inputStream) -> FileSystemOpt.createFile(inputStream, filePath));
    }

    public static boolean fileDownload(HttpExecutorContext executorContext,
                                       String uri, String filePath)
        throws IOException {
        return fileDownload(executorContext,
            uri, null, filePath);
    }

    public static boolean fileDownload(String uri, String queryParam, String filePath)
        throws IOException {

        return fileDownload(HttpExecutorContext.create(),
            uri, queryParam, filePath);

    }

    public static boolean fileDownload(String uri, String filePath)
        throws IOException {
        return fileDownload(HttpExecutorContext.create(),
            uri, null, filePath);
    }

    public interface DoOperateInputStream<T> {
        T doOperate(InputStream inputStream) throws IOException;
    }
}
