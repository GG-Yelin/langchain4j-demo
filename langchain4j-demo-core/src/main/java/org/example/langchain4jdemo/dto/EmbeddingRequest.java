package org.example.langchain4jdemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmbeddingRequest {

    /**
     * 需要向量化的文本
     */
    private String text;

    /**
     * 批量文本列表
     */
    private List<String> texts;
}
