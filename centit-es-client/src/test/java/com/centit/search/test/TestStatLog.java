package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
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
            .interval(360000L) // 3600000 milliseconds = 1 hour
            .format("yyyy-MM-dd"); // 明确日期格式
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


    public static JSONArray statTopTask(String osId, String countType, int topSize, Date startDate, Date endDate)  {
        SearchRequest searchRequest = new SearchRequest("callapilog");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 构建过滤条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery("applicationId", osId));
        if("failed".equalsIgnoreCase(countType)) {
            boolQuery.must( QueryBuilders.rangeQuery("errorPieces").gt(0));
        }
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("runBeginTime")
            .gte(startDate)
            .lte(endDate);
        boolQuery.must(rangeQuery);

        sourceBuilder.query(boolQuery);

        // 构建聚合
        TermsAggregationBuilder termsAggregation = AggregationBuilders.terms("top_task_ids")
            .field("taskId")
            .size(topSize) // 只取前30个
            .order(BucketOrder.count(false)); // 按条目数降序排列

        sourceBuilder.aggregation(termsAggregation);
        JSONArray result = new JSONArray();
        searchRequest.source(sourceBuilder);
        try (RestHighLevelClient client = createSearch().fetchClient()) { // 使用 try-with-resources
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            ParsedTerms topTaskIds = searchResponse.getAggregations().get("top_task_ids");
            for (Terms.Bucket bucket : topTaskIds.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();
                long docCount = bucket.getDocCount();

                result.add(CollectionsOpt.createHashMap("taskId", keyAsString, "callSum", docCount));
            }
        } catch (IOException | ElasticsearchException e) { // 捕获更广泛的异常
            logger.error("Error occurred while processing application: {}, countType: {}, start date: {}, end date: {}",
                osId, countType, startDate, endDate, e);
        }
        return result;
    }

    public static JSONArray statCallSumByOs(String osId, Date startDate, Date endDate){
        SearchRequest searchRequest = new SearchRequest("callapilog");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建过滤条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery("applicationId", osId));
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("runBeginTime")
            .gte(startDate)
            .lte(endDate);
        boolQuery.must(rangeQuery);
        sourceBuilder.query(boolQuery);

        // 构建聚合
        DateHistogramAggregationBuilder dateHistogramAggregation = AggregationBuilders.dateHistogram("daily")
            .field("runBeginTime")
            .interval(86400000L) // 3600000 milliseconds = 1 hour
            .format("yyyy-MM-dd"); // 明确日期格式

        sourceBuilder.aggregation(dateHistogramAggregation);
        searchRequest.source(sourceBuilder);
        JSONArray result = new JSONArray();
        try (RestHighLevelClient client = createSearch().fetchClient()) { // 使用 try-with-resources
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            Histogram dailyHistogram = searchResponse.getAggregations().get("daily");
            for (Histogram.Bucket dailyBucket : dailyHistogram.getBuckets()) {
                String keyAsString = dailyBucket.getKeyAsString();
                long docCount = dailyBucket.getDocCount();
                JSONObject sums = new JSONObject();
                sums.put("runBeginTime", keyAsString); // daily
                sums.put("callSum", docCount);
                result.add(sums);
            }
        } catch (IOException | ElasticsearchException e) { // 捕获更广泛的异常
            logger.error("Error occurred while processing application: {}, start date: {}, end date: {}",
                osId, startDate, endDate, e);
        }

        return result;
    }

    public static void main(String[] args)  throws IOException {
        JSONArray  map = statTopTask("t_H4w2emTnq89GXxEN5Dsw", "all", 10,
            DatetimeOpt.createUtilDate(2025,1,18, 9, 0, 0),
            DatetimeOpt.createUtilDate(2025,2,20, 9, 0, 0));
        /*List<Map<String, Object>> logs = listLogs("bb08bb1dea024ddebd8e84d65173564d",
            DatetimeOpt.createUtilDate(2025,2,18, 9, 0, 0),
            DatetimeOpt.currentUtilDate());*/
        System.out.println(JSON.toJSONString(map));
    }
}
