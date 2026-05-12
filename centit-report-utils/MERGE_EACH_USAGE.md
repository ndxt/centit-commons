# JXLS 自定义命令使用说明

本文档介绍 centit-commons 项目中提供的两个 JXLS 自定义命令：`MergeEachCommand` 和 `AutoRowHeightCommand`。

---

# 一、MergeEachCommand - 单元格合并命令

## 功能说明

`MergeEachCommand` 是一个自定义的 JXLS 命令，用于在循环遍历数据时自动合并相同值的单元格。

## 使用方法

在 Excel 模板的单元格批注中使用：

```
jx:mergeEach(items="list" var="item" cols="0,1,2")
```

### 参数说明

- **items**: 必填，要遍历的集合变量名
- **var**: 必填，每次迭代的变量名
- **cols**: 可选，需要合并的列索引（从0开始），多个列用逗号分隔

### 示例

#### 示例 1：合并单列

假设有一个部门列表，需要合并部门名称列：

```
jx:mergeEach(items="departments" var="dept" cols="0")
```

#### 示例 2：合并多列

如果需要同时合并部门名称和地区列：

```
jx:mergeEach(items="employees" var="emp" cols="0,1")
```

#### 示例 3：不指定 cols 参数

如果不指定 cols 参数，则不会进行任何合并操作，等同于普通的 each 命令：

```
jx:mergeEach(items="list" var="item")
```

## 工作原理

1. 遍历集合中的每个元素
2. 应用模板到每一行
3. 对于指定的列，检查当前行和上一行的值是否相同
4. 如果值相同且连续，则合并这些单元格
5. 如果已经存在合并区域，则扩展该区域

## 注意事项

1. **数据排序**：使用前请确保数据已按照需要合并的字段排序，否则只会合并连续的相同值
   
2. **列索引**：cols 参数中的列索引是相对于模板起始列的偏移量，从 0 开始
   
3. **合并条件**：只有当相邻行的值完全相同时才会合并
   
4. **性能考虑**：大量数据时使用合并功能可能会影响性能

## 完整示例

### 数据模型

```java
Map<String, Object> model = new HashMap<>();
List<Map<String, Object>> employees = new ArrayList<>();

// 添加员工数据（已按部门排序）
Map<String, Object> emp1 = new HashMap<>();
emp1.put("department", "技术部");
emp1.put("name", "张三");
emp1.put("position", "工程师");
employees.add(emp1);

Map<String, Object> emp2 = new HashMap<>();
emp2.put("department", "技术部");
emp2.put("name", "李四");
emp2.put("position", "高级工程师");
employees.add(emp2);

Map<String, Object> emp3 = new HashMap<>();
emp3.put("department", "市场部");
emp3.put("name", "王五");
emp3.put("position", "经理");
employees.add(emp3);

model.put("employees", employees);
```

### Excel 模板批注

在 A2 单元格添加批注：
```
jx:mergeEach(items="employees" var="emp" cols="0")
```

模板内容：
- A2: `${emp.department}` （部门列，会被合并）
- B2: `${emp.name}` （姓名列，不合并）
- C2: `${emp.position}` （职位列，不合并）

### 输出结果

生成的 Excel 中：
- A2:A3 单元格会合并（都是"技术部"）
- A4 单独显示"市场部"
- B 列和 C 列不会合并

## 与标准 each 命令的区别

| 特性 | jx:each | jx:mergeEach |
|------|---------|--------------|
| 基本循环 | ✓ | ✓ |
| 自动合并单元格 | ✗ | ✓ |
| 性能 | 较快 | 略慢 |
| 复杂度 | 简单 | 中等 |

## 常见问题

### Q: 为什么我的单元格没有合并？

A: 请检查：
1. 是否正确指定了 cols 参数
2. 数据是否已排序
3. 相邻行的值是否完全相同（包括空格等）

### Q: 可以合并非连续的相同值吗？

A: 不可以。MergeEachCommand 只合并连续的相同值。如果需要合并非连续的值，需要在数据处理阶段先对数据进行排序。

### Q: 合并后样式会保留吗？

A: 是的，合并时会保留第一个单元格的样式。

---

# 二、AutoRowHeightCommand - 自动行高命令

## 功能说明

`AutoRowHeightCommand` 是一个自定义的 JXLS 命令，用于自动调整行高以适应单元格内容。当单元格内容较多需要自动换行时，此命令可以确保行高足够显示所有内容。

## 使用方法

在 Excel 模板的单元格批注中使用：

```
jx:autoRowHeight(lastCell="C3")
```

### 参数说明

- **lastCell**: 必填，指定区域的最后一个单元格位置（如 "C3"、"D5" 等）

## 工作原理

1. 应用模板到指定区域
2. 获取该区域的所有行
3. 将每行的行高设置为自动调整（-1）
4. Excel 会根据单元格内容自动计算合适的行高

## 示例

### 示例 1：基本用法

假设有一个包含多行文本的区域 A2:C5，需要自动调整行高：

```
jx:autoRowHeight(lastCell="C5")
```

### 示例 2：与 mergeEach 结合使用

可以在同一个模板中同时使用合并单元格和自动行高：

```
jx:mergeEach(items="employees" var="emp" cols="0")
```

然后在适当的位置添加：

```
jx:autoRowHeight(lastCell="D10")
```

## 完整示例

### 数据模型

