package com.clj.ai.service.impl;

import com.clj.ai.dto.ChunkSimilarityDto;
import com.clj.ai.dto.RagSearchRequestDto;
import com.clj.ai.mapper.AiRagChunkMapper;
import com.clj.ai.service.AiRagSearchService;
import com.clj.ai.vo.RagSearchResultVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRagSearchServiceImpl implements AiRagSearchService {

    private final EmbeddingModel embeddingModel;
    private final AiRagChunkMapper chunkMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<RagSearchResultVo> search(RagSearchRequestDto request) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
            return List.of();
        }

        int topK = request.getTopK() == null || request.getTopK() <= 0
                ? 5
                : Math.min(request.getTopK(), 50);
        double minScore = request.getMinScore() == null ? 0.65 : request.getMinScore();
        List<Long> knowledgeBaseIds = resolveKnowledgeBaseIds(request);
        List<Long> documentIds = normalizeIds(request.getDocumentIds());

        // 检索范围必须由调用方显式指定：用户未勾选任何知识库/文档时不检索全库，直接返回空结果
        if (knowledgeBaseIds.isEmpty() && documentIds.isEmpty()) {
            log.warn("RAG 检索未指定知识库或文档范围，按“未勾选不检索”策略返回空结果: query={}", request.getQuery());
            return List.of();
        }

        Embedding queryEmbedding = embeddingModel.embed(request.getQuery()).content();
        List<ChunkSimilarityDto> similarChunks = chunkMapper.searchSimilar(
                new PGvector(queryEmbedding.vector()),
                topK,
                knowledgeBaseIds,
                documentIds);

        List<RagSearchResultVo> results = new ArrayList<>();
        for (ChunkSimilarityDto chunk : similarChunks) {
            if (chunk.getSimilarity() == null || chunk.getSimilarity() < minScore) {
                continue;
            }
            results.add(RagSearchResultVo.builder()
                    .documentId(chunk.getDocumentId())
                    .chunkId(chunk.getId())
                    .content(chunk.getContent())
                    .knowledgeBaseId(chunk.getKnowledgeBaseId())
                    .knowledgeBaseName(chunk.getKnowledgeBaseName())
                    .score(chunk.getSimilarity())
                    .metadata(parseMetadata(chunk.getMetadata()))
                    .build());
        }
        return results;
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private List<Long> resolveKnowledgeBaseIds(RagSearchRequestDto request) {
        List<Long> ids = new ArrayList<>();
        if (request.getKnowledgeBaseIds() != null) {
            ids.addAll(request.getKnowledgeBaseIds());
        }
        if (request.getKnowledgeBaseId() != null) {
            ids.add(request.getKnowledgeBaseId());
        }
        return ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private Map<String, Object> parseMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadata, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse RAG metadata: {}", e.getMessage());
            return Map.of();
        }
    }
}
