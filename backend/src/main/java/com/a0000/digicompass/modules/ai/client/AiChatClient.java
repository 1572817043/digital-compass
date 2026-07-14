package com.a0000.digicompass.modules.ai.client;

import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;

public interface AiChatClient {

    String chat(AiProviderConfigItem config, String systemPrompt, String userMessage);
}
