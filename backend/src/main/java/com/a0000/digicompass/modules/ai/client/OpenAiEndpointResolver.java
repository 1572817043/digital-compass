package com.a0000.digicompass.modules.ai.client;

public final class OpenAiEndpointResolver {

    private OpenAiEndpointResolver() {
    }

    public static String chatCompletionsUrl(String baseUrl) {
        return versionedUrl(baseUrl, "/chat/completions");
    }

    public static String embeddingsUrl(String baseUrl) {
        return versionedUrl(baseUrl, "/embeddings");
    }

    public static String modelsUrl(String baseUrl) {
        return versionedUrl(baseUrl, "/models");
    }

    private static String versionedUrl(String baseUrl, String path) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        if (normalized.endsWith("/v1")) {
            return normalized + path;
        }
        return normalized + "/v1" + path;
    }
}
