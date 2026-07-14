import { http } from '@/api/http';
import type { ApiResponse, LoginResponse, LoginUser } from '@/types/auth';

export interface LoginPayload {
  username: string;
  password: string;
}

export async function login(payload: LoginPayload): Promise<LoginResponse> {
  const response = await http.post<ApiResponse<LoginResponse>>('/auth/login', payload, {
    skipAuthRedirect: true,
  });
  return response.data.data;
}

export async function getCurrentUser(): Promise<LoginUser> {
  const response = await http.get<ApiResponse<LoginUser>>('/auth/me');
  return response.data.data;
}
