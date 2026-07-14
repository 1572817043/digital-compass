<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { CheckCircle2, Loader2, Plus, Power, RefreshCw, Settings2, Star, TestTube, Trash2 } from '@lucide/vue';
import { useToastStore } from '@/stores/toastStore';
import {
  fetchAiProviders, createAiProvider, updateAiProvider, deleteAiProvider,
  setDefaultAiProvider, testAiProvider, fetchAiProviderModels, fetchSavedAiProviderModels,
} from '@/api/aiProvider';
import type { AiModelItem, AiProviderConfigItem, AiTestResult } from '@/types/aiProvider';

type ProviderForm = {
  providerCode: string;
  providerName: string;
  protocolType: string;
  baseUrl: string;
  apiKey: string;
  chatModel: string;
  embeddingModel: string;
  temperature: number;
  maxTokens: number;
  millionContext: boolean;
  timeoutSeconds: number;
  enabled: boolean;
  defaultProvider: boolean;
  remark: string;
};

const providers = ref<AiProviderConfigItem[]>([]);
const loading = ref(true);
const loadError = ref(false);
const toastStore = useToastStore();
const showModal = ref(false);
const editingId = ref<number | null>(null);
const saving = ref(false);
const testingId = ref<number | null>(null);
const testResults = ref<Record<number, AiTestResult>>({});
const modelLoading = ref(false);
const modelError = ref('');
const fetchedModels = ref<AiModelItem[]>([]);

const protocolOptions = [
  { value: 'openai-compatible', label: 'OpenAI 兼容' },
  { value: 'anthropic-compatible', label: 'Anthropic 兼容（预留）', disabled: true },
  { value: 'ollama', label: 'Ollama 本地（预留）', disabled: true },
];

const providerPresets = [
  {
    key: 'mimo-main',
    name: '小米 Mimo',
    protocolType: 'openai-compatible',
    baseUrl: 'https://api.xiaomimimo.com/v1',
    chatModel: 'mimo-v2.5-pro',
    embeddingModel: '',
  },
  {
    key: 'deepseek-main',
    name: 'DeepSeek',
    protocolType: 'openai-compatible',
    baseUrl: 'https://api.deepseek.com',
    chatModel: 'deepseek-chat',
    embeddingModel: '',
  },
  {
    key: 'openrouter-main',
    name: 'OpenRouter',
    protocolType: 'openai-compatible',
    baseUrl: 'https://openrouter.ai/api/v1',
    chatModel: '',
    embeddingModel: '',
  },
  {
    key: 'custom-openai',
    name: '自定义兼容接口',
    protocolType: 'openai-compatible',
    baseUrl: '',
    chatModel: '',
    embeddingModel: '',
  },
];

const form = ref<ProviderForm>(blankForm());
const defaultProvider = computed(() => providers.value.find(item => item.defaultProvider) || null);

onMounted(async () => {
  await loadProviders();
});

function blankForm(): ProviderForm {
  return {
    providerCode: 'custom-openai',
    providerName: '',
    protocolType: 'openai-compatible',
    baseUrl: '',
    apiKey: '',
    chatModel: '',
    embeddingModel: '',
    temperature: 0.7,
    maxTokens: 2048,
    millionContext: false,
    timeoutSeconds: 30,
    enabled: true,
    defaultProvider: false,
    remark: '',
  };
}

