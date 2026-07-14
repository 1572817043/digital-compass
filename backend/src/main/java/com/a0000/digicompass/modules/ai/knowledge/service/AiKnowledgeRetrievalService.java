package com.a0000.digicompass.modules.ai.knowledge.service;

import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSearchResult;
import java.util.List;

public interface AiKnowledgeRetrievalService {

    List<AiKnowledgeSearchResult> search(String query, Long categoryId, Long productId, int limit);
}
