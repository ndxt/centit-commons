package com.centit.search.document;


import com.alibaba.fastjson.JSONObject;

/**
 * Created by codefan on 17-6-27.
 */
public interface ESDocument {

    String obtainDocumentId();

    JSONObject toJSONObject();

}
