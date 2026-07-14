package com.a0000.digicompass.modules.assistant.service.impl;

import com.a0000.digicompass.modules.ai.client.OpenAiCompatibleChatClient;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSearchResult;
import com.a0000.digicompass.modules.ai.knowledge.service.AiKnowledgeRetrievalService;
import com.a0000.digicompass.modules.ai.mapper.AiWorkflowLogMapper;
import com.a0000.digicompass.modules.ai.service.AiProviderConfigService;
import com.a0000.digicompass.modules.ai.service.AiRequirementParserService;
import com.a0000.digicompass.modules.assistant.dto.AssistantChatResponse;
import com.a0000.digicompass.modules.assistant.dto.AssistantConversationItem;
import com.a0000.digicompass.modules.assistant.dto.AssistantRecommendationItem;
import com.a0000.digicompass.modules.assistant.dto.ChatMessageItem;
import com.a0000.digicompass.modules.assistant.dto.ChatMessageRequest;
import com.a0000.digicompass.modules.assistant.dto.ConversationCreateRequest;
import com.a0000.digicompass.modules.assistant.dto.ConversationMessagesResponse;
import com.a0000.digicompass.modules.assistant.dto.RecommendationCandidate;
import com.a0000.digicompass.modules.assistant.dto.RecommendationRequest;
import com.a0000.digicompass.modules.assistant.dto.RecommendationResponse;
import com.a0000.digicompass.modules.assistant.mapper.AssistantMapper;
import com.a0000.digicompass.modules.assistant.service.AssistantService;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import com.a0000.digicompass.modules.product.dto.ProductDetail;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import com.a0000.digicompass.modules.product.dto.ProductTagItem;
import com.a0000.digicompass.modules.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssistantServiceImpl implements AssistantService {

    private static final String DEFAULT_TITLE = "新的选购咨询";
    private static final Pattern BUDGET_PATTERN = Pattern.compile("(\\d{3,6})");

    private final AssistantMapper assistantMapper;
    private final ProductService productService;
    private final AiRequirementParserService aiParser;
    private final AiKnowledgeRetrievalService knowledgeRetrievalService;
    private final AiProviderConfigService aiConfigService;
    private final AiWorkflowLogMapper aiWorkflowLogMapper;
    private final OpenAiCompatibleChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AssistantServiceImpl(AssistantMapper assistantMapper, ProductService productService,
                                 AiRequirementParserService aiParser,
                                 AiKnowledgeRetrievalService knowledgeRetrievalService,
                                 AiProviderConfigService aiConfigService,
                                 AiWorkflowLogMapper aiWorkflowLogMapper,
                                 OpenAiCompatibleChatClient chatClient,
                                 ObjectMapper objectMapper) {
        this.assistantMapper = assistantMapper;
        this.productService = productService;
        this.aiParser = aiParser;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.aiConfigService = aiConfigService;
        this.aiWorkflowLogMapper = aiWorkflowLogMapper;
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public RecommendationResponse recommend(RecommendationRequest request) {
        List<RecommendationCandidate> candidates = productService
                .listProducts(null, null, request.requirement(), null, null, null, null, null, null, null)
                .stream()
                .limit(3)
                .map(product -> toCandidate(product, request.requirement()))
                .toList();

        if (candidates.isEmpty()) {
            candidates = productService.listProducts(null, null, null, null, null, null, null, null, null, null).stream()
                    .sorted((a, b) -> b.score() - a.score())
                    .limit(3)
                    .map(product -> toCandidate(product, "综合推荐"))
                    .toList();
        }

        return new RecommendationResponse(
                "根据你的预算、用途和风险接受度，优先推荐匹配度高且价格更稳的产品。",
                candidates,
                List.of("二手购买前检查电池健康", "确认保修和维修记录", "价格过低时优先排查渠道风险")
        );
    }

    private RecommendationCandidate toCandidate(ProductListItem product, String reason) {
        return new RecommendationCandidate(product.id(), product.name(), product.score(), reason);
    }

    @Override
    public List<AssistantConversationItem> listConversations(LoginUser user) {
        return assistantMapper.findConversations(user.id());
    }

    @Override
    public AssistantConversationItem createConversation(LoginUser user, ConversationCreateRequest request) {
        String title = normalizeTitle(request != null ? request.title() : null);
        Long id = assistantMapper.insertConversation(user.id(), title);
        return assistantMapper.findConversations(user.id()).stream()
                .filter(item -> Objects.equals(item.id(), id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("会话创建失败"));
    }

    @Override
    public ConversationMessagesResponse getConversationMessages(LoginUser user, Long conversationId) {
        ensureConversationOwner(user, conversationId);
        return new ConversationMessagesResponse(
                conversationId,
                assistantMapper.findMessages(conversationId),
                assistantMapper.findRecommendationsByConversation(conversationId)
        );
    }

    @Override
    @Transactional
    public AssistantChatResponse sendMessage(LoginUser user, Long conversationId, ChatMessageRequest request) {
        ensureConversationOwner(user, conversationId);
        String content = request.content().trim();

        if (assistantMapper.findMessages(conversationId).isEmpty()) {
            assistantMapper.updateConversationTitle(conversationId, buildTitle(content));
        }

        Long userMessageId = assistantMapper.insertMessage(conversationId, user.id(), "USER", content);
        ChatMessageItem userMessage = assistantMapper.findMessage(userMessageId);

        // 尝试用 AI 解析需求
        AiRequirementParserService.ParsedRequirement parsed = aiParser.parse(content);
        List<AiKnowledgeSearchResult> retrievedContexts = knowledgeRetrievalService.search(buildRetrievalQuery(content, parsed), null, null, 8);
        Map<Long, List<AiKnowledgeSearchResult>> contextsByProduct = groupContextsByProduct(retrievedContexts);
        List<RecommendationDraft> drafts = buildRecommendations(content, parsed, contextsByProduct);
        RecommendationEnhancement enhancement = enhanceRecommendationsWithAi(content, parsed, retrievedContexts, drafts);
        drafts = enhancement.drafts();
        Long assistantMessageId = assistantMapper.insertMessage(
                conversationId,
                user.id(),
                "ASSISTANT",
                buildAssistantReply(drafts, !parsed.fallbackUsed() || enhancement.aiUsed())
        );
        List<AssistantRecommendationItem> recommendations = persistRecommendations(conversationId, assistantMessageId, drafts);
        recordWorkflow(user, conversationId, content, parsed, retrievedContexts, drafts);
        assistantMapper.touchConversation(conversationId);

        return new AssistantChatResponse(
                conversationId,
                userMessage,
                assistantMapper.findMessage(assistantMessageId),
                recommendations
        );
    }

    private void ensureConversationOwner(LoginUser user, Long conversationId) {
        if (!assistantMapper.conversationBelongsToUser(conversationId, user.id())) {
            throw new AccessDeniedException("无权访问该会话");
        }
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return DEFAULT_TITLE;
        }
        String trimmed = title.trim();
        return trimmed.length() > 60 ? trimmed.substring(0, 60) : trimmed;
    }

    private String buildTitle(String content) {
        String compact = content.replaceAll("\\s+", " ").trim();
        if (compact.isBlank()) {
            return DEFAULT_TITLE;
        }
        return compact.length() > 24 ? compact.substring(0, 24) + "..." : compact;
    }

    private List<RecommendationDraft> buildRecommendations(
            String requirement,
            AiRequirementParserService.ParsedRequirement parsed,
            Map<Long, List<AiKnowledgeSearchResult>> contextsByProduct
    ) {
        Integer budget = selectedBudget(requirement, parsed);
        String normalized = requirement.toLowerCase(Locale.ROOT);
        String requestedCategory = selectedCategory(normalized, parsed);
        String scoringText = normalized + " " + parsedText(parsed);
        List<ProductListItem> products = productService.listProducts(null, null, null, null, null, null, null, null, null, null);

        return products.stream()
                .filter(product -> requestedCategory == null || categoryMatches(product, requestedCategory))
                .map(product -> buildDraft(
                        product,
                        scoringText,
                        budget,
                        requestedCategory,
                        parsed,
                        contextsByProduct.getOrDefault(product.id(), List.of())
                ))
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt(RecommendationDraft::matchScore).reversed()
                        .thenComparing((RecommendationDraft draft) -> draft.productScore() != null ? draft.productScore() : 0, Comparator.reverseOrder()))
                .limit(4)
                .toList();
    }

    private Map<Long, List<AiKnowledgeSearchResult>> groupContextsByProduct(List<AiKnowledgeSearchResult> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return Map.of();
        }
        return contexts.stream()
                .filter(context -> context.productId() != null)
                .collect(Collectors.groupingBy(
                        AiKnowledgeSearchResult::productId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private RecommendationEnhancement enhanceRecommendationsWithAi(
            String requirement,
            AiRequirementParserService.ParsedRequirement parsed,
            List<AiKnowledgeSearchResult> retrievedContexts,
            List<RecommendationDraft> drafts
    ) {
        if (drafts.isEmpty()) {
            return new RecommendationEnhancement(drafts, false);
        }
        AiProviderConfigItem provider = aiConfigService.getDefaultProvider();
        if (provider == null) {
            return new RecommendationEnhancement(drafts, false);
        }

        try {
            String response = chatClient.chat(
                    provider,
                    recommendationSystemPrompt(),
                    buildRecommendationPayload(requirement, parsed, retrievedContexts, drafts, provider.millionContext())
            );
            List<RecommendationDraft> enhanced = applyRecommendationEnhancement(response, drafts);
            return new RecommendationEnhancement(enhanced, !Objects.equals(enhanced, drafts));
        } catch (RuntimeException e) {
            return new RecommendationEnhancement(drafts, false);
        }
    }

    private String recommendationSystemPrompt() {
        return """
                你是 DigiCompass 数码产品选购助手，负责在后端已筛出的候选产品中生成推荐文案。
                只返回 JSON，不要返回 Markdown、解释文字或多余字段。

                JSON 格式：
                {
                  "items": [
                    {
                      "productId": 1,
                      "reason": "结合用户预算、用途、知识库依据和产品参数说明为什么推荐，40字以内",
                      "riskTip": "给出购买前需要确认的具体风险点，40字以内"
                    }
                  ]
                }

                规则：
                - 只能使用候选产品里的 productId，不要新增产品
                - 不要编造候选产品和知识库中没有的参数、价格、保修或库存
                - reason 要说明匹配用户需求的原因，riskTip 要说明避坑点
                - 如果依据不足，可以保守改写，但仍要返回对应 productId
                """;
    }

    private String buildRecommendationPayload(
            String requirement,
            AiRequirementParserService.ParsedRequirement parsed,
            List<AiKnowledgeSearchResult> retrievedContexts,
            List<RecommendationDraft> drafts,
            boolean millionContext
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userRequirement", requirement);
        payload.put("parsedRequirement", parsed != null ? parsed.data() : Map.of());
        payload.put("ragContext", millionContext ? buildRetrievedContextSummary(retrievedContexts) : truncate(buildRetrievedContextSummary(retrievedContexts), 1200));
        payload.put("candidates", drafts.stream().map(this::candidatePayload).toList());
        return toJson(payload);
    }

    private Map<String, Object> candidatePayload(RecommendationDraft draft) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("productId", draft.productId());
        item.put("productName", draft.productName());
        item.put("brandName", draft.brandName());
        item.put("categoryName", draft.categoryName());
        item.put("officialPrice", draft.officialPrice());
        item.put("usedMinPrice", draft.usedMinPrice());
        item.put("usedMaxPrice", draft.usedMaxPrice());
        item.put("productScore", draft.productScore());
        item.put("matchScore", draft.matchScore());
        item.put("ruleReason", draft.reason());
        item.put("ruleRiskTip", draft.riskTip());
        item.put("explainSummary", draft.explainSummary());
        item.put("matchedRequirements", draft.matchedRequirements());
        item.put("tradeoffNotes", draft.tradeoffNotes());
        item.put("knowledgeEvidence", draft.knowledgeEvidence());
        return item;
    }

    private List<RecommendationDraft> applyRecommendationEnhancement(String response, List<RecommendationDraft> drafts) {
        try {
            JsonNode items = objectMapper.readTree(extractJson(response)).path("items");
            if (!items.isArray()) {
                return drafts;
            }
            Map<Long, JsonNode> byProductId = new LinkedHashMap<>();
            for (JsonNode item : items) {
                Long productId = readLong(item, "productId");
                if (productId != null) {
                    byProductId.put(productId, item);
                }
            }
            if (byProductId.isEmpty()) {
                return drafts;
            }

            List<RecommendationDraft> enhanced = new ArrayList<>();
            for (RecommendationDraft draft : drafts) {
                JsonNode item = byProductId.get(draft.productId());
                enhanced.add(item != null ? withEnhancedText(draft, item) : draft);
            }
            return enhanced;
        } catch (RuntimeException | JsonProcessingException e) {
            return drafts;
        }
    }

    private RecommendationDraft withEnhancedText(RecommendationDraft draft, JsonNode item) {
        String reason = cleanAiText(item.path("reason").asText(null), 120);
        String riskTip = cleanAiText(item.path("riskTip").asText(null), 120);
        return new RecommendationDraft(
                draft.productId(),
                draft.productName(),
                draft.brandName(),
                draft.categoryName(),
                draft.coverUrl(),
                draft.officialPrice(),
                draft.usedMinPrice(),
                draft.usedMaxPrice(),
                draft.productScore(),
                draft.matchScore(),
                reason != null ? reason : draft.reason(),
                riskTip != null ? riskTip : draft.riskTip(),
                draft.explainSummary(),
                draft.matchedRequirements(),
                draft.tradeoffNotes(),
                draft.knowledgeEvidence()
        );
    }

    private String cleanAiText(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.length() > maxLength ? cleaned.substring(0, maxLength) : cleaned;
    }

    private Long readLong(JsonNode item, String field) {
        JsonNode value = item.path(field);
        if (value.isIntegralNumber()) {
            return value.asLong();
        }
        if (value.isTextual() && !value.asText().isBlank()) {
            try {
                return Long.valueOf(value.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String extractJson(String response) {
        String json = response != null ? response.trim() : "";
        if (json.contains("```")) {
            int start = json.indexOf("```");
            int end = json.indexOf("```", start + 3);
            if (end > start) {
                json = json.substring(start + 3, end).trim();
                if (json.startsWith("json")) {
                    json = json.substring(4).trim();
                }
            }
        }
        return json;
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private RecommendationDraft buildDraft(
            ProductListItem product,
            String requirement,
            Integer budget,
            String requestedCategory,
            AiRequirementParserService.ParsedRequirement parsed,
            List<AiKnowledgeSearchResult> productContexts
    ) {
        if (matchesAny(product.brandName(), parsedList(parsed, "avoidedBrands"))) {
            return null;
        }

        ProductDetail detail = productService.getProductDetail(product.id());
        int matchScore = product.score() != null ? product.score() / 2 : 40;
        List<String> reasons = new ArrayList<>();

        if (requestedCategory != null && categoryMatches(product, requestedCategory)) {
            matchScore += 24;
            reasons.add("品类匹配");
        }
        if (matchesText(requirement, product.name(), product.model(), product.summary(), product.brandName())) {
            matchScore += 12;
            reasons.add("关键词匹配");
        }
        if (matchesAny(product.brandName(), parsedList(parsed, "preferredBrands"))) {
            matchScore += 14;
            reasons.add("偏好品牌匹配");
        }
        if (productContexts != null && !productContexts.isEmpty()) {
            matchScore += 18;
            reasons.add("知识库匹配");
        }

        int tagScore = tagScore(requirement, detail.tags());
        if (tagScore > 0) {
            matchScore += tagScore;
            reasons.add("用途标签匹配");
        }

        ProductDetail.PriceInfo usedPrice = firstUsedPrice(detail.prices());
        if (budget != null) {
            int priceScore = priceScore(product.officialPrice(), usedPrice, budget, acceptUsed(requirement, parsed));
            matchScore += priceScore;
            if (priceScore > 0) {
                reasons.add("预算匹配");
            }
        }

        matchScore = Math.max(0, Math.min(100, matchScore));
        String reason = buildReason(product, budget, reasons);
        String riskTip = buildRiskTip(detail.tags(), usedPrice);
        String explainSummary = buildExplainSummary(matchScore, reasons, productContexts);
        List<String> matchedRequirements = buildMatchedRequirements(product, budget, requestedCategory, parsed, reasons);
        List<String> tradeoffNotes = buildTradeoffNotes(product, budget, detail, usedPrice, riskTip);
        List<String> knowledgeEvidence = buildKnowledgeEvidence(product, detail, productContexts);

        return new RecommendationDraft(
                product.id(),
                product.name(),
                product.brandName(),
                product.categoryName(),
                product.coverUrl(),
                product.officialPrice(),
                usedPrice != null ? usedPrice.minPrice() : null,
                usedPrice != null ? usedPrice.maxPrice() : null,
                product.score(),
                matchScore,
                reason,
                riskTip,
                explainSummary,
                matchedRequirements,
                tradeoffNotes,
                knowledgeEvidence
        );
    }

    private Integer selectedBudget(String requirement, AiRequirementParserService.ParsedRequirement parsed) {
        Integer parsedMax = parsedInteger(parsed, "maxBudget");
        if (parsedMax != null) {
            return parsedMax;
        }
        Integer parsedMin = parsedInteger(parsed, "minBudget");
        if (parsedMin != null) {
            return parsedMin;
        }
        return extractBudget(requirement);
    }

    private String selectedCategory(String normalizedRequirement, AiRequirementParserService.ParsedRequirement parsed) {
        String category = normalizeCategory(parsedString(parsed, "categoryKeyword"));
        return category != null ? category : requestedCategory(normalizedRequirement);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) return null;
        if (category.contains("手机")) return "手机";
        if (category.contains("笔记本") || category.contains("电脑")) return "电脑";
        if (category.contains("平板")) return "平板";
        if (category.contains("耳机")) return "耳机";
        if (category.contains("手表") || category.contains("手环")) return "手表";
        return category;
    }

    private boolean acceptUsed(String requirement, AiRequirementParserService.ParsedRequirement parsed) {
        return requirement.contains("二手") || Boolean.TRUE.equals(parsedBoolean(parsed, "acceptUsed"));
    }

    private String parsedText(AiRequirementParserService.ParsedRequirement parsed) {
        if (parsed == null || parsed.data() == null) return "";
        List<String> parts = new ArrayList<>();
        parts.add(parsedString(parsed, "rawKeywords"));
        parts.addAll(parsedList(parsed, "usageScenes"));
        parts.addAll(parsedList(parsed, "priorityTags"));
        parts.addAll(parsedList(parsed, "avoidTags"));
        parts.addAll(parsedList(parsed, "preferredBrands"));
        return String.join(" ", parts).toLowerCase(Locale.ROOT);
    }

    private String buildRetrievalQuery(String content, AiRequirementParserService.ParsedRequirement parsed) {
        List<String> parts = new ArrayList<>();
        parts.add(content);
        parts.add(parsedString(parsed, "categoryKeyword"));
        parts.add(parsedString(parsed, "rawKeywords"));
        parts.addAll(parsedList(parsed, "usageScenes"));
        parts.addAll(parsedList(parsed, "priorityTags"));
        parts.addAll(parsedList(parsed, "preferredBrands"));
        return parts.stream()
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String parsedString(AiRequirementParserService.ParsedRequirement parsed, String key) {
        Object value = parsedValue(parsed, key);
        return value != null ? String.valueOf(value) : null;
    }

    private Integer parsedInteger(AiRequirementParserService.ParsedRequirement parsed, String key) {
        Object value = parsedValue(parsed, key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank() && !"null".equalsIgnoreCase(text)) {
            try {
                return new BigDecimal(text).intValue();
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Boolean parsedBoolean(AiRequirementParserService.ParsedRequirement parsed, String key) {
        Object value = parsedValue(parsed, key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return null;
    }

    private List<String> parsedList(AiRequirementParserService.ParsedRequirement parsed, String key) {
        Object value = parsedValue(parsed, key);
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .filter(text -> !text.isBlank())
                    .toList();
        }
        if (value instanceof String text && !text.isBlank() && !"[]".equals(text)) {
            String normalized = text.replace("[", "").replace("]", "").replace("\"", "");
            return Pattern.compile("[,，、\\s]+")
                    .splitAsStream(normalized)
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .toList();
        }
        return List.of();
    }

    private Object parsedValue(AiRequirementParserService.ParsedRequirement parsed, String key) {
        Map<String, Object> data = parsed != null ? parsed.data() : null;
        return data != null ? data.get(key) : null;
    }

    private boolean matchesAny(String source, List<String> candidates) {
        if (source == null || source.isBlank() || candidates.isEmpty()) {
            return false;
        }
        String normalizedSource = source.toLowerCase(Locale.ROOT);
        return candidates.stream()
                .filter(candidate -> candidate != null && !candidate.isBlank())
                .map(candidate -> candidate.toLowerCase(Locale.ROOT))
                .anyMatch(candidate -> normalizedSource.contains(candidate) || candidate.contains(normalizedSource));
    }

    private Integer extractBudget(String requirement) {
        Matcher matcher = BUDGET_PATTERN.matcher(requirement);
        Integer budget = null;
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            if (budget == null || value > budget) {
                budget = value;
            }
        }
        return budget;
    }

    private String requestedCategory(String requirement) {
        if (requirement.contains("手机")) return "手机";
        if (requirement.contains("笔记本") || requirement.contains("电脑")) return "电脑";
        if (requirement.contains("平板")) return "平板";
        if (requirement.contains("耳机")) return "耳机";
        if (requirement.contains("手表")) return "手表";
        return null;
    }

    private boolean categoryMatches(ProductListItem product, String requestedCategory) {
        String categoryName = product.categoryName() != null ? product.categoryName() : "";
        return categoryName.contains(requestedCategory);
    }

    private boolean matchesText(String requirement, String... texts) {
        for (String text : texts) {
            if (text != null && !text.isBlank() && requirement.contains(text.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private int tagScore(String requirement, List<ProductTagItem> tags) {
        int score = 0;
        for (ProductTagItem tag : tags) {
            String text = ((tag.tagName() != null ? tag.tagName() : "") + " " + (tag.tagValue() != null ? tag.tagValue() : "")).toLowerCase(Locale.ROOT);
            if (matchesIntent(requirement, text, "拍照", "影像", "相机", "视频", "旅行")) score += 20;
            if (matchesIntent(requirement, text, "游戏", "性能", "高负载")) score += 18;
            if (matchesIntent(requirement, text, "办公", "学习", "网课", "文档", "通勤")) score += 16;
            if (matchesIntent(requirement, text, "轻薄", "便携", "小屏")) score += 12;
            if (matchesIntent(requirement, text, "续航", "电池", "充电")) score += 12;
        }
        return Math.min(score, 30);
    }

    private boolean matchesIntent(String requirement, String source, String... words) {
        boolean requirementHit = false;
        boolean sourceHit = false;
        for (String word : words) {
            requirementHit = requirementHit || requirement.contains(word);
            sourceHit = sourceHit || source.contains(word);
        }
        return requirementHit && sourceHit;
    }

    private int priceScore(BigDecimal officialPrice, ProductDetail.PriceInfo usedPrice, Integer budget, boolean acceptUsed) {
        BigDecimal budgetValue = BigDecimal.valueOf(budget);
        if (officialPrice != null && officialPrice.compareTo(budgetValue) <= 0) {
            return 20;
        }
        if (acceptUsed && usedPrice != null && usedPrice.minPrice() != null && usedPrice.minPrice().compareTo(budgetValue) <= 0) {
            return 16;
        }
        if (officialPrice != null && officialPrice.compareTo(budgetValue.multiply(BigDecimal.valueOf(1.2))) > 0) {
            return -32;
        }
        return 0;
    }

    private ProductDetail.PriceInfo firstUsedPrice(List<ProductDetail.PriceInfo> prices) {
        return prices.stream()
                .filter(price -> "used".equalsIgnoreCase(price.priceType()))
                .findFirst()
                .orElse(null);
    }

    private String buildReason(ProductListItem product, Integer budget, List<String> reasons) {
        String priceText = product.officialPrice() != null
                ? "官方价约 " + product.officialPrice().setScale(0, RoundingMode.HALF_UP) + " 元"
                : "官方价暂缺";
        String reasonText = reasons.isEmpty() ? "综合评分较高" : String.join("、", reasons);
        String budgetText = budget != null ? "，已参考 " + budget + " 元预算" : "";
        return reasonText + "，" + priceText + budgetText + "。";
    }

    private String buildRiskTip(List<ProductTagItem> tags, ProductDetail.PriceInfo usedPrice) {
        return tags.stream()
                .filter(tag -> "weakness".equals(tag.tagType()) || "unsuitable".equals(tag.tagType()))
                .findFirst()
                .map(tag -> tag.tagValue() != null && !tag.tagValue().isBlank() ? tag.tagValue() : tag.tagName())
                .orElse(usedPrice != null ? "二手购买前重点确认成色、电池健康、保修和维修记录。" : "购买前建议对比价格、售后和平台保修政策。");
    }

    private String buildExplainSummary(Integer matchScore, List<String> reasons, List<AiKnowledgeSearchResult> productContexts) {
        String source = productContexts != null && !productContexts.isEmpty() ? "RAG 知识库和产品库规则" : "产品库规则";
        String reasonText = reasons == null || reasons.isEmpty()
                ? "综合评分"
                : reasons.stream().limit(3).collect(Collectors.joining("、"));
        return "推荐依据：" + source + "命中 " + reasonText + "，本次匹配度 " + matchScore + "。";
    }

    private List<String> buildMatchedRequirements(
            ProductListItem product,
            Integer budget,
            String requestedCategory,
            AiRequirementParserService.ParsedRequirement parsed,
            List<String> reasons
    ) {
        List<String> matches = new ArrayList<>();
        if (budget != null) addUnique(matches, "预算参考：" + budget + " 元");
        if (requestedCategory != null) addUnique(matches, "目标品类：" + requestedCategory);
        for (String scene : parsedList(parsed, "usageScenes")) {
            addUnique(matches, "使用场景：" + scene);
        }
        for (String tag : parsedList(parsed, "priorityTags")) {
            addUnique(matches, "关注点：" + tag);
        }
        if (matchesAny(product.brandName(), parsedList(parsed, "preferredBrands"))) {
            addUnique(matches, "品牌偏好：" + product.brandName());
        }
        if (reasons != null && reasons.contains("知识库匹配")) {
            addUnique(matches, "知识库命中：" + product.name());
        }
        if (matches.isEmpty()) {
            addUnique(matches, "产品评分：" + (product.score() != null ? product.score() : "暂无"));
        }
        return limitList(matches, 5);
    }

    private List<String> buildTradeoffNotes(
            ProductListItem product,
            Integer budget,
            ProductDetail detail,
            ProductDetail.PriceInfo usedPrice,
            String riskTip
    ) {
        List<String> notes = new ArrayList<>();
        if (budget != null && product.officialPrice() != null) {
            BigDecimal budgetValue = BigDecimal.valueOf(budget);
            if (product.officialPrice().compareTo(budgetValue) > 0) {
                addUnique(notes, "官方价高于预算，建议重点比较二手价或同价位替代产品。");
            } else if (product.officialPrice().compareTo(budgetValue.multiply(BigDecimal.valueOf(0.9))) >= 0) {
                addUnique(notes, "价格接近预算上限，购买前建议确认活动价和售后政策。");
            }
        }
        if (usedPrice == null) {
            addUnique(notes, "暂无二手行情参考，二手购买需要单独核对成交价。");
        }
        if (detail.purchaseLinks() == null || detail.purchaseLinks().isEmpty()) {
            addUnique(notes, "暂无购买链接，建议从官方渠道或可信平台核验。");
        }
        if (riskTip != null && !riskTip.isBlank()) {
            addUnique(notes, riskTip);
        }
        if (notes.isEmpty()) {
            addUnique(notes, "建议加入对比后查看参数、价格和购买渠道差异。");
        }
        return limitList(notes, 4);
    }

    private List<String> buildKnowledgeEvidence(
            ProductListItem product,
            ProductDetail detail,
            List<AiKnowledgeSearchResult> productContexts
    ) {
        List<String> evidence = new ArrayList<>();
        if (productContexts != null) {
            productContexts.stream()
                    .limit(2)
                    .map(context -> {
                        String title = context.title() != null ? context.title() : "知识片段";
                        String content = context.content() != null ? context.content().replaceAll("\\s+", " ").trim() : "";
                        return title + "：" + truncate(content, 64);
                    })
                    .forEach(item -> addUnique(evidence, item));
        }
        if (evidence.isEmpty() && product.summary() != null && !product.summary().isBlank()) {
            addUnique(evidence, "产品摘要：" + truncate(product.summary(), 64));
        }
        if (evidence.isEmpty() && detail.tags() != null) {
            detail.tags().stream()
                    .filter(tag -> tag.tagValue() != null && !tag.tagValue().isBlank())
                    .limit(2)
                    .map(tag -> tag.tagName() + "：" + tag.tagValue())
                    .forEach(item -> addUnique(evidence, item));
        }
        if (evidence.isEmpty() && detail.specs() != null) {
            detail.specs().stream()
                    .limit(2)
                    .map(spec -> spec.name() + "：" + spec.value())
                    .forEach(item -> addUnique(evidence, item));
        }
        return limitList(evidence, 3);
    }

    private void addUnique(List<String> values, String value) {
        if (value != null && !value.isBlank() && !values.contains(value)) {
            values.add(value);
        }
    }

    private List<String> limitList(List<String> values, int max) {
        return values.size() > max ? values.subList(0, max) : values;
    }

    private String buildAssistantReply(List<RecommendationDraft> drafts, boolean aiUsed) {
        if (drafts.isEmpty()) {
            return "当前产品库里没有找到足够匹配的产品，可以换一个预算、用途或品类再试。";
        }
        RecommendationDraft first = drafts.getFirst();
        String prefix = aiUsed ? "根据 AI 分析你的需求，" : "";
        return prefix + "我从产品库中筛选出 " + drafts.size() + " 个候选产品，优先推荐 "
                + first.productName() + "。下面的卡片可以直接进入详情页，也可以加入对比后再看差异。";
    }

    private void recordWorkflow(
            LoginUser user,
            Long conversationId,
            String content,
            AiRequirementParserService.ParsedRequirement parsed,
            List<AiKnowledgeSearchResult> retrievedContexts,
            List<RecommendationDraft> drafts
    ) {
        AiProviderConfigItem provider = aiConfigService.getDefaultProvider();
        aiWorkflowLogMapper.insert(
                user.id(),
                conversationId,
                provider != null ? provider.id() : null,
                provider != null ? provider.chatModel() : null,
                content,
                toJson(parsed != null ? parsed.data() : Map.of()),
                buildRetrievedContextSummary(retrievedContexts),
                drafts.stream()
                        .map(RecommendationDraft::productId)
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .reduce((left, right) -> left + "," + right)
                        .orElse(""),
                parsed != null && parsed.fallbackUsed(),
                parsed != null && parsed.fallbackUsed() ? "AI 未配置、调用失败或返回格式异常，已使用规则解析" : null
        );
    }

    private String buildRetrievedContextSummary(List<AiKnowledgeSearchResult> retrievedContexts) {
        if (retrievedContexts == null || retrievedContexts.isEmpty()) {
            return "";
        }
        return retrievedContexts.stream()
                .limit(3)
                .map(context -> {
                    String title = context.title() != null ? context.title() : "未命名知识";
                    String content = context.content() != null ? context.content().replaceAll("\\s+", " ").trim() : "";
                    String source = context.retrievalSource() != null ? context.retrievalSource() : "UNKNOWN";
                    String model = context.embeddingModel() != null && !context.embeddingModel().isBlank()
                            ? "，model=" + context.embeddingModel()
                            : "";
                    return "[" + source + " score=" + context.score() + model + "] "
                            + title + "：" + (content.length() > 80 ? content.substring(0, 80) + "..." : content);
                })
                .collect(Collectors.joining("\n"));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private List<AssistantRecommendationItem> persistRecommendations(
            Long conversationId,
            Long assistantMessageId,
            List<RecommendationDraft> drafts
    ) {
        List<AssistantRecommendationItem> result = new ArrayList<>();
        for (int i = 0; i < drafts.size(); i++) {
            RecommendationDraft draft = drafts.get(i);
            Long id = assistantMapper.insertRecommendation(
                    conversationId,
                    assistantMessageId,
                    draft.productId(),
                    draft.productName(),
                    draft.brandName(),
                    draft.categoryName(),
                    draft.coverUrl(),
                    draft.officialPrice(),
                    draft.usedMinPrice(),
                    draft.usedMaxPrice(),
                    draft.productScore(),
                    draft.matchScore(),
                    draft.reason(),
                    draft.riskTip(),
                    draft.explainSummary(),
                    draft.matchedRequirements(),
                    draft.tradeoffNotes(),
                    draft.knowledgeEvidence(),
                    i + 1
            );
            result.add(assistantMapper.findRecommendation(id));
        }
        return result;
    }

    private record RecommendationEnhancement(List<RecommendationDraft> drafts, boolean aiUsed) {
    }

    private record RecommendationDraft(
            Long productId,
            String productName,
            String brandName,
            String categoryName,
            String coverUrl,
            BigDecimal officialPrice,
            BigDecimal usedMinPrice,
            BigDecimal usedMaxPrice,
            Integer productScore,
            Integer matchScore,
            String reason,
            String riskTip,
            String explainSummary,
            List<String> matchedRequirements,
            List<String> tradeoffNotes,
            List<String> knowledgeEvidence
    ) {
    }
}
