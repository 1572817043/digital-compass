package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;

import com.a0000.digicompass.modules.ai.client.OpenAiEndpointResolver;
import org.junit.jupiter.api.Test;

class AiEndpointResolverTests {

    @Test
    void doesNotDuplicateV1WhenBaseUrlAlreadyContainsVersionPath() {
        assertThat(OpenAiEndpointResolver.chatCompletionsUrl("https://api.xiaomimimo.com/v1"))
                .isEqualTo("https://api.xiaomimimo.com/v1/chat/completions");
    }

    @Test
    void appendsV1WhenBaseUrlIsProviderRoot() {
        assertThat(OpenAiEndpointResolver.embeddingsUrl("https://api.xiaomimimo.com"))
                .isEqualTo("https://api.xiaomimimo.com/v1/embeddings");
    }
}
