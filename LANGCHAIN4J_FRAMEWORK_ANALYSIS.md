# LangChain4j 框架深度分析文档

## 1. 框架概述

### 1.1 什么是 LangChain4j？

LangChain4j 是一个用于简化在 Java 应用程序中集成大语言模型(LLM)的开源框架。它从 2023 年初开始开发，受到 Python 生态中 LangChain、Haystack、LlamaIndex 等项目的启发，专门为 Java 开发者提供了构建 LLM 驱动应用的能力。

**版本**: 1.11.0-beta19-SNAPSHOT

### 1.2 核心设计理念

1. **统一 API**: 为不同的 LLM 提供商和向量存储提供统一的接口，避免开发者学习各种专有 API
2. **全面的工具箱**: 从低级的提示模板、聊天记忆管理到高级的 Agent 和 RAG 模式
3. **丰富的示例**: 提供大量使用案例帮助快速入门

---

## 2. 项目结构

### 2.1 模块分类

```
langchain4j/
├── langchain4j-core/           # 核心模块 - 定义所有核心接口和抽象
├── langchain4j/                # 主模块 - 包含 AiServices、文档处理、内存管理等
├── langchain4j-bom/            # Maven BOM 依赖管理
│
├── 【LLM 提供商集成】
│   ├── langchain4j-open-ai/           # OpenAI
│   ├── langchain4j-anthropic/         # Anthropic (Claude)
│   ├── langchain4j-azure-open-ai/     # Azure OpenAI
│   ├── langchain4j-bedrock/           # AWS Bedrock
│   ├── langchain4j-google-ai-gemini/  # Google Gemini
│   ├── langchain4j-vertex-ai*/        # Google Vertex AI
│   ├── langchain4j-ollama/            # Ollama (本地部署)
│   ├── langchain4j-mistral-ai/        # Mistral AI
│   ├── langchain4j-hugging-face/      # Hugging Face
│   ├── langchain4j-cohere/            # Cohere
│   └── ... (20+ 提供商)
│
├── 【向量存储/Embedding Store】
│   ├── langchain4j-pgvector/          # PostgreSQL pgvector
│   ├── langchain4j-pinecone/          # Pinecone
│   ├── langchain4j-milvus/            # Milvus
│   ├── langchain4j-chroma/            # Chroma
│   ├── langchain4j-qdrant/            # Qdrant
│   ├── langchain4j-elasticsearch/     # Elasticsearch
│   ├── langchain4j-mongodb-atlas/     # MongoDB Atlas
│   ├── langchain4j-weaviate/          # Weaviate
│   └── ... (30+ 存储)
│
├── 【本地 Embedding 模型】
│   └── embeddings/
│       ├── langchain4j-embeddings-all-minilm-l6-v2/
│       ├── langchain4j-embeddings-bge-small-*/
│       └── langchain4j-embeddings-e5-small-v2/
│
├── 【文档处理】
│   ├── document-loaders/              # 文档加载器
│   │   ├── langchain4j-document-loader-amazon-s3/
│   │   ├── langchain4j-document-loader-github/
│   │   └── langchain4j-document-loader-selenium/
│   ├── document-parsers/              # 文档解析器
│   │   ├── langchain4j-document-parser-apache-pdfbox/
│   │   ├── langchain4j-document-parser-apache-poi/
│   │   └── langchain4j-document-parser-apache-tika/
│   └── document-transformers/         # 文档转换器
│
├── 【其他功能模块】
│   ├── langchain4j-mcp/               # Model Context Protocol
│   ├── langchain4j-easy-rag/          # 简化 RAG 配置
│   ├── langchain4j-guardrails/        # 护栏功能
│   ├── web-search-engines/            # Web 搜索引擎集成
│   ├── code-execution-engines/        # 代码执行引擎
│   └── langchain4j-agentic*/          # Agent 模式
│
└── integration-tests/                  # 集成测试
```

---

## 3. 核心接口和类

### 3.1 模型层接口 (`langchain4j-core`)

#### 3.1.1 ChatModel - 聊天模型接口

