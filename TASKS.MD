# 工作总体描述
项目包括一下模块：

1. centit-utils: 工具类库
2. centit-es-client: ES客户端封装
3. centit-office-utils: Office文档处理工具类库
4. centit-report-utils: 报表工具类库
5. centit-quartz-extend: Quartz扩展

这些模块中新建了很多常用的算法、工具类供公司其他项目使用，统一技术选项和避免重复开发。
现在要分析项目中这些模块中的源代码，编写一个工AI工具（agents）使用的描述文件。
文件中包括各个模块中包含的字模块和具体的方法， 每个模块一个文案放在项目的agent-prompt目录下。
由于这些模块内容比较多，文件的可能会比较大，所以还希望建立一个总体摘要文件供agent分步加载

# 工作输出
每个模块 一个文件，如果模块的内容比较多，比如centit-utils，也可以建立子模块对应的文件
一个总体的摘要描述文件
文件都放在项目的agent-prompt目录下
