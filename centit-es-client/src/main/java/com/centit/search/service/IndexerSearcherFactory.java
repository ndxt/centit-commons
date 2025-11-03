package com.centit.search.service;

import com.centit.search.document.DocumentUtils;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.Impl.PooledRestClientFactory;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.file.PropertiesReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by codefan on 17-6-22.
 */
@SuppressWarnings("unused")
public abstract class IndexerSearcherFactory {

    private static ConcurrentHashMap<String, ESIndexer> indexerMap
            = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, ESSearcher> searcherMap
            = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<ESServerConfig, GenericObjectPool<RestHighLevelClient>> clientPoolMap
        = new ConcurrentHashMap<>();


    public static GenericObjectPool<RestHighLevelClient> obtainclientPool(ESServerConfig config, boolean createNew){
        GenericObjectPool<RestHighLevelClient> clientPool =
            clientPoolMap.get(config);
        if(clientPool==null && createNew) {
            GenericObjectPoolConfig<RestHighLevelClient> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(100);
            poolConfig.setMinIdle(20);
            clientPool = new GenericObjectPool<>(new PooledRestClientFactory(config),
                poolConfig);
            clientPoolMap.put(config, clientPool);
        }
        return clientPool;
    }

    public static GenericObjectPool<RestHighLevelClient> obtainclientPool(ESServerConfig config){
        return obtainclientPool(config, true);
    }

    /**
     * 根据索引名称 获取 Indexer
     * @param indexName 索引名称
     * @return Indexer
     */
    public static ESIndexer obtainIndexer(String indexName) {
        return indexerMap.get(indexName);
    }

    /**
     * 创建 索引客户端，或者 返回已经创建的 客户端
     *
     * @param config elastic search 服务器的配置信息
     * @param objType 索引对象类型
     * @return Indexer 索引器
     */

    public static ESIndexer obtainIndexer(ESServerConfig config, Class<?> objType) {
        String indexName = DocumentUtils.obtainDocumentIndexName(objType);
        if(StringUtils.isBlank(indexName)){
            return null;
        }

        ESIndexer index = indexerMap.get(indexName);
        if(index!=null) {
            return index;
        }
        ESIndexer indexer = new ESIndexer(obtainclientPool(config), indexName, objType);
        //indexer.createIndexIfNotExist(indexName, objType);
        indexerMap.put(indexName,indexer);
        return indexer;
    }


    /**
     * 根据索引名称 获取 Searcher
     * @param indexName 索引名称
     * @return Searcher
     */
    public static ESSearcher obtainSearcher(String indexName) {
        return searcherMap.get(indexName);
    }

    /**
     * 创建 检索客户端，或者 返回已经创建的 客户端
     *
     * @param config elastic search 服务器的配置信息
     * @param objType 索引对象类型
     * @return Indexer 检索器
     */

    public static ESSearcher obtainSearcher(ESServerConfig config, Class<?> objType) {
        String indexName = DocumentUtils.obtainDocumentIndexName(objType);
        if(indexName==null){
            return null;
        }
        ESSearcher search = searcherMap.get(indexName);
        if(search!=null) {
            return search;
        }
        ESSearcher searcher = new ESSearcher(config, obtainclientPool(config));
        searcher.initTypeFields(indexName, objType);
        searcher.setHighlightPreTags(new String[]{"<span class='highlight'>"});
        searcher.setHighlightPostTags(new String[]{"</span>"});
        searcherMap.put(indexName,searcher);
        return searcher;
    }

    /**
     * 从 属性文件 中读取 elastic search 服务器的配置信息
     * @param properties 属性文件
     * @return elastic search 服务器的配置信息
     */
    public static ESServerConfig loadESServerConfigFormProperties(Properties properties){

        ESServerConfig config = new ESServerConfig();
        config.setServerHostIp(properties.getProperty("elasticsearch.server.ip"));
        config.setServerHostPort(properties.getProperty("elasticsearch.server.port"));
        config.setClusterName(properties.getProperty("elasticsearch.server.cluster"));
        config.setUsername(properties.getProperty("elasticsearch.server.username"));
        config.setPassword(properties.getProperty("elasticsearch.server.password"));
        config.setMinScore(NumberBaseOpt.parseFloat(
                properties.getProperty("elasticsearch.filter.minScore"), 0.5f));
        return config;
    }

    public static ESServerConfig loadESServerConfigFormProperties(String propertiesFile){
        Properties properties = PropertiesReader.getClassPathProperties(propertiesFile);
        return loadESServerConfigFormProperties(properties);
    }
}
