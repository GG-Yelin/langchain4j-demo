# Demo API Token 限制说明

## 问题背景

使用 LangChain4j 官方的 Demo API 时，有以下限制：
- **每次请求最多 10000 tokens**
- 超过此限制会报错：`Maximum number of tokens per request for demonstration purposes is 10000`

你的 PDF 文档（阿里巴巴Java开发规范）约 2MB，包含大量文本，如果全部加载会远超 10000 tokens 限制。

## 解决方案

已实现**智能截断机制**，只加载文档的前 20000 个字符（约 5000-6000 tokens），确保不超过限制。

### 实现策略

1. **字符限制**: 20000 个字符
   - 1 token ≈ 4 个字符（英文）
   - 1 token ≈ 1.5-2 个字符（中文）
   - 20000 字符 ≈ 5000-6000 tokens（留有安全余量）

2. **分批处理**: 每批 10 个片段
   - 避免单次请求过大
   - 逐批生成 embedding
   - 显示处理进度

3. **截断策略**:
   - 优先加载文档开头内容
   - 达到限制后停止加载
   - 记录截断日志

## 代码实现

```java
// 限制文档大小
final int MAX_CHARS = 20000;

// 截断文档内容
StringBuilder combinedText = new StringBuilder();
int totalChars = 0;

for (Document doc : documents) {
    String text = doc.text();
    if (totalChars + text.length() <= MAX_CHARS) {
        combinedText.append(text).append("\n\n");
        totalChars += text.length();
    } else {
        // 只添加剩余的字符数
        int remainingChars = MAX_CHARS - totalChars;
        if (remainingChars > 0) {
            combinedText.append(text.substring(0, remainingChars));
        }
        break;
    }
}

// 分批处理片段
int batchSize = 10;
for (int i = 0; i < segments.size(); i += batchSize) {
    List<TextSegment> batch = segments.subList(i, end);
    // 逐个生成 embedding
    for (TextSegment segment : batch) {
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);
    }
}
```

## 使用效果

### 加载日志示例

```
2026-01-21 15:35:00 - Loading documents from classpath:knowledge directory (with token limit for demo API)
2026-01-21 15:35:00 - Knowledge directory path: /path/to/knowledge
2026-01-21 15:35:01 - Loaded 1 documents
2026-01-21 15:35:01 - Total characters loaded: 20000 (limit: 20000)
2026-01-21 15:35:01 - Split into 35 segments
2026-01-21 15:35:02 - Processing batch 1/4 (10 segments)
2026-01-21 15:35:05 - Processed 10/35 segments
2026-01-21 15:35:05 - Processing batch 2/4 (10 segments)
2026-01-21 15:35:08 - Processed 20/35 segments
2026-01-21 15:35:08 - Processing batch 3/4 (10 segments)
2026-01-21 15:35:11 - Processed 30/35 segments
2026-01-21 15:35:11 - Processing batch 4/4 (5 segments)
2026-01-21 15:35:13 - Processed 35/35 segments
2026-01-21 15:35:13 - Documents ingested successfully from knowledge directory (limited to 20000 chars)
```

### 前端显示

加载成功后会显示：
```
✅ 已成功从 knowledge 目录加载文档
```

## 测试步骤

1. **启动服务**
   ```bash
   cd langchain4j-demo-core
   mvn spring-boot:run
   ```

2. **加载文档**
   - 打开前端 http://localhost:5173
   - 点击"📚 加载文档到向量库"
   - 点击"开始加载"
   - 观察控制台日志，应该显示截断信息

3. **验证问答**
   - 切换到"RAG 问答"模式
   - 提问文档开头部分的内容
   - 例如: "Java 命名规范有哪些？"（通常在规范文档开头）

## 限制说明

### ⚠️ 只能加载部分内容

由于 token 限制，只能加载文档的**前 20000 个字符**（约占完整文档的一小部分）。

**影响：**
- ✅ 可以回答文档开头部分的问题
- ❌ 无法回答文档后半部分的问题
- ❌ 无法获取完整的文档知识

### 📊 内容估算

假设 PDF 有 100 页：
- 完整文档: 约 200,000 字符
- 加载限制: 20,000 字符
- 实际加载: 约 10% 的内容

### 🎯 适用场景

这个限制版本适合：
- ✅ **测试和演示** - 验证 RAG 功能是否正常
- ✅ **开发调试** - 快速测试代码逻辑
- ✅ **原型验证** - 展示 RAG 概念

**不适合：**
- ❌ 生产环境
- ❌ 完整文档问答
- ❌ 大规模知识库

## 如何突破限制

如果需要加载完整文档，有以下方案：

### 方案 1: 使用自己的 OpenAI API Key

