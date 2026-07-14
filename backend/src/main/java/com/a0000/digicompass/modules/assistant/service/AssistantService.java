package com.a0000.digicompass.modules.assistant.service;

import com.a0000.digicompass.modules.assistant.dto.AssistantChatResponse;
import com.a0000.digicompass.modules.assistant.dto.AssistantConversationItem;
import com.a0000.digicompass.modules.assistant.dto.ChatMessageRequest;
import com.a0000.digicompass.modules.assistant.dto.ConversationCreateRequest;
import com.a0000.digicompass.modules.assistant.dto.ConversationMessagesResponse;
import com.a0000.digicompass.modules.assistant.dto.RecommendationRequest;
import com.a0000.digicompass.modules.assistant.dto.RecommendationResponse;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import java.util.List;

public interface AssistantService {

    RecommendationResponse recommend(RecommendationRequest request);

    List<AssistantConversationItem> listConversations(LoginUser user);

    AssistantConversationItem createConversation(LoginUser user, ConversationCreateRequest request);

    ConversationMessagesResponse getConversationMessages(LoginUser user, Long conversationId);

    AssistantChatResponse sendMessage(LoginUser user, Long conversationId, ChatMessageRequest request);
}
