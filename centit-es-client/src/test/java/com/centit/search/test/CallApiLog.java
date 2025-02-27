package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.annotation.ESField;
import com.centit.search.annotation.ESType;
import com.centit.search.document.ESDocument;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhf
 */
@Data
@ESType(indexName = "callapilog", shards = 5)
public class CallApiLog implements ESDocument, Serializable {

    private static final long serialVersionUID =  1L;

    @ESField(type = "keyword") //(name="log_id")
    private String logId;

    @ESField(type = "keyword") //(name="task_id")
    private String taskId;

    @ESField(type = "keyword") //(name="opt_id")
    private String optId;

    @ESField(type = "keyword") //(name="application_id")
    private String applicationId;

    @ESField(type = "keyword") //(name="opt_id")
    private String topUnit;

    @ESField(type = "keyword")
    private String requestIp;

    @ESField(type = "keyword")
    private String requestParams;

    @ESField(type = "date") //
    private Date runBeginTime;


    @ESField(type = "date") //
    private Date runEndTime;

    @ESField(type = "keyword") //(name="run_type")
    private String runType;

    @ESField(type = "keyword") //(name="runner")
    private String runner;

    @ESField(type = "keyword") //(name="other_message")
    private String otherMessage;

    @ESField(type = "keyword") //(name="error_pieces")
    private Integer errorPieces;

    @ESField(type = "keyword") //(name="success_pieces")
    private Integer successPieces;

    @ESField(type = "keyword") //(name="api_type")
    private Integer apiType;

    /**
     * 临时记录 执行步骤
     */
    private int stepNo;

    public CallApiLog(){
        this.stepNo = 0;
    }

    public int plusStepNo(){
        this.stepNo = this.stepNo + 1;
        return this.stepNo;
    }

    @Override
    public String obtainDocumentId() {
        return this.logId;
    }

    @Override
    public JSONObject toJSONObject() {
        return (JSONObject) JSON.toJSON(this);
    }
}
