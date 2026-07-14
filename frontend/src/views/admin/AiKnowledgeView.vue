<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Loader2, Plus, RefreshCw, Trash2, Puzzle } from '@lucide/vue';
import { useToastStore } from '@/stores/toastStore';
import {
  fetchAiKnowledgeList, createAiKnowledge, updateAiKnowledge, deleteAiKnowledge,
  rebuildAiKnowledgeChunks, fetchAiKnowledgeChunks, rebuildProductKnowledge,
  rebuildAiKnowledgeEmbeddings, searchAiKnowledgeEmbeddings,
} from '@/api/aiKnowledge';
import type { AiKnowledgeItem, AiKnowledgeChunkItem, AiKnowledgeSearchResult } from '@/types/aiKnowledge';

const knowledgeList = ref<AiKnowledgeItem[]>([]);
const loading = ref(true);
const loadError = ref(false);
const toastStore = useToastStore();
const keyword = ref('');
const typeFilter = ref('');

const showModal = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ title: '', content: '', knowledgeType: 'guide', tags: '', source: '', status: 1 });
const saving = ref(false);

const showChunks = ref(false);
const chunks = ref<AiKnowledgeChunkItem[]>([]);
const chunksKnowledgeTitle = ref('');

const rebuilding = ref(false);
const rebuildingEmbeddings = ref(false);
const vectorQuery = ref('');
const vectorSearching = ref(false);
const vectorResults = ref<AiKnowledgeSearchResult[]>([]);

onMounted(() => loadList());

async function loadList() {
  loading.value = true;
  loadError.value = false;
  try {
    knowledgeList.value = await fetchAiKnowledgeList({
      keyword: keyword.value || undefined,
      knowledgeType: typeFilter.value || undefined,
    });
  } catch {
    loadError.value = true;
    knowledgeList.value = [];
  } finally { loading.value = false; }
}

function openCreate() {
  editingId.value = null;
  form.value = { title: '', content: '', knowledgeType: 'guide', tags: '', source: '', status: 1 };
  showModal.value = true;
}

function openEdit(item: AiKnowledgeItem) {
  editingId.value = item.id;
  form.value = { title: item.title, content: item.content, knowledgeType: item.knowledgeType, tags: item.tags || '', source: item.source || '', status: item.status };
  showModal.value = true;
}

async function handleSave() {
  if (!form.value.title || !form.value.content) return;
  saving.value = true;
  try {
    if (editingId.value) await updateAiKnowledge(editingId.value, form.value as any);
    else await createAiKnowledge(form.value as any);
    toastStore.success(editingId.value ? '知识修改成功' : '知识新增成功');
    showModal.value = false;
    await loadList();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  } finally { saving.value = false; }
}

async function handleDelete(id: number) {
  if (!confirm('确定删除该知识及其切片吗？')) return;
  try {
    await deleteAiKnowledge(id);
    toastStore.success('知识已删除');
    await loadList();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '删除失败');
  }
}

async function handleRebuildChunks(id: number) {
  try {
    await rebuildAiKnowledgeChunks(id);
    toastStore.success('切片重建完成');
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '重建失败');
  }
}

async function handleViewChunks(item: AiKnowledgeItem) {
  try {
    chunksKnowledgeTitle.value = item.title;
    chunks.value = await fetchAiKnowledgeChunks(item.id);
    showChunks.value = true;
  } catch {
    toastStore.error('加载切片失败');
  }
}

async function handleRebuildAll() {
  if (!confirm('将从产品库重新生成所有产品知识并切片，是否继续？')) return;
  rebuilding.value = true;
  try {
    const count = await rebuildProductKnowledge();
    toastStore.success(`已生成 ${count} 条产品知识`);
    await loadList();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '生成失败');
  } finally { rebuilding.value = false; }
}

async function handleRebuildEmbeddings() {
  if (!confirm('将为当前启用的知识切片重建向量，是否继续？')) return;
  rebuildingEmbeddings.value = true;
  try {
    const count = await rebuildAiKnowledgeEmbeddings();
    toastStore.success(`已生成 ${count} 条知识向量`);
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '向量重建失败');
  } finally { rebuildingEmbeddings.value = false; }
}

async function handleVectorSearch() {
  if (!vectorQuery.value.trim()) return;
  vectorSearching.value = true;
  try {
    vectorResults.value = await searchAiKnowledgeEmbeddings({ query: vectorQuery.value.trim(), limit: 5 });
    if (vectorResults.value.length === 0) toastStore.info('暂无命中知识');
  } catch {
    toastStore.error('向量检索失败');
  } finally { vectorSearching.value = false; }
}

let searchTimer: ReturnType<typeof setTimeout> | null = null;
function onSearch() {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => loadList(), 300);
}
</script>

