package com.centit.search.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.json.JsonData;
import com.centit.search.annotation.ESType;
import com.centit.search.document.DocumentUtils;
import com.centit.search.document.ESDocument;
import com.centit.search.service.Indexer;
import com.centit.support.algorithm.ByteBaseOpt;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * Created by codefan on 17-6-12.
 */
public class ESIndexer implements Indexer{

    private static final Logger logger = LoggerFactory.getLogger(ESIndexer.class);

    private GenericObjectPool<ElasticsearchClient> clientPool;
    private String indexName;
    private boolean sureIndexExist;
    private Class<?> objType ;

    public ESIndexer(GenericObjectPool<ElasticsearchClient> clientPool,
                     String indexName, Class<?> objType){
        this.clientPool = clientPool;
        this.indexName=indexName;
        this.objType = objType;
        this.sureIndexExist = false;
    }

    public void setClientPool(GenericObjectPool<ElasticsearchClient> clientPool) {
        this.clientPool = clientPool;
    }

    private void makeSureIndexIsExist() {
        if(sureIndexExist){
            return;
        }
        ElasticsearchClient client = null;
        try {
            client = clientPool.borrowObject();
            if (!client.indices().exists(e -> e.index(indexName)).value()) {
                createEsIndex(indexName, objType);
            }
            sureIndexExist = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }finally {
            if(client!=null) {
                clientPool.returnObject(client);
            }
        }
    }

    // 定义索引的映射类型
    private void createEsIndex(String indexName, Class<?> objType) {
        this.indexName = indexName;
        //判断索引是否存在，不存在则新建
        ElasticsearchClient client = null;
        ESType esType = objType.getAnnotation(ESType.class);
        try {
            client = clientPool.borrowObject();
            client.indices().create(c -> c
                .index(indexName)
                .settings(s -> s
                    .numberOfShards(String.valueOf(esType.shards()))
                    .numberOfReplicas(String.valueOf(esType.replicas())))
                .mappings(m -> m
                    .withJson( new ByteArrayInputStream(
                        ByteBaseOpt.castObjectToBytes(
                        DocumentUtils.obtainDocumentMapping(objType))))));
        //return client;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }finally {
            if(client!=null) {
                clientPool.returnObject(client);
            }
        }
    }

    /**
     * 新建 文档
     * @param document 文档
     * @return 返回文档的 文档ID// 原错误代码（使用旧API）
     */
    @Override
    public String saveNewDocument(ESDocument document) {
        makeSureIndexIsExist();
        ElasticsearchClient client = null;
        try {
            client = clientPool.borrowObject();
            IndexResponse indexResponse = client.index(b -> b
                .index(indexName)
                .id(document.obtainDocumentId())
                .document(JsonData.of(document.toJSONObject())));
            return indexResponse.id();
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }finally {
            if(client!=null) {
                clientPool.returnObject(client);
            }
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
        ElasticsearchClient client = null;
        try {
            client = clientPool.borrowObject();
            /*DeleteResponse response = client.prepareDelete(
                indexName, docType, docId).execute().actionGet();*/
            DeleteResponse response = client.delete(b -> b
                .index(indexName)
                .id(docId)
            );
            // 更可靠的判断方式（Deleted 结果状态）
            return response.result() == Result.Deleted;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }finally {
            if(client!=null) {
                clientPool.returnObject(client);
            }
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
        ElasticsearchClient client = null;
        try {
            client = clientPool.borrowObject();
            // 使用 builder 模式构建更新请求
            UpdateResponse<Object> response = client.update(b -> b
                    .index(indexName)
                    .id(docId)
                    .doc(JsonData.of(document.toJSONObject())),  // 确保返回 Map/JSON 结构
                  Object.class  // 指定文档类型（或使用具体DTO类）
            );
            // 根据操作结果枚举判断
            return response.result() == Result.Updated ? 1 : 0;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 0;
        }finally {
            if(client!=null) {
                clientPool.returnObject(client);
            }
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
        ElasticsearchClient client = null;
        try {
            client = clientPool.borrowObject();
            String docId = document.obtainDocumentId();
            // 使用 exists API 简化存在性检查
            boolean exists = client.exists(b -> b
                .index(indexName)
                .id(docId)
            ).value();

            if (exists) {
                UpdateResponse<Object> response = client.update(b -> b
                        .index(indexName)
                        .id(docId)
                        .doc(JsonData.of(document.toJSONObject())) // 确保返回 Map 或 POJO
                        .docAsUpsert(false), // 明确关闭 upsert
                    Object.class
                );
                return response.result() == Result.Updated ? docId : null;
            }else {
                // 使用带 ID 的索引请求
                IndexResponse response = client.index(b -> b
                    .index(indexName)
                    .id(docId)
                    .document(JsonData.of(document.toJSONObject()))
                );
                return response.id();
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }finally {
            if(client!=null) {
                clientPool.returnObject(client);
            }
        }
    }
}
