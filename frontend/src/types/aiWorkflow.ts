export interface AiWorkflowLogItem {
  id: number;
  userId: number | null;
  username: string | null;
  conversationId: number | null;
  providerId: number | null;
  providerName: string | null;
  modelName: string | null;
  userRequirement: string | null;
  parsedRequirementJson: string | null;
  retrievedContextSummary: string | null;
  candidateProductIds: string | null;
  fallbackUsed: boolean;
  errorMessage: string | null;
  createdAt: string;
}

export interface AiWorkflowCandidateProduct {
  id: number;
  name: string;
  brandName: string | null;
  categoryName: string | null;
  coverUrl: string | null;
  officialPrice: number | null;
  score: number | null;
  detailPath: string;
}

export interface AiWorkflowStepItem {
  code: string;
  name: string;
  status: 'success' | 'fallback' | 'empty' | string;
  description: string;
  detail: string | null;
}

export interface AiWorkflowLogDetail extends Omit<AiWorkflowLogItem, 'candidateProductIds'> {
  parsedRequirement: Record<string, unknown>;
  retrievedContexts: string[];
  candidateProductIdsText: string | null;
  candidateProductIds: number[];
  candidateProducts: AiWorkflowCandidateProduct[];
  workflowSteps: AiWorkflowStepItem[];
}
