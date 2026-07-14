<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { CheckCircle2, ExternalLink, Loader2, RefreshCw, MessageSquare } from '@lucide/vue';
import { fetchAiWorkflowLogDetail, fetchAiWorkflowLogs } from '@/api/aiWorkflow';
import { useToastStore } from '@/stores/toastStore';
import type { AiWorkflowLogDetail, AiWorkflowLogItem } from '@/types/aiWorkflow';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const logs = ref<AiWorkflowLogItem[]>([]);
const loading = ref(true);
const loadError = ref(false);
const detailLoading = ref(false);
const detailError = ref(false);
const toastStore = useToastStore();
const keyword = ref('');
const fallbackFilter = ref('');
const selectedId = ref<number | null>(null);
const selectedDetail = ref<AiWorkflowLogDetail | null>(null);

const selectedSummary = computed(() => logs.value.find(item => item.id === selectedId.value) || logs.value[0] || null);
const selectedLog = computed(() => selectedDetail.value || selectedSummary.value);
const selectedCandidateIdsText = computed(() => selectedDetail.value?.candidateProductIdsText ?? selectedSummary.value?.candidateProductIds ?? null);

onMounted(() => loadLogs());

async function loadLogs() {
  loading.value = true;
  loadError.value = false;
  try {
    logs.value = await fetchAiWorkflowLogs({
      keyword: keyword.value || undefined,
      fallbackUsed: fallbackFilter.value === '' ? undefined : fallbackFilter.value === 'true',
      limit: 80,
    });
    if (!logs.value.some(item => item.id === selectedId.value)) {
      selectedId.value = logs.value[0]?.id || null;
    }
    if (selectedId.value) {
      await loadDetail(selectedId.value);
    } else {
      selectedDetail.value = null;
    }
  } catch {
    loadError.value = true;
    logs.value = [];
    selectedDetail.value = null;
    toastStore.error('加载工作流日志失败');
  } finally {
    loading.value = false;
  }
}

async function selectLog(id: number) {
  selectedId.value = id;
  await loadDetail(id);
}

async function loadDetail(id: number) {
  detailLoading.value = true;
  detailError.value = false;
  try {
    selectedDetail.value = await fetchAiWorkflowLogDetail(id);
  } catch {
    detailError.value = true;
    selectedDetail.value = null;
    toastStore.error('加载日志详情失败');
  } finally {
    detailLoading.value = false;
  }
}

function formatTime(value: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-';
}

function formatJson(value: string | null) {
  if (!value) return '-';
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
}

function formatParsedRequirement(value: Record<string, unknown> | undefined) {
  if (!value || Object.keys(value).length === 0) return '-';
  return JSON.stringify(value, null, 2);
}

function candidateList(value: string | null) {
  return value ? value.split(',').map(item => item.trim()).filter(Boolean) : [];
}

function stepStatusLabel(status: string) {
  if (status === 'success') return '完成';
  if (status === 'fallback') return '回退';
  if (status === 'empty') return '无数据';
  return status || '-';
}

function formatPrice(value: number | null) {
  if (value == null) return '价格暂缺';
  return `¥${Math.round(value)}`;
}
</script>

