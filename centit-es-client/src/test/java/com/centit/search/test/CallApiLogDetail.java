package com.centit.search.test;

import lombok.Data;

import java.util.Date;


/**
 * @author zhf
 */
@Data
public class CallApiLogDetail implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    //@OrderBy
    //@Schema(title =  "执行步骤序号")
    private Integer stepNo;

    //@Schema(title =  "操作节点id", required = true)
    private String optNodeId;

    //@Schema(title =  "日志类别", required = true)
    private String logType;

    //@Schema(title =  "执行开始时间")
    private Date runBeginTime;

    //@Schema(title =  "执行结束时间")
    private Date runEndTime;

    //@Schema(title =  "任务明细描述")
    // @Basic(fetch = FetchType.LAZY)
    private String logInfo;

    //@Schema(title =  "成功条数")
    private Integer successPieces;

    //@Schema(title =  "失败条数")
    private Integer errorPieces;

    public CallApiLogDetail(){
        successPieces = 0;
        errorPieces = 0;
    }
}
