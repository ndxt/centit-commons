package com.centit.support.json.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.centit.support.algorithm.DatetimeOpt;

import java.lang.reflect.Type;

public class SqlDateDeserializer implements ObjectReader<java.sql.Date> {

    public final static SqlDateDeserializer instance = new SqlDateDeserializer();

    @Override
    public java.sql.Date readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            return new java.sql.Date(millis);
        }

        Object val = jsonReader.readAny();
        return DatetimeOpt.castObjectToSqlDate(val);
    }
}

