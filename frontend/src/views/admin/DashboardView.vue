<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { BarChart3, Heart, History, MessageSquare, RefreshCw, TrendingUp, Users } from '@lucide/vue';
import { fetchDashboardOverview } from '@/api/admin';
import { useToastStore } from '@/stores/toastStore';
import type { DashboardOverview } from '@/api/admin';
import LoadingState from '@/components/common/LoadingState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const data = ref<DashboardOverview | null>(null);
const loading = ref(true);
const error = ref(false);
const toastStore = useToastStore();

async function loadOverview() {
  loading.value = true;
  error.value = false;
  try {
    data.value = await fetchDashboardOverview();
  } catch {
    error.value = true;
  } finally {
    loading.value = false;
  }
}

onMounted(loadOverview);
</script>

<template>
  <LoadingState v-if="loading" />

  <ErrorState v-else-if="error" message="加载概览数据失败" @retry="loadOverview" />

  <section v-else-if="data">
    <div class="admin-head">
      <div>
        <p>Admin Console</p>
        <h1>管理端概览</h1>
      </div>
      <button class="ghost-button" @click="loadOverview">
        <RefreshCw :size="14" /> 刷新
      </button>
    </div>

    <div class="stat-cards">
      <div class="stat-card panel">
        <BarChart3 :size="20" />
        <div><small>产品数量</small><strong>{{ data.productCount }}</strong></div>
      </div>
      <div class="stat-card panel">
        <Users :size="20" />
        <div><small>用户数量</small><strong>{{ data.userCount }}</strong></div>
      </div>
      <div class="stat-card panel">
        <Heart :size="20" />
        <div><small>收藏总数</small><strong>{{ data.favoriteCount }}</strong></div>
      </div>
      <div class="stat-card panel">
        <History :size="20" />
        <div><small>浏览记录</small><strong>{{ data.historyCount }}</strong></div>
      </div>
      <div class="stat-card panel">
        <TrendingUp :size="20" />
        <div><small>价格提醒</small><strong>{{ data.priceAlertCount }}</strong></div>
      </div>
      <div class="stat-card panel">
        <MessageSquare :size="20" />
        <div><small>AI 会话</small><strong>{{ data.assistantConversationCount }}</strong></div>
      </div>
    </div>

    <div class="bottom-grid">
      <div class="panel">
        <h2>最近产品</h2>
        <div class="list-item" v-for="p in data.recentProducts" :key="p.id">
          <div>
            <RouterLink :to="`/products/${p.id}`" class="item-link">{{ p.name }}</RouterLink>
            <span>{{ p.brandName }} · {{ p.categoryName }}</span>
          </div>
          <strong>{{ p.officialPrice != null ? `¥${p.officialPrice}` : '-' }}</strong>
        </div>
      </div>

      <div class="panel">
        <h2>最近 AI 咨询</h2>
        <div class="list-item" v-for="log in data.recentWorkflowLogs" :key="log.id">
          <div>
            <span class="username">{{ log.username || '匿名' }}</span>
            <p>{{ log.userRequirement?.substring(0, 40) || '-' }}{{ (log.userRequirement?.length || 0) > 40 ? '...' : '' }}</p>
          </div>
          <span :class="log.fallbackUsed ? 'fallback' : 'ai'">{{ log.fallbackUsed ? '规则' : 'AI' }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-head { display: flex; justify-content: space-between; align-items: start; margin-bottom: 22px; }
p { margin: 0 0 6px; color: var(--green); font-weight: 850; font-size: 13px; }
h1 { margin: 0; font-size: 28px; }
.ghost-button { height: 36px; padding: 0 14px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--ink); font-weight: 650; font-size: 13px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; }
h2 { margin: 0 0 14px; font-size: 17px; }

.stat-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 22px; }
.stat-card { display: flex; align-items: center; gap: 14px; padding: 18px; }
.stat-card svg { color: var(--green); flex-shrink: 0; }
.stat-card small { display: block; color: var(--muted); font-size: 12px; margin-bottom: 4px; }
.stat-card strong { font-size: 24px; }

.bottom-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.bottom-grid .panel { padding: 18px; }

.list-item { display: flex; justify-content: space-between; align-items: center; gap: 12px; padding: 10px 0; border-top: 1px solid var(--line); }
.list-item:first-of-type { border-top: 0; }
.list-item span { display: block; color: var(--muted); font-size: 12px; margin-top: 3px; }
.list-item strong { font-size: 14px; white-space: nowrap; }
.item-link { color: var(--ink); font-weight: 700; text-decoration: none; }
.item-link:hover { color: var(--green); }
.username { font-weight: 700; color: var(--ink); }
.fallback { padding: 2px 8px; border-radius: 4px; background: #fef2f2; color: #b2472f; font-size: 11px; font-weight: 700; }
.ai { padding: 2px 8px; border-radius: 4px; background: #eef5f2; color: var(--green); font-size: 11px; font-weight: 700; }

@media (max-width: 900px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
  .bottom-grid { grid-template-columns: 1fr; }
}
</style>
