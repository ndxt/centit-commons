package com.centit.support.network;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Utf8ResponseHandler implements HttpClientResponseHandler<String> {

    public static final HttpClientResponseHandler<String> INSTANCE = new Utf8ResponseHandler();

    @Override
    public String handleResponse(final ClassicHttpResponse response) throws IOException {
        final int statusCode = response.getCode();
        final HttpEntity entity = response.getEntity();
        if (statusCode >= 400) {
            EntityUtils.consume(entity);
            throw new HttpResponseException(statusCode, "Error Code : " +
                statusCode + ", " + response.getReasonPhrase() + "。");
        }
        try {
            return entity == null ? null : EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (org.apache.hc.core5.http.ParseException e) {
            throw new IOException(e);
        }
    }

}
