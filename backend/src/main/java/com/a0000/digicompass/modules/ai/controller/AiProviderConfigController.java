package com.a0000.digicompass.modules.ai.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.ai.dto.AiModelItem;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;
import com.a0000.digicompass.modules.ai.dto.AiProviderConfigSaveRequest;
import com.a0000.digicompass.modules.ai.dto.AiTestResult;
import com.a0000.digicompass.modules.ai.service.AiProviderConfigService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ai/providers")
public class AiProviderConfigController {

    private final AiProviderConfigService service;

    public AiProviderConfigController(AiProviderConfigService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AiProviderConfigItem>> list() {
        return ApiResponse.success(service.listProviders());
    }

    @GetMapping("/{id}")
    public ApiResponse<AiProviderConfigItem> get(@PathVariable Long id) {
        return ApiResponse.success(service.getProvider(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody AiProviderConfigSaveRequest request) {
        return ApiResponse.success(service.createProvider(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody AiProviderConfigSaveRequest request) {
        service.updateProvider(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.deleteProvider(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/default")
    public ApiResponse<Void> setDefault(@PathVariable Long id) {
        service.setDefault(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<AiTestResult> test(@PathVariable Long id) {
        return ApiResponse.success(service.testConnection(id));
    }

    @GetMapping("/{id}/models")
    public ApiResponse<List<AiModelItem>> models(@PathVariable Long id) {
        return ApiResponse.success(service.fetchModels(id));
    }

    @PostMapping("/models")
    public ApiResponse<List<AiModelItem>> models(@Valid @RequestBody AiProviderConfigSaveRequest request) {
        return ApiResponse.success(service.fetchModels(request));
    }
}
