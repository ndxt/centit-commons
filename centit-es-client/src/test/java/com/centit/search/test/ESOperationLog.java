
package com.centit.search.test;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.annotation.ESField;
import com.centit.search.annotation.ESType;
import com.centit.search.document.ESDocument;
import lombok.Data;

import java.io.Serializable;

@Data
@ESType(indexName = "esoperationlog")
public class ESOperationLog  implements ESDocument , Serializable {

    private static final long serialVersionUID =  1L;

    //es id
    @ESField(type = "keyword")
    private  String  logId;

    /**
     * 日志级别，用户可以自己解释这个属性
     */
    @ESField(type = "keyword")
    private String logLevel;

    /**
     * 操作用户
     */
    @ESField(type = "keyword")
    private String userCode;


    /**
     * 操作用户所属机构
     */
    @ESField(type = "keyword")
    private String unitCode;

    @ESField(type = "keyword")
    private String topUnit;
    /**
     * 同一个请求同一个 协作号，主要用于调试和跟踪
     */
    @ESField(type = "keyword")
    private String correlationId;

    /**
     * 操作时间
     */
    @ESField(type = "date")
    private Long optTime;

    /**
     * 业务系统编号
     */
    @ESField(type = "keyword")
    private String osId;

    /**
     * 操作业务编号
     */
    @ESField(type = "keyword")
    private String optId;

    /**
     * 业务操作方法
     */
    @ESField(type = "keyword")
    private String optMethod;

    /**
     * 业务对象组件，复合主键用&amp;连接格式与url参数类似
     */
    @ESField(type = "keyword")
    private String optTag;

    @ESField(type = "keyword")
    private String loginIp;

    /**
     * 日志内容描述; 也可以是json
     */
    @ESField(type = "text", query = true, highlight = true, analyzer = "ik_smart")
    private String optContent;

    /**
     * 更新前旧值，json格式，这个字段不是必须的
     */
    @ESField(type = "text", query = true, highlight = true, analyzer = "ik_smart")
    private String newValue;

    /**
     * 更新后新值，json格式，这个字段不是必须的
     */
    @ESField(type = "text", query = true, highlight = true, analyzer = "ik_smart")
    private String oldValue;


    @Override
    public String obtainDocumentId() {
        return this.logId;
    }

    @Override
    public JSONObject toJSONObject() {
        return (JSONObject) JSON.toJSON(this);
    }

}

