package com.centit.support.test;

import com.centit.support.database.utils.QueryUtils;

public class TestQueryUtils {
    public static void main(String[] args) {
        String sql = "select top 20  FLOW_INST_ID, VERSION, FLOW_CODE, FLOW_OPT_NAME, " +
            "FLOW_OPT_TAG, CREATE_TIME, PROMISE_TIME, TIME_LIMIT, INST_STATE, " +
            "IS_SUB_INST, PRE_INST_ID, PRE_NODE_INST_ID, UNIT_CODE, " +
            "USER_CODE, LAST_UPDATE_TIME, LAST_UPDATE_USER, IS_TIMER " +
            "from WF_FLOW_INSTANCE where 1=1 and PROMISE_TIME =:pt order by CREATE_TIME DESC";

        System.out.println(QueryUtils.buildSqlServerLimitQuerySQL(sql, 0, 30));
        System.out.println(QueryUtils.buildSqlServerLimitQuerySQL(sql, 30, 30));
    }
}
