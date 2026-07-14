import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';

// Dashboard
export interface DashboardOverview {
  productCount: number;
  userCount: number;
  favoriteCount: number;
  historyCount: number;
  priceAlertCount: number;
  assistantConversationCount: number;
  aiWorkflowLogCount: number;
  recentProducts: Array<{ id: number; name: string; brandName: string; categoryName: string; officialPrice: number | null; createdAt: string }>;
  recentWorkflowLogs: Array<{ id: number; username: string; userRequirement: string; fallbackUsed: boolean; createdAt: string }>;
}

export async function fetchDashboardOverview(): Promise<DashboardOverview> {
  const res = await http.get<ApiResponse<DashboardOverview>>('/admin/dashboard/overview');
  return res.data.data;
}

// Users
export interface UserListItem {
  id: number;
  username: string;
  nickname: string;
  role: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export async function fetchUsers(params?: { keyword?: string; role?: string; status?: number }): Promise<UserListItem[]> {
  const res = await http.get<ApiResponse<UserListItem[]>>('/admin/users', { params });
  return res.data.data;
}

export async function updateUserRole(id: number, role: string): Promise<void> {
  await http.put(`/admin/users/${id}/role`, null, { params: { role } });
}

export async function updateUserStatus(id: number, status: number): Promise<void> {
  await http.put(`/admin/users/${id}/status`, null, { params: { status } });
}

export async function resetUserPassword(id: number): Promise<void> {
  await http.put(`/admin/users/${id}/password/reset`);
}

// Taxonomy
export interface CategoryItem { id: number; code: string; name: string; description: string | null; sortOrder: number; enabled: number; }
export interface BrandItem { id: number; name: string; sortOrder: number; }

export async function fetchAdminCategories(): Promise<CategoryItem[]> {
  const res = await http.get<ApiResponse<CategoryItem[]>>('/admin/categories');
  return res.data.data;
}

export async function createCategory(data: { code: string; name: string; description?: string; sortOrder?: number; enabled?: number }): Promise<number> {
  const res = await http.post<ApiResponse<number>>('/admin/categories', data);
  return res.data.data;
}

export async function updateCategory(id: number, data: { code: string; name: string; description?: string; sortOrder?: number; enabled?: number }): Promise<void> {
  await http.put(`/admin/categories/${id}`, data);
}

export async function updateCategoryStatus(id: number, enabled: number): Promise<void> {
  await http.put(`/admin/categories/${id}/status`, null, { params: { enabled } });
}

export async function fetchAdminBrands(): Promise<BrandItem[]> {
  const res = await http.get<ApiResponse<BrandItem[]>>('/admin/brands');
  return res.data.data;
}

export async function createBrand(data: { name: string; sortOrder?: number }): Promise<number> {
  const res = await http.post<ApiResponse<number>>('/admin/brands', data);
  return res.data.data;
}

export async function updateBrand(id: number, data: { name: string; sortOrder?: number }): Promise<void> {
  await http.put(`/admin/brands/${id}`, data);
}
