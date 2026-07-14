<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchProductDetail } from '@/api/product';
import { recordView } from '@/api/history';
import { fetchProductAlert, savePriceAlert } from '@/api/priceAlert';
import { fetchProductMarketPrices } from '@/api/market';
import type { ProductDetail } from '@/types/product';
import type { PriceAlertItem } from '@/types/priceAlert';
import type { MarketProductPriceRecord } from '@/types/market';
import { useCompareStore } from '@/stores/compareStore';
import { useFavoriteStore } from '@/stores/favoriteStore';
import { useAuthStore } from '@/stores/authStore';
import { Smartphone, Laptop, GitCompare, Heart, Bell } from '@lucide/vue';
import LoadingState from '@/components/common/LoadingState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const route = useRoute();
const router = useRouter();
const compareStore = useCompareStore();
const favoriteStore = useFavoriteStore();
const authStore = useAuthStore();
const loadError = ref(false);
const product = ref<ProductDetail | null>(null);
const loading = ref(true);
const mainImageFailed = ref(false);
const selectedImageUrl = ref<string | null>(null);
const galleryImageFailed = ref<Record<number, boolean>>({});

// 价格提醒
const priceAlert = ref<PriceAlertItem | null>(null);
const showAlertModal = ref(false);
const alertTargetPrice = ref<number | null>(null);
const alertSaving = ref(false);

// 价格走势
const priceRecords = ref<MarketProductPriceRecord[]>([]);
const priceTypeFilter = ref('all');

const mainImage = computed(() => {
  if (!product.value) return null;
  const main = product.value.images.find(i => i.imageType === 'MAIN');
  return main?.imageUrl ?? product.value.images[0]?.imageUrl ?? null;
});

const activeImageUrl = computed(() => selectedImageUrl.value ?? mainImage.value);

const galleryImages = computed(() => {
  if (!product.value) return [];
  return [...product.value.images].sort((a, b) => a.sortOrder - b.sortOrder);
});

const specGroups = computed(() => {
  if (!product.value) return [];
  const map = new Map<string, { name: string; value: string }[]>();
  for (const spec of product.value.specs) {
    if (!map.has(spec.group)) map.set(spec.group, []);
    map.get(spec.group)!.push({ name: spec.name, value: spec.value });
  }
  return Array.from(map.entries());
});

const tagSections = computed(() => {
  if (!product.value) return [];
  const labels: Record<string, string> = {
    selling_point: '核心优点',
    weakness: '主要短板',
    suitable: '适合人群',
    unsuitable: '不适合人群',
    scene: '使用场景',
  };

  return Object.entries(labels)
    .map(([type, label]) => ({
      type,
      label,
      items: (product.value!.tags ?? []).filter(tag => tag.tagType === type),
    }))
    .filter(section => section.items.length > 0);
});

const metricItems = computed(() => product.value?.metrics ?? []);

const usedPriceRange = computed(() => {
  if (!product.value) return null;
  const used = product.value.prices.find(p => p.priceType === 'used');
  if (!used) return null;
  if (used.minPrice != null && used.maxPrice != null) {
    return `${Math.round(used.minPrice)}-${Math.round(used.maxPrice)}`;
  }
  return null;
});

const compareButtonText = computed(() => {
  if (!product.value) return '加入对比';
  return compareStore.isSelected(product.value.id) ? '已加入对比' : '加入对比';
});

const compareButtonDisabled = computed(() => {
  if (!product.value) return true;
  return !compareStore.canAdd && !compareStore.isSelected(product.value.id);
});

function toggleCompare() {
  if (!product.value) return;
  if (compareStore.isSelected(product.value.id)) {
    compareStore.removeProduct(product.value.id);
  } else if (compareStore.canAdd) {
    compareStore.addProduct(product.value.id);
  }
}

function selectImage(imageUrl: string) {
  selectedImageUrl.value = imageUrl;
  mainImageFailed.value = false;
}

function markGalleryImageFailed(id: number) {
  galleryImageFailed.value = {
    ...galleryImageFailed.value,
    [id]: true,
  };
}

async function toggleFavorite() {
  if (!product.value) return;
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return;
  }
  try {
    await favoriteStore.toggleFavorite(product.value.id);
  } catch {
    // 静默处理
  }
}

// 价格提醒
function openAlertModal() {
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } });
    return;
  }
  alertTargetPrice.value = priceAlert.value?.targetPrice ?? null;
  showAlertModal.value = true;
}

