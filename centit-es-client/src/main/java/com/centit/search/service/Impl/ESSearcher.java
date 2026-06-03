package com.centit.search.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.annotation.ESField;
import com.centit.search.document.DocumentUtils;
import com.centit.search.service.ElasticsearchClientFactory;
import com.centit.search.service.Searcher;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.compiler.Lexer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by codefan on 17-6-12.
 */
public class ESSearcher implements Searcher{

    public static final String SELF_ORDER_BY = "ORDER_BY";
    public static final String SELF_ORDER_BY2 = "orderBy";
    public static final String TABLE_SORT_FIELD = "sort";
    public static final String TABLE_SORT_ORDER = "order";

    private static final Logger logger = LoggerFactory.getLogger(ESSearcher.class);

    private ElasticsearchClient client;

    @Getter
    private String indexName;
    @Setter
    private String[] highlightPreTags;
    @Setter
    private String[] highlightPostTags;

    private final List<String> allFields;
    private Map<String, Float> queryFields;
    private final Set<String> highlightFields;
    private final Set<String> dateFields; // 存储日期类型字段

    public ESSearcher(ElasticsearchClient client){
        this.highlightFields = new HashSet<>();
        this.highlightPreTags = new String[]{"<strong>"};
        this.highlightPostTags = new String[]{"</strong>"};
        this.allFields = new ArrayList<>();
        this.dateFields = new HashSet<>();
        this.client = client;
    }

    public ElasticsearchClient fetchClient() {
        return client;
    }

    public void releaseClient(){
        ElasticsearchClientFactory.closeClient(client);
    }

    public void initTypeFields(Class<?> objType) {
        String indexName = DocumentUtils.obtainDocumentIndexName(objType);
        if(indexName!=null){
            initTypeFields(indexName, objType);
        }
    }

    public void initTypeFields(String indexName ,Class<?> objType) {
        this.indexName =indexName;
        Set<String> qf = new HashSet<>();

        Field[] objFields = objType.getDeclaredFields();
        for(Field field :objFields){
            if(field.isAnnotationPresent(ESField.class)){
                ESField esType = field.getAnnotation(ESField.class);
                if(esType.query()){
                    qf.add(field.getName());
                }
                if(esType.highlight()){
                    highlightFields.add(field.getName());
                }
                // 记录日期类型字段
                if("date".equals(esType.type())){
                    dateFields.add(field.getName());
                }
                allFields.add(field.getName());
            }
        }

        queryFields = new HashMap<>();
        for(String f :qf){
            queryFields.put(f, 1.f);
        }
    }

