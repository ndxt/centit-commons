package com.centit.support.json;

import com.alibaba.fastjson2.JSONObject;

import java.io.Serializable;
import java.util.List;

public class JSONDifferent implements Serializable {
    private static final long serialVersionUID = 20230301L;
    public static final String JSON_DIFF_TYPE_ADD = "+";
    public static final String JSON_DIFF_TYPE_DELETE = "-";
    public static final String JSON_DIFF_TYPE_UPDATE = "*";
    private String jsonPath;

    /**
     * 差异类别，新增 + A 删除 - D 修改 * U
     */
    private String diffType;

    private Object newData;

    private Object oldData;

    private Object objectId;


    private List<JSONDifferent> diffChildren;

    public JSONDifferent() {
        this.diffChildren = null;
    }

    public JSONDifferent(String jsonPath, String diffType, Object oldData, Object newData) {
        this.jsonPath = jsonPath;
        this.diffType = diffType;
        this.newData = newData;
        this.oldData = oldData;
        this.diffChildren = null;
        this.objectId = null;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getDiffType() {
        return diffType;
    }

    public void setDiffType(String diffType) {
        this.diffType = diffType;
    }

    public Object getNewData() {
        return newData;
    }

    public void setNewData(Object newData) {
        this.newData = newData;
    }

    public Object getOldData() {
        return oldData;
    }

    public void setOldData(Object oldData) {
        this.oldData = oldData;
    }

    public List<JSONDifferent> getDiffChildren() {
        return diffChildren;
    }

    public void setDiffChildren(List<JSONDifferent> diffChildren) {
        this.diffChildren = diffChildren;
    }

    public Object getObjectId() {
        return objectId;
    }

    public void setObjectId(Object objectId) {
        this.objectId = objectId;
    }

    private JSONObject toJson(){
        JSONObject jsonObj = toJson(this.diffChildren);
        jsonObj.put("diffType", this.diffType);
        if(this.newData != null){
            jsonObj.put("newData", this.newData);
        }
        if(this.oldData != null){
            jsonObj.put("oldData", this.oldData);
        }
        if(this.objectId != null){
            jsonObj.put("objectId", this.objectId);
        }

        return jsonObj;
    }

    private static JSONObject toJson(List<JSONDifferent> diffList){
        JSONObject jsonObj = new JSONObject();
        if(diffList!=null && diffList.size()>0) {
            for (JSONDifferent childDiff : diffList) {
                jsonObj.put(childDiff.getJsonPath(), childDiff.toJson());
            }
        }
        return jsonObj;
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(this.jsonPath, this.toJson());
        return jsonObj;
    }
}
