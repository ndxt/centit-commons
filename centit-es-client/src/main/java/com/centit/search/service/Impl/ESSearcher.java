package com.centit.search.service.Impl;

import com.centit.search.annotation.ESField;
import com.centit.search.document.DocumentUtils;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Searcher;
import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.common.ObjectException;
import com.centit.support.compiler.Lexer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
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

    private GenericObjectPool<RestHighLevelClient> clientPool;
    private String indexName;
    private String[] highlightPreTags;
    private String[] highlightPostTags;

    private List<String> allFields;
    private String[] queryFields;
    private Set<String> highlightFields;

    public String[] getQueryFields() {
        return queryFields;
    }

    public String getIndexName() {
        return indexName;
    }

    public ESSearcher(){
        this.highlightFields = new HashSet<>();
        this.highlightPreTags = new String[]{"<strong>"};
        this.highlightPostTags = new String[]{"</strong>"};
        this.allFields = new ArrayList<>();
    }

    public ESSearcher(ESServerConfig config, GenericObjectPool<RestHighLevelClient> clientPool){
        this();
        this.config = config;
        this.clientPool = clientPool;
    }

    public void setESServerConfig(ESServerConfig config){
        this.config = config;
    }

    public void setClientPool(GenericObjectPool<RestHighLevelClient> clientPool) {
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

        queryFields = CollectionsOpt.listToArray(qf);
    }

    public Pair<Long,List<Map<String, Object>>> esSearch(QueryBuilder queryBuilder, List<SortBuilder<?>> sortBuilders,
                                                         String[] includes, String[] excludes,
                                                         int pageNo, int pageSize){
        RestHighLevelClient client = null;
        long totalHits =0;
        try {
            client = clientPool.borrowObject();
            List<Map<String, Object>> retList = new ArrayList<>(pageSize+5);
            /*IndicesExistsResponse indicesExistsResponse = client.admin().indices()
                    .exists(new IndicesExistsRequest(config.getIndexName()))
                    .actionGet();
            if (!indicesExistsResponse.isExists()){
                json.put("error","索引不存在");
                retList.add(json);
                return retList;
            }*/
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder);
            //添加默认根据匹配度排序 .order(SortOrder.DESC)
            searchSourceBuilder.sort(SortBuilders.scoreSort());
            if(sortBuilders != null && !sortBuilders.isEmpty()) {
                searchSourceBuilder.sort(sortBuilders);
            }
            if(pageSize>0) {
                searchSourceBuilder.explain(true)
                    .from((pageNo > 1) ? (pageNo - 1) * pageSize : 0)
                    .size(pageSize);
            }

            if(!highlightFields.isEmpty()) {
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                for (String hf : highlightFields) {
                    highlightBuilder.field(hf);
                }
                highlightBuilder.preTags(this.highlightPreTags).postTags(this.highlightPostTags)
                    .fragmentSize(Searcher.SEARCH_FRAGMENT_SIZE).numOfFragments(Searcher.SEARCH_FRAGMENT_NUM);
                searchSourceBuilder.highlighter(highlightBuilder);
            }

            if(includes!=null || excludes!=null)
                searchSourceBuilder.fetchSource(includes, excludes);

            SearchRequest searchRequest = new SearchRequest(indexName)
                .source(searchSourceBuilder);
            SearchResponse actionGet = client.search(searchRequest,RequestOptions.DEFAULT);
            SearchHits hits = actionGet.getHits();
            //totalHits = hits.getTotalHits();
            if (hits != null) {
                totalHits = hits.getTotalHits().value;
                //System.out.println("查询到记录数=" + hits.getTotalHits());
                /*if (StringUtils.isNotBlank(hit.getSourceAsString())) {
                    json = JSONObject.parseObject(hit.getSourceAsString());
                }*/
                SearchHit[] hitsRes = hits.getHits();
                if (hitsRes != null) {
                    for (SearchHit hit : hitsRes) {
                        Map<String, Object> json = hit.getSourceAsMap();
                        if (json == null) {
                            json = new HashMap<>(4);
                        }
                        /*for (Map.Entry<String, DocumentField> field : hit.getFields().entrySet()){
                            List<Object> objValues = field.getValue().getValues();
                            if(objValues!=null && objValues.size()>0) {
                                if (objValues.size() == 1) {
                                    json.put(field.getKey(), objValues.get(0));
                                } else {
                                    json.put(field.getKey(), objValues);
                                }
                            }
                        }*/
                        //Highlight
                        if (hit.getHighlightFields() != null) {
                            StringBuilder content = new StringBuilder("");
                            for (Map.Entry<String, HighlightField> highlight : hit.getHighlightFields().entrySet()) {
                                HighlightField highlightField = highlight.getValue();
//                        content.append(highlight.getKey()).append(":");
                                if (highlightField != null) {
                                    for (Text t : highlightField.fragments()) {
                                        content.append(t.string()/*.replace("\n", "")*/);
                                    }
                                }
                                content.append("\n");
                            }
                            json.put("highlight", content);
                        }
                        json.put("_score", hit.getScore());
                        //hit.type()
                        String hitType = hit.getType();
                        if (hitType != null) {//获取返回对象的类型
                            json.put("_type", hitType);
                        }
                        retList.add(json);
                    }
                }
            }
            return new ImmutablePair<>(totalHits, retList);
        } catch (Exception e) {
            throw new ObjectException(ObjectException.UNKNOWN_EXCEPTION,
                "查询ES失败:"+e.getMessage(), e);
        }finally {
            if(client!=null) {
                clientPool.returnObject(client);
            }
        }
    }

    public Pair<Long,List<Map<String, Object>>> esSearch(QueryBuilder queryBuilder, int pageNo, int pageSize) {
        return esSearch(queryBuilder, null,  null,null,  pageNo, pageSize);
    }

    public Pair<Long,List<Map<String, Object>>> esSearch(QueryBuilder queryBuilder, List<SortBuilder<?>> sortBuilders,
                                                         int pageNo, int pageSize) {
        return esSearch(queryBuilder, sortBuilders,  null,null,  pageNo, pageSize);
    }

    public static List<SortBuilder<?>> mapSortBuilder(Map<String, Object> filterMap) {
        if(filterMap==null || filterMap.isEmpty()){
            return null;
        }
        List<SortBuilder<?>> sortBuilders = new ArrayList<>();
        String selfOrderBy = StringBaseOpt.objectToString(filterMap.get(SELF_ORDER_BY));
        if(StringUtils.isBlank(selfOrderBy)){
            selfOrderBy = StringBaseOpt.objectToString(filterMap.get(SELF_ORDER_BY2));
        }
        if (StringUtils.isNotBlank(selfOrderBy)) {
            Lexer lexer = new Lexer(selfOrderBy, Lexer.LANG_TYPE_SQL);
            String aWord = lexer.getAWord();
            while (StringUtils.isNotBlank(aWord)) {
                String field = aWord;
                SortOrder sortOrder = SortOrder.ASC;
                aWord = lexer.getAWord();
                if("desc".equalsIgnoreCase(aWord)){
                    sortOrder = SortOrder.DESC;
                }
                sortBuilders.add(SortBuilders.fieldSort(field).order(sortOrder));
                while(StringUtils.equalsAnyIgnoreCase(aWord, "desc", "asc", ",")){
                    aWord = lexer.getAWord();
                }
            }
        }

        String sortField = StringBaseOpt.objectToString(filterMap.get(TABLE_SORT_FIELD));
        if (StringUtils.isNotBlank(sortField)) {
            SortBuilder sortBuilder;
            String sOrder = StringBaseOpt.objectToString(filterMap.get(TABLE_SORT_ORDER));
            if ("desc".equalsIgnoreCase(sOrder)) {
                sortBuilder = SortBuilders.fieldSort(sortField).order(SortOrder.DESC);
            } else {
                sortBuilder = SortBuilders.fieldSort(sortField).order(SortOrder.ASC);
            }
            sortBuilders.add(sortBuilder);
        }
        return sortBuilders;
    }

    /**
     * 检索所有文档
     * @param fieldFilter 过滤的文件
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 返回的list结果集
     */
    @Override
    public Pair<Long,List<Map<String, Object>>> search(Map<String, Object> fieldFilter,
                                                       String queryWord, int pageNo, int pageSize){
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
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
                if (ent.getValue().getClass().isArray()) {
                    queryBuilder.must(QueryBuilders.termsQuery(ent.getKey(), (String[]) ent.getValue()));
                } else if (ent.getValue() instanceof Collection) {
                    queryBuilder.must(QueryBuilders.termsQuery(
                        ent.getKey(), CollectionsOpt.listToArray((Collection)ent.getValue())));
                } else {
                    String key = ent.getKey();
                    int keyLen = key.length();
                    String optSuffix = keyLen>3 ? key.substring(keyLen - 3).toLowerCase() : "_eq";
                    switch (optSuffix) {
                        case "_gt":
                            queryBuilder.must(QueryBuilders.rangeQuery(key.substring(0,keyLen-3)).gt(ent.getValue()));
                            break;
                        case "_ge":
                            queryBuilder.must(QueryBuilders.rangeQuery(key.substring(0,keyLen-3)).gte(ent.getValue()));
                            break;
                        case "_lt":
                            queryBuilder.must(QueryBuilders.rangeQuery(key.substring(0,keyLen-3)).lt(ent.getValue()));
                            break;
                        case "_le":
                            queryBuilder.must(QueryBuilders.rangeQuery(key.substring(0,keyLen-3)).lte(ent.getValue()));
                            break;
                        default:
                            queryBuilder.must(QueryBuilders.termQuery(ent.getKey(), ent.getValue()));
                            break;
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(queryWord)) {
            queryBuilder.filter(QueryBuilders.multiMatchQuery(
                queryWord, queryFields));
        }

        return esSearch(queryBuilder, mapSortBuilder(fieldFilter), pageNo, pageSize);
    }

    /**
     * 检索所有文档
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 返回的list结果集
     */
    @Override
    public Pair<Long,List<Map<String, Object>>> search(String queryWord, int pageNo, int pageSize) {
        return search(null, queryWord, pageNo, pageSize);
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
    public Pair<Long,List<Map<String, Object>>> searchOpt(String optId,
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
    public Pair<Long,List<Map<String, Object>>> searchOwner(String owner,
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
    public Pair<Long,List<Map<String, Object>>> searchOwner(String owner, String optId,
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
    public Pair<Long,List<Map<String, Object>>> searchUnits(String[] units,
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
    public Pair<Long,List<Map<String, Object>>> searchUnits(String[] units,
                                                            String optId, String queryWord, int pageNo, int pageSize) {
        return search(CollectionsOpt.createHashMap("optId", optId, "unitCode", units),
            queryWord, pageNo, pageSize);
    }

    public ESSearcher setHighlightPreTags(String[] highlightPreTags) {
        this.highlightPreTags = highlightPreTags;
        return this;
    }

    public ESSearcher setHighlightPostTags(String[] highlightPostTags) {
        this.highlightPostTags = highlightPostTags;
        return this;
    }

}
