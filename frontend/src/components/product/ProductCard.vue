<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Heart, Laptop, Smartphone } from '@lucide/vue';
import type { ProductListItem } from '@/types/product';
import { useCompareStore } from '@/stores/compareStore';
import { useFavoriteStore } from '@/stores/favoriteStore';
import { useAuthStore } from '@/stores/authStore';

const props = defineProps<{
  product: ProductListItem;
}>();

const router = useRouter();
const compareStore = useCompareStore();
const favoriteStore = useFavoriteStore();
const authStore = useAuthStore();
const imageFailed = ref(false);

watch(
  () => props.product.coverUrl,
  () => { imageFailed.value = false; },
);

function toggleCompare(e: Event) {
  e.preventDefault();
  e.stopPropagation();
  if (compareStore.isSelected(props.product.id)) {
    compareStore.removeProduct(props.product.id);
  } else if (compareStore.canAdd) {
    compareStore.addProduct(props.product.id);
  }
}

async function toggleFavorite(e: Event) {
  e.preventDefault();
  e.stopPropagation();
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: '/products' } });
    return;
  }
  try {
    await favoriteStore.toggleFavorite(props.product.id);
  } catch {
    // 静默处理
  }
}
</script>

<template>
  <RouterLink :to="`/products/${product.id}`" class="product-card">
    <div class="product-art">
      <img v-if="product.coverUrl && !imageFailed" :src="product.coverUrl" :alt="product.name" @error="imageFailed = true" />
      <Smartphone v-else-if="product.categoryCode === 'phone'" :size="50" />
      <Laptop v-else :size="58" />
      <button
        class="favorite-btn"
        :class="{ active: favoriteStore.isFavorite(product.id) }"
        @click="toggleFavorite"
      >
        <Heart :size="16" :fill="favoriteStore.isFavorite(product.id) ? 'currentColor' : 'none'" />
      </button>
      <button
        class="compare-btn"
        :class="{ active: compareStore.isSelected(product.id), disabled: !compareStore.canAdd && !compareStore.isSelected(product.id) }"
        :disabled="!compareStore.canAdd && !compareStore.isSelected(product.id)"
        @click="toggleCompare"
      >
        {{ compareStore.isSelected(product.id) ? '已加入' : '+ 对比' }}
      </button>
    </div>
    <div class="product-main">
      <div class="product-title">
        <h3>{{ product.name }}</h3>
        <span>{{ product.score }}</span>
      </div>
      <p class="brand-line">{{ product.brandName || '未知品牌' }} · {{ product.categoryName || '数码产品' }}</p>
      <p>{{ product.summary }}</p>
      <div class="price-grid">
        <div><small>官方价</small><strong>{{ product.officialPrice != null ? `¥${product.officialPrice}` : '待定' }}</strong></div>
      </div>
    </div>
  </RouterLink>
</template>

<style scoped>
.product-card {
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 300px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--panel);
  overflow: hidden;
  transition: border-color .2s ease, transform .2s ease;
}

.product-card:hover {
  border-color: #d3d0c8;
  transform: translateY(-2px);
}

.product-art {
  position: relative;
  height: 160px;
  display: grid;
  place-items: center;
  background: var(--soft);
  color: var(--ink);
  overflow: hidden;
  padding: 18px;
}

.favorite-btn {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: #fff;
  color: var(--muted);
  cursor: pointer;
  z-index: 1;
  transition: all .15s ease;
}

.favorite-btn:hover {
  color: #e25555;
  border-color: #e25555;
}

.favorite-btn.active {
  color: #e25555;
  border-color: #e25555;
  background: #fef2f2;
}

.compare-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  height: 28px;
  padding: 0 10px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: #fff;
  color: var(--muted);
  font-size: 12px;
  font-weight: 650;
  cursor: pointer;
  z-index: 1;
  transition: all .15s ease;
}

.compare-btn:hover {
  border-color: var(--green);
  color: var(--green);
}

.compare-btn.active {
  background: var(--green);
  border-color: var(--green);
  color: #fff;
}

.compare-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.compare-btn:disabled:hover {
  border-color: var(--line);
  color: var(--muted);
}

.product-art img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
}

.product-main {
  padding: 16px;
}

.product-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

h3 {
  margin: 0;
  font-size: 18px;
}

.product-title span {
  color: var(--green);
  font-size: 20px;
  font-weight: 900;
}

.brand-line {
  margin: 6px 0 0 !important;
  color: var(--muted) !important;
  font-size: 12px !important;
}

p {
  min-height: 40px;
  margin: 10px 0 14px;
  color: var(--text);
  font-size: 13px;
  line-height: 1.65;
}

.price-grid {
  display: grid;
  gap: 10px;
}

.price-grid div {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 10px;
  background: #fdfdfc;
}

small {
  display: block;
  margin-bottom: 4px;
  color: var(--muted);
  font-size: 12px;
}

strong {
  font-size: 14px;
}
</style>
