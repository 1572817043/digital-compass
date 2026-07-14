package com.a0000.digicompass.modules.ai.knowledge.service;

import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeChunkItem;
import java.util.List;

public interface AiKnowledgeChunkService {

    List<AiKnowledgeChunkItem> listChunks(Long knowledgeId);

    void rebuildChunks(Long knowledgeId, String title, String content, Long productId, Long categoryId);

    void deleteChunks(Long knowledgeId);
}
