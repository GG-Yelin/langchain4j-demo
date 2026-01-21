package org.example.langchain4jdemo.controller;

import lombok.RequiredArgsConstructor;
import org.example.langchain4jdemo.dto.RagRequest;
import org.example.langchain4jdemo.dto.RagResponse;
import org.example.langchain4jdemo.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    /**
     * RAG查询
     * POST /api/rag/query
     */
    @PostMapping("/query")
    public RagResponse query(@RequestBody RagRequest request) {
        return ragService.query(request);
    }

    /**
     * 从 classpath:knowledge 目录加载文档到向量数据库
     * POST /api/rag/load
     */
    @PostMapping("/load")
    public ResponseEntity<?> loadDocuments() {
        try {
            ragService.loadDocumentsFromKnowledge();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "已成功从 knowledge 目录加载文档"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

}
