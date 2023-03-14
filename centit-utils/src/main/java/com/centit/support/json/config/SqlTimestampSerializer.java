package com.centit.support.json.config;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.centit.support.algorithm.DatetimeOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Type;
import java.util.Date;

public class SqlTimestampSerializer implements ObjectWriter<java.sql.Timestamp> {
    protected Logger logger = LoggerFactory.getLogger(SqlTimestampSerializer.class);

    public final static SqlTimestampSerializer instance = new SqlTimestampSerializer();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {

        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeString(DatetimeOpt.convertDateToString(
            (Date)object, "yyyy-MM-dd HH:mm:ss.SSS zzz"));
    }
}
