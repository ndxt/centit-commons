package com.centit.support.json.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.centit.support.algorithm.DatetimeOpt;

import java.lang.reflect.Type;

public class SqlTimestampDeserializer implements ObjectReader<java.sql.Timestamp> {

    public final static SqlTimestampDeserializer instance = new SqlTimestampDeserializer();

    @Override
    public java.sql.Timestamp readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            return new java.sql.Timestamp(millis);
        }
        Object val = jsonReader.readAny();
        return DatetimeOpt.castObjectToSqlTimestamp(val);
    }

}

