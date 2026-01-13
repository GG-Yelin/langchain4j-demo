# 流式传输中文编码问题分析

## 问题现象
1. 使用流式聊天时，中文字符显示为 "???"
2. 英文单词连在一起，没有空格
3. 简单聊天（非流式）中文显示正常

## 数据流转链路

```
OpenAI API (UTF-8)
    ↓
LangChain4j HTTP Client (OkHttp)
    ↓ SSE 流解析
LangChain4j StreamingChatResponseHandler
    ↓ onPartialResponse(String token)
ChatServiceImpl
    ↓ callback.onToken(token)
ChatController (Spring SSE)
    ↓ SseEmitter.send(token, MediaType.TEXT_PLAIN)
前端 (fetch API + TextDecoder)
    ↓
浏览器显示
```

## 问题定位

### 1. OpenAI API 响应
- OpenAI API 返回的是标准的 SSE 格式
- 编码：UTF-8
- 格式：`data: {"choices":[{"delta":{"content":"你"}}]}\n\n`
- **此环节正常**（简单聊天证明API返回的JSON是正确的UTF-8）

### 2. LangChain4j HTTP Client (OkHttp)
这是**问题的根源**！

LangChain4j 1.0.0-beta3 使用 OkHttp 处理 SSE 流时：

```java
// 伪代码示意
ResponseBody body = response.body();
BufferedSource source = body.source();

while (!source.exhausted()) {
    String line = source.readUtf8Line(); // 这里有问题！
    // 解析 SSE 格式
}
```

**问题点：**
- OkHttp 的 `readUtf8Line()` 在处理 SSE 流时，对于**分块传输**的UTF-8多字节字符会出现问题
- UTF-8 中文字符占 3 个字节（如 "你" = E4 BD A0）
- 如果 SSE 数据块恰好在多字节字符中间切分，`readUtf8Line()` 无法正确解码
- 结果：多字节字符被解析为 `?` (Unicode 替换字符 U+FFFD)

### 3. 为什么英文单词连在一起？

OpenAI 的流式响应通常是**按 token 分块**的：
```
data: {"delta":{"content":"Hello"}}
data: {"delta":{"content":" "}}
data: {"delta":{"content":"world"}}
```

但 LangChain4j 在解析时可能：
1. 没有正确处理空格 token
2. 或者空格 token 也被编码问题影响

### 4. Spring SSE (SseEmitter)
```java
emitter.send(SseEmitter.event()
    .name("token")
    .data(token, MediaType.TEXT_PLAIN));
```

- **此环节正常**
- Spring 正确使用 UTF-8 编码
- 响应头已设置：`text/event-stream;charset=UTF-8`

### 5. 前端解码
```javascript
const decoder = new TextDecoder(); // 默认 UTF-8
buffer += decoder.decode(value, { stream: true })
```

- **此环节正常**
- TextDecoder 正确处理 UTF-8

## 根本原因（已确认）

**问题不在 LangChain4j，而在演示服务器的配置！**

通过实际测试发现，`http://langchain4j.dev/demo/openai/v1` 演示服务器返回的响应头是：

```
content-type: text/event-stream;charset=iso-8859-1
```

**而不是正确的：**
```
content-type: text/event-stream;charset=UTF-8
```

### 为什么导致中文显示为 `?`？

1. **ISO-8859-1（Latin-1）** 是单字节字符集，只能表示 0-255 的字符
2. **中文 UTF-8 编码** 需要 3 个字节（如 "你" = E4 BD A0）
3. 当服务器声称使用 ISO-8859-1 时，HTTP 客户端会按照 ISO-8859-1 解码
4. UTF-8 的多字节序列在 ISO-8859-1 中无法识别，被替换为 `?`（ASCII 63，0x3F）

### 实际测试结果

```
ServerSentEvent: data={"choices":[{"delta":{"content":"??"}}]}
Token #1: [??]  字节码: 3F 3F
```

可以看到，在 LangChain4j 收到 SSE 事件时，content 字段已经是 `??` 了。

该 Bug 导致：
1. 多字节 UTF-8 字符（中文、日文等）被错误解码为 `?`
2. 英文虽然能正常显示，但 token 分隔可能也受影响

## 解决方案

### 方案 1：使用真实的 OpenAI API（推荐）
如果有 OpenAI API Key，可以切换到真实的 OpenAI API：

```java
OpenAiStreamingChatModel.builder()
    .baseUrl("https://api.openai.com/v1")
    .apiKey("sk-your-api-key-here")
    .modelName("gpt-4o-mini")
    // ...
    .build();
```

真实的 OpenAI API 返回正确的 `charset=UTF-8`，流式传输中文完全正常。

### 方案 2：使用其他兼容的 API 服务
- 国内的 OpenAI API 代理服务
- 其他兼容 OpenAI API 格式的服务（如 Azure OpenAI）
- 确保服务器返回正确的 UTF-8 字符集

### 方案 3：前端模拟流式（当前方案）
```javascript
// 使用简单聊天接口 + 前端逐字显示
const response = await chatApi.simpleChat(params)
// 每 30ms 显示一个字符
```

**优点：**
- 中英文都正常显示
- 用户体验良好
- 实现简单可靠

**缺点：**
- 不是真正的流式（需要等待完整响应）
- 延迟感知可能稍差（对于长回答）

### 方案 4：自己实现 SSE 客户端（不推荐）
- 绕过 LangChain4j 的流式客户端
- 直接调用 OpenAI API
- 工作量大，维护成本高

## 验证方法

通过 Maven 测试验证：

```bash
mvn test -Dtest=StreamingEncodingDebugTest#testStreamingChineseEncoding
```

测试结果清楚地显示：
1. HTTP 响应头：`content-type: text/event-stream;charset=iso-8859-1`
2. SSE 事件中 content 已经是 `??`
3. 每个 `?` 的字节码是 `3F`（ASCII 问号）

## 结论

**问题根源：** LangChain4j 演示服务器 `http://langchain4j.dev/demo/openai/v1` 配置错误，返回 `charset=iso-8859-1` 而不是 `charset=UTF-8`

**LangChain4j 库本身：** 正常工作，按照响应头声明的字符集进行解码

**当前最佳方案：**
1. 如果有 API Key：使用真实的 OpenAI API 或其他正确配置的服务
2. 如果只能使用演示服务器：使用前端模拟流式显示

**长期方案：** 期待 LangChain4j 修复演示服务器的配置问题
