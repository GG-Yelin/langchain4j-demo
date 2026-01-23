# 快速开始 - 远程 MCP

## 5 分钟快速体验

### 步骤 1: 编译项目

```bash
mvn clean package -DskipTests
```

### 步骤 2: 启动 MCP Server（终端 1）

```bash
./start-mcp-server.sh
```

等待看到：
```
Started McpServerApplication in X.XXX seconds
```

### 步骤 3: 启动 Demo Core（终端 2）

```bash
./start-demo-core.sh
```

等待看到：
```
Started LangChain4jDemoApplication in X.XXX seconds
```

### 步骤 4: 测试（终端 3）

**测试 1 - 健康检查:**
```bash
curl http://localhost:8081/mcp/health
```

期望输出:
```json
{"status":"UP","service":"mcp-server","version":"1.0.0"}
```

**测试 2 - 列出工具:**
```bash
curl http://localhost:8081/mcp/tools | jq
```

期望输出: 工具列表（包括 getWeather, add, subtract 等）

**测试 3 - 直接调用工具 (计算器):**
```bash
curl -X POST http://localhost:8081/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{"toolName":"add","arguments":{"a":12,"b":34}}'
```

期望输出:
```json
{"success":true,"result":"46.0"}
```

**测试 4 - AI 自动调用工具 (天气查询):**
```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"北京今天天气怎么样？"}'
```

期望输出: AI 会自动调用 `getWeather` 工具并整合结果返回

**测试 5 - AI 自动调用工具 (计算):**
```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"帮我计算 123 + 456"}'
```

期望输出: AI 会自动调用 `add` 工具并返回计算结果

### 步骤 5: 运行完整测试套件

```bash
./test-remote-mcp.sh
```

这会自动运行所有测试并显示结果。

## 测试效果

### 直接调用工具
```bash
$ curl -X POST http://localhost:8081/mcp/execute \
    -H "Content-Type: application/json" \
    -d '{"toolName":"getWeather","arguments":{"city":"北京"}}'

{"success":true,"result":"北京今天晴，温度15-25度，空气质量良好"}
```

### AI 自动调用工具
```bash
$ curl -X POST http://localhost:8080/api/mcp/chat \
    -H "Content-Type: application/json" \
    -d '{"message":"上海和深圳今天天气怎么样，对比一下"}'

{
  "success": true,
  "content": "根据查询结果：\n\n上海今天多云，温度18-26度，湿度较大。\n深圳今天阴，温度22-28度，可能有小雨。\n\n对比来看，深圳温度相对更高一些，但可能会下雨。上海虽然多云但天气相对稳定。建议根据具体出行计划选择合适的城市。",
  "toolsUsed": ["getWeather", "getWeather"]
}
```

## 架构说明

```
┌──────────────────┐    HTTP REST API     ┌─────────────┐
│ langchain4j-demo │ ──────────────────→  │ mcp-server  │
│   (Port 8080)    │                       │ (Port 8081) │
│                  │ ←──────────────────   │             │
│  - AI Agent      │                       │ - Tools     │
│  - RemoteClient  │                       │ - Service   │
└──────────────────┘                       └─────────────┘
```

**工作流程:**
1. 用户发送问题到 langchain4j-demo
2. AI Agent 分析问题，决定调用哪个工具
3. RemoteMcpToolAdapter 调用 RemoteMcpClient
4. RemoteMcpClient 通过 HTTP POST 调用 mcp-server
5. mcp-server 执行工具并返回结果
6. AI Agent 整合结果，生成自然语言回复

## 支持的工具

### 天气工具
- `getWeather(city)` - 查询指定城市的天气
- `getWeatherForecast(city, days)` - 查询未来几天的天气预报

### 计算器工具
- `add(a, b)` - 加法
- `subtract(a, b)` - 减法
- `multiply(a, b)` - 乘法
- `divide(a, b)` - 除法
- `power(base, exponent)` - 幂运算
- `sqrt(number)` - 平方根

## 端口说明

- **8080** - langchain4j-demo-core (AI Agent + Web API)
- **8081** - mcp-server (工具服务器)

## 故障排查

### 端口被占用
```bash
# 检查端口占用
lsof -i :8080
lsof -i :8081

# 杀死进程
kill -9 <PID>
```

### MCP Server 无法启动
- 检查 Java 版本（需要 Java 17+）
- 检查 Maven 配置
- 查看日志文件

### Demo Core 连接不上 MCP Server
- 确认 MCP Server 已启动
- 测试健康检查: `curl http://localhost:8081/mcp/health`
- 检查配置文件中的 `mcp.server.url`

### AI 不调用工具
- 确保问题明确（如"北京天气"比"天气"更明确）
- 检查日志，看是否有错误信息
- 尝试更明确的指令，如"请使用天气工具查询北京天气"

## 更多信息

- **详细使用指南**: [REMOTE_MCP_GUIDE.md](REMOTE_MCP_GUIDE.md)
- **实现总结**: [REMOTE_MCP_IMPLEMENTATION.md](REMOTE_MCP_IMPLEMENTATION.md)

## 下一步

现在你已经成功运行了远程 MCP 系统！可以尝试：

1. 添加自己的工具
2. 修改工具实现
3. 集成到自己的项目中
4. 部署到生产环境

祝你使用愉快！🎉
