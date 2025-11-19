package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.centit.search.document.FileDocument;
import com.centit.search.service.ElasticConfig;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestSearchFiles {

    private static final Logger logger = LogManager.getLogger(TestSearchFiles.class);
    public static Properties loadProperties() {
        Properties prop = new Properties();
        try(InputStream resource = DocumentSearchTest
            .class.getResourceAsStream("/system.properties")){
            //new ClassPathResource("system.properties").getInputStream();
            if(resource==null) {
                try(InputStream resource2 = ClassLoader.getSystemResourceAsStream("/system.properties")){
                    if(resource2 != null) {
                        prop.load(resource2);
                    }
                }
            }else {
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
        return IndexerSearcherFactory.obtainSearcher(elasticConfig, FileDocument.class);
    }

    public static void main(String[] args)  throws IOException {
        ESSearcher searcher = createSearch();
        Pair<Long, List<Map<String, Object>>>  p = searcher.search("\"数字化平台\"", 1, 10);
        System.out.println(JSON.toJSONString(p.getLeft()));
    }
}
