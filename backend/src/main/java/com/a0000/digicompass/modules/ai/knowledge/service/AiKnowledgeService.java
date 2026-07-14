package com.a0000.digicompass.modules.ai.knowledge.service;

import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeChunkItem;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeItem;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSaveRequest;
import java.util.List;

public interface AiKnowledgeService {

    List<AiKnowledgeItem> listKnowledge(String keyword, String knowledgeType, Integer status, Long categoryId, Long productId);

    AiKnowledgeItem getKnowledge(Long id);

    Long createKnowledge(AiKnowledgeSaveRequest request);

    void updateKnowledge(Long id, AiKnowledgeSaveRequest request);

    void deleteKnowledge(Long id);

    void rebuildChunks(Long id);

    List<AiKnowledgeChunkItem> listChunks(Long knowledgeId);

    int rebuildProductKnowledge();
}