async function loadProductAlert() {
  if (!authStore.isLoggedIn || !product.value) return;
  try {
    priceAlert.value = await fetchProductAlert(product.value.id);
  } catch {
    priceAlert.value = null;
  }
}

async function handleSaveAlert() {
  if (!product.value || !alertTargetPrice.value || alertTargetPrice.value <= 0) return;
  alertSaving.value = true;
  try {
    await savePriceAlert({ productId: product.value.id, targetPrice: alertTargetPrice.value, priceType: 'official' });
    await loadProductAlert();
    showAlertModal.value = false;
  } catch {
    // 静默处理
  } finally {
    alertSaving.value = false;
  }
}

const alertButtonText = computed(() => {
  if (!priceAlert.value) return '价格提醒';
  if (priceAlert.value.triggered) return `已达到目标价 ¥${priceAlert.value.targetPrice}`;
  return `目标价 ¥${priceAlert.value.targetPrice}`;
});

const alertButtonClass = computed(() => {
  if (!priceAlert.value) return '';
  return priceAlert.value.triggered ? 'triggered' : 'set';
});

// 价格走势
function getRefPrice(r: MarketProductPriceRecord): number | null {
  return r.avgPrice ?? r.minPrice ?? r.maxPrice;
}

const filteredPriceRecords = computed(() => {
  if (priceTypeFilter.value === 'all') return priceRecords.value;
  return priceRecords.value.filter(r => r.priceType === priceTypeFilter.value);
});

const priceOverview = computed(() => {
  const records = filteredPriceRecords.value;
  if (records.length === 0) return null;
  const prices = records.map(getRefPrice).filter((p): p is number => p != null);
  if (prices.length === 0) return null;
  const latest = records[0];
  return {
    latest: getRefPrice(latest),
    min: Math.min(...prices),
    max: Math.max(...prices),
    lastDate: latest.referenceDate,
  };
});

const priceTypeLabels: Record<string, string> = {
  official: '官方价',
  used: '二手参考',
  channel: '渠道参考',
};

onMounted(async () => {
  try {
    product.value = await fetchProductDetail(Number(route.params.id));
    loadError.value = false;
    // 记录浏览历史（已登录时）
    if (authStore.isLoggedIn && product.value) {
      recordView(product.value.id).catch(() => {});
    }
    // 加载价格提醒
    await loadProductAlert();
    // 加载价格走势
    fetchProductMarketPrices(Number(route.params.id)).then(r => { priceRecords.value = r; }).catch(() => {});
  } catch {
    product.value = null;
    loadError.value = true;
  } finally {
    loading.value = false;
  }
});

watch(
  () => product.value?.id,
  () => {
    selectedImageUrl.value = null;
    mainImageFailed.value = false;
    galleryImageFailed.value = {};
  },
);

watch(activeImageUrl, () => {
  mainImageFailed.value = false;
});
</script>

