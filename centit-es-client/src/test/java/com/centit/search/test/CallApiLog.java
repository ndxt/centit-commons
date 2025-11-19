package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.centit.search.annotation.ESField;
import com.centit.search.annotation.ESType;
import com.centit.search.document.ESDocument;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhf
 */
@Data
//@Schema(title = "调用API日志")
@ESType(indexName = "callapilog", shards = 5)
public class CallApiLog implements ESDocument, Serializable {

    private static final long serialVersionUID =  1L;

    @ESField(type = "keyword") //(name="log_id")
    //@Schema(title =  "日志ID", hidden = true)
    private String logId;

    @ESField(type = "keyword") //(name="task_id")
    //@Schema(title =  "API网关ID, apiId", hidden = true)
    private String taskId;

    @ESField(type = "keyword") //(name="module_id")
    //@Schema(title =  "菜单ID", hidden = true)
    private String moduleId;

    @ESField(type = "keyword") //(name="application_id")
    //@Schema(title =  "项目id", hidden = true)
    private String applicationId;

    @ESField(type = "keyword") //(name="top_unit")
    //@Schema(title =  "菜单ID", hidden = true)
    private String topUnit;

    @ESField(type = "keyword",query = true, highlight = true, analyzer = "ik_smart")
    //@Schema(title =  "请求方地址")
    private String requestIp;

    @ESField(type = "text",query = true, highlight = true, analyzer = "ik_smart")
    //@Schema(title =  "请求参数")
    private String requestParams;

    //@Schema(title =  "执行开始时间")
    @ESField(type = "date") //
    private Date runBeginTime;

    //@Schema(title =  "执行结束时间")
    @ESField(type = "date") //
    private Date runEndTime;

    @ESField(type = "keyword") //(name="run_type")
    //@Schema(title =  "执行方式", required = true)
    private String runType;

    @ESField(type = "keyword") //(name="runner")
    //@Schema(title =  "执行人员")
    private String runner;

    @ESField(type = "text",query = true, highlight = true, analyzer = "ik_smart") //(name="other_message")
    //@Schema(title =  "其他提示信息", required = true)
    private String otherMessage;

    @ESField(type = "keyword") //(name="error_pieces")
    //@Schema(title =  "失败条数")
    private Integer errorPieces;

    @ESField(type = "keyword") //(name="success_pieces")
    //@Schema(title =  "成功条数")
    private Integer successPieces;

    @ESField(type = "keyword") //(name="api_type")
    //@Schema(title =  "API类别，是草稿还是正式运行的日志，0 草稿，1 正式")
    private Integer apiType;

    //@OneToMany(targetEntity = TaskDetailLog.class)
    //@JoinESField(type = "keyword") //(name = "log_id", referencedESField(type = "keyword") //Name = "log_id")
    @JSONField(serialize = false, deserialize = false)
    //@Schema(title =  "日志明细")
    private List<CallApiLogDetail> detailLogs;

    public void addDetailLog(CallApiLogDetail detailLog) {
        if (this.detailLogs == null) {
            this.detailLogs = new ArrayList<>();
        }
        this.detailLogs.add(detailLog);
    }
    /**
     * 临时记录 执行步骤
     */
    private int stepNo;

    public CallApiLog(){
        this.stepNo = 0;
        this.successPieces = 0;
        this.errorPieces = 0;
        this.apiType = 0;
    }

    public void plusStepNo(){
        this.stepNo = this.stepNo + 1;
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
