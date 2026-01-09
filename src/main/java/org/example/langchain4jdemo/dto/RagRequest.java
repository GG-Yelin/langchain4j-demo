package org.example.langchain4jdemo.dto;

import lombok.Data;

@Data
public class RagRequest {

    /**
     * 用户查询
     */
    private String query;

    /**
     * 返回的文档数量
     */
    private Integer topK;

    /**
     * 相似度阈值
     */
    private Double minScore;

    /**
     * 是否包含来源信息
     */
    private Boolean includeSource;
}
