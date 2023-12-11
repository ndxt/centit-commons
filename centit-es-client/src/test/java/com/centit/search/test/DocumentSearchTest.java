package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.centit.search.document.ObjectDocument;
import com.centit.search.service.ESServerConfig;
import com.centit.search.service.Impl.ESSearcher;
import com.centit.search.service.IndexerSearcherFactory;
import com.centit.support.algorithm.CollectionsOpt;
import org.apache.commons.lang3.tuple.Pair;

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
            e.printStackTrace();
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

        System.out.println(JSON.toJSONString(res));
    }
}
