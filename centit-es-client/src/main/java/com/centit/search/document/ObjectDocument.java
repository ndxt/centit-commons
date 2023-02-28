package com.centit.search.document;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.annotation.ESField;
import com.centit.search.annotation.ESType;
import com.centit.search.utils.ObjectTextExtractor;
import com.centit.support.security.Md5Encoder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by codefan on 17-6-1.
 */
@Data
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
    @ESField(type="text", query = true, revert = false, highlight = true, analyzer = "ik_smart")
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
}
