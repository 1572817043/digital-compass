import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type { AiWorkflowLogDetail, AiWorkflowLogItem } from '@/types/aiWorkflow';

export async function fetchAiWorkflowLogs(params?: {
  keyword?: string;
  fallbackUsed?: boolean;
  limit?: number;
}): Promise<AiWorkflowLogItem[]> {
  const res = await http.get<ApiResponse<AiWorkflowLogItem[]>>('/admin/ai/workflow-logs', { params });
  return res.data.data;
}

export async function fetchAiWorkflowLogDetail(id: number): Promise<AiWorkflowLogDetail> {
  const res = await http.get<ApiResponse<AiWorkflowLogDetail>>(`/admin/ai/workflow-logs/${id}`);
  return res.data.data;
}
