# LangChain4j 使用指南

LangChain4j 是一个用于简化 Java 应用程序与大语言模型（LLM）集成的框架。本文档将帮助你快速上手并掌握其核心功能。

## 目录

1. [快速开始](#快速开始)
2. [核心概念](#核心概念)
3. [Chat Model - 对话模型](#chat-model---对话模型)
4. [AI Services - AI 服务](#ai-services---ai-服务)
5. [Chat Memory - 对话记忆](#chat-memory---对话记忆)
6. [Tools - 工具调用](#tools---工具调用)
7. [RAG - 检索增强生成](#rag---检索增强生成)
8. [Embedding Store - 向量存储](#embedding-store---向量存储)
9. [Document Processing - 文档处理](#document-processing---文档处理)
10. [支持的模型提供商](#支持的模型提供商)
11. [高级特性](#高级特性)

---

## 快速开始

### Maven 依赖

```xml
<!-- 核心依赖 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>1.11.0-beta19-SNAPSHOT</version>
</dependency>

<!-- OpenAI 模型支持 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>1.11.0-beta19-SNAPSHOT</version>
</dependency>
```

### 最简单的示例

```java
import dev.langchain4j.model.openai.OpenAiChatModel;

public class QuickStart {
    public static void main(String[] args) {
        // 创建 OpenAI Chat 模型
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey("your-api-key")
                .modelName("gpt-4o-mini")
                .build();

        // 发送消息并获取回复
        String response = model.chat("你好，请介绍一下自己");
        System.out.println(response);
    }
}
```

---

## 核心概念

LangChain4j 的核心组件包括：

| 组件 | 说明 |
|------|------|
| `ChatModel` | 与 LLM 对话的核心接口 |
| `EmbeddingModel` | 将文本转换为向量的模型 |
| `EmbeddingStore` | 存储和检索向量的数据库 |
| `ChatMemory` | 管理对话历史记录 |
| `AiServices` | 高级 API，通过接口定义与 LLM 交互 |
| `Tool` | 让 LLM 调用外部工具/函数 |
| `ContentRetriever` | RAG 中的内容检索器 |

---

## Chat Model - 对话模型

### ChatModel 接口

`ChatModel` 是与语言模型交互的主要接口：

```java
public interface ChatModel {
    // 简单的字符串对话
    String chat(String userMessage);

    // 完整的请求/响应
    ChatResponse chat(ChatRequest chatRequest);

    // 多消息对话
    ChatResponse chat(List<ChatMessage> messages);
}
```

### 使用 OpenAI 模型

```java
import dev.langchain4j.model.openai.OpenAiChatModel;

OpenAiChatModel model = OpenAiChatModel.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-4o-mini")
        .temperature(0.7)
        .maxTokens(1000)
        .timeout(Duration.ofSeconds(60))
        .logRequests(true)
        .logResponses(true)
        .build();

// 简单对话
String answer = model.chat("Java 的主要特点是什么？");

// 使用消息对象
ChatResponse response = model.chat(
    SystemMessage.from("你是一个专业的 Java 开发者"),
    UserMessage.from("如何创建一个单例模式？")
);

System.out.println(response.aiMessage().text());
```

### 流式响应

```java
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

OpenAiStreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-4o-mini")
        .build();

streamingModel.chat("讲一个故事", new StreamingChatResponseHandler() {
    @Override
    public void onPartialResponse(String partialResponse) {
        System.out.print(partialResponse); // 逐字打印
    }

    @Override
    public void onCompleteResponse(ChatResponse response) {
        System.out.println("\n完成!");
    }

    @Override
    public void onError(Throwable error) {
        error.printStackTrace();
    }
});
```

---

## AI Services - AI 服务

AI Services 是 LangChain4j 的高级 API，允许你通过定义 Java 接口来与 LLM 交互。

### 基础用法

```java
// 定义接口
interface Assistant {
    String chat(String userMessage);
}

// 创建实现
Assistant assistant = AiServices.create(Assistant.class, model);

// 使用
String answer = assistant.chat("你好！");
```

### 使用注解定义提示词

```java
interface SentimentAnalyzer {

    @UserMessage("分析以下文本的情感：{{text}}")
    Sentiment analyzeSentiment(@V("text") String text);
}

enum Sentiment {
    POSITIVE, NEUTRAL, NEGATIVE
}

// 使用
SentimentAnalyzer analyzer = AiServices.create(SentimentAnalyzer.class, model);
Sentiment sentiment = analyzer.analyzeSentiment("今天天气真好，心情愉快！");
// 返回: POSITIVE
```

### 系统消息和用户消息

```java
interface Translator {

    @SystemMessage("你是一个专业的翻译官，擅长将中文翻译成{{targetLanguage}}")
    @UserMessage("请翻译以下文本：{{text}}")
    String translate(@V("text") String text, @V("targetLanguage") String targetLanguage);
}

Translator translator = AiServices.create(Translator.class, model);
String result = translator.translate("你好世界", "英语");
// 返回: Hello World
```

### 结构化输出

```java
// 定义返回类型
record Person(String name, int age, String occupation) {}

interface PersonExtractor {

    @UserMessage("从以下文本中提取人物信息：{{text}}")
    Person extractPerson(@V("text") String text);
}

PersonExtractor extractor = AiServices.create(PersonExtractor.class, model);
Person person = extractor.extractPerson("张三今年25岁，是一名软件工程师");
// 返回: Person[name=张三, age=25, occupation=软件工程师]
```

### 使用 Builder 模式配置

```java
interface Assistant {
    String chat(@MemoryId String memoryId, @UserMessage String message);
}

Assistant assistant = AiServices.builder(Assistant.class)
        .chatModel(model)
        .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
        .tools(new CalculatorTool(), new WebSearchTool())
        .build();
```

---

## Chat Memory - 对话记忆

### MessageWindowChatMemory

基于消息数量的滑动窗口记忆：

```java
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

// 保留最近 10 条消息
ChatMemory memory = MessageWindowChatMemory.builder()
        .maxMessages(10)
        .build();

// 添加消息
memory.add(UserMessage.from("你好"));
memory.add(AiMessage.from("你好！有什么可以帮助你的吗？"));

// 获取所有消息
List<ChatMessage> messages = memory.messages();

// 清空记忆
memory.clear();
```

### TokenWindowChatMemory

基于 Token 数量的滑动窗口记忆：

```java
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiTokenizer;

ChatMemory memory = TokenWindowChatMemory.builder()
        .maxTokens(4000)
        .tokenizer(new OpenAiTokenizer("gpt-4o-mini"))
        .build();
```

### 在 AI Services 中使用

```java
// 单一共享记忆
Assistant assistant = AiServices.builder(Assistant.class)
        .chatModel(model)
        .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
        .build();

// 多用户独立记忆
interface MultiUserAssistant {
    String chat(@MemoryId String memoryId, @UserMessage String message);
}

MultiUserAssistant assistant = AiServices.builder(MultiUserAssistant.class)
        .chatModel(model)
        .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
        .build();

// 不同用户的对话互不影响
assistant.chat("user1", "我叫张三");
assistant.chat("user2", "我叫李四");
assistant.chat("user1", "我叫什么名字？"); // 返回: 张三
```

---

## Tools - 工具调用

### 定义工具

使用 `@Tool` 注解定义可被 LLM 调用的方法：

```java
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;

class Calculator {

    @Tool("计算两个数字的和")
    public double add(@P("第一个数字") double a, @P("第二个数字") double b) {
        return a + b;
    }

    @Tool("计算两个数字的乘积")
    public double multiply(@P("第一个数字") double a, @P("第二个数字") double b) {
        return a * b;
    }

    @Tool("计算一个数字的平方根")
    public double sqrt(@P("要计算平方根的数字") double number) {
        return Math.sqrt(number);
    }
}
```

### 注册工具到 AI Service

```java
interface MathAssistant {
    String chat(String message);
}

MathAssistant assistant = AiServices.builder(MathAssistant.class)
        .chatModel(model)
        .tools(new Calculator())
        .build();

// LLM 会自动调用工具来计算
String answer = assistant.chat("计算 (3 + 5) * 2 的结果");
// LLM 会调用 add(3, 5) 然后 multiply(8, 2)
```

### 实际应用示例

```java
class WeatherTool {

    @Tool("获取指定城市的天气信息")
    public String getWeather(@P("城市名称") String city) {
        // 实际应用中这里会调用天气 API
        return String.format("%s 的天气：晴，温度 25°C", city);
    }
}

class SearchTool {

    @Tool("在网络上搜索信息")
    public String search(@P("搜索关键词") String query) {
        // 实际应用中这里会调用搜索 API
        return "搜索结果：" + query + " 相关信息...";
    }
}

interface SmartAssistant {
    String chat(String message);
}

SmartAssistant assistant = AiServices.builder(SmartAssistant.class)
        .chatModel(model)
        .tools(new WeatherTool(), new SearchTool())
        .build();

// LLM 会根据需要自动选择调用哪个工具
assistant.chat("北京今天天气怎么样？");
assistant.chat("搜索一下 Java 21 的新特性");
```

---

## RAG - 检索增强生成

RAG（Retrieval-Augmented Generation）允许 LLM 基于外部知识库回答问题。

### 基本架构

```
用户问题 -> 查询转换 -> 内容检索 -> 内容注入 -> LLM 生成答案
```

### 快速入门 (Easy RAG)

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-easy-rag</artifactId>
    <version>1.11.0-beta19-SNAPSHOT</version>
</dependency>
```

```java
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;

// 1. 创建向量存储
EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

// 2. 创建嵌入模型
EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("text-embedding-3-small")
        .build();

// 3. 准备文档并存储
Document document = FileSystemDocumentLoader.loadDocument("knowledge.txt");
List<TextSegment> segments = DocumentSplitters.recursive(300, 0).split(document);
List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
embeddingStore.addAll(embeddings, segments);

// 4. 创建内容检索器
ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
        .embeddingStore(embeddingStore)
        .embeddingModel(embeddingModel)
        .maxResults(3)
        .minScore(0.6)
        .build();

// 5. 创建带 RAG 的 AI Service
interface KnowledgeAssistant {
    String answer(String question);
}

KnowledgeAssistant assistant = AiServices.builder(KnowledgeAssistant.class)
        .chatModel(model)
        .contentRetriever(contentRetriever)
        .build();

// 使用
String answer = assistant.answer("文档中提到了什么？");
```

### 高级 RAG 配置

```java
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.query.transformer.ExpandingQueryTransformer;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;

// 查询扩展器 - 生成多个相关查询
QueryTransformer queryTransformer = ExpandingQueryTransformer.builder()
        .chatModel(model)
        .build();

// 内容聚合器 - 重新排序检索结果
ContentAggregator contentAggregator = ReRankingContentAggregator.builder()
        .scoringModel(scoringModel)
        .build();

// 创建检索增强器
RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
        .queryTransformer(queryTransformer)
        .contentRetriever(contentRetriever)
        .contentAggregator(contentAggregator)
        .build();

// 在 AI Service 中使用
interface Assistant {
    String chat(String message);
}

Assistant assistant = AiServices.builder(Assistant.class)
        .chatModel(model)
        .retrievalAugmentor(retrievalAugmentor)
        .build();
```

---

## Embedding Store - 向量存储

### InMemoryEmbeddingStore

内存向量存储，适合开发测试：

```java
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

// 添加向量
Embedding embedding = embeddingModel.embed("Hello World").content();
String id = store.add(embedding, TextSegment.from("Hello World"));

// 搜索相似内容
EmbeddingSearchResult<TextSegment> result = store.search(
    EmbeddingSearchRequest.builder()
        .queryEmbedding(embeddingModel.embed("Hi").content())
        .maxResults(5)
        .minScore(0.7)
        .build()
);

// 获取匹配结果
result.matches().forEach(match -> {
    System.out.println("Score: " + match.score());
    System.out.println("Text: " + match.embedded().text());
});
```

### 支持的向量数据库

| 数据库 | 依赖 |
|--------|------|
| Chroma | `langchain4j-chroma` |
| Pinecone | `langchain4j-pinecone` |
| Milvus | `langchain4j-milvus` |
| Weaviate | `langchain4j-weaviate` |
| Qdrant | `langchain4j-qdrant` |
| PGVector | `langchain4j-pgvector` |
| Elasticsearch | `langchain4j-elasticsearch` |
| MongoDB Atlas | `langchain4j-mongodb-atlas` |
| Redis | `langchain4j-redis` |

### 使用 PGVector 示例

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-pgvector</artifactId>
    <version>1.11.0-beta19-SNAPSHOT</version>
</dependency>
```

```java
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

EmbeddingStore<TextSegment> store = PgVectorEmbeddingStore.builder()
        .host("localhost")
        .port(5432)
        .database("vector_db")
        .user("postgres")
        .password("password")
        .table("embeddings")
        .dimension(1536)
        .build();
```

---

## Document Processing - 文档处理

### 文档加载

```java
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;

// 加载单个文件
Document document = FileSystemDocumentLoader.loadDocument("path/to/file.txt");

// 加载目录下所有文件
List<Document> documents = FileSystemDocumentLoader.loadDocuments("path/to/directory");

// 从 URL 加载
Document webDoc = UrlDocumentLoader.load("https://example.com/page.html");

// 从 classpath 加载
Document classpathDoc = ClassPathDocumentLoader.loadDocument("documents/guide.txt");
```

### 文档解析器

不同类型文件需要不同的解析器：

```xml
<!-- PDF 解析 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-document-parser-apache-pdfbox</artifactId>
    <version>1.11.0-beta19-SNAPSHOT</version>
</dependency>

<!-- Office 文档解析 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-document-parser-apache-poi</artifactId>
    <version>1.11.0-beta19-SNAPSHOT</version>
</dependency>
```

```java
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;

// PDF 解析
Document pdfDoc = FileSystemDocumentLoader.loadDocument(
    "document.pdf",
    new ApachePdfBoxDocumentParser()
);

// Word/Excel 解析
Document wordDoc = FileSystemDocumentLoader.loadDocument(
    "document.docx",
    new ApachePoiDocumentParser()
);
```

### 文档分割

```java
import dev.langchain4j.data.document.splitter.DocumentSplitters;

// 递归字符分割（推荐）
DocumentSplitter splitter = DocumentSplitters.recursive(
    500,  // 每个片段的最大字符数
    50    // 片段之间的重叠字符数
);

List<TextSegment> segments = splitter.split(document);

// 其他分割器
DocumentSplitter bySentence = new DocumentBySentenceSplitter(500, 50);
DocumentSplitter byParagraph = new DocumentByParagraphSplitter(500, 50);
DocumentSplitter byLine = new DocumentByLineSplitter(500, 50);
```

---

## 支持的模型提供商

### OpenAI

```java
// Chat 模型
OpenAiChatModel chatModel = OpenAiChatModel.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("gpt-4o-mini") // gpt-4o, gpt-4-turbo, gpt-3.5-turbo 等
        .temperature(0.7)
        .build();

// Embedding 模型
OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
        .apiKey(System.getenv("OPENAI_API_KEY"))
        .modelName("text-embedding-3-small")
        .build();
```

### Azure OpenAI

```java
AzureOpenAiChatModel model = AzureOpenAiChatModel.builder()
        .endpoint("https://your-resource.openai.azure.com/")
        .apiKey(System.getenv("AZURE_OPENAI_KEY"))
        .deploymentName("gpt-4")
        .build();
```

### Anthropic Claude

```java
AnthropicChatModel model = AnthropicChatModel.builder()
        .apiKey(System.getenv("ANTHROPIC_API_KEY"))
        .modelName("claude-3-5-sonnet-20241022")
        .build();
```

### Google Gemini

```java
GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
        .apiKey(System.getenv("GOOGLE_API_KEY"))
        .modelName("gemini-1.5-pro")
        .build();
```

### Ollama (本地模型)

```java
OllamaChatModel model = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("llama3")
        .build();
```

### 其他支持的提供商

- AWS Bedrock
- Hugging Face
- Mistral AI
- Cohere
- IBM watsonx
- Vertex AI
- 更多...

---

## 高级特性

### Guardrails - 安全护栏

```java
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;

// 输入护栏 - 检查用户输入
class ContentModerationGuardrail implements InputGuardrail {
    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        String userMessage = request.userMessage().text();
        if (containsBadContent(userMessage)) {
            return InputGuardrailResult.failure("内容包含不当信息");
        }
        return InputGuardrailResult.success();
    }
}

// 输出护栏 - 检查 LLM 输出
class OutputValidationGuardrail implements OutputGuardrail {
    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        String response = request.aiMessage().text();
        if (!isValid(response)) {
            return OutputGuardrailResult.retry("请重新生成更合适的回答");
        }
        return OutputGuardrailResult.success();
    }
}

// 使用护栏
interface SafeAssistant {
    String chat(String message);
}

SafeAssistant assistant = AiServices.builder(SafeAssistant.class)
        .chatModel(model)
        .inputGuardrails(new ContentModerationGuardrail())
        .outputGuardrails(new OutputValidationGuardrail())
        .build();
```

### MCP (Model Context Protocol)

```java
import dev.langchain4j.mcp.McpToolProvider;

// 连接 MCP 服务器
McpToolProvider toolProvider = McpToolProvider.builder()
        .mcpTransport(new StdioMcpTransport("npx", "-y", "@modelcontextprotocol/server-filesystem"))
        .build();

interface McpAssistant {
    String chat(String message);
}

McpAssistant assistant = AiServices.builder(McpAssistant.class)
        .chatModel(model)
        .toolProvider(toolProvider)
        .build();
```

### 多模态支持

```java
// 图片理解
UserMessage messageWithImage = UserMessage.from(
    TextContent.from("这张图片里有什么？"),
    ImageContent.from("https://example.com/image.jpg")
);

ChatResponse response = model.chat(messageWithImage);
```

### 并发工具执行

```java
Assistant assistant = AiServices.builder(Assistant.class)
        .chatModel(model)
        .tools(tool1, tool2, tool3)
        .executeToolsConcurrently() // 启用并发执行
        .build();
```

---

## Spring Boot 集成

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>1.11.0-beta19-SNAPSHOT</version>
</dependency>
```

```yaml
# application.yml
langchain4j:
  open-ai:
    chat-model:
      api-key: ${OPENAI_API_KEY}
      model-name: gpt-4o-mini
```

```java
@RestController
public class ChatController {

    @Autowired
    private ChatModel chatModel;

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatModel.chat(message);
    }
}
```

---

## 最佳实践

### 1. 环境变量管理 API Key

```java
String apiKey = System.getenv("OPENAI_API_KEY");
```

### 2. 使用适当的 Token 限制

```java
OpenAiChatModel model = OpenAiChatModel.builder()
        .maxTokens(1000) // 限制输出长度
        .build();
```

### 3. 实现重试机制

```java
OpenAiChatModel model = OpenAiChatModel.builder()
        .maxRetries(3) // 失败时重试
        .timeout(Duration.ofSeconds(60))
        .build();
```

### 4. 日志记录

```java
OpenAiChatModel model = OpenAiChatModel.builder()
        .logRequests(true)
        .logResponses(true)
        .build();
```

### 5. 合理设置记忆窗口

```java
ChatMemory memory = MessageWindowChatMemory.builder()
        .maxMessages(20) // 避免 token 超限
        .build();
```

---

## 常见问题

### Q: 如何处理 Rate Limit？

```java
// LangChain4j 内置了重试机制
OpenAiChatModel model = OpenAiChatModel.builder()
        .maxRetries(5)
        .build();
```

### Q: 如何使用代理？

```java
OpenAiChatModel model = OpenAiChatModel.builder()
        .baseUrl("https://your-proxy.com/v1")
        .build();
```

### Q: 如何在生产环境中监控？

使用 `ChatModelListener` 监听请求和响应：

```java
ChatModelListener listener = new ChatModelListener() {
    @Override
    public void onRequest(ChatModelRequestContext context) {
        log.info("Request: {}", context.chatRequest());
    }

    @Override
    public void onResponse(ChatModelResponseContext context) {
        log.info("Response: {}", context.chatResponse());
    }
};
```

---

## 更多资源

- **官方文档**: https://docs.langchain4j.dev
- **示例代码**: https://github.com/langchain4j/langchain4j-examples
- **Discord 社区**: https://discord.gg/JzTFvyjG6R
- **GitHub Issues**: https://github.com/langchain4j/langchain4j/issues

---

本文档覆盖了 LangChain4j 的核心功能。如需了解更多高级特性，请参考官方文档或示例代码仓库。
