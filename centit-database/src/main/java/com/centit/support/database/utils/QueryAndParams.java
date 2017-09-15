package com.centit.support.database.utils;

import com.centit.support.compiler.Lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class QueryAndParams {
    public String queryStmt;
    public Object[] params;

    public String getSql() {
        return queryStmt;
    }

    public void setSql(String hql) {
        this.queryStmt = hql;
    }
    
    public String getQuery() {
        return queryStmt;
    }

    public void setQuery(String hql) {
        this.queryStmt = hql;
    }
    
    public String getHql() {
        return queryStmt;
    }

    public void setHql(String hql) {
        this.queryStmt = hql;
    }

    public Object[] getParams() {
        return params;
    }
    
    public Object getParam(int index){
        if( params==null)
            return null;
        if(index>=params.length)
            return null;
        return params[index];
    }

    public QueryAndParams() {
        this.queryStmt = null;
        this.params = null;
    }
    
    public QueryAndParams(String shql) {
        this.queryStmt = shql;
        this.params = null;
    }
    
    public QueryAndParams(String shql,final Object[] values) {
        this.queryStmt = shql;
        this.params = values;
    }
    
    public static QueryAndParams creepArrayParamForInQuery(String sql, Object[] sqlParams){
 
        StringBuilder sqlb = new StringBuilder();
        List<Object> params = new ArrayList<>();
        Lexer lex = new Lexer(sql,Lexer.LANG_TYPE_SQL);       
        
        int prePos = 0;
        int paramInd = -1;
        String aWord = lex.getAWord();
        while (aWord != null && !"".equals(aWord)) {
            if ("?".equals(aWord)) {
                paramInd ++;
                int curPos = lex.getCurrPos();
                if(curPos-1>prePos)
                    sqlb.append(sql.substring(prePos, curPos-1));

                Object obj = null;
                if(sqlParams!=null && paramInd<sqlParams.length)
                    obj = sqlParams[paramInd];

                if(obj==null){
                    params.add(obj);
                    sqlb.append("?");
                }else if (obj instanceof Collection) {
                    int n=0;
                    for(Object po :(Collection<?>) obj){
                        if(n>0)
                            sqlb.append(",");
                        sqlb.append("?");
                        params.add(po);
                        n++;
                    }
                } else if (obj instanceof Object[]) {
                    int n=0;
                    for(Object po :(Object[]) obj){
                        if(n>0)
                            sqlb.append(",");
                        sqlb.append("?");
                        params.add(po);
                        n++;
                    }                   
                }else{
                    params.add(obj);
                    sqlb.append("?");
                }
                prePos = lex.getCurrPos();
            } 

            aWord = lex.getAWord();
        }
        sqlb.append(sql.substring(prePos));        
        
        return new QueryAndParams(sqlb.toString(),params.toArray());
    }
    
    public static QueryAndParams createFromQueryAndNamedParams(String sql, Map<String,Object> namedParams){
 
        StringBuilder sqlb = new StringBuilder();
        List<Object> params = new ArrayList<Object>(namedParams.size()+5);
        Lexer lex = new Lexer(sql,Lexer.LANG_TYPE_SQL);       
        
        int prePos = 0;
        String aWord = lex.getAWord();
        while (aWord != null && !"".equals(aWord)) {
            if (":".equals(aWord)) {

                int curPos = lex.getCurrPos();
                if(curPos-1>prePos)
                    sqlb.append(sql.substring(prePos, curPos-1));

                aWord = lex.getAWord();
                if (aWord == null || "".equals(aWord))
                    break;
                Object obj = namedParams.get(aWord);
                if(obj==null){
                    params.add(null);
                    sqlb.append("?");
                }else if (obj instanceof Collection) {
                    int n=0;
                    for(Object po :(Collection<?>) obj){
                        if(n>0)
                            sqlb.append(",");
                        sqlb.append("?");
                        params.add(po);
                        n++;
                    }
                } else if (obj instanceof Object[]) {
                    int n=0;
                    for(Object po :(Object[]) obj){
                        if(n>0)
                            sqlb.append(",");
                        sqlb.append("?");
                        params.add(po);
                        n++;
                    }                   
                }else{
                    params.add(obj);
                    sqlb.append("?");
                }
                prePos = lex.getCurrPos();
            } 

            aWord = lex.getAWord();
        }
        sqlb.append(sql.substring(prePos));        
        
        return new QueryAndParams(sqlb.toString(),params.toArray());
    }
    
    public static QueryAndParams creepArrayParamForInQuery(QueryAndParams queryParam){
        return creepArrayParamForInQuery(
                queryParam.getQuery(),queryParam.getParams());
    }
    
    public static QueryAndParams createFromQueryAndNamedParams(QueryAndNamedParams namedParamQuery){
        return createFromQueryAndNamedParams(
                namedParamQuery.getQuery(),namedParamQuery.getParams());
    }

}
