package com.centit.search.service;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * Created by codefan on 17-6-12.
 */
@SuppressWarnings("unused")
public interface Searcher {
    int SEARCH_FRAGMENT_SIZE = 200;
    int SEARCH_FRAGMENT_NUM = 2;
    /**
     * 检索所有文档
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 检索的结果集
     */
    Pair<Long,List<Map<String, Object>>> search(String queryWord, int pageNo, int pageSize);

    /**
     * 检索所有文档
     * @param fieldFilter 过滤的文件
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 检索的结果集
     */
    Pair<Long,List<Map<String, Object>>> search(Map<String, Object> fieldFilter, String queryWord, int pageNo, int pageSize);

    /**
     * 检索某个业务
     * @param optId 所属业务id
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 检索的结果集
     */
    Pair<Long,List<Map<String, Object>>> searchOpt(String optId, String  queryWord, int pageNo, int pageSize);

    /**
     * 根据文档所属 人员来检索
     * @param owner 所属人员
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 检索的结果集
     */
    Pair<Long,List<Map<String, Object>>> searchOwner(String owner, String queryWord, int pageNo, int pageSize);

    /**
     * 根据文档所属 人员来检索
     * @param owner 所属人员
     * @param optId 所属业务id
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 检索的结果集
     */
    Pair<Long,List<Map<String, Object>>> searchOwner(String owner, String optId, String queryWord, int pageNo, int pageSize);

    /**
     * 根据文档所属机构来检索
     * @param units 文档所属机构
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 检索的结果集
     */

    Pair<Long,List<Map<String, Object>>> searchUnits(String [] units, String queryWord, int pageNo, int pageSize);

    /**
     * 根据文档所属机构来检索
     * @param units 文档所属机构
     * @param optId 所属业务id
     * @param queryWord 检索的关键字
     * @param pageNo 当前页
     * @param pageSize 每页多少条
     * @return 检索的结果集
     */
    Pair<Long,List<Map<String, Object>>> searchUnits(String [] units, String optId, String queryWord, int pageNo, int pageSize);

}
