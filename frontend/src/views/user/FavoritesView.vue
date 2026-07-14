<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { Heart } from '@lucide/vue';
import { fetchFavoriteProducts, removeFavorite } from '@/api/favorite';
import { useFavoriteStore } from '@/stores/favoriteStore';
import { useToastStore } from '@/stores/toastStore';
import type { ProductListItem } from '@/types/product';
import ProductCard from '@/components/product/ProductCard.vue';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const favoriteStore = useFavoriteStore();
const toastStore = useToastStore();
const products = ref<ProductListItem[]>([]);
const loading = ref(true);
const error = ref(false);

async function loadFavorites() {
  loading.value = true;
  error.value = false;
  try {
    products.value = await fetchFavoriteProducts();
    favoriteStore.favoriteIds = products.value.map(p => p.id);
  } catch {
    error.value = true;
    products.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(loadFavorites);

async function handleRemove(productId: number) {
  try {
    await removeFavorite(productId);
    products.value = products.value.filter(p => p.id !== productId);
    favoriteStore.favoriteIds = favoriteStore.favoriteIds.filter(id => id !== productId);
    toastStore.success('已取消收藏');
  } catch {
    toastStore.error('取消收藏失败');
  }
}
</script>

<template>
  <section class="favorites-page">
    <div class="page-head">
      <div>
        <p>My Favorites</p>
        <h1>我的收藏</h1>
      </div>
      <RouterLink to="/products" class="ghost-button">去产品库添加</RouterLink>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="error" message="加载收藏失败" @retry="loadFavorites" />

    <EmptyState v-else-if="products.length === 0"
      :icon="Heart"
      title="还没有收藏产品"
      description="去产品库浏览产品，点击心形图标即可收藏"
      button-label="去产品库"
      @action="$router.push('/products')"
    />

    <div v-else class="product-grid">
      <div v-for="product in products" :key="product.id" class="fav-item">
        <ProductCard :product="product" />
        <button class="remove-btn" @click="handleRemove(product.id)">取消收藏</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.favorites-page { padding-top: 36px; }
.page-head { display: flex; justify-content: space-between; align-items: end; margin-bottom: 24px; }
.page-head p { margin: 0 0 6px; color: var(--green); font-weight: 850; font-size: 13px; }
.page-head h1 { margin: 0; font-size: 36px; }
.ghost-button { height: 36px; padding: 0 14px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--ink); font-weight: 650; font-size: 13px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; text-decoration: none; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.fav-item { position: relative; }
.remove-btn { position: absolute; bottom: 8px; right: 8px; height: 28px; padding: 0 10px; border: 1px solid #e8d5d0; border-radius: 6px; background: #fff; color: #b2472f; font-size: 12px; font-weight: 650; cursor: pointer; z-index: 2; opacity: 0; transition: opacity .15s ease; }
.fav-item:hover .remove-btn { opacity: 1; }
@media (max-width: 1100px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 700px) { .product-grid { grid-template-columns: 1fr; } }
</style>
