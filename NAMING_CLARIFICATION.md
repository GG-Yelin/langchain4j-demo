# 命名澄清说明

## 当前实现

**实际功能**: 本地工具调用（Local Tools）
**错误命名**: MCP（Model Context Protocol）

## 为什么命名不准确？

### MCP 的真正含义
MCP (Model Context Protocol) 是一个**远程协议**，用于：
- 客户端和服务器之间的远程通信
- 通过网络调用外部工具
- 使用 SSE/HTTP 等传输方式

### 我们的实际实现
- ✅ 本地工具类（WeatherTool, CalculatorTool）
- ✅ 直接方法调用（无网络通信）
- ✅ LangChain4j 的 `@Tool` 注解
- ✅ `AiServices` 自动集成

**这是标准的 LangChain4j 本地工具机制，不是 MCP！**

## 应该如何命名？

### 正确的术语

| 当前错误命名 | 应该叫什么 |
|------------|-----------|
| MCP Service | Tool Service / AI Service |
| MCP Tools | Local Tools / Agent Tools |
| MCP Chat | Tool-Assisted Chat |
| MCP Controller | Tools Controller |

### API 端点建议

**当前**（误导性）:
```
GET  /api/mcp/tools
POST /api/mcp/chat
POST /api/mcp/invoke
```

**建议改为**:
```
GET  /api/tools/list
POST /api/chat/with-tools
POST /api/tools/invoke
```

或者保持简单：
```
GET  /api/tools
POST /api/chat
POST /api/tools/{toolName}
```

## 为什么会有这个混淆？

### 原始计划
1. 项目最初想使用 MCP 协议连接远程 MCP Server
2. 创建了 `McpService`, `McpController` 等类
3. 准备实现 MCP 远程调用

### 实际情况
1. 发现 LangChain4j MCP Client 和 Spring AI MCP Server 协议不兼容
2. 改为使用本地工具实现
3. 但类名和 API 路径还保留着 "MCP" 字样 ❌

## 是否需要重构命名？

### 方案 1: 保持现状（不推荐）
- 优点: 不需要改代码
- 缺点:
  - ❌ 命名误导（明明是本地工具，却叫 MCP）
  - ❌ 新开发者会困惑
  - ❌ 文档和代码不一致

### 方案 2: 完整重构（推荐）
重命名所有相关类和 API：

**文件重命名**:
```
McpService.java          → ToolService.java
McpServiceImpl.java      → ToolServiceImpl.java
McpController.java       → ToolsController.java
McpRequest.java          → ToolChatRequest.java
McpResponse.java         → ToolChatResponse.java
```

**API 路径**:
```
/api/mcp/*  →  /api/tools/*
```

**前端更新**:
- MCP 模式 → 工具模式 / AI 助手模式
- 查看 MCP Tools → 查看可用工具

### 方案 3: 保留但加注释（折中）
- 保持类名和 API 不变
- 在类和方法上添加明确注释说明这不是真正的 MCP
- 更新用户文档

```java
/**
 * 工具服务实现
 *
 * 注意: 虽然命名为 "Mcp"，但实际使用的是 LangChain4j 本地工具机制，
 * 不是真正的 MCP (Model Context Protocol) 远程调用。
 *
 * 为保持 API 兼容性，暂时保留 "Mcp" 命名。
 */
@Service
public class McpServiceImpl implements McpService {
    // ...
}
```

## 技术对比

### MCP (Model Context Protocol)
```
┌─────────────┐   HTTP/SSE    ┌─────────────┐
│ MCP Client  │ ◄──────────► │ MCP Server  │
│ (LangChain) │   远程调用     │ (Spring AI) │
└─────────────┘              └─────────────┘
                                    │
                                    ▼
                             ┌─────────────┐
                             │ Tools/APIs  │
                             └─────────────┘
```

### Local Tools (我们的实现)
```
┌─────────────────────────────────────┐
│      Same Application               │
│                                     │
│  ┌──────────┐      ┌──────────┐   │
│  │ LangChain│      │  Tools   │   │
│  │ Service  │─────►│  (Local) │   │
│  └──────────┘      └──────────┘   │
│                                     │
│  直接方法调用，无网络通信             │
└─────────────────────────────────────┘
```

## 前端显示建议

### 当前（误导）
```
模式选择:
○ 普通对话
○ RAG 问答
● MCP 模式  ← 误导！这不是 MCP
```

### 建议修改
```
模式选择:
○ 普通对话
○ RAG 问答
● 工具助手  ← 更准确
```

或者：
```
模式选择:
○ 普通对话
○ 文档问答 (RAG)
● AI 工具 (Tools)  ← 清晰明了
```

## 文档更新

需要更新的文档：
- `README.md` - 说明这是本地工具，不是 MCP
- `MCP_IMPLEMENTATION_GUIDE.md` - 重命名为 `TOOLS_GUIDE.md`
- `MCP_QUICK_START.md` - 重命名为 `TOOLS_QUICK_START.md`
- 前端组件注释

## 建议行动

### 立即行动（文档层面）
1. ✅ 创建本文档澄清命名
2. 在现有文档中添加说明："注意：虽然叫 MCP，但实际是本地工具"
3. 更新 README，明确说明技术栈

### 后续重构（代码层面）
如果用户同意，可以：
1. 重命名所有 Mcp* 类为 Tool*
2. 修改 API 路径
3. 更新前端显示

## 用户决策

用户可以选择：

**选项 A**: 保持现状，只更新文档说明
- 工作量: 小
- 影响: 无
- 缺点: 命名不准确

**选项 B**: 完整重构命名
- 工作量: 中
- 影响: API 路径变化，前端需更新
- 优点: 命名准确，代码清晰

**选项 C**: 渐进式重构
- 新功能使用正确命名
- 旧代码保持不变但加注释
- 逐步迁移

## 总结

**关键点**:
- ❌ 当前实现**不是** MCP（Model Context Protocol）
- ✅ 当前实现**是** LangChain4j 本地工具（Local Tools）
- ⚠️ 命名需要澄清或重构

**核心功能没有问题**，只是命名容易引起误解。

---

**创建时间**: 2026-01-21
**状态**: 命名混淆已识别，等待用户决策是否重构
