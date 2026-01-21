package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.EmbeddingRequest;
import org.example.langchain4jdemo.dto.EmbeddingResponse;
import org.example.langchain4jdemo.service.EmbeddingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {


    private final EmbeddingModel embeddingModel;

    @Override
    public EmbeddingResponse embed(EmbeddingRequest request) {
        try {

            Response<Embedding> response = embeddingModel.embed(request.getText());
            List<Float> vector = response.content().vectorAsList();

            return EmbeddingResponse.builder()
                    .embedding(vector)
                    .dimension(response.content().dimension())
                    .success(true)
                    .build();


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

             List<TextSegment> segments = request.getTexts().stream()
                 .map(TextSegment::from)
                 .toList();
             Response<List<Embedding>> response = embeddingModel.embedAll(segments);

            List<List<Float>> vectors = response.content().stream().map(Embedding::vectorAsList).toList();

            return EmbeddingResponse.builder()
                    .embeddings(vectors)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Batch embedding error", e);
            return EmbeddingResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
