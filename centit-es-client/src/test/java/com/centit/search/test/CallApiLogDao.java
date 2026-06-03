package com.centit.search.test;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.service.ElasticConfig;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class CallApiLogDao {

    private final static Logger logger = LoggerFactory.getLogger(CallApiLogDao.class);

    private final ESIndexer callApiLogIndexer;
    private final ESSearcher callApiLogSearcher;
    private final ESIndexer callApiLogDetailIndexer;
    private final ESSearcher callApiLogDetailSearcher;

    public CallApiLogDao(ElasticConfig systemLogEs) {
        this.callApiLogIndexer = IndexerSearcherFactory.obtainIndexer(systemLogEs, CallApiLog.class);
        this.callApiLogSearcher = IndexerSearcherFactory.obtainSearcher(systemLogEs, CallApiLog.class);
        this.callApiLogDetailIndexer = IndexerSearcherFactory.obtainIndexer(systemLogEs, CallApiLogDetails.class);
        this.callApiLogDetailSearcher = IndexerSearcherFactory.obtainSearcher(systemLogEs, CallApiLogDetails.class);
    }

    public void saveLog(CallApiLog callApiLog) {
        if(StringUtils.length(callApiLog.getOtherMessage())> 10240){
            logger.error("日志信息 ：" + callApiLog.getTaskId()+
                " otherMessage length is ：" + StringUtils.length(callApiLog.getOtherMessage()));
        }
        callApiLogIndexer.saveNewDocument(callApiLog);
    }

    public void saveLogDetails(CallApiLog callApiLog) {
        CallApiLogDetails details = new CallApiLogDetails();
        details.setLogId(callApiLog.getLogId());
        details.setTaskId(callApiLog.getTaskId());
        details.setRunBeginTime(callApiLog.getRunBeginTime());
        int logDetailCount = callApiLog.getDetailLogs().size();
        //保留100条日志明细, 前90条，后10条
        if(logDetailCount > 100){
            List<CallApiLogDetail> detailLogs = new ArrayList<>();
            for(int i=0; i<90; i++){
                detailLogs.add(callApiLog.getDetailLogs().get(i));
            }
            CallApiLogDetail separate = new CallApiLogDetail();
            separate.setStepNo(91);
            separate.setOptNodeId("separate line");
            separate.setLogType("info");
            separate.setRunBeginTime(new Date());
            separate.setRunEndTime(new Date());
            separate.setLogInfo("日志明细共"+logDetailCount+"条，中间"+(logDetailCount-100)+"条被截断，仅保留100条，前90条，后10条");
            separate.setErrorPieces(0);
            separate.setSuccessPieces(0);
            detailLogs.add(separate);
            for(int i=10; i>=1; i--){
                detailLogs.add(callApiLog.getDetailLogs().get(logDetailCount-i));
            }
            details.setDetailLogs(detailLogs);
        } else {
            details.setDetailLogs(callApiLog.getDetailLogs());
        }
        callApiLogDetailIndexer.saveNewDocument(details);
    }

    public CallApiLog getLog(String logId) {
        JSONObject object = callApiLogSearcher.getDocumentById("logId", logId);
        if(object==null){
            return null;
        }
        return object.toJavaObject(CallApiLog.class);
    }

    public List<CallApiLogDetail> listLogDetails(String logId) {
        JSONObject object = callApiLogDetailSearcher.getDocumentById("logId", logId);
        if(object==null){
            return null;
        }
        return JSONArray.parseArray(object.getString("detailLogs"), CallApiLogDetail.class);
    }

    public CallApiLog getLogWithDetail(String logId) {
        CallApiLog callLog = getLog(logId);
        if(callLog==null){
            return null;
        }
        callLog.setDetailLogs(listLogDetails(logId));
        return callLog;
    }

    public void deleteLogById(String logId) {

    }

    public List<Map<String, Object>> listLogsByProperties(Map<String, Object> param, PageDesc pageDesc) {
        for(String sKey : param.keySet()){
            if(sKey.startsWith("runBeginTime") || sKey.startsWith("runEndTime")){
                param.computeIfPresent(sKey, (k, timeValue) -> convertLocalToUTC(timeValue.toString()));
            }
        }
        Pair<Long, List<Map<String, Object>>> queryOut =
            callApiLogSearcher.search(param, StringBaseOpt.objectToString(param.get("query")), pageDesc.getPageNo(), pageDesc.getPageSize());
        pageDesc.setTotalRows(queryOut.getLeft().intValue());
        List<Map<String, Object>> objectList =  queryOut.getRight();
        if(objectList==null) return null;
        for(Map<String, Object> obj : objectList){
            obj.put("runBeginTime", DatetimeOpt.smartPraseDate((String)obj.get("runBeginTime")));
            obj.put("runEndTime", DatetimeOpt.smartPraseDate((String)obj.get("runEndTime")));
        }
        return objectList;
    }

    private  String convertLocalToUTC(String localDateTimeStr) {
        // 解析本地时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeStr, formatter);
        // 假设输入是系统默认时区的时间
        ZonedDateTime zonedLocalTime = localDateTime.atZone(ZoneId.systemDefault());
        // 转换为UTC时区
        ZonedDateTime utcTime = zonedLocalTime.withZoneSameInstant(ZoneId.of("UTC"));
        // 添加毫秒部分（示例中为331）
        ZonedDateTime utcTimeWithMillis = utcTime.withNano(331 * 1_000_000);
        // 格式化为ISO 8601格式
        return utcTimeWithMillis.format(DateTimeFormatter.ISO_INSTANT);
    }

    public JSONArray statApiCallSum(String statType, String typeId, Date startDate, Date endDate){
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 构建过滤条件
        if("topUnit".equalsIgnoreCase(statType)) {
            boolQueryBuilder.must(TermQuery.of(t -> t.field("topUnit").value(typeId))._toQuery());
        } else if("application".equalsIgnoreCase(statType)) {
            boolQueryBuilder.must(TermQuery.of(t -> t.field("applicationId").value(typeId))._toQuery());
        } else if("opt".equalsIgnoreCase(statType)) {
            boolQueryBuilder.must(TermQuery.of(t -> t.field("optId").value(typeId))._toQuery());
        } else {
            boolQueryBuilder.must(TermQuery.of(t -> t.field("taskId").value(typeId))._toQuery());
        }

        boolQueryBuilder.must(RangeQuery.of(r -> r
            .field("runBeginTime")
            .gte(JsonData.of(startDate))
            .lte(JsonData.of(endDate)))._toQuery());

        //大于5天按照天聚合， 否则按照小时聚合
        String interval; String dateFormat;
        if(DatetimeOpt.calcSpanDays(startDate, endDate) >= 5 ){
            interval = "1d";
            dateFormat = DatetimeOpt.defaultDatePattern; // "yyyy-MM-dd";
        } else {
            interval = "1h";
            dateFormat = DatetimeOpt.datetimePattern;// "yyyy-MM-dd HH:mm:ss";
        }

        // 构建聚合 - 使用更简单的方式
        Aggregation dateHistogramAggregation = Aggregation.of(a -> a
            .dateHistogram(DateHistogramAggregation.of(d -> d
                .field("runBeginTime")
                .calendarInterval(CalendarInterval.valueOf(interval))
                .format(dateFormat)
                .timeZone(ZoneId.systemDefault().toString())))
            .aggregations("errorPiecesSum", SumAggregation.of(s -> s.field("errorPieces"))._toAggregation())
            .aggregations("successPiecesSum", SumAggregation.of(s -> s.field("successPieces"))._toAggregation()));

        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("callapilog")
            .query(boolQueryBuilder.build()._toQuery())
            .aggregations("hourly", dateHistogramAggregation));

        JSONArray result = new JSONArray();
        ElasticsearchClient client = callApiLogSearcher.fetchClient();
        try {
            SearchResponse<JsonData> searchResponse = client.search(searchRequest, JsonData.class);
            DateHistogramAggregate dateHistogramAgg = searchResponse.aggregations().get("hourly").dateHistogram();

            for (DateHistogramBucket bucket : dateHistogramAgg.buckets().array()) {
                String keyAsString = bucket.keyAsString();
                double errorPiecesValue = bucket.aggregations().get("errorPiecesSum").sum().value();
                double successPiecesValue = bucket.aggregations().get("successPiecesSum").sum().value();

                JSONObject sums = new JSONObject();
                sums.put("runBeginTime", keyAsString);
                sums.put("errorPieces", errorPiecesValue);
                sums.put("successPieces", successPiecesValue);

                result.add(sums);
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing statType: {} param: {}, start date: {}, end date: {}",
                statType, typeId, startDate, endDate, e);
        }
        return result;
    }

    public JSONArray statCallSumByOs(String osId, Date startDate, Date endDate){
        BoolQuery boolQuery = BoolQuery.of(b -> b
            .must(TermQuery.of(t -> t.field("applicationId").value(osId))._toQuery())
            .must(RangeQuery.of(r -> r
                .field("runBeginTime")
                .gte(JsonData.of(startDate))
                .lte(JsonData.of(endDate)))._toQuery()));

        // 构建聚合
        Aggregation dateHistogramAggregation = DateHistogramAggregation.of(d -> d
            .field("runBeginTime")
            .calendarInterval(CalendarInterval.Day)
            .format(DatetimeOpt.defaultDatePattern)
            .timeZone(ZoneId.systemDefault().toString()))._toAggregation();

        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("callapilog")
            .query(boolQuery._toQuery())
            .aggregations("daily", dateHistogramAggregation));

        JSONArray result = new JSONArray();
        ElasticsearchClient client = callApiLogSearcher.fetchClient();
        try {
            SearchResponse<JsonData> searchResponse = client.search(searchRequest, JsonData.class);
            DateHistogramAggregate dailyHistogram = searchResponse.aggregations().get("daily").dateHistogram();

            for (DateHistogramBucket dailyBucket : dailyHistogram.buckets().array()) {
                String keyAsString = dailyBucket.keyAsString();
                long docCount = dailyBucket.docCount();
                JSONObject sums = new JSONObject();
                sums.put("runBeginTime", keyAsString);
                sums.put("callSum", docCount);
                result.add(sums);
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing application: {}, start date: {}, end date: {}",
                osId, startDate, endDate, e);
        }
        return result;
    }

    public JSONArray statTopTask(String osId, String countType, int topSize, Date startDate, Date endDate)  {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder()
            .must(TermQuery.of(t -> t.field("applicationId").value(osId))._toQuery())
            .must(RangeQuery.of(r -> r
                .field("runBeginTime")
                .gte(JsonData.of(startDate))
                .lte(JsonData.of(endDate)))._toQuery());

        if("failed".equalsIgnoreCase(countType)) {
            boolQueryBuilder.must(RangeQuery.of(r -> r.field("errorPieces").gt(JsonData.of(0)))._toQuery());
        }

        // 构建聚合 - 按文档数量降序排序
        Aggregation termsAggregation = TermsAggregation.of(t -> t
            .field("taskId")
            .size(topSize))._toAggregation();

        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("callapilog")
            .query(boolQueryBuilder.build()._toQuery())
            .aggregations("top_task_ids", termsAggregation));

        JSONArray result = new JSONArray();
        ElasticsearchClient client = callApiLogSearcher.fetchClient();
        try {
            SearchResponse<JsonData> searchResponse = client.search(searchRequest, JsonData.class);
            StringTermsAggregate topTaskIds = searchResponse.aggregations().get("top_task_ids").sterms();

            for (StringTermsBucket bucket : topTaskIds.buckets().array()) {
                String keyAsString = bucket.key().stringValue();
                long docCount = bucket.docCount();

                result.add(CollectionsOpt.createHashMap("taskId", keyAsString, "callSum", docCount));
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing application: {}, countType: {}, start date: {}, end date: {}",
                osId, countType, startDate, endDate, e);
        }
        return result;
    }

    /**
     * 根据optId统计接口的响应时间和成功率（整体统计）
     * @param taskId 接口ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 统计结果
     */
    public JSONObject statApiEfficiency(String taskId, Date startDate, Date endDate){
        BoolQuery boolQuery = BoolQuery.of(b -> b
            .must(TermQuery.of(t -> t.field("taskId").value(taskId))._toQuery())
            .must(RangeQuery.of(r -> r
                .field("runBeginTime")
                .gte(JsonData.of(startDate))
                .lte(JsonData.of(endDate)))._toQuery()));

        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("callapilog")
            .query(boolQuery._toQuery())
            .size(1000)
            .source(sr -> sr.filter(f -> f
                .includes("runBeginTime", "runEndTime", "successPieces", "errorPieces"))));

        JSONObject result = new JSONObject();
        ElasticsearchClient client = callApiLogSearcher.fetchClient();
        long totalResponseTime = 0;
        long totalCount = 0;
        long totalSuccessPieces = 0;
        long totalErrorPieces = 0;

        try {
            SearchResponse<JsonData> searchResponse = client.search(searchRequest, JsonData.class);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

            // 遍历所有匹配的文档，计算总响应时间和成功率相关数据
            for (Hit<JsonData> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    JSONObject source = JSONObject.parseObject(hit.source().toJson().toString());

                    // 计算响应时间
                    String runBeginTimeStr = source.getString("runBeginTime");
                    String runEndTimeStr = source.getString("runEndTime");
                    if (runBeginTimeStr != null && runEndTimeStr != null) {
                        Instant startInstant = Instant.from(formatter.parse(runBeginTimeStr));
                        Instant endInstant = Instant.from(formatter.parse(runEndTimeStr));
                        long diffInMillis = endInstant.toEpochMilli() - startInstant.toEpochMilli();
                        totalResponseTime += diffInMillis;
                        totalCount++;
                    }

                    // 累加成功率相关数据
                    Integer successPieces = source.getInteger("successPieces");
                    Integer errorPieces = source.getInteger("errorPieces");
                    if (successPieces != null) {
                        totalSuccessPieces += successPieces;
                    }
                    if (errorPieces != null) {
                        totalErrorPieces += errorPieces;
                    }
                }
            }

            // 计算平均响应时间（毫秒）
            double avgResponseTime = totalCount > 0 ?
                (double) totalResponseTime / totalCount : 0;
            result.put("avgResponseTime", avgResponseTime);

            // 计算成功率
            long totalPieces = totalSuccessPieces + totalErrorPieces;
            double successRate = totalPieces > 0 ?
                (double) totalSuccessPieces / totalPieces : 0;
            result.put("successRate", successRate);

            // 添加总请求数
            result.put("totalCount", totalCount);

        } catch (Exception e) {
            logger.error("Error occurred while processing taskId: {}, start date: {}, end date: {}",
                taskId, startDate, endDate, e);
        }
        return result;
    }
}
