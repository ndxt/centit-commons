package com.centit.search.document;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.annotation.ESField;
import com.centit.search.annotation.ESType;
import com.centit.search.utils.ObjectTextExtractor;
import com.centit.support.security.Md5Encoder;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by codefan on 17-6-1.
 */

@ESType(indexName="objects", replicas = 2, shards = 5)
public class ObjectDocument implements ESDocument,Serializable {
    private static final long serialVersionUID =  1L;
    /**
     * 所属系统
     */
    @ESField(type="keyword")
    private String osId;
    /**
     * 所属业务
     */
    @ESField(type="keyword")
    private String optId;
    /**
     * 关联的方法 可以为空
     */
    @ESField(type="keyword")
    private String optMethod;
    /**
     * 关联的业务对象主键 键值对的形式
     */
    @ESField(type="keyword")
    private String optTag;
    /**
     * 文档反向关联url
     */
    @ESField(type="text")
    private String optUrl;
    /**
     * 所属人员 可以为空
     */
    @ESField(type="keyword")
    private String userCode;
    /**
     * 所属机构 可以为空
     */
    @ESField(type="keyword")
    private String unitCode;

    /**
     * 对象的标题，用于显示
     */
    @ESField(type="text", query = true, highlight = true, analyzer = "ik_smart")
     private String title;
    /**
     * 文档的内容，用于索引
     */
    @ESField(type="text", query = true, highlight = true, analyzer = "ik_smart")
    private String content;
    /**
     * 文档创建时间
     */
    @ESField(type="date")
    private Date createTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ObjectDocument)) {
            return false;
        }

        ObjectDocument that = (ObjectDocument) o;

        if (!getOsId().equals(that.getOsId())) {
            return false;
        }
        if (!getOptId().equals(that.getOptId())) {
            return false;
        }
        return getOptTag().equals(that.getOptTag()) && (getOptMethod() != null ? getOptMethod().equals(that.getOptMethod()) : that.getOptMethod() == null);
    }

    @Override
    public int hashCode() {
        return obtainDocumentId().hashCode();
    }

    @Override
    public String toString(){
        return toJsonString();
    }

    public String toJsonString(){
        return JSON.toJSONString(this);
    }

    @Override
    //@JSONField(serialize=false,deserialize=false)
    public String obtainDocumentId() {
        String objectId = osId + ":" + optId + ":" + optTag;
        if(StringUtils.length(objectId) > 36){
            objectId =  optId+":"+ Md5Encoder.encode(objectId);
        }
        return objectId;
    }

    @Override
    public JSONObject toJSONObject() {
        return (JSONObject)JSON.toJSON(this);
    }

    public ObjectDocument contentObject(Object obj){
        this.content = ObjectTextExtractor.extractText(obj);
        return this;
    }

    public String getOsId() {
        return osId;
    }

    public void setOsId(String osId) {
        this.osId = osId;
    }

    public String getOptId() {
        return optId;
    }

    public void setOptId(String optId) {
        this.optId = optId;
    }

    public String getOptMethod() {
        return optMethod;
    }

    public void setOptMethod(String optMethod) {
        this.optMethod = optMethod;
    }

    public String getOptTag() {
        return optTag;
    }

    public void setOptTag(String optTag) {
        this.optTag = optTag;
    }

    public String getOptUrl() {
        return optUrl;
    }

    public void setOptUrl(String optUrl) {
        this.optUrl = optUrl;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
