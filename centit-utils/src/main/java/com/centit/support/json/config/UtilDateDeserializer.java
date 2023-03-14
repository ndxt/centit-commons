package com.centit.support.json.config;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.centit.support.algorithm.DatetimeOpt;
import org.apache.commons.lang3.StringUtils;
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
        if (val instanceof java.util.Date) {
            return (java.util.Date) val;
        }  if (val instanceof java.sql.Date) {
            return  DatetimeOpt.convertToUtilDate((java.sql.Date)val);
        } else if (val instanceof Number) {
            return  new java.util.Date(((Number) val).longValue());
        } else if (val instanceof String) {
            String strVal = (String) val;
            if (StringUtils.isBlank(strVal)) {
                return null;
            } else {
                return DatetimeOpt.smartPraseDate(strVal);
            }
        }
        return null;
    }

}

