package com.centit.support.json;

import java.io.Serializable;

public class JsonDifferent implements Serializable {
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

    public JsonDifferent() {
    }

    public JsonDifferent(String jsonPath, String diffType, Object oldData, Object newData) {
        this.jsonPath = jsonPath;
        this.diffType = diffType;
        this.newData = newData;
        this.oldData = oldData;
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
}
