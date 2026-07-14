package com.a0000.digicompass.modules.ai.knowledge.service.impl;

import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeChunkItem;
import com.a0000.digicompass.modules.ai.knowledge.mapper.AiKnowledgeChunkMapper;
import com.a0000.digicompass.modules.ai.knowledge.service.AiKnowledgeChunkService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AiKnowledgeChunkServiceImpl implements AiKnowledgeChunkService {

    private static final int CHUNK_SIZE = 600;
    private static final int OVERLAP = 80;

    private final AiKnowledgeChunkMapper chunkMapper;

    public AiKnowledgeChunkServiceImpl(AiKnowledgeChunkMapper chunkMapper) {
        this.chunkMapper = chunkMapper;
    }

    @Override
    public List<AiKnowledgeChunkItem> listChunks(Long knowledgeId) {
        return chunkMapper.findByKnowledgeId(knowledgeId);
    }

    @Override
    public void rebuildChunks(Long knowledgeId, String title, String content, Long productId, Long categoryId) {
        chunkMapper.deleteByKnowledgeId(knowledgeId);
        if (content == null || content.isBlank()) return;

        List<String> chunks = splitContent(content);
        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            String chunkTitle = title + " #" + (i + 1);
            String hash = sha256(chunkContent);
            chunkMapper.insert(knowledgeId, productId, categoryId, i + 1, chunkTitle, chunkContent, hash, chunkContent.length());
        }
    }

    @Override
    public void deleteChunks(Long knowledgeId) {
        chunkMapper.deleteByKnowledgeId(knowledgeId);
    }

    private List<String> splitContent(String content) {
        if (content.length() <= CHUNK_SIZE) {
            return List.of(content);
        }
        List<String> chunks = new java.util.ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + CHUNK_SIZE, content.length());
            chunks.add(content.substring(start, end));
            start += CHUNK_SIZE - OVERLAP;
            if (start >= content.length()) break;
        }
        return chunks;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
