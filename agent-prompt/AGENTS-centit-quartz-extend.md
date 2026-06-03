# centit-quartz-extend 模块

> Maven 坐标: `com.centit.support:centit-quartz-extend:JDK21-SNAPSHOT`
> 根包: `com.centit.support.quartz`
> 基于 Quartz 的定时任务扩展模块。

---

## 架构总览

```
com.centit.support.quartz
├── AbstractQuartzJob.java     -- 任务抽象基类（模板方法）
├── QuartzJobUtils.java        -- 任务管理工具（注册/创建/删除）
├── PrintMessageJob.java       -- 打印消息任务（测试用）
├── CallProcessJob.java        -- 系统进程调用任务
└── JavaBeanJob.java           -- Spring Bean 方法调用任务
```

### 类继承关系

```
Job (Quartz接口)
 └── AbstractQuartzJob (抽象基类)
      ├── PrintMessageJob   (打印消息)
      ├── CallProcessJob    (调用系统进程)
      └── JavaBeanJob       (调用Spring Bean方法)

QuartzJobUtils (独立工具类，静态方法)
```

---

## 1. AbstractQuartzJob — 任务抽象基类

所有自定义 Quartz Job 的抽象基类（模板方法模式）。统一了日志记录和异常处理流程。

### 生命周期

```
execute()
  ├── 1. loadExecutionContext()  -- 加载参数（抽象，子类实现）
  ├── 2. beforeRun()             -- 前置日志（可覆盖）
  ├── 3. runRealJob()            -- 执行任务（抽象，子类实现）
  └── 4. onSuccess() 或 onError()  -- 后置回调（可覆盖）
```

### 抽象方法（子类必须实现）

| 方法 | 描述 |
|------|------|
| `protected abstract boolean runRealJob(JobExecutionContext context)` | 执行实际任务逻辑。返回 true=成功, false=失败 |
| `protected abstract void loadExecutionContext(JobExecutionContext context)` | 从 MergedJobDataMap 提取参数 |

### 可覆盖的钩子方法

| 方法 | 描述 |
|------|------|
| `protected void beforeRun(JobExecutionContext context)` | 任务执行前回调（默认记录 debug 日志） |
| `protected void onSuccess(JobExecutionContext context)` | 成功后回调（默认记录 debug 日志） |
| `protected void onError(JobExecutionContext context)` | 失败后回调（默认记录 error 日志 + JobDataMap JSON） |

### 公共方法

| 方法 | 描述 |
|------|------|
| `public void execute(JobExecutionContext context)` | Quartz Job 接口实现，模板方法入口 |

---

## 2. QuartzJobUtils — 任务管理工具

定时任务管理工具（抽象类），静态方法。

### 注册任务类型

| 方法 | 描述 |
|------|------|
| `static void registerJobType(String jobType, Class<? extends AbstractQuartzJob> type)` | 注册自定义任务类型 |

**内置注册类型**:
- `"PrintMessage"` -> `PrintMessageJob`
- `"CallProcess"` -> `CallProcessJob`
- `"JavaBean"` -> `JavaBeanJob`

### 创建 Cron 定时任务

| 方法 | 描述 |
|------|------|
| `static void createOrReplaceCronJob(Scheduler scheduler, String jobName, String jobGroup, String jobType, String cronExpress, Map<String, Object> param)` | 通过类型名创建 Cron 任务 |
| `static void createOrReplaceCronJob(Scheduler scheduler, String jobName, String jobGroup, Class<? extends Job> jobClass, String cronExpress, Map<String, Object> param)` | 通过 Job 类创建 Cron 任务 |

**行为**: 触发器不存在则新建；已存在则更新 Cron 表达式和参数。自动验证 Cron 表达式合法性。

### 创建简单间隔任务

| 方法 | 描述 |
|------|------|
| `static void createOrReplaceSimpleJob(Scheduler scheduler, String jobName, String jobGroup, String jobType, int intervalInSeconds, Map<String, Object> param)` | 通过类型名创建间隔任务 |
| `static void createOrReplaceSimpleJob(Scheduler scheduler, String jobName, String jobGroup, Class<? extends Job> jobClass, int intervalInSeconds, Map<String, Object> param)` | 通过 Job 类创建间隔任务 |

**行为**: 固定秒数间隔，立即开始，无限重复。

### 删除任务

| 方法 | 描述 |
|------|------|
| `static void deleteJob(Scheduler scheduler, String jobName, String jobGroup)` | 删除任务（暂停触发器 -> 移除触发器 -> 删除任务） |

### 设计约定

一个 `jobDetail` 只对应一个 `trigger`，因此 `triggerName` = `jobName`，`triggerGroupName` = `jobGroupName`。

---

## 3. PrintMessageJob — 打印消息任务

简单的测试任务，从参数获取 `message` 并打印到 stdout。

| JobDataMap 参数 | 类型 | 描述 |
|-----------------|------|------|
| `message` | String | 要打印的消息内容 |

---

## 4. CallProcessJob — 系统进程调用任务

通过 `Runtime.getRuntime().exec()` 调用操作系统命令。

| JobDataMap 参数 | 类型 | 描述 |
|-----------------|------|------|
| `command` | String | 要执行的命令（必填） |
| `envp` | Map<String,Object> | 环境变量键值对（可选） |
| `dir` | String | 工作目录路径（可选） |

Setter 方法（编程式配置）:
- `setCommand(String)`
- `setEnvp(Map<String, Object>)`
- `setDir(String)`

---

## 5. JavaBeanJob — Spring Bean 方法调用

通过反射调用 Spring 容器中 Bean 的指定方法。使用 `ReflectionOpt.getMatchBestMethod()` 进行方法最佳匹配。

| JobDataMap 参数 | 类型 | 描述 |
|-----------------|------|------|
| `bean` | Object | Bean 实例（与 beanName 二选一） |
| `beanName` | String | Spring Bean 名称 |
| `methodName` | String | 要调用的方法名（必填） |
| `params` | Map<String,Object> | 方法参数（可选） |

Setter 方法（编程式配置）:
- `setBean(Object)`
- `setBeanName(String)`
- `setMethodName(String)`
- `setParams(Map<String, Object>)`

**Bean 查找逻辑**: 优先使用 `bean` 参数；为 null 时通过 `ContextLoader.getCurrentWebApplicationContext().getBean(beanName)` 查找。

---

## 典型使用示例

```java
// 1. 注册自定义任务类型
QuartzJobUtils.registerJobType("MyJob", MyCustomJob.class);

// 2. 创建 Cron 定时任务
Map<String, Object> params = new HashMap<>();
params.put("message", "Hello World");
QuartzJobUtils.createOrReplaceCronJob(scheduler, "job1", "group1",
    "PrintMessage", "0 0/5 * * * ?", params);

// 3. 创建间隔任务
params.put("command", "ls -la");
QuartzJobUtils.createOrReplaceSimpleJob(scheduler, "job2", "group2",
    "CallProcess", 60, params);

// 4. 创建 Bean 方法调用任务
params.clear();
params.put("beanName", "myService");
params.put("methodName", "processData");
QuartzJobUtils.createOrReplaceCronJob(scheduler, "job3", "group3",
    "JavaBean", "0 0 2 * * ?", params);

// 5. 删除任务
QuartzJobUtils.deleteJob(scheduler, "job1", "group1");
```
