package com.centit.support.quartz;

import com.alibaba.fastjson2.JSON;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时任务抽象类
 * DisallowConcurrentExecution 这个注解要求同一个任务不能同时运行，
 * 这个是否会带了问题 需要验证
 */
//@DisallowConcurrentExecution
public abstract class AbstractQuartzJob implements Job {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractQuartzJob.class);

    /**
     * 正式运行任务的地方
     * @param context job运行环境变量
     * @return 是否成功执行
     * @throws JobExecutionException 执行异常
     */
    protected abstract boolean runRealJob(JobExecutionContext context) throws JobExecutionException;

    /**
     * 通过 context 构建任务的实例，job的参数内容在 context.getMergedJobDataMap() 中。
     * 一般这些信息是写在数据库中的，这个函数就是通过这些参数来初始化任务的内容
     * @param context job运行环境变量
     */
    protected abstract void loadExecutionContext(JobExecutionContext context);

    /**
     * 在任务开始之前记录日志
     * @param context job运行环境变量
     */
    protected void beforeRun(JobExecutionContext context){
        JobKey jobKey = context.getJobDetail().getKey();
        logger.debug("beforeRun ：" +jobKey.getGroup()+ ":"+ jobKey.getName());
        /*
        OperationLogCenter.log(OperationLog.create()
                .method("before").user("admin").unit("U00000")
                .operation("quartz").tag(jobKey.getGroup()+ ":"+ jobKey.getName())
                .content(JSON.toJSONString(context.getMergedJobDataMap()))); */
    }
    /**
     * 在任务成功执行之后记录日志
     * @param context job运行环境变量
     */
    protected void onSuccess(JobExecutionContext context){
        JobKey jobKey = context.getJobDetail().getKey();
        logger.debug("onSuccess ：" +jobKey.getGroup()+ ":"+ jobKey.getName());
        /*JobKey jobKey = context.getJobDetail().getKey();
        OperationLogCenter.log(
            OperationLog.create()
                .method("sucess").user("admin").unit("U00000")
                .operation("quartz").tag(jobKey.getGroup()+ ":"+ jobKey.getName())
                .content(JSON.toJSONString(context.getMergedJobDataMap())));*/
    }
    /**
     * 在任务运行失败时记录日志
     * @param context job运行环境变量
     */
    protected void onError(JobExecutionContext context){
        JobKey jobKey = context.getJobDetail().getKey();
        logger.error("onError ：" +jobKey.getGroup()+ ":"+ jobKey.getName()
             + "\r\n\t" + JSON.toJSONString(context.getMergedJobDataMap()));
        /*OperationLogCenter.log(
            OperationLog.create()
                .method("error").user("admin").unit("U00000")
                .operation("quartz").tag(jobKey.getGroup()+ ":"+ jobKey.getName())
                .content(JSON.toJSONString(context.getMergedJobDataMap())));*/
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 这边记录 任务启动日志，内容需要重构
        loadExecutionContext(context);
        beforeRun(context);
        boolean success = runRealJob(context);
        // 这边记录 任务完成日志，内容需要重构
        if(success) {
            onSuccess(context);
        } else {
            onError(context);
        }
    }
}
