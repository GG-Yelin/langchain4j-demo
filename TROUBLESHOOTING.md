# 故障排查指南

## 问题：demo-core 启动失败

### 错误信息

```
Failed to initialize MCP Client
java.net.ConnectException: Connection refused
Failed to connect to localhost:8081
```

### 原因分析

这个错误表示 demo-core 使用了 **HTTP 模式** 连接 mcp-server，但 mcp-server 还没有启动。

从日志可以看到：
```
Initializing MCP Client with HTTP/SSE transport
SSE URL: http://localhost:8081/mcp/sse
Note: HTTP/SSE requires manually starting mcp-server first!
```

### 解决方案

根据你的使用场景，选择以下两种解决方案之一：

---

## 解决方案 1：使用 Stdio 模式（推荐用于开发测试）✅

**优点**：
- ✅ 只需启动一个服务
- ✅ demo-core 自动启动 mcp-server
- ✅ 简单方便，适合开发测试

**步骤**：

### 1. 修改配置文件

编辑 `langchain4j-demo-core/src/main/resources/application.yml`：

```yaml
mcp:
  transport:
    type: stdio  # 改为 stdio 模式
```

### 2. 确保 mcp-server JAR 已构建

```bash
cd mcp-server
mvn clean package -DskipTests
```

检查 JAR 是否存在：
```bash
ls -la mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar
```

### 3. 启动 demo-core

```bash
cd langchain4j-demo-core
mvn spring-boot:run
```

或使用启动脚本：
```bash
./start-demo-core-fixed.sh
```

**工作原理**：
demo-core 会通过标准输入输出启动 mcp-server JAR，两个进程通过 stdio 通信。

---

## 解决方案 2：使用 HTTP 模式（推荐用于独立服务）

**优点**：
- ✅ 两个独立的服务
- ✅ 可以独立重启
- ✅ 适合生产环境

**步骤**：

### 1. 确认配置文件

编辑 `langchain4j-demo-core/src/main/resources/application.yml`：

```yaml
mcp:
  transport:
    type: http  # 使用 http 模式
  server:
    url: http://localhost:8081
```

### 2. 先启动 mcp-server

**终端 1**：
```bash
cd mcp-server
mvn spring-boot:run
```

等待看到以下日志表示启动成功：
```
MCP Server 启动成功!
Spring AI MCP Server WebMVC 运行中
访问地址: http://localhost:8081
```

### 3. 再启动 demo-core

**终端 2**：
```bash
cd langchain4j-demo-core
mvn spring-boot:run
```

应该看到：
```
Initializing MCP Client with HTTP/SSE transport
SSE URL: http://localhost:8081/mcp/sse
MCP Client initialized successfully with http transport
```

---

## 配置对比

| 特性 | Stdio 模式 | HTTP 模式 |
|------|-----------|-----------|
| **启动方式** | 只启动 demo-core | 分别启动两个服务 |
| **服务数量** | 1 个（自动启动 mcp-server） | 2 个独立服务 |
| **通信方式** | 标准输入输出 | HTTP/SSE |
| **适用场景** | 开发测试 | 生产环境 |
| **优点** | 简单方便 | 独立部署、易扩展 |
| **缺点** | 耦合在一起 | 需要管理两个服务 |

---

## 常见问题

### Q1: 如何确认使用的是哪种模式？

**方法 1**：查看配置文件
```bash
cat langchain4j-demo-core/src/main/resources/application.yml | grep -A 2 "transport:"
```

**方法 2**：查看启动日志
- Stdio 模式: `Initializing MCP Client with Stdio transport`
- HTTP 模式: `Initializing MCP Client with HTTP/SSE transport`

### Q2: mcp-server JAR 不存在怎么办？

**错误**：
```
MCP Server JAR not found: /path/to/mcp-server-0.0.1-SNAPSHOT.jar
Please build mcp-server first
```

**解决**：
```bash
cd mcp-server
mvn clean package -DskipTests
```

### Q3: 端口 8080 被占用

**错误**：
```
Web server failed to start. Port 8080 was already in use.
```

**解决方法 1** - 修改端口：

编辑 `langchain4j-demo-core/src/main/resources/application.yml`：
```yaml
server:
  port: 8082  # 改为其他端口
```

**解决方法 2** - 找到占用进程：
```bash
lsof -i :8080
kill -9 <PID>
```