```yaml
# application.yml
langchain4j:
  open-ai:
    embedding-model:
      base-url: https://api.openai.com/v1
      api-key: sk-your-real-api-key-here
      model-name: text-embedding-3-small
```

**优势：**
- ✅ 无 token 限制
- ✅ 更快的响应速度
- ✅ 更稳定的服务

**成本：**
- text-embedding-3-small: $0.02 / 1M tokens
- 100 页文档 ≈ 50,000 tokens ≈ $0.001

### 方案 2: 使用本地 Embedding 模型

```xml
<!-- pom.xml -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-embeddings-all-minilm-l6-v2</artifactId>
    <version>1.0.0-beta3</version>
</dependency>
```

```java
// LangChain4jConfig.java
@Bean
public EmbeddingModel embeddingModel() {
    return new AllMiniLmL6V2EmbeddingModel();
}
```

**优势：**
- ✅ 完全免费
- ✅ 无 token 限制
- ✅ 数据隐私

**劣势：**
- ❌ 首次加载较慢（下载模型）
- ❌ 内存占用较大（约 100MB）
- ❌ Embedding 质量略低于 OpenAI

### 方案 3: 分割文档

将大文档分割成小文件：

```
knowledge/
├── java-naming-rules.txt      (前 10 页)
├── java-constant-rules.txt    (第 11-20 页)
├── java-code-format.txt       (第 21-30 页)
...
```

每次只加载需要的部分。

## 配置参数

可以根据需要调整限制：

```java
// RagServiceImpl.java

// 更保守（适合复杂文档）
final int MAX_CHARS = 15000;  // 约 3750-5000 tokens

// 当前配置（平衡）
final int MAX_CHARS = 20000;  // 约 5000-6000 tokens

// 更激进（风险较高）
final int MAX_CHARS = 30000;  // 约 7500-10000 tokens（接近限制）
```

### 批处理大小

```java
// 更小批次（更安全但更慢）
int batchSize = 5;

// 当前配置
int batchSize = 10;

// 更大批次（更快但风险更高）
int batchSize = 20;
```

## 日志监控

加载时注意观察日志：

### ✅ 成功标志
```
Total characters loaded: 20000 (limit: 20000)
Split into 35 segments
Documents ingested successfully from knowledge directory (limited to 20000 chars)
```

### ⚠️ 警告信息
```
Reached character limit (20000), truncating remaining documents
```
这表示文档被截断了，只加载了部分内容。

### ❌ 错误标志
```
Maximum number of tokens per request for demonstration purposes is 10000
```
如果仍然看到这个错误，说明：
1. 批处理大小太大
2. 字符限制设置过高
3. 单个片段太大

**解决方法：**
- 减小 `MAX_CHARS`（如 15000）
- 减小 `batchSize`（如 5）
- 减小片段大小（如 300 字符）

## 常见问题

### Q1: 为什么只能回答文档开头的问题？
A: 因为受 Demo API 限制，只加载了文档的前 20000 个字符。

### Q2: 如何知道加载了多少内容？
A: 查看后端日志，会显示 `Total characters loaded: XXX`

### Q3: 能不能多加载一些内容？
A: 可以调整 `MAX_CHARS`，但不建议超过 25000，否则可能超过 token 限制。

### Q4: 加载失败怎么办？
A:
1. 检查日志中的错误信息
2. 尝试减小 `MAX_CHARS` 到 15000
3. 减小 `batchSize` 到 5
4. 考虑使用自己的 API Key

### Q5: 为什么加载很慢？
A:
- Demo API 可能有速率限制
- 分批处理需要多次请求
- 正常情况 30-35 个片段需要 10-15 秒

## 性能优化建议

### 1. 选择文档片段

手动选择文档中最重要的部分：

```bash
# 提取 PDF 的前 10 页
pdftk full.pdf cat 1-10 output first-10-pages.pdf
```

### 2. 预处理文档

- 移除无关内容（目录、页眉页脚）
- 只保留核心规范内容
- 转换为纯文本格式

### 3. 使用摘要

为每个章节创建摘要文档：
```
knowledge/
├── summary-chapter-1.txt
├── summary-chapter-2.txt
...
```

## 总结

当前实现：
- ✅ 支持 Demo API 的 token 限制
- ✅ 智能截断，避免错误
- ✅ 批处理，提高成功率
- ✅ 详细日志，便于调试

限制：
- ⚠️ 只能加载部分文档（约 10%）
- ⚠️ 无法回答文档后半部分的问题
- ⚠️ 适合测试，不适合生产

**建议：**
- 测试阶段：使用当前限制版本
- 生产环境：使用自己的 API Key 或本地模型

现在可以安全地加载文档进行测试了！
