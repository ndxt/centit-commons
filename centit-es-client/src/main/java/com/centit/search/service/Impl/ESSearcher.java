package com.centit.search.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.centit.search.annotation.ESField;
import com.centit.search.document.DocumentUtils;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Searcher;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.compiler.Lexer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by codefan on 17-6-12.
 */
public class ESSearcher implements Searcher{

    public static final String SELF_ORDER_BY = "ORDER_BY";

    public static final String SELF_ORDER_BY2 = "orderBy";
    /**
     * 用户自定义排序字段 ， 放到 filterDesc 中
     */
    public static final String TABLE_SORT_FIELD = "sort";
    /**
     * 用户自定义排序字段的排序顺序 ， 放到 filterDesc 中
     */
    public static final String TABLE_SORT_ORDER = "order";

    private static final Logger logger = LoggerFactory.getLogger(ESSearcher.class);

    private ESServerConfig config;

    private GenericObjectPool<ElasticsearchClient> clientPool;
    private String indexName;
    private List<String> highlightPreTags;
    private List<String> highlightPostTags;

    private List<String> queryFields;
    private final List<String> allFields;
    private final Set<String> highlightFields;


    public ESSearcher(){
        this.highlightFields = new HashSet<>();
        this.highlightPreTags = CollectionsOpt.createList("<strong>");
        this.highlightPostTags = CollectionsOpt.createList("</strong>");
        this.allFields = new ArrayList<>();
    }

    public ESSearcher(ESServerConfig config, GenericObjectPool<ElasticsearchClient> clientPool){
        this();
        this.config = config;
        this.clientPool = clientPool;
    }

    public String getIndexName() {
        return indexName;
    }
    public void setESServerConfig(ESServerConfig config){
        this.config = config;
    }

    public void setClientPool(GenericObjectPool<ElasticsearchClient> clientPool) {
        this.clientPool = clientPool;
    }

    public void initTypeFields(Class<?> objType) {
        String indexName = DocumentUtils.obtainDocumentIndexName(objType);
        if(indexName!=null){
            initTypeFields(indexName, objType);
        }
    }
    // 定义索引的映射类型
    public void initTypeFields(String indexName ,Class<?> objType) {
        this.indexName =indexName;
        Set<String> rf = new HashSet<>();
        //rf.add("_type");//添加这个必须返回的保留字段
        Set<String> qf = new HashSet<>();

        Field[] objFields = objType.getDeclaredFields();
        for(Field field :objFields){
            if(field.isAnnotationPresent(ESField.class)){
                ESField esType =
                    field.getAnnotation(ESField.class);
                if(esType.query()){
                    qf.add(field.getName());
                }
                if(esType.highlight()){
                    highlightFields.add(field.getName());
                }
                allFields.add(field.getName());
            }
        }//end of for
        queryFields = CollectionsOpt.cloneList(qf);
    }

    public BoolQuery.Builder mapSearchRequest(Map<String, Object> fieldFilter,
                                                        String queryWord){
        BoolQuery.Builder builder = new BoolQuery.Builder();
        builder.must(m -> m.match(t -> t.field("title").query("紧急")))
                .filter(f -> f.range(r -> r
                    .field("createTime")
                    .gte(JsonData.of("2023-01-01"))
                ));

        if(fieldFilter!=null) {
            for (Map.Entry<String, Object> ent : fieldFilter.entrySet()) {
                boolean isField = false;
                for(String fieldName : allFields) {
                    if(ent.getKey().startsWith(fieldName)) {
                        isField = true;
                        break;
                    }
                }
                if(!isField) continue;

                String key = ent.getKey();
                int keyLen = key.length();
                String optSuffix = keyLen>3 ? key.substring(keyLen - 3).toLowerCase() : "_eq";
                String fieldName = keyLen>3 && key.charAt(keyLen-3) == '_' ? key.substring(0, keyLen-3) : key;
                switch (optSuffix) {
                    case "_gt":
                        builder.filter(f -> f.range(r -> r
                            .field(fieldName)
                            .gt(JsonData.of(ent.getValue())
                            ))
                        );
                        break;
                    case "_ge":
                        builder.filter(f -> f.range(r -> r
                            .field(fieldName)
                            .gte(JsonData.of(ent.getValue())
                            ))
                        );
                        break;
                    case "_lt":
                        builder.filter(f -> f.range(r -> r
                            .field(fieldName)
                            .lt(JsonData.of(ent.getValue())
                            ))
                        );
                        break;
                    case "_le":
                        builder.filter(f -> f.range(r -> r
                            .field(fieldName)
                            .lte(JsonData.of(ent.getValue())
                            ))
                        );
                        break;
                    default:
                        if (ent.getValue().getClass().isArray() || ent.getValue() instanceof Collection) {
                            List<String> values = StringBaseOpt.objectToStringList(ent.getValue());
                            builder.filter(f -> f
                                .terms(t -> t          // terms 查询
                                    .field(ent.getKey())
                                    .terms(ts -> ts
                                        .value(values.stream()     // 使用流式转换
                                            .map(FieldValue::of)
                                            .collect(Collectors.toList())
                                        )
                                    )
                                )
                            );// QueryBuilders.termsQuery(ent.getKey(), (String[]) ent.getValue()));
                        } else {
                            builder.must(m -> m
                                .term(t -> t
                                    .field(fieldName)
                                    .value(StringBaseOpt.castObjectToString(ent.getValue())) // 值类型需与字段映射匹配
                                ));
                        }
                        break;
                }
            }
        }

        if (StringUtils.isNotBlank(queryWord)) {
            builder.must(m -> m.multiMatch(
                t -> t.query(queryWord)
                    .fields(queryFields)
                    .type(TextQueryType.BestFields)
                    .operator(Operator.Or)
                    .fuzziness("1")
                    .prefixLength(3)
                    .maxExpansions(10)
                    .minimumShouldMatch("0.5")
                    .fuzzyTranspositions(true)
            ));
        }

        return builder;
    }

