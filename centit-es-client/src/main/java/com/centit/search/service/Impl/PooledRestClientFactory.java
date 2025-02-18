package com.centit.search.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.centit.search.service.ESServerConfig;
import com.centit.support.security.SecurityOptUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

/**
 * Created by codefan on 17-6-27.
 */
public class PooledRestClientFactory implements PooledObjectFactory<ElasticsearchClient> {

    private ESServerConfig config;

    public PooledRestClientFactory(ESServerConfig config){
        this.config = config;
    }

    @Override
    public PooledObject<ElasticsearchClient> makeObject() throws Exception {
        RestClientBuilder clientBuilder = RestClient.builder(config.getHttpHosts());
        // Create the low-level client
        /*RestClient restClient = RestClient
            .builder(HttpHost.create(serverUrl))
            .setDefaultHeaders(new Header[]{
                new BasicHeader("Authorization", "ApiKey " + apiKey)
            })
            .build();
        */
        //添加用户认证
        if (StringUtils.isNotBlank(config.getUsername()) && StringUtils.isNotBlank(config.getPassword())){
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(
                    SecurityOptUtils.decodeSecurityString(config.getUsername()),
                    SecurityOptUtils.decodeSecurityString(config.getPassword())));
            clientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
            // httpClientBuilder.disableAuthCaching();
        }
        ElasticsearchTransport transport = new RestClientTransport(
            clientBuilder.build(), new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);
        return new DefaultPooledObject<>(client);
    }

    @Override
    public void destroyObject(PooledObject<ElasticsearchClient> p) throws Exception {
        // RestClient restClient = p.getObject(); restClient.close();
        //p.getObject().close();
        /* ElasticsearchClient client = p.getObject();
         if(client!= null && client.ping().value()) { //RequestOptions.DEFAULT
           try {
                client.close();
            }catch (Exception e){
                //ignore
            }
        }*/
    }

    @Override
    public boolean validateObject(PooledObject<ElasticsearchClient> p) {
        ElasticsearchClient client = p.getObject();
        try {
            return client.ping().value(); // RequestOptions.DEFAULT
        }catch(Exception e){
            return false;
        }
    }

    @Override
    public void activateObject(PooledObject<ElasticsearchClient> p) throws Exception {
        ///ElasticsearchClient client = p.getObject();
        /*boolean response = */
        // client.ping(); // RequestOptions.DEFAULT
    }

    @Override
    public void passivateObject(PooledObject<ElasticsearchClient> p) throws Exception {
        // Auto-generated method stub
    }

    public void setConifg(ESServerConfig conifg) {
        this.config = conifg;
    }
}