```java
Map<String, Object> model = new HashMap<>();
List<Map<String, Object>> employees = new ArrayList<>();

Map<String, Object> emp1 = new HashMap<>();
emp1.put("department", "技术部");
emp1.put("name", "张三");
emp1.put("description", "这是一段很长的描述文字，需要自动换行显示，以确保所有内容都能完整展示在单元格中。");
employees.add(emp1);

Map<String, Object> emp2 = new HashMap<>();
emp2.put("department", "技术部");
emp2.put("name", "李四");
emp2.put("description", "另一段较长的描述，同样需要自动调整行高来适应内容。");
employees.add(emp2);

model.put("employees", employees);
```

### Excel 模板设置

1. **在 A2 单元格添加批注**（循环命令）：
   ```
   jx:mergeEach(items="employees" var="emp" cols="0")
   ```

2. **在 D2 单元格添加批注**（自动行高命令）：
   ```
   jx:autoRowHeight(lastCell="D3")
   ```

3. **模板内容**：
   - A2: `${emp.department}` （部门列，会被合并）
   - B2: `${emp.name}` （姓名）
   - C2: `${emp.description}` （描述，需要自动换行）
   
4. **设置单元格格式**：
   - 选中 C 列单元格
   - 右键 → 设置单元格格式 → 对齐
   - 勾选"自动换行"

### 输出结果

生成的 Excel 中：
- A2:A3 单元格会合并（都是"技术部"）
- C 列的行高会自动调整以显示完整的描述文字
- 即使描述文字很长，也能完整显示而不会被截断

## 注意事项

1. **必须设置自动换行**：使用前需要在 Excel 模板中为相关单元格设置"自动换行"格式，否则自动行高不会生效

2. **lastCell 范围**：lastCell 指定的区域应该覆盖所有需要自动调整行高的单元格

3. **性能考虑**：大量数据时使用自动行高可能会略微影响生成速度

4. **与其他命令配合**：可以与 `mergeEach`、`each` 等其他 JXLS 命令配合使用

## 常见问题

### Q: 为什么设置了 autoRowHeight 但行高没有变化？

A: 请检查：
1. 是否在 Excel 模板中为单元格设置了"自动换行"格式
2. lastCell 参数是否正确指定了区域范围
3. 单元格内容是否确实需要多行显示

### Q: autoRowHeight 可以和 mergeEach 一起使用吗？

A: 可以。两者可以配合使用，mergeEach 负责合并单元格，autoRowHeight 负责调整行高。建议在模板的不同位置分别添加这两个命令的批注。

### Q: lastCell 参数应该如何确定？

A: lastCell 应该指向需要自动调整行高区域的右下角单元格。例如，如果数据从 A2 开始，有 3 列数据，预计最多 10 行，则可以设置为 "C11" 或更大的范围。

---

# 三、综合使用示例

## 场景描述

生成一个员工列表报表，要求：
1. 相同部门的单元格合并
2. 长文本自动换行并调整行高
3. 美观的表格布局

## 实现步骤

### 1. 准备数据

```java
Map<String, Object> model = new HashMap<>();
List<Map<String, Object>> employees = new ArrayList<>();

// 技术部员工
employees.add(createEmployee("技术部", "张三", "高级工程师\n负责系统架构设计\n技术指导"));
employees.add(createEmployee("技术部", "李四", "工程师\n负责后端开发"));
employees.add(createEmployee("技术部", "王五", "工程师\n负责前端开发\nUI设计"));

// 市场部员工
employees.add(createEmployee("市场部", "赵六", "市场经理\n负责市场推广\n客户关系维护"));
employees.add(createEmployee("市场部", "孙七", "市场专员\n负责活动策划"));

model.put("employees", employees);
```

### 2. 设置 Excel 模板

**单元格布局和批注：**

| 单元格 | 内容 | 批注 |
|--------|------|------|
| A1 | 部门 | （表头，无批注） |
| B1 | 姓名 | （表头，无批注） |
| C1 | 工作职责 | （表头，无批注） |
| A2 | `${emp.department}` | `jx:mergeEach(items="employees" var="emp" cols="0")` |
| B2 | `${emp.name}` | （无批注） |
| C2 | `${emp.duties}` | `jx:autoRowHeight(lastCell="C20")` |

**单元格格式设置：**
- 选中 C 列（工作职责列）
- 右键 → 设置单元格格式 → 对齐
- 勾选"自动换行"
- 设置垂直对齐为"居中"

### 3. 生成报表

```java
InputStream templateStream = getClass().getResourceAsStream("/templates/employee_report.xlsx");
OutputStream outputStream = new FileOutputStream("output/employee_report.xlsx");
ExcelReportUtil.exportExcel(templateStream, outputStream, model);
```

### 4. 输出效果

生成的 Excel 报表将具有：
- ✓ A 列中相同部门名称的单元格已合并
- ✓ C 列中长文本已自动换行
- ✓ 每行的行高根据内容自动调整
- ✓ 整体布局美观、易读

## 最佳实践建议

1. **先测试简单场景**：先单独测试每个命令，确认正常工作后再组合使用

2. **合理设置 lastCell**：lastCell 的范围应该略大于实际数据范围，留有余地

3. **数据预处理**：在使用 mergeEach 前，确保数据已按需要合并的字段排序

4. **样式统一**：在模板中预先设置好单元格样式，包括字体、边框、对齐方式等

5. **调试技巧**：如果遇到问题，可以先移除自动行高命令，确认基本循环正常后再添加

6. **文档注释**：在复杂的模板中添加注释，说明各个批注的作用，便于后续维护
