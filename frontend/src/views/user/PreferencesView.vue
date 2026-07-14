<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Settings, Check } from '@lucide/vue';
import { fetchMyPreference, saveMyPreference } from '@/api/preference';
import { fetchBrands, fetchCategories } from '@/api/product';
import { useToastStore } from '@/stores/toastStore';
import LoadingState from '@/components/common/LoadingState.vue';
import ErrorState from '@/components/common/ErrorState.vue';
import type { Brand, Category } from '@/types/product';

const loading = ref(true);
const saving = ref(false);
const saved = ref(false);
const error = ref(false);
const toastStore = useToastStore();

const brands = ref<Brand[]>([]);
const categories = ref<Category[]>([]);

const minBudget = ref<number | null>(null);
const maxBudget = ref<number | null>(null);
const categoryId = ref<number | null>(null);
const selectedBrands = ref<number[]>([]);
const usageScenes = ref<string[]>([]);
const priorityTags = ref<string[]>([]);
const avoidTags = ref<string[]>([]);
const remark = ref('');

const usageSceneOptions = ['学习', '办公', '游戏', '摄影', '影音', '出差', '创作', '日常'];
const priorityTagOptions = ['性能', '续航', '屏幕', '影像', '便携', '性价比', '保值', '音质', '散热'];
const avoidTagOptions = ['机身重', '发热', '续航弱', '系统广告', '维修贵', '屏幕差', '拍照弱', '存储小'];

async function loadPreference() {
  loading.value = true;
  error.value = false;
  try {
    const [pref, b, c] = await Promise.all([
      fetchMyPreference(),
      fetchBrands(),
      fetchCategories(),
    ]);
    brands.value = b;
    categories.value = c;
    if (pref.id != null) {
      minBudget.value = pref.minBudget;
      maxBudget.value = pref.maxBudget;
      categoryId.value = pref.categoryId;
      selectedBrands.value = pref.brandIds ? pref.brandIds.split(',').map(Number).filter(n => !isNaN(n)) : [];
      usageScenes.value = pref.usageScenes ? pref.usageScenes.split(',').filter(Boolean) : [];
      priorityTags.value = pref.priorityTags ? pref.priorityTags.split(',').filter(Boolean) : [];
      avoidTags.value = pref.avoidTags ? pref.avoidTags.split(',').filter(Boolean) : [];
      remark.value = pref.remark || '';
    }
  } catch {
    error.value = true;
  } finally {
    loading.value = false;
  }
}

onMounted(loadPreference);

function toggleArray(arr: string[], value: string) {
  const idx = arr.indexOf(value);
  if (idx >= 0) arr.splice(idx, 1);
  else arr.push(value);
}

function toggleBrand(id: number) {
  const idx = selectedBrands.value.indexOf(id);
  if (idx >= 0) selectedBrands.value.splice(idx, 1);
  else selectedBrands.value.push(id);
}

async function handleSave() {
  if (maxBudget.value != null && minBudget.value != null && maxBudget.value < minBudget.value) {
    toastStore.error('最高预算不能低于最低预算');
    return;
  }
  saving.value = true;
  saved.value = false;
  try {
    await saveMyPreference({
      minBudget: minBudget.value,
      maxBudget: maxBudget.value,
      categoryId: categoryId.value,
      brandIds: selectedBrands.value.length > 0 ? selectedBrands.value.join(',') : null,
      usageScenes: usageScenes.value.length > 0 ? usageScenes.value.join(',') : null,
      priorityTags: priorityTags.value.length > 0 ? priorityTags.value.join(',') : null,
      avoidTags: avoidTags.value.length > 0 ? avoidTags.value.join(',') : null,
      remark: remark.value || null,
    });
    saved.value = true;
    toastStore.success('偏好设置已保存');
    setTimeout(() => { saved.value = false; }, 2000);
  } catch {
    toastStore.error('保存失败，请重试');
  } finally {
    saving.value = false;
  }
}
</script>

