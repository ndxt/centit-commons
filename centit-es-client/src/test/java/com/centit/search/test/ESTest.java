package com.centit.search.test;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.document.ObjectDocument;
import com.centit.search.service.*;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.utils.TikaTextExtractor;
import com.centit.support.algorithm.UuidOpt;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-6-22.
 */
public class ESTest {

    public static void main(String[] args) {
        try{
            testQuery();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void testQuery() throws Exception {
        //ESServerConfig config = IndexerSearcherFactory.loadESServerConfigFormProperties("/src/test/resources/system.properties");
        ElasticConfig config = new ElasticConfig();
        config.setServerHostIp("192.168.134.250");
        config.setServerHostPort("32590");
        config.setClusterName("elastic");
        //config.setUsername("elastic");
        config.setPassword("********");
        /*config.setIndexName(
                StringUtils.lowerCase(properties.getProperty("elasticsearch.index")));*/
        config.setMinScore(0.5f);


        ElasticsearchClient esClient = ElasticsearchClientManager.getClient(config);

        // 构建多字段匹配查询
        MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
            .query("交通")
            .fields("xiangmmc", "guanjc", "content")
            .minimumShouldMatch("50%"));

        BoolQuery boolQuery = BoolQuery.of(b -> b
            .must(multiMatchQuery._toQuery()));

        // 构建高亮配置
        Map<String, HighlightField> highlightFields = new HashMap<>();
        highlightFields.put("content", HighlightField.of(h -> h
            .fragmentSize(300)
            .numberOfFragments(6)));
        highlightFields.put("xiangmmc", HighlightField.of(h -> h
            .fragmentSize(300)
            .numberOfFragments(6)));

        Highlight highlight = Highlight.of(h -> h
            .fields(highlightFields)
            .preTags("<span style='color:red'>")
            .postTags("</span>"));

        // 构建搜索请求
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("jsjtkj_index")
            .query(boolQuery._toQuery())
            .from(0)
            .size(10)
            .highlight(highlight)
            .trackTotalHits(th -> th.enabled(true)));

        SearchResponse<JsonData> searchResponse = esClient.search(searchRequest, JsonData.class);

        JSONArray jsonArray = returnHighlightResult(searchResponse, true);
        System.out.println(jsonArray.toJSONString());
    }
    private static JSONArray returnHighlightResult(SearchResponse<JsonData> searchResponse, Boolean explain) {
        JSONArray jsonArray = new JSONArray();

        if (searchResponse.hits() != null) {
            for (Hit<JsonData> hit : searchResponse.hits().hits()) {
                JSONObject jsonObject = new JSONObject();

                // 获取原始数据
                if (hit.source() != null) {
                    jsonObject = JSONObject.parseObject(hit.source().toJson().toString());
                }

                if (explain && hit.explanation() != null) {
                    jsonObject.put("explain_info", hit.explanation());
                }

                // 解析高亮字段
                if (hit.highlight() != null && !hit.highlight().isEmpty()) {
                    for (Map.Entry<String, List<String>> ent : hit.highlight().entrySet()) {
                        List<String> fragments = ent.getValue();
                        if (fragments != null && !fragments.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (String fragment : fragments) {
                                sb.append(fragment.trim());
                            }
                            // 高亮标题覆盖原标题
                            jsonObject.put(ent.getKey(), sb.toString());
                        }
                    }
                }

                jsonObject.put("_score", hit.score());
                jsonObject.put("_id", hit.id());
                jsonArray.add(jsonObject);
            }
        }
        return jsonArray;
    }

    public static void testESIndex3(){
        ElasticConfig config = IndexerSearcherFactory.loadESServerConfigFormProperties("/src/test/resources/system.properties");
        ESIndexer indexer = IndexerSearcherFactory.obtainIndexer(config, ObjectDocument.class);
        //testESIndex2();
        ObjectDocument obj= new ObjectDocument();
        obj.setOptId("ABC");
        obj.setOptTag(UuidOpt.getUuidAsString22());
        obj.setContent("测试我的索引，使用 elasticsearch-rest-high-level-client");
        indexer.saveNewDocument(obj);
        System.out.println("Done!");
    }

    public static void testESIndex2(){
        try {
            ElasticConfig config = new ElasticConfig();
            config.setServerHostIp("192.168.134.250");
            config.setServerHostPort("32404");

            ElasticsearchClient client = ElasticsearchClientManager.getClient(config);

            // 构建文档数据
            final Map<String, Object> map = new HashMap<>();
            map.put("user", "hainet");
            map.put("message", "elasticsearch-java-client-sample");

            final IndexRequest<JsonData> request = IndexRequest.of(i -> i
                .index("index")
                .id("id")
                .document(JsonData.of(map)));

            final IndexResponse response = client.index(request);

            System.out.println("Index: " + response.index());
            System.out.println("ID: " + response.id());
            System.out.println("Result: " + response.result());

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    //@Test
    public  void testESIndex() throws Exception {
        ElasticConfig config = IndexerSearcherFactory.loadESServerConfigFormProperties("/src/test/resources/system.properties");
        Indexer indexer = IndexerSearcherFactory.obtainIndexer(config,
                ObjectDocument.class);
       ObjectDocument indexDocument = new ObjectDocument();
       //String[] keyWord = {"Object","IJ"};
       indexDocument.setOsId("esxm");
       indexDocument.setOptId("IJ");
       indexDocument.setOptTag("IJ_OptTag");
       indexDocument.setOptMethod("IJ_OptMethod");
       indexDocument.setUserCode("zgd");
       indexDocument.setTitle("智能问答");
       //indexDocument.setKeywords(keyWord);
       indexDocument.setCreateTime(new Date());
       //String content = TikaTextExtractor.extractFileText("C:\\Users\\zhang_gd\\Desktop\\sola\\testTypeFile\\read\\IJ使用文档.docx");
       indexDocument.setContent(JSON.toJSONString(indexDocument));
       indexer.saveNewDocument(indexDocument);
    }

    //@Test
    public void testESSearch()throws Exception
    {
//        System.out.println( DocumentUtils.obtainDocumentType(ObjectDocument.class));
//        System.out.println( DocumentUtils.obtainDocumentMapping(ObjectDocument.class));
        ElasticConfig config =
            IndexerSearcherFactory.loadESServerConfigFormProperties("/src/test/resources/system.properties");
        Searcher searcher = IndexerSearcherFactory.obtainSearcher(config,
                ObjectDocument.class);
        searcher.search("",1,10);
    }

    //@Test
    public void testTiki() throws TikaException, IOException, SAXException {
        TikaTextExtractor.extractFileText("C:\\Users\\zhang_gd\\Desktop\\sola\\testTypeFile\\read\\10.地面服务部部门管理手册 .pdf");
    }
}
