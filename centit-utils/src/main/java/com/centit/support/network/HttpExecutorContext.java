package com.centit.support.network;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;

import java.util.HashMap;
import java.util.Map;

public class HttpExecutorContext {
    private CloseableHttpClient httpclient;
    private HttpClientContext httpContext;
    private Map<String, String> httpHeaders;
    private Map<String, String> httpCookies;
    private HttpHost httpProxy;
    private int timeout;
    private boolean keepSession;
    private boolean useSSL;

    public HttpExecutorContext() {
        httpHeaders = null;
        httpCookies = null;
        httpContext = null;
        httpclient = null;
        httpProxy = null;
        timeout=-1;
    }

    public static HttpExecutorContext create() {
        return new HttpExecutorContext();
    }

    public static HttpExecutorContext empty() {
        return new HttpExecutorContext();
    }

    public static HttpExecutorContext create(CloseableHttpClient httpclient) {
        HttpExecutorContext executorContext = new HttpExecutorContext();
        executorContext.httpclient = httpclient;
        return executorContext;
    }

    public HttpExecutorContext client(CloseableHttpClient httpclient) {
        this.httpclient = httpclient;
        return this;
    }

    public HttpExecutorContext context(HttpClientContext httpContext) {
        this.httpContext = httpContext;
        return this;
    }

    public HttpExecutorContext proxy(HttpHost httpProxy) {
        this.httpProxy = httpProxy;
        return this;
    }

    public HttpExecutorContext headers(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }

    public HttpExecutorContext header(String name, String value) {
        if (this.httpHeaders == null) {
            this.httpHeaders = new HashMap<>(6);
        }
        httpHeaders.put(name, value);
        return this;
    }

    public HttpExecutorContext cookies(Map<String, String> httpCookies) {
        this.httpCookies = httpCookies;
        return this;
    }

    public HttpExecutorContext cookie(String name, String value) {
        if (this.httpCookies == null) {
            this.httpCookies = new HashMap<>(6);
        }
        httpCookies.put(name, value);
        return this;
    }
    public HttpExecutorContext enableSSL(boolean useSSL){
        this.useSSL=useSSL;
        return  this;
    }

    public HttpExecutorContext enableSession(boolean keepSession){
        this.keepSession=keepSession;
        return  this;
    }

    public HttpExecutorContext timeout(int timeout){
        this.timeout=timeout;
        return  this;
    }

    public int getTimeout(){
        return this.timeout;
    }

    public boolean isUseSSL(){
        return this.useSSL;
    }

    public boolean isKeepSession(){
        return this.keepSession;
    }

    public CloseableHttpClient getHttpclient() {
        return httpclient;
    }

    public HttpClientContext getHttpContext() {
        return httpContext;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public Map<String, String> getHttpCookies() {
        return httpCookies;
    }

    public HttpHost getHttpProxy() {
        return httpProxy;
    }

    public boolean hasHeader(String headerName){
        if(this.httpHeaders == null) return false;
        return this.httpHeaders.containsKey(headerName);
    }
}