<template>
  <section>
    <div class="admin-head">
      <h1>AI 知识库</h1>
      <div class="head-actions">
        <button class="ghost-button" :disabled="rebuildingEmbeddings" @click="handleRebuildEmbeddings">
          <RefreshCw :size="14" :class="{ spin: rebuildingEmbeddings }" />
          {{ rebuildingEmbeddings ? '向量生成中...' : '重建向量' }}
        </button>
        <button class="ghost-button" :disabled="rebuilding" @click="handleRebuildAll">
          <RefreshCw :size="14" :class="{ spin: rebuilding }" />
          {{ rebuilding ? '生成中...' : '从产品库生成' }}
        </button>
        <button class="primary-button" @click="openCreate">新增知识</button>
      </div>
    </div>

    <div class="filter-bar">
      <input v-model="keyword" placeholder="搜索标题或内容" @input="onSearch" />
      <select v-model="typeFilter" @change="loadList">
        <option value="">全部类型</option>
        <option value="guide">指南</option>
        <option value="risk">风险</option>
        <option value="review">评测</option>
        <option value="rule">规则</option>
        <option value="product">产品</option>
      </select>
    </div>

    <div class="vector-panel panel">
      <div>
        <strong>向量检索测试</strong>
        <span>用于验证 RAG 命中的知识切片</span>
      </div>
      <form @submit.prevent="handleVectorSearch">
        <input v-model="vectorQuery" placeholder="例如：预算 4000，拍照旅行手机" />
        <button class="ghost-button" type="submit" :disabled="vectorSearching">
          {{ vectorSearching ? '检索中...' : '检索' }}
        </button>
      </form>
      <div v-if="vectorResults.length" class="vector-results">
        <div v-for="result in vectorResults" :key="result.chunkId" class="vector-result">
          <div>
            <strong>{{ result.title }}</strong>
            <div class="vector-meta">
              <span class="source-badge">{{ result.retrievalSource || 'VECTOR' }}</span>
              <span>score {{ result.score }}</span>
              <span v-if="result.embeddingModel">{{ result.embeddingModel }}</span>
            </div>
          </div>
          <p>{{ result.content.substring(0, 120) }}{{ result.content.length > 120 ? '...' : '' }}</p>
        </div>
      </div>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="loadError" message="加载知识库失败" @retry="loadList" />

    <EmptyState v-else-if="knowledgeList.length === 0"
      :icon="Puzzle"
      title="暂无知识库内容"
      description="可以从产品库生成产品知识"
      button-label="从产品库生成"
      @action="handleRebuildAll"
    />

    <div v-else class="knowledge-list">
      <div v-for="item in knowledgeList" :key="item.id" class="knowledge-item panel">
        <div class="item-head">
          <div>
            <h3>{{ item.title }}</h3>
            <span class="type-badge">{{ item.knowledgeType }}</span>
            <span v-if="item.status === 0" class="disabled-badge">已禁用</span>
          </div>
          <div class="item-actions">
            <button class="link-btn" @click="handleViewChunks(item)">切片</button>
            <button class="link-btn" @click="handleRebuildChunks(item.id)">重建切片</button>
            <button class="link-btn" @click="openEdit(item)">编辑</button>
            <button class="link-btn danger" @click="handleDelete(item.id)"><Trash2 :size="14" /></button>
          </div>
        </div>
        <p class="item-content">{{ item.content.substring(0, 150) }}{{ item.content.length > 150 ? '...' : '' }}</p>
        <div class="item-meta">
          <span v-if="item.tags">标签: {{ item.tags }}</span>
          <span v-if="item.source">来源: {{ item.source }}</span>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <Teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal modal-wide panel">
          <h2>{{ editingId ? '编辑知识' : '新增知识' }}</h2>
          <form @submit.prevent="handleSave">
            <label><span>标题 *</span><input v-model="form.title" required /></label>
            <label><span>类型</span>
              <select v-model="form.knowledgeType">
                <option value="guide">指南</option>
                <option value="risk">风险</option>
                <option value="review">评测</option>
                <option value="rule">规则</option>
                <option value="product">产品</option>
              </select>
            </label>
            <label><span>内容 *</span><textarea v-model="form.content" rows="6" required></textarea></label>
            <label><span>标签</span><input v-model="form.tags" placeholder="逗号分隔" /></label>
            <label><span>来源</span><input v-model="form.source" /></label>
            <div class="modal-actions">
              <button type="button" class="ghost-button" @click="showModal = false">取消</button>
              <button type="submit" class="primary-button" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- 切片查看弹窗 -->
    <Teleport to="body">
      <div v-if="showChunks" class="modal-overlay" @click.self="showChunks = false">
        <div class="modal modal-wide panel">
          <h2>切片 - {{ chunksKnowledgeTitle }}</h2>
          <div v-if="chunks.length === 0" class="empty">暂无切片</div>
          <div v-else class="chunk-list">
            <div v-for="c in chunks" :key="c.id" class="chunk-item">
              <div class="chunk-head">
                <span class="chunk-idx">#{{ c.chunkIndex }}</span>
                <span>{{ c.title }}</span>
                <span class="chunk-len">{{ c.charCount }} 字</span>
              </div>
              <p>{{ c.content.substring(0, 200) }}{{ c.content.length > 200 ? '...' : '' }}</p>
            </div>
          </div>
          <div class="modal-actions">
            <button class="ghost-button" @click="showChunks = false">关闭</button>
          </div>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.admin-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.head-actions { display: flex; gap: 10px; }
