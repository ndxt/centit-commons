package com.centit.search.service.Impl;

import com.centit.search.annotation.ESType;
import com.centit.search.document.DocumentUtils;
import com.centit.search.document.ESDocument;
import com.centit.search.service.Indexer;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by codefan on 17-6-12.
 */
public class ESIndexer implements Indexer{

    private static Logger logger = LoggerFactory.getLogger(ESIndexer.class);

    private GenericObjectPool<RestHighLevelClient> clientPool;
    private String indexName;
    private boolean sureIndexExist;
    private Class<?> objType ;

    public ESIndexer(GenericObjectPool<RestHighLevelClient> clientPool,
                     String indexName, Class<?> objType){
        this.clientPool = clientPool;
        this.indexName=indexName;
        this.objType = objType;
        this.sureIndexExist = false;
    }

    public void setClientPool(GenericObjectPool<RestHighLevelClient> clientPool) {
        this.clientPool = clientPool;
    }

    private void makeSureIndexIsExist() {
        if(sureIndexExist){
            return;
        }
        RestHighLevelClient client = null;
        try {
            client = clientPool.borrowObject();
            GetIndexRequest request = new GetIndexRequest(indexName);
            if (!client.indices().exists(request, RequestOptions.DEFAULT)) {
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
        RestHighLevelClient client = null;
        try {
            client = clientPool.borrowObject();

            CreateIndexRequest request = new CreateIndexRequest(indexName);
            ESType esType = objType.getAnnotation(ESType.class);
            request.settings(Settings.builder()
                .put("index.number_of_shards", esType.shards())
                .put("index.number_of_replicas", esType.replicas()));

            request.mapping(
                DocumentUtils.obtainDocumentMapping(objType).toJSONString(),
                XContentType.JSON);
            //client.admin().indices().putMapping(putMappingRequest).actionGet();
            client.indices().create(request, RequestOptions.DEFAULT);
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
     * @return 返回文档的 文档ID
     */
    @Override
    public String saveNewDocument(ESDocument document) {
        makeSureIndexIsExist();
        RestHighLevelClient client = null;
        try {
            client = clientPool.borrowObject();
            /*String type = document.obtainDocumentType();
            String docId = document.obtainDocumentId();*/
            IndexRequest request = new IndexRequest(indexName)
                            .id(document.obtainDocumentId())
                            .source(document.toJSONObject());

            IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
            return indexResponse.getId();
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
        RestHighLevelClient client = null;
        try {
            client = clientPool.borrowObject();
            /*DeleteResponse response = client.prepareDelete(
                indexName, docType, docId).execute().actionGet();*/
            DeleteRequest request = new DeleteRequest(indexName,docId);
            DeleteResponse response = client.delete(request,RequestOptions.DEFAULT);
            return response.status().getStatus() == 200;
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
        RestHighLevelClient client = null;
        try {
            client = clientPool.borrowObject();
            UpdateRequest request = new UpdateRequest()
                        .index(indexName)
                        //.type(type)
                        .id(docId)
                        .doc(document.toJSONObject());
            UpdateResponse response = client.update(request,RequestOptions.DEFAULT);
            int ret = response.status().getStatus();
            return (ret == 200)?1:0;
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
        RestHighLevelClient client = null;
        try {
            client = clientPool.borrowObject();
            String docId = document.obtainDocumentId();
            GetRequest request = new GetRequest(indexName)
                        .id(docId);
            if(client.exists(request, RequestOptions.DEFAULT)) {
                UpdateRequest req = new UpdateRequest()
                    .index(indexName)
                    //.type(type)
                    .id(docId)
                    .doc(document.toJSONObject());
                UpdateResponse response = client.update(req,RequestOptions.DEFAULT);
                int ret = response.status().getStatus();
                return (ret == 200)? response.getId() : null;
            }else {
                IndexRequest req = new IndexRequest(indexName)
                        .id(docId)
                        .source(document.toJSONObject());
                IndexResponse indexResponse = client.index(req,RequestOptions.DEFAULT);
                return indexResponse.getId();
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
