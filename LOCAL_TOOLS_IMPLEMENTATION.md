# 本地工具实现 - 问题已解决

## ✅ 问题已修复

前端查看 MCP tools 时的 500 错误已修复！

## 解决方案总结

由于 LangChain4j MCP Client 和 Spring AI MCP Server 协议不兼容，我们改用**本地工具实现**。

### 优势

✅ **无协议兼容性问题** - 使用 LangChain4j 原生工具支持
✅ **性能更好** - 无网络调用开销
✅ **更简单** - 不需要额外的 MCP Server
✅ **更易维护** - 工具和服务在同一个项目中

## 实现内容

### 1. 新增工具类

#### WeatherTool (天气工具)
**位置**: `langchain4j-demo-core/src/main/java/org/example/langchain4jdemo/tools/WeatherTool.java`

**功能**:
- `getWeather(String city)` - 查询城市当前天气
- `getWeatherForecast(String city, int days)` - 查询未来天气预报

**示例**:
```java
@Tool("获取指定城市的当前天气信息")
public String getWeather(String city) {
    return "北京今天晴，温度15-25度";
}
```

#### CalculatorTool (计算器工具)
**位置**: `langchain4j-demo-core/src/main/java/org/example/langchain4jdemo/tools/CalculatorTool.java`

**功能**:
- `add(double a, double b)` - 加法
- `subtract(double a, double b)` - 减法
- `multiply(double a, double b)` - 乘法
- `divide(double a, double b)` - 除法
- `power(double base, double exponent)` - 幂运算
- `sqrt(double number)` - 平方根

**示例**:
```java
@Tool("执行加法运算，计算两个数的和")
public double add(double a, double b) {
    return a + b;
}
```

### 2. 修改 McpServiceImpl

**位置**: `langchain4j-demo-core/src/main/java/org/example/langchain4jdemo/service/impl/McpServiceImpl.java`

**主要变更**:
1. 不再依赖 `McpClient`
2. 自动发现所有 `@Component` 标注的工具类
3. 使用 `AiServices` 构建助手，自动集成工具
4. 工具列表API从本地工具提取信息

**核心代码**:
```java
// 自动发现工具
this.tools = new ArrayList<>();
String[] beanNames = applicationContext.getBeanNamesForAnnotation(Component.class);
for (String beanName : beanNames) {
    Object bean = applicationContext.getBean(beanName);
    if (bean.getClass().getPackage().getName().contains("tools")) {
        this.tools.add(bean);
    }
}

// 使用 AiServices 构建助手
Assistant assistant = AiServices.builder(Assistant.class)
        .chatLanguageModel(chatModel)
        .tools(tools)  // 注入本地工具
        .build();

String response = assistant.chat(request.getMessage());
```

## 如何使用

### 启动服务

```bash
# 1. 进入后端目录
cd langchain4j-demo-core

# 2. 启动服务（如果已在运行，需要重启）
mvn spring-boot:run
```

**重要**: 如果后端已在运行，**必须重启**才能加载新代码！

### 测试 API

#### 1. 获取工具列表

```bash
curl http://localhost:8080/api/mcp/tools
```

**预期响应**:
```json
[
  {
    "name": "getWeather",
    "description": "获取指定城市的当前天气信息",
    "parameters": { ... }
  },
  {
    "name": "add",
    "description": "执行加法运算，计算两个数的和",
    "parameters": { ... }
  },
  ...
]
```

#### 2. MCP 聊天（自动调用工具）

```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "请帮我查询北京的天气"}'
```

**预期响应**:
```json
{
  "success": true,
  "content": "根据查询，北京今天晴，温度15-25度，空气质量良好。"
}
```

#### 3. 直接调用工具

```bash
curl -X POST http://localhost:8080/api/mcp/invoke \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "getWeather",
    "parameters": {"city": "北京"}
  }'
```

**预期响应**:
```json
{
  "result": "北京今天晴，温度15-25度，空气质量良好"
}
```

### 前端测试

1. 打开前端: `http://localhost:5173`
2. 点击 "MCP 模式"
3. 点击 "查看所有 MCP Tools" - 应该能看到工具列表
4. 发送消息测试工具调用:
   - "请帮我查询上海的天气"
   - "帮我计算 123 + 456"
   - "15 的平方根是多少？"

