package com.a0000.digicompass.modules.ai.client;

import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;

public interface AiEmbeddingClient {

    double[] embed(AiProviderConfigItem config, String input);
}
