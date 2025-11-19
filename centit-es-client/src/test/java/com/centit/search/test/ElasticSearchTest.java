package com.centit.search.test;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.document.ObjectDocument;
import com.centit.search.utils.TikaTextExtractor;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.http.HttpHost;
import org.apache.tika.exception.TikaException;
import org.elasticsearch.client.RestClient;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by zhang_gd on 2017/6/12.
 */
public class ElasticSearchTest {
    ElasticsearchClient client;

    public void before() throws UnknownHostException, InterruptedException, ExecutionException {
        // 创建 REST 客户端
        RestClient restClient = RestClient.builder(
            new HttpHost("192.168.134.250", 32590, "http")).build();

        // 创建传输层
        RestClientTransport transport = new RestClientTransport(
            restClient, new JacksonJsonpMapper());

        // 创建 Elasticsearch 客户端
        client = new ElasticsearchClient(transport);
        System.out.println("success connect");
    }

    //@Test
    public void index() throws Exception {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("name", "广告信息11");
        infoMap.put("title", "我的广告22");
        infoMap.put("createTime", new Date());
        infoMap.put("count", 1022);

        IndexRequest<JsonData> request = IndexRequest.of(i -> i
            .index("test")
            .document(JsonData.of(infoMap)));

        IndexResponse indexResponse = client.index(request);
        System.out.println("id:" + indexResponse.id());
    }

    //@Test
    public void get() throws Exception {
        GetRequest getRequest = GetRequest.of(g -> g
            .index("test")
            .id("peVI2WoB192wz409binr"));

        GetResponse<JsonData> response = client.get(getRequest, JsonData.class);
        System.out.println("response.getId():" + response.id());
        if (response.source() != null) {
            System.out.println("response.getSourceAsString():" + response.source().toJson().toString());
        }
    }

    //@Test
    public void testGet() throws IOException {
        QueryStringQuery queryStringQuery = QueryStringQuery.of(q -> q.query(""));

        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("objects")
            .query(queryStringQuery._toQuery()));

        SearchResponse<JsonData> response = client.search(searchRequest, JsonData.class);

        if (response.hits() != null && response.hits().total() != null) {
            System.out.println("查询到记录数=" + response.hits().total().value());

            for (Hit<JsonData> hit : response.hits().hits()) {
                Map<String, Object> json = new HashMap<>();
                if (hit.source() != null) {
                    // 将 JsonData 转换为 Map
                    json = hit.source().toJson().asJsonObject()
                        .entrySet().stream()
                        .collect(HashMap::new, (map, entry) -> {
                            map.put(entry.getKey(), entry.getValue().toString());
                        }, HashMap::putAll);
                }
                System.out.println(json.toString());
            }
        }
    }

    //@Test
    public void queryGet() throws Exception {
        // range查询
        RangeQuery rangeQuery = RangeQuery.of(r -> r
            .field("age")
            .gt(JsonData.of(50)));

        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("sxq")
            .query(rangeQuery._toQuery())
            .sort(sort -> sort.field(f -> f.field("age").order(SortOrder.Desc)))
            .size(20));

        SearchResponse<JsonData> searchResponse = client.search(searchRequest, JsonData.class);

        if (searchResponse.hits() != null && searchResponse.hits().total() != null) {
            System.out.println("查到记录数：" + searchResponse.hits().total().value());

            for (Hit<JsonData> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    Map<String, Object> sourceMap = hit.source().toJson().asJsonObject()
                        .entrySet().stream()
                        .collect(HashMap::new, (map, entry) -> {
                            /*if (entry.getValue().isJsonPrimitive()) {
                                if (entry.getValue().getAsJsonPrimitive().isString()) {
                                    map.put(entry.getKey(), entry.getValue().getAsString());
                                } else if (entry.getValue().getAsJsonPrimitive().isNumber()) {
                                    map.put(entry.getKey(), entry.getValue().getAsNumber());
                                }
                            } else {*/
                                map.put(entry.getKey(), entry.getValue());
                            //}
                        }, HashMap::putAll);

                    String name = StringBaseOpt.castObjectToString(sourceMap.get("name"));
                    Integer age = sourceMap.get("age") instanceof Number ?
                        ((Number) sourceMap.get("age")).intValue() : null;
                    if (name != null && age != null) {
                        System.out.format("name:%s ,age :%d \n", name, age);
                    }
                }
            }
        }
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

    //@Test
    public void Obj2json() throws IOException, SAXException, TikaException {
        ObjectDocument indexDocument = setIndexDocument();
        String jsonString = JSONObject.toJSONString(indexDocument);

        IndexRequest<JsonData> request = IndexRequest.of(i -> i
            .index("test")
            .document(JsonData.fromJson(jsonString)));

        IndexResponse indexResponse = client.index(request);
        System.out.println("id:" + indexResponse.id());
    }

    public void close() throws IOException {
        if (client != null && client._transport() != null) {
            client._transport().close();
        }
    }
}