    public Pair<Long, List<Map<String, Object>>> esSearch(Query query, List<SortOptions> sortOptions,
                                                         String[] includes, String[] excludes,
                                                         int pageNo, int pageSize){
        long totalHits = 0;
        try {
            List<Map<String, Object>> retList = new ArrayList<>(pageSize + 5);

            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                .index(indexName)
                .query(query);

            // 添加分页
            if(pageSize > 0) {
                searchBuilder.from((pageNo > 1) ? (pageNo - 1) * pageSize : 0)
                    .size(pageSize);
            }

            // 添加排序
            if(sortOptions != null && !sortOptions.isEmpty()) {
                searchBuilder.sort(sortOptions);
            }

            // 添加高亮
            if(!highlightFields.isEmpty()) {
                Map<String, HighlightField> highlightFieldMap = new HashMap<>();
                for (String hf : highlightFields) {
                    highlightFieldMap.put(hf, HighlightField.of(h -> h
                        .fragmentSize(Searcher.SEARCH_FRAGMENT_SIZE)
                        .numberOfFragments(Searcher.SEARCH_FRAGMENT_NUM)));
                }

                Highlight highlight = Highlight.of(h -> h
                    .fields(highlightFieldMap)
                    .preTags(Arrays.asList(this.highlightPreTags))
                    .postTags(Arrays.asList(this.highlightPostTags)));

                searchBuilder.highlight(highlight);
            }

            // 添加字段过滤
            if(includes != null || excludes != null) {
                searchBuilder.source(s -> s
                    .filter(f -> f
                        .includes(includes != null ? Arrays.asList(includes) : Collections.emptyList())
                        .excludes(excludes != null ? Arrays.asList(excludes) : Collections.emptyList())));
            }

            SearchResponse<JsonData> response = client.search(searchBuilder.build(), JsonData.class);

            if (response.hits() != null && response.hits().total() != null) {
                totalHits = response.hits().total().value();

                for (Hit<JsonData> hit : response.hits().hits()) {
                    Map<String, Object> json;
                    if (hit.source() != null) {
                        // 将 JsonData 转换为 JSON 字符串，然后使用 fastjson 解析
                        // 这样可以保持日期字段的原始格式（ISO 8601字符串），而不是转换为 long
                        String jsonStr = hit.source().toJson().toString();
                        json = JSONObject.parseObject(jsonStr);
                        // 将日期类型字段从 long 转换为日期字符串
                        if (!dateFields.isEmpty()) {
                            for (String dateFieldName : dateFields) {
                                Object value = json.get(dateFieldName);
                                if (value instanceof Number) {
                                    long timestamp = ((Number) value).longValue();
                                    // 转换为 ISO 8601 格式的日期字符串
                                    json.put(dateFieldName,
                                        DatetimeOpt.convertDateToString(new Date(timestamp), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
                                }
                            }
                        }
                    } else {
                        json = new HashMap<>();
                    }
                    // 添加高亮信息
                    if (hit.highlight() != null && !hit.highlight().isEmpty()) {
                        StringBuilder content = new StringBuilder();
                        for (Map.Entry<String, List<String>> highlight : hit.highlight().entrySet()) {
                            for (String fragment : highlight.getValue()) {
                                content.append(fragment);
                            }
                            content.append("\n");
                        }
                        json.put("highlight", content.toString());
                    }

                    json.put("_score", hit.score());
                    json.put("_id", hit.id());
                    retList.add(json);
                }
            }
            return new ImmutablePair<>(totalHits, retList);
        } catch (Exception e) {
            throw new ObjectException(ObjectException.UNKNOWN_EXCEPTION,
                "查询ES失败:" + e.getMessage(), e);
        }
    }

    public Pair<Long, List<Map<String, Object>>> esSearch(Query query, int pageNo, int pageSize) {
        return esSearch(query, null, null, null, pageNo, pageSize);
    }

    public Pair<Long, List<Map<String, Object>>> esSearch(Query query, List<SortOptions> sortOptions,
                                                         int pageNo, int pageSize) {
        return esSearch(query, sortOptions, null, null, pageNo, pageSize);
    }

    public static List<SortOptions> mapSortBuilder(Map<String, Object> filterMap) {
        if(filterMap == null || filterMap.isEmpty()){
            return null;
        }
        List<SortOptions> sortOptions = new ArrayList<>();
        String selfOrderBy = StringBaseOpt.objectToString(filterMap.get(SELF_ORDER_BY));
        if(StringUtils.isBlank(selfOrderBy)){
            selfOrderBy = StringBaseOpt.objectToString(filterMap.get(SELF_ORDER_BY2));
        }
        if (StringUtils.isNotBlank(selfOrderBy)) {
            Lexer lexer = new Lexer(selfOrderBy, Lexer.LANG_TYPE_SQL);
            String aWord = lexer.getAWord();
            while (StringUtils.isNotBlank(aWord)) {
                String field = aWord;
                aWord = lexer.getAWord();
                final SortOrder sortOrder = "desc".equalsIgnoreCase(aWord)?  SortOrder.Desc : SortOrder.Asc;
                sortOptions.add(SortOptions.of(s -> s.field(f -> f.field(field).order(sortOrder))));
                while(StringUtils.equalsAnyIgnoreCase(aWord, "desc", "asc", ",")){
                    aWord = lexer.getAWord();
                }
            }
        }

        String sortField = StringBaseOpt.objectToString(filterMap.get(TABLE_SORT_FIELD));
        if (StringUtils.isNotBlank(sortField)) {
            String sOrder = StringBaseOpt.objectToString(filterMap.get(TABLE_SORT_ORDER));
            SortOrder sortOrder = "desc".equalsIgnoreCase(sOrder) ? SortOrder.Desc : SortOrder.Asc;
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field(sortField).order(sortOrder))));
        }
        return sortOptions;
    }

    public static String buildWildcardQuery(String sMatch) {
        StringBuilder sRes = new StringBuilder();
        char preChar = '#', curChar;
        boolean haveStar = false;
        int sL = sMatch.length();
        for (int i = 0; i < sL; i++) {
            curChar = sMatch.charAt(i);
            if ((curChar == ' ') || (curChar == '\t') || (curChar == '%') ||
                    (curChar == '*') || (curChar == '?') || (curChar == '_')) {
                if (preChar != '*') {
                    sRes.append('*');
                    preChar = '*';
                    haveStar = true;
                }
            } else {
                sRes.append(curChar);
                preChar = curChar;
            }
        }
        if (!haveStar)
            sRes.append('*');
        return sRes.toString();
    }

    public static void makeFilterCondition(String field, Object filterValue, BoolQuery.Builder boolQueryBuilder){
        String fieldSuffix = "_UN";
        if(StringUtils.endsWithAny(field, "_gt", "_ge", "_lt", "_le", "_in", "_ni", "_eq", "_ne", "_lk")) {
            fieldSuffix = field.substring(field.length() - 3).toLowerCase();
            field = field.substring(0,field.length() - 3);
        }

        final String finalField = field;
        switch (fieldSuffix) {
            case "_gt":
                boolQueryBuilder.must(q -> q.range(r -> r.field(finalField).gt(JsonData.of(filterValue))));
                break;
            case "_ge":
                boolQueryBuilder.must(q -> q.range(r -> r.field(finalField).gte(JsonData.of(filterValue))));
                break;
            case "_lt":
                boolQueryBuilder.must(q -> q.range(r -> r.field(finalField).lt(JsonData.of(filterValue))));
                break;
            case "_le":
                boolQueryBuilder.must(q -> q.range(r -> r.field(finalField).lte(JsonData.of(filterValue))));
                break;
            case "_ne":
                if (filterValue == null || "null".equals(String.valueOf(filterValue)) || StringUtils.isBlank(String.valueOf(filterValue))) {
                    boolQueryBuilder.must(q -> q.exists(e -> e.field(finalField)));
                } else {
                    boolQueryBuilder.mustNot(q -> q.term(t -> t.field(finalField).value(String.valueOf(filterValue))));
                }
                break;
            case "_ni":
                if (filterValue == null || "null".equals(String.valueOf(filterValue)) || StringUtils.isBlank(String.valueOf(filterValue))) {
                    boolQueryBuilder.must(q -> q.exists(e -> e.field(finalField)));
                } else {
                    String[] values = StringBaseOpt.objectToStringArray(filterValue);
                    List<FieldValue> valueList = new ArrayList<>();
                    for (String value : values) {
                        valueList.add(FieldValue.of(value));
                    }
                    boolQueryBuilder.mustNot(q -> q.terms(t -> t.field(finalField).terms(f -> f.value(valueList))));
                }
                break;
            case "_lk":{ // like 模糊查询
                String value = StringBaseOpt.objectToString(filterValue);
                // 使用通配符查询，支持 * 和 ? 通配符
                boolQueryBuilder.must(q -> q.wildcard(w -> w.field(finalField).value(value)));
                break;
            }
            case "_in":
            default: // _eq
                if (filterValue == null || "null".equals(String.valueOf(filterValue)) || StringUtils.isBlank(String.valueOf(filterValue))) {
                    boolQueryBuilder.mustNot(q -> q.exists(e -> e.field(finalField)));
                } else {
                    String[] values = StringBaseOpt.objectToStringArray(filterValue);
                    if (values.length > 1) {
                        List<FieldValue> valueList = new ArrayList<>();
                        for (String value : values) {
                            valueList.add(FieldValue.of(value));
                        }
                        boolQueryBuilder.must(q -> q.terms(t -> t.field(finalField).terms(f -> f.value(valueList))));
                    } else if (values.length == 1) {
                        boolQueryBuilder.must(q -> q.term(t -> t.field(finalField).value(values[0])));
                    }
                }
        }
    }

    @Override
    public Pair<Long, List<Map<String, Object>>> search(Map<String, Object> fieldFilter,
                                                       String queryWord, int pageNo, int pageSize){
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        if(fieldFilter != null) {
            for (Map.Entry<String, Object> ent : fieldFilter.entrySet()) {
                boolean isField = false;
                for(String fieldName : allFields) {
                    if(ent.getKey().startsWith(fieldName)) {
                        isField = true;
                        break;
                    }
                }
                if(!isField) continue;
                ESSearcher.makeFilterCondition(ent.getKey(), ent.getValue(), boolBuilder);
            }
        }

        if (StringUtils.isNotBlank(queryWord)) {
            QueryStringQuery.Builder queryStringBuilder = new QueryStringQuery.Builder()
                .query(queryWord);
            for (Map.Entry<String, Float> entry : queryFields.entrySet()) {
                queryStringBuilder.fields(entry.getKey());//, entry.getValue()
            }
            boolBuilder.filter(queryStringBuilder.build()._toQuery());
        }

        Query query = boolBuilder.build()._toQuery();
        return esSearch(query, mapSortBuilder(fieldFilter), pageNo, pageSize);
    }

    @Override
    public Pair<Long, List<Map<String, Object>>> search(String queryWord, int pageNo, int pageSize) {
        return search(null, queryWord, pageNo, pageSize);
    }

    @Override
    public Pair<Long, List<Map<String, Object>>> searchOpt(String optId,
                                                          String queryWord, int pageNo, int pageSize) {
        return search(CollectionsOpt.createHashMap("optId", optId),
            queryWord, pageNo, pageSize);
    }

    @Override
    public Pair<Long, List<Map<String, Object>>> searchOwner(String owner,
                                                            String queryWord, int pageNo, int pageSize) {
        return search(CollectionsOpt.createHashMap("userCode", owner),
            queryWord, pageNo, pageSize);
    }

    @Override
    public Pair<Long, List<Map<String, Object>>> searchOwner(String owner, String optId,
                                                            String queryWord, int pageNo, int pageSize){
        return search(CollectionsOpt.createHashMap("userCode", owner,"optId", optId),
            queryWord, pageNo, pageSize);
    }

    @Override
    public Pair<Long, List<Map<String, Object>>> searchUnits(String[] units,
                                                            String queryWord, int pageNo, int pageSize) {
        return search(CollectionsOpt.createHashMap("unitCode", units),
            queryWord, pageNo, pageSize);
    }

    @Override
    public Pair<Long, List<Map<String, Object>>> searchUnits(String[] units,
                                                            String optId, String queryWord, int pageNo, int pageSize) {
        return search(CollectionsOpt.createHashMap("optId", optId, "unitCode", units),
            queryWord, pageNo, pageSize);
    }

    @Override
    public JSONObject getDocumentById(String idFieldName, String docId) {
        try {
            TermQuery termQuery = TermQuery.of(t -> t.field(idFieldName).value(docId));
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(termQuery._toQuery()));

            SearchResponse<JsonData> response = client.search(searchRequest, JsonData.class);

            if(!response.hits().hits().isEmpty()) {
                Hit<JsonData> hit = response.hits().hits().get(0);
                if (hit.source() != null) {
                    return JSONObject.parseObject(hit.source().toJson().toString());
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("查询异常,异常信息：" + e.getMessage());
            return null;
        }
    }
}
