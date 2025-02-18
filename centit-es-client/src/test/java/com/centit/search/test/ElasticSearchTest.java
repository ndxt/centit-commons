package com.centit.search.test;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.centit.search.document.ObjectDocument;
import com.centit.search.utils.TikaTextExtractor;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Created by zhang_gd on 2017/6/12.
 */
@SuppressWarnings("deprecated")
public class ElasticSearchTest {
    ElasticsearchClient client;


    public void before() throws UnknownHostException, InterruptedException, ExecutionException {
        /*Settings esSettings = Settings.builder()
                //.put("cluster.name", "elasticsearch_zgd") //设置ES实例的名称
                .put("client.transport.sniff", true) //自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
                .build();*/
        //client = new PreBuiltTransportClient(esSettings);//初始化client较老版本发生了变化，此方法有几个重载方法，初始化插件等。
        //此步骤添加IP，至少一个，其实一个就够了，因为添加了自动嗅探配置
        //client.addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));
       /* client = new ElasticsearchClient(
            RestClient.builder(
                new HttpHost("192.168.134.250", 32590, "http")) );
        System.out.println("success connect");*/
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
