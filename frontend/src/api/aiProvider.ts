import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type { AiModelItem, AiProviderConfigItem, AiTestResult } from '@/types/aiProvider';

export async function fetchAiProviders(): Promise<AiProviderConfigItem[]> {
  const res = await http.get<ApiResponse<AiProviderConfigItem[]>>('/admin/ai/providers');
  return res.data.data;
}

export async function fetchAiProvider(id: number): Promise<AiProviderConfigItem> {
  const res = await http.get<ApiResponse<AiProviderConfigItem>>(`/admin/ai/providers/${id}`);
  return res.data.data;
}

export async function createAiProvider(data: {
  providerCode: string;
  providerName: string;
  protocolType?: string;
  baseUrl: string;
  apiKey?: string;
  chatModel?: string;
  embeddingModel?: string;
  temperature?: number;
  maxTokens?: number;
  millionContext?: boolean;
  timeoutSeconds?: number;
  enabled?: boolean;
  defaultProvider?: boolean;
  remark?: string;
}): Promise<number> {
  const res = await http.post<ApiResponse<number>>('/admin/ai/providers', data);
  return res.data.data;
}

export async function updateAiProvider(id: number, data: {
  providerCode?: string;
  providerName?: string;
  protocolType?: string;
  baseUrl?: string;
  apiKey?: string;
  chatModel?: string;
  embeddingModel?: string;
  temperature?: number;
  maxTokens?: number;
  millionContext?: boolean;
  timeoutSeconds?: number;
  enabled?: boolean;
  defaultProvider?: boolean;
  remark?: string;
}): Promise<void> {
  await http.put(`/admin/ai/providers/${id}`, data);
}

export async function deleteAiProvider(id: number): Promise<void> {
  await http.delete(`/admin/ai/providers/${id}`);
}

export async function setDefaultAiProvider(id: number): Promise<void> {
  await http.put(`/admin/ai/providers/${id}/default`);
}

export async function testAiProvider(id: number): Promise<AiTestResult> {
  const res = await http.post<ApiResponse<AiTestResult>>(`/admin/ai/providers/${id}/test`);
  return res.data.data;
}

export async function fetchSavedAiProviderModels(id: number): Promise<AiModelItem[]> {
  const res = await http.get<ApiResponse<AiModelItem[]>>(`/admin/ai/providers/${id}/models`);
  return res.data.data;
}

export async function fetchAiProviderModels(data: {
  providerCode: string;
  providerName: string;
  protocolType?: string;
  baseUrl: string;
  apiKey?: string;
  chatModel?: string;
  embeddingModel?: string;
  temperature?: number;
  maxTokens?: number;
  millionContext?: boolean;
  timeoutSeconds?: number;
  enabled?: boolean;
  defaultProvider?: boolean;
  remark?: string;
}): Promise<AiModelItem[]> {
  const res = await http.post<ApiResponse<AiModelItem[]>>('/admin/ai/providers/models', data);
  return res.data.data;
}
