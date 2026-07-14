package com.a0000.digicompass.modules.ai.client;

import com.a0000.digicompass.modules.ai.crypto.AiKeyCrypto;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OpenAiCompatibleChatClient implements AiChatClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleChatClient.class);

    private final AiKeyCrypto aiKeyCrypto;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatibleChatClient(AiKeyCrypto aiKeyCrypto, ObjectMapper objectMapper) {
        this.aiKeyCrypto = aiKeyCrypto;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String chat(AiProviderConfigItem config, String systemPrompt, String userMessage) {
        String apiKey = aiKeyCrypto.decrypt(config.maskedApiKey());
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiCallException("API Key 未配置或解密失败");
        }

        String url = OpenAiEndpointResolver.chatCompletionsUrl(config.baseUrl());

        try {
            Map<String, Object> body = Map.of(
                    "model", config.chatModel() != null ? config.chatModel() : "gpt-3.5-turbo",
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", config.temperature() != null ? config.temperature() : 0.7,
                    "max_tokens", config.maxTokens() != null ? config.maxTokens() : 2048
            );

            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(config.timeoutSeconds() != null ? config.timeoutSeconds() : 30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            log.info("AI 调用: provider={}, model={}, url={}", config.providerCode(), config.chatModel(), url);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    String content = choices.get(0).path("message").path("content").asText("");
                    log.info("AI 调用成功: provider={}, response_length={}", config.providerCode(), content.length());
                    return content;
                }
                throw new AiCallException("AI 响应格式异常: choices 为空");
            } else if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new AiCallException("API Key 无效或无权限 (HTTP " + response.statusCode() + ")");
            } else if (response.statusCode() == 429) {
                throw new AiCallException("请求频率超限 (HTTP 429)");
            } else {
                throw new AiCallException("AI 调用失败 (HTTP " + response.statusCode() + "): " + response.body().substring(0, Math.min(200, response.body().length())));
            }
        } catch (AiCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 调用异常: provider={}, error={}", config.providerCode(), e.getMessage());
            throw new AiCallException("AI 调用异常: " + e.getMessage(), e);
        }
    }
}
