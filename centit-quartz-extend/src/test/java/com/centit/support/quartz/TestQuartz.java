package com.centit.support.quartz;

import com.centit.support.algorithm.CollectionsOpt;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.util.concurrent.TimeUnit;


public class TestQuartz {

    @Test
    public void testQuartz() throws SchedulerException, InterruptedException {

        /** 创建 schedulerFactory
         *  在Spring 框架中无需手动创建引入 对应的配置文件就可以，并通过下面代码获取
         *  WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
         *         SchedulerFactoryBean schedulerFactoryBean = webApplicationContext.getBean(SchedulerFactoryBean.class);
         *         Scheduler scheduler = schedulerFactoryBean.getScheduler()
          */
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        /**
         * 注册 job的类型，可以自行扩展 job类别
         * 类型 必须 扩展AbstractQuartzJob 类， 在这个类中完成了 日志的记录工作，
         * 日志记录工作也是可以在子类中覆盖的
         * 目前一共设置了四个类型的job
         * 1, PrintMessageJob 这个仅用来测试打印消息
         * 2, CallProcessJob 调用系统程序
         * 3, JavaBeanJob 调用bean的方法
         * 4, HttpRquestJob 调用一个web 请求
         */
        QuartzJobUtils.registerJobType("msg", PrintMessageJob.class);
        //QuartzJobUtils.registerJobType("httpRequest", HttpRquestJob.class);
        QuartzJobUtils.registerJobType("exec", CallProcessJob.class);

        /**
         * 下面的代码 是 添加和变更定时器任务的
         * 正式使用中定时器任务应该从数据库中加载
         */
        QuartzJobUtils.createOrReplaceSimpleJob(scheduler, "print",
            "test", "msg", 1,
            CollectionsOpt.createHashMap("message", "hello quartz"));

        QuartzJobUtils.createOrReplaceSimpleJob(scheduler, "app",
            "test", "exec", 6,
            CollectionsOpt.createHashMap("command", "java -jar /D/Projects/execute/TestApplication.jar",
                "envp", CollectionsOpt.createHashMap("say","hello","to","app")));

        System.out.println("--------scheduler start ! ------------");
        scheduler.start();
        TimeUnit.SECONDS.sleep(10);
        QuartzJobUtils.createOrReplaceSimpleJob(scheduler, "print",
            "test", "msg", 8,
            CollectionsOpt.createHashMap("message", "Hellow quartz!"));
        TimeUnit.SECONDS.sleep(40);
        scheduler.shutdown();
        System.out.println("--------scheduler shutdown ! ------------");
    }
}
