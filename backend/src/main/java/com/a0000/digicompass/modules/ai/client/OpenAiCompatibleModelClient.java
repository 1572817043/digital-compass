package com.a0000.digicompass.modules.ai.client;

import com.a0000.digicompass.modules.ai.crypto.AiKeyCrypto;
import com.a0000.digicompass.modules.ai.dto.AiModelItem;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OpenAiCompatibleModelClient {

    private final AiKeyCrypto aiKeyCrypto;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatibleModelClient(AiKeyCrypto aiKeyCrypto, ObjectMapper objectMapper) {
        this.aiKeyCrypto = aiKeyCrypto;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<AiModelItem> listModels(AiProviderConfigItem config) {
        String apiKey = aiKeyCrypto.decrypt(config.maskedApiKey());
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiCallException("API Key 未配置或解密失败");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OpenAiEndpointResolver.modelsUrl(config.baseUrl())))
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(config.timeoutSeconds() != null ? config.timeoutSeconds() : 30))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new AiCallException("获取模型列表失败 (HTTP " + response.statusCode() + ")");
            }
            JsonNode data = objectMapper.readTree(response.body()).path("data");
            if (!data.isArray()) {
                throw new AiCallException("模型列表响应格式异常");
            }
            List<AiModelItem> models = new ArrayList<>();
            for (JsonNode node : data) {
                String id = node.path("id").asText("");
                if (!id.isBlank()) {
                    models.add(new AiModelItem(id, node.path("owned_by").asText("")));
                }
            }
            return models;
        } catch (AiCallException e) {
            throw e;
        } catch (Exception e) {
            throw new AiCallException("获取模型列表异常: " + e.getMessage(), e);
        }
    }
}
