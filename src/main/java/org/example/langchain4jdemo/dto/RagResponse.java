package org.example.langchain4jdemo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RagResponse {

    /**
     * AI生成的回答
     */
    private String answer;

    /**
     * 检索到的相关文档片段
     */
    private List<RetrievedDocument> sources;

    private boolean success;
    private String errorMessage;

    @Data
    @Builder
    public static class RetrievedDocument {
        /**
         * 文档内容
         */
        private String content;

        /**
         * 相似度分数
         */
        private Double score;

        /**
         * 来源信息
         */
        private String source;
    }
}
