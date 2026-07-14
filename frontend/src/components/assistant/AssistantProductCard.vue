<script setup lang="ts">
import { computed, ref } from 'vue';
import { ArrowRight, GitCompare, ImageOff } from '@lucide/vue';
import type { AssistantRecommendationItem } from '@/types/assistant';
import { useCompareStore } from '@/stores/compareStore';

const props = defineProps<{
  item: AssistantRecommendationItem;
}>();

const compareStore = useCompareStore();
const imageFailed = ref(false);

const priceText = computed(() => {
  if (props.item.officialPrice == null) return '官方价待定';
  return `¥${props.item.officialPrice}`;
});

const usedText = computed(() => {
  if (props.item.usedMinPrice == null && props.item.usedMaxPrice == null) return '暂无二手参考';
  if (props.item.usedMinPrice != null && props.item.usedMaxPrice != null) {
    return `¥${props.item.usedMinPrice}-${props.item.usedMaxPrice}`;
  }
  return `¥${props.item.usedMinPrice ?? props.item.usedMaxPrice}`;
});

const hasExplain = computed(() =>
  Boolean(props.item.explainSummary)
  || Boolean(props.item.matchedRequirements?.length)
  || Boolean(props.item.knowledgeEvidence?.length)
  || Boolean(props.item.tradeoffNotes?.length)
);

function toggleCompare(event: Event) {
  event.preventDefault();
  event.stopPropagation();
  if (props.item.productId == null) return;
  if (compareStore.isSelected(props.item.productId)) {
    compareStore.removeProduct(props.item.productId);
  } else if (compareStore.canAdd) {
    compareStore.addProduct(props.item.productId);
  }
}
</script>

<template>
  <RouterLink v-if="item.detailPath" :to="item.detailPath" class="assistant-product-card">
    <div class="product-thumb">
      <img v-if="item.coverUrl && !imageFailed" :src="item.coverUrl" :alt="item.productName" @error="imageFailed = true" />
      <ImageOff v-else :size="30" />
    </div>
    <div class="product-info">
      <div class="product-topline">
        <span>{{ item.brandName || '未知品牌' }} · {{ item.categoryName || '数码产品' }}</span>
        <strong>{{ item.matchScore ?? item.productScore ?? '-' }}</strong>
      </div>
      <h3>{{ item.productName }}</h3>
      <div v-if="item.matchTags?.length" class="tag-row match-tags">
        <span v-for="tag in item.matchTags" :key="tag">{{ tag }}</span>
      </div>
      <p>{{ item.reason }}</p>
      <div v-if="hasExplain" class="explain-box">
        <div v-if="item.explainSummary" class="explain-summary">{{ item.explainSummary }}</div>
        <div class="explain-grid">
          <div v-if="item.matchedRequirements?.length" class="explain-group">
            <small>匹配条件</small>
            <span v-for="value in item.matchedRequirements" :key="value">{{ value }}</span>
          </div>
          <div v-if="item.knowledgeEvidence?.length" class="explain-group">
            <small>知识依据</small>
            <span v-for="value in item.knowledgeEvidence" :key="value">{{ value }}</span>
          </div>
          <div v-if="item.tradeoffNotes?.length" class="explain-group">
            <small>权衡点</small>
            <span v-for="value in item.tradeoffNotes" :key="value">{{ value }}</span>
          </div>
        </div>
      </div>
      <div class="meta-row">
        <span>{{ priceText }}</span>
        <span>{{ usedText }}</span>
      </div>
      <div v-if="item.cautionTags?.length" class="tag-row caution-tags">
        <span v-for="tag in item.cautionTags" :key="tag">{{ tag }}</span>
      </div>
      <div class="action-row">
        <button
          class="compare-action"
          :class="{ active: item.productId != null && compareStore.isSelected(item.productId) }"
          :disabled="item.productId == null || (!compareStore.canAdd && !compareStore.isSelected(item.productId))"
          @click="toggleCompare"
        >
          <GitCompare :size="14" />
          {{ item.productId != null && compareStore.isSelected(item.productId) ? '已加入对比' : '加入对比' }}
        </button>
        <span class="detail-link">查看详情 <ArrowRight :size="14" /></span>
      </div>
      <div v-if="item.nextActions?.length" class="next-actions">
        <small>下一步</small>
        <span v-for="action in item.nextActions" :key="action">{{ action }}</span>
      </div>
      <div v-if="item.riskTip" class="risk-tip">{{ item.riskTip }}</div>
    </div>
  </RouterLink>
