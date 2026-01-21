# RAG 功能使用指南

## 概述

已成功补全 `RagServiceImpl` 的 `query` 方法，现在可以完整使用 RAG（检索增强生成）功能。项目已通过编译并构建成功。

## 主要完成的工作

### 1. 配置类 (LangChain4jConfig.java)
- 配置了 `EmbeddingModel`: 使用 OpenAI 的 text-embedding-ada-002 模型
- 配置了 `EmbeddingStore<TextSegment>`: 使用内存向量存储

### 2. 完善 RagServiceImpl
- **query 方法**: 完整实现了 RAG 查询流程
  - 将查询转换为向量
  - 在向量存储中检索相似文档
  - 构建提示词并生成回答
  - 返回结构化结果（包含答案和可选的来源信息）

- **loadDocumentFromPath 方法**: 实现文档加载功能
  - 从指定路径加载文档
  - 使用文档分割器切分文档（500字符/片段，100字符重叠）
  - 将文档向量化并存储到向量数据库

### 3. 更新 Controller
- 添加了 `POST /api/rag/load` 端点用于加载文档

### 4. 更新依赖
添加了必要的依赖：
- `langchain4j-embeddings-all-minilm-l6-v2`: 本地 embedding 模型
- `langchain4j-document-parser-apache-tika`: 文档解析器

## API 使用说明

### 1. 加载文档
```bash
curl -X POST http://localhost:8080/api/rag/load \
  -H "Content-Type: application/json" \
  -d '{
    "path": "/absolute/path/to/documents"
  }'
```

响应：
```json
{
  "success": true,
  "message": "Documents loaded successfully"
}
```

### 2. RAG 查询
```bash
curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "什么是 LangChain4j？",
    "topK": 5,
    "minScore": 0.7,
    "includeSource": true
  }'
```

请求参数说明：
- `query`: 用户的查询问题（必填）
- `topK`: 返回的相似文档数量（可选，默认 5）
- `minScore`: 相似度阈值，0-1 之间（可选，默认 0.7）
- `includeSource`: 是否在响应中包含来源文档（可选，默认 false）

响应示例：
```json
{
  "success": true,
  "answer": "LangChain4j 是一个用于在 Java 应用程序中集成大型语言模型的框架...",
  "sources": [
    {
      "content": "LangChain4j 是一个用于在 Java 应用程序中集成大型语言模型（LLM）的框架。",
      "score": 0.89,
      "source": "Unknown"
    }
  ]
}
```

## 测试步骤

1. **启动应用**
```bash
cd /Users/wangyubj15/project/langchain4j-demo/langchain4j-demo-core
mvn spring-boot:run
```

2. **加载测试文档**
```bash
curl -X POST http://localhost:8080/api/rag/load \
  -H "Content-Type: application/json" \
  -d '{
    "path": "/Users/wangyubj15/project/langchain4j-demo/test-documents"
  }'
```

3. **测试查询**
```bash
curl -X POST http://localhost:8080/api/rag/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "LangChain4j 有哪些主要特性？",
    "topK": 3,
    "minScore": 0.7,
    "includeSource": true
  }'
```

## 配置说明

### application.yml
```yaml
langchain4j:
  open-ai:
    embedding-model:
      base-url: http://langchain4j.dev/demo/openai/v1
      api-key: demo
      model-name: text-embedding-3-small  # Demo API 只支持此模型
      timeout: 60s
```

**重要提示：** 如果使用 LangChain4j 官方的 demo API，必须使用 `text-embedding-3-small` 模型。

如果要使用真实的 OpenAI API，可以修改为：
```yaml
langchain4j:
  open-ai:
    embedding-model:
      base-url: https://api.openai.com/v1
      api-key: your-actual-api-key
      model-name: text-embedding-3-small  # 或 text-embedding-ada-002, text-embedding-3-large
```

## 生产环境建议

1. **使用持久化向量数据库**
   当前使用 `InMemoryEmbeddingStore`，生产环境建议使用：
   - Pinecone
   - Weaviate
   - Milvus
   - Qdrant
   - Chroma

2. **调整文档分割参数**
   根据实际文档特点调整 `DocumentSplitters.recursive()` 的参数：
   - `maxSegmentSize`: 每个片段的最大字符数
   - `maxOverlapSize`: 片段之间的重叠字符数

3. **优化检索参数**
   - `topK`: 根据应用场景调整返回的文档数量
   - `minScore`: 调整相似度阈值以平衡准确性和召回率

4. **添加文档元数据**
   在加载文档时添加元数据（如来源、作者、时间等），便于追溯和过滤

5. **实现增量更新**
   添加文档更新和删除功能，而不是每次都重新加载所有文档

## 注意事项

- 内存向量存储在服务重启后会丢失数据
- 确保文档路径有正确的读取权限
- 大量文档加载可能需要较长时间
- embedding 模型调用可能会产生费用（如使用 OpenAI API）
