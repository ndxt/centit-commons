package com.centit.support.network;

import com.alibaba.fastjson.JSON;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.json.JSONOpt;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract class HttpExecutor {

    public static final ContentType APPLICATION_FORM_URLENCODED = ContentType.create(
        "application/x-www-form-urlencoded", Consts.UTF_8);
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
        "application/json", Consts.UTF_8).toString();
    public static final String plainTextHead = ContentType.create(
        "text/plain", Consts.UTF_8).toString();
    public static final String xmlTextHead = ContentType.create(
        "text/xml", Consts.UTF_8).toString();
    public static final String applicationOctetStream = ContentType.create(
        "application/octet-stream", (Charset) null).toString();
    protected static final Logger logger = LoggerFactory.getLogger(HttpExecutor.class);
    private static TrustManager manager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    private HttpExecutor() {
        throw new IllegalAccessError("Utility class");
    }

    public static CloseableHttpClient createHttpClient(HttpHost httpProxy, boolean keepSession, boolean useSSL)
        throws NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder clientBuilder = HttpClients.custom();
        if (useSSL) {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{manager}, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
            //RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.NETSCAPE).build();
            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", socketFactory)
                    .build();
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            clientBuilder.setConnectionManager(connectionManager);
        }
        if (keepSession) {
            RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.NETSCAPE).build();
            clientBuilder.setDefaultRequestConfig(config);
        }
        if (httpProxy != null)
            clientBuilder.setProxy(httpProxy);
        return clientBuilder.build();
    }

    public static CloseableHttpClient createHttpClient() {
        return HttpClients.createDefault();
    }

    public static CloseableHttpClient createHttpClient(HttpHost httpProxy) {
        return HttpClients.custom().setProxy(httpProxy).build();
    }

    /*
     * 设置cookie的保存策略为CookieSpecs.NETSCAPE，这样可以保持session
     */
    public static CloseableHttpClient createKeepSessionHttpClient() {
        RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.NETSCAPE).build();
        return HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    public static CloseableHttpClient createKeepSessionHttpClient(HttpHost httpProxy) {
        RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.NETSCAPE).build();
        return HttpClients.custom().setDefaultRequestConfig(config).setProxy(httpProxy).build();
    }

    public static CloseableHttpClient createHttpsClient()
        throws NoSuchAlgorithmException, KeyManagementException {
        return createHttpClient(null, false, true);
    }

    public static CloseableHttpClient createKeepSessionHttpsClient()
        throws NoSuchAlgorithmException, KeyManagementException {
        return createHttpClient(null, true, true);
    }

    public static <T> T httpExecute(HttpExecutorContext executorContext,
                                    HttpRequestBase httpRequest, ResponseHandler<T> responseHandler)
        throws IOException {

        /*if(executorContext==null){
            executorContext = HttpExecutorContext.create();
        }else {*/
        if (executorContext.getHttpHeaders() != null) {
            for (Map.Entry<String, String> entHeader : executorContext.getHttpHeaders().entrySet())
                httpRequest.setHeader(entHeader.getKey(), entHeader.getValue());
        }
        if (executorContext.getHttpProxy() != null) {
            RequestConfig config = RequestConfig.custom().setProxy(executorContext.getHttpProxy())
                .build();
            httpRequest.setConfig(config);
        }
        //}
        CloseableHttpClient httpClient = null;
        boolean createSelfClient = executorContext.getHttpclient() == null;
        if (createSelfClient) {
            httpClient = executorContext.getHttpProxy() == null ?
                HttpExecutor.createHttpClient() :
                HttpExecutor.createHttpClient(executorContext.getHttpProxy());
        } else {
            httpClient = executorContext.getHttpclient();
        }

        try (CloseableHttpResponse response = httpClient.execute(httpRequest, executorContext.getHttpContext())) {
            return responseHandler.handleResponse(response);
        } finally {
            if (createSelfClient) {
                httpClient.close();
            }
        }
    }

    public static String httpExecute(HttpExecutorContext executorContext,
                                     HttpRequestBase httpRequest)
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
            StringEntity entity = new StringEntity(putEntity, Consts.UTF_8);
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
            ByteArrayEntity entity = new ByteArrayEntity(bytes);
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
            InputStreamEntity entity = new InputStreamEntity(putIS);
            httpPut.setEntity(entity);
        }

        return httpExecute(executorContext, httpPut);
    }

    public static List<NameValuePair> makeRequectParams(Object obj, String prefixName) {
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
            String sFN = (prefixName == null || "".equals(prefixName)) ? "" : prefixName + ".";
            @SuppressWarnings("unchecked")
            Map<String, Object> objMap = (Map<String, Object>) obj;
            /*objMap.entrySet().forEach( f -> {if(f.getRight()!=null){
                List<NameValuePair> subNP = makeRequectParams(f.getRight(), sFN + f.getLeft());
                params.addAll(subNP);
            }} );*/
            for (Map.Entry<String, Object> f : objMap.entrySet()) {
                if (f.getValue() != null) {
                    List<NameValuePair> subNP = makeRequectParams(f.getValue(), sFN + f.getKey());
                    params.addAll(subNP);
                }
            }//end of for
            return params;
        } else if (obj instanceof Collection) {//end of map
            Collection<?> objList = (Collection<?>) obj;
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
                            List<NameValuePair> subNP = makeRequectParams(subObj, prefixName);
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
                            List<NameValuePair> subNP = makeRequectParams(subObj, prefixName + "[" + n + "]");
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
        } else if (obj instanceof Object[]) {
            Object[] objs = (Object[]) obj;
            if (objs.length == 1) {
                Object subobj = objs[0];
                if (subobj != null) {
                    if (ReflectionOpt.isScalarType(subobj.getClass())) {
                        params.add(new BasicNameValuePair(prefixName,
                            StringBaseOpt.objectToString(obj)));
                    } else if (subobj instanceof NameValuePair) {
                        params.add((NameValuePair) subobj);
                    } else {
                        List<NameValuePair> subNP = makeRequectParams(subobj, prefixName);
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
                            List<NameValuePair> subNP = makeRequectParams(objs[i], prefixName + "[" + i + "]");
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
            String sFN = (prefixName == null || "".equals(prefixName)) ? "" : prefixName + ".";
            for (Method md : methods) {
                try {
                    Object v = md.invoke(obj);
                    if (v != null) {
                        String skey = ReflectionOpt.methodNameToField(md.getName());

                        List<NameValuePair> subNP = makeRequectParams(v, sFN + skey);
                        params.addAll(subNP);

                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);//e.printStackTrace();
                }
            }
            return params;
        }
        //return params;
    }

    public static List<NameValuePair> makeRequectParams(Object obj) {
        return makeRequectParams(obj, "");
    }

    public static String formPut(HttpExecutorContext executorContext,
                                 String uri, Object formData)
        throws IOException {
        HttpPut httpPut = new HttpPut(uri);
        httpPut.setHeader("Content-Type", applicationFormHead);

        if (formData != null) {

            EntityBuilder eb = EntityBuilder.create();
            eb.setContentType(APPLICATION_FORM_URLENCODED);
            eb.setContentEncoding("utf-8");
            //FormBodyPartBuilder formBuilder = FormBodyPartBuilder.create(formName,null);
            List<NameValuePair> params = makeRequectParams(formData, "");

            eb.setParameters(params);
            httpPut.setEntity(eb.build());
        }

        return httpExecute(executorContext, httpPut);
    }


    public static String multiFormPut(HttpExecutorContext executorContext,
                                      String uri, Object[] formObjects, Map<String, Object> extFormObjects)
        throws IOException {

        HttpPut httpPut = new HttpPut(uri);

        httpPut.setHeader("Content-Type", applicationFormHead);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (formObjects != null) {
            for (int i = 0; i < formObjects.length; i++) {
                if (formObjects[i] != null) {
                    List<NameValuePair> subNP = makeRequectParams(formObjects[i], "");
                    params.addAll(subNP);
                }
            }//end of for
        }
        if (extFormObjects != null) {
            List<NameValuePair> subNP = makeRequectParams(extFormObjects, "");
            params.addAll(subNP);
        }

        EntityBuilder eb = EntityBuilder.create();
        eb.setContentType(APPLICATION_FORM_URLENCODED);
        eb.setContentEncoding("utf-8");
        //FormBodyPartBuilder formBuilder = FormBodyPartBuilder.create(formName,null);
        //List<NameValuePair> params = makeRequectParams(formData,"");
        eb.setParameters(params);
        httpPut.setEntity(eb.build());

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
        httpPost.setHeader("Content-Type", plainTextHead);

        if (postEntity != null) {
            StringEntity entity = new StringEntity(postEntity, Consts.UTF_8);
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

        httpPost.setHeader("Content-Type", applicationFormHead);

        if (postIS != null) {
            InputStreamEntity entity = new InputStreamEntity(postIS);
            httpPost.setEntity(entity);
        }

        return httpExecute(executorContext, httpPost);
    }


    public static String rawPost(HttpExecutorContext executorContext,
                                 String uri, byte[] bytes, final boolean asPutMethod)
        throws IOException {

        HttpPost httpPost = new HttpPost(asPutMethod ? urlAddMethodParameter(uri, "PUT") : uri);

        httpPost.setHeader("Content-Type", applicationFormHead);

        if (bytes != null) {
            ByteArrayEntity entity = new ByteArrayEntity(bytes);
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
                                  String uri, String jsonString, final boolean asPutMethod)
        throws IOException {

        HttpPost httpPost = new HttpPost(asPutMethod ? urlAddMethodParameter(uri, "PUT") : uri);
        httpPost.setHeader("Content-Type", applicationJSONHead);
        if (jsonString != null && !"".equals(jsonString)) {
            StringEntity entity = new StringEntity(jsonString, Consts.UTF_8);
            httpPost.setEntity(entity);
        }

        return httpExecute(executorContext, httpPost);
    }

    public static String jsonPost(HttpExecutorContext executorContext,
                                  String uri, JSON jsonEntity, final boolean asPutMethod)
        throws IOException {

        return jsonPost(executorContext,
            uri, jsonEntity == null ? null : jsonEntity.toJSONString(), asPutMethod);
    }

    public static String jsonPost(HttpExecutorContext executorContext,
                                  String uri, Object obj, final boolean asPutMethod)
        throws IOException {
        String jsonString = null;
        if (obj != null) {
            if (obj instanceof String) {
                jsonString = (String) obj;
            } else {
                jsonString = JSON.toJSONString(obj);
            }
        }
        return jsonPost(executorContext,
            uri, jsonString, asPutMethod);
    }


    public static String jsonPost(HttpExecutorContext executorContext,
                                  String uri, String jsonString)
        throws IOException {
        return jsonPost(executorContext, uri, jsonString, false);
    }


    public static String jsonPost(HttpExecutorContext executorContext,
                                  String uri, Object obj)
        throws IOException {
        return jsonPost(executorContext, uri, obj, false);
    }


    public static String jsonPost(HttpExecutorContext executorContext,
                                  String uri, JSON jsonEntity)
        throws IOException {
        return jsonPost(executorContext, uri, jsonEntity, false);
    }


    public static String jsonPut(HttpExecutorContext executorContext,
                                 String uri, String jsonString)
        throws IOException {

        HttpPut httpPut = new HttpPut(uri);
        httpPut.setHeader("Content-Type", applicationJSONHead);
        if (jsonString != null && !"".equals(jsonString)) {
            StringEntity entity = new StringEntity(jsonString, Consts.UTF_8);
            httpPut.setEntity(entity);
        }
        return httpExecute(executorContext, httpPut);
    }


    public static String xmlPost(HttpExecutorContext executorContext,
                                 String uri, String xmlEntity, final boolean asPutMethod)
        throws IOException {

        HttpPost httpPost = new HttpPost(asPutMethod ? urlAddMethodParameter(uri, "PUT") : uri);
        httpPost.setHeader("Content-Type", xmlTextHead);

        if (xmlEntity != null) {
            StringEntity entity = new StringEntity(xmlEntity, Consts.UTF_8);
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
        httpPut.setHeader("Content-Type", xmlTextHead);
        if (xmlEntity != null && !"".equals(xmlEntity)) {
            StringEntity entity = new StringEntity(xmlEntity, Consts.UTF_8);
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

        httpPost.setHeader("Content-Type", applicationFormHead);

        if (formData != null) {

            EntityBuilder eb = EntityBuilder.create();
            eb.setContentType(APPLICATION_FORM_URLENCODED);
            eb.setContentEncoding("utf-8");
            //FormBodyPartBuilder formBuilder = FormBodyPartBuilder.create(formName,null);
            List<NameValuePair> params = makeRequectParams(formData, "");
            eb.setParameters(params);
            httpPost.setEntity(eb.build());
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

        httpPost.setHeader("Content-Type", applicationFormHead);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (formObjects != null) {
            for (int i = 0; i < formObjects.length; i++) {
                if (formObjects[i] != null) {
                    List<NameValuePair> subNP = makeRequectParams(formObjects[i], "");
                    params.addAll(subNP);
                }
            }//end of for
        }
        if (extFormObjects != null) {
            List<NameValuePair> subNP = makeRequectParams(extFormObjects, "");
            params.addAll(subNP);
        }

        EntityBuilder eb = EntityBuilder.create();
        eb.setContentType(APPLICATION_FORM_URLENCODED);
        eb.setContentEncoding("utf-8");
        //FormBodyPartBuilder formBuilder = FormBodyPartBuilder.create(formName,null);
        //List<NameValuePair> params = makeRequectParams(formData,"");
        eb.setParameters(params);
        httpPost.setEntity(eb.build());

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

    public static String inputStreamUpload(HttpExecutorContext executorContext,
                                           String uri, InputStream inputStream)
        throws IOException {

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", applicationOctetStream);
        /*httpPost.setHeader("Content-Type",
                //ContentType.MULTIPART_FORM_DATA.toString());
                "multipart/form-data; boundary=" + BOUNDARY);
        //httpPost.addHeader("boundary", BOUNDARY);*/
        InputStreamEntity entity = new InputStreamEntity(inputStream);
        httpPost.setEntity(entity);

        return httpExecute(executorContext, httpPost);
    }


    public static String inputStreamUpload(HttpExecutorContext executorContext,
                                           String uri, Map<String, Object> formObjects, InputStream inputStream)
        throws IOException {

        String paramsUrl = null;
        if (formObjects != null) {
            List<NameValuePair> params = makeRequectParams(formObjects, "");
            paramsUrl =
                EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
        }
        return inputStreamUpload(executorContext,
            UrlOptUtils.appendParamToUrl(uri, paramsUrl), inputStream);
    }


    public static String formPostWithFileUpload(HttpExecutorContext executorContext,
                                                String uri, Map<String, Object> formObjects, Map<String, File> files)
        throws IOException {


        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", multiPartTypeHead);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setBoundary(BOUNDARY);
        if (formObjects != null) {
            for (Map.Entry<String, Object> param : formObjects.entrySet()) {
                builder.addTextBody(param.getKey(),
                    JSONOpt.objectToJSONString(param.getValue()));
            }
        }

        if (files != null) {
            for (Map.Entry<String, File> file : files.entrySet()) {
                builder.addBinaryBody(file.getKey(), file.getValue());
            }
        }
        httpPost.setEntity(builder.build());
        return httpExecute(executorContext, httpPost);
    }


    public static String fileUpload(HttpExecutorContext executorContext,
                                    String uri, File file)
        throws IOException {

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", applicationOctetStream);

        /*httpPost.setHeader("Content-Type",
                //ContentType.MULTIPART_FORM_DATA.toString());
                "multipart/form-data; boundary=" + BOUNDARY);
        //httpPost.addHeader("boundary", BOUNDARY);*/
        InputStreamEntity entity = new InputStreamEntity(new FileInputStream(file));
        httpPost.setEntity(entity);
        return httpExecute(executorContext, httpPost);
    }

    public static String fileUpload(HttpExecutorContext executorContext,
                                    String uri, Map<String, Object> formObjects, File file)
        throws IOException {

        String paramsUrl = null;
        if (formObjects != null) {
            List<NameValuePair> params = makeRequectParams(formObjects, "");
            paramsUrl =
                EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
        }
        return fileUpload(executorContext,
            UrlOptUtils.appendParamToUrl(uri, paramsUrl), file);
    }

    protected static String extraFileName(CloseableHttpResponse response) {
        Header[] contentDispositionHeader = response
            .getHeaders("Content-disposition");

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
        boolean createSelfClient = executorContext.getHttpclient() == null;
        if (createSelfClient) {
            httpClient = executorContext.getHttpProxy() == null ?
                HttpExecutor.createHttpClient() :
                HttpExecutor.createHttpClient(executorContext.getHttpProxy());
        } else {
            httpClient = executorContext.getHttpclient();
        }

        try (CloseableHttpResponse response = httpClient.execute(httpGet, executorContext.getHttpContext())) {

            Header[] contentTypeHeader = response.getHeaders("Content-Type");
            if (contentTypeHeader == null || contentTypeHeader.length < 1 ||
                StringUtils.indexOf(
                    contentTypeHeader[0].getValue(), "text/") >= 0
            ) {
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
        } finally {
            if (createSelfClient) {
                httpClient.close();
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
