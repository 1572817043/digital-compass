<script setup lang="ts">
import { onMounted, watch } from 'vue';
import ProductCard from '@/components/product/ProductCard.vue';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import { useProductStore } from '@/stores/productStore';

const productStore = useProductStore();

const tagOptions = ['影像强', '性能强', '游戏强', '拍照旅行', '学习办公', '通勤办公', '学生党', '创作者', '轻薄便携'];
const sortOptions = [
  { value: '', label: '综合推荐' },
  { value: 'price_asc', label: '价格从低到高' },
  { value: 'price_desc', label: '价格从高到低' },
  { value: 'newest', label: '发售时间新优先' },
];

onMounted(async () => {
  await productStore.loadBrandsAndCategories();
  await productStore.loadProducts();
});

watch(
  () => [
    productStore.keyword,
    productStore.categoryId,
    productStore.brandId,
    productStore.minPrice,
    productStore.maxPrice,
    productStore.tagName,
    productStore.sortBy,
    productStore.hasUsedPrice,
    productStore.hasPurchaseLink,
  ],
  () => { productStore.loadProducts(); },
);

function resetFilters() {
  productStore.resetFilters();
  productStore.loadProducts();
}
</script>

<template>
  <section class="page-head">
    <div>
      <p>Product Library</p>
      <h1>产品库</h1>
    </div>
    <div class="filters primary-filters">
      <input v-model="productStore.keyword" placeholder="搜索产品、品牌" />
      <select v-model="productStore.categoryId">
        <option :value="null">全部分类</option>
        <option v-for="cat in productStore.categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>
      <select v-model="productStore.brandId">
        <option :value="null">全部品牌</option>
        <option v-for="b in productStore.brands" :key="b.id" :value="b.id">{{ b.name }}</option>
      </select>
    </div>
  </section>

  <section class="filter-panel">
    <div class="filters">
      <input v-model.number="productStore.minPrice" type="number" min="0" placeholder="最低价" />
      <input v-model.number="productStore.maxPrice" type="number" min="0" placeholder="最高价" />
      <select v-model="productStore.tagName">
        <option value="">全部用途</option>
        <option v-for="tag in tagOptions" :key="tag" :value="tag">{{ tag }}</option>
      </select>
      <select v-model="productStore.sortBy">
        <option v-for="item in sortOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
      </select>
    </div>
    <div class="checks">
      <label><input v-model="productStore.hasUsedPrice" type="checkbox" /> 有二手参考</label>
      <label><input v-model="productStore.hasPurchaseLink" type="checkbox" /> 有购买链接</label>
      <button class="reset-button" @click="resetFilters">重置</button>
    </div>
  </section>

  <section v-if="productStore.loading" class="loading-wrap">
    <LoadingState />
  </section>

  <section v-else-if="productStore.filteredProducts.length === 0" class="empty-wrap">
    <EmptyState
      title="暂无符合条件的产品"
      :description="productStore.keyword ? '试试调整搜索关键词或筛选条件' : '产品库暂无数据'"
      button-label="重置筛选"
      @action="resetFilters"
    />
  </section>

  <section v-else class="product-grid">
    <ProductCard v-for="product in productStore.filteredProducts" :key="product.id" :product="product" />
  </section>
</template>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: end;
  padding: 44px 0 22px;
}

p {
  margin: 0 0 8px;
  color: var(--green);
  font-weight: 850;
}

h1 {
  margin: 0;
  font-size: 42px;
}

.filters {
  display: flex;
  gap: 10px;
}

.filter-panel {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 20px;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: #fdfdfc;
}

input,
select {
  height: 42px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  padding: 0 12px;
  color: var(--ink);
}

.filter-panel input {
  width: 110px;
}

.checks {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--muted);
  font-size: 13px;
  font-weight: 650;
}

.checks label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.checks input {
  width: 14px;
  height: 14px;
}

.reset-button {
  height: 34px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--ink);
  padding: 0 12px;
  cursor: pointer;
  font-weight: 650;
}

.loading-wrap,
.empty-wrap {
  padding: 40px 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.empty {
  text-align: center;
  padding: 60px 0;
  color: var(--muted);
  font-size: 16px;
}

@media (max-width: 1100px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 700px) {
  .page-head,
  .filters,
  .filter-panel,
  .checks {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-panel input {
    width: auto;
  }

  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
