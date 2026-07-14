import { http } from '@/api/http';
import type { ApiResponse } from '@/types/auth';
import type {
  Brand, Category, ProductDetail, ProductListItem,
  ProductSpecItem, ProductPriceItem, ProductLinkItem, ProductTagItem,
  ProductSpecPayload, ProductPricePayload, ProductLinkPayload, ProductTagPayload,
  UploadImageResult,
} from '@/types/product';

export async function fetchBrands(): Promise<Brand[]> {
  const res = await http.get<ApiResponse<Brand[]>>('/brands');
  return res.data.data;
}

export async function fetchCategories(): Promise<Category[]> {
  const res = await http.get<ApiResponse<Category[]>>('/categories');
  return res.data.data;
}

export async function fetchProducts(params?: {
  categoryId?: number;
  brandId?: number;
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  tagType?: string;
  tagName?: string;
  hasUsedPrice?: boolean;
  hasPurchaseLink?: boolean;
  sortBy?: string;
}): Promise<ProductListItem[]> {
  const res = await http.get<ApiResponse<ProductListItem[]>>('/products', { params });
  return res.data.data;
}

export async function fetchProductDetail(id: number): Promise<ProductDetail> {
  const res = await http.get<ApiResponse<ProductDetail>>(`/products/${id}`);
  return res.data.data;
}

export async function createProduct(data: {
  categoryId: number;
  brandId: number;
  name: string;
  model?: string;
  summary?: string;
  coverUrl?: string;
  officialPrice?: number;
  score?: number;
  status?: number;
}): Promise<number> {
  const res = await http.post<ApiResponse<number>>('/admin/products', data);
  return res.data.data;
}

export async function updateProduct(id: number, data: {
  categoryId: number;
  brandId: number;
  name: string;
  model?: string;
  summary?: string;
  coverUrl?: string;
  officialPrice?: number;
  score?: number;
  status?: number;
}): Promise<void> {
  await http.put(`/admin/products/${id}`, data);
}

export async function deleteProduct(id: number): Promise<void> {
  await http.delete(`/admin/products/${id}`);
}

export async function fetchAdminProducts(params?: { status?: number }): Promise<ProductListItem[]> {
  const res = await http.get<ApiResponse<ProductListItem[]>>('/admin/products', { params });
  return res.data.data;
}

export async function fetchAdminProductDetail(id: number): Promise<ProductDetail> {
  const res = await http.get<ApiResponse<ProductDetail>>(`/admin/products/${id}`);
  return res.data.data;
}

export async function updateProductStatus(id: number, status: number): Promise<void> {
  await http.put(`/admin/products/${id}/status`, { status });
}

export async function bindProductImage(productId: number, data: {
  imageUrl: string;
  imageType?: string;
  sortOrder?: number;
}): Promise<void> {
  await http.post(`/admin/products/${productId}/images`, data);
}

export async function deleteProductImage(productId: number, imageId: number): Promise<void> {
  await http.delete(`/admin/products/${productId}/images/${imageId}`);
}

export async function uploadImage(file: File, productName?: string, imageUsage?: string): Promise<UploadImageResult> {
  const formData = new FormData();
  formData.append('file', file);
  if (productName) formData.append('productName', productName);
  if (imageUsage) formData.append('imageUsage', imageUsage);
  const res = await http.post<ApiResponse<UploadImageResult>>(
    '/admin/upload/image',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  );
  return res.data.data;
}

// ========== Specs ==========
export async function fetchAdminProductSpecs(productId: number): Promise<ProductSpecItem[]> {
  const res = await http.get<ApiResponse<ProductSpecItem[]>>(`/admin/products/${productId}/specs`);
  return res.data.data;
}

export async function createProductSpec(productId: number, data: ProductSpecPayload): Promise<number> {
  const res = await http.post<ApiResponse<number>>(`/admin/products/${productId}/specs`, data);
  return res.data.data;
}

export async function updateProductSpec(productId: number, specId: number, data: ProductSpecPayload): Promise<void> {
  await http.put(`/admin/products/${productId}/specs/${specId}`, data);
}

export async function deleteProductSpec(productId: number, specId: number): Promise<void> {
  await http.delete(`/admin/products/${productId}/specs/${specId}`);
}

// ========== Prices ==========
export async function fetchAdminProductPrices(productId: number): Promise<ProductPriceItem[]> {
  const res = await http.get<ApiResponse<ProductPriceItem[]>>(`/admin/products/${productId}/prices`);
  return res.data.data;
}

export async function createProductPrice(productId: number, data: ProductPricePayload): Promise<number> {
  const res = await http.post<ApiResponse<number>>(`/admin/products/${productId}/prices`, data);
  return res.data.data;
}

export async function updateProductPrice(productId: number, priceId: number, data: ProductPricePayload): Promise<void> {
  await http.put(`/admin/products/${productId}/prices/${priceId}`, data);
}

export async function deleteProductPrice(productId: number, priceId: number): Promise<void> {
  await http.delete(`/admin/products/${productId}/prices/${priceId}`);
}

// ========== Links ==========
export async function fetchAdminProductLinks(productId: number): Promise<ProductLinkItem[]> {
  const res = await http.get<ApiResponse<ProductLinkItem[]>>(`/admin/products/${productId}/links`);
  return res.data.data;
}

export async function createProductLink(productId: number, data: ProductLinkPayload): Promise<number> {
  const res = await http.post<ApiResponse<number>>(`/admin/products/${productId}/links`, data);
  return res.data.data;
}

export async function updateProductLink(productId: number, linkId: number, data: ProductLinkPayload): Promise<void> {
  await http.put(`/admin/products/${productId}/links/${linkId}`, data);
}

export async function deleteProductLink(productId: number, linkId: number): Promise<void> {
  await http.delete(`/admin/products/${productId}/links/${linkId}`);
}

// ========== Tags ==========
export async function fetchAdminProductTags(productId: number): Promise<ProductTagItem[]> {
  const res = await http.get<ApiResponse<ProductTagItem[]>>(`/admin/products/${productId}/tags`);
  return res.data.data;
}

export async function createProductTag(productId: number, data: ProductTagPayload): Promise<number> {
  const res = await http.post<ApiResponse<number>>(`/admin/products/${productId}/tags`, data);
  return res.data.data;
}

export async function updateProductTag(productId: number, tagId: number, data: ProductTagPayload): Promise<void> {
  await http.put(`/admin/products/${productId}/tags/${tagId}`, data);
}

export async function deleteProductTag(productId: number, tagId: number): Promise<void> {
  await http.delete(`/admin/products/${productId}/tags/${tagId}`);
}
