package com.a0000.digicompass.modules.assistant.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.assistant.dto.AssistantChatResponse;
import com.a0000.digicompass.modules.assistant.dto.AssistantConversationItem;
import com.a0000.digicompass.modules.assistant.dto.ChatMessageRequest;
import com.a0000.digicompass.modules.assistant.dto.ConversationCreateRequest;
import com.a0000.digicompass.modules.assistant.dto.ConversationMessagesResponse;
import com.a0000.digicompass.modules.assistant.dto.RecommendationRequest;
import com.a0000.digicompass.modules.assistant.dto.RecommendationResponse;
import com.a0000.digicompass.modules.assistant.service.AssistantService;
import com.a0000.digicompass.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final AuthService authService;

    public AssistantController(AssistantService assistantService, AuthService authService) {
        this.assistantService = assistantService;
        this.authService = authService;
    }

    @PostMapping("/recommend")
    public ApiResponse<RecommendationResponse> recommend(@Valid @RequestBody RecommendationRequest request) {
        return ApiResponse.success(assistantService.recommend(request));
    }

    @GetMapping("/conversations")
    public ApiResponse<List<AssistantConversationItem>> listConversations() {
        return ApiResponse.success(assistantService.listConversations(authService.currentUser()));
    }

    @PostMapping("/conversations")
    public ApiResponse<AssistantConversationItem> createConversation(@RequestBody(required = false) ConversationCreateRequest request) {
        return ApiResponse.success(assistantService.createConversation(authService.currentUser(), request));
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<ConversationMessagesResponse> getMessages(@PathVariable Long id) {
        return ApiResponse.success(assistantService.getConversationMessages(authService.currentUser(), id));
    }

    @PostMapping("/conversations/{id}/messages")
    public ApiResponse<AssistantChatResponse> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        return ApiResponse.success(assistantService.sendMessage(authService.currentUser(), id, request));
    }
}