```java
// 位置: langchain4j-core/src/main/java/dev/langchain4j/model/chat/ChatModel.java

public interface ChatModel {
    // 主要 API - 发送聊天请求
    default ChatResponse chat(ChatRequest chatRequest);

    // 便捷方法
    default String chat(String userMessage);
    default ChatResponse chat(ChatMessage... messages);
    default ChatResponse chat(List<ChatMessage> messages);

    // 默认请求参数
    default ChatRequestParameters defaultRequestParameters();

    // 模型能力
    default Set<Capability> supportedCapabilities();
}
```

#### 3.1.2 StreamingChatModel - 流式聊天模型

```java
// 位置: langchain4j-core/src/main/java/dev/langchain4j/model/chat/StreamingChatModel.java

public interface StreamingChatModel {
    // 主要 API - 流式响应
    default void chat(ChatRequest chatRequest, StreamingChatResponseHandler handler);

    // 流式响应处理器支持:
    // - onPartialResponse: 部分文本响应
    // - onPartialThinking: 思考过程 (如 Claude)
    // - onPartialToolCall: 工具调用
    // - onCompleteResponse: 完整响应
    // - onError: 错误处理
}
```

#### 3.1.3 EmbeddingModel - 嵌入模型

```java
// 位置: langchain4j-core/src/main/java/dev/langchain4j/model/embedding/EmbeddingModel.java

public interface EmbeddingModel {
    // 嵌入单个文本
    default Response<Embedding> embed(String text);
    default Response<Embedding> embed(TextSegment textSegment);

    // 批量嵌入
    Response<List<Embedding>> embedAll(List<TextSegment> textSegments);

    // 获取嵌入维度
    default int dimension();
}
```

### 3.2 消息类型 (`dev.langchain4j.data.message`)

```java
public interface ChatMessage {
    ChatMessageType type();
}

// 具体消息类型:
- SystemMessage     // 系统消息 - 定义 AI 行为
- UserMessage       // 用户消息 - 支持文本、图片、音频、视频等多模态内容
- AiMessage         // AI 响应消息 - 支持工具调用
- ToolExecutionResultMessage  // 工具执行结果
- CustomMessage     // 自定义消息
```

### 3.3 存储层接口

#### 3.3.1 EmbeddingStore - 向量存储

```java
// 位置: langchain4j-core/src/main/java/dev/langchain4j/store/embedding/EmbeddingStore.java

public interface EmbeddingStore<Embedded> {
    // 添加嵌入
    String add(Embedding embedding);
    void add(String id, Embedding embedding);
    String add(Embedding embedding, Embedded embedded);

    // 批量添加
    List<String> addAll(List<Embedding> embeddings);
    List<String> addAll(List<Embedding> embeddings, List<Embedded> embedded);

    // 删除
    void remove(String id);
    void removeAll(Collection<String> ids);
    void removeAll(Filter filter);
    void removeAll();

    // 搜索 - 核心功能
    EmbeddingSearchResult<Embedded> search(EmbeddingSearchRequest request);
}
```

#### 3.3.2 ChatMemory - 聊天记忆

```java
// 位置: langchain4j-core/src/main/java/dev/langchain4j/memory/ChatMemory.java

public interface ChatMemory {
    Object id();                        // 记忆 ID
    void add(ChatMessage message);      // 添加消息
    List<ChatMessage> messages();       // 获取所有消息
    void clear();                       // 清除记忆
}

// 实现类:
- MessageWindowChatMemory  // 滑动窗口 - 按消息数量限制
- TokenWindowChatMemory    // Token 窗口 - 按 Token 数量限制
```

### 3.4 工具系统 (Function Calling)

```java
// 位置: langchain4j-core/src/main/java/dev/langchain4j/agent/tool/Tool.java

@Retention(RUNTIME)
@Target({METHOD})
public @interface Tool {
    String name() default "";           // 工具名称
    String[] value() default "";        // 工具描述
    ReturnBehavior returnBehavior() default ReturnBehavior.TO_LLM;
    String metadata() default "{}";     // LLM 提供商特定元数据
}

// ToolSpecification - 工具规格描述
public class ToolSpecification {
    private final String name;
    private final String description;
    private final JsonObjectSchema parameters;
    private final Map<String, Object> metadata;
}
```

---

## 4. AiServices - 高级 API

### 4.1 设计理念

AiServices 是 LangChain4j 的高级 API，它允许开发者定义一个 Java 接口，框架自动生成实现。