async function loadProviders() {
  loading.value = true;
  loadError.value = false;
  try {
    providers.value = await fetchAiProviders();
  } catch {
    loadError.value = true;
    providers.value = [];
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editingId.value = null;
  form.value = blankForm();
  fetchedModels.value = [];
  modelError.value = '';
  showModal.value = true;
}

function openEdit(p: AiProviderConfigItem) {
  editingId.value = p.id;
  form.value = {
    providerCode: p.providerCode,
    providerName: p.providerName,
    protocolType: p.protocolType || 'openai-compatible',
    baseUrl: p.baseUrl,
    apiKey: '',
    chatModel: p.chatModel || '',
    embeddingModel: p.embeddingModel || '',
    temperature: p.temperature ?? 0.7,
    maxTokens: p.maxTokens ?? 2048,
    millionContext: p.millionContext,
    timeoutSeconds: p.timeoutSeconds ?? 30,
    enabled: p.enabled,
    defaultProvider: p.defaultProvider,
    remark: p.remark || '',
  };
  fetchedModels.value = [];
  modelError.value = '';
  showModal.value = true;
}

function applyPreset(preset: typeof providerPresets[number]) {
  form.value = {
    ...form.value,
    providerCode: editingId.value ? form.value.providerCode : preset.key,
    providerName: preset.name,
    protocolType: preset.protocolType,
    baseUrl: preset.baseUrl,
    chatModel: preset.chatModel,
    embeddingModel: preset.embeddingModel,
  };
  fetchedModels.value = [];
  modelError.value = '';
}

async function handleFetchModels() {
  if (!form.value.baseUrl) {
    modelError.value = '请先填写 Endpoint';
    return;
  }
  if (!editingId.value && !form.value.apiKey) {
    modelError.value = '新增配置需要先填写 API Key';
    return;
  }
  modelLoading.value = true;
  modelError.value = '';
  fetchedModels.value = [];
  try {
    const payload = {
      ...form.value,
      apiKey: form.value.apiKey || undefined,
      embeddingModel: form.value.embeddingModel || undefined,
    };
    fetchedModels.value = editingId.value && !form.value.apiKey
      ? await fetchSavedAiProviderModels(editingId.value)
      : await fetchAiProviderModels(payload);
    if (fetchedModels.value.length === 0) {
      modelError.value = '接口返回了空模型列表';
      toastStore.info('接口返回了空模型列表');
    } else {
      toastStore.success('模型列表获取成功');
    }
  } catch {
    modelError.value = '获取模型失败，请检查 Endpoint、API Key 和协议';
    toastStore.error('获取模型失败，请检查 Endpoint、API Key 和协议');
  } finally {
    modelLoading.value = false;
  }
}

function useChatModel(model: AiModelItem) {
  form.value.chatModel = model.id;
}

async function handleSave() {
  if (!form.value.providerCode || !form.value.providerName || !form.value.baseUrl) return;
  saving.value = true;
  try {
    const payload = {
      ...form.value,
      apiKey: form.value.apiKey || undefined,
      embeddingModel: form.value.embeddingModel || undefined,
    };
    if (editingId.value) {
      await updateAiProvider(editingId.value, payload);
    } else {
      await createAiProvider(payload);
    }
    toastStore.success(editingId.value ? '配置修改成功' : '配置新增成功');
    showModal.value = false;
    await loadProviders();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  } finally {
    saving.value = false;
  }
}

async function handleDelete(id: number) {
  if (!confirm('确定删除该 AI 配置吗？')) return;
  try {
    await deleteAiProvider(id);
    toastStore.success('配置已删除');
    await loadProviders();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '删除失败');
  }
}

async function handleSetDefault(id: number) {
  try {
    await setDefaultAiProvider(id);
    toastStore.success('已设为默认');
    await loadProviders();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  }
}

async function handleTest(id: number) {
  testingId.value = id;
  testResults.value = { ...testResults.value, [id]: { success: false, model: null, responsePreview: null, errorMessage: '测试中...' } };
  try {
    const result = await testAiProvider(id);
    testResults.value = { ...testResults.value, [id]: result };
    if (result.success) toastStore.success('连接测试成功');
    else toastStore.error('连接测试失败');
  } catch {
    testResults.value = { ...testResults.value, [id]: { success: false, model: null, responsePreview: null, errorMessage: '测试请求失败' } };
    toastStore.error('连接测试失败');
  } finally {
    testingId.value = null;
  }
}

function protocolLabel(protocolType: string | null | undefined) {
  return protocolOptions.find(item => item.value === protocolType)?.label || 'OpenAI 兼容';
}
</script>

