package com.a0000.digicompass.modules.ai.knowledge.service;

import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSearchResult;
import java.util.List;

public interface AiKnowledgeEmbeddingService {

    int rebuildAllEmbeddings();

    int rebuildKnowledgeEmbeddings(Long knowledgeId);

    List<AiKnowledgeSearchResult> search(String query, Long categoryId, Long productId, int limit);
}
