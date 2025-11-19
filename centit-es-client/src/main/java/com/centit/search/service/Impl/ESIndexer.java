package com.centit.search.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.json.JsonData;
import com.centit.search.annotation.ESType;
import com.centit.search.document.DocumentUtils;
import com.centit.search.document.ESDocument;
import com.centit.search.service.Indexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;

/**
 * Created by codefan on 17-6-12.
 */
public class ESIndexer implements Indexer{

    private static final Logger logger = LoggerFactory.getLogger(ESIndexer.class);

    private final ElasticsearchClient client;
    private String indexName;
    private boolean sureIndexExist;
    private final Class<?> objType ;

    public ESIndexer(ElasticsearchClient client, String indexName, Class<?> objType){
        this.client = client;
        this.indexName = indexName;
        this.objType = objType;
        this.sureIndexExist = false;
    }

    private void makeSureIndexIsExist() {
        if(sureIndexExist){
            return;
        }
        try {
            ExistsRequest request = ExistsRequest.of(e -> e.index(indexName));
            if (!client.indices().exists(request).value()) {
                createEsIndex(indexName, objType);
            }
            sureIndexExist = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    // 定义索引的映射类型
    private void createEsIndex(String indexName, Class<?> objType) {
        this.indexName = indexName;
        try {
            ESType esType = objType.getAnnotation(ESType.class);
            // 构建索引设置
            IndexSettings settings = IndexSettings.of(s -> s
                .numberOfShards(String.valueOf(esType.shards()))
                .numberOfReplicas(String.valueOf(esType.replicas())));

            // 构建映射
            String mappingJson = DocumentUtils.obtainDocumentMapping(objType).toJSONString();
            TypeMapping mapping = TypeMapping.of(m -> m.withJson(new StringReader(mappingJson)));

            CreateIndexRequest request = CreateIndexRequest.of(c -> c
                .index(indexName)
                .settings(settings)
                .mappings(mapping));

            client.indices().create(request);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 新建 文档
     * @param document 文档
     * @return 返回文档的 文档ID
     */
    @Override
    public String saveNewDocument(ESDocument document) {
        makeSureIndexIsExist();
        try {
            IndexRequest<JsonData> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(document.obtainDocumentId())
                .document(JsonData.fromJson(document.toJSONObject().toJSONString())));

            IndexResponse indexResponse = client.index(request);
            return indexResponse.id();
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据文档ID 删除文档
     *
     * @param document 文档
     * @return 返回布尔类型
     */
    @Override
    public boolean deleteDocument(ESDocument document) {
        return deleteDocument(document.obtainDocumentId());
    }

    /**
     * 根据文件类型和ID 删除文档
     * @param docId String 文档id
     * @return 删除返回的布尔类型
     */
    @Override
    public boolean deleteDocument(String docId) {

        try {

            DeleteRequest request = DeleteRequest.of(d -> d
                .index(indexName)
                .id(docId));

            DeleteResponse response = client.delete(request);
            return response.result().jsonValue().equals("deleted");
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新文档
     *
     * @param document 要跟新的文档对象
     * @return 更新后返回int值
     */
    @Override
    public int updateDocument(ESDocument document) {
        String docId = document.obtainDocumentId();
        return updateDocument(docId, document);
    }

    /**
     * 更新文档
     *
     * @param docId 文档id
     * @param document 要更新的文档对象
     * @return 更新后返回的int值
     */
    @Override
    public int updateDocument(String docId, ESDocument document) {

        try {

            UpdateRequest<JsonData, JsonData> request = UpdateRequest.of(u -> u
                .index(indexName)
                .id(docId)
                .doc(JsonData.fromJson(document.toJSONObject().toJSONString())));

            UpdateResponse<JsonData> response = client.update(request, JsonData.class);
            return response.result().jsonValue().equals("updated") ? 1 : 0;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 合并文档
     *
     * @param document ESDocument
     * @return 是否成功
     */
    @Override
    public String mergeDocument(ESDocument document) {
        makeSureIndexIsExist();

        try {
            String docId = document.obtainDocumentId();

            // 判断文档是否存在
            GetRequest getRequest = GetRequest.of(g -> g
                .index(indexName)
                .id(docId));

            boolean exists;
            try {
                GetResponse<JsonData> getResponse = client.get(getRequest, JsonData.class);
                exists = getResponse.found();
            } catch (Exception e) {
                // 文档不存在时会抛出异常，设置exists为false
                exists = false;
            }

            if(exists) {
                UpdateRequest<JsonData, JsonData> updateReq = UpdateRequest.of(u -> u
                    .index(indexName)
                    .id(docId)
                    .doc(JsonData.fromJson(document.toJSONObject().toJSONString())));

                UpdateResponse<JsonData> response = client.update(updateReq, JsonData.class);
                return response.result().jsonValue().equals("updated") ? response.id() : null;
            } else {
                IndexRequest<JsonData> indexReq = IndexRequest.of(i -> i
                    .index(indexName)
                    .id(docId)
                    .document(JsonData.fromJson(document.toJSONObject().toJSONString())));

                IndexResponse indexResponse = client.index(indexReq);
                return indexResponse.id();
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
