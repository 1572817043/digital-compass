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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OpenAiCompatibleEmbeddingClient implements AiEmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleEmbeddingClient.class);

    private final AiKeyCrypto aiKeyCrypto;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatibleEmbeddingClient(AiKeyCrypto aiKeyCrypto, ObjectMapper objectMapper) {
        this.aiKeyCrypto = aiKeyCrypto;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public double[] embed(AiProviderConfigItem config, String input) {
        String apiKey = aiKeyCrypto.decrypt(config.maskedApiKey());
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiCallException("API Key 未配置或解密失败");
        }
        if (config.embeddingModel() == null || config.embeddingModel().isBlank()) {
            throw new AiCallException("Embedding 模型未配置");
        }

        String url = OpenAiEndpointResolver.embeddingsUrl(config.baseUrl());
        try {
            Map<String, Object> body = Map.of(
                    "model", config.embeddingModel(),
                    "input", input
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(config.timeoutSeconds() != null ? config.timeoutSeconds() : 30))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new AiCallException("Embedding 调用失败 (HTTP " + response.statusCode() + ")");
            }

            JsonNode embeddingNode = objectMapper.readTree(response.body()).path("data").path(0).path("embedding");
            if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                throw new AiCallException("Embedding 响应格式异常");
            }
            List<Double> values = new ArrayList<>();
            embeddingNode.forEach(node -> values.add(node.asDouble()));
            double[] vector = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vector[i] = values.get(i);
            }
            return normalize(vector);
        } catch (AiCallException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Embedding 调用异常: provider={}, error={}", config.providerCode(), e.getMessage());
            throw new AiCallException("Embedding 调用异常: " + e.getMessage(), e);
        }
    }

    private double[] normalize(double[] vector) {
        double norm = 0;
        for (double value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm == 0) return vector;
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
        return vector;
    }
}