    public static SortOptions.Builder mapSortBuilder( Map<String, Object> filterMap) {
        boolean setSortField = false;
        SortOptions.Builder builder = new SortOptions.Builder();
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
                String finalAWord = aWord;
                builder.field(f -> f.field(field).order("desc".equalsIgnoreCase(finalAWord)?SortOrder.Desc:SortOrder.Asc));
                setSortField = true;
                while(StringUtils.equalsAnyIgnoreCase(aWord, "desc", "asc", ",")){
                    aWord = lexer.getAWord();
                }
            }
        }

        String sortField = StringBaseOpt.objectToString(filterMap.get(TABLE_SORT_FIELD));
        if (StringUtils.isNotBlank(sortField)) {
            String sOrder = StringBaseOpt.objectToString(filterMap.get(TABLE_SORT_ORDER));
            builder.field(f -> f.field(sortField).order("desc".equalsIgnoreCase(sOrder)?SortOrder.Desc:SortOrder.Asc));
            setSortField = true;
        }
        if(!setSortField) {
            builder.score(s -> s.order(SortOrder.Desc));
        }
        return builder;
    }

    public Highlight.Builder builderHighlight() {
        if(highlightFields.isEmpty()) return null;
        Highlight.Builder highlightBuilder = new Highlight.Builder();
        Map<String, HighlightField> highlightMap = new HashMap<>();
        for (String hf : highlightFields) {
            highlightMap.put(hf,
                HighlightField.of(h -> h.preTags(this.highlightPreTags).postTags(this.highlightPostTags)));
        }
        highlightBuilder.fields(highlightMap);
        highlightBuilder.preTags(this.highlightPreTags).postTags(this.highlightPostTags)
            .fragmentSize(Searcher.SEARCH_FRAGMENT_SIZE).numberOfFragments(Searcher.SEARCH_FRAGMENT_NUM);
        return highlightBuilder;
    }

    public SearchRequest builderSearchRequest(String queryWords, Map<String, Object> filterMap,
                                              List<String> includes, List<String> excludes,
                                              int pageNo, int pageSize) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.query(Query.of(q -> q.bool(mapSearchRequest(filterMap, queryWords).build())))
            .sort(mapSortBuilder(filterMap).build());
        if(pageSize>0){
            builder.from(pageNo>1?(pageNo-1)*pageSize:0)
                .size(pageSize);
        }
        if(includes!=null && !includes.isEmpty() || excludes!=null && !excludes.isEmpty()) {
            builder.source(sc -> sc.filter(f -> {
                    if(includes!=null && !includes.isEmpty()){
                        f.includes(includes);
                    }
                    if(excludes!=null && !excludes.isEmpty()){
                        f.excludes(excludes);
                    }
                    return f;
                }));
        }
        Highlight.Builder highlight = builderHighlight();
        if(highlight!=null) {
            builder.highlight(highlight.build());
        }
        return builder.build();
    }

    public Pair<Long, List<Map<String, Object>>> esSearch(SearchRequest request) {
        ElasticsearchClient client = null;
        long totalHits =0;
        try {
            client = clientPool.borrowObject();

            SearchResponse<JsonData> response = client.search(request,
                JsonData.class );
            List<Map<String, Object>> retList = new ArrayList<>();
            for (Hit<JsonData> hit : response.hits().hits()) {
                JsonData jsonData = hit.source();
                retList.add((Map<String, Object>) jsonData.to(Map.class)); // 转换为 Map 结构
            }
            if(response.hits().total()!=null)
                totalHits = response.hits().total().value();
            return Pair.of(totalHits, retList);
        } catch (Exception e) {
            throw new ObjectException(ObjectException.UNKNOWN_EXCEPTION,
                "查询ES失败:"+e.getMessage(), e);
        }finally {
            if(client!=null) {
                clientPool.returnObject(client);
            }
        }
    }

    /**
     * 检索所有文档
     * @param fieldFilter 过滤的文件
     * @param queryWords 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 返回的list结果集
     */
    @Override
    public Pair<Long, List<Map<String, Object>>> search(Map<String, Object> fieldFilter,
                                                       String queryWords, int pageNo, int pageSize){
        return esSearch(builderSearchRequest(queryWords, fieldFilter,
            null, null, pageNo, pageSize));
    }

    /**
     * 检索所有文档
     * @param queryWords 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 返回的list结果集
     */
    @Override
    public Pair<Long, List<Map<String, Object>>> search(String queryWords, int pageNo, int pageSize) {
        return search(null, queryWords, pageNo, pageSize);
    }

    /**
     * 检索某一个系统
     * @param optId 所属业务id
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 返回的list结果集
     */
    @Override
    public Pair<Long, List<Map<String, Object>>> searchOpt(String optId,
                                                          String queryWord, int pageNo, int pageSize) {
        return search(CollectionsOpt.createHashMap("optId", optId),
            queryWord, pageNo, pageSize);
    }

    /**
     * 根据文档所属 人员来检索
     * @param owner 所属人员
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 返回list结果集
     */
    @Override
    public Pair<Long, List<Map<String, Object>>> searchOwner(String owner,
                                                            String queryWord, int pageNo, int pageSize) {
        return search(CollectionsOpt.createHashMap("userCode", owner),
            queryWord, pageNo, pageSize);
    }

    /**
     * 根据文档所属 人员 业务来检索
     * @param owner 所属人员
     * @param optId 所属业务id
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 返回list结果集
     */
    @Override
    public Pair<Long, List<Map<String, Object>>> searchOwner(String owner, String optId,
                                                            String queryWord, int pageNo, int pageSize){
        return search(CollectionsOpt.createHashMap("userCode", owner,"optId", optId),
            queryWord, pageNo, pageSize);
    }

    /**
     * 根据文档所属机构来检索
     * @param units 文档所属机构
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 返回list结果集
     */
    @Override
    public Pair<Long, List<Map<String, Object>>> searchUnits(String[] units,
                                                            String queryWord, int pageNo, int pageSize) {
        return search(CollectionsOpt.createHashMap("unitCode", units),
            queryWord, pageNo, pageSize);
    }

    /**
     * 根据文档所属机构 业务 关键字 来检索
     * @param units 文档所属机构
     * @param optId 所属业务id
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 返回list结果集
     */
    @Override
    public Pair<Long, List<Map<String, Object>>> searchUnits(String[] units,
                                                            String optId, String queryWord, int pageNo, int pageSize) {
        return search(CollectionsOpt.createHashMap("optId", optId, "unitCode", units),
            queryWord, pageNo, pageSize);
    }

    public ESSearcher setHighlightPreTags(String... highlightPreTags) {
        this.highlightPreTags = Arrays.asList(highlightPreTags);
        return this;
    }

    public ESSearcher setHighlightPostTags(String... highlightPostTags) {
        this.highlightPostTags =  Arrays.asList(highlightPostTags);
        return this;
    }

    public <T> T getDocumentById(String idFieldName, String docId, Class<T> clazz) {
        ElasticsearchClient client = null;
        try {
            client = clientPool.borrowObject();
            GetResponse<T> response = client.get(g -> g
                    .index(indexName)
                    .id(docId), clazz
            );
            if (response.found()) {
                return response.source();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("查询异常,异常信息：" + e.getMessage());
            return null;
        } finally {
            if (client != null) {
                clientPool.returnObject(client);
            }
        }
    }

}
