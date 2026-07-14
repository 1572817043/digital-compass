import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type { MarketPriceItem, MarketProductPriceRecord, MarketSummary } from '@/types/market';

export async function fetchMarketPrices(params?: {
  categoryId?: number;
  brandId?: number;
  keyword?: string;
  priceType?: string;
  sort?: string;
}): Promise<MarketPriceItem[]> {
  const res = await http.get<ApiResponse<MarketPriceItem[]>>('/market/prices', { params });
  return res.data.data;
}

export async function fetchMarketSummary(): Promise<MarketSummary> {
  const res = await http.get<ApiResponse<MarketSummary>>('/market/summary');
  return res.data.data;
}

export async function fetchProductMarketPrices(productId: number): Promise<MarketProductPriceRecord[]> {
  const res = await http.get<ApiResponse<MarketProductPriceRecord[]>>(`/market/products/${productId}/prices`);
  return res.data.data;
}
