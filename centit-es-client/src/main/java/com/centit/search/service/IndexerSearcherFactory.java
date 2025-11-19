package com.centit.search.service;

import com.centit.search.document.DocumentUtils;
import com.centit.search.service.Impl.ESIndexer;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.support.algorithm.NumberBaseOpt;
import com.centit.support.file.PropertiesReader;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by codefan on 17-6-22.
 */
@SuppressWarnings("unused")
public abstract class IndexerSearcherFactory {

    private static final ConcurrentHashMap<String, ESIndexer> indexerMap
            = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ESSearcher> searcherMap
            = new ConcurrentHashMap<>();


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

    public static ESIndexer obtainIndexer(ElasticConfig config, Class<?> objType) {
        String indexName = DocumentUtils.obtainDocumentIndexName(objType);
        if(StringUtils.isBlank(indexName)){
            return null;
        }

        ESIndexer index = indexerMap.get(indexName);
        if(index!=null) {
            return index;
        }
        ESIndexer indexer = new ESIndexer(ElasticsearchClientManager.getClient(config), indexName, objType);
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

    public static ESSearcher obtainSearcher(ElasticConfig config, Class<?> objType) {
        String indexName = DocumentUtils.obtainDocumentIndexName(objType);
        if(indexName==null){
            return null;
        }
        ESSearcher search = searcherMap.get(indexName);
        if(search!=null) {
            return search;
        }
        ESSearcher searcher = new ESSearcher(ElasticsearchClientManager.getClient(config));
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
    public static ElasticConfig loadESServerConfigFormProperties(Properties properties){

        ElasticConfig config = new ElasticConfig();
        config.setServerHostIp(properties.getProperty("elasticsearch.server.ip"));
        config.setServerHostPort(properties.getProperty("elasticsearch.server.port"));
        config.setClusterName(properties.getProperty("elasticsearch.server.cluster"));
        config.setUsername(properties.getProperty("elasticsearch.server.username"));
        config.setPassword(properties.getProperty("elasticsearch.server.password"));
        config.setMinScore(NumberBaseOpt.parseFloat(
                properties.getProperty("elasticsearch.filter.minScore"), 0.5f));
        return config;
    }

    public static ElasticConfig loadESServerConfigFormProperties(String propertiesFile){
        Properties properties = PropertiesReader.getClassPathProperties(propertiesFile);
        assert properties != null;
        return loadESServerConfigFormProperties(properties);
    }
}
