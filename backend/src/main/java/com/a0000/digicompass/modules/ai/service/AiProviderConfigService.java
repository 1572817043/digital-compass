package com.a0000.digicompass.modules.ai.service;

import com.a0000.digicompass.modules.ai.dto.AiModelItem;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigSaveRequest;
import com.a0000.digicompass.modules.ai.dto.AiTestResult;
import java.util.List;

public interface AiProviderConfigService {

    List<AiProviderConfigItem> listProviders();

    AiProviderConfigItem getProvider(Long id);

    AiProviderConfigItem getDefaultProvider();

    Long createProvider(AiProviderConfigSaveRequest request);

    void updateProvider(Long id, AiProviderConfigSaveRequest request);

    void deleteProvider(Long id);

    void setDefault(Long id);

    AiTestResult testConnection(Long id);

    List<AiModelItem> fetchModels(Long id);

    List<AiModelItem> fetchModels(AiProviderConfigSaveRequest request);
}
