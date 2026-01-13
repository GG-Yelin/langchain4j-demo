package org.example.langchain4jdemo.controller;

import lombok.RequiredArgsConstructor;
import org.example.langchain4jdemo.dto.EmbeddingRequest;
import org.example.langchain4jdemo.dto.EmbeddingResponse;
import org.example.langchain4jdemo.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    /**
     * 单文本向量化
     * POST /api/embedding/single
     */
    @PostMapping("/single")
    public EmbeddingResponse embed(@RequestBody EmbeddingRequest request) {
        return embeddingService.embed(request);
    }

    /**
     * 批量文本向量化
     * POST /api/embedding/batch
     */
    @PostMapping("/batch")
    public EmbeddingResponse embedBatch(@RequestBody EmbeddingRequest request) {
        return embeddingService.embedBatch(request);
    }
}
