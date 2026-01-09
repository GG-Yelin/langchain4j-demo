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
     * 添加文档到知识库
     * POST /api/rag/document
     */
    @PostMapping("/document")
    public ResponseEntity<Map<String, String>> addDocument(
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "manual") String source) {
        ragService.addDocument(content, source);
        return ResponseEntity.ok(Map.of("message", "Document added successfully"));
    }

    /**
     * 从文件加载文档
     * POST /api/rag/document/file
     */
    @PostMapping("/document/file")
    public ResponseEntity<Map<String, String>> loadDocument(@RequestParam String filePath) {
        ragService.loadDocumentFromFile(filePath);
        return ResponseEntity.ok(Map.of("message", "Document loaded successfully"));
    }
}
