# 远程 MCP 实现指南

## 架构说明

本项目实现了基于 HTTP REST API 的远程 MCP (Model Context Protocol) 架构：

```
┌─────────────────────┐         HTTP REST API          ┌──────────────────┐
│  langchain4j-demo   │  ─────────────────────────────> │   mcp-server     │
│      (Client)       │                                 │    (Server)      │
│   Port: 8080        │  <───────────────────────────── │   Port: 8081     │
└─────────────────────┘                                 └──────────────────┘
        │                                                        │
        ├─ RemoteMcpClient                                     ├─ McpController
        ├─ RemoteMcpToolAdapter                               ├─ McpToolService
        └─ McpServiceImpl                                     └─ Tools (Weather, Calculator)
```

## 核心组件

### MCP Server (端口 8081)

**提供的 REST API 端点:**

1. **健康检查**
   ```
   GET /mcp/health
   ```
   返回: `{"status":"UP","service":"mcp-server","version":"1.0.0"}`

2. **列出工具**
   ```
   GET /mcp/tools
   ```
   返回工具列表，包含名称、描述、参数等信息

3. **执行工具**
   ```
   POST /mcp/execute
   Content-Type: application/json

   {
     "toolName": "add",
     "arguments": {
       "a": 12,
       "b": 34
     }
   }
   ```
   返回: `{"success":true,"result":"46.0"}`

**核心代码:**
- `McpController.java` - REST API 控制器
- `McpToolService.java` - 工具管理和执行服务
- `WeatherTool.java` - 天气查询工具
- `CalculatorTool.java` - 计算器工具

### LangChain4j Demo Core (端口 8080)

**客户端组件:**

1. **RemoteMcpClient** - HTTP 客户端，负责调用远程 MCP Server
   - `checkHealth()` - 健康检查
   - `listTools()` - 列出工具
   - `executeTool(toolName, arguments)` - 执行工具

2. **RemoteMcpToolAdapter** - 工具适配器
   - 将远程工具封装为带 `@Tool` 注解的本地方法
   - LangChain4j 的 AI Agent 可以自动发现和调用这些工具

3. **McpServiceImpl** - MCP 服务实现
   - 使用 `RemoteMcpToolAdapter` 构建 AI Agent
   - AI 自动决策何时调用哪个远程工具

## 启动步骤

### 1. 启动 MCP Server

```bash
cd mcp-server
mvn spring-boot:run
```

服务启动在 `http://localhost:8081`

验证启动成功:
```bash
curl http://localhost:8081/mcp/health
```

### 2. 启动 Demo Core

```bash
cd langchain4j-demo-core
mvn spring-boot:run
```

服务启动在 `http://localhost:8080`

### 3. 运行测试

```bash
./test-remote-mcp.sh
```

## 测试示例

### 直接调用 MCP Server

**测试计算器工具:**
```bash
curl -X POST http://localhost:8081/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "add",
    "arguments": {"a": 12, "b": 34}
  }'
```

**测试天气工具:**
```bash
curl -X POST http://localhost:8081/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "getWeather",
    "arguments": {"city": "北京"}
  }'
```

### 通过 AI Agent 调用远程工具

**测试天气查询:**
```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "北京今天天气怎么样？"
  }'
```

AI 会自动：
1. 识别用户意图（查询天气）
2. 调用远程 `getWeather` 工具
3. 将结果整合到自然语言回复中

**测试计算功能:**
```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "帮我计算 123 + 456"
  }'
```

AI 会自动：
1. 识别计算需求
2. 调用远程 `add` 工具
3. 返回计算结果和解释

## 配置说明

### MCP Server 配置 (mcp-server/src/main/resources/application.yml)

```yaml
server:
  port: 8081  # MCP Server 端口

spring:
  application:
    name: mcp-server
```

### Demo Core 配置 (langchain4j-demo-core/src/main/resources/application.yml)

```yaml
mcp:
  server:
    url: http://localhost:8081  # 远程 MCP Server 地址
```

## 添加新工具

### 1. 在 MCP Server 中添加工具类

```java
@Component
public class MyNewTool {

    public String myMethod(String param) {
        // 工具逻辑
        return "result";
    }
}
```

### 2. 在 McpToolService 中注册工具

```java
@RequiredArgsConstructor
public class McpToolService {
    private final MyNewTool myNewTool;

    private List<Object> getTools() {
        return Arrays.asList(weatherTool, calculatorTool, myNewTool);
    }
}
```

### 3. 在 RemoteMcpToolAdapter 中添加适配方法

```java
@Tool("Description of my new tool")
public String myMethod(String param) {
    Map<String, Object> args = new HashMap<>();
    args.put("param", param);
    return remoteMcpClient.executeTool("myMethod", args);
}
```

完成！AI Agent 现在可以自动发现并调用新工具。

## 协议设计

### 请求格式

```json
{
  "toolName": "工具方法名",
  "arguments": {
    "参数名1": "值1",
    "参数名2": "值2"
  }
}
```

### 响应格式

**成功:**
```json
{
  "success": true,
  "result": "执行结果"
}
```

**失败:**
```json
{
  "success": false,
  "error": "错误信息"
}
```

## 优势

1. **简单直观** - 使用标准的 HTTP REST API，易于理解和调试
2. **跨语言支持** - 任何支持 HTTP 的语言都可以作为客户端
3. **易于扩展** - 添加新工具只需要在 Server 端实现，Client 端自动适配
4. **独立部署** - MCP Server 可以独立部署和扩展
5. **AI 自动集成** - LangChain4j 的 AI Agent 可以自动发现和调用远程工具

## 故障排查

### MCP Server 无法启动
- 检查端口 8081 是否被占用
- 查看日志: `mcp-server/logs/`

### Demo Core 无法连接 MCP Server
- 确认 MCP Server 已启动
- 测试健康检查: `curl http://localhost:8081/mcp/health`
- 检查配置文件中的 `mcp.server.url`

### AI 不调用工具
- 检查工具描述是否清晰（`@Tool` 注解的描述）
- 尝试更明确的问题，如"请使用计算器计算..."
- 查看日志中的工具调用信息

## 性能优化建议

1. **连接池** - 配置 RestTemplate 的连接池大小
2. **超时设置** - 根据工具执行时间调整超时配置
3. **缓存** - 对于不变的工具列表可以添加缓存
4. **异步调用** - 对于耗时工具可以改为异步执行

## 下一步

- [ ] 添加工具执行结果缓存
- [ ] 支持流式响应
- [ ] 添加工具调用监控和统计
- [ ] 实现工具权限控制
- [ ] 支持批量工具调用
