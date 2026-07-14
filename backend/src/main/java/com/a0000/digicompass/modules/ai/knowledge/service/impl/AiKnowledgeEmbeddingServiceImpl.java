package com.a0000.digicompass.modules.ai.knowledge.service.impl;

import com.a0000.digicompass.modules.ai.client.AiCallException;
import com.a0000.digicompass.modules.ai.client.OpenAiCompatibleEmbeddingClient;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;
import com.a0000.digicompass.modules.ai.embedding.LocalTextEmbeddingService;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSearchResult;
import com.a0000.digicompass.modules.ai.knowledge.mapper.AiKnowledgeEmbeddingMapper;
import com.a0000.digicompass.modules.ai.knowledge.mapper.AiKnowledgeEmbeddingMapper.ChunkForEmbedding;
import com.a0000.digicompass.modules.ai.knowledge.mapper.AiKnowledgeEmbeddingMapper.EmbeddingRow;
import com.a0000.digicompass.modules.ai.knowledge.service.AiKnowledgeEmbeddingService;
import com.a0000.digicompass.modules.ai.service.AiProviderConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiKnowledgeEmbeddingServiceImpl implements AiKnowledgeEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(AiKnowledgeEmbeddingServiceImpl.class);

    private final AiKnowledgeEmbeddingMapper embeddingMapper;
    private final AiProviderConfigService aiProviderConfigService;
    private final OpenAiCompatibleEmbeddingClient embeddingClient;
    private final LocalTextEmbeddingService localTextEmbeddingService;
    private final ObjectMapper objectMapper;

    public AiKnowledgeEmbeddingServiceImpl(
            AiKnowledgeEmbeddingMapper embeddingMapper,
            AiProviderConfigService aiProviderConfigService,
            OpenAiCompatibleEmbeddingClient embeddingClient,
            LocalTextEmbeddingService localTextEmbeddingService,
            ObjectMapper objectMapper
    ) {
        this.embeddingMapper = embeddingMapper;
        this.aiProviderConfigService = aiProviderConfigService;
        this.embeddingClient = embeddingClient;
        this.localTextEmbeddingService = localTextEmbeddingService;
        this.objectMapper = objectMapper;
    }

    @Override
    public int rebuildAllEmbeddings() {
        return rebuild(null);
    }

    @Override
    public int rebuildKnowledgeEmbeddings(Long knowledgeId) {
        return rebuild(knowledgeId);
    }

    @Override
    public List<AiKnowledgeSearchResult> search(String query, Long categoryId, Long productId, int limit) {
        if (query == null || query.isBlank()) return List.of();
        EmbeddingValue queryEmbedding = createEmbedding(query);
        return embeddingMapper.findSearchableEmbeddings(categoryId, productId)
                .stream()
                .map(row -> toSearchResult(row, queryEmbedding.vector()))
                .filter(result -> result.score() > 12)
                .sorted(Comparator.comparingInt(AiKnowledgeSearchResult::score).reversed())
                .limit(limit)
                .toList();
    }

    private int rebuild(Long knowledgeId) {
        int count = 0;
        for (ChunkForEmbedding chunk : embeddingMapper.findActiveChunks(knowledgeId)) {
            EmbeddingValue embedding = createEmbedding(chunk.title() + "\n" + chunk.content());
            try {
                embeddingMapper.upsertEmbedding(
                        chunk.id(),
                        embedding.providerId(),
                        embedding.modelName(),
                        chunk.contentHash(),
                        objectMapper.writeValueAsString(embedding.vector()),
                        embedding.vector().length
                );
                count++;
            } catch (Exception e) {
                log.warn("知识向量保存失败: chunkId={}, error={}", chunk.id(), e.getMessage());
            }
        }
        return count;
    }

    private AiKnowledgeSearchResult toSearchResult(EmbeddingRow row, double[] queryVector) {
        double[] storedVector = parseVector(row.vectorJson());
        int score = 0;
        if (storedVector.length == queryVector.length) {
            score = (int) Math.round(cosine(queryVector, storedVector) * 100);
        }
        return new AiKnowledgeSearchResult(
                row.chunkId(),
                row.knowledgeId(),
                row.productId(),
                row.title(),
                row.content(),
                score,
                "VECTOR",
                row.modelName()
        );
    }

    private EmbeddingValue createEmbedding(String text) {
        AiProviderConfigItem provider = aiProviderConfigService.getDefaultProvider();
        if (provider != null && provider.embeddingModel() != null && !provider.embeddingModel().isBlank()) {
            try {
                return new EmbeddingValue(provider.id(), provider.embeddingModel(), embeddingClient.embed(provider, text));
            } catch (AiCallException e) {
                log.warn("远程向量生成失败，使用本地向量兜底: {}", e.getMessage());
            }
        }
        return new EmbeddingValue(null, localTextEmbeddingService.modelName(), localTextEmbeddingService.embed(text));
    }

    private double[] parseVector(String vectorJson) {
        try {
            JsonNode node = objectMapper.readTree(vectorJson);
            double[] vector = new double[node.size()];
            for (int i = 0; i < node.size(); i++) {
                vector[i] = node.get(i).asDouble();
            }
            return vector;
        } catch (Exception e) {
            return new double[0];
        }
    }

    private double cosine(double[] a, double[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record EmbeddingValue(Long providerId, String modelName, double[] vector) {
    }
}