<template>
  <LoadingState v-if="loading" />

  <ErrorState
    v-else-if="loadError || !product"
    message="产品不存在或已下架"
    button-label="返回产品库"
    @action="$router.push('/products')"
  />

  <section v-else class="detail">
    <div class="summary panel">
      <div class="img-area">
        <img v-if="activeImageUrl && !mainImageFailed" :src="activeImageUrl" :alt="product.name" @error="mainImageFailed = true" />
        <div v-else class="placeholder">
          <Smartphone v-if="product.categoryCode === 'phone'" :size="64" />
          <Laptop v-else :size="72" />
        </div>
      </div>
      <div class="info">
        <p>{{ product.brandName }}</p>
        <h1>{{ product.name }}</h1>
        <span class="cat-badge">{{ product.categoryName }}</span>
        <span v-if="product.summary" class="desc">{{ product.summary }}</span>
        <div class="detail-actions">
          <button
            class="favorite-action"
            :class="{ active: favoriteStore.isFavorite(product.id) }"
            @click="toggleFavorite"
          >
            <Heart :size="16" :fill="favoriteStore.isFavorite(product.id) ? 'currentColor' : 'none'" />
            {{ favoriteStore.isFavorite(product.id) ? '已收藏' : '收藏' }}
          </button>
          <button
            class="alert-action"
            :class="alertButtonClass"
            @click="openAlertModal"
          >
            <Bell :size="16" />
            {{ alertButtonText }}
          </button>
          <button
            class="compare-action"
            :class="{ active: compareStore.isSelected(product.id) }"
            :disabled="compareButtonDisabled"
            @click="toggleCompare"
          >
            <GitCompare :size="16" />
            {{ compareButtonText }}
          </button>
          <RouterLink v-if="compareStore.count > 0" to="/compare" class="compare-link">
            查看对比 {{ compareStore.count }}/3
          </RouterLink>
        </div>
        <div class="price-row">
          <div><small>官方价</small><strong>{{ product.officialPrice != null ? `¥${product.officialPrice}` : '待定' }}</strong></div>
          <div v-if="usedPriceRange"><small>二手参考</small><strong>{{ usedPriceRange }}</strong></div>
          <div><small>推荐度</small><strong>{{ product.score }}</strong></div>
        </div>
      </div>
    </div>

    <div class="side">
      <div v-if="galleryImages.length" class="panel gallery">
        <h2>图集</h2>
        <div class="gallery-grid">
          <button
            v-for="img in galleryImages"
            :key="img.id"
            class="gallery-button"
            :class="{ active: img.imageUrl === activeImageUrl }"
            type="button"
            @click="selectImage(img.imageUrl)"
          >
            <img
              v-if="!galleryImageFailed[img.id]"
              :src="img.imageUrl"
              :alt="product.name"
              @error="markGalleryImageFailed(img.id)"
            />
            <span v-else class="gallery-placeholder">
              <Smartphone v-if="product.categoryCode === 'phone'" :size="28" />
              <Laptop v-else :size="32" />
            </span>
          </button>
        </div>
      </div>

      <div v-if="metricItems.length" class="panel metrics">
        <h2>关键指标</h2>
        <div class="metric-grid">
          <div v-for="metric in metricItems" :key="metric.id" class="metric-item">
            <small>{{ metric.metricLabel }}</small>
            <strong>{{ metric.metricValue }}</strong>
          </div>
        </div>
      </div>

      <div v-if="specGroups.length" class="panel specs">
        <h2>核心参数</h2>
        <div v-for="([group, items]) in specGroups" :key="group">
          <h3>{{ group }}</h3>
          <div class="spec" v-for="item in items" :key="item.name">
            <strong>{{ item.name }}</strong>
            <em>{{ item.value }}</em>
          </div>
        </div>
      </div>

      <div v-if="tagSections.length" class="panel tags">
        <h2>选购判断</h2>
        <div v-for="section in tagSections" :key="section.type" class="tag-section">
          <h3>{{ section.label }}</h3>
          <div class="tag-list">
            <span v-for="tag in section.items" :key="tag.id" class="decision-tag">
              <strong>{{ tag.tagName }}</strong>
              <em v-if="tag.tagValue">{{ tag.tagValue }}</em>
            </span>
          </div>
        </div>
      </div>

      <!-- 价格走势 -->
      <div class="panel price-trend">
        <div class="trend-head">
          <div>
            <p class="section-en">Price Trend</p>
            <h2>价格走势</h2>
          </div>
          <div v-if="priceRecords.length" class="trend-filters">
            <button :class="{ active: priceTypeFilter === 'all' }" @click="priceTypeFilter = 'all'">全部</button>
            <button :class="{ active: priceTypeFilter === 'official' }" @click="priceTypeFilter = 'official'">官方价</button>
            <button :class="{ active: priceTypeFilter === 'used' }" @click="priceTypeFilter = 'used'">二手参考</button>
            <button :class="{ active: priceTypeFilter === 'channel' }" @click="priceTypeFilter = 'channel'">渠道参考</button>
          </div>
        </div>

        <!-- 概览 -->
        <div v-if="priceOverview" class="trend-overview">
          <div class="overview-box">
            <small>最新参考价</small>
            <strong>{{ priceOverview.latest != null ? `¥${Math.round(priceOverview.latest)}` : '-' }}</strong>
          </div>
          <div class="overview-box">
            <small>最低参考价</small>
            <strong>¥{{ Math.round(priceOverview.min) }}</strong>
          </div>
          <div class="overview-box">
            <small>最高参考价</small>
            <strong>¥{{ Math.round(priceOverview.max) }}</strong>
          </div>
          <div class="overview-box">
            <small>最近更新</small>
            <strong>{{ priceOverview.lastDate || '-' }}</strong>
          </div>
        </div>

        <!-- 记录列表 -->
        <div v-if="filteredPriceRecords.length" class="price-records">
          <div v-for="(record, idx) in filteredPriceRecords" :key="idx" class="price-record">
            <div class="record-head">
              <span class="record-type">{{ priceTypeLabels[record.priceType] || record.priceType }}</span>
              <span class="record-platform">{{ record.platform }}</span>
              <span class="record-date">{{ record.referenceDate }}</span>
            </div>
            <div class="record-prices">
              <div v-if="record.avgPrice != null" class="record-price">
                <small>均价</small>
                <strong>¥{{ Math.round(record.avgPrice) }}</strong>
              </div>
              <div v-if="record.minPrice != null && record.maxPrice != null" class="record-price">
                <small>区间</small>
                <strong>¥{{ Math.round(record.minPrice) }} - ¥{{ Math.round(record.maxPrice) }}</strong>
              </div>
              <div v-if="record.sampleCount" class="record-price">
                <small>样本</small>
                <strong>{{ record.sampleCount }}</strong>
              </div>
            </div>
            <p v-if="record.remark" class="record-remark">{{ record.remark }}</p>
          </div>
        </div>

        <div v-else class="trend-empty">暂无价格记录</div>
      </div>

      <div v-if="product.purchaseLinks.length" class="panel links">
        <h2>购买链接</h2>
        <a v-for="link in product.purchaseLinks" :key="link.id" :href="link.url" target="_blank" rel="noopener" class="link-item">
          {{ link.platform }} - {{ link.title }}
        </a>
      </div>
    </div>
  </section>

  <!-- 价格提醒弹窗 -->
  <Teleport to="body">
    <div v-if="showAlertModal" class="modal-overlay" @click.self="showAlertModal = false">
      <div class="modal panel">
        <h2>设置价格提醒</h2>
        <p class="modal-desc">当产品参考价低于或等于目标价时，会标记为"已达到目标价"</p>
        <label>
          <span>目标价（元）</span>
          <input v-model.number="alertTargetPrice" type="number" step="1" min="1" placeholder="输入目标价格" />
        </label>
        <div class="modal-actions">
          <button class="ghost-button" @click="showAlertModal = false">取消</button>
          <button class="primary-button" :disabled="alertSaving || !alertTargetPrice" @click="handleSaveAlert">
            {{ alertSaving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 22px;
  padding-top: 42px;
}

.summary {
  padding: 0;
  overflow: hidden;
}

.img-area {
  width: 100%;
  height: 360px;
  background: var(--soft);
  display: grid;
  place-items: center;
  overflow: hidden;
  padding: 28px;
}

.img-area img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
}

