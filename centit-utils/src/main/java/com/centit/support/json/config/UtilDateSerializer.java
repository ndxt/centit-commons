package com.centit.support.json.config;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.centit.support.algorithm.DatetimeOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Type;

public class UtilDateSerializer implements ObjectWriter<java.util.Date> {
    protected Logger logger = LoggerFactory.getLogger(UtilDateSerializer.class);

    public final static UtilDateSerializer instance = new UtilDateSerializer();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {

        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeString(DatetimeOpt.convertDateToString(
            (java.util.Date)object, "yyyy-MM-dd HH:mm:ss zzz"));
    }
}
