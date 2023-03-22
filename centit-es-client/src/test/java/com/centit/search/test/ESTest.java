package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.document.ObjectDocument;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.Indexer;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.search.service.Searcher;
import com.centit.search.utils.TikaTextExtractor;
import com.centit.support.algorithm.BooleanBaseOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.UuidOpt;
import com.centit.support.json.JSONTransformer;
import com.centit.support.security.SecurityOptUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.tika.exception.TikaException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

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
        ESServerConfig config = new ESServerConfig();
        config.setServerHostIp("192.168.134.250");
        config.setServerHostPort("32404");
        config.setClusterName("centit");
        config.setUsername("elastic");
        config.setPassword("*********");
        /*config.setIndexName(
                StringUtils.lowerCase(properties.getProperty("elasticsearch.index")));*/
        config.setOsId("dde");
        config.setMinScore(0.5f);


        RestHighLevelClient esClient = IndexerSearcherFactory.obtainclientPool(config).borrowObject();
        //jsjtkj_index
        //过滤条件 filterColumnName  filterValue
        SearchRequest searchRequest = new SearchRequest("jsjtkj_index");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        MultiMatchQueryBuilder multiMatchQueryBuilder =
            QueryBuilders.multiMatchQuery("交通", "xiangmmc","guanjc","content") ;
        multiMatchQueryBuilder.minimumShouldMatch("50%");

        boolQueryBuilder.must(multiMatchQueryBuilder);


        //封装分页  排序信息
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);

        //searchSourceBuilder.fetchSource(null, new String[]{"content"});

        //设置查询超时时间 1分钟
        //searchSourceBuilder.timeout(new TimeValue(60*1000, TimeUnit.SECONDS));
        //查全部数据(如果不写或者写false当总记录数超过10000时会返回总数10000,配置为true就会返回真实条数)
        searchSourceBuilder.trackTotalHits(true);
        //explain 返回文档的评分解释
        searchSourceBuilder.explain(false);
        //设置高亮显示字段
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("content").field("xiangmmc");

        String color = "red";
        if (highlightBuilder.fields().size() > 0) {
            highlightBuilder.preTags("<span style='color:"+color+"'>").postTags("</span>");
            highlightBuilder.highlighterType("unified");
            highlightBuilder.requireFieldMatch(true);
            //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
            //最大高亮分片数
            highlightBuilder.fragmentSize(300);
            //从第一个分片获取高亮片段
            highlightBuilder.numOfFragments(6);
        }
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);


        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

        JSONArray jsonArray =
            returnHighlightResult(searchResponse,  true);
        System.out.println(jsonArray.toJSONString());
    }
    private static JSONArray returnHighlightResult(SearchResponse searchResponse, Boolean explain) {
        JSONArray jsonArray = new JSONArray();

        for (SearchHit hit : searchResponse.getHits()) {

            JSONObject jsonObject = JSON.parseObject(hit.getSourceAsString());

            if (explain) jsonObject.put("explain_info", hit.getExplanation());
            //解析高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            for (Map.Entry<String, HighlightField> ent : highlightFields.entrySet()) {
                HighlightField field = ent.getValue();
                if (field != null) {
                    Text[] fragments = field.fragments();
                    StringBuilder sb = new StringBuilder();
                    for (Text fragment : fragments) {
                        sb.append(fragment.string().trim());
                    }
                    //高亮标题覆盖原标题
                    jsonObject.put(ent.getKey(), sb.toString());
                }
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public static void testESIndex3(){
        ESServerConfig config = IndexerSearcherFactory.loadESServerConfigFormProperties("/src/test/resources/system.properties");
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
        try (final RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(
                new HttpHost("192.168.134.250", 32404, "http")
            )
        )) {
            // Map
            final Map<String, Object> map = new HashMap<>();
            map.put("user", "hainet");
            map.put("message", "elasticsearch-rest-high-level-client-sample");

            // XContentBuilder
            final XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.field("user", "hainet");
                builder.field("message", "elasticsearch-rest-high-level-client-sample");
            }
            builder.endObject();

            final IndexRequest request = new IndexRequest()
                .index("index")
                .type("logs")
                .id("id")
                .timeout(TimeValue.timeValueMinutes(2))
                // Map
                .source(map);
            // XContentBUilder
            // .source(builder);
            // Object key-pairs
            // .source("user", "hainet",
            //         "message", "elasticsearch-rest-high-level-client-sample")

            final IndexResponse response = client.index(request, RequestOptions.DEFAULT);

            //assertThat(response.getIndex(), is("index"));
            //assertThat(response.getType(), is("logs"));
            //assertThat(response.getId(), is("id"));

            if (response.getResult() == DocWriteResponse.Result.CREATED) {
                //assertThat(response.getVersion(), is(1L));
            } else if (response.getResult() == DocWriteResponse.Result.UPDATED) {
                //assertThat(response.getVersion(), is(greaterThan(1L)));
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    //@Test
    public  void testESIndex() throws Exception {
        ESServerConfig config = IndexerSearcherFactory.loadESServerConfigFormProperties("/src/test/resources/system.properties");
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
        ESServerConfig config =
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