.placeholder {
  color: var(--ink);
  opacity: 0.3;
}

.info {
  padding: 28px 30px 30px;
}

p {
  margin: 0 0 10px;
  color: var(--green);
  font-weight: 850;
}

h1 {
  margin: 0;
  font-size: 42px;
  line-height: 1.1;
}

.cat-badge {
  display: inline-block;
  margin-top: 12px;
  padding: 4px 12px;
  border-radius: 999px;
  background: var(--soft);
  color: var(--muted);
  font-size: 13px;
  font-weight: 650;
}

.desc {
  display: block;
  margin-top: 16px;
  color: var(--text);
  font-size: 15px;
  line-height: 1.75;
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 22px;
}

.favorite-action {
  height: 38px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--ink);
  font-size: 13px;
  font-weight: 750;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  cursor: pointer;
}

.favorite-action.active {
  border-color: #e25555;
  color: #e25555;
  background: #fef2f2;
}

.alert-action {
  height: 38px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--ink);
  font-size: 13px;
  font-weight: 750;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  cursor: pointer;
}

.alert-action.set {
  border-color: #d97706;
  color: #d97706;
  background: #fefce8;
}

.alert-action.triggered {
  border-color: var(--green);
  color: var(--green);
  background: #eef5f2;
}

.compare-action,
.compare-link {
  height: 38px;
  padding: 0 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 750;
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.compare-action {
  border: 1px solid var(--ink);
  background: var(--ink);
  color: #fff;
  cursor: pointer;
}

.compare-action.active {
  border-color: var(--green);
  background: var(--green);
}

.compare-action:disabled {
  border-color: var(--line);
  background: var(--soft);
  color: var(--muted);
  cursor: not-allowed;
}

.compare-link {
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink);
  text-decoration: none;
}

.price-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 24px;
}

.price-row div {
  border: 1px solid var(--line);
  border-radius: 9px;
  padding: 14px;
  background: #fdfdfc;
}

small {
  display: block;
  margin-bottom: 6px;
  color: var(--muted);
  font-size: 13px;
}

.side {
  display: grid;
  gap: 18px;
}

.panel {
  padding: 22px;
}

h2 {
  margin: 0 0 16px;
  font-size: 20px;
}

h3 {
  margin: 14px 0 8px;
  font-size: 14px;
  color: var(--green);
  font-weight: 800;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.gallery-button {
  aspect-ratio: 1;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--soft);
  color: var(--muted);
  cursor: pointer;
  overflow: hidden;
  padding: 8px;
  transition: border-color .15s ease, box-shadow .15s ease;
}

