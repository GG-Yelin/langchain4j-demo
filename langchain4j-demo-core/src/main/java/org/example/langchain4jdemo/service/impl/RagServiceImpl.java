package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.RagRequest;
import org.example.langchain4jdemo.dto.RagResponse;
import org.example.langchain4jdemo.service.RagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

     private final EmbeddingModel embeddingModel;

     private final EmbeddingStore<TextSegment> embeddingStore;

     private final ChatLanguageModel chatModel;

    @Override
    public RagResponse query(RagRequest request) {
        try {
            log.info("Processing RAG query: {}", request.getQuery());

            // 步骤1: 将查询转换为向量
            Embedding queryEmbedding = embeddingModel.embed(request.getQuery()).content();
            log.debug("Query embedding generated successfully");

            // 步骤2: 在向量存储中检索相似文档
            int topK = request.getTopK() != null ? request.getTopK() : 5;
            double minScore = request.getMinScore() != null ? request.getMinScore() : 0.7;

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .minScore(minScore)
                .build();
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
            log.debug("Found {} matching documents", searchResult.matches().size());

            // 步骤3: 构建提示词并生成回答
            String context = searchResult.matches().stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

            String prompt = "基于以下上下文回答问题:\n\n" +
                           "上下文:\n" + context + "\n\n" +
                           "问题: " + request.getQuery() + "\n\n" +
                           "请根据上下文提供准确的回答。如果上下文中没有相关信息，请明确说明。";

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(UserMessage.from(prompt))
                    .build();
            ChatResponse chatResponse = chatModel.chat(chatRequest);
            String answer = chatResponse.aiMessage().text();
            log.info("Answer generated successfully");

            // 步骤4: 构建返回结果
            List<RagResponse.RetrievedDocument> sources = null;
            if (Boolean.TRUE.equals(request.getIncludeSource())) {
                sources = searchResult.matches().stream()
                    .map(match -> RagResponse.RetrievedDocument.builder()
                        .content(match.embedded().text())
                        .score(match.score())
                        .source("Document")
                        .build())
                    .collect(Collectors.toList());
            }

            return RagResponse.builder()
                    .success(true)
                    .answer(answer)
                    .sources(sources)
                    .build();

        } catch (Exception e) {
            log.error("RAG query error", e);
            return RagResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }


    /**
     * 从类路径下的 knowledge 目录加载文档并存储到向量数据库
     * 限制加载内容，避免超过 Demo API 的 token 限制
     */
    @Override
    public void loadDocumentsFromKnowledge() {
        try {
            log.info("Loading documents from classpath:knowledge directory (with token limit for demo API)");

            // 获取类路径下的 knowledge 目录
            ClassLoader classLoader = getClass().getClassLoader();
            java.net.URL resourceUrl = classLoader.getResource("knowledge");

            if (resourceUrl == null) {
                throw new RuntimeException("Knowledge directory not found in classpath");
            }

            String knowledgePath = resourceUrl.getPath();
            log.info("Knowledge directory path: {}", knowledgePath);

            // 加载目录下的所有文档
            List<Document> documents = FileSystemDocumentLoader.loadDocuments(knowledgePath);
            log.info("Loaded {} documents", documents.size());

            if (documents.isEmpty()) {
                throw new RuntimeException("No documents found in knowledge directory");
            }

            // Demo API 限制: 每次最多 10000 tokens
            // 粗略估算: 1 token ≈ 4 个字符（中文约 1.5-2 个字符）
            // 为安全起见，限制为 20000 个字符（约 5000-6000 tokens，留有余量）
            final int MAX_CHARS = 20000;

            // 合并所有文档内容并截断
            StringBuilder combinedText = new StringBuilder();
            int totalChars = 0;

            for (Document doc : documents) {
                String text = doc.text();
                if (totalChars + text.length() <= MAX_CHARS) {
                    combinedText.append(text).append("\n\n");
                    totalChars += text.length();
                } else {
                    // 添加部分内容直到达到限制
                    int remainingChars = MAX_CHARS - totalChars;
                    if (remainingChars > 0) {
                        combinedText.append(text.substring(0, remainingChars));
                        totalChars = MAX_CHARS;
                    }
                    log.warn("Reached character limit ({}), truncating remaining documents", MAX_CHARS);
                    break;
                }
            }

            log.info("Total characters loaded: {} (limit: {})", totalChars, MAX_CHARS);

            // 创建截断后的文档
            Document limitedDocument = Document.from(combinedText.toString());

            // 配置文档分割器 (每个片段最多500个字符，重叠100个字符)
            DocumentSplitter splitter = DocumentSplitters.recursive(500, 100);

            // 分割文档
            List<TextSegment> segments = splitter.split(limitedDocument);
            log.info("Split into {} segments", segments.size());

            // 批量处理，避免单次请求过大
            // Demo API 限制每次最多 10000 tokens
            // 每批处理 10 个片段（约 5000 字符，1000-1500 tokens）
            int batchSize = 10;
            int totalSegments = segments.size();
            int processedSegments = 0;

            for (int i = 0; i < totalSegments; i += batchSize) {
                int end = Math.min(i + batchSize, totalSegments);
                List<TextSegment> batch = segments.subList(i, end);

                log.info("Processing batch {}/{} ({} segments)",
                    (i / batchSize + 1),
                    (totalSegments + batchSize - 1) / batchSize,
                    batch.size());

                // 为每个片段生成 embedding 并存储
                for (TextSegment segment : batch) {
                    Embedding embedding = embeddingModel.embed(segment).content();
                    embeddingStore.add(embedding, segment);
                    processedSegments++;
                }

                log.info("Processed {}/{} segments", processedSegments, totalSegments);
            }

            log.info("Documents ingested successfully from knowledge directory (limited to {} chars)", totalChars);

        } catch (Exception e) {
            log.error("Error loading documents from classpath knowledge directory", e);
            throw new RuntimeException("Failed to load documents from knowledge directory: " + e.getMessage(), e);
        }
    }
}
