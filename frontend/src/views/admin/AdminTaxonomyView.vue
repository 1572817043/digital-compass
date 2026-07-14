<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Plus } from '@lucide/vue';
import {
  fetchAdminCategories, createCategory, updateCategory, updateCategoryStatus,
  fetchAdminBrands, createBrand, updateBrand,
} from '@/api/admin';
import { useToastStore } from '@/stores/toastStore';
import type { CategoryItem, BrandItem } from '@/api/admin';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const activeTab = ref<'categories' | 'brands'>('categories');
const toastStore = useToastStore();

const categories = ref<CategoryItem[]>([]);
const brands = ref<BrandItem[]>([]);
const loading = ref(true);
const error = ref(false);

const showCatModal = ref(false);
const editingCatId = ref<number | null>(null);
const catForm = ref({ code: '', name: '', description: '', sortOrder: 0, enabled: 1 });

const showBrandModal = ref(false);
const editingBrandId = ref<number | null>(null);
const brandForm = ref({ name: '', sortOrder: 0 });

onMounted(() => loadData());

async function loadData() {
  loading.value = true;
  error.value = false;
  try {
    if (activeTab.value === 'categories') categories.value = await fetchAdminCategories();
    else brands.value = await fetchAdminBrands();
  } catch {
    error.value = true;
  } finally { loading.value = false; }
}

function switchTab(tab: typeof activeTab.value) {
  activeTab.value = tab;
  loadData();
}

// Categories
function openCatForm(cat?: CategoryItem) {
  if (cat) { editingCatId.value = cat.id; catForm.value = { code: cat.code, name: cat.name, description: cat.description || '', sortOrder: cat.sortOrder, enabled: cat.enabled }; }
  else { editingCatId.value = null; catForm.value = { code: '', name: '', description: '', sortOrder: 0, enabled: 1 }; }
  showCatModal.value = true;
}

async function handleSaveCat() {
  if (!catForm.value.code || !catForm.value.name) return;
  try {
    if (editingCatId.value) await updateCategory(editingCatId.value, catForm.value as any);
    else await createCategory(catForm.value as any);
    toastStore.success(editingCatId.value ? '分类修改成功' : '分类新增成功');
    showCatModal.value = false;
    await loadData();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  }
}

async function handleToggleCatStatus(cat: CategoryItem) {
  try {
    await updateCategoryStatus(cat.id, cat.enabled === 1 ? 0 : 1);
    toastStore.success(cat.enabled === 1 ? '已禁用' : '已启用');
    await loadData();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  }
}

// Brands
function openBrandForm(brand?: BrandItem) {
  if (brand) { editingBrandId.value = brand.id; brandForm.value = { name: brand.name, sortOrder: brand.sortOrder }; }
  else { editingBrandId.value = null; brandForm.value = { name: '', sortOrder: 0 }; }
  showBrandModal.value = true;
}

async function handleSaveBrand() {
  if (!brandForm.value.name) return;
  try {
    if (editingBrandId.value) await updateBrand(editingBrandId.value, brandForm.value);
    else await createBrand(brandForm.value);
    toastStore.success(editingBrandId.value ? '品牌修改成功' : '品牌新增成功');
    showBrandModal.value = false;
    await loadData();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  }
}
</script>

