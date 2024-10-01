package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.CollectionsOpt;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by zou_wy on 2017/8/2.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
public class DocumentSearchTest {

    private static final Logger logger = LogManager.getLogger(DocumentSearchTest.class);

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

    public static void main(String[] args) throws Exception {

        ESServerConfig esServerConfig = IndexerSearcherFactory.loadESServerConfigFormProperties(
                loadProperties() );

        ESSearcher fetchSearcher = IndexerSearcherFactory.obtainSearcher(esServerConfig, HelpDoc.class);

        Pair<Long, List<Map<String, Object>>> res = fetchSearcher.search(
            CollectionsOpt.createHashMap("osId", "zp_Qn5R5ROSo4sf-eovoWA"),
            "输入框", 1, 20);

        //添加.keyword是因为类型不一样， text类型的如果想精确匹配就需要添加.keyword，如果本来就是keyword类型就不需要
        /*Pair<Long, List<Map<String, Object>>> res = fetchSearcher.search(
            CollectionsOpt.createHashMap("topUnit.keyword", "T2w2m00e", "logLevel.keyword", "0"),
            "胡知非", 1, 20);
        */
        System.out.println(JSON.toJSONString(res));
    }
}