```java
// 位置: langchain4j/src/main/java/dev/langchain4j/service/AiServices.java

// 简单示例
interface Assistant {
    String chat(String userMessage);
}

Assistant assistant = AiServices.create(Assistant.class, model);
String answer = assistant.chat("hello");
```

### 4.2 支持的功能

1. **系统消息模板**: `@SystemMessage` 注解
2. **用户消息模板**: `@UserMessage` 注解
3. **聊天记忆**: `.chatMemory()` 或 `.chatMemoryProvider()`
4. **RAG**: `.contentRetriever()` 或 `.retrievalAugmentor()`
5. **工具调用**: `.tools()` 配置 `@Tool` 注解的方法
6. **输出解析**: 支持多种返回类型 (String, POJO, Enum, List 等)
7. **流式响应**: 返回 `TokenStream` 类型
8. **内容审核**: `@Moderate` 注解
9. **护栏**: 输入/输出护栏验证

### 4.3 Builder 模式配置

```java
AiServices.builder(MyAssistant.class)
    .chatModel(chatModel)                    // 或 .streamingChatModel()
    .chatMemory(chatMemory)                  // 单一记忆
    .chatMemoryProvider(provider)            // 多用户记忆
    .contentRetriever(retriever)             // RAG
    .tools(toolObject)                       // 工具
    .toolProvider(toolProvider)              // 动态工具
    .moderationModel(moderationModel)        // 内容审核
    .inputGuardrails(guardrails)             // 输入护栏
    .outputGuardrails(guardrails)            // 输出护栏
    .build();
```

---

## 5. RAG (检索增强生成) 架构

### 5.1 整体流程

```
┌──────────────────────────────────────────────────────────────────┐
│                    DefaultRetrievalAugmentor                      │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  UserMessage ──► QueryTransformer ──► Query(s)                   │
│                       │                                           │
│                       ▼                                           │
│               QueryRouter ──► ContentRetriever(s)                │
│                       │                                           │
│                       ▼                                           │
│               ContentAggregator                                   │
│                       │                                           │
│                       ▼                                           │
│               ContentInjector ──► Augmented UserMessage          │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### 5.2 核心组件

```java
// 1. RetrievalAugmentor - 入口点
public interface RetrievalAugmentor {
    AugmentationResult augment(AugmentationRequest augmentationRequest);
}

// 2. QueryTransformer - 查询转换
public interface QueryTransformer {
    Collection<Query> transform(Query query);
}
// 实现: DefaultQueryTransformer, ExpandingQueryTransformer, CompressingQueryTransformer

// 3. QueryRouter - 查询路由
public interface QueryRouter {
    Collection<ContentRetriever> route(Query query);
}
// 实现: DefaultQueryRouter, LanguageModelQueryRouter

// 4. ContentRetriever - 内容检索
public interface ContentRetriever {
    List<Content> retrieve(Query query);
}
// 实现: EmbeddingStoreContentRetriever, WebSearchContentRetriever

// 5. ContentAggregator - 内容聚合
public interface ContentAggregator {
    List<Content> aggregate(Map<Query, Collection<List<Content>>> queryToContents);
}
// 实现: DefaultContentAggregator, ReRankingContentAggregator

// 6. ContentInjector - 内容注入
public interface ContentInjector {
    ChatMessage inject(List<Content> contents, ChatMessage chatMessage);
}
```

---

## 6. LLM 提供商集成

### 6.1 支持的提供商 (20+)

| 提供商 | 模块 | 说明 |
|--------|------|------|
| OpenAI | langchain4j-open-ai | GPT-4, GPT-3.5 等 |
| Anthropic | langchain4j-anthropic | Claude 系列 |
| Azure OpenAI | langchain4j-azure-open-ai | Azure 托管 OpenAI |
| AWS Bedrock | langchain4j-bedrock | 多模型托管 |
| Google Gemini | langchain4j-google-ai-gemini | Gemini 系列 |
| Vertex AI | langchain4j-vertex-ai* | Google Cloud AI |
| Ollama | langchain4j-ollama | 本地部署开源模型 |
| Mistral AI | langchain4j-mistral-ai | Mistral 系列 |
| Hugging Face | langchain4j-hugging-face | 开源模型 Hub |
| Cohere | langchain4j-cohere | 嵌入和重排模型 |
| JLama | langchain4j-jlama | 纯 Java LLM (JDK 21+) |
| Local AI | langchain4j-local-ai | 本地 AI 服务 |
| Workers AI | langchain4j-workers-ai | Cloudflare AI |
| WatsonX | langchain4j-watsonx | IBM Watson |
| OVH AI | langchain4j-ovh-ai | OVH 云 AI |

### 6.2 典型实现结构 (以 OpenAI 为例)

```java
// OpenAiChatModel - 同步聊天
OpenAiChatModel.builder()
    .apiKey("...")
    .modelName("gpt-4")
    .temperature(0.7)
    .build();

