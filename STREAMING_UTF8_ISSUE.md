# 流式聊天 UTF-8 编码问题说明

## 问题描述

在使用 LangChain4j 1.0.0-beta3 的 `OpenAiStreamingChatModel` 时，中文字符在流式传输过程中被转换成问号 `?`。

## 根本原因

从后端日志可以看到，OpenAI API 返回的 SSE JSON 中 `content` 字段就已经是问号：

```json
{"delta":{"content":"??"}}  // API 返回的就是问号
```

这说明问题出在 **LangChain4j 库内部的 HTTP 客户端（OkHttp）在解析 SSE 流时没有正确处理 UTF-8 编码**。

## 可能的解决方案

###方案 1：升级 LangChain4j 版本（推荐）

等待 LangChain4j 修复此问题，或尝试升级到最新版本：

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>最新版本</version>
</dependency>
```

### 方案 2：使用非流式 + 前端模拟流式效果

如果简单聊天模式正常显示中文，可以：
1. 后端使用普通的 `OpenAiChatModel`（非流式）
2. 前端在收到完整响应后，逐字显示内容

### 方案 3：切换到其他 LLM 提供商

LangChain4j 支持多个 LLM 提供商，可以尝试其他提供商的流式 API。

### 方案 4：自定义 HTTP 客户端

深入定制 OkHttpClient 的 SSE 解析逻辑（较复杂）。

## 测试建议

1. 测试"简单聊天"模式是否正常显示中文
2. 如果正常，说明问题仅在流式传输
3. 临时使用非流式模式作为 workaround