.gallery-button:hover,
.gallery-button.active {
  border-color: var(--green);
  box-shadow: inset 0 0 0 1px var(--green);
}

.gallery-button img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
}

.gallery-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.metric-item {
  min-width: 0;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 10px;
  background: #fdfdfc;
}

.metric-item small {
  margin-bottom: 5px;
}

.metric-item strong {
  display: block;
  color: var(--ink);
  font-size: 13px;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.spec {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-top: 1px solid var(--line);
}

.spec strong {
  color: var(--muted);
  font-size: 13px;
  font-weight: 600;
}

.spec em {
  color: var(--text);
  font-style: normal;
  font-size: 13px;
}

.tag-section + .tag-section {
  margin-top: 16px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.decision-tag {
  display: inline-grid;
  gap: 3px;
  max-width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fdfdfc;
}

.decision-tag strong {
  color: var(--ink);
  font-size: 13px;
}

.decision-tag em {
  color: var(--muted);
  font-size: 12px;
  font-style: normal;
  line-height: 1.5;
}

.link-item {
  display: block;
  padding: 12px 0;
  border-top: 1px solid var(--line);
  color: var(--ink);
  font-size: 14px;
  text-decoration: none;
}

.link-item:first-of-type {
  border-top: none;
}

.link-item:hover {
  color: var(--green);
}

.empty {
  margin-top: 42px;
  padding: 24px;
  text-align: center;
  color: var(--muted);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.3);
  display: grid;
  place-items: center;
  z-index: 100;
}

.modal {
  width: min(420px, 90vw);
  padding: 28px;
}

.modal h2 {
  margin: 0 0 6px;
  font-size: 20px;
}

.modal-desc {
  margin: 0 0 16px;
  color: var(--muted);
  font-size: 13px;
}

.modal label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
}

.modal input {
  height: 42px;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 0 12px;
  font: inherit;
  color: var(--ink);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.primary-button {
  height: 38px;
  padding: 0 18px;
  border: 0;
  border-radius: 8px;
  background: var(--ink);
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
}

.ghost-button {
  height: 38px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--ink);
  font-weight: 650;
  font-size: 13px;
  cursor: pointer;
}

/* 价格走势 */
.price-trend {
  padding: 22px;
}

.trend-head {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 16px;
  margin-bottom: 16px;
}

.section-en {
  margin: 0 0 4px;
  color: var(--green);
  font-size: 12px;
  font-weight: 850;
}

.trend-head h2 {
  margin: 0;
  font-size: 20px;
}

.trend-filters {
  display: flex;
  gap: 0;
  border: 1px solid var(--line);
  border-radius: 8px;
  overflow: hidden;
}

.trend-filters button {
  height: 32px;
  padding: 0 12px;
  border: 0;
  border-right: 1px solid var(--line);
  background: #fff;
  color: var(--muted);
  font-size: 12px;
  font-weight: 650;
  cursor: pointer;
}

.trend-filters button:last-child {
  border-right: 0;
}

.trend-filters button.active {
  background: var(--ink);
  color: #fff;
}

.trend-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.overview-box {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 10px 12px;
  background: #fdfdfc;
}

.overview-box small {
  display: block;
  color: var(--muted);
  font-size: 11px;
  margin-bottom: 4px;
}

.overview-box strong {
  font-size: 16px;
}

.price-records {
  display: grid;
  gap: 10px;
}

.price-record {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 12px 14px;
  background: #fafaf8;
}

.record-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.record-type {
  padding: 2px 8px;
  border-radius: 4px;
  background: #eef5f2;
  color: var(--green);
  font-size: 12px;
  font-weight: 700;
}

.record-platform {
  color: var(--ink);
  font-size: 13px;
  font-weight: 650;
}

.record-date {
  color: var(--muted);
  font-size: 12px;
  margin-left: auto;
}

.record-prices {
  display: flex;
  gap: 16px;
}

.record-price small {
  display: block;
  color: var(--muted);
  font-size: 11px;
  margin-bottom: 2px;
}

.record-price strong {
  font-size: 14px;
}

.record-remark {
  margin: 8px 0 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.5;
}

.trend-empty {
  text-align: center;
  padding: 24px;
  color: var(--muted);
  font-size: 14px;
}

@media (max-width: 900px) {
  .detail {
    grid-template-columns: 1fr;
  }

  .price-row {
    grid-template-columns: 1fr;
  }

  .trend-overview {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