<template>
  <section>
    <h1>分类品牌管理</h1>

    <div class="tabs">
      <button :class="{ active: activeTab === 'categories' }" @click="switchTab('categories')">分类管理</button>
      <button :class="{ active: activeTab === 'brands' }" @click="switchTab('brands')">品牌管理</button>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="error" message="加载数据失败" @retry="loadData" />

    <!-- 分类 Tab -->
    <div v-else-if="activeTab === 'categories'" class="tab-content">
      <div class="tab-head"><button class="primary-button" @click="openCatForm()"><Plus :size="14" /> 新增分类</button></div>
      <div class="table-wrap panel">
        <table>
          <thead><tr><th>ID</th><th>编码</th><th>名称</th><th>描述</th><th>排序</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="c in categories" :key="c.id">
              <td>{{ c.id }}</td><td>{{ c.code }}</td><td>{{ c.name }}</td><td>{{ c.description || '-' }}</td><td>{{ c.sortOrder }}</td>
              <td><span :class="c.enabled ? 'status-active' : 'status-disabled'">{{ c.enabled ? '启用' : '禁用' }}</span></td>
              <td class="actions">
                <button class="link-btn" @click="openCatForm(c)">编辑</button>
                <button class="link-btn" @click="handleToggleCatStatus(c)">{{ c.enabled ? '禁用' : '启用' }}</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 品牌 Tab -->
    <div v-else class="tab-content">
      <div class="tab-head"><button class="primary-button" @click="openBrandForm()"><Plus :size="14" /> 新增品牌</button></div>
      <div class="table-wrap panel">
        <table>
          <thead><tr><th>ID</th><th>名称</th><th>排序</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="b in brands" :key="b.id">
              <td>{{ b.id }}</td><td>{{ b.name }}</td><td>{{ b.sortOrder }}</td>
              <td><button class="link-btn" @click="openBrandForm(b)">编辑</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 分类弹窗 -->
    <Teleport to="body">
      <div v-if="showCatModal" class="modal-overlay" @click.self="showCatModal = false">
        <div class="modal panel">
          <h2>{{ editingCatId ? '编辑分类' : '新增分类' }}</h2>
          <form @submit.prevent="handleSaveCat">
            <label><span>编码 *</span><input v-model="catForm.code" required :disabled="!!editingCatId" /></label>
            <label><span>名称 *</span><input v-model="catForm.name" required /></label>
            <label><span>描述</span><input v-model="catForm.description" /></label>
            <label><span>排序</span><input v-model.number="catForm.sortOrder" type="number" /></label>
            <div class="modal-actions">
              <button type="button" class="ghost-button" @click="showCatModal = false">取消</button>
              <button type="submit" class="primary-button">保存</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- 品牌弹窗 -->
    <Teleport to="body">
      <div v-if="showBrandModal" class="modal-overlay" @click.self="showBrandModal = false">
        <div class="modal panel">
          <h2>{{ editingBrandId ? '编辑品牌' : '新增品牌' }}</h2>
          <form @submit.prevent="handleSaveBrand">
            <label><span>名称 *</span><input v-model="brandForm.name" required /></label>
            <label><span>排序</span><input v-model.number="brandForm.sortOrder" type="number" /></label>
            <div class="modal-actions">
              <button type="button" class="ghost-button" @click="showBrandModal = false">取消</button>
              <button type="submit" class="primary-button">保存</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
h1 { margin: 0 0 22px; font-size: 28px; }
.tabs { display: flex; gap: 0; border-bottom: 2px solid var(--line); margin-bottom: 18px; }
.tabs button { flex: 1; height: 40px; border: 0; background: none; color: var(--muted); font-weight: 650; font-size: 14px; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; }
.tabs button.active { color: var(--ink); border-bottom-color: var(--ink); }
.tab-head { display: flex; justify-content: flex-end; margin-bottom: 14px; }
.primary-button { height: 36px; padding: 0 16px; border: 0; border-radius: 8px; background: var(--ink); color: #fff; font-weight: 700; font-size: 13px; cursor: pointer; display: inline-flex; align-items: center; gap: 6px; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 12px 14px; border-bottom: 1px solid var(--line); font-size: 14px; }
th { color: var(--muted); font-weight: 700; font-size: 12px; }
.status-active { color: var(--green); font-weight: 700; }
.status-disabled { color: #b2472f; font-weight: 700; }
.actions { display: flex; gap: 8px; }
.link-btn { background: none; border: 0; color: var(--green); cursor: pointer; font-size: 13px; font-weight: 650; padding: 0; }
.empty { text-align: center; padding: 32px; color: var(--muted); }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: grid; place-items: center; z-index: 100; }
.modal { width: min(420px, 90vw); padding: 28px; }
.modal h2 { margin: 0 0 14px; font-size: 20px; }
.modal form { display: grid; gap: 12px; }
.modal label { display: grid; gap: 5px; font-size: 13px; font-weight: 700; }
.modal input { height: 40px; border: 1px solid var(--line); border-radius: 8px; padding: 0 12px; font: inherit; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 8px; }
.ghost-button { height: 38px; padding: 0 14px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--ink); font-weight: 650; font-size: 13px; cursor: pointer; }
</style>
