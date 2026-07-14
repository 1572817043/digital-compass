<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { useCompareStore } from '@/stores/compareStore';
import { fetchProductDetail } from '@/api/product';
import type { ProductDetail } from '@/types/product';
import { Smartphone, Laptop, ArrowLeft, Trash2, Diff, GitCompare } from '@lucide/vue';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const compareStore = useCompareStore();
const products = ref<ProductDetail[]>([]);
const loading = ref(true);
const showOnlyDiff = ref(false);

const tagRows = [
  { type: 'selling_point', label: '核心优点' },
  { type: 'weakness', label: '主要短板' },
  { type: 'suitable', label: '适合人群' },
  { type: 'unsuitable', label: '不适合人群' },
];

onMounted(async () => {
  if (compareStore.selectedIds.length === 0) {
    loading.value = false;
    return;
  }
  try {
    const results = await Promise.all(
      compareStore.selectedIds.map(id => fetchProductDetail(id))
    );
    products.value = results;
  } catch {
    products.value = [];
  } finally {
    loading.value = false;
  }
});

const hasProducts = computed(() => products.value.length > 0);
const hasComparableProducts = computed(() => products.value.length > 1);

const gridCols = computed(() => `140px repeat(${products.value.length}, 1fr)`);

const specGroups = computed(() => {
  const map = new Map<string, Set<string>>();
  for (const p of products.value) {
    for (const s of p.specs) {
      if (!map.has(s.group)) map.set(s.group, new Set());
      map.get(s.group)!.add(s.name);
    }
  }
  return map;
});

const visibleSpecGroups = computed(() => Array.from(specGroups.value.entries())
  .map(([group, names]) => ({
    group,
    names: Array.from(names).filter(name => shouldShowRow(specValues(group, name))),
  }))
  .filter(item => item.names.length > 0));

const visibleTagRows = computed(() => tagRows.filter(row => {
  const hasTags = products.value.some(p => getTags(p, row.type).length > 0);
  return hasTags && shouldShowRow(tagValues(row.type));
}));

function normalizeCell(value: unknown) {
  const text = String(value ?? '-').trim();
  return text.length > 0 ? text : '-';
}

function isDifferent(values: unknown[]) {
  if (products.value.length < 2) return false;
  return new Set(values.map(normalizeCell)).size > 1;
}

function shouldShowRow(values: unknown[]) {
  return !showOnlyDiff.value || isDifferent(values);
}

function rowClass(values: unknown[]) {
  return { 'diff-row': isDifferent(values) };
}

function getUsedPrice(p: ProductDetail): string {
  const used = p.prices.find(pr => pr.priceType === 'used');
  if (!used) return '-';
  if (used.minPrice != null && used.maxPrice != null) {
    return `${Math.round(used.minPrice)}-${Math.round(used.maxPrice)}`;
  }
  return '-';
}

function getTags(p: ProductDetail, type: string) {
  return p.tags.filter(t => t.tagType === type);
}

function getTagText(p: ProductDetail, type: string) {
  const tags = getTags(p, type);
  if (tags.length === 0) return '-';
  return tags.map(tag => tag.tagName || tag.tagValue || '-').join('、');
}

function getSpecValue(p: ProductDetail, group: string, name: string) {
  return p.specs.find(s => s.group === group && s.name === name)?.value || '-';
}

function scoreValues() {
  return products.value.map(p => p.score);
}

function officialPriceValues() {
  return products.value.map(p => p.officialPrice ?? '待定');
}

function usedPriceValues() {
  return products.value.map(getUsedPrice);
}

function specValues(group: string, name: string) {
  return products.value.map(p => getSpecValue(p, group, name));
}

function tagValues(type: string) {
  return products.value.map(p => getTagText(p, type));
}

function remove(id: number) {
  compareStore.removeProduct(id);
  products.value = products.value.filter(p => p.id !== id);
}

