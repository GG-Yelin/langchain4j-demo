# LangChain4j Demo - 远程 MCP 版本

## 🎯 项目概述

这是一个基于 **LangChain4j** 和 **Spring Boot** 的 AI Agent 示例项目，实现了**远程 MCP (Model Context Protocol)** 架构。

AI Agent 可以通过网络调用独立部署的工具服务器（MCP Server），实现**工具与应用的解耦**。

## ✨ 核心特性

- ✅ **远程工具调用** - AI Agent 通过 HTTP REST API 调用远程工具
- ✅ **自定义协议** - 简单直观的 JSON 协议，不依赖特定框架
- ✅ **AI 自动集成** - LangChain4j Agent 自动发现和调用工具
- ✅ **独立部署** - 工具服务器可独立部署、扩展、升级
- ✅ **易于扩展** - 添加新工具只需在 Server 端实现

## 🏗️ 架构设计

```
┌─────────────────────────────┐         HTTP REST API          ┌──────────────────────┐
│   langchain4j-demo-core     │  ────────────────────────────>  │     mcp-server       │
│       (AI Application)      │                                 │   (Tool Server)      │
│        Port: 8080           │  <────────────────────────────  │     Port: 8081       │
└─────────────────────────────┘                                 └──────────────────────┘
        │                                                                │
        ├─ AI Agent (LangChain4j)                                       ├─ REST Controller
        ├─ RemoteMcpClient                                              ├─ Tool Service
        ├─ RemoteMcpToolAdapter                                         └─ Tools
        └─ McpServiceImpl                                                   ├─ WeatherTool
                                                                             └─ CalculatorTool
```

## 📦 模块说明

### 1. langchain4j-demo-core
**AI 应用模块** (端口 8080)

- **AI Agent** - 使用 LangChain4j 构建的智能对话代理
- **RemoteMcpClient** - HTTP 客户端，调用远程工具服务
- **RemoteMcpToolAdapter** - 将远程工具适配为 LangChain4j `@Tool` 注解的本地方法
- **Web API** - 提供 REST API 供前端调用

### 2. mcp-server
**工具服务器模块** (端口 8081)

- **REST API** - 提供工具查询和执行接口
- **工具管理** - 自动扫描和管理所有工具
- **工具执行** - 执行工具调用并返回结果

**提供的 API:**
- `GET /mcp/health` - 健康检查
- `GET /mcp/tools` - 列出所有工具
- `POST /mcp/execute` - 执行指定工具

## 🚀 快速开始

### 前置要求
- Java 17+
- Maven 3.6+

### 1. 编译项目
```bash
mvn clean package -DskipTests
```

### 2. 启动服务

**终端 1 - 启动 MCP Server:**
```bash
./start-mcp-server.sh
```

**终端 2 - 启动 Demo Core:**
```bash
./start-demo-core.sh
```

### 3. 测试

**快速测试:**
```bash
# 健康检查
curl http://localhost:8081/mcp/health

# AI 对话测试
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"北京今天天气怎么样？"}'
```

**完整测试套件:**
```bash
./test-remote-mcp.sh
```

## 🧪 测试示例

### 示例 1: AI 自动调用天气工具

**请求:**
```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"北京今天天气怎么样？"}'
```

**响应:**
```json
{
  "success": true,
  "content": "根据查询结果，北京今天天气晴朗，温度在15-25度之间，空气质量良好。"
}
```

**工作流程:**
1. AI 识别用户需要查询天气
2. 自动调用远程 `getWeather` 工具
3. 整合结果，生成自然语言回复

### 示例 2: AI 自动调用计算器工具

**请求:**
```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"帮我计算 123 + 456"}'
```

**响应:**
```json
{
  "success": true,
  "content": "计算结果是 579。"
}
```

### 示例 3: 直接调用工具 API

**请求:**
```bash
curl -X POST http://localhost:8081/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "add",
    "arguments": {"a": 12, "b": 34}
  }'
```

**响应:**
```json
{
  "success": true,
  "result": "46.0"
}
```

## 🛠️ 可用工具

### 天气工具
- `getWeather(city)` - 查询城市天气
- `getWeatherForecast(city, days)` - 查询天气预报

### 计算器工具
- `add(a, b)` - 加法
- `subtract(a, b)` - 减法
- `multiply(a, b)` - 乘法
- `divide(a, b)` - 除法
- `power(base, exponent)` - 幂运算
- `sqrt(number)` - 平方根

## 📡 API 协议

### 工具执行请求
```json
{
  "toolName": "工具方法名",
  "arguments": {
    "参数名1": "值1",
    "参数名2": "值2"
  }
}
```

### 工具执行响应

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

## ➕ 添加新工具

### 步骤 1: 在 MCP Server 创建工具类

```java
// mcp-server/src/main/java/org/example/mcpserver/tools/TranslationTool.java
@Component
public class TranslationTool {
    public String translate(String text, String targetLang) {
        // 翻译逻辑
        return "Translated: " + text;
    }
}
```

### 步骤 2: 在 McpToolService 注册

```java
@RequiredArgsConstructor
public class McpToolService {
    private final TranslationTool translationTool;

    private List<Object> getTools() {
        return Arrays.asList(
            weatherTool,
            calculatorTool,
            translationTool  // 新增
        );
    }
}
```

