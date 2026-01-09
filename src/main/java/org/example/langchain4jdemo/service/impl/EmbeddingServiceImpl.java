package org.example.langchain4jdemo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.EmbeddingRequest;
import org.example.langchain4jdemo.dto.EmbeddingResponse;
import org.example.langchain4jdemo.service.EmbeddingService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    // TODO: 注入 EmbeddingModel
    // 示例:
    // private final EmbeddingModel embeddingModel;

    @Override
    public EmbeddingResponse embed(EmbeddingRequest request) {
        try {
            // TODO: 在这里实现单文本向量化
            // 提示: 使用 embeddingModel.embed(text)
            // 示例:
            // Response<Embedding> response = embeddingModel.embed(request.getText());
            // List<Float> vector = response.content().vectorAsList();

            throw new UnsupportedOperationException("请实现 embed 方法");

        } catch (Exception e) {
            log.error("Embedding error", e);
            return EmbeddingResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public EmbeddingResponse embedBatch(EmbeddingRequest request) {
        try {
            // TODO: 在这里实现批量文本向量化
            // 提示: 使用 embeddingModel.embedAll(texts)
            // 示例:
            // List<TextSegment> segments = request.getTexts().stream()
            //     .map(TextSegment::from)
            //     .toList();
            // Response<List<Embedding>> response = embeddingModel.embedAll(segments);

            throw new UnsupportedOperationException("请实现 embedBatch 方法");

        } catch (Exception e) {
            log.error("Batch embedding error", e);
            return EmbeddingResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
