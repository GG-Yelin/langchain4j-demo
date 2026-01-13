package org.example.langchain4jdemo.service;

import org.example.langchain4jdemo.dto.EmbeddingRequest;
import org.example.langchain4jdemo.dto.EmbeddingResponse;

/**
 * 向量嵌入服务接口
 * 用于文本向量化
 */
public interface EmbeddingService {

    /**
     * 单个文本向量化
     */
    EmbeddingResponse embed(EmbeddingRequest request);

    /**
     * 批量文本向量化
     */
    EmbeddingResponse embedBatch(EmbeddingRequest request);
}