// OpenAiStreamingChatModel - 流式聊天
// OpenAiEmbeddingModel - 嵌入模型
// OpenAiImageModel - 图像生成
// OpenAiModerationModel - 内容审核
// OpenAiAudioTranscriptionModel - 语音转文本
```

---

## 7. 向量存储集成

### 7.1 支持的存储 (30+)

| 类型 | 存储 | 模块 |
|------|------|------|
| 专用向量数据库 | Pinecone, Milvus, Qdrant, Chroma, Weaviate, Vespa | langchain4j-* |
| 数据库扩展 | PostgreSQL (pgvector), Elasticsearch, OpenSearch | langchain4j-* |
| 云服务 | Azure AI Search, MongoDB Atlas | langchain4j-azure-*, langchain4j-mongodb-* |
| 分布式存储 | Cassandra, Couchbase, Infinispan | langchain4j-* |
| 关系数据库 | MariaDB, Oracle | langchain4j-* |

### 7.2 InMemoryEmbeddingStore

```java
// 内置的内存向量存储，适合开发和测试
InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
store.add(embedding, textSegment);

EmbeddingSearchResult<TextSegment> result = store.search(
    EmbeddingSearchRequest.builder()
        .queryEmbedding(queryEmbedding)
        .maxResults(5)
        .minScore(0.7)
        .filter(filter)
        .build()
);
```

---

## 8. 文档处理管道

### 8.1 完整流程

```
Document Source ──► Document Loader ──► Document ──► Document Splitter ──► TextSegments
                                                            │
                                                            ▼
EmbeddingStore ◄── Embeddings ◄── EmbeddingModel ◄─── TextSegments
```

### 8.2 关键组件

```java
// 1. DocumentLoader - 文档加载
FileSystemDocumentLoader.loadDocument("path/to/file.pdf");
ClassPathDocumentLoader.loadDocument("classpath:document.txt");

// 2. DocumentParser - 文档解析
new ApachePdfBoxDocumentParser()
new ApachePoiDocumentParser()  // Word, Excel
new ApacheTikaDocumentParser() // 通用解析

// 3. DocumentSplitter - 文档分割
DocumentSplitters.recursive(500, 50);  // 递归分割
new DocumentByParagraphSplitter(500, 50);
new DocumentBySentenceSplitter(500, 50);

// 4. TextSegment - 文本片段
TextSegment.from("text content", Metadata.from("key", "value"));
```

---

## 9. 高级特性

### 9.1 护栏 (Guardrails)

```java
// 输入护栏 - 验证用户输入
public interface InputGuardrail {
    InputGuardrailResult validate(InputGuardrailRequest request);
}

// 输出护栏 - 验证模型输出
public interface OutputGuardrail {
    OutputGuardrailResult validate(OutputGuardrailRequest request);
}

// 配置
AiServices.builder(MyService.class)
    .inputGuardrails(promptInjectionGuardrail)
    .outputGuardrails(toxicityGuardrail)
    .build();
```

### 9.2 MCP (Model Context Protocol)

```java
// langchain4j-mcp 模块支持 MCP 协议
// 允许 LLM 与外部工具和数据源交互
```

### 9.3 Agent 模式

```java
// langchain4j-agentic 模块
// 支持多 Agent 协作和 A2A (Agent-to-Agent) 通信
```

### 9.4 监听器和可观测性

```java
// ChatModelListener - 模型调用监听
public interface ChatModelListener {
    void onRequest(ChatModelRequestContext context);
    void onResponse(ChatModelResponseContext context);
    void onError(ChatModelErrorContext context);
}

