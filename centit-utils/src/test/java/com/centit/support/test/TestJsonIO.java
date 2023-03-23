package com.centit.support.test;

import com.alibaba.fastjson2.JSON;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.json.JSONOpt;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class TestJsonIO {

    public static class JsonWithDateField implements Serializable {
        private java.util.Date  utilDate;
        private java.sql.Date  sqlDate;
        private java.sql.Timestamp  timestamp;

        public Date getUtilDate() {
            return utilDate;
        }

        public void setUtilDate(Date utilDate) {
            this.utilDate = utilDate;
        }

        public java.sql.Date getSqlDate() {
            return sqlDate;
        }

        public void setSqlDate(java.sql.Date sqlDate) {
            this.sqlDate = sqlDate;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static void main(String arg[]) throws IOException {
        JSONOpt.fastjsonGlobalConfig();

        JsonWithDateField jsonDate = new JsonWithDateField();
        jsonDate.setSqlDate(DatetimeOpt.convertToSqlDate(
            DatetimeOpt.createUtilDate(2021,11, 11, 12,12,12)));
        jsonDate.setUtilDate(DatetimeOpt.createUtilDate(2020,12, 31));
        jsonDate.setTimestamp(DatetimeOpt.currentSqlTimeStamp());

        String s = JSON.toJSONString(jsonDate);
        System.out.println(s);

         s = "{\"sqlDate\":\"2021-11-11 12:12:12 GMT+9\",\"timestamp\":\"2023-03-13 09:16:01.538 GMT+11\"," +
             "\"utilDate\":\"2020-12-31 00:00:00 PST\"}";

        JsonWithDateField jsonDate2 = JSON.parseObject(s, JsonWithDateField.class);

        System.out.println(JSON.toJSONString(jsonDate2));
    }

}
