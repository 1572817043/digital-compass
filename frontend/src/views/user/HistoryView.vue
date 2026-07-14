<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { Clock } from '@lucide/vue';
import { fetchRecentProducts, clearHistory } from '@/api/history';
import { useToastStore } from '@/stores/toastStore';
import type { ProductListItem } from '@/types/product';
import ProductCard from '@/components/product/ProductCard.vue';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const toastStore = useToastStore();
const products = ref<ProductListItem[]>([]);
const loading = ref(true);
const error = ref(false);

async function loadHistory() {
  loading.value = true;
  error.value = false;
  try {
    products.value = await fetchRecentProducts();
  } catch {
    error.value = true;
    products.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(loadHistory);

async function handleClear() {
  if (!confirm('确定清空所有浏览记录吗？')) return;
  try {
    await clearHistory();
    products.value = [];
    toastStore.success('浏览记录已清空');
  } catch {
    toastStore.error('清空失败，请重试');
  }
}
</script>

<template>
  <section class="history-page">
    <div class="page-head">
      <div>
        <p>Recently Viewed</p>
        <h1>最近看过</h1>
      </div>
      <div class="head-actions">
        <button v-if="products.length > 0" class="ghost-button danger" @click="handleClear">清空记录</button>
        <RouterLink to="/products" class="ghost-button">去产品库</RouterLink>
      </div>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="error" message="加载浏览记录失败" @retry="loadHistory" />

    <EmptyState v-else-if="products.length === 0"
      :icon="Clock"
      title="还没有浏览记录"
      description="查看产品详情后会自动记录"
      button-label="去产品库"
      @action="$router.push('/products')"
    />

    <div v-else class="product-grid">
      <ProductCard v-for="product in products" :key="product.id" :product="product" />
    </div>
  </section>
</template>

<style scoped>
.history-page { padding-top: 36px; }
.page-head { display: flex; justify-content: space-between; align-items: end; margin-bottom: 24px; }
.page-head p { margin: 0 0 6px; color: var(--green); font-weight: 850; font-size: 13px; }
.page-head h1 { margin: 0; font-size: 36px; }
.head-actions { display: flex; gap: 10px; }
.ghost-button { height: 36px; padding: 0 14px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--ink); font-weight: 650; font-size: 13px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; text-decoration: none; }
.ghost-button.danger { color: #b2472f; border-color: #e8d5d0; }
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
@media (max-width: 1100px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 700px) { .product-grid { grid-template-columns: 1fr; } }
</style>
