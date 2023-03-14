package com.centit.support.json.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.lang3.StringUtils;
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
        if (val instanceof java.sql.Date) {
            return (java.sql.Date) val;
        }  if (val instanceof java.util.Date) {
            return  DatetimeOpt.convertToSqlDate((java.util.Date)val);
        } else if (val instanceof Number) {
            return  new java.sql.Date(((Number) val).longValue());
        } else if (val instanceof String) {
            String strVal = (String) val;
            if (StringUtils.isBlank(strVal)) {
                return null;
            } else {
                return DatetimeOpt.convertToSqlDate(DatetimeOpt.smartPraseDate(strVal));
            }
        }
        return null;

    }
}