<template>
  <section>
    <div class="admin-head">
      <div>
        <span class="eyebrow">AI Workflow</span>
        <h1>AI 工作流日志</h1>
      </div>
      <button class="ghost-button" type="button" @click="loadLogs" :disabled="loading">
        <RefreshCw :size="15" :class="{ spin: loading }" /> 刷新
      </button>
    </div>

    <div class="filters panel">
      <input v-model="keyword" placeholder="搜索需求、解析结果、RAG 摘要" @keyup.enter="loadLogs" />
      <select v-model="fallbackFilter">
        <option value="">全部状态</option>
        <option value="false">AI 解析</option>
        <option value="true">规则回退</option>
      </select>
      <button class="primary-button" type="button" @click="loadLogs">查询</button>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="loadError" message="加载工作流日志失败" @retry="loadLogs" />

    <EmptyState v-else-if="logs.length === 0"
      :icon="MessageSquare"
      title="暂无 AI 调用记录"
      description="用户使用选购助手后会产生工作流日志"
    />

    <div v-else class="workflow-layout">
      <div class="log-list">
        <button
          v-for="item in logs"
          :key="item.id"
          class="log-card panel"
          :class="{ active: selectedLog?.id === item.id }"
          type="button"
          @click="selectLog(item.id)"
        >
          <div class="log-card-head">
            <strong>#{{ item.id }}</strong>
            <span :class="item.fallbackUsed ? 'status fallback' : 'status ai'">
              {{ item.fallbackUsed ? '规则回退' : 'AI 解析' }}
            </span>
          </div>
          <p>{{ item.userRequirement || '-' }}</p>
          <div class="meta">
            <span>{{ item.username || '未知用户' }}</span>
            <span>{{ item.modelName || '本地规则' }}</span>
            <span>{{ formatTime(item.createdAt) }}</span>
          </div>
        </button>
      </div>

      <article v-if="selectedLog || detailError" class="detail panel">
        <div v-if="detailLoading" class="detail-loading">
          <Loader2 :size="18" class="spin" /> 详情加载中...
        </div>

        <div v-if="detailError && !detailLoading" class="detail-error">
          <p>详情加载失败</p>
          <button class="ghost-button" @click="loadDetail(selectedId!)">重试</button>
        </div>

        <div class="detail-head">
          <div>
            <span class="eyebrow">Workflow Detail</span>
            <h2>日志 #{{ selectedLog.id }}</h2>
          </div>
          <span :class="selectedLog.fallbackUsed ? 'status fallback' : 'status ai'">
            {{ selectedLog.fallbackUsed ? '规则回退' : 'AI 解析' }}
          </span>
        </div>

        <div v-if="selectedDetail?.workflowSteps?.length" class="flow-steps">
          <div
            v-for="(step, index) in selectedDetail.workflowSteps"
            :key="step.code"
            class="flow-step"
            :class="`step-${step.status}`"
          >
            <div class="step-mark">
              <CheckCircle2 :size="15" />
              <span>{{ index + 1 }}</span>
            </div>
            <div>
              <div class="step-title">
                <strong>{{ step.name }}</strong>
                <em>{{ stepStatusLabel(step.status) }}</em>
              </div>
              <p>{{ step.description }}</p>
            </div>
          </div>
        </div>

        <div class="summary-grid">
          <div><small>用户</small><strong>{{ selectedLog.username || '-' }}</strong></div>
          <div><small>会话</small><strong>{{ selectedLog.conversationId || '-' }}</strong></div>
          <div><small>模型</small><strong>{{ selectedLog.modelName || '本地规则' }}</strong></div>
          <div><small>时间</small><strong>{{ formatTime(selectedLog.createdAt) }}</strong></div>
        </div>

        <section class="detail-block">
          <h3>用户需求</h3>
          <p>{{ selectedLog.userRequirement || '-' }}</p>
        </section>

        <section class="detail-block">
          <h3>需求解析 JSON</h3>
          <pre>{{ selectedDetail ? formatParsedRequirement(selectedDetail.parsedRequirement) : formatJson(selectedLog.parsedRequirementJson) }}</pre>
        </section>

        <section class="detail-block">
          <h3>RAG 检索摘要（来源 / 分数）</h3>
          <div v-if="selectedDetail?.retrievedContexts?.length" class="context-list">
            <p v-for="(context, index) in selectedDetail.retrievedContexts" :key="index">{{ context }}</p>
          </div>
          <pre v-else>{{ selectedLog.retrievedContextSummary || '-' }}</pre>
        </section>

        <section class="detail-block">
          <h3>候选产品</h3>
          <div v-if="selectedDetail?.candidateProducts?.length" class="candidate-products">
            <RouterLink
              v-for="product in selectedDetail.candidateProducts"
              :key="product.id"
              class="candidate-product"
              :to="product.detailPath"
            >
              <div class="candidate-cover">
                <img v-if="product.coverUrl" :src="product.coverUrl" :alt="product.name" />
                <span v-else>{{ product.name.slice(0, 1) }}</span>
              </div>
              <div>
                <strong>{{ product.name }}</strong>
                <small>{{ product.brandName || '-' }} · {{ product.categoryName || '-' }}</small>
                <b>{{ formatPrice(product.officialPrice) }} · {{ product.score || '-' }} 分</b>
              </div>
              <ExternalLink :size="14" />
            </RouterLink>
          </div>
          <div v-else-if="candidateList(selectedCandidateIdsText).length" class="candidate-list">
            <RouterLink v-for="id in candidateList(selectedCandidateIdsText)" :key="id" :to="`/products/${id}`">
              产品 #{{ id }}
            </RouterLink>
          </div>
          <p v-else>-</p>
        </section>

        <section v-if="selectedLog.errorMessage" class="detail-block error-block">
          <h3>回退说明</h3>
          <p>{{ selectedLog.errorMessage }}</p>
        </section>
      </article>
    </div>
  </section>
</template>

<style scoped>
.admin-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin-bottom: 18px;
}

.eyebrow {
  color: var(--green);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
}

h1 {
  margin: 4px 0 0;
  font-size: 30px;
}

.primary-button,
.ghost-button {
  height: 38px;
  padding: 0 16px;
  border-radius: 8px;
  font-weight: 750;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
}

.primary-button {
  border: 0;
  background: var(--ink);
  color: #fff;
}

.ghost-button {
  border: 1px solid var(--line);
  background: #fff;
  color: var(--ink);
}

