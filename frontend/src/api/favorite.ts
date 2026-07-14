import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type { ProductListItem } from '@/types/product';

export async function fetchFavoriteProducts(): Promise<ProductListItem[]> {
  const res = await http.get<ApiResponse<ProductListItem[]>>('/favorites');
  return res.data.data;
}

export async function fetchFavoriteIds(): Promise<number[]> {
  const res = await http.get<ApiResponse<number[]>>('/favorites/ids');
  return res.data.data;
}

export async function addFavorite(productId: number): Promise<void> {
  await http.post(`/favorites/${productId}`);
}

export async function removeFavorite(productId: number): Promise<void> {
  await http.delete(`/favorites/${productId}`);
}
