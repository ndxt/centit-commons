package com.centit.support.json.config;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Blob;
import java.sql.SQLException;

public class LobSerializer implements ObjectWriter<java.sql.Blob> {
    protected Logger logger = LoggerFactory.getLogger(LobSerializer.class);

    public final static LobSerializer instance = new LobSerializer();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {

        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        /*if (object instanceof Clob) {
            try {
                Clob clob = (Clob) object;
                try(Reader reader = clob.getCharacterStream()) {
                    StringWriter writer = new StringWriter();
                    char[] buf = new char[1024];
                    int len = 0;
                    while ((len = reader.read(buf)) != -1) {
                        writer.write(buf, 0, len);
                    }

                    String text = writer.toString();
                    jsonWriter.writeString(text);
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(),e);//e.printStackTrace();
                throw new IOException("write clob error", e);
            }
        }else */
        if (object instanceof Blob) {
            try {
                Blob lobData = (Blob) object;
                InputStream is = lobData.getBinaryStream();
                byte[] readBytes = new byte[is.available()];
                int count = is.read(readBytes);
                if(count>0)
                    jsonWriter.writeString(new String(Base64.encodeBase64(readBytes)));
            } catch (SQLException | IOException e) {
                logger.error(e.getMessage(),e);//e.printStackTrace();
                //throw new IOException("write blob error", e);
            }
            jsonWriter.writeString(StringBaseOpt.objectToString(object));
        }
    }
}