function clearCompare() {
  compareStore.clear();
  products.value = [];
}
</script>

<template>
  <section class="compare-page">
    <div class="compare-head">
      <div>
        <p>Compare Tool</p>
        <h1>产品对比</h1>
      </div>
      <div class="head-actions">
        <RouterLink to="/products" class="ghost-button"><ArrowLeft :size="16" /> 去产品库添加</RouterLink>
        <button v-if="hasProducts" class="ghost-button danger" @click="clearCompare">清空对比</button>
      </div>
    </div>

    <!-- 空状态 -->
    <LoadingState v-if="loading" />

    <EmptyState v-else-if="!hasProducts"
      :icon="GitCompare"
      title="暂无对比产品"
      description="在产品库中选择最多 3 个产品加入对比"
      button-label="去产品库"
      @action="$router.push('/products')"
    />

    <!-- 对比内容 -->
    <div v-else class="compare-body">
      <div class="compare-toolbar">
        <label class="diff-toggle" :class="{ disabled: !hasComparableProducts }">
          <input v-model="showOnlyDiff" type="checkbox" :disabled="!hasComparableProducts" />
          <Diff :size="15" />
          <span>只看差异</span>
        </label>
        <span>{{ products.length }} 个产品正在对比</span>
      </div>

      <!-- 产品头部卡片 -->
      <div class="compare-grid" :style="{ gridTemplateColumns: gridCols }">
        <div class="row-label"></div>
        <div v-for="p in products" :key="p.id" class="compare-col">
          <div class="col-header">
            <button class="remove-btn" @click="remove(p.id)"><Trash2 :size="14" /></button>
            <div class="col-img">
              <img v-if="p.images.length > 0" :src="p.images[0].imageUrl" :alt="p.name" />
              <Smartphone v-else-if="p.categoryCode === 'phone'" :size="40" />
              <Laptop v-else :size="48" />
            </div>
            <h3>{{ p.name }}</h3>
            <p class="col-brand">{{ p.brandName }} · {{ p.categoryName }}</p>
          </div>
        </div>
      </div>

      <!-- 推荐分 -->
      <div v-if="shouldShowRow(scoreValues())" class="compare-grid" :class="rowClass(scoreValues())" :style="{ gridTemplateColumns: gridCols }">
        <div class="row-label">推荐分</div>
        <div v-for="p in products" :key="p.id" class="cell score-cell">{{ p.score }}</div>
      </div>

      <!-- 价格 -->
      <div v-if="shouldShowRow(officialPriceValues())" class="compare-grid" :class="rowClass(officialPriceValues())" :style="{ gridTemplateColumns: gridCols }">
        <div class="row-label">官方价</div>
        <div v-for="p in products" :key="p.id" class="cell">{{ p.officialPrice != null ? `¥${p.officialPrice}` : '待定' }}</div>
      </div>
      <div v-if="shouldShowRow(usedPriceValues())" class="compare-grid" :class="rowClass(usedPriceValues())" :style="{ gridTemplateColumns: gridCols }">
        <div class="row-label">二手参考</div>
        <div v-for="p in products" :key="p.id" class="cell">{{ getUsedPrice(p) }}</div>
      </div>

      <!-- 核心参数 -->
      <template v-if="visibleSpecGroups.length">
        <div class="section-divider">核心参数</div>
        <div v-for="item in visibleSpecGroups" :key="item.group">
        <div class="compare-grid" v-for="specName in item.names" :key="`${item.group}-${specName}`" :class="rowClass(specValues(item.group, specName))" :style="{ gridTemplateColumns: gridCols }">
          <div class="row-label">{{ specName }}</div>
          <div v-for="p in products" :key="p.id" class="cell">
            {{ getSpecValue(p, item.group, specName) }}
          </div>
        </div>
      </div>
      </template>

      <!-- 选购判断 tags -->
      <template v-if="visibleTagRows.length">
        <div class="section-divider">选购判断</div>
        <div class="compare-grid" v-for="row in visibleTagRows" :key="row.type" :class="rowClass(tagValues(row.type))" :style="{ gridTemplateColumns: gridCols }">
          <div class="row-label">{{ row.label }}</div>
          <div v-for="p in products" :key="p.id" class="cell">
            <div v-for="tag in getTags(p, row.type)" :key="tag.id" class="tag-chip">{{ tag.tagName || tag.tagValue }}</div>
            <span v-if="getTags(p, row.type).length === 0" class="muted">-</span>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.compare-page {
  padding-top: 36px;
}

