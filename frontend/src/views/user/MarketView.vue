<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { RouterLink } from 'vue-router';
import { Search, TrendingUp, BarChart3, Bell } from '@lucide/vue';
import { fetchMarketPrices, fetchMarketSummary } from '@/api/market';
import { fetchBrands, fetchCategories } from '@/api/product';
import type { MarketPriceItem, MarketSummary } from '@/types/market';
import type { Brand, Category } from '@/types/product';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';

const prices = ref<MarketPriceItem[]>([]);
const summary = ref<MarketSummary | null>(null);
const brands = ref<Brand[]>([]);
const categories = ref<Category[]>([]);
const loading = ref(true);

const keyword = ref('');
const categoryId = ref<number | null>(null);
const brandId = ref<number | null>(null);
const priceType = ref('all');
const sort = ref('latest');

const priceTypeOptions = [
  { value: 'all', label: '全部' },
  { value: 'official', label: '官方价' },
  { value: 'used', label: '二手参考' },
  { value: 'channel', label: '渠道参考' },
];

const sortOptions = [
  { value: 'latest', label: '最新更新' },
  { value: 'priceAsc', label: '价格从低到高' },
  { value: 'priceDesc', label: '价格从高到低' },
  { value: 'scoreDesc', label: '推荐度' },
];

onMounted(async () => {
  const [b, c] = await Promise.all([fetchBrands(), fetchCategories()]);
  brands.value = b;
  categories.value = c;
  await loadData();
});

async function loadData() {
  loading.value = true;
  try {
    const [p, s] = await Promise.all([
      fetchMarketPrices({
        categoryId: categoryId.value ?? undefined,
        brandId: brandId.value ?? undefined,
        keyword: keyword.value || undefined,
        priceType: priceType.value !== 'all' ? priceType.value : undefined,
        sort: sort.value,
      }),
      fetchMarketSummary(),
    ]);
    prices.value = p;
    summary.value = s;
  } catch {
    prices.value = [];
  } finally {
    loading.value = false;
  }
}

let searchTimer: ReturnType<typeof setTimeout> | null = null;
function onSearch() {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => loadData(), 300);
}

watch([categoryId, brandId, priceType, sort], () => loadData());

function formatPrice(v: number | null): string {
  if (v == null) return '-';
  return `¥${Math.round(v)}`;
}

function formatDate(d: string | null): string {
  if (!d) return '-';
  return d;
}
</script>

<template>
  <section class="market-page">
    <div class="page-head">
      <div>
        <p>Market Watch</p>
        <h1>价格行情</h1>
        <span class="subtitle">展示官方价、二手参考价和渠道参考价，帮你掌握数码产品价格动态</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div v-if="summary" class="summary-cards">
      <div class="summary-card">
        <TrendingUp :size="18" />
        <div><small>产品总数</small><strong>{{ summary.totalProducts }}</strong></div>
      </div>
      <div class="summary-card">
        <BarChart3 :size="18" />
        <div><small>有行情产品</small><strong>{{ summary.productsWithPrice }}</strong></div>
      </div>
      <div class="summary-card">
        <TrendingUp :size="18" />
        <div><small>二手参考</small><strong>{{ summary.usedPriceCount }}</strong></div>
      </div>
      <div class="summary-card">
        <BarChart3 :size="18" />
        <div><small>最近更新</small><strong>{{ summary.recentUpdates }}</strong></div>
      </div>
    </div>

    <!-- 筛选区 -->
    <div class="filter-bar">
      <div class="search-wrap">
        <Search :size="16" />
        <input v-model="keyword" placeholder="搜索产品、品牌" @input="onSearch" />
      </div>
      <select v-model="categoryId">
        <option :value="null">全部分类</option>
        <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <select v-model="brandId">
        <option :value="null">全部品牌</option>
        <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
      </select>
      <select v-model="priceType">
        <option v-for="opt in priceTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
      </select>
      <select v-model="sort">
        <option v-for="opt in sortOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
      </select>
    </div>

    <!-- 行情列表 -->
    <LoadingState v-if="loading" />

    <EmptyState v-else-if="prices.length === 0"
      :icon="BarChart3"
      :title="keyword || categoryId || brandId || priceType !== 'all' ? '暂无符合条件的行情数据' : '暂无价格行情'"
      :description="keyword || categoryId || brandId || priceType !== 'all' ? '试试调整筛选条件' : '管理端维护价格数据后会显示在这里'"
    />

    <div v-else class="price-list">
      <div v-for="item in prices" :key="`${item.productId}-${item.priceType}`" class="price-card panel">
        <div class="card-img">
          <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.productName" />
          <div v-else class="placeholder-img" />
        </div>
        <div class="card-info">
          <div class="card-topline">
            <span>{{ item.brandName || '未知品牌' }} · {{ item.categoryName || '数码产品' }}</span>
            <span class="score-badge">{{ item.score }}</span>
          </div>
          <h3>{{ item.productName }}</h3>
          <div class="price-tags">
            <span class="price-tag type">{{ item.priceType === 'official' ? '官方价' : item.priceType === 'used' ? '二手参考' : '渠道参考' }}</span>
            <span class="price-tag avg" v-if="item.avgPrice != null">均价 {{ formatPrice(item.avgPrice) }}</span>
            <span class="price-tag range" v-if="item.minPrice != null && item.maxPrice != null">
              {{ formatPrice(item.minPrice) }} - {{ formatPrice(item.maxPrice) }}
            </span>
            <span class="price-tag official" v-if="item.officialPrice != null">官方 ¥{{ item.officialPrice }}</span>
          </div>
          <div class="card-meta">
            <span v-if="item.sampleCount">样本 {{ item.sampleCount }}</span>
            <span v-if="item.referenceDate">更新 {{ formatDate(item.referenceDate) }}</span>
          </div>
        </div>
        <div class="card-actions">
          <RouterLink :to="`/products/${item.productId}`" class="action-btn">查看产品</RouterLink>
          <RouterLink :to="`/products/${item.productId}`" class="action-btn ghost">
            <Bell :size="14" /> 设置提醒
          </RouterLink>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.market-page {
  padding-top: 36px;
}

