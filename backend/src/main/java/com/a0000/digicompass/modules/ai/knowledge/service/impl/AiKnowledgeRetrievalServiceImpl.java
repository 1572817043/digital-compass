package com.a0000.digicompass.modules.ai.knowledge.service.impl;

import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSearchResult;
import com.a0000.digicompass.modules.ai.knowledge.mapper.AiKnowledgeChunkMapper;
import com.a0000.digicompass.modules.ai.knowledge.service.AiKnowledgeEmbeddingService;
import com.a0000.digicompass.modules.ai.knowledge.service.AiKnowledgeRetrievalService;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiKnowledgeRetrievalServiceImpl implements AiKnowledgeRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(AiKnowledgeRetrievalServiceImpl.class);

    private final AiKnowledgeChunkMapper chunkMapper;
    private final AiKnowledgeEmbeddingService embeddingService;

    public AiKnowledgeRetrievalServiceImpl(AiKnowledgeChunkMapper chunkMapper, AiKnowledgeEmbeddingService embeddingService) {
        this.chunkMapper = chunkMapper;
        this.embeddingService = embeddingService;
    }

    @Override
    public List<AiKnowledgeSearchResult> search(String query, Long categoryId, Long productId, int limit) {
        if (query == null || query.isBlank()) return List.of();
        try {
            List<AiKnowledgeSearchResult> vectorResults = embeddingService.search(query, categoryId, productId, limit);
            List<AiKnowledgeSearchResult> results = chunkMapper.search(query, categoryId, productId, limit);
            List<AiKnowledgeSearchResult> mergedResults = mergeResults(vectorResults, results, limit);
            log.info("知识检索: query={}, vector_results={}, keyword_results={}, merged_results={}",
                    query, vectorResults.size(), results.size(), mergedResults.size());
            return mergedResults;
        } catch (Exception e) {
            log.warn("知识检索失败: query={}, error={}", query, e.getMessage());
            return List.of();
        }
    }

    private List<AiKnowledgeSearchResult> mergeResults(
            List<AiKnowledgeSearchResult> vectorResults,
            List<AiKnowledgeSearchResult> keywordResults,
            int limit
    ) {
        Map<Long, AiKnowledgeSearchResult> merged = new LinkedHashMap<>();
        for (AiKnowledgeSearchResult result : vectorResults) {
            merged.put(result.chunkId(), result);
        }
        for (AiKnowledgeSearchResult result : keywordResults) {
            AiKnowledgeSearchResult existing = merged.get(result.chunkId());
            if (existing == null) {
                merged.put(result.chunkId(), result);
            } else {
                merged.put(result.chunkId(), mergeHit(existing, result));
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparingInt(AiKnowledgeSearchResult::score).reversed())
                .limit(limit)
                .toList();
    }

    private AiKnowledgeSearchResult mergeHit(AiKnowledgeSearchResult left, AiKnowledgeSearchResult right) {
        int score = Math.max(left.score(), right.score());
        String source = left.retrievalSource() != null && left.retrievalSource().equals(right.retrievalSource())
                ? left.retrievalSource()
                : "HYBRID";
        return new AiKnowledgeSearchResult(
                left.chunkId(),
                left.knowledgeId(),
                left.productId(),
                left.title(),
                left.content(),
                score,
                source,
                firstNonBlank(left.embeddingModel(), right.embeddingModel())
        );
    }

    private String firstNonBlank(String left, String right) {
        if (left != null && !left.isBlank()) return left;
        if (right != null && !right.isBlank()) return right;
        return null;
    }
}
