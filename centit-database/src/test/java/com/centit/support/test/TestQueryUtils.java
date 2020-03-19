package com.centit.support.test;

import com.centit.support.database.utils.FieldType;
import com.centit.support.database.utils.QueryUtils;

public class TestQueryUtils {
    public static void main(String[] args) {

        System.out.println(FieldType.mapToHumpName("F_Bc_2014_cAe", false) );


        String sql = "select distinct top 20 abcd.FLOW_INST_ID, abcd.VERSION, abcd.FLOW_CODE, FLOW_OPT_NAME, " +
            "FLOW_OPT_TAG, CREATE_TIME, PROMISE_TIME, TIME_LIMIT, INST_STATE, " +
            "IS_SUB_INST, PRE_INST_ID, PRE_NODE_INST_ID, UNIT_CODE, " +
            "USER_CODE, LAST_UPDATE_TIME, LAST_UPDATE_USER, IS_TIMER " +
            "from WF_FLOW_INSTANCE where 1=1 and PROMISE_TIME =:pt order by CREATE_TIME DESC";

        System.out.println(QueryUtils.buildSqlServerLimitQuerySQL(sql, 0, 30));
        System.out.println(QueryUtils.buildSqlServerLimitQuerySQL(sql, 30, 30));
    }
}
