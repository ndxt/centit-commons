package com.centit.support.database.utils;

import java.util.HashMap;
import java.util.Map;
/**
 * 这个类比较简单主要目的是将查询语句和其对应的参数变量放在一起便于参数传输。其中查询语句中的变量为命名变量，形式为：变量名。
 * @author codefan
 */
public class QueryAndNamedParams {
    /**
     * 查询语句，可以是hql也可能是sql
     */
    private String queryStmt;
    /**
     * 变量和变量值的对应map
     */
    private Map<String, Object> params;

    public QueryAndNamedParams() {
        this.queryStmt = null;
        this.params = null;
    }

    public QueryAndNamedParams(String shql) {
        this.queryStmt = shql;
        this.params = null;
    }
    
    public QueryAndNamedParams(String shql, Map<String, Object> values) {
        this.queryStmt = shql;
        this.params = values;
    }

    @Deprecated
    public String getSql() {
        return queryStmt;
    }

    @Deprecated
    public void setSql(String hql) {
        this.queryStmt = hql;
    }
    
    public String getQuery() {
        return queryStmt;
    }

    public void setQuery(String hql) {
        this.queryStmt = hql;
    }
    @Deprecated
    public String getHql() {
        return queryStmt;
    }
    @Deprecated
    public void setHql(String hql) {
        this.queryStmt = hql;
    }

    public Map<String, Object> getParams() {
        if(params == null)
            params = new HashMap<> ();
        return params;
    }
    
    public Object getParam(String paramName){
        if( params==null)
            return null;
       
        return params.get(paramName);
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    /**
     * 这个方法返回this所以可以  addParam(k1，v1).addAllParams(Map K，V）.
     * addParam(k2,v2)便捷的添加多个参数
     * @param paramName paramName
     * @param paramValue paramValue
     * @return QueryAndNamedParams
     */
    public QueryAndNamedParams addParam(String paramName,Object paramValue) {
        if(params == null)
            params = new HashMap<> ();
        params.put(paramName, paramValue);
        return this;
    }
    
    public QueryAndNamedParams addAllParams(Map<String, Object> oParams) {
        if(oParams==null)
            return this;
        if(params == null)
            params = new HashMap<> ();
        params.putAll(oParams);
        /*for(Map.Entry<String, Object> param : oParams.entrySet())
            params.put(param.getKey(), param.getValue());*/
        return this;
    }
}
