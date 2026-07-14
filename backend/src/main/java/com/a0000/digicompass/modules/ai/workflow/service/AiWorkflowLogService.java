package com.a0000.digicompass.modules.ai.workflow.service;

import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowLogItem;
import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowLogDetail;
import java.util.List;

public interface AiWorkflowLogService {

    List<AiWorkflowLogItem> listLogs(String keyword, Boolean fallbackUsed, Integer limit);

    AiWorkflowLogDetail getDetail(Long id);
}
