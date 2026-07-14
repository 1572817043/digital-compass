package com.a0000.digicompass.modules.ai.service;

import com.a0000.digicompass.modules.ai.client.AiCallException;
import com.a0000.digicompass.modules.ai.client.OpenAiCompatibleChatClient;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiRequirementParserService {

    private static final Logger log = LoggerFactory.getLogger(AiRequirementParserService.class);

    private static final String SYSTEM_PROMPT = """
            你是一个数码产品选购需求解析助手。用户会用自然语言描述他们的选购需求。
            请从用户输入中提取结构化信息，只返回 JSON，不要返回任何解释文字。

            JSON 格式：
            {
              "categoryKeyword": "手机/笔记本电脑/平板/耳机/智能手表 或空字符串",
              "minBudget": 数字或null,
              "maxBudget": 数字或null,
              "acceptUsed": true/false,
              "usageScenes": ["学习","办公"] 或空数组,
              "preferredBrands": ["Apple","Xiaomi"] 或空数组,
              "avoidedBrands": [] 或空数组,
              "priorityTags": ["性能","续航"] 或空数组,
              "avoidTags": ["机身重"] 或空数组,
              "riskPreference": "low/medium/high",
              "rawKeywords": "原始关键词"
            }

            注意：
            - 只返回 JSON，不要返回其他内容
            - 预算单位是人民币元
            - 如果用户没提到某个字段，对应值设为 null 或空数组
            """;

    private final AiProviderConfigService configService;
    private final OpenAiCompatibleChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiRequirementParserService(AiProviderConfigService configService, OpenAiCompatibleChatClient chatClient, ObjectMapper objectMapper) {
        this.configService = configService;
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    public ParsedRequirement parse(String userMessage) {
        AiProviderConfigItem config = configService.getDefaultProvider();
        if (config == null) {
            log.info("无默认 AI 配置，使用规则解析");
            return fallbackParse(userMessage);
        }

        try {
            String response = chatClient.chat(config, SYSTEM_PROMPT, userMessage);
            return parseJsonResponse(response, userMessage);
        } catch (AiCallException e) {
            log.warn("AI 解析失败，回退到规则解析: {}", e.getMessage());
            return fallbackParse(userMessage);
        } catch (Exception e) {
            log.warn("AI 解析异常，回退到规则解析: {}", e.getMessage());
            return fallbackParse(userMessage);
        }
    }

    private ParsedRequirement parseJsonResponse(String response, String originalMessage) {
        try {
            String json = response.trim();
            // 提取 JSON 部分（AI 可能返回 markdown 代码块）
            if (json.contains("```")) {
                int start = json.indexOf("```");
                int end = json.indexOf("```", start + 3);
                if (end > start) {
                    json = json.substring(start + 3, end).trim();
                    if (json.startsWith("json")) json = json.substring(4).trim();
                }
            }
            JsonNode node = objectMapper.readTree(json);
            Map<String, Object> result = new HashMap<>();
            result.put("categoryKeyword", getField(node, "categoryKeyword", ""));
            result.put("minBudget", getField(node, "minBudget", null));
            result.put("maxBudget", getField(node, "maxBudget", null));
            result.put("acceptUsed", getField(node, "acceptUsed", false));
            result.put("usageScenes", getField(node, "usageScenes", java.util.List.of()));
            result.put("preferredBrands", getField(node, "preferredBrands", java.util.List.of()));
            result.put("avoidedBrands", getField(node, "avoidedBrands", java.util.List.of()));
            result.put("priorityTags", getField(node, "priorityTags", java.util.List.of()));
            result.put("avoidTags", getField(node, "avoidTags", java.util.List.of()));
            result.put("riskPreference", getField(node, "riskPreference", "medium"));
            result.put("rawKeywords", getField(node, "rawKeywords", ""));
            return new ParsedRequirement(result, false);
        } catch (Exception e) {
            log.warn("AI 返回格式异常: {}", e.getMessage());
            return fallbackParse(originalMessage);
        }
    }

    private Object getField(JsonNode node, String field, Object defaultValue) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) return defaultValue;
        if (value.isArray()) {
            java.util.List<String> values = new java.util.ArrayList<>();
            value.forEach(item -> values.add(item.asText()));
            return values;
        }
        if (value.isBoolean()) return value.asBoolean();
        if (value.isNumber()) return value.numberValue();
        return value.asText(defaultValue != null ? defaultValue.toString() : null);
    }

    private ParsedRequirement fallbackParse(String userMessage) {
        Map<String, Object> result = new HashMap<>();
        String lower = userMessage.toLowerCase();
        result.put("categoryKeyword", extractCategory(lower));
        result.put("minBudget", extractBudgetMin(lower));
        result.put("maxBudget", extractBudgetMax(lower));
        result.put("acceptUsed", lower.contains("二手") || lower.contains("旧"));
        result.put("usageScenes", extractScenes(lower));
        result.put("preferredBrands", extractBrands(lower));
        result.put("avoidedBrands", java.util.List.of());
        result.put("priorityTags", extractPriority(lower));
        result.put("avoidTags", java.util.List.of());
        result.put("riskPreference", "medium");
        result.put("rawKeywords", userMessage);
        return new ParsedRequirement(result, true);
    }

    private String extractCategory(String text) {
        if (text.contains("手机")) return "手机";
        if (text.contains("笔记本") || text.contains("电脑") || text.contains("办公本")) return "笔记本电脑";
        if (text.contains("平板")) return "平板";
        if (text.contains("耳机")) return "耳机";
        if (text.contains("手表") || text.contains("手环")) return "智能手表";
        return "";
    }

    private Integer extractBudgetMin(String text) {
        String[] parts = text.split("[到至-]");
        for (String part : parts) {
            String num = part.replaceAll("[^0-9]", "");
            if (!num.isEmpty() && Integer.parseInt(num) > 100) return Integer.parseInt(num);
        }
        return null;
    }

    private Integer extractBudgetMax(String text) {
        String[] parts = text.split("[到至-]");
        Integer max = null;
        for (String part : parts) {
            String num = part.replaceAll("[^0-9]", "");
            if (!num.isEmpty() && Integer.parseInt(num) > 100) max = Integer.parseInt(num);
        }
        return max;
    }

    private java.util.List<String> extractScenes(String text) {
        java.util.List<String> scenes = new java.util.ArrayList<>();
        if (text.contains("学习")) scenes.add("学习");
        if (text.contains("办公")) scenes.add("办公");
        if (text.contains("游戏")) scenes.add("游戏");
        if (text.contains("摄影") || text.contains("拍照")) scenes.add("摄影");
        return scenes;
    }

    private java.util.List<String> extractBrands(String text) {
        java.util.List<String> brands = new java.util.ArrayList<>();
        if (text.contains("苹果") || text.contains("apple")) brands.add("Apple");
        if (text.contains("小米") || text.contains("红米")) brands.add("Xiaomi");
        if (text.contains("华为")) brands.add("Huawei");
        if (text.contains("荣耀")) brands.add("Honor");
        if (text.contains("联想")) brands.add("Lenovo");
        return brands;
    }

    private java.util.List<String> extractPriority(String text) {
        java.util.List<String> tags = new java.util.ArrayList<>();
        if (text.contains("性能")) tags.add("性能");
        if (text.contains("续航")) tags.add("续航");
        if (text.contains("拍照") || text.contains("影像")) tags.add("影像");
        if (text.contains("轻薄") || text.contains("便携")) tags.add("便携");
        if (text.contains("性价比")) tags.add("性价比");
        return tags;
    }

    public record ParsedRequirement(Map<String, Object> data, boolean fallbackUsed) {}
}
