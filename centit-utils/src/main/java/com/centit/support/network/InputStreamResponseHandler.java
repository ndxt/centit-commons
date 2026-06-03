package com.centit.support.network;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamResponseHandler implements HttpClientResponseHandler<InputStream> {

    public static final HttpClientResponseHandler<InputStream> INSTANCE = new InputStreamResponseHandler();

    @Override
    public InputStream handleResponse(final ClassicHttpResponse response) throws IOException {
        final int statusCode = response.getCode();
        final HttpEntity entity = response.getEntity();
        if (statusCode >= 300) {
            EntityUtils.consume(entity);
            throw new HttpResponseException(statusCode, response.getReasonPhrase());
        }
        return entity == null ? null : entity.getContent();
    }

}