h1 { margin: 0; font-size: 28px; }
.primary-button { height: 38px; padding: 0 18px; border: 0; border-radius: 8px; background: var(--ink); color: #fff; font-weight: 700; font-size: 13px; cursor: pointer; }
.ghost-button { height: 38px; padding: 0 14px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--ink); font-weight: 650; font-size: 13px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; }
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.filter-bar input, .filter-bar select { height: 38px; border: 1px solid var(--line); border-radius: 8px; padding: 0 12px; font-size: 13px; }
.filter-bar input { flex: 1; }
.vector-panel { padding: 14px; display: grid; gap: 12px; margin-bottom: 16px; }
.vector-panel > div:first-child { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.vector-panel strong { font-size: 14px; }
.vector-panel span { color: var(--muted); font-size: 12px; }
.vector-panel form { display: flex; gap: 10px; }
.vector-panel input { flex: 1; height: 38px; border: 1px solid var(--line); border-radius: 8px; padding: 0 12px; font-size: 13px; }
.vector-results { display: grid; gap: 8px; }
.vector-result { border: 1px solid var(--line); border-radius: 6px; padding: 10px; }
.vector-result > div { display: flex; justify-content: space-between; gap: 12px; margin-bottom: 4px; }
.vector-meta { display: flex; align-items: center; justify-content: flex-end; gap: 8px; flex-wrap: wrap; }
.source-badge { color: var(--green) !important; font-weight: 800; }
.vector-result p { margin: 0; color: var(--text); line-height: 1.5; font-size: 12px; }
.empty { text-align: center; padding: 48px; color: var(--muted); }
.empty p { margin: 0 0 6px; color: var(--ink); font-size: 16px; font-weight: 700; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.knowledge-list { display: grid; gap: 12px; }
.knowledge-item { padding: 16px; }
.item-head { display: flex; justify-content: space-between; align-items: start; gap: 12px; margin-bottom: 8px; }
.item-head h3 { margin: 0; font-size: 16px; }
.type-badge { display: inline-block; padding: 2px 8px; border-radius: 4px; background: #eef5f2; color: var(--green); font-size: 11px; margin-left: 8px; }
.disabled-badge { display: inline-block; padding: 2px 8px; border-radius: 4px; background: #fef2f2; color: #b2472f; font-size: 11px; margin-left: 4px; }
.item-actions { display: flex; gap: 4px; flex-shrink: 0; }
.link-btn { background: none; border: 0; color: var(--green); cursor: pointer; font-size: 12px; font-weight: 650; padding: 4px 6px; }
.link-btn.danger { color: #b2472f; }
.item-content { margin: 0; color: var(--text); font-size: 13px; line-height: 1.6; }
.item-meta { display: flex; gap: 16px; margin-top: 8px; color: var(--muted); font-size: 12px; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: grid; place-items: center; z-index: 100; }
.modal { width: min(520px, 90vw); max-height: 85vh; overflow-y: auto; padding: 28px; }
.modal-wide { width: min(700px, 92vw); }
.modal h2 { margin: 0 0 14px; font-size: 20px; }
.modal form { display: grid; gap: 12px; }
.modal label { display: grid; gap: 5px; font-size: 13px; font-weight: 700; }
.modal input, .modal select, .modal textarea { height: 40px; border: 1px solid var(--line); border-radius: 8px; padding: 0 12px; font: inherit; }
.modal textarea { height: auto; padding: 10px 12px; resize: vertical; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 8px; }

.chunk-list { display: grid; gap: 10px; max-height: 400px; overflow-y: auto; }
.chunk-item { border: 1px solid var(--line); border-radius: 6px; padding: 10px; }
.chunk-head { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.chunk-idx { color: var(--green); font-weight: 800; font-size: 14px; }
.chunk-len { margin-left: auto; color: var(--muted); font-size: 12px; }
.chunk-item p { margin: 0; color: var(--text); font-size: 12px; line-height: 1.5; }
</style>
