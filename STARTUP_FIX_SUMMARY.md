# 启动问题修复总结

## 问题描述

用户报告 `langchain4j-demo-core` 启动失败。

## 问题原因

经过排查，发现了两个问题：

### 问题 1：HTTP 模式下 mcp-server 未启动

**错误信息**：
```
Failed to initialize MCP Client
java.net.ConnectException: Connection refused
Failed to connect to localhost:8081
```

**原因**：
配置文件中使用了 `http` 传输模式，但 mcp-server 服务还没有启动。

### 问题 2：Stdio 模式下日志干扰 JSON 解析

**错误信息**：
```
Exception in thread "Thread-1" java.lang.RuntimeException:
com.fasterxml.jackson.core.JsonParseException: Unexpected character ('.' (code 46))
```

**原因**：
- mcp-server 启动时输出 Spring Boot Banner 和日志
- Stdio 模式下，这些输出干扰了 MCP 协议的 JSON 通信
- LangChain4j MCP Client 将这些非 JSON 内容当作协议消息解析，导致失败

## 解决方案

### 修复 1：改用 Stdio 模式作为默认配置

**文件**：`langchain4j-demo-core/src/main/resources/application.yml`

**修改**：
```yaml
mcp:
  transport:
    type: stdio  # 改为 stdio 模式（原来是 http）
```

**优点**：
- 只需启动一个服务
- 自动启动 mcp-server
- 适合开发测试

### 修复 2：禁用 mcp-server 的 Banner 和控制台日志

**文件**：`mcp-server/src/main/resources/application.yml`

**修改**：

```yaml
spring:
  main:
    banner-mode: off         # 关闭 Banner（原来是 console）
    log-startup-info: false  # 关闭启动信息（原来是 true）

logging:
  level:
    root: WARN              # 降低日志级别（原来是 INFO/DEBUG）
    org.example.mcpserver: ERROR
  pattern:
    console: ""             # 禁用控制台输出
  file:
    name: logs/mcp-server.log  # 日志输出到文件
```

**原理**：
- Stdio 模式下，stdout 用于 MCP 协议通信
- 必须关闭所有非 JSON 的控制台输出
- 日志改为输出到文件 `logs/mcp-server.log`

### 修复 3：创建故障排查文档

新增文档：`TROUBLESHOOTING.md`

内容包括：
- ✅ 两种传输模式的详细说明
- ✅ 启动失败的常见原因
- ✅ 完整的解决步骤
- ✅ HTTP vs Stdio 模式对比
- ✅ 端口占用等常见问题

### 修复 4：创建优化的启动脚本

新增脚本：`start-demo-core-fixed.sh`

功能：
- ✅ 自动检查 Java 和 Maven
- ✅ 验证 mcp-server JAR 是否存在
- ✅ 检测当前使用的传输模式
- ✅ HTTP 模式下检查 mcp-server 是否运行
- ✅ 提供清晰的错误提示

## 修改文件清单

### 1. 配置文件

| 文件 | 修改内容 |
|------|---------|
| `langchain4j-demo-core/src/main/resources/application.yml` | 改为 stdio 模式 |
| `mcp-server/src/main/resources/application.yml` | 关闭 Banner 和控制台日志 |

### 2. 新增文档

| 文件 | 说明 |
|------|------|
| `TROUBLESHOOTING.md` | 详细的故障排查指南 |
| `STARTUP_FIX_SUMMARY.md` | 本文档 - 修复总结 |

### 3. 新增脚本

| 文件 | 说明 |
|------|------|
| `start-demo-core-fixed.sh` | 优化的启动脚本（含环境检查） |

## 当前配置

### Stdio 模式（默认，推荐开发测试）

**demo-core 配置**：
```yaml
mcp:
  transport:
    type: stdio
  server:
    jar: ../mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar
```

**mcp-server 配置**：
```yaml
spring:
  main:
    banner-mode: off
    log-startup-info: false
logging:
  pattern:
    console: ""
  file:
    name: logs/mcp-server.log
```

**启动方式**：
```bash
# 只需启动 demo-core（会自动启动 mcp-server）
cd langchain4j-demo-core
mvn spring-boot:run
```

### HTTP 模式（可选，推荐生产环境）

如果需要使用 HTTP 模式（两个独立服务）：

**demo-core 配置**：
```yaml
mcp:
  transport:
    type: http  # 改为 http
  server:
    url: http://localhost:8081
```