</template>

<style scoped>
.assistant-product-card {
  display: grid;
  grid-template-columns: 116px 1fr;
  gap: 14px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  padding: 12px;
  transition: border-color .18s ease, transform .18s ease;
}

.assistant-product-card:hover {
  border-color: #cbc8bf;
  transform: translateY(-1px);
}

.product-thumb {
  height: 116px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: var(--soft);
  color: var(--muted);
  overflow: hidden;
}

.product-thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-info {
  min-width: 0;
}

.product-topline,
.meta-row,
.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.product-topline span {
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
}

.product-topline strong {
  color: var(--green);
  font-size: 18px;
}

h3 {
  margin: 4px 0 6px;
  color: var(--ink);
  font-size: 17px;
  line-height: 1.25;
}

p {
  margin: 0;
  color: var(--text);
  font-size: 13px;
  line-height: 1.55;
}

.meta-row {
  justify-content: flex-start;
  margin-top: 10px;
}

.meta-row span {
  border: 1px solid var(--line);
  border-radius: 7px;
  background: #fdfdfc;
  padding: 6px 8px;
  color: var(--ink);
  font-size: 12px;
  font-weight: 750;
}

.explain-box {
  margin-top: 10px;
  border: 1px solid #e5e2d9;
  border-radius: 8px;
  background: #fbfaf7;
  padding: 9px;
}

.explain-summary {
  margin-bottom: 8px;
  color: var(--ink);
  font-size: 12px;
  font-weight: 800;
  line-height: 1.5;
}

.explain-grid {
  display: grid;
  gap: 8px;
}

.explain-group {
  display: grid;
  gap: 5px;
}

.explain-group small {
  color: var(--green);
  font-size: 11px;
  font-weight: 900;
}

.explain-group span {
  color: var(--text);
  font-size: 12px;
  line-height: 1.5;
}

.tag-row,
.next-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-row {
  margin: 6px 0 8px;
}

.tag-row span,
.next-actions span {
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
}

.match-tags span {
  background: #eef7f3;
  color: var(--green);
}

.caution-tags {
  margin-top: 8px;
}

.caution-tags span {
  background: #f7f1e8;
  color: #80531f;
}

.action-row {
  margin-top: 10px;
}

.compare-action {
  height: 30px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--line);
  border-radius: 7px;
  background: #fff;
  color: var(--ink);
  padding: 0 10px;
  font-size: 12px;
  font-weight: 750;
  cursor: pointer;
}

.compare-action.active {
  border-color: var(--green);
  background: #eef7f3;
  color: var(--green);
}

.compare-action:disabled {
  opacity: .45;
  cursor: not-allowed;
}

.detail-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--green);
  font-size: 12px;
  font-weight: 800;
}

.next-actions {
  margin-top: 8px;
}

.next-actions small {
  color: var(--muted);
  font-size: 11px;
  font-weight: 800;
}

.next-actions span {
  background: var(--soft);
  color: var(--text);
}

.risk-tip {
  margin-top: 10px;
  border-radius: 7px;
  background: #f7f1e8;
  color: #80531f;
  padding: 8px 9px;
  font-size: 12px;
  line-height: 1.55;
}

@media (max-width: 720px) {
  .assistant-product-card {
    grid-template-columns: 88px 1fr;
  }

  .product-thumb {
    height: 88px;
  }
}
</style>
