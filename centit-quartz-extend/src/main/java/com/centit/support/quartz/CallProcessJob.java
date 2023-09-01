package com.centit.support.quartz;

import com.centit.support.algorithm.GeneralAlgorithm;
import com.centit.support.algorithm.StringBaseOpt;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallProcessJob extends AbstractQuartzJob {

    private String command;
    /**
     * envp 环境变量，在需要修改环境变量时才有用
     */
    private Map<String,Object> envp;
    /**
     * 指定工作目录
     */
    private String dir;
    @Override
    protected void loadExecutionContext(JobExecutionContext context){
        JobDataMap paramMap = context.getMergedJobDataMap();// .getJobDetail().getJobDataMap();
        command = paramMap.getString("command");
        Object obj = paramMap.get("envp");
        if(obj!=null){
            envp = (Map) GeneralAlgorithm.castObjectToType(obj, Map.class);
        }else{
            envp = new HashMap<>(1);
        }
    }

    @Override
    protected boolean runRealJob(JobExecutionContext context) throws JobExecutionException {
        boolean ret = true;
        try {
            if (envp == null || envp.isEmpty()) {
                if(StringUtils.isBlank(this.dir)) {
                    Runtime.getRuntime().exec(command);
                } else {
                    Runtime.getRuntime().exec(command,
                        new String[0],
                        new File(this.dir));
                }
            } else {
                List<String> envp = new ArrayList<>(this.envp.size()+1);
                for(Map.Entry<String, Object> ent : this.envp.entrySet()){
                    envp.add(ent.getKey()+"="+ StringBaseOpt.castObjectToString(ent.getValue()));
                }
                if(StringUtils.isBlank(this.dir)) {
                    Runtime.getRuntime().exec(command, envp.toArray(new String[envp.size()]));
                } else {
                    Runtime.getRuntime().exec(command,
                        envp.toArray(new String[envp.size()]),
                        new File(this.dir));
                }
            }
        } catch (IOException e){
            logger.error(e.getLocalizedMessage(), e);
            ret = false;
        }
        //process.waitFor();
        return ret;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setEnvp(Map<String, Object> envp) {
        this.envp = envp;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
