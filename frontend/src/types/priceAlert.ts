export interface PriceAlertItem {
  id: number;
  productId: number;
  productName: string;
  brandName: string | null;
  categoryName: string | null;
  categoryCode: string | null;
  coverUrl: string | null;
  targetPrice: number;
  priceType: string;
  status: string;
  lastPrice: number | null;
  triggered: boolean;
  triggeredAt: string | null;
}
