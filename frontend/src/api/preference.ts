import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type { UserPreferenceItem } from '@/types/preference';

export async function fetchMyPreference(): Promise<UserPreferenceItem> {
  const res = await http.get<ApiResponse<UserPreferenceItem>>('/preferences/me');
  return res.data.data;
}

export async function saveMyPreference(data: Partial<UserPreferenceItem>): Promise<void> {
  await http.put('/preferences/me', data);
}
