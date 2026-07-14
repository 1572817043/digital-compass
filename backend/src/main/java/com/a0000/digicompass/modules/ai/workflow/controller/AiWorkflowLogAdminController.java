package com.a0000.digicompass.modules.ai.workflow.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowLogDetail;
import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowLogItem;
import com.a0000.digicompass.modules.ai.workflow.service.AiWorkflowLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ai/workflow-logs")
public class AiWorkflowLogAdminController {

    private final AiWorkflowLogService service;

    public AiWorkflowLogAdminController(AiWorkflowLogService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AiWorkflowLogItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean fallbackUsed,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(service.listLogs(keyword, fallbackUsed, limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<AiWorkflowLogDetail> detail(@PathVariable Long id) {
        return ApiResponse.success(service.getDetail(id));
    }
}