### Q4: 端口 8081 被占用（mcp-server）

**错误**：
```
Web server failed to start. Port 8081 was already in use.
```

**解决**：

编辑 `mcp-server/src/main/resources/application.yml`：
```yaml
server:
  port: 8082  # 改为其他端口
```

同时修改 demo-core 的配置：
```yaml
mcp:
  server:
    url: http://localhost:8082  # 更新为新端口
```

### Q5: 如何验证服务启动成功？

**验证 mcp-server (8081)**：
```bash
curl http://localhost:8081/actuator/health
```

应该返回：
```json
{"status":"UP"}
```

**验证 demo-core (8080)**：
```bash
curl http://localhost:8080/actuator/health
```

应该返回：
```json
{"status":"UP"}
```

### Q6: HTTP 模式下连接失败

**错误**：
```
Connection refused
Failed to connect to localhost:8081
```

**检查清单**：

1. ✅ mcp-server 是否已启动？
   ```bash
   curl http://localhost:8081/actuator/health
   ```

2. ✅ 端口号是否正确？
   - mcp-server 默认端口：8081
   - demo-core 配置的 URL：http://localhost:8081

3. ✅ 防火墙是否阻止？
   ```bash
   # macOS
   sudo /usr/libexec/ApplicationFirewall/socketfilterfw --getglobalstate
   ```

4. ✅ 查看 mcp-server 日志确认启动成功

---

## 推荐的启动流程

### 开发测试（使用 Stdio 模式）

```bash
# 1. 修改配置
# 编辑 langchain4j-demo-core/src/main/resources/application.yml
# 设置 mcp.transport.type: stdio

# 2. 构建 mcp-server JAR
cd mcp-server
mvn clean package -DskipTests

# 3. 启动 demo-core（会自动启动 mcp-server）
cd ../langchain4j-demo-core
mvn spring-boot:run

# 4. 测试
curl http://localhost:8080/api/mcp/tools
```

### 生产环境（使用 HTTP 模式）

```bash
# 1. 修改配置
# 编辑 langchain4j-demo-core/src/main/resources/application.yml
# 设置 mcp.transport.type: http

# 2. 启动 mcp-server（终端 1）
cd mcp-server
mvn spring-boot:run

# 3. 等待 mcp-server 启动完成
# 看到 "MCP Server 启动成功!" 后继续

# 4. 启动 demo-core（终端 2）
cd langchain4j-demo-core
mvn spring-boot:run

# 5. 测试
curl http://localhost:8080/api/mcp/tools
```

---

## 完整测试流程

启动成功后，运行测试脚本：

```bash
./test-mcp-client.sh
```

测试内容：
- ✅ 检查服务状态
- ✅ 获取工具列表
- ✅ 调用计算器工具
- ✅ 调用天气工具
- ✅ AI 聊天（自动调用工具）

---

## 日志调试

### 启用详细日志

编辑 `application.yml`：

```yaml
logging:
  level:
    root: INFO
    org.example.langchain4jdemo: DEBUG
    dev.langchain4j: DEBUG
    org.springframework.ai: DEBUG
```

### 关键日志

**成功的 Stdio 模式日志**：
```
Initializing MCP Client with Stdio transport
MCP Server JAR: /path/to/mcp-server-0.0.1-SNAPSHOT.jar
MCP Client initialized successfully with stdio transport
```

**成功的 HTTP 模式日志**：
```
Initializing MCP Client with HTTP/SSE transport
SSE URL: http://localhost:8081/mcp/sse
MCP Client initialized successfully with http transport
```

**失败的日志（需要解决）**：
```
Failed to initialize MCP Client
java.net.ConnectException: Connection refused
```

---

## 获取帮助

如果问题仍未解决，请提供以下信息：

1. 完整的错误日志
2. 使用的配置模式（stdio 或 http）
3. Java 版本：`java -version`
4. Maven 版本：`mvn -version`
5. 端口占用情况：`lsof -i :8080 -i :8081`

---

## 相关文档

- [MCP_CLIENT_USAGE_GUIDE.md](MCP_CLIENT_USAGE_GUIDE.md) - 详细使用指南
- [MCP_SERVER_REFACTOR_SUMMARY.md](MCP_SERVER_REFACTOR_SUMMARY.md) - MCP Server 实现说明
- [test-mcp-client.sh](test-mcp-client.sh) - 自动化测试脚本
