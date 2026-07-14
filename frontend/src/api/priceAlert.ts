import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type { PriceAlertItem } from '@/types/priceAlert';

export async function fetchPriceAlerts(): Promise<PriceAlertItem[]> {
  const res = await http.get<ApiResponse<PriceAlertItem[]>>('/price-alerts');
  return res.data.data;
}

export async function fetchProductAlert(productId: number): Promise<PriceAlertItem | null> {
  const res = await http.get<ApiResponse<PriceAlertItem | null>>(`/price-alerts/product/${productId}`);
  return res.data.data;
}

export async function savePriceAlert(data: { productId: number; targetPrice: number; priceType?: string }): Promise<void> {
  await http.post('/price-alerts', data);
}

export async function deletePriceAlert(id: number): Promise<void> {
  await http.delete(`/price-alerts/${id}`);
}

export async function refreshPriceAlerts(): Promise<PriceAlertItem[]> {
  const res = await http.post<ApiResponse<PriceAlertItem[]>>('/price-alerts/refresh');
  return res.data.data;
}
