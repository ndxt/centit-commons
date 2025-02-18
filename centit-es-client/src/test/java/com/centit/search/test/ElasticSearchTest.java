package com.centit.search.test;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.centit.search.document.ObjectDocument;
import com.centit.search.utils.TikaTextExtractor;
import com.centit.support.security.SecurityOptUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.tika.exception.TikaException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;

/**
 * Created by zhang_gd on 2017/6/12.
 */
@SuppressWarnings("deprecated")
public class ElasticSearchTest {


    public static void main(String[] args) throws IOException, SAXException, TikaException {
        RestClientBuilder clientBuilder = RestClient.builder(
            new HttpHost("192.168.134.250", 32590, "http"));
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(
                SecurityOptUtils.decodeSecurityString("elastic"),
                SecurityOptUtils.decodeSecurityString("MrGehkgo")));
        clientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        ElasticsearchTransport transport = new RestClientTransport(
            clientBuilder.build(), new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);

    }

    public ObjectDocument setIndexDocument() throws IOException, SAXException, TikaException {
        ObjectDocument indexDocument = new ObjectDocument();
        indexDocument.setOsId("ESXM");
        indexDocument.setOptId("JSPT");
        indexDocument.setUserCode("zgd");
        indexDocument.setUnitCode("jgzx");
        indexDocument.setCreateTime(new Date());
        String content = TikaTextExtractor.extractFileText("C:\\Users\\zhang_gd\\Desktop\\sola\\testTypeFile\\read\\test2010.docx");
        indexDocument.setContent(content);
        return indexDocument;
    }


}
