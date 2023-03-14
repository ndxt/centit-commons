package com.centit.support.json.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.centit.support.algorithm.DatetimeOpt;
import java.lang.reflect.Type;

public class UtilDateDeserializer implements ObjectReader<java.util.Date> {

    public final static UtilDateDeserializer instance = new UtilDateDeserializer();

    @Override
    public java.util.Date readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.isInt()) {
            long millis = jsonReader.readInt64Value();
            return new java.util.Date(millis);
        }

        Object val = jsonReader.readAny();
        return DatetimeOpt.castObjectToDate(val);
    }

}

