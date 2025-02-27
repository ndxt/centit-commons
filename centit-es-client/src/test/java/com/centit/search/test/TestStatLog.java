package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TestStatLog {

    private static final Logger logger = LogManager.getLogger(TestStatLog.class);
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
        return IndexerSearcherFactory.obtainSearcher(esServerConfig, CallApiLog.class);
    }


    public static List<Map<String, Object>> listLogs(String taskId, Date startDate, Date endDate) throws IOException {
        SearchRequest searchRequest = new SearchRequest("callapilog");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建过滤条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery("taskId", taskId));

        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("runBeginTime")
            .gte(startDate)
            .lte(endDate);
        boolQuery.must(rangeQuery);

        SearchSourceBuilder searchSourceBuilder = sourceBuilder.query(boolQuery);
        List<Map<String, Object>> retList = new ArrayList<>();
        searchRequest.source(searchSourceBuilder);
        SearchResponse actionGet = createSearch().fetchClient().search(searchRequest,RequestOptions.DEFAULT);
        SearchHits hits = actionGet.getHits();
        if (hits != null) {
            SearchHit[] hitsRes = hits.getHits();
            if (hitsRes != null) {
                for (SearchHit hit : hitsRes) {
                    Map<String, Object> json = hit.getSourceAsMap();
                    if (json != null) {
                        retList.add(json);
                    }
                }
            }
        }
        return retList;
    }


    public static Map<String, Long> getLogStatistics(String taskId, Date startDate, Date endDate) throws IOException {
        SearchRequest searchRequest = new SearchRequest("callapilog");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建过滤条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery("taskId", taskId));

        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("runBeginTime")
            .gte(startDate)
            .lte(endDate);
        boolQuery.must(rangeQuery);

        sourceBuilder.query(boolQuery);

        // 构建聚合
        DateHistogramAggregationBuilder dateHistogramAggregation = AggregationBuilders.dateHistogram("hourly")
            .field("runBeginTime")
            .interval(60000L);
        ValueCountAggregationBuilder countAggregation = AggregationBuilders.count("count").field("taskId");
        dateHistogramAggregation.subAggregation(countAggregation);

        sourceBuilder.aggregation(dateHistogramAggregation);

        searchRequest.source(sourceBuilder);
        RestHighLevelClient client = createSearch().fetchClient(); // 假设 ESSearcher 有 getClient 方法
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Map<String, Long> result = new HashMap<>();

        Histogram hourlyHistogram = searchResponse.getAggregations().get("hourly");
        for (Histogram.Bucket hourlyBucket : hourlyHistogram.getBuckets()) {
            String keyAsString = hourlyBucket.getKeyAsString();
            ParsedValueCount count = hourlyBucket.getAggregations().get("count");
            result.put(keyAsString, count.getValue());
        }

        return result;
    }

    public static void main(String[] args)  throws IOException {
        Map<String, Long>  map = getLogStatistics("bb08bb1dea024ddebd8e84d65173564d",
            DatetimeOpt.createUtilDate(2025,2,18, 9, 0, 0),
            DatetimeOpt.createUtilDate(2025,2,20, 9, 0, 0));
        /*List<Map<String, Object>> logs = listLogs("bb08bb1dea024ddebd8e84d65173564d",
            DatetimeOpt.createUtilDate(2025,2,18, 9, 0, 0),
            DatetimeOpt.currentUtilDate());*/
        System.out.println(JSON.toJSONString(map));
    }
}
