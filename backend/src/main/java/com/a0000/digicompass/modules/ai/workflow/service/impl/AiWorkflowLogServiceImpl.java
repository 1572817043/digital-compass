package com.a0000.digicompass.modules.ai.workflow.service.impl;

import com.a0000.digicompass.modules.ai.mapper.AiWorkflowLogMapper;
import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowCandidateProduct;
import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowLogDetail;
import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowLogItem;
import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowStepItem;
import com.a0000.digicompass.modules.ai.workflow.service.AiWorkflowLogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AiWorkflowLogServiceImpl implements AiWorkflowLogService {

    private final AiWorkflowLogMapper mapper;
    private final ObjectMapper objectMapper;

    public AiWorkflowLogServiceImpl(AiWorkflowLogMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AiWorkflowLogItem> listLogs(String keyword, Boolean fallbackUsed, Integer limit) {
        return mapper.findRecent(keyword, fallbackUsed, limit != null ? limit : 50);
    }

    @Override
    public AiWorkflowLogDetail getDetail(Long id) {
        AiWorkflowLogItem item = mapper.findById(id);
        if (item == null) {
            throw new IllegalArgumentException("AI 工作流日志不存在");
        }
        Map<String, Object> parsedRequirement = parseRequirement(item.parsedRequirementJson());
        List<String> retrievedContexts = splitContexts(item.retrievedContextSummary());
        List<Long> candidateProductIds = parseProductIds(item.candidateProductIds());
        List<AiWorkflowCandidateProduct> candidateProducts = mapper.findCandidateProducts(candidateProductIds);
        return new AiWorkflowLogDetail(
                item.id(),
                item.userId(),
                item.username(),
                item.conversationId(),
                item.providerId(),
                item.providerName(),
                item.modelName(),
                item.userRequirement(),
                item.parsedRequirementJson(),
                parsedRequirement,
                item.retrievedContextSummary(),
                retrievedContexts,
                item.candidateProductIds(),
                candidateProductIds,
                candidateProducts,
                item.fallbackUsed(),
                item.errorMessage(),
                buildWorkflowSteps(item, parsedRequirement, retrievedContexts, candidateProductIds),
                item.createdAt()
        );
    }

    private Map<String, Object> parseRequirement(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }

    private List<String> splitContexts(String summary) {
        if (summary == null || summary.isBlank()) {
            return List.of();
        }
        return summary.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private List<Long> parseProductIds(String candidateProductIds) {
        if (candidateProductIds == null || candidateProductIds.isBlank()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (String item : candidateProductIds.split(",")) {
            try {
                ids.add(Long.valueOf(item.trim()));
            } catch (NumberFormatException ignored) {
                // 跳过异常候选 ID，详情页仍展示可解析的部分。
            }
        }
        return ids;
    }

    private List<AiWorkflowStepItem> buildWorkflowSteps(
            AiWorkflowLogItem item,
            Map<String, Object> parsedRequirement,
            List<String> retrievedContexts,
            List<Long> candidateProductIds
    ) {
        String parserStatus = item.fallbackUsed() ? "fallback" : "success";
        String parserDescription = item.fallbackUsed()
                ? "AI 解析不可用，系统使用规则从文本中抽取预算、品类和用途。"
                : "默认 AI 配置完成自然语言需求解析，结果写入 parsedRequirement。";
        String ragStatus = retrievedContexts.isEmpty() ? "empty" : "success";
        String candidateStatus = candidateProductIds.isEmpty() ? "empty" : "success";
        return List.of(
                new AiWorkflowStepItem(
                        "input",
                        "用户输入",
                        "success",
                        "记录用户在选购助手中提交的原始需求。",
                        item.userRequirement()
                ),
                new AiWorkflowStepItem(
                        "parse",
                        "需求解析",
                        parserStatus,
                        parserDescription,
                        parsedRequirement.isEmpty() ? "-" : parsedRequirement.toString()
                ),
                new AiWorkflowStepItem(
                        "rag",
                        "RAG 检索",
                        ragStatus,
                        retrievedContexts.isEmpty() ? "未命中知识库摘要。" : "根据需求关键词检索产品知识库切片。",
                        retrievedContexts.isEmpty() ? "-" : String.join("\n", retrievedContexts)
                ),
                new AiWorkflowStepItem(
                        "candidate",
                        "候选产品",
                        candidateStatus,
                        candidateProductIds.isEmpty() ? "未生成候选产品。" : "结合需求、RAG 命中和规则评分得到候选产品。",
                        candidateProductIds.isEmpty() ? "-" : candidateProductIds.toString()
                ),
                new AiWorkflowStepItem(
                        "output",
                        "推荐输出",
                        candidateStatus,
                        item.fallbackUsed()
                                ? "推荐结果由规则解析、RAG 检索和候选排序生成。"
                                : "推荐结果由 AI 需求解析、RAG 检索、候选排序和推荐文案生成链路共同产生。",
                        item.errorMessage() != null ? item.errorMessage() : "已写入会话消息和推荐卡片。"
                )
        );
    }
}
