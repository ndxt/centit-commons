package com.centit.support.json.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.lang3.StringUtils;
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

        if (val instanceof java.sql.Timestamp) {
            return (java.sql.Timestamp) val;
        }  if (val instanceof java.util.Date) {
            return  DatetimeOpt.convertToSqlTimestamp((java.util.Date)val);
        } else if (val instanceof Number) {
            return  new java.sql.Timestamp(((Number) val).longValue());
        } else if (val instanceof String) {
            String strVal = (String) val;

            if (StringUtils.isBlank(strVal)) {
                return null;
            } else {
               return DatetimeOpt.convertToSqlTimestamp(DatetimeOpt.smartPraseDate(strVal));
            }
        }
        return null;
    }

}

