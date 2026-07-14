import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type {
  AssistantChatResponse,
  AssistantConversationItem,
  ConversationMessagesResponse,
} from '@/types/assistant';

export async function fetchAssistantConversations(): Promise<AssistantConversationItem[]> {
  const res = await http.get<ApiResponse<AssistantConversationItem[]>>('/assistant/conversations');
  return res.data.data;
}

export async function createAssistantConversation(title?: string): Promise<AssistantConversationItem> {
  const res = await http.post<ApiResponse<AssistantConversationItem>>('/assistant/conversations', { title });
  return res.data.data;
}

export async function fetchAssistantMessages(conversationId: number): Promise<ConversationMessagesResponse> {
  const res = await http.get<ApiResponse<ConversationMessagesResponse>>(`/assistant/conversations/${conversationId}/messages`);
  return res.data.data;
}

export async function sendAssistantMessage(conversationId: number, content: string): Promise<AssistantChatResponse> {
  const res = await http.post<ApiResponse<AssistantChatResponse>>(`/assistant/conversations/${conversationId}/messages`, { content });
  return res.data.data;
}
