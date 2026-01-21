package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
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

             // 步骤1: 将查询转换为向量
             Embedding queryEmbedding = embeddingModel.embed(request.getQuery()).content();

            // 步骤2: 在向量存储中检索相似文档
            // EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
            //     .queryEmbedding(queryEmbedding)
            //     .maxResults(request.getTopK())
            //     .minScore(request.getMinScore())
            //     .build();
            // EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

            // 步骤3: 构建提示词并生成回答
            // String context = searchResult.matches().stream()
            //     .map(match -> match.embedded().text())
            //     .collect(Collectors.joining("\n\n"));
            // String prompt = "基于以下上下文回答问题:\n" + context + "\n\n问题: " + request.getQuery();
            // String answer = chatModel.generate(prompt);

            // 或者使用 AiServices + ContentRetriever 的方式

            throw new UnsupportedOperationException("请实现 query 方法");

        } catch (Exception e) {
            log.error("RAG query error", e);
            return RagResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }


    private void loadDocumentFromPath(String path) {

        // 加载目录下的所有文档
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(path);

        // 将文档存储在专门的嵌入存储（向量数据库）中
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

    }
}
