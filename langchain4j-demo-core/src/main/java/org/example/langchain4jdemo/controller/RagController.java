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


}
