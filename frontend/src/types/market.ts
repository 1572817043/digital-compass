export interface MarketPriceItem {
  productId: number;
  productName: string;
  brandName: string | null;
  categoryName: string | null;
  categoryCode: string | null;
  coverUrl: string | null;
  officialPrice: number | null;
  score: number;
  priceType: string;
  minPrice: number | null;
  maxPrice: number | null;
  avgPrice: number | null;
  sampleCount: number;
  referenceDate: string | null;
}

export interface MarketSummary {
  totalProducts: number;
  productsWithPrice: number;
  usedPriceCount: number;
  recentUpdates: number;
}

export interface MarketProductPriceRecord {
  productId: number;
  priceType: string;
  platform: string;
  minPrice: number | null;
  maxPrice: number | null;
  avgPrice: number | null;
  sampleCount: number;
  referenceDate: string | null;
  sourceType: string | null;
  remark: string | null;
}