### 步骤 3: 在 RemoteMcpToolAdapter 添加适配

```java
// langchain4j-demo-core/.../mcp/RemoteMcpToolAdapter.java
@Tool("Translate text to target language")
public String translate(String text, String targetLang) {
    Map<String, Object> args = new HashMap<>();
    args.put("text", text);
    args.put("targetLang", targetLang);
    return remoteMcpClient.executeTool("translate", args);
}
```

完成！AI 现在可以自动使用翻译工具。

## 📁 项目结构

```
langchain4j-demo/
├── langchain4j-demo-core/          # AI 应用模块
│   ├── src/main/java/.../
│   │   ├── controller/             # REST 控制器
│   │   ├── service/                # 业务服务
│   │   ├── mcp/
│   │   │   ├── client/
│   │   │   │   ├── RemoteMcpClient.java          ✨ 远程客户端
│   │   │   │   └── McpClientConfiguration.java   ✅ 配置类
│   │   │   └── RemoteMcpToolAdapter.java         ✨ 工具适配器
│   │   ├── config/
│   │   │   └── RestTemplateConfig.java           ✨ HTTP 客户端配置
│   │   └── ...
│   └── src/main/resources/
│       └── application.yml                        ✅ 配置文件
│
├── mcp-server/                     # 工具服务器模块
│   ├── src/main/java/.../
│   │   ├── controller/
│   │   │   └── McpController.java                ✨ REST API
│   │   ├── service/
│   │   │   └── McpToolService.java               ✨ 工具管理
│   │   ├── tools/
│   │   │   ├── WeatherTool.java                  # 天气工具
│   │   │   └── CalculatorTool.java               # 计算器工具
│   │   └── dto/                                  # 数据传输对象
│   └── src/main/resources/
│       └── application.yml                        # 配置文件
│
├── start-mcp-server.sh             ✨ MCP Server 启动脚本
├── start-demo-core.sh              ✨ Demo Core 启动脚本
├── test-remote-mcp.sh              ✨ 自动化测试脚本
├── QUICK_START.md                  ✨ 快速开始指南
├── REMOTE_MCP_GUIDE.md             ✨ 详细使用指南
├── REMOTE_MCP_IMPLEMENTATION.md    ✨ 实现总结
└── README_REMOTE_MCP.md            # 本文档
```

## ⚙️ 配置说明

### MCP Server 配置
```yaml
# mcp-server/src/main/resources/application.yml
server:
  port: 8081  # MCP Server 端口
```

### Demo Core 配置
```yaml
# langchain4j-demo-core/src/main/resources/application.yml
mcp:
  server:
    url: http://localhost:8081  # 远程 MCP Server 地址
```

## 🔍 故障排查

### 端口被占用
```bash
lsof -i :8080  # 检查 Demo Core 端口
lsof -i :8081  # 检查 MCP Server 端口
kill -9 <PID>  # 杀死进程
```

### MCP Server 无法连接
```bash
# 测试健康检查
curl http://localhost:8081/mcp/health

# 查看日志
tail -f mcp-server/logs/application.log
```

### AI 不调用工具
- 确保问题清晰明确
- 查看日志中的工具调用信息
- 尝试更明确的指令

## 📚 文档

- **[快速开始](QUICK_START.md)** - 5 分钟快速体验
- **[使用指南](REMOTE_MCP_GUIDE.md)** - 详细的使用说明
- **[实现总结](REMOTE_MCP_IMPLEMENTATION.md)** - 技术实现细节

## 🎯 优势

### 架构优势
- ✅ **解耦设计** - 工具与应用分离，独立开发和部署
- ✅ **易于扩展** - 添加新工具无需修改 AI 应用
- ✅ **独立升级** - 工具服务器可以独立升级，不影响 AI 应用

### 技术优势
- ✅ **标准协议** - 使用 HTTP REST API，跨语言支持
- ✅ **简单直观** - JSON 格式，易于理解和调试
- ✅ **AI 自动集成** - LangChain4j 自动发现和调用工具

### 使用优势
- ✅ **透明调用** - AI Agent 透明地调用远程工具
- ✅ **手动调用** - 也可以直接调用工具 API
- ✅ **易于测试** - 可以单独测试每个工具

## 🚀 生产部署

### Docker 部署
```bash
# MCP Server
docker build -t mcp-server ./mcp-server
docker run -p 8081:8081 mcp-server

# Demo Core
docker build -t demo-core ./langchain4j-demo-core
docker run -p 8080:8080 \
  -e MCP_SERVER_URL=http://mcp-server:8081 \
  demo-core
```

### Kubernetes 部署
```yaml
# 参考 k8s/ 目录下的配置文件
kubectl apply -f k8s/mcp-server.yaml
kubectl apply -f k8s/demo-core.yaml
```

## 📈 后续计划

- [ ] 添加更多工具（文件操作、数据库查询等）
- [ ] 实现工具调用监控和统计
- [ ] 支持工具调用结果缓存
- [ ] 添加工具权限验证
- [ ] 支持流式响应（SSE）
- [ ] 实现工具版本管理
- [ ] 支持批量工具调用

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📮 联系方式

如有问题，请提交 GitHub Issue。

---

**享受使用远程 MCP 的乐趣！** 🎉