.compare-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  margin-bottom: 24px;
}

.compare-head p {
  margin: 0 0 6px;
  color: var(--green);
  font-weight: 850;
  font-size: 13px;
}

.compare-head h1 {
  margin: 0;
  font-size: 36px;
}

.head-actions {
  display: flex;
  gap: 10px;
}

.empty {
  text-align: center;
  padding: 60px 24px;
  color: var(--muted);
}

.empty p {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 18px;
  font-weight: 700;
}

.primary-button {
  height: 38px;
  padding: 0 18px;
  border: 0;
  border-radius: 8px;
  background: var(--ink);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
}

.ghost-button {
  height: 36px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--ink);
  font-weight: 650;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
}

.ghost-button.danger {
  color: #b2472f;
  border-color: #e8d5d0;
}

.compare-body {
  overflow-x: auto;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: #fff;
}

.compare-toolbar {
  min-width: 680px;
  min-height: 48px;
  padding: 0 14px;
  border-bottom: 1px solid var(--line);
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--muted);
  font-size: 13px;
}

.diff-toggle {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--ink);
  font-weight: 700;
  cursor: pointer;
}

.diff-toggle input {
  width: 15px;
  height: 15px;
  accent-color: var(--green);
}

.diff-toggle.disabled {
  color: var(--muted);
  cursor: not-allowed;
}

.compare-grid {
  display: grid;
  border-bottom: 1px solid var(--line);
}

.compare-grid:last-child {
  border-bottom: none;
}

.row-label {
  padding: 12px 14px;
  color: var(--muted);
  font-size: 13px;
  font-weight: 650;
  background: #fafaf8;
  border-right: 1px solid var(--line);
  display: flex;
  align-items: center;
}

.cell {
  padding: 12px 14px;
  font-size: 13px;
  color: var(--ink);
  border-right: 1px solid var(--line);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.cell:last-child {
  border-right: none;
}

.diff-row .row-label {
  background: #eef5f2;
  color: var(--green);
}

.diff-row .cell {
  background: #fbfdfc;
}

.score-cell {
  color: var(--green);
  font-size: 18px;
  font-weight: 900;
}

.section-divider {
  padding: 10px 14px;
  background: var(--soft);
  color: var(--ink);
  font-size: 14px;
  font-weight: 750;
  border-bottom: 1px solid var(--line);
}

/* 列头部 */
.col-header {
  padding: 14px;
  text-align: center;
  position: relative;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: #fff;
  color: var(--muted);
  cursor: pointer;
  display: grid;
  place-items: center;
}

.remove-btn:hover {
  color: #b2472f;
  border-color: #e8d5d0;
}

.col-img {
  width: 80px;
  height: 80px;
  margin: 0 auto 10px;
  display: grid;
  place-items: center;
  background: var(--soft);
  border-radius: 8px;
  overflow: hidden;
}

.col-img img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.col-header h3 {
  margin: 0;
  font-size: 15px;
}

.col-brand {
  margin: 4px 0 0 !important;
  color: var(--muted) !important;
  font-size: 12px !important;
}

.tag-chip {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  background: #eef5f2;
  color: var(--green);
  font-size: 12px;
  font-weight: 600;
}

.muted {
  color: var(--muted);
}
</style>