<template>
  <section>
    <div class="admin-head">
      <div>
        <span class="eyebrow">AI Provider</span>
        <h1>AI 接入配置</h1>
      </div>
      <button class="primary-button" @click="openCreate"><Plus :size="15" /> 新增配置</button>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="loadError" message="加载 AI 配置失败" @retry="loadProviders" />

    <template v-else>
      <div class="switch-panel panel">
        <div class="switch-main">
          <div class="switch-icon"><Power :size="18" /></div>
          <div>
            <small>当前默认模型</small>
            <h2>{{ defaultProvider?.providerName || '未设置' }}</h2>
            <p>{{ defaultProvider ? `${defaultProvider.chatModel || '-'} · ${protocolLabel(defaultProvider.protocolType)}` : '选购助手会回退到规则解析和本地向量检索' }}</p>
          </div>
        </div>
        <button v-if="defaultProvider" class="ghost-button" @click="openEdit(defaultProvider)"><Settings2 :size="14" /> 配置</button>
      </div>

      <EmptyState v-if="providers.length === 0"
        title="暂无 AI 供应商配置"
        description="新增配置后选购助手可以调用真实模型"
        button-label="新增配置"
        @action="openCreate"
      />

      <div v-else class="provider-list">
        <div v-for="p in providers" :key="p.id" class="provider-item panel" :class="{ active: p.defaultProvider, disabled: !p.enabled }">
          <div class="provider-head">
            <div>
              <div class="title-row">
                <h3>{{ p.providerName }}</h3>
                <span v-if="p.defaultProvider" class="default-badge"><CheckCircle2 :size="13" /> 默认</span>
                <span v-if="!p.enabled" class="disabled-badge">停用</span>
              </div>
              <div class="sub-row">
                <span>{{ p.providerCode }}</span>
                <span>{{ protocolLabel(p.protocolType) }}</span>
              </div>
            </div>
            <div class="provider-actions">
              <button class="link-btn" @click="handleTest(p.id)" :disabled="testingId === p.id">
                <TestTube :size="14" /> {{ testingId === p.id ? '测试中' : '测试' }}
              </button>
              <button v-if="!p.defaultProvider" class="link-btn" @click="handleSetDefault(p.id)"><Star :size="14" /> 设为默认</button>
              <button class="link-btn" @click="openEdit(p)">编辑</button>
              <button class="link-btn danger" @click="handleDelete(p.id)"><Trash2 :size="14" /> 删除</button>
            </div>
          </div>

            <div class="provider-info">
              <div><small>Endpoint</small><span>{{ p.baseUrl }}</span></div>
              <div><small>对话模型</small><span>{{ p.chatModel || '-' }}</span></div>
              <div><small>向量模型</small><span>{{ p.embeddingModel || '本地 fallback' }}</span></div>
              <div><small>上下文</small><span>{{ p.millionContext ? '1M' : '普通' }}</span></div>
              <div><small>API Key</small><span>{{ p.maskedApiKey || '未配置' }}</span></div>
            </div>

          <div v-if="testResults[p.id]" class="test-result" :class="{ ok: testResults[p.id].success, fail: !testResults[p.id].success }">
            <span v-if="testResults[p.id].success">连接成功：{{ testResults[p.id].responsePreview }}</span>
            <span v-else>连接失败：{{ testResults[p.id].errorMessage }}</span>
          </div>
        </div>
      </div>
    </template>

    <Teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal panel">
          <div class="modal-head">
            <div>
              <span class="eyebrow">Provider Profile</span>
              <h2>{{ editingId ? '编辑 AI 配置' : '新增 AI 配置' }}</h2>
            </div>
          </div>

          <div class="preset-row">
            <button v-for="preset in providerPresets" :key="preset.key" type="button" @click="applyPreset(preset)">
              {{ preset.name }}
            </button>
          </div>

          <form @submit.prevent="handleSave">
            <div class="form-grid">
              <label><span>配置标识 *</span><input v-model="form.providerCode" required placeholder="如 mimo-main" /></label>
              <label><span>显示名称 *</span><input v-model="form.providerName" required placeholder="如 小米 Mimo" /></label>
              <label>
                <span>协议 *</span>
                <select v-model="form.protocolType">
                  <option v-for="opt in protocolOptions" :key="opt.value" :value="opt.value" :disabled="opt.disabled">{{ opt.label }}</option>
                </select>
              </label>
              <label><span>Endpoint *</span><input v-model="form.baseUrl" required placeholder="https://api.xiaomimimo.com/v1" /></label>
              <label><span>API Key</span><input v-model="form.apiKey" type="password" :placeholder="editingId ? '留空则保留原密钥' : '输入 API Key'" /></label>
              <label class="model-field">
                <span>对话模型</span>
                <div class="model-input-row">
                  <input v-model="form.chatModel" placeholder="mimo-v2.5-pro" />
                  <button type="button" class="mini-button" @click="handleFetchModels" :disabled="modelLoading">
                    <RefreshCw :size="13" :class="{ spin: modelLoading }" /> {{ modelLoading ? '获取中' : '获取模型' }}
                  </button>
                </div>
              </label>
              <label><span>向量模型</span><input v-model="form.embeddingModel" placeholder="没有可留空，使用本地 fallback" /></label>
              <label><span>备注</span><input v-model="form.remark" placeholder="用途、额度或账号说明" /></label>
            </div>

            <div v-if="fetchedModels.length || modelError" class="model-result">
              <span v-if="modelError" class="model-error">{{ modelError }}</span>
              <template v-else>
                <small>点击模型名称填入对话模型</small>
                <div class="model-list">
                  <button v-for="model in fetchedModels" :key="model.id" type="button" @click="useChatModel(model)" :class="{ active: form.chatModel === model.id }">
                    {{ model.id }}
                    <em v-if="model.ownedBy">{{ model.ownedBy }}</em>
                  </button>
                </div>
              </template>
            </div>

            <div class="form-row">
              <label><span>Temperature</span><input v-model.number="form.temperature" type="number" step="0.1" min="0" max="2" /></label>
              <label><span>Max Tokens</span><input v-model.number="form.maxTokens" type="number" /></label>
              <label><span>超时(秒)</span><input v-model.number="form.timeoutSeconds" type="number" /></label>
            </div>

            <div class="check-row">
              <label><input type="checkbox" v-model="form.millionContext" /> 支持 1M 上下文</label>
              <label><input type="checkbox" v-model="form.enabled" /> 启用</label>
              <label><input type="checkbox" v-model="form.defaultProvider" /> 设为默认</label>
            </div>

            <div class="modal-actions">
              <button type="button" class="ghost-button" @click="showModal = false">取消</button>
              <button type="submit" class="primary-button" :disabled="saving">{{ saving ? '保存中...' : '保存配置' }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.admin-head { display: flex; justify-content: space-between; align-items: center; gap: 18px; margin-bottom: 22px; }
.eyebrow { color: var(--green); font-size: 12px; font-weight: 800; letter-spacing: 0; }
h1 { margin: 4px 0 0; font-size: 28px; }
.primary-button, .ghost-button { height: 38px; padding: 0 16px; border-radius: 8px; font-weight: 750; font-size: 13px; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; gap: 7px; }
.primary-button { border: 0; background: var(--ink); color: #fff; }
.ghost-button { border: 1px solid var(--line); background: #fff; color: var(--ink); }
.primary-button:disabled { opacity: .55; cursor: not-allowed; }
.empty { text-align: center; padding: 48px; color: var(--muted); }
.empty p { margin: 0 0 6px; color: var(--ink); font-size: 16px; font-weight: 750; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.switch-panel { display: flex; justify-content: space-between; align-items: center; gap: 18px; padding: 18px; margin-bottom: 16px; }
.switch-main { display: flex; align-items: center; gap: 14px; min-width: 0; }
.switch-icon { width: 38px; height: 38px; border-radius: 8px; background: var(--ink); color: #fff; display: grid; place-items: center; flex-shrink: 0; }
.switch-panel small { color: var(--muted); font-size: 12px; font-weight: 700; }
.switch-panel h2 { margin: 2px 0 4px; font-size: 18px; }
.switch-panel p { margin: 0; color: var(--muted); font-size: 13px; }

.provider-list { display: grid; gap: 12px; }
.provider-item { padding: 18px; border-left: 3px solid transparent; }
.provider-item.active { border-left-color: var(--green); background: #fbfdfc; }
.provider-item.disabled { opacity: .72; }
.provider-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 14px; margin-bottom: 14px; }
.title-row { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.title-row h3 { margin: 0; font-size: 17px; }
.sub-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 6px; color: var(--muted); font-size: 12px; }
.sub-row span { padding: 3px 8px; background: var(--soft); border-radius: 6px; }
.default-badge, .disabled-badge { display: inline-flex; align-items: center; gap: 4px; padding: 3px 8px; border-radius: 6px; font-size: 12px; font-weight: 750; }
.default-badge { background: #eef5f2; color: var(--green); }
.disabled-badge { background: #fef2f2; color: #b2472f; }
.provider-actions { display: flex; flex-wrap: wrap; justify-content: flex-end; gap: 6px; flex-shrink: 0; }
.link-btn { background: none; border: 0; color: var(--green); cursor: pointer; font-size: 12px; font-weight: 700; padding: 4px 8px; display: inline-flex; align-items: center; gap: 4px; }
.link-btn.danger { color: #b2472f; }
.link-btn:disabled { opacity: .5; cursor: not-allowed; }
.provider-info { display: grid; grid-template-columns: 1.4fr 1fr 1fr .7fr 1fr; gap: 12px; }
.provider-info small { display: block; color: var(--muted); font-size: 11px; margin-bottom: 3px; }
.provider-info span { font-size: 13px; word-break: break-all; }
.test-result { margin-top: 12px; padding: 9px 10px; border-radius: 7px; font-size: 13px; }
.test-result.ok { background: #eef5f2; color: #166534; }
.test-result.fail { background: #fef2f2; color: #991b1b; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.3); display: grid; place-items: center; z-index: 100; }
.modal { width: min(720px, 92vw); max-height: 86vh; overflow-y: auto; padding: 26px; }
.modal-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 16px; }
.modal h2 { margin: 4px 0 0; font-size: 20px; }
.preset-row { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.preset-row button { height: 34px; padding: 0 12px; border: 1px solid var(--line); border-radius: 8px; background: #fff; font: inherit; font-size: 13px; font-weight: 700; cursor: pointer; }
.preset-row button:hover { border-color: var(--green); color: var(--green); }
.modal form { display: grid; gap: 14px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.modal label { display: grid; gap: 6px; font-size: 13px; font-weight: 750; }
.modal input, .modal select { height: 40px; border: 1px solid var(--line); border-radius: 8px; padding: 0 12px; font: inherit; min-width: 0; }
.model-field { min-width: 0; }
.model-input-row { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 8px; }
.mini-button { height: 40px; padding: 0 11px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--green); font: inherit; font-size: 12px; font-weight: 750; cursor: pointer; display: inline-flex; align-items: center; gap: 5px; white-space: nowrap; }
.mini-button:disabled { opacity: .55; cursor: not-allowed; }
.model-result { padding: 12px; border: 1px solid var(--line); border-radius: 8px; background: #fafafa; }
.model-result small { display: block; color: var(--muted); font-size: 12px; font-weight: 700; margin-bottom: 8px; }
.model-error { color: #991b1b; font-size: 13px; font-weight: 700; }
.model-list { display: flex; flex-wrap: wrap; gap: 8px; }
.model-list button { min-height: 34px; padding: 6px 10px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--ink); font: inherit; font-size: 12px; font-weight: 750; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; }
.model-list button.active { border-color: var(--green); color: var(--green); background: #eef5f2; }
.model-list em { color: var(--muted); font-style: normal; font-weight: 600; }
.form-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.check-row { display: flex; flex-wrap: wrap; gap: 16px; }
.check-row label { display: flex; align-items: center; gap: 8px; font-weight: 700; }
.check-row input { width: auto; height: auto; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 4px; }

@media (max-width: 900px) {
  .provider-head, .switch-panel { align-items: stretch; flex-direction: column; }
  .provider-actions { justify-content: flex-start; }
  .provider-info { grid-template-columns: 1fr 1fr; }
}

@media (max-width: 640px) {
  .admin-head { align-items: flex-start; flex-direction: column; }
  .provider-info, .form-grid, .form-row { grid-template-columns: 1fr; }
}
</style>
