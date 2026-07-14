import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { fetchBrands, fetchCategories, fetchProducts } from '@/api/product';
import type { Brand, Category, ProductListItem } from '@/types/product';

export const useProductStore = defineStore('product', () => {
  const products = ref<ProductListItem[]>([]);
  const brands = ref<Brand[]>([]);
  const categories = ref<Category[]>([]);
  const keyword = ref('');
  const categoryId = ref<number | null>(null);
  const brandId = ref<number | null>(null);
  const minPrice = ref<number | null>(null);
  const maxPrice = ref<number | null>(null);
  const tagName = ref('');
  const sortBy = ref('');
  const hasUsedPrice = ref(false);
  const hasPurchaseLink = ref(false);
  const loading = ref(false);

  const filteredProducts = computed(() => {
    return products.value;
  });

  const topProducts = computed(() => [...products.value].sort((a, b) => b.score - a.score).slice(0, 3));

  async function loadProducts() {
    loading.value = true;
    try {
      products.value = await fetchProducts({
        categoryId: categoryId.value ?? undefined,
        brandId: brandId.value ?? undefined,
        keyword: keyword.value || undefined,
        minPrice: minPrice.value ?? undefined,
        maxPrice: maxPrice.value ?? undefined,
        tagName: tagName.value || undefined,
        hasUsedPrice: hasUsedPrice.value || undefined,
        hasPurchaseLink: hasPurchaseLink.value || undefined,
        sortBy: sortBy.value || undefined,
      });
    } finally {
      loading.value = false;
    }
  }

  async function loadBrandsAndCategories() {
    const [b, c] = await Promise.all([fetchBrands(), fetchCategories()]);
    brands.value = b;
    categories.value = c;
  }

  function resetFilters() {
    keyword.value = '';
    categoryId.value = null;
    brandId.value = null;
    minPrice.value = null;
    maxPrice.value = null;
    tagName.value = '';
    sortBy.value = '';
    hasUsedPrice.value = false;
    hasPurchaseLink.value = false;
  }

  return {
    products,
    brands,
    categories,
    keyword,
    categoryId,
    brandId,
    minPrice,
    maxPrice,
    tagName,
    sortBy,
    hasUsedPrice,
    hasPurchaseLink,
    loading,
    filteredProducts,
    topProducts,
    loadProducts,
    loadBrandsAndCategories,
    resetFilters,
  };
});
