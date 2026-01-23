# MCP 功能快速开始指南

## 🎯 当前状态

✅ **MCP 功能已完整实现并编译成功！**

- 代码状态: ✅ 已实现
- 编译状态: ✅ BUILD SUCCESS
- 测试状态: ⏳ 等待 MCP Server 启动

---

## 🚀 快速启动步骤

### 步骤 1: 启动 MCP Server (必需)

MCP 功能依赖独立的 MCP Server，需要先启动它。

```bash
# 进入 MCP Server 目录
cd mcp-server

# 启动 MCP Server
mvn spring-boot:run
```

**验证 MCP Server**:
```bash
# 健康检查
curl http://localhost:8081/health

# 应该返回: {"status":"UP"}
```

---

### 步骤 2: 启动后端服务

```bash
# 进入后端目录
cd langchain4j-demo-core

# 启动服务
mvn spring-boot:run
```

**查看日志**，应该看到:
```
✅ MCP Client initialized successfully
✅ MCP Server health check passed
✅ Found X MCP tools
```

---

### 步骤 3: 测试 MCP 功能

#### 选项 A: 使用测试脚本 (推荐)

```bash
# 在项目根目录执行
./test-mcp.sh
```

#### 选项 B: 使用 curl 命令

```bash
# 1. 获取可用工具列表
curl http://localhost:8080/api/mcp/tools | jq '.'

# 2. MCP 聊天 (LLM 自动决定是否使用工具)
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "请帮我查询北京的天气"}' | jq '.'

# 3. 直接调用工具
curl -X POST http://localhost:8080/api/mcp/invoke \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "get_weather",
    "parameters": {"city": "北京"}
  }' | jq '.'
```

#### 选项 C: 使用前端界面

```bash
# 启动前端 (在另一个终端)
cd langchain4j-demo-frontend
npm run dev
```

1. 打开浏览器: `http://localhost:5173`
2. 点击 "MCP 模式"
3. 输入消息与 MCP 工具交互

---

## 📝 API 端点说明

### 1. GET /api/mcp/tools
获取 MCP Server 提供的所有工具

**响应示例**:
```json
[
  {
    "name": "get_weather",
    "description": "获取指定城市的天气信息",
    "parameters": {
      "type": "object",
      "properties": {
        "city": {"type": "string", "description": "城市名称"}
      }
    }
  }
]
```

### 2. POST /api/mcp/chat
与 LLM 聊天，LLM 自动决定是否调用 MCP 工具

**请求**:
```json
{
  "message": "请帮我查询北京的天气"
}
```

**响应**:
```json
{
  "success": true,
  "content": "根据天气查询工具的结果，北京今天晴，温度15-25度...",
  "toolsUsed": ["get_weather"]
}
```

### 3. POST /api/mcp/invoke
直接调用指定的 MCP 工具（跳过 LLM）

**请求**:
```json
{
  "toolName": "get_weather",
  "parameters": {
    "city": "北京"
  }
}
```

**响应**:
```json
{
  "result": "北京今天晴，温度15-25度"
}
```

---

## 🔧 配置说明

MCP 配置位于 `application.yml`:

```yaml
mcp:
  server:
    sse-url: http://localhost:8081/sse  # MCP Server 的 SSE 端点
  client:
    timeout: 60  # 超时时间(秒)
```

如果 MCP Server 运行在不同的端口，修改这里的配置。

---

## ⚠️ 常见问题

### Q1: 工具列表为空 / 返回 []

**原因**: MCP Server 未启动或未注册工具

**解决**:
```bash
# 1. 确认 MCP Server 正在运行
curl http://localhost:8081/health

# 2. 检查 MCP Server 日志，确认工具已注册
```

### Q2: 错误 "No MCP tools available"

**原因**: MCP Server 没有运行或连接失败

**解决**:
```bash
# 1. 启动 MCP Server
cd mcp-server && mvn spring-boot:run

# 2. 重启后端服务
cd langchain4j-demo-core && mvn spring-boot:run
```

### Q3: LLM 不使用工具

**原因**: LLM 认为不需要工具来回答问题

**解决**: 使用更明确的提示词，例如:
- ❌ "天气怎么样?" (模糊)
- ✅ "请帮我查询北京的天气" (明确)

或者直接使用 `/api/mcp/invoke` 端点。

### Q4: 应用启动慢 / MCP Client 初始化失败

**情况**: 这是正常的！

如果 MCP Server 未启动:
- 应用仍会正常启动
- 日志会显示警告: "MCP Server health check failed"
- MCP 功能暂时不可用，但不影响其他功能

---

## 📊 日志解读

### 正常启动日志

```
2026-01-21 17:50:00 - Initializing MCP Client with SSE URL: http://localhost:8081/sse
2026-01-21 17:50:01 - MCP Client initialized successfully
2026-01-21 17:50:01 - MCP Server health check passed
```
✅ MCP 功能可用

### MCP Server 未启动日志

```
2026-01-21 17:50:00 - Initializing MCP Client with SSE URL: http://localhost:8081/sse
2026-01-21 17:50:01 - MCP Client initialized successfully
2026-01-21 17:50:02 - MCP Server health check failed (server may not be running): Connection refused
```
⚠️ 应用可用，但 MCP 功能不可用

### 成功的工具调用日志

```
2026-01-21 17:55:00 - Processing MCP chat request: 请帮我查询北京的天气
2026-01-21 17:55:00 - Found 5 MCP tools
2026-01-21 17:55:01 - LLM requested 1 tool executions
2026-01-21 17:55:01 - Executing MCP tool: get_weather with arguments: {"city":"北京"}
2026-01-21 17:55:02 - Tool execution result: 北京今天晴，温度15-25度
2026-01-21 17:55:03 - Final response generated with tool results
```
✅ 工具调用成功

---

## 🎯 下一步

1. **创建 MCP Server** (如果还没有)
   - 实现具体的工具 (天气、文件系统、数据库等)
   - 在 MCP Server 中注册工具

2. **测试工具调用**
   - 使用 `test-mcp.sh` 脚本
   - 或通过前端界面交互

3. **扩展工具集**
   - 添加更多实用工具
   - 完善工具描述和参数

4. **集成到前端**
   - 在聊天界面显示工具调用过程
   - 展示工具执行结果

---

## 📚 相关文档

- [MCP 实现详细指南](./MCP_IMPLEMENTATION_GUIDE.md) - 架构和实现细节
- [MCP 实现总结](./MCP_IMPLEMENTATION_SUMMARY.md) - 完整的实现清单

---

## 🎉 总结

**MCP 功能已经就绪，只需启动 MCP Server 即可使用！**

最简单的测试流程:
```bash
# 终端 1: 启动 MCP Server
cd mcp-server && mvn spring-boot:run

# 终端 2: 启动后端
cd langchain4j-demo-core && mvn spring-boot:run

# 终端 3: 测试
./test-mcp.sh
```

祝使用愉快！🚀
