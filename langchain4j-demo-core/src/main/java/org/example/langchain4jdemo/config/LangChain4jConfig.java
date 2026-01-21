package org.example.langchain4jdemo.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 配置类
 * 配置 EmbeddingModel 和 EmbeddingStore
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.embedding-model.base-url:http://langchain4j.dev/demo/openai/v1}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.embedding-model.api-key:demo}")
    private String apiKey;

    @Value("${langchain4j.open-ai.embedding-model.model-name:text-embedding-3-small}")
    private String modelName;

    @Value("${langchain4j.open-ai.embedding-model.timeout:60s}")
    private Duration timeout;

    /**
     * 配置 OpenAI Embedding 模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(timeout)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 配置内存向量存储
     * 生产环境建议使用持久化的向量数据库，如 Pinecone, Weaviate, Milvus 等
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