// AiServiceListener - AI 服务事件监听
```

---

## 10. 快速入门示例

### 10.1 简单聊天

```java
// 依赖: langchain4j-open-ai
ChatModel model = OpenAiChatModel.withApiKey("sk-...");
String answer = model.chat("What is Java?");
```

### 10.2 带记忆的对话

```java
ChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);

interface Assistant {
    String chat(String message);
}

Assistant assistant = AiServices.builder(Assistant.class)
    .chatModel(model)
    .chatMemory(memory)
    .build();

assistant.chat("My name is Alice");
assistant.chat("What is my name?"); // "Your name is Alice"
```

### 10.3 RAG 示例

```java
// 1. 创建向量存储和嵌入模型
EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
EmbeddingModel embeddingModel = OpenAiEmbeddingModel.withApiKey("...");

// 2. 加载和嵌入文档
Document document = FileSystemDocumentLoader.loadDocument("knowledge.txt");
List<TextSegment> segments = DocumentSplitters.recursive(500, 50).split(document);
List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
store.addAll(embeddings, segments);

// 3. 创建 ContentRetriever
ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
    .embeddingStore(store)
    .embeddingModel(embeddingModel)
    .maxResults(3)
    .minScore(0.7)
    .build();

// 4. 配置 AI Service
interface Expert {
    String answer(String question);
}

Expert expert = AiServices.builder(Expert.class)
    .chatModel(chatModel)
    .contentRetriever(retriever)
    .build();

String answer = expert.answer("What does the document say about X?");
```

### 10.4 工具调用

```java
class Calculator {
    @Tool("Calculate the sum of two numbers")
    double add(@P("first number") double a, @P("second number") double b) {
        return a + b;
    }
}

interface MathAssistant {
    String chat(String message);
}

MathAssistant assistant = AiServices.builder(MathAssistant.class)
    .chatModel(model)
    .tools(new Calculator())
    .build();

assistant.chat("What is 17 + 25?"); // 调用 Calculator.add()
```

---

## 11. 框架集成

### 11.1 Spring Boot

```xml
<!-- 使用 langchain4j-spring-boot-starter -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
</dependency>
```

### 11.2 Quarkus

```xml
<!-- 使用 quarkus-langchain4j -->
<dependency>
    <groupId>io.quarkiverse.langchain4j</groupId>
    <artifactId>quarkus-langchain4j-openai</artifactId>
</dependency>
```

### 11.3 Micronaut / Helidon

也有对应的集成模块。

---

## 12. 架构总结

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              应用层                                          │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                         AiServices                                   │   │
│   │  - 声明式接口定义                                                     │   │
│   │  - 自动参数映射                                                       │   │
│   │  - 输出解析                                                          │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────────────────┤
│                              能力层                                          │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│   │   Memory     │  │     RAG      │  │    Tools     │  │  Guardrails  │   │
│   │  ChatMemory  │  │ Retrieval    │  │ @Tool 注解   │  │ Input/Output │   │
│   │  Provider    │  │ Augmentor    │  │ ToolProvider │  │  Validation  │   │
│   └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
├─────────────────────────────────────────────────────────────────────────────┤
│                              核心抽象层                                      │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│   │  ChatModel   │  │ Embedding    │  │ Embedding    │  │  Document    │   │
│   │  Streaming   │  │    Model     │  │    Store     │  │  Processing  │   │
│   │  ChatModel   │  │              │  │              │  │              │   │
│   └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
├─────────────────────────────────────────────────────────────────────────────┤
│                              集成层                                          │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │  OpenAI │ Anthropic │ Azure │ Bedrock │ Gemini │ Ollama │ ...      │   │
│   │  Pinecone │ Milvus │ PGVector │ Elasticsearch │ Chroma │ ...       │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 13. 学习路径建议

1. **入门**: 从 `langchain4j-core` 了解核心接口
2. **实践**: 使用 `AiServices` 构建简单聊天应用
3. **进阶**: 学习 RAG 架构和向量存储
4. **深入**: 研究 Tool 系统和 Agent 模式
5. **扩展**: 探索护栏、MCP 和自定义组件

---

*文档生成时间: 2024-12-30*
*框架版本: 1.11.0-beta19-SNAPSHOT*
