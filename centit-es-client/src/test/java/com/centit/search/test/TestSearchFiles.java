package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.document.FileDocument;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TestSearchFiles {

    private static final Logger logger = LogManager.getLogger(TestSearchFiles.class);
    public static Properties loadProperties() {
        Properties prop = new Properties();
        try(InputStream resource = DocumentSearchTest
            .class.getResourceAsStream("/system.properties")){
            //new ClassPathResource("system.properties").getInputStream();
            if(resource==null) {
                try(InputStream resource2 = ClassLoader.getSystemResourceAsStream("/system.properties")){
                    if(resource2 != null) {
                        prop.load(resource2);
                    }
                }
            }else {
                prop.load(resource);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return prop;
    }
    public static ESSearcher createSearch(){
        ESServerConfig esServerConfig = IndexerSearcherFactory.loadESServerConfigFormProperties(
            loadProperties() );
        return IndexerSearcherFactory.obtainSearcher(esServerConfig, FileDocument.class);
    }

    public static void main(String[] args)  throws IOException {
        ESSearcher searcher = createSearch();
        Pair<Long, List<Map<String, Object>>>  p = searcher.search("\"数字化平台\"", 1, 10);
        System.out.println(JSON.toJSONString(p.getLeft()));
    }
}
