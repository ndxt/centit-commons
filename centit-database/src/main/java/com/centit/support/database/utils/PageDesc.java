package com.centit.support.database.utils;

/**
 * Created by codefan on 17-9-19.
 */
public class PageDesc {

    private int totalRows;
    private int pageSize;
    /**
     * base 1
     */
    private int pageNo;

    public static PageDesc createNotPaging(){
        return new PageDesc(-1,-1,-1);
    }

    public PageDesc() {
        totalRows = 0;
        pageSize = 20;
        pageNo = 1;
    }

    public PageDesc(int pn, int ps) {
        totalRows = 0;
        pageSize = ps;
        pageNo = pn;
    }

    public PageDesc(int pn, int ps, int tr) {
        totalRows = tr;
        pageSize = ps;
        pageNo = pn;
    }

    public int getTotalRows() {
        return totalRows;
    }

    /**
     * 设置条目总数
     * @param totalRows Integer 用这个类型主要是为了应对查询总数的语句返回null的情况
     */
    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows==null ? 0 : totalRows;
    }

    /**
     * 获取每页最大条目数
     * @return 每页最大条目数
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页最大条目数
     * @param pageSize 每页最大条目数
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 应对遗留系统命名不一致的情况
     * @param pageSize 每页数量
     */
    public void setRows(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取当前页码
     * @return 当前页码 base 1
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * 设置当前页码
     * @param pageNo 当前页码 base 1
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * 应对遗留系统命名不一致的情况
     * @param pageNo 当前页码 base 1
     */
    public void setPage(int pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * pageNo Base 1
     * @return 当前页第一条记录index
     */
    public int getRowStart() {
        return ( pageNo > 1 ? pageNo - 1 : 0 ) * pageSize;
    }

    /**
     * pageNo Base 1
     * @return 当前页最后一条记录index （不包含这条记录）
     */
    public int getRowEnd() {
        return ( pageNo > 1 ? pageNo : 1 ) * pageSize;
    }

    /**
     * 没有分页，仅仅返回 条目总数
     * @param totalRows 条目总数
     */
    public void noPaging(int totalRows){
        this.totalRows = totalRows;
        this.pageSize = totalRows;
        this.pageNo = 1;
    }
}
