package com.centit.support.quartz;

import com.centit.support.algorithm.GeneralAlgorithm;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.common.LeftRightPair;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class JavaBeanJob extends AbstractQuartzJob {

    private Object bean;
    private String beanName;
    private String methodName;// get put post delete
    private Map<String,Object> params;

    @Override
    protected void loadExecutionContext(JobExecutionContext context){
        JobDataMap paramMap = context.getMergedJobDataMap();
        bean = paramMap.get("bean");
        beanName = paramMap.getString("beanName");
        methodName = paramMap.getString("methodName");
        Object obj = paramMap.get("params");
        if(obj!=null){
            params =(Map) GeneralAlgorithm.castObjectToType(obj, Map.class);
        }else{
            params = new HashMap<>(1);
        }
    }

    @Override
    protected boolean runRealJob(JobExecutionContext context) throws JobExecutionException {
        if(bean == null) {
            WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
            bean = webApplicationContext.getBean(beanName);
        }
        LeftRightPair<Method, Object[]> mp = ReflectionOpt.getMatchBestMethod(bean.getClass(), methodName, params);
        boolean ret = true;
        if(/*mp !=null &&*/ mp.getLeft() != null){
            try {
                if(mp.getRight() == null) {
                    mp.getLeft().invoke(bean);
                }else{
                    mp.getLeft().invoke(bean, mp.getRight());
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getLocalizedMessage(),e);
                ret = false;
            }
        }
        return ret;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
