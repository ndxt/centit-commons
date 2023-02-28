package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.centit.search.document.ObjectDocument;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.Indexer;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.search.service.Searcher;
import com.centit.search.utils.TikaTextExtractor;
import com.centit.support.algorithm.UuidOpt;
import org.apache.http.HttpHost;
import org.apache.tika.exception.TikaException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by codefan on 17-6-22.
 */
public class ESTest {

    public static void main(String[] args) {

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
                new HttpHost("192.168.134.10", 9200, "http")
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
        ESServerConfig config = IndexerSearcherFactory.loadESServerConfigFormProperties("/src/test/resources/system.properties");
        Searcher searcher = IndexerSearcherFactory.obtainSearcher(config,
                ObjectDocument.class);
        searcher.search("",1,10);
    }

    //@Test
    public void testTiki() throws TikaException, IOException, SAXException {
        TikaTextExtractor.extractFileText("C:\\Users\\zhang_gd\\Desktop\\sola\\testTypeFile\\read\\10.地面服务部部门管理手册 .pdf");
    }
}
