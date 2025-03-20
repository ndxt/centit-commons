package com.centit.search.test;

import com.alibaba.fastjson2.JSONObject;
import com.centit.search.annotation.ESField;
import com.centit.search.annotation.ESType;
import com.centit.search.document.ESDocument;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * create by scaffold 2017-05-08
 *
 * @author codefan@sina.com
 * <p>
 * 系统帮助文档null
 */

@ESType(indexName="helpdocs-2", replicas = 2, shards = 5)
public class HelpDoc implements ESDocument, java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文档ID null
     */
    @ESField(type="keyword")
    private String docId;

    /**
     * 类别ID null
     */
    @ESField(type="keyword")
    //@Length(max = 32, message = "字段长度不能大于{max}")
    private String catalogId;
    /**
     * 文档标题 null
     */
    @ESField(type="text", query = true, highlight = true, analyzer = "ik_smart")
    private String docTitle;
    /**
     * 文档层级 null
     */
//    @NotBlank(message = "字段不能为空")
    private int docLevel;
    /**
     * 文档路径 null
     */
    @ESField(type="keyword")
    //@Length(max = 500, message = "字段长度不能大于{max}")
    private String docPath;
    /**
     * 副文本 CLOB 字段
     */
    @ESField(type="text", query = true, highlight = true, analyzer = "ik_smart")
    private String docFile;
    /**
     * 业务系统ID null
     */
    @ESField(type="keyword")
    //@Length(max = 20, message = "字段长度不能大于{max}")
    private String osId;
    /**
     * 业务项目模块 模块，或者表
     */
    @ESField(type="keyword")
    //@Length(max = 64, message = "字段长度不能大于{max}")
    private String optId;
    /**
     * 业务操作方法 方法，或者字段
     */
    @ESField(type="keyword")
    //@Length(max = 64, message = "字段长度不能大于{max}")
    private String optMethod;

    /**
     * 归属人员 null， 他有管理权
     */
    @ESField(type="keyword")
    //@Length(max = 32, message = "字段长度不能大于{max}")
    private String ownerUser;

    /**
     * 编辑人员 null
     */
    @ESField(type="keyword")
    //@Length(max = 32, message = "字段长度不能大于{max}")
    private String updateUser;
    /**
     * 是否开放编辑，默认为true
     */
    //@Length(max = 1, message = "字段长度不能大于{max}")
    private boolean openEdit;
    /**
     * 编辑时间 null
     */
    @ESField(type="keyword")
    private Date lastUpdateTime;

    /**
     * 上一个文档id
     */

    @ESField(type="keyword")
    private String prevDocId;


    private Integer orderInd;

    public List<HelpDoc> getChildren() {
        return children;
    }

    public void setChildren(List<HelpDoc> children) {
        this.children = children;
    }


    private List<HelpDoc> children;

    // Constructors

    /**
     * default constructor
     */
    public HelpDoc() {
    }

    /**
     * minimal constructor
     */
    public HelpDoc(String docId, String docTitle, int docLevel, String docPath, String optId) {
        this.docId = docId;
        this.docTitle = docTitle;
        this.docLevel = docLevel;
        this.docPath = docPath;
        this.optId = optId;
    }

    public Integer getOrderInd() {
        return orderInd;
    }

    public void setOrderInd(Integer orderInd) {
        this.orderInd = orderInd;
    }

    public String getPrevDocId() {
        return prevDocId;
    }

    public void setPrevDocId(String prevDocId) {
        this.prevDocId = prevDocId;
    }

    public String getDocId() {
        return this.docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }
    // Property accessors

    public String getCatalogId() {
        return this.catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public String getDocTitle() {
        return this.docTitle;
    }

    public String getLabel() {
        return this.docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public int getDocLevel() {
        return this.docLevel;
    }

    public void setDocLevel(int docLevel) {
        this.docLevel = docLevel;
    }

    public String getDocPath() {
        return this.docPath;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    public String getDocFile() {
        return this.docFile;
    }

    public void setDocFile(String docFile) {
        this.docFile = docFile;
    }

    public String getOsId() {
        return this.osId;
    }

    public void setOsId(String osId) {
        this.osId = osId;
    }

    public String getOptId() {
        return this.optId;
    }

    public void setOptId(String optId) {
        this.optId = optId;
    }

    public String getOptMethod() {
        return this.optMethod;
    }

    public void setOptMethod(String optMethod) {
        this.optMethod = optMethod;
    }

    public String getUpdateUser() {
        return this.updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Date getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public HelpDoc copy(HelpDoc other) {

        this.setDocId(other.getDocId());
        this.catalogId = other.getCatalogId();
        this.docTitle = other.getDocTitle();
        this.docLevel = other.getDocLevel();
        this.docPath = other.getDocPath();
        this.docFile = other.getDocFile();
        this.osId = other.getOsId();
        this.optId = other.getOptId();
        this.optMethod = other.getOptMethod();
        this.updateUser = other.getUpdateUser();
        this.lastUpdateTime = other.getLastUpdateTime();

        return this;
    }

    public HelpDoc copyNotNullProperty(HelpDoc other) {

        if (other.getDocId() != null)
            this.setDocId(other.getDocId());

        if (other.getCatalogId() != null)
            this.catalogId = other.getCatalogId();
        if (other.getDocTitle() != null)
            this.docTitle = other.getDocTitle();
        if (other.getDocLevel() != -1)
            this.docLevel = other.getDocLevel();
        if (other.getDocPath() != null)
            this.docPath = other.getDocPath();
        if (other.getDocFile() != null)
            this.docFile = other.getDocFile();
        if (other.getOsId() != null)
            this.osId = other.getOsId();
        if (other.getOptId() != null)
            this.optId = other.getOptId();
        if (other.getOptMethod() != null)
            this.optMethod = other.getOptMethod();
        if (other.getUpdateUser() != null)
            this.updateUser = other.getUpdateUser();
        if (other.getLastUpdateTime() != null)
            this.lastUpdateTime = other.getLastUpdateTime();
        if (other.getPrevDocId() != null) {
            this.setPrevDocId(other.getPrevDocId());
        }
        this.setOrderInd(other.getOrderInd());
        return this;
    }

    public HelpDoc clearProperties() {

        this.catalogId = null;
        this.docTitle = null;
        this.docLevel = -1;
        this.docPath = null;
        this.docFile = null;
        this.osId = null;
        this.optId = null;
        this.optMethod = null;
        this.updateUser = null;
        this.lastUpdateTime = null;

        return this;
    }

    private String filterTag(String content) {
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(content);
        content = m_style.replaceAll(""); //过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(content);
        content = m_html.replaceAll(""); //过滤html标签

        return content.trim(); //返回文本字符串

    }

    @Override
    public String obtainDocumentId() {
        return docId;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = JSONObject.from(this);
        if (StringUtils.isNotBlank(this.getDocFile())) {
            object.put("docFile", filterTag(this.getDocFile()));
        } else {
            object.put("docFile", this.getDocTitle());
        }
        return object;
    }
}
