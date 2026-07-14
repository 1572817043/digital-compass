import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type { AiKnowledgeItem, AiKnowledgeChunkItem, AiKnowledgeSearchResult } from '@/types/aiKnowledge';

export async function fetchAiKnowledgeList(params?: {
  keyword?: string;
  knowledgeType?: string;
  status?: number;
  categoryId?: number;
  productId?: number;
}): Promise<AiKnowledgeItem[]> {
  const res = await http.get<ApiResponse<AiKnowledgeItem[]>>('/admin/ai/knowledge', { params });
  return res.data.data;
}

export async function fetchAiKnowledge(id: number): Promise<AiKnowledgeItem> {
  const res = await http.get<ApiResponse<AiKnowledgeItem>>(`/admin/ai/knowledge/${id}`);
  return res.data.data;
}

export async function createAiKnowledge(data: {
  categoryId?: number;
  productId?: number;
  title: string;
  content: string;
  knowledgeType?: string;
  tags?: string;
  source?: string;
  status?: number;
}): Promise<number> {
  const res = await http.post<ApiResponse<number>>('/admin/ai/knowledge', data);
  return res.data.data;
}

export async function updateAiKnowledge(id: number, data: {
  categoryId?: number;
  productId?: number;
  title?: string;
  content?: string;
  knowledgeType?: string;
  tags?: string;
  source?: string;
  status?: number;
}): Promise<void> {
  await http.put(`/admin/ai/knowledge/${id}`, data);
}

export async function deleteAiKnowledge(id: number): Promise<void> {
  await http.delete(`/admin/ai/knowledge/${id}`);
}

export async function rebuildAiKnowledgeChunks(id: number): Promise<void> {
  await http.post(`/admin/ai/knowledge/${id}/chunks/rebuild`);
}

export async function fetchAiKnowledgeChunks(id: number): Promise<AiKnowledgeChunkItem[]> {
  const res = await http.get<ApiResponse<AiKnowledgeChunkItem[]>>(`/admin/ai/knowledge/${id}/chunks`);
  return res.data.data;
}

export async function rebuildProductKnowledge(): Promise<number> {
  const res = await http.post<ApiResponse<number>>('/admin/ai/knowledge/products/rebuild');
  return res.data.data;
}

export async function rebuildAiKnowledgeEmbeddings(): Promise<number> {
  const res = await http.post<ApiResponse<number>>('/admin/ai/knowledge/embeddings/rebuild');
  return res.data.data;
}

export async function searchAiKnowledgeEmbeddings(params: {
  query: string;
  limit?: number;
}): Promise<AiKnowledgeSearchResult[]> {
  const res = await http.get<ApiResponse<AiKnowledgeSearchResult[]>>('/admin/ai/knowledge/embeddings/search', { params });
  return res.data.data;
}
