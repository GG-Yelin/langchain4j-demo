package org.example.langchain4jdemo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmbeddingResponse {

    /**
     * 单个文本的向量结果
     */
    private List<Float> embedding;

    /**
     * 批量文本的向量结果
     */
    private List<List<Float>> embeddings;

    /**
     * 向量维度
     */
    private Integer dimension;

    private boolean success;
    private String errorMessage;
}
