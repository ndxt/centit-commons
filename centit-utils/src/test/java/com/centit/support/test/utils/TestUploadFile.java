package com.centit.support.test.utils;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.file.FileSystemOpt;
import com.centit.support.file.FileType;
import com.centit.support.json.JSONOpt;
import com.centit.support.network.HttpExecutor;
import com.centit.support.network.HttpExecutorContext;
import com.centit.support.network.UrlOptUtils;
import org.apache.http.Consts;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TestUploadFile {

    private static MultipartEntityBuilder buildMultiPartEntity(String filePath) throws IOException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setBoundary(HttpExecutor.BOUNDARY);
        builder.setMode(HttpMultipartMode.RFC6532);
        //builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        String fileName = FileSystemOpt.extractFullFileName(filePath);
        builder.addBinaryBody("file", Files.newInputStream(Paths.get(filePath)),
            ContentType.create(FileType.getFileMimeType(fileName)), fileName);

        return builder;
    }
    public static void main(String[] args) throws IOException {
        HttpClientContext context = HttpClientContext.create();
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        context.setCookieStore(cookieStore);
        HttpExecutorContext httpExecutorContext = HttpExecutorContext.create(httpClient).context(context).header("Connection", "close").timout(10000);

        HttpPost httpPost = new HttpPost("http://10.0.101.93/ccApi4gx/attach/uploadTaskPhotos");
        httpPost.setHeader("Content-Type", "multipart/form-data; boundary=" + HttpExecutor.BOUNDARY);
        MultipartEntityBuilder builder = buildMultiPartEntity("D:/test/1.png");
        httpPost.setEntity(builder.build());
        String str = HttpExecutor.httpExecute(httpExecutorContext, httpPost);
        System.out.println(str);
    }
}
