package com.centit.search.test;


import com.alibaba.fastjson.JSONObject;
import com.centit.search.document.ObjectDocument;
import com.centit.search.utils.TikaTextExtractor;
import org.apache.http.HttpHost;
import org.apache.tika.exception.TikaException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Before;
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
@SuppressWarnings("deprecated")
public class ElasticSearchTest {
    RestHighLevelClient client;
    @Before
    @SuppressWarnings({ "unchecked" })
    public void before() throws UnknownHostException, InterruptedException, ExecutionException {
        Settings esSettings = Settings.builder()
                .put("cluster.name", "elasticsearch_zgd") //设置ES实例的名称
                .put("client.transport.sniff", true) //自动嗅探整个集群的状态，把集群中其他ES节点的ip添加到本地的客户端列表中
                .build();
        //client = new PreBuiltTransportClient(esSettings);//初始化client较老版本发生了变化，此方法有几个重载方法，初始化插件等。
        //此步骤添加IP，至少一个，其实一个就够了，因为添加了自动嗅探配置
        //client.addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));
        client = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("192.168.134.9", 9200, "http")));
        System.out.println("success connect");
    }

    //@Test
    public void index() throws Exception {
        Map<String,Object> infoMap = new HashMap<String, Object>();
        infoMap.put("name", "广告信息11");
        infoMap.put("title", "我的广告22");
        infoMap.put("createTime", new Date());
        infoMap.put("count", 1022);
        //IndexResponse indexResponse = client.prepareIndex("test", "info").setSource(infoMap).execute().actionGet();
        IndexRequest request = new IndexRequest()
                    .index("test")
                    .type("info")
                    .source(infoMap);
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        System.out.println("id:"+indexResponse.getId());
    }

    //@Test
    public void get() throws Exception {
        GetRequest getRequest = new GetRequest("test", "info", "peVI2WoB192wz409binr");
        /*GetResponse response = client.prepareGet("test", "info", "AVyf5lkJDq8ZfYxZWJfE")
                .execute().actionGet();*/
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        System.out.println("response.getId():"+response.getId());
        System.out.println("response.getSourceAsString():"+response.getSourceAsString());
    }

    //@Test
    public void testGet() throws IOException {

//        String fields = "content";
//        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("git");
//        SearchResponse sr = client.prepareSearch("test").setTypes("info").addDocValueField(fields).setQuery(queryBuilder).execute().actionGet();
//        JSONArray array = new JSONArray();
//        SearchHits hits = sr.getHits();
//        for (SearchHit hit : hits.getHits()){
//            hit.getFields().entrySet();
//            System.out.println(hit.getSourceAsString());
//        }
        QueryBuilder queryBuilder2 = QueryBuilders.queryStringQuery("");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder2);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("objects");
        //searchRequest.types("info");
       // searchRequest.source(searchSourceBuilder);
        SearchResponse actionGet = client.search(searchRequest, RequestOptions.DEFAULT);
        /*SearchResponse actionGet = client
                .prepareSearch("test")
                .setTypes("info")
                .fields("content")
                //.setTypes("info")

                //.fields("content")
//                .addDocValueField("content")
//                .addStoredField("content")
//                .addFieldDataField("content")
                .setQuery(queryBuilder2)
                .execute()
                .actionGet();*/
        SearchHits hits = actionGet.getHits();
        System.out.println("查询到记录数=" + hits.getTotalHits());
//        List<Map<String, Object>> matchRsult = new LinkedList<>();
        for (SearchHit hit : hits.getHits()){
            /*Set<Map.Entry<String, DocumentField>> fieldEntry =  hit.getFields().entrySet();
            JSONObject json = new JSONObject();
            for(Map.Entry<String, DocumentField> entry:fieldEntry){
                json.put(entry.getValue().getName(), entry.getValue().getValue());
            }*/
            Map<String, Object> json = hit.getSourceAsMap();
            if(json==null) {
                json = new HashMap<>(4);
            }
            System.out.println(json.toString());
//            matchRsult.add(hit.getSource());
//            System.out.println(hit.getSourceAsString());
        }
    }

    //@Test
    public void queryGet() throws Exception {
        //term查询
//        QueryBuilder queryBuilder = QueryBuilders.termQuery("age", 50) ;
        //range查询
        QueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("age").gt(50);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(rangeQueryBuilder);
        searchSourceBuilder.sort("age", SortOrder.DESC);
        searchSourceBuilder.size(20);

        SearchRequest searchRequest = new SearchRequest()
                        .indices("sxq")
                        .types("user");
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
        /*SearchResponse searchResponse = client.prepareSearch("sxq")
                .setTypes("user")
                .setQuery(rangeQueryBuilder)
                .addSort("age", SortOrder.DESC)
                .setSize(20)
                .execute()
                .actionGet();*/
        SearchHits hits = searchResponse.getHits();
        System.out.println("查到记录数："+hits.getTotalHits());
        SearchHit[] searchHists = hits.getHits();
        if(searchHists.length>0){
            for(SearchHit hit:searchHists){
                String name =  (String)hit.getSourceAsMap().get("name");
                Integer age = (Integer)hit.getSourceAsMap().get("age");
                System.out.format("name:%s ,age :%d \n",name ,age);
            }
        }
    }


    public ObjectDocument setIndexDocument() throws TikaException, IOException, SAXException {
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
    public void Obj2json() throws TikaException, IOException, SAXException {
        ObjectDocument indexDocument = setIndexDocument();
        String aa = JSONObject.toJSONString(indexDocument);
        IndexRequest request = new IndexRequest("test", "info");
        //IndexResponse indexResponse = client.prepareIndex("test", "info").setSource(aa).execute().actionGet();
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        System.out.println("id:"+indexResponse.getId());

        /*ImmutableOpenMap<String, MappingMetaData> mappings = client.admin().cluster().prepareState().execute()
                .actionGet().getState().getMetaData().getIndices().get(config.getIndexName()).getMappings();
        for (ObjectObjectCursor<String, MappingMetaData> cursor : mappings) {
            System.out.println(cursor.key); // 索引下的每个type
            System.out.println(cursor.value.getSourceAsMap()); // 每个type的mapping
        }*/
    }

}
