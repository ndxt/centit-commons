package com.centit.search.test;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.service.ElasticConfig;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.NumberBaseOpt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TestStatLog {

    private static final Logger logger = LogManager.getLogger(TestStatLog.class);

    public static Properties loadProperties() {
        Properties prop = new Properties();
        try(InputStream resource = DocumentSearchTest
            .class.getResourceAsStream("/system.properties")){
            if(resource == null) {
                try(InputStream resource2 = ClassLoader.getSystemResourceAsStream("/system.properties")){
                    if(resource2 != null) {
                        prop.load(resource2);
                    }
                }
            } else {
                prop.load(resource);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return prop;
    }

    public static ESSearcher createSearch(){
        ElasticConfig elasticConfig = IndexerSearcherFactory.loadESServerConfigFormProperties(
            loadProperties() );
        return IndexerSearcherFactory.obtainSearcher(elasticConfig, CallApiLog.class);
    }

    public static List<Map<String, Object>> listLogs(String taskId, Date startDate, Date endDate) throws IOException {
        // 构建查询条件
        TermQuery taskIdQuery = TermQuery.of(t -> t
            .field("taskId")
            .value(taskId));

        RangeQuery rangeQuery = RangeQuery.of(r -> r
            .field("runBeginTime")
            .gte(JsonData.of(startDate.getTime()))
            .lte(JsonData.of(endDate.getTime())));

        BoolQuery boolQuery = BoolQuery.of(b -> b
            .must(taskIdQuery._toQuery())
            .must(rangeQuery._toQuery()));

        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("callapilog")
            .query(boolQuery._toQuery()));

        List<Map<String, Object>> retList = new ArrayList<>();
        ElasticsearchClient client = createSearch().fetchClient();
        try {
            SearchResponse<JsonData> response = client.search(searchRequest, JsonData.class);
            if (response.hits() != null) {
                for (Hit<JsonData> hit : response.hits().hits()) {
                    if (hit.source() != null) {
                        Map<String, Object> sourceMap = hit.source().toJson().asJsonObject()
                            .entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                map.put(entry.getKey(), entry.getValue().toString());
                            }, HashMap::putAll);
                        retList.add(sourceMap);
                    }
                }
            }
        } finally {
            createSearch().releaseClient(client);
        }
        return retList;
    }

    public static Map<String, Long> getLogStatistics(String taskId, Date startDate, Date endDate) throws IOException {
        // 构建查询条件
        TermQuery taskIdQuery = TermQuery.of(t -> t
            .field("taskId")
            .value(taskId));

        RangeQuery rangeQuery = RangeQuery.of(r -> r
            .field("runBeginTime")
            .gte(JsonData.of(startDate.getTime()))
            .lte(JsonData.of(endDate.getTime())));

        BoolQuery boolQuery = BoolQuery.of(b -> b
            .must(taskIdQuery._toQuery())
            .must(rangeQuery._toQuery()));

        // 构建日期直方图聚合
        Aggregation dateHistogramAgg = Aggregation.of(a -> a
            .dateHistogram(DateHistogramAggregation.of(d -> d
                .field("runBeginTime")
                .fixedInterval(Time.of(t -> t.time("1h")))
                .format("yyyy-MM-dd")))
            .aggregations("count", Aggregation.of(sub -> sub
                .valueCount(ValueCountAggregation.of(v -> v.field("taskId"))))));

        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("callapilog")
            .query(boolQuery._toQuery())
            .aggregations("hourly", dateHistogramAgg)
            .size(0));

        Map<String, Long> result = new HashMap<>();
        ElasticsearchClient client = createSearch().fetchClient();
        try {
            SearchResponse<JsonData> response = client.search(searchRequest, JsonData.class);

            if (response.aggregations() != null) {
                DateHistogramAggregate hourlyAgg = response.aggregations()
                    .get("hourly")
                    .dateHistogram();

                for (DateHistogramBucket bucket : hourlyAgg.buckets().array()) {
                    String keyAsString = bucket.keyAsString();
                    ValueCountAggregate countAgg = bucket.aggregations()
                        .get("count")
                        .valueCount();
                    result.put(keyAsString, NumberBaseOpt.castObjectToLong(countAgg.value()));
                }
            }
        } finally {
            createSearch().releaseClient(client);
        }
        return result;
    }

    public static JSONArray statTopTask(String osId, String countType, int topSize, Date startDate, Date endDate) {
        JSONArray result = new JSONArray();

        try {
            // 构建查询条件
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder()
                .must(TermQuery.of(t -> t.field("applicationId").value(osId))._toQuery());

            if("failed".equalsIgnoreCase(countType)) {
                boolBuilder.must(RangeQuery.of(r -> r
                    .field("errorPieces")
                    .gt(JsonData.of(0)))._toQuery());
            }

            boolBuilder.must(RangeQuery.of(r -> r
                .field("runBeginTime")
                .gte(JsonData.of(startDate.getTime()))
                .lte(JsonData.of(endDate.getTime())))._toQuery());

            // 构建Terms聚合
            Aggregation termsAgg = Aggregation.of(a -> a
                .terms(TermsAggregation.of(t -> t
                    .field("taskId")
                    .size(topSize))));

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("callapilog")
                .query(boolBuilder.build()._toQuery())
                .aggregations("top_task_ids", termsAgg)
                .size(0));

            ElasticsearchClient client = createSearch().fetchClient();
            try {
                SearchResponse<JsonData> response = client.search(searchRequest, JsonData.class);

                if (response.aggregations() != null) {
                    StringTermsAggregate topTaskIds = response.aggregations()
                        .get("top_task_ids")
                        .sterms();

                    for (StringTermsBucket bucket : topTaskIds.buckets().array()) {
                        String keyAsString = bucket.key().stringValue();
                        long docCount = bucket.docCount();
                        result.add(CollectionsOpt.createHashMap("taskId", keyAsString, "callSum", docCount));
                    }
                }
            } finally {
                createSearch().releaseClient(client);
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing application: {}, countType: {}, start date: {}, end date: {}",
                osId, countType, startDate, endDate, e);
        }

        return result;
    }

    public static JSONArray statCallSumByOs(String osId, Date startDate, Date endDate) {
        JSONArray result = new JSONArray();

        try {
            // 构建查询条件
            TermQuery appIdQuery = TermQuery.of(t -> t
                .field("applicationId")
                .value(osId));

            RangeQuery rangeQuery = RangeQuery.of(r -> r
                .field("runBeginTime")
                .gte(JsonData.of(startDate.getTime()))
                .lte(JsonData.of(endDate.getTime())));

            BoolQuery boolQuery = BoolQuery.of(b -> b
                .must(appIdQuery._toQuery())
                .must(rangeQuery._toQuery()));

            // 构建日期直方图聚合
            Aggregation dateHistogramAgg = Aggregation.of(a -> a
                .dateHistogram(DateHistogramAggregation.of(d -> d
                    .field("runBeginTime")
                    .fixedInterval(Time.of(t -> t.time("1d")))
                    .format("yyyy-MM-dd"))));

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("callapilog")
                .query(boolQuery._toQuery())
                .aggregations("daily", dateHistogramAgg)
                .size(0));

            ElasticsearchClient client = createSearch().fetchClient();
            try {
                SearchResponse<JsonData> response = client.search(searchRequest, JsonData.class);

                if (response.aggregations() != null) {
                    DateHistogramAggregate dailyAgg = response.aggregations()
                        .get("daily")
                        .dateHistogram();

                    for (DateHistogramBucket bucket : dailyAgg.buckets().array()) {
                        String keyAsString = bucket.keyAsString();
                        long docCount = bucket.docCount();
                        JSONObject sums = new JSONObject();
                        sums.put("runBeginTime", keyAsString);
                        sums.put("callSum", docCount);
                        result.add(sums);
                    }
                }
            } finally {
                createSearch().releaseClient(client);
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing application: {}, start date: {}, end date: {}",
                osId, startDate, endDate, e);
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        JSONArray map = statTopTask("t_H4w2emTnq89GXxEN5Dsw", "all", 10,
            DatetimeOpt.createUtilDate(2025, 1, 18, 9, 0, 0),
            DatetimeOpt.createUtilDate(2025, 2, 20, 9, 0, 0));
        /*List<Map<String, Object>> logs = listLogs("bb08bb1dea024ddebd8e84d65173564d",
            DatetimeOpt.createUtilDate(2025,2,18, 9, 0, 0),
            DatetimeOpt.currentUtilDate());*/
        System.out.println(JSON.toJSONString(map));
    }
}