<template>
  <section class="pref-page">
    <div class="page-head">
      <div>
        <p>Preferences</p>
        <h1>偏好设置</h1>
      </div>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="error" message="加载偏好设置失败" @retry="loadPreference" />

    <div v-else class="pref-body">
      <!-- 预算 -->
      <div class="pref-card panel">
        <h2>预算区间</h2>
        <div class="budget-row">
          <label>
            <span>最低预算（元）</span>
            <input v-model.number="minBudget" type="number" step="100" placeholder="如 3000" />
          </label>
          <span class="budget-sep">-</span>
          <label>
            <span>最高预算（元）</span>
            <input v-model.number="maxBudget" type="number" step="100" placeholder="如 8000" />
          </label>
        </div>
      </div>

      <!-- 品类 -->
      <div class="pref-card panel">
        <h2>主要品类</h2>
        <select v-model="categoryId">
          <option :value="null">不限品类</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </div>

      <!-- 品牌 -->
      <div class="pref-card panel">
        <h2>偏好品牌</h2>
        <div class="chip-grid">
          <button v-for="b in brands" :key="b.id" class="chip" :class="{ active: selectedBrands.includes(b.id) }" @click="toggleBrand(b.id)">
            {{ b.name }}
          </button>
        </div>
      </div>

      <!-- 使用场景 -->
      <div class="pref-card panel">
        <h2>使用场景</h2>
        <div class="chip-grid">
          <button v-for="s in usageSceneOptions" :key="s" class="chip" :class="{ active: usageScenes.includes(s) }" @click="toggleArray(usageScenes, s)">
            {{ s }}
          </button>
        </div>
      </div>

      <!-- 关注重点 -->
      <div class="pref-card panel">
        <h2>关注重点</h2>
        <div class="chip-grid">
          <button v-for="t in priorityTagOptions" :key="t" class="chip" :class="{ active: priorityTags.includes(t) }" @click="toggleArray(priorityTags, t)">
            {{ t }}
          </button>
        </div>
      </div>

      <!-- 避坑点 -->
      <div class="pref-card panel">
        <h2>不想遇到</h2>
        <div class="chip-grid">
          <button v-for="a in avoidTagOptions" :key="a" class="chip" :class="{ active: avoidTags.includes(a) }" @click="toggleArray(avoidTags, a)">
            {{ a }}
          </button>
        </div>
      </div>

      <!-- 备注 -->
      <div class="pref-card panel">
        <h2>备注</h2>
        <textarea v-model="remark" rows="3" placeholder="其他选购需求，例如：主要写代码、偶尔剪视频"></textarea>
      </div>

      <!-- 保存 -->
      <div class="save-row">
        <span v-if="saved" class="save-ok"><Check :size="16" /> 已保存</span>
        <button class="primary-button" :disabled="saving" @click="handleSave">
          <Loader2 v-if="saving" :size="16" class="spin" />
          {{ saving ? '保存中...' : '保存偏好' }}
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.pref-page {
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

.pref-body {
  max-width: 720px;
}

.pref-card {
  padding: 22px;
  margin-bottom: 16px;
}

.pref-card h2 {
  margin: 0 0 14px;
  font-size: 17px;
}

.pref-card select,
.pref-card input,
.pref-card textarea {
  width: 100%;
  height: 42px;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 0 12px;
  font: inherit;
  color: var(--ink);
}

.pref-card textarea {
  height: auto;
  padding: 10px 12px;
  resize: vertical;
}

.budget-row {
  display: flex;
  align-items: end;
  gap: 12px;
}

.budget-row label {
  flex: 1;
  display: grid;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
}

.budget-sep {
  padding-bottom: 10px;
  color: var(--muted);
  font-size: 18px;
}

.chip-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  height: 34px;
  padding: 0 14px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--muted);
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  transition: all .15s ease;
}

.chip:hover {
  border-color: var(--green);
  color: var(--green);
}

.chip.active {
  background: var(--green);
  border-color: var(--green);
  color: #fff;
}

.save-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 0 40px;
}

.primary-button {
  height: 42px;
  padding: 0 24px;
  border: 0;
  border-radius: 8px;
  background: var(--ink);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.primary-button:disabled {
  opacity: .6;
  cursor: not-allowed;
}

.save-ok {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--green);
  font-size: 14px;
  font-weight: 700;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
