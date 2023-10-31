package com.centit.search.document;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.centit.search.annotation.ESField;
import com.centit.search.annotation.ESType;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by codefan on 17-6-1.
 */
@ESType(indexName="files", replicas = 2, shards = 5)
public class FileDocument implements ESDocument, Serializable {
    //public static final String ES_DOCUMENT_TYPE = "file";
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
     * 关联的业务对象主键 键值对形式
     */
    @ESField(type="keyword")
    private String optTag;
    /**
     * 关联的方法 可以为空
     */
    @ESField(type="keyword")
    private String optMethod;
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
     * 文档名称
     */
    @ESField(type="text", query = true, analyzer = "ik_smart")
    private String fileName;
    /**
     * 文档的摘要
     */
    @ESField(type="text",query = true, highlight = true, analyzer = "ik_smart")
    private String fileSummary;
    /**
     * 文档的ID
     */
    @ESField(type="keyword")
    private String fileId;
    /**
     * 文档的Md5
     */
    @ESField(type="keyword")
    private String fileMD5;
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
    /**
     * 文档的关键字 2018-7-23 hpz添加
     */
    @ESField(type="text", query = true, analyzer = "ik_smart")
    private String [] keywords;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FileDocument)) {
            return false;
        }

        FileDocument that = (FileDocument) o;

        if (!getOsId().equals(that.getOsId())) {
            return false;
        }
        if (!getFileId().equals(that.getFileId())) {
            return false;
        }
        return getFileMD5().equals(that.getFileMD5());
    }

    @Override
    public int hashCode() {
        return obtainDocumentId().hashCode();
        /*int result = getOsId().hashCode();
        result = 31 * result + getFileId().hashCode();
        result = 31 * result + getFileMD5().hashCode();
        return result;*/
    }

    @Override
    public String toString(){
        return toJsonString();
    }

    public String toJsonString(){
        return JSON.toJSONString(this);
    }

    @Override
    public String obtainDocumentId() {
        return fileId;
    }


    @Override
    public JSONObject toJSONObject() {
        return (JSONObject)JSON.toJSON(this);
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

    public String getOptTag() {
        return optTag;
    }

    public void setOptTag(String optTag) {
        this.optTag = optTag;
    }

    public String getOptMethod() {
        return optMethod;
    }

    public void setOptMethod(String optMethod) {
        this.optMethod = optMethod;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSummary() {
        return fileSummary;
    }

    public void setFileSummary(String fileSummary) {
        this.fileSummary = fileSummary;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
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

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }
}
