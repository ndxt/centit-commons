package com.centit.support.test;

import com.centit.support.algorithm.CollectionsOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.database.utils.FieldType;
import com.centit.support.database.utils.QueryAndNamedParams;
import com.centit.support.database.utils.QueryUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestQueryUtils {
    public static void main(String[] args) {
        String sql = "select USER_CODE, LAST_UPDATE_TIME, LAST_UPDATE_USER, IS_TIMER " +
            "from WF_FLOW_INSTANCE where 1=1 " +
            "[ :(splitForIn, loop)pt | and PROMISE_TIME =:pt ] order by CREATE_TIME DESC";
        QueryAndNamedParams query =  QueryUtils.translateQuery(sql,
            CollectionsOpt.createHashMap("pt", "1,2,3,4"));
        System.out.println(query.getQuery());
    }

    public static void testSqlBuilder1() {
        InputStream s =new ByteArrayInputStream("ss".getBytes());
        List<Object> params = new ArrayList<>();
        System.out.println(params.toArray());
        System.out.println(FieldType.mapToHumpName("F_Bc_2014_AAe", false) );

        String sql = "select distinct top 20 abcd.FLOW_INST_ID, abcd.VERSION, abcd.FLOW_CODE, FLOW_OPT_NAME, " +
            "FLOW_OPT_TAG, CREATE_TIME, PROMISE_TIME, TIME_LIMIT, INST_STATE, " +
            "IS_SUB_INST as '你好啊', PRE_INST_ID as 爱上, PRE_NODE_INST_ID 地球, UNIT_CODE, " +
            "USER_CODE, LAST_UPDATE_TIME, LAST_UPDATE_USER, IS_TIMER " +
            "from WF_FLOW_INSTANCE where 1=1 and PROMISE_TIME =:pt order by CREATE_TIME DESC";
        System.out.println(StringBaseOpt.castObjectToString(QueryUtils.splitSqlFieldNames(sql)));
        System.out.println(QueryUtils.buildSqlServerLimitQuerySQL(sql, 30, 30));
    }
}
