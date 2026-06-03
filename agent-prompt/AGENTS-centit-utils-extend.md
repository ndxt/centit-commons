# centit-utils / extend 子包

> 包路径: `com.centit.support.extend`
> 运行时上下文扩展。

---

## AbstractRuntimeContext

脚本运行时上下文抽象基类。

---

## JSRuntimeContext

JavaScript 运行时上下文（基于 Nashorn/GraalJS）。提供脚本执行环境。

---

## PythonRuntimeContext

Python 运行时上下文。提供 Python 脚本执行环境。

---

## CallSystemProcess

系统进程调用工具。

| 方法 | 描述 |
|------|------|
| 通过 `Runtime.getRuntime().exec()` | 调用操作系统命令/进程 |
