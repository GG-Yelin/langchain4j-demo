package org.example.langchain4jdemo.service;

import org.example.langchain4jdemo.dto.RagRequest;
import org.example.langchain4jdemo.dto.RagResponse;

/**
 * RAG(检索增强生成)服务接口
 */
public interface RagService {

    /**
     * RAG查询 - 检索相关文档并生成回答
     */
    RagResponse query(RagRequest request);

    /**
     * 添加文档到知识库
     */
    void addDocument(String content, String source);

    /**
     * 从文件加载文档到知识库
     */
    void loadDocumentFromFile(String filePath);
}
