package com.centit.search.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.centit.search.service.ElasticConfig;
import com.centit.support.security.SecurityOptUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

/**
 * Created by codefan on 17-6-27.
 */
public class PooledRestClientFactory implements PooledObjectFactory<ElasticsearchClient> {

    private ElasticConfig config;

    public PooledRestClientFactory(ElasticConfig config){
        this.config = config;
    }

    @Override
    public PooledObject<ElasticsearchClient> makeObject() throws Exception {
        // 构建HttpHost数组
        String[] urls = config.getServerUrls();
        HttpHost[] httpHosts = new HttpHost[urls.length];
        for (int i = 0; i < urls.length; i++) {
            String url = urls[i];
            if (url.startsWith("http://")) {
                String[] parts = url.substring(7).split(":");
                httpHosts[i] = new HttpHost(parts[0], Integer.parseInt(parts[1]), "http");
            } else if (url.startsWith("https://")) {
                String[] parts = url.substring(8).split(":");
                httpHosts[i] = new HttpHost(parts[0], Integer.parseInt(parts[1]), "https");
            }
        }

        RestClientBuilder clientBuilder = RestClient.builder(httpHosts);

        //添加用户认证
        if (StringUtils.isNotBlank(config.getUsername()) && StringUtils.isNotBlank(config.getPassword())){
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(
                    SecurityOptUtils.decodeSecurityString(config.getUsername()),
                    SecurityOptUtils.decodeSecurityString(config.getPassword())));
            clientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        RestClient restClient = clientBuilder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);
        return new DefaultPooledObject<>(client);
    }

    @Override
    public void destroyObject(PooledObject<ElasticsearchClient> p) throws Exception {
        ElasticsearchClient client = p.getObject();
        if (client != null) {
            try {
                client._transport().close();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    @Override
    public boolean validateObject(PooledObject<ElasticsearchClient> p) {
        ElasticsearchClient client = p.getObject();
        try {
            return client.ping().value();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void activateObject(PooledObject<ElasticsearchClient> p) throws Exception {
        ElasticsearchClient client = p.getObject();
        client.ping();
    }

    @Override
    public void passivateObject(PooledObject<ElasticsearchClient> p) throws Exception {
        // Auto-generated method stub
    }

    public void setConifg(ElasticConfig conifg) {
        this.config = conifg;
    }
}
