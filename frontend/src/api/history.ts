import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type { ProductListItem } from '@/types/product';

export async function recordView(productId: number): Promise<void> {
  await http.post(`/history/products/${productId}`, undefined, { skipAuthRedirect: true } as any);
}

export async function fetchRecentProducts(): Promise<ProductListItem[]> {
  const res = await http.get<ApiResponse<ProductListItem[]>>('/history/products');
  return res.data.data;
}

export async function clearHistory(): Promise<void> {
  await http.delete('/history/products');
}
