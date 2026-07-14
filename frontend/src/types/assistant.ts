export interface AssistantConversationItem {
  id: number;
  title: string;
  lastMessage: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessageItem {
  id: number;
  conversationId: number;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}

export interface AssistantRecommendationItem {
  id: number;
  messageId: number;
  productId: number | null;
  productName: string;
  brandName: string | null;
  categoryName: string | null;
  coverUrl: string | null;
  officialPrice: number | null;
  usedMinPrice: number | null;
  usedMaxPrice: number | null;
  productScore: number | null;
  matchScore: number | null;
  reason: string | null;
  riskTip: string | null;
  explainSummary: string | null;
  detailPath: string | null;
  matchTags: string[];
  cautionTags: string[];
  nextActions: string[];
  matchedRequirements: string[];
  tradeoffNotes: string[];
  knowledgeEvidence: string[];
}

export interface ConversationMessagesResponse {
  conversationId: number;
  messages: ChatMessageItem[];
  recommendations: AssistantRecommendationItem[];
}

export interface AssistantChatResponse {
  conversationId: number;
  userMessage: ChatMessageItem;
  assistantMessage: ChatMessageItem;
  recommendations: AssistantRecommendationItem[];
}
