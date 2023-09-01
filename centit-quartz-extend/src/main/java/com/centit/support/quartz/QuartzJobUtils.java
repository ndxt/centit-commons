package com.centit.support.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 先腾定时任务抽象类
 * Quartz 规定一个jobDetail 可以有多个 trigger（反之不行）
 * 框架为了简单将 一个jobDetail 只对应一个 trigger 这样在方法调用中
 *  triggerName 就用 jobName， triggerGroupName 就用 jobGroupName
 */
public abstract class QuartzJobUtils {

    protected static final Logger logger = LoggerFactory.getLogger(QuartzJobUtils.class);
    private static ConcurrentHashMap<String, Class<? extends AbstractQuartzJob>> jobTypeMap
        = new ConcurrentHashMap<>(10);
    /**
     * 注册 job的类型，可以自行扩展 job类别
     * 类型 必须 扩展AbstractQuartzJob 类， 在这个类中完成了 日志的记录工作，
     * 日志记录工作也是可以在子类中覆盖的
     * 目前一共设置了四个类型的job
     * 1, PrintMessageJob 这个仅用来测试打印消息
     * 2, CallProcessJob 调用系统程序
     * 3, JavaBeanJob 调用bean的方法
     * 4, HttpRquestJob 调用一个web 请求
     * @param jobType job类别
     * @param type 类型 必须 扩展AbstractQuartzJob 类
     */
    public static void registerJobType(String jobType, Class<? extends AbstractQuartzJob> type){
        jobTypeMap.put(jobType, type);
    }
    /**
     * 定时任务
     * @param scheduler 主API
     * @param jobName 和 triggerName 一致，
     * @param jobGroup 和 triggerGroupName 一致
     * @param jobType 任务类别
     * @param cronExpress 定时器描述
     * @param param 数据
     * @throws SchedulerException 执行异常
     */
    public static void createOrReplaceCronJob(Scheduler scheduler,
                                              String jobName, String jobGroup,
                                              String jobType, String cronExpress,
                                              Map<String, Object> param) throws SchedulerException {
        createOrReplaceCronJob( scheduler, jobName, jobGroup,
            jobTypeMap.get(jobType), cronExpress, param);
    }
    /**
     * 定时任务
     * @param scheduler 主API
     * @param jobName 和 triggerName 一致，
     * @param jobGroup 和 triggerGroupName 一致
     * @param jobClass 任务类
     * @param cronExpress 定时器描述
     * @param param 数据
     * @throws SchedulerException 执行异常
     */

    public static void createOrReplaceCronJob(Scheduler scheduler,
                                              String jobName, String jobGroup,
                                              Class<? extends Job> jobClass, String cronExpress,
                                              Map<String, Object> param) throws SchedulerException {
        /*WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        SchedulerFactoryBean schedulerFactoryBean = webApplicationContext.getBean(SchedulerFactoryBean.class);
        Scheduler scheduler = schedulerFactoryBean.getScheduler();*/
        if (CronExpression.isValidExpression(cronExpress)){

            try { // 这个地方是周抄添加的表达式判断，如果表达式不符合要求将不会被执行
                CronExpression cc = new CronExpression(cronExpress);
                cc.getNextValidTimeAfter(new Date());
            } catch (Exception e) {
                logger.error(cronExpress + "：" + e.getMessage(), e.getCause());
                return;
            }

            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            // 新建一个任务
            if(trigger == null){
                JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData(new JobDataMap(param)).build();
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpress);
                trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup)
                    .withSchedule(scheduleBuilder).build();
                scheduler.scheduleJob(jobDetail, trigger);
            }else {
                if (!(trigger instanceof CronTrigger)) {
                    throw new SchedulerException("任务定时器类型不能改变！");
                }
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpress);
                trigger = ((CronTrigger)trigger).getTriggerBuilder()
                    .withIdentity(triggerKey)
                    .usingJobData(new JobDataMap(param))
                    .withSchedule(scheduleBuilder).build();
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        }
    }
    /**
     * 定时间隔任务
     * @param scheduler 主API
     * @param jobName 和 triggerName 一致
     * @param jobGroup 和 triggerGroupName 一致
     * @param jobType 任务类别
     * @param intervalInSeconds 定时器描述
     * @param param 数据
     * @throws SchedulerException 执行异常
     */
    public static void createOrReplaceSimpleJob(Scheduler scheduler,
                                                String jobName, String jobGroup,
                                                String jobType, int intervalInSeconds,
                                                Map<String, Object> param) throws SchedulerException {
        createOrReplaceSimpleJob(scheduler, jobName, jobGroup,
            jobTypeMap.get(jobType), intervalInSeconds, param);
    }
    /**
     * 定时间隔任务
     * @param scheduler 主API
     * @param jobName 和 triggerName 一致
     * @param jobGroup 和 triggerGroupName 一致
     * @param jobClass 任务类
     * @param intervalInSeconds 定时器描述
     * @param param 数据
     * @throws SchedulerException 执行异常
     */
    public static void createOrReplaceSimpleJob(Scheduler scheduler,
                                                String jobName, String jobGroup,
                                                Class<? extends Job> jobClass, int intervalInSeconds,
                                                Map<String, Object> param) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        Trigger trigger = scheduler.getTrigger(triggerKey);

        // 新建一个任务
        if(trigger == null){
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .usingJobData(new JobDataMap(param))
                .build();

            trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().
                    withIntervalInSeconds(intervalInSeconds).repeatForever()).build();
            scheduler.scheduleJob(jobDetail, trigger);
        }else {
            if (!(trigger instanceof SimpleTrigger)) {
                throw new SchedulerException("任务定时器类型不能改变！");
            }

            trigger = ((SimpleTrigger)trigger).getTriggerBuilder()
                .withIdentity(triggerKey)
                .usingJobData(new JobDataMap(param))
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().
                    withIntervalInSeconds(intervalInSeconds).repeatForever()).build();
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }
    /**
     * 删除定时任务
     * @param scheduler 主API
     * @param jobName 和 triggerName 一致
     * @param jobGroup 和 triggerGroupName 一致
     * @throws SchedulerException 执行异常
     */
    public static void deleteJob(Scheduler scheduler,
                                 String jobName, String jobGroup) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName,jobGroup);
        // 停止触发器
        scheduler.pauseTrigger(triggerKey);
        // 移除触发器
        scheduler.unscheduleJob(triggerKey);
        // 删除任务
        scheduler.deleteJob(JobKey.jobKey(jobName,jobGroup));
    }
}
