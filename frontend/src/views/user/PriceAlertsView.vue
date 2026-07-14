<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { Bell, Loader2 } from '@lucide/vue';
import { fetchPriceAlerts, deletePriceAlert, refreshPriceAlerts } from '@/api/priceAlert';
import { useToastStore } from '@/stores/toastStore';
import type { PriceAlertItem } from '@/types/priceAlert';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const toastStore = useToastStore();
const alerts = ref<PriceAlertItem[]>([]);
const loading = ref(true);
const error = ref(false);

async function loadAlerts() {
  loading.value = true;
  error.value = false;
  try {
    alerts.value = await fetchPriceAlerts();
  } catch {
    error.value = true;
    alerts.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(loadAlerts);

async function handleDelete(id: number) {
  if (!confirm('确定删除该价格提醒吗？')) return;
  try {
    await deletePriceAlert(id);
    alerts.value = alerts.value.filter(a => a.id !== id);
    toastStore.success('已删除价格提醒');
  } catch {
    toastStore.error('删除失败，请重试');
  }
}

const refreshing = ref(false);

async function handleRefresh() {
  refreshing.value = true;
  try {
    alerts.value = await refreshPriceAlerts();
    toastStore.success('价格状态已刷新');
  } catch {
    toastStore.error('刷新失败，请重试');
  } finally {
    refreshing.value = false;
  }
}

function statusText(alert: PriceAlertItem): string {
  if (alert.triggered) return '已达到目标价';
  return alert.status === 'ACTIVE' ? '监控中' : alert.status;
}

function statusClass(alert: PriceAlertItem): string {
  if (alert.triggered) return 'triggered';
  return alert.status === 'ACTIVE' ? 'active' : '';
}
</script>

<template>
  <section class="alerts-page">
    <div class="page-head">
      <div>
        <p>Price Alerts</p>
        <h1>价格提醒</h1>
      </div>
      <div class="head-actions">
        <button class="ghost-button" @click="handleRefresh" :disabled="refreshing">
          <Loader2 v-if="refreshing" :size="14" class="spin" />
          {{ refreshing ? '刷新中...' : '刷新价格状态' }}
        </button>
        <RouterLink to="/products" class="ghost-button">去产品库设置</RouterLink>
      </div>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="error" message="加载价格提醒失败" @retry="loadAlerts" />

    <EmptyState v-else-if="alerts.length === 0"
      :icon="Bell"
      title="还没有价格提醒"
      description="在产品详情页设置目标价后会显示在这里"
      button-label="去产品库"
      @action="$router.push('/products')"
    />

    <div v-else class="alert-list">
      <div v-for="alert in alerts" :key="alert.id" class="alert-item panel">
        <div class="alert-img">
          <img v-if="alert.coverUrl" :src="alert.coverUrl" :alt="alert.productName" />
          <div v-else class="placeholder-img" />
        </div>
        <div class="alert-info">
          <div class="alert-topline">
            <span>{{ alert.brandName || '未知品牌' }} · {{ alert.categoryName || '数码产品' }}</span>
            <span class="status-badge" :class="statusClass(alert)">{{ statusText(alert) }}</span>
          </div>
          <h3>{{ alert.productName }}</h3>
          <div class="price-row">
            <div class="price-box"><small>目标价</small><strong>¥{{ alert.targetPrice }}</strong></div>
            <div class="price-box"><small>当前参考价</small><strong>{{ alert.lastPrice != null ? `¥${alert.lastPrice}` : '暂无数据' }}</strong></div>
          </div>
          <p v-if="alert.triggeredAt" class="triggered-time">触发于 {{ alert.triggeredAt }}</p>
        </div>
        <div class="alert-actions">
          <RouterLink :to="`/products/${alert.productId}`" class="action-btn">查看产品</RouterLink>
          <button class="action-btn danger" @click="handleDelete(alert.id)">删除</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.alerts-page { padding-top: 36px; }
.page-head { display: flex; justify-content: space-between; align-items: end; margin-bottom: 24px; }
.page-head p { margin: 0 0 6px; color: var(--green); font-weight: 850; font-size: 13px; }
.page-head h1 { margin: 0; font-size: 36px; }
.ghost-button { height: 36px; padding: 0 14px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--ink); font-weight: 650; font-size: 13px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; text-decoration: none; }
.head-actions { display: flex; gap: 10px; }
.triggered-time { margin: 6px 0 0; color: var(--green); font-size: 12px; font-weight: 600; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.alert-list { display: grid; gap: 14px; }
.alert-item { display: grid; grid-template-columns: 100px 1fr auto; gap: 16px; align-items: center; padding: 14px; }
.alert-img { width: 100px; height: 80px; border-radius: 8px; background: var(--soft); overflow: hidden; display: grid; place-items: center; }
.alert-img img { width: 100%; height: 100%; object-fit: contain; }
.placeholder-img { width: 100%; height: 100%; background: var(--soft); }
.alert-info { min-width: 0; }
.alert-topline { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.alert-topline > span:first-child { color: var(--muted); font-size: 12px; font-weight: 700; }
.status-badge { padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 700; }
.status-badge.active { background: #eef5f2; color: var(--green); }
.status-badge.triggered { background: #fef3e2; color: #b45309; }
h3 { margin: 4px 0 8px; font-size: 16px; }
.price-row { display: flex; gap: 12px; }
.price-box { border: 1px solid var(--line); border-radius: 7px; padding: 6px 10px; background: #fdfdfc; }
.price-box small { display: block; color: var(--muted); font-size: 11px; margin-bottom: 2px; }
.price-box strong { font-size: 14px; }
.alert-actions { display: flex; flex-direction: column; gap: 8px; }
.action-btn { height: 32px; padding: 0 12px; border: 1px solid var(--line); border-radius: 7px; background: #fff; color: var(--ink); font-size: 12px; font-weight: 650; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; justify-content: center; }
.action-btn.danger { border-color: #e8d5d0; color: #b2472f; }
@media (max-width: 700px) { .alert-item { grid-template-columns: 1fr; } .alert-img { display: none; } }
</style>