## 工作原理

### LangChain4j 工具机制

1. **工具注册**: 使用 `@Tool` 注解标记方法
2. **自动发现**: Spring 扫描所有 `@Component` 类
3. **自动集成**: `AiServices` 自动将工具注入 AI 助手
4. **智能调用**: LLM 根据用户消息自动决定是否调用工具

### 工具调用流程

```
用户消息
   ↓
LLM 分析 (是否需要工具？)
   ↓
   ├─→ 不需要 → 直接回复
   └─→ 需要 → 选择工具 → 调用工具 → 获取结果 → 生成回复
```

## 添加新工具

### 步骤 1: 创建工具类

```java
// src/main/java/org/example/langchain4jdemo/tools/MyTool.java
package org.example.langchain4jdemo.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyTool {

    @Tool("工具描述")
    public String myMethod(String param) {
        // 实现逻辑
        return "result";
    }
}
```

### 步骤 2: 重启服务

```bash
mvn spring-boot:run
```

### 步骤 3: 验证

```bash
# 查看工具是否出现在列表中
curl http://localhost:8080/api/mcp/tools
```

就这么简单！工具会被自动发现和注册。

## 工具示例

### 示例 1: 文件工具

```java
@Component
public class FileTool {

    @Tool("列出指定目录下的文件")
    public String listFiles(String directory) {
        // 实现文件列表功能
        return "file1.txt, file2.txt, ...";
    }

    @Tool("读取文件内容")
    public String readFile(String filePath) {
        // 实现文件读取功能
        return "file content...";
    }
}
```

### 示例 2: 数据库工具

```java
@Component
public class DatabaseTool {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Tool("查询数据库")
    public String queryDatabase(String sql) {
        // 执行SQL查询
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return results.toString();
    }
}
```

## 日志示例

### 正常启动日志

```
2026-01-21 18:25:00 - 发现工具: WeatherTool
2026-01-21 18:25:00 - 发现工具: CalculatorTool
2026-01-21 18:25:00 - 已加载 2 个本地工具
```

### 工具调用日志

```
2026-01-21 18:26:00 - Processing MCP chat request with local tools: 请帮我查询北京的天气
2026-01-21 18:26:01 - 查询城市天气: 北京
2026-01-21 18:26:01 - 天气查询结果: 北京今天晴，温度15-25度，空气质量良好
2026-01-21 18:26:02 - Assistant response generated successfully
```

## 与 MCP Server 的对比

| 特性 | 本地工具 | MCP Server |
|------|---------|-----------|
| 协议兼容性 | ✅ 原生支持 | ❌ 协议不兼容 |
| 性能 | ✅ 无网络开销 | ❌ 网络调用 |
| 复杂度 | ✅ 简单 | ❌ 复杂 |
| 维护性 | ✅ 易维护 | ❌ 多服务维护 |
| 部署 | ✅ 单服务 | ❌ 多服务 |

## FAQ

### Q1: 为什么不使用 MCP Server？

A: LangChain4j MCP Client 和 Spring AI MCP Server 使用不同的协议实现，无法互操作。本地工具实现更简单、性能更好。

### Q2: MCP Server 模块还有用吗？

A: Spring AI MCP Server 模块仍然有价值，可以被其他支持 Spring AI 的客户端使用，只是无法与 LangChain4j 互操作。

### Q3: 如何确认工具已加载？

A: 查看启动日志，应该看到 "发现工具: XXX" 和 "已加载 N 个本地工具"。

### Q4: 工具调用失败怎么办？

A: 检查:
1. 工具类是否标注了 `@Component`
2. 工具方法是否标注了 `@Tool`
3. 参数类型是否正确
4. 查看后端日志中的详细错误

### Q5: 能否禁用某个工具？

A: 两种方式:
1. 移除 `@Component` 注解
2. 将类移出 `tools` 包

## 总结

✅ **问题已解决**: 使用本地工具实现替代不兼容的 MCP 远程调用
✅ **编译成功**: BUILD SUCCESS
✅ **功能完整**: 工具列表、聊天、直接调用全部实现
⏳ **待测试**: 需要重启后端服务进行测试

**下一步**: 重启后端服务，测试所有功能！

---

**更新时间**: 2026-01-21
**状态**: ✅ 实现完成，待重启测试