.primary-button:disabled,
.ghost-button:disabled {
  opacity: .55;
  cursor: not-allowed;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.filters {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 150px auto;
  gap: 10px;
  padding: 14px;
  margin-bottom: 16px;
}

.filters input,
.filters select {
  height: 40px;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 0 12px;
  background: #fff;
  font: inherit;
}

.empty {
  min-height: 220px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  color: var(--muted);
  text-align: center;
}

.empty p {
  margin: 0;
  color: var(--ink);
  font-weight: 800;
}

.workflow-layout {
  display: grid;
  grid-template-columns: minmax(320px, .86fr) minmax(0, 1.4fr);
  gap: 16px;
  align-items: start;
}

.log-list {
  display: grid;
  gap: 10px;
}

.log-card {
  width: 100%;
  border: 1px solid var(--line);
  text-align: left;
  padding: 14px;
  cursor: pointer;
  background: #fff;
}

.log-card.active {
  border-color: var(--green);
  background: #fbfdfc;
}

.log-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.log-card strong {
  color: var(--ink);
  font-size: 15px;
}

.log-card p {
  min-height: 44px;
  margin: 12px 0;
  color: var(--ink);
  font-size: 14px;
  line-height: 1.6;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  color: var(--muted);
  font-size: 12px;
}

.meta span {
  padding: 3px 7px;
  border-radius: 6px;
  background: var(--soft);
}

.status {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 800;
}

.status.ai {
  background: #eef5f2;
  color: var(--green);
}

.status.fallback {
  background: #fff4e5;
  color: #9a5a00;
}

.detail {
  position: relative;
  padding: 18px;
}

.detail-loading {
  position: absolute;
  top: 16px;
  right: 18px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  border: 1px solid var(--line);
  border-radius: 7px;
  background: rgba(255, 255, 255, .92);
  color: var(--muted);
  font-size: 12px;
  font-weight: 750;
}

.detail-error {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 1px solid #fecaca;
  border-radius: 8px;
  background: #fef2f2;
}

.detail-error p {
  margin: 0;
  color: #991b1b;
  font-size: 14px;
  font-weight: 650;
}

.ghost-button {
  height: 30px;
  padding: 0 12px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: #fff;
  color: var(--ink);
  font-size: 12px;
  font-weight: 650;
  cursor: pointer;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 16px;
}

.detail h2 {
  margin: 4px 0 0;
  font-size: 22px;
}

.flow-steps {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
  margin: 6px 0 18px;
}

.flow-step {
  min-height: 132px;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}

.flow-step.step-success {
  background: #fbfdfc;
  border-color: #d9e6df;
}

.flow-step.step-fallback {
  background: #fffaf1;
  border-color: #f0dfbd;
}

.flow-step.step-empty {
  background: #fafafa;
}

.step-mark {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--green);
  margin-bottom: 12px;
}

.step-mark span {
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--ink);
  color: #fff;
  font-size: 12px;
  font-weight: 850;
}

.step-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.step-title strong {
  color: var(--ink);
  font-size: 14px;
}

.step-title em {
  font-style: normal;
  color: var(--green);
  font-size: 12px;
  font-weight: 800;
}

.flow-step.step-fallback .step-title em {
  color: #9a5a00;
}

.flow-step p {
  margin: 0;
  color: var(--muted);
  font-size: 12px;
  line-height: 1.7;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 18px;
}

.summary-grid div {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 10px;
}

.summary-grid small {
  display: block;
  color: var(--muted);
  font-size: 12px;
  margin-bottom: 6px;
}

.summary-grid strong {
  color: var(--ink);
  font-size: 13px;
  word-break: break-word;
}

.detail-block {
  border-top: 1px solid var(--line);
  padding-top: 16px;
  margin-top: 16px;
}

.detail-block h3 {
  margin: 0 0 10px;
  font-size: 15px;
}

.detail-block p {
  margin: 0;
  color: var(--text);
  line-height: 1.8;
}

pre {
  max-height: 240px;
  overflow: auto;
  margin: 0;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
  padding: 12px;
  color: var(--ink);
  font: 12px/1.7 ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  white-space: pre-wrap;
  word-break: break-word;
}

.context-list {
  display: grid;
  gap: 8px;
}

.context-list p {
  margin: 0;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fafafa;
  padding: 10px 12px;
  color: var(--text);
  font-size: 13px;
}

.candidate-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.candidate-list a {
  height: 30px;
  display: inline-flex;
  align-items: center;
  padding: 0 10px;
  border: 1px solid var(--line);
  border-radius: 7px;
  color: var(--green);
  font-size: 13px;
  font-weight: 750;
}

.candidate-products {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.candidate-product {
  min-height: 86px;
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 10px;
  color: var(--ink);
  background: #fff;
}

.candidate-cover {
  width: 64px;
  height: 64px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 8px;
  background: var(--soft);
  color: var(--green);
  font-weight: 850;
}

.candidate-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.candidate-product strong,
.candidate-product small,
.candidate-product b {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.candidate-product strong {
  font-size: 14px;
}

.candidate-product small {
  margin-top: 4px;
  color: var(--muted);
  font-size: 12px;
}

.candidate-product b {
  margin-top: 6px;
  color: var(--green);
  font-size: 12px;
}

.error-block p {
  color: #9a5a00;
}

@media (max-width: 1100px) {
  .workflow-layout {
    grid-template-columns: 1fr;
  }

  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .flow-steps,
  .candidate-products {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .filters,
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
