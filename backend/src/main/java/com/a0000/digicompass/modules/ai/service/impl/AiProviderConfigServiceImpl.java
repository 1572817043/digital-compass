package com.a0000.digicompass.modules.ai.service.impl;

import com.a0000.digicompass.modules.ai.client.AiCallException;
import com.a0000.digicompass.modules.ai.client.OpenAiCompatibleChatClient;
import com.a0000.digicompass.modules.ai.client.OpenAiCompatibleModelClient;
import com.a0000.digicompass.modules.ai.crypto.AiKeyCrypto;
import com.a0000.digicompass.modules.ai.dto.AiModelItem;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigSaveRequest;
import com.a0000.digicompass.modules.ai.dto.AiTestResult;
import com.a0000.digicompass.modules.ai.mapper.AiProviderConfigMapper;
import com.a0000.digicompass.modules.ai.service.AiProviderConfigService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AiProviderConfigServiceImpl implements AiProviderConfigService {

    private static final Logger log = LoggerFactory.getLogger(AiProviderConfigServiceImpl.class);

    private final AiProviderConfigMapper mapper;
    private final AiKeyCrypto aiKeyCrypto;
    private final OpenAiCompatibleChatClient chatClient;
    private final OpenAiCompatibleModelClient modelClient;

    public AiProviderConfigServiceImpl(AiProviderConfigMapper mapper, AiKeyCrypto aiKeyCrypto, OpenAiCompatibleChatClient chatClient, OpenAiCompatibleModelClient modelClient) {
        this.mapper = mapper;
        this.aiKeyCrypto = aiKeyCrypto;
        this.chatClient = chatClient;
        this.modelClient = modelClient;
    }

    @Override
    public List<AiProviderConfigItem> listProviders() {
        return mapper.findAll().stream().map(this::maskApiKey).toList();
    }

    @Override
    public AiProviderConfigItem getProvider(Long id) {
        AiProviderConfigItem item = mapper.findById(id);
        if (item == null) throw new IllegalArgumentException("AI 配置不存在");
        return maskApiKey(item);
    }

    @Override
    public AiProviderConfigItem getDefaultProvider() {
        return mapper.findDefault();
    }

    @Override
    public Long createProvider(AiProviderConfigSaveRequest request) {
        String cipher = request.apiKey() != null && !request.apiKey().isBlank()
                ? aiKeyCrypto.encrypt(request.apiKey()) : null;
        boolean isDefault = Boolean.TRUE.equals(request.defaultProvider());
        if (isDefault) mapper.clearDefault();
        return mapper.insert(
                request.providerCode(), request.providerName(), normalizeProtocol(request.protocolType()), request.baseUrl(), cipher,
                request.chatModel(), request.embeddingModel(),
                request.temperature() != null ? request.temperature() : 0.7,
                request.maxTokens() != null ? request.maxTokens() : 2048,
                Boolean.TRUE.equals(request.millionContext()),
                request.timeoutSeconds() != null ? request.timeoutSeconds() : 30,
                request.enabled() == null || request.enabled(),
                isDefault, request.remark()
        );
    }

    @Override
    public void updateProvider(Long id, AiProviderConfigSaveRequest request) {
        AiProviderConfigItem existing = mapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("AI 配置不存在");

        String cipher;
        if (request.apiKey() != null && !request.apiKey().isBlank()) {
            cipher = aiKeyCrypto.encrypt(request.apiKey());
        } else {
            cipher = existing.maskedApiKey();
        }

        boolean isDefault = Boolean.TRUE.equals(request.defaultProvider());
        if (isDefault) mapper.clearDefault();

        mapper.update(id,
                request.providerCode() != null ? request.providerCode() : existing.providerCode(),
                request.providerName() != null ? request.providerName() : existing.providerName(),
                request.protocolType() != null ? normalizeProtocol(request.protocolType()) : existing.protocolType(),
                request.baseUrl() != null ? request.baseUrl() : existing.baseUrl(),
                cipher,
                request.chatModel() != null ? request.chatModel() : existing.chatModel(),
                request.embeddingModel() != null ? request.embeddingModel() : existing.embeddingModel(),
                request.temperature() != null ? request.temperature() : existing.temperature(),
                request.maxTokens() != null ? request.maxTokens() : existing.maxTokens(),
                request.millionContext() != null ? request.millionContext() : existing.millionContext(),
                request.timeoutSeconds() != null ? request.timeoutSeconds() : existing.timeoutSeconds(),
                request.enabled() != null ? request.enabled() : existing.enabled(),
                isDefault,
                request.remark() != null ? request.remark() : existing.remark()
        );
    }

    @Override
    public void deleteProvider(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public void setDefault(Long id) {
        mapper.clearDefault();
        mapper.setDefault(id);
    }

    @Override
    public AiTestResult testConnection(Long id) {
        AiProviderConfigItem config = mapper.findById(id);
        if (config == null) return new AiTestResult(false, null, null, "配置不存在");
        try {
            String response = chatClient.chat(config, "只返回 OK", "请回复 OK");
            String preview = response.length() > 200 ? response.substring(0, 200) + "..." : response;
            return new AiTestResult(true, config.chatModel(), preview, null);
        } catch (AiCallException e) {
            log.warn("AI 测试连接失败: provider={}, error={}", config.providerCode(), e.getMessage());
            return new AiTestResult(false, config.chatModel(), null, e.getMessage());
        } catch (Exception e) {
            return new AiTestResult(false, config.chatModel(), null, "未知错误: " + e.getMessage());
        }
    }

    @Override
    public List<AiModelItem> fetchModels(Long id) {
        AiProviderConfigItem config = mapper.findById(id);
        if (config == null) throw new IllegalArgumentException("AI 配置不存在");
        ensureOpenAiCompatible(config.protocolType());
        return modelClient.listModels(config);
    }

    @Override
    public List<AiModelItem> fetchModels(AiProviderConfigSaveRequest request) {
        ensureOpenAiCompatible(normalizeProtocol(request.protocolType()));
        String cipher = request.apiKey() != null && !request.apiKey().isBlank()
                ? aiKeyCrypto.encrypt(request.apiKey()) : null;
        AiProviderConfigItem config = new AiProviderConfigItem(
                null,
                request.providerCode(),
                request.providerName(),
                normalizeProtocol(request.protocolType()),
                request.baseUrl(),
                cipher,
                request.chatModel(),
                request.embeddingModel(),
                request.temperature(),
                request.maxTokens(),
                Boolean.TRUE.equals(request.millionContext()),
                request.timeoutSeconds(),
                true,
                false,
                request.remark()
        );
        return modelClient.listModels(config);
    }

    private AiProviderConfigItem maskApiKey(AiProviderConfigItem item) {
        String plainKey = null;
        try {
            plainKey = aiKeyCrypto.decrypt(item.maskedApiKey());
        } catch (Exception e) {
            log.warn("AI Key 脱敏解密失败: provider={}", item.providerCode());
        }
        String masked = aiKeyCrypto.mask(plainKey);
        return new AiProviderConfigItem(
                item.id(), item.providerCode(), item.providerName(), item.protocolType(), item.baseUrl(), masked,
                item.chatModel(), item.embeddingModel(), item.temperature(), item.maxTokens(),
                item.millionContext(), item.timeoutSeconds(), item.enabled(), item.defaultProvider(), item.remark()
        );
    }

    private String normalizeProtocol(String protocolType) {
        return protocolType == null || protocolType.isBlank() ? "openai-compatible" : protocolType;
    }

    private void ensureOpenAiCompatible(String protocolType) {
        if (!"openai-compatible".equals(normalizeProtocol(protocolType))) {
            throw new IllegalArgumentException("当前只支持 OpenAI 兼容协议获取模型列表");
        }
    }
}
