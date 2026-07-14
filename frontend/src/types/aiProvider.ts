export interface AiProviderConfigItem {
  id: number;
  providerCode: string;
  providerName: string;
  protocolType: string;
  baseUrl: string;
  maskedApiKey: string;
  chatModel: string | null;
  embeddingModel: string | null;
  temperature: number | null;
  maxTokens: number | null;
  millionContext: boolean;
  timeoutSeconds: number | null;
  enabled: boolean;
  defaultProvider: boolean;
  remark: string | null;
}

export interface AiTestResult {
  success: boolean;
  model: string | null;
  responsePreview: string | null;
  errorMessage: string | null;
}

export interface AiModelItem {
  id: string;
  ownedBy: string | null;
}
