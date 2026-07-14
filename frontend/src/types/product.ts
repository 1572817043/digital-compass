export interface Brand {
  id: number;
  name: string;
  logoUrl: string | null;
  sortOrder: number;
}

export interface Category {
  id: number;
  code: string;
  name: string;
  description: string | null;
}

export interface ProductListItem {
  id: number;
  name: string;
  model: string | null;
  summary: string | null;
  officialPrice: number | null;
  score: number;
  status: number;
  categoryId: number;
  categoryName: string;
  categoryCode: string;
  brandId: number;
  brandName: string;
  coverUrl: string | null;
}

export interface Product {
  id: string;
  name: string;
  brand: string;
  category: string;
  summary: string;
  officialPrice: string;
  usedPrice: string;
  score: number;
  tags: string[];
  specs: ProductSpec[];
}

export interface ProductImage {
  id: number;
  productId: number;
  imageUrl: string;
  imageType: string;
  sortOrder: number;
}

export interface ImageAuditResult {
  width: number | null;
  height: number | null;
  aspectRatio: number | null;
  status: 'PASS' | 'REVIEW' | string;
  summary: string;
  warnings: string[];
  suggestions: string[];
  recommendedUsage: string;
}

export interface UploadImageResult {
  url: string;
  objectName: string;
  originalFilename: string;
  audit: ImageAuditResult;
}

export interface ProductSpec {
  id: number | null;
  group: string;
  name: string;
  value: string;
  sortOrder: number;
}

export interface ProductTagItem {
  id: number;
  tagType: string;
  tagName: string;
  tagValue: string | null;
  sortOrder: number;
}

export interface ProductMetricItem {
  id: number;
  metricKey: string;
  metricLabel: string;
  metricValue: string;
  numericValue: number | null;
  unit: string | null;
  sortOrder: number;
}

export interface ProductDetail {
  id: number;
  name: string;
  model: string | null;
  summary: string | null;
  officialPrice: number | null;
  score: number;
  status: number;
  categoryId: number;
  categoryName: string;
  categoryCode: string;
  brandId: number;
  brandName: string;
  images: ProductImage[];
  specs: ProductSpec[];
  metrics: ProductMetricItem[];
  tags: ProductTagItem[];
  prices: PriceInfo[];
  purchaseLinks: PurchaseLinkInfo[];
}

export interface PriceInfo {
  id: number;
  priceType: string;
  platform: string;
  minPrice: number | null;
  maxPrice: number | null;
  avgPrice: number | null;
  sampleCount: number;
  referenceDate: string;
  sourceType: string;
  remark: string | null;
}

export interface PurchaseLinkInfo {
  id: number;
  platform: string;
  linkType: string;
  title: string;
  url: string;
  sortOrder: number;
}

// Admin CRUD types
export interface ProductSpecItem {
  id: number;
  group: string;
  name: string;
  value: string;
  sortOrder: number;
}

export interface ProductPriceItem {
  id: number;
  priceType: string;
  platform: string;
  minPrice: number | null;
  maxPrice: number | null;
  avgPrice: number | null;
  sampleCount: number;
  referenceDate: string;
  sourceType: string;
  remark: string | null;
}

export interface ProductLinkItem {
  id: number;
  platform: string;
  linkType: string;
  title: string;
  url: string;
  sortOrder: number;
}

export interface ProductTagPayload {
  tagType: string;
  tagName: string;
  tagValue?: string;
  sortOrder?: number;
}

export interface ProductSpecPayload {
  specGroup: string;
  specName: string;
  specValue: string;
  sortOrder?: number;
}

export interface ProductPricePayload {
  priceType: string;
  platform: string;
  minPrice?: number | null;
  maxPrice?: number | null;
  avgPrice?: number | null;
  sampleCount?: number;
  referenceDate?: string;
  sourceType?: string;
  remark?: string;
}

export interface ProductLinkPayload {
  platform: string;
  linkType?: string;
  title: string;
  url: string;
  sortOrder?: number;
  enabled?: number;
}