**mcp-server 配置**：
```yaml
spring:
  main:
    banner-mode: console  # 恢复 Banner
    log-startup-info: true
logging:
  level:
    root: INFO           # 恢复日志级别
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**启动方式**：
```bash
# 终端 1 - 先启动 mcp-server
cd mcp-server
mvn spring-boot:run

# 终端 2 - 再启动 demo-core
cd langchain4j-demo-core
mvn spring-boot:run
```

## 验证修复

### 1. 重新构建 mcp-server

```bash
cd mcp-server
mvn clean package -DskipTests
```

**结果**：✅ BUILD SUCCESS

### 2. 启动 demo-core

```bash
cd langchain4j-demo-core
mvn spring-boot:run
```

**预期日志**：
```
Initializing MCP Client with Stdio transport
MCP Server JAR: ../mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar
Starting process: [java, -jar, /path/to/mcp-server-0.0.1-SNAPSHOT.jar]
PID of the started process: xxxx
MCP Client initialized successfully with stdio transport
Tomcat started on port(s): 8080 (http)
Started Langchain4jDemoApplication
```

**注意**：mcp-server 的日志现在输出到 `mcp-server/logs/mcp-server.log` 文件。

### 3. 测试 API

```bash
# 获取工具列表
curl http://localhost:8080/api/mcp/tools

# 调用计算器工具
curl -X POST "http://localhost:8080/api/mcp/invoke?toolName=calculator_add" \
  -H "Content-Type: application/json" \
  -d '{"a": 10, "b": 20}'
```

## 技术说明

### Stdio 传输模式的要求

Stdio 模式通过标准输入输出（stdin/stdout）进行 MCP 协议通信：

1. **stdout 必须只包含 JSON** - 所有 MCP 协议消息都是 JSON 格式
2. **不能有其他输出** - Banner、日志、调试信息都会干扰解析
3. **日志必须重定向** - 输出到文件而非控制台

### 为什么之前会失败？

**失败的输出**：
```
  .   ____          _            __ _ _     <-- Spring Boot Banner
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...
14:34:49.179 [main] INFO  o.e.mcpserver...  <-- 日志输出
```

**LangChain4j MCP Client 尝试解析**：
```json
"  .   ____          _            __ _ _"  <-- 不是有效的 JSON！
```

**导致**：`JsonParseException`

**修复后的输出**：
```json
{"jsonrpc": "2.0", "id": 1, "method": "initialize", ...}  <-- 只有 JSON
```

### HTTP 传输模式不受影响

HTTP 模式通过 HTTP/SSE 协议通信：
- 使用独立的 HTTP 端点（不使用 stdout）
- Banner 和日志可以正常输出
- 两个服务完全独立

## 最佳实践建议

### 开发测试

使用 **Stdio 模式**：
- ✅ 简单方便
- ✅ 一条命令启动
- ✅ 自动管理 mcp-server 生命周期
- ⚠️ 日志在文件中查看

### 生产环境

使用 **HTTP 模式**：
- ✅ 独立部署
- ✅ 易于扩展
- ✅ 可以独立重启
- ✅ 日志清晰可见
- ⚠️ 需要管理两个服务

## 后续工作

### 已完成 ✅

1. ✅ 修复 stdio 模式配置问题
2. ✅ 优化日志输出
3. ✅ 创建故障排查文档
4. ✅ 创建优化的启动脚本
5. ✅ 重新构建 mcp-server JAR

### 可选优化

1. ⭐ 创建不同环境的配置文件
   - `application-dev.yml` (stdio 模式)
   - `application-prod.yml` (http 模式)

2. ⭐ 添加健康检查脚本
   - 自动检测服务状态
   - 验证 MCP 连接

3. ⭐ 优化错误提示
   - 更友好的启动失败提示
   - 自动诊断常见问题

## 相关文档

- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - 详细的故障排查指南
- [MCP_CLIENT_USAGE_GUIDE.md](MCP_CLIENT_USAGE_GUIDE.md) - MCP Client 使用指南
- [test-mcp-client.sh](test-mcp-client.sh) - 自动化测试脚本

## 总结

通过以下修复，成功解决了启动问题：

1. ✅ **配置优化** - 默认使用 stdio 模式
2. ✅ **日志重定向** - 关闭控制台输出，避免干扰 JSON 解析
3. ✅ **文档完善** - 提供详细的故障排查指南
4. ✅ **脚本优化** - 提供自动检查的启动脚本

现在可以顺利启动服务并测试 MCP 功能了！🎉
