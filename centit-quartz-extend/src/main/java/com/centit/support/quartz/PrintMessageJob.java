package com.centit.support.quartz;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class PrintMessageJob extends AbstractQuartzJob {

    private String message;
    @Override
    protected void loadExecutionContext(JobExecutionContext context){
        JobDataMap paramMap = context.getMergedJobDataMap();// .getJobDetail().getJobDataMap();
        message = paramMap.getString("message");
    }

    @Override
    protected boolean runRealJob(JobExecutionContext context) throws JobExecutionException {
        System.out.println(message);
        return true;
    }

}
