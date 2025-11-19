package com.centit.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.centit.support.security.SecurityOptUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;

/**
 * Elasticsearch 客户端管理器 - 使用单例模式
 * ElasticsearchClient 是线程安全的，建议应用程序中使用单个实例
 */
public abstract class ElasticsearchClientFactory {
    /**
     * 创建新的 ElasticsearchClient
     */
    public static ElasticsearchClient createClient(ElasticConfig config) {
        try {
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

            // 添加用户认证
            if (StringUtils.isNotBlank(config.getUsername()) && StringUtils.isNotBlank(config.getPassword())) {
                final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(
                        SecurityOptUtils.decodeSecurityString(config.getUsername()),
                        SecurityOptUtils.decodeSecurityString(config.getPassword())));
                clientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
            }

            // 配置连接池参数
            clientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
                if (StringUtils.isNotBlank(config.getUsername()) && StringUtils.isNotBlank(config.getPassword())) {
                    final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(
                            SecurityOptUtils.decodeSecurityString(config.getUsername()),
                            SecurityOptUtils.decodeSecurityString(config.getPassword())));
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
                return httpClientBuilder
                    .setMaxConnTotal(500)         // 最大连接数
                    .setMaxConnPerRoute(100);     // 每个路由的最大连接数
            });

            RestClient restClient = clientBuilder.build();
            // 配置兼容模式的 RestClientTransport
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            return new ElasticsearchClient(transport);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create ElasticsearchClient", e);
        }
    }

    /**
     * 关闭指定配置的客户端
     */
    public static void closeClient(ElasticsearchClient client) {
        if (client != null) {
            try {
                client._transport().close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