.page-head {
  margin-bottom: 22px;
}

.page-head p {
  margin: 0 0 6px;
  color: var(--green);
  font-weight: 850;
  font-size: 13px;
}

.page-head h1 {
  margin: 0;
  font-size: 36px;
}

.subtitle {
  display: block;
  margin-top: 8px;
  color: var(--muted);
  font-size: 14px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 18px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  padding: 16px 18px;
}

.summary-card svg {
  color: var(--green);
  flex-shrink: 0;
}

.summary-card small {
  display: block;
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 4px;
}

.summary-card strong {
  font-size: 22px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.search-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  padding: 0 12px;
  flex: 1;
  min-width: 200px;
}

.search-wrap svg {
  color: var(--muted);
  flex-shrink: 0;
}

.search-wrap input {
  height: 40px;
  border: 0;
  outline: 0;
  background: transparent;
  width: 100%;
  color: var(--ink);
}

.filter-bar select {
  height: 40px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  padding: 0 10px;
  color: var(--ink);
  font-size: 13px;
}

.empty {
  display: grid;
  place-items: center;
  gap: 10px;
  color: var(--muted);
  padding: 48px 24px;
  text-align: center;
}

.empty p {
  margin: 0;
  font-size: 15px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.price-list {
  display: grid;
  gap: 12px;
}

.price-card {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  gap: 16px;
  align-items: center;
  padding: 14px;
}

.card-img {
  width: 100px;
  height: 80px;
  border-radius: 8px;
  background: var(--soft);
  overflow: hidden;
  display: grid;
  place-items: center;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.placeholder-img {
  width: 100%;
  height: 100%;
  background: var(--soft);
}

.card-info {
  min-width: 0;
}

.card-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.card-topline > span:first-child {
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
}

.score-badge {
  color: var(--green);
  font-size: 16px;
  font-weight: 900;
}

h3 {
  margin: 3px 0 8px;
  font-size: 16px;
}

.price-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 6px;
}

.price-tag {
  padding: 3px 8px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 650;
}

.price-tag.type {
  background: #eef5f2;
  color: var(--green);
}

.price-tag.avg {
  background: #fefce8;
  color: #92400e;
}

.price-tag.range {
  background: #f3f4f6;
  color: var(--ink);
}

.price-tag.official {
  background: #eef2ff;
  color: #4338ca;
}

.card-meta {
  display: flex;
  gap: 12px;
  color: var(--muted);
  font-size: 12px;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-btn {
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--line);
  border-radius: 7px;
  background: #fff;
  color: var(--ink);
  font-size: 12px;
  font-weight: 650;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.action-btn.ghost {
  border-color: var(--green);
  color: var(--green);
}

@media (max-width: 700px) {
  .summary-cards { grid-template-columns: repeat(2, 1fr); }
  .price-card { grid-template-columns: 1fr; }
  .card-img { display: none; }
}
</style>
