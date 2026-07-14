<script setup lang="ts">
import { onMounted, ref } from 'vue';
import {
  createProduct, updateProduct, deleteProduct,
  bindProductImage, deleteProductImage, uploadImage,
  fetchProducts, fetchBrands, fetchCategories,
  fetchAdminProducts, fetchAdminProductDetail, updateProductStatus,
  fetchAdminProductSpecs, createProductSpec, updateProductSpec, deleteProductSpec,
  fetchAdminProductPrices, createProductPrice, updateProductPrice, deleteProductPrice,
  fetchAdminProductLinks, createProductLink, updateProductLink, deleteProductLink,
  fetchAdminProductTags, createProductTag, updateProductTag, deleteProductTag,
} from '@/api/product';
import type {
  Brand, Category, ProductListItem, ProductDetail,
  ProductSpecItem, ProductPriceItem, ProductLinkItem, ProductTagItem,
  ImageAuditResult,
} from '@/types/product';
import { useToastStore } from '@/stores/toastStore';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const toastStore = useToastStore();
const products = ref<ProductListItem[]>([]);
const brands = ref<Brand[]>([]);
const categories = ref<Category[]>([]);
const loading = ref(false);
const loadError = ref(false);
const statusFilter = ref<number | null>(null);

// 产品表单
const showProductModal = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ name: '', model: '', summary: '', categoryId: null as number | null, brandId: null as number | null, officialPrice: null as number | null, score: 0, status: 1 });
const saving = ref(false);

// 资料维护弹窗
const showDetailModal = ref(false);
const detailProductId = ref<number | null>(null);
const detailProduct = ref<ProductDetail | null>(null);
const activeTab = ref<'images' | 'specs' | 'prices' | 'links' | 'tags'>('images');

// 参数
const specs = ref<ProductSpecItem[]>([]);
const specForm = ref({ specGroup: '', specName: '', specValue: '', sortOrder: 0 });
const editingSpecId = ref<number | null>(null);
const showSpecForm = ref(false);

// 价格
const prices = ref<ProductPriceItem[]>([]);
const priceForm = ref({ priceType: 'used', platform: '', minPrice: null as number | null, maxPrice: null as number | null, avgPrice: null as number | null, sampleCount: 0, referenceDate: new Date().toISOString().slice(0, 10), sourceType: 'manual', remark: '' });
const editingPriceId = ref<number | null>(null);
const showPriceForm = ref(false);

// 链接
const links = ref<ProductLinkItem[]>([]);
const linkForm = ref({ platform: '', linkType: 'official', title: '', url: '', sortOrder: 0, enabled: 1 });
const editingLinkId = ref<number | null>(null);
const showLinkForm = ref(false);

// 标签
const tagTypeOptions = [
  { value: 'selling_point', label: '核心优点' },
  { value: 'weakness', label: '主要短板' },
  { value: 'suitable', label: '适合人群' },
  { value: 'unsuitable', label: '不适合人群' },
  { value: 'scene', label: '使用场景' },
];
const tags = ref<ProductTagItem[]>([]);
const tagForm = ref({ tagType: 'selling_point', tagName: '', tagValue: '', sortOrder: 0 });
const editingTagId = ref<number | null>(null);
const showTagForm = ref(false);

// 上传
const uploading = ref(false);
const uploadImageType = ref<'GALLERY' | 'MAIN'>('GALLERY');
const lastImageAudit = ref<ImageAuditResult | null>(null);

function trimValue(value: string | null | undefined) {
  return value?.trim() ?? '';
}

function hasNumber(value: number | null | undefined) {
  return typeof value === 'number' && Number.isFinite(value);
}

function isNegativeNumber(value: number | null | undefined) {
  return typeof value === 'number' && Number.isFinite(value) && value < 0;
}

function validateProductForm() {
  if (!trimValue(form.value.name)) {
    toastStore.error('请输入产品名称');
    return false;
  }
  if (!form.value.categoryId) {
    toastStore.error('请选择产品分类');
    return false;
  }
  if (!form.value.brandId) {
    toastStore.error('请选择产品品牌');
    return false;
  }
  if (isNegativeNumber(form.value.officialPrice)) {
    toastStore.error('官方价不能小于 0');
    return false;
  }
  if (!hasNumber(form.value.score) || form.value.score < 0 || form.value.score > 100) {
    toastStore.error('评分需要填写 0 到 100 之间的数字');
    return false;
  }
  return true;
}

function validatePriceForm() {
  const { platform, minPrice, maxPrice, avgPrice, sampleCount, referenceDate } = priceForm.value;
  if (!trimValue(platform)) {
    toastStore.error('请输入价格来源平台');
    return false;
  }
  if (!hasNumber(minPrice) && !hasNumber(maxPrice) && !hasNumber(avgPrice)) {
    toastStore.error('至少填写一个价格值');
    return false;
  }
  if (isNegativeNumber(minPrice) || isNegativeNumber(maxPrice) || isNegativeNumber(avgPrice)) {
    toastStore.error('价格不能小于 0');
    return false;
  }
  if (hasNumber(minPrice) && hasNumber(maxPrice) && minPrice! > maxPrice!) {
    toastStore.error('最低价不能大于最高价');
    return false;
  }
  if (!hasNumber(sampleCount) || sampleCount < 0) {
    toastStore.error('样本数不能小于 0');
    return false;
  }
  if (!trimValue(referenceDate)) {
    toastStore.error('请选择参考日期');
    return false;
  }
  return true;
}

function validateLinkForm() {
  if (!trimValue(linkForm.value.platform)) {
    toastStore.error('请输入购买平台');
    return false;
  }
  if (!trimValue(linkForm.value.title)) {
    toastStore.error('请输入链接标题');
    return false;
  }
  try {
    const url = new URL(trimValue(linkForm.value.url));
    if (!['http:', 'https:'].includes(url.protocol)) throw new Error('invalid protocol');
  } catch {
    toastStore.error('请输入正确的购买链接 URL');
    return false;
  }
  return true;
}

async function loadProducts() {
  loading.value = true;
  loadError.value = false;
  try {
    products.value = await fetchAdminProducts({ status: statusFilter.value ?? undefined });
  } catch {
    loadError.value = true;
    toastStore.error('加载产品列表失败');
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  loadError.value = false;
  try {
    const [b, c] = await Promise.all([fetchBrands(), fetchCategories()]);
    brands.value = b;
    categories.value = c;
    products.value = await fetchAdminProducts({ status: statusFilter.value ?? undefined });
  } catch {
    loadError.value = true;
    toastStore.error('加载产品管理数据失败');
  } finally {
    loading.value = false;
  }
});

// ========== 产品 CRUD ==========
function openCreate() {
  editingId.value = null;
  form.value = { name: '', model: '', summary: '', categoryId: null, brandId: null, officialPrice: null, score: 0, status: 1 };
  showProductModal.value = true;
}

function openEdit(p: ProductListItem) {
  editingId.value = p.id;
  form.value = { name: p.name, model: p.model ?? '', summary: p.summary ?? '', categoryId: p.categoryId, brandId: p.brandId, officialPrice: p.officialPrice, score: p.score, status: 1 };
  showProductModal.value = true;
}

async function handleToggleStatus(id: number, currentStatus: number) {
  const newStatus = currentStatus === 1 ? 0 : 1;
  const action = newStatus === 1 ? '启用' : '禁用';
  if (!confirm(`确定${action}该产品吗？`)) return;
  try {
    await updateProductStatus(id, newStatus);
    toastStore.success(`产品已${action}`);
    await loadProducts();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  }
}

async function handleSave() {
  if (!validateProductForm()) return;
  saving.value = true;
  try {
    const categoryId = form.value.categoryId!;
    const brandId = form.value.brandId!;
    const p = { name: trimValue(form.value.name), model: trimValue(form.value.model) || undefined, summary: trimValue(form.value.summary) || undefined, categoryId, brandId, officialPrice: form.value.officialPrice ?? undefined, score: form.value.score, status: form.value.status };
    if (editingId.value) await updateProduct(editingId.value, p);
    else await createProduct(p);
    toastStore.success(editingId.value ? '产品修改成功' : '产品新增成功');
    showProductModal.value = false;
    await loadProducts();
  } catch (e: any) { toastStore.error(e.response?.data?.message || '操作失败'); } finally { saving.value = false; }
}

async function handleDelete(id: number, name: string) {
  if (!confirm(`确定删除产品「${name}」吗？`)) return;
  try { await deleteProduct(id); toastStore.success('产品已删除'); await loadProducts(); } catch (e: any) { toastStore.error(e.response?.data?.message || '删除失败'); }
}

// ========== 资料维护弹窗 ==========
async function openDetail(p: ProductListItem) {
  detailProductId.value = p.id;
  try {
    detailProduct.value = await fetchAdminProductDetail(p.id);
    activeTab.value = 'images';
    showDetailModal.value = true;
    await loadTabData();
  } catch { toastStore.error('加载产品资料失败'); }
}

async function loadTabData() {
  if (!detailProductId.value) return;
  const id = detailProductId.value;
  try {
    if (activeTab.value === 'specs') specs.value = await fetchAdminProductSpecs(id);
    else if (activeTab.value === 'prices') prices.value = await fetchAdminProductPrices(id);
    else if (activeTab.value === 'links') links.value = await fetchAdminProductLinks(id);
    else if (activeTab.value === 'tags') tags.value = await fetchAdminProductTags(id);
    else if (activeTab.value === 'images') detailProduct.value = await fetchAdminProductDetail(id);
  } catch { toastStore.error('加载资料失败'); }
}

function switchTab(tab: typeof activeTab.value) {
  activeTab.value = tab;
  loadTabData();
}

// ========== 图片 ==========
async function handleUpload(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file || !detailProductId.value) return;
  uploading.value = true;
  lastImageAudit.value = null;
  try {
    const result = await uploadImage(file, detailProduct.value?.name, uploadImageType.value);
    await bindProductImage(detailProductId.value, { imageUrl: result.url, imageType: uploadImageType.value, sortOrder: 0 });
    lastImageAudit.value = result.audit;
    if (result.audit?.status === 'PASS') {
      toastStore.success('图片上传成功，规范审核通过');
    } else {
      toastStore.info('图片已上传，请查看规范审核建议');
    }
    await loadTabData();
  } catch (e: any) { toastStore.error(e.response?.data?.message || '图片上传失败'); } finally { uploading.value = false; input.value = ''; }
}

async function handleSetMain(imageId: number) {
  if (!detailProductId.value || !detailProduct.value) return;
  const img = detailProduct.value.images.find(i => i.id === imageId);
  if (!img) return;
  try { await bindProductImage(detailProductId.value, { imageUrl: img.imageUrl, imageType: 'MAIN', sortOrder: 0 }); toastStore.success('已设为主图'); await loadTabData(); } catch (e: any) { toastStore.error(e.response?.data?.message || '操作失败'); }
}

async function handleDeleteImage(imageId: number) {
  if (!detailProductId.value) return;
  try { await deleteProductImage(detailProductId.value, imageId); toastStore.success('图片已删除'); await loadTabData(); } catch (e: any) { toastStore.error(e.response?.data?.message || '删除失败'); }
}

// ========== 参数 ==========
function openSpecForm(spec?: ProductSpecItem) {
  if (spec) { editingSpecId.value = spec.id; specForm.value = { specGroup: spec.group, specName: spec.name, specValue: spec.value, sortOrder: spec.sortOrder }; }
  else { editingSpecId.value = null; specForm.value = { specGroup: '', specName: '', specValue: '', sortOrder: 0 }; }
  showSpecForm.value = true;
}

async function handleSaveSpec() {
  if (!detailProductId.value) return;
  if (!trimValue(specForm.value.specGroup) || !trimValue(specForm.value.specName) || !trimValue(specForm.value.specValue)) {
    toastStore.error('请完整填写参数组、参数名和参数值');
    return;
  }
  try {
    if (editingSpecId.value) await updateProductSpec(detailProductId.value, editingSpecId.value, specForm.value);
    else await createProductSpec(detailProductId.value, specForm.value);
    toastStore.success(editingSpecId.value ? '参数修改成功' : '参数新增成功');
    showSpecForm.value = false;
    await loadTabData();
  } catch (e: any) { toastStore.error(e.response?.data?.message || '操作失败'); }
}

async function handleDeleteSpec(specId: number) {
  if (!detailProductId.value || !confirm('确定删除该参数吗？')) return;
  try { await deleteProductSpec(detailProductId.value, specId); toastStore.success('参数已删除'); await loadTabData(); } catch (e: any) { toastStore.error(e.response?.data?.message || '删除失败'); }
}

// ========== 价格 ==========
function openPriceForm(price?: ProductPriceItem) {
  if (price) {
    editingPriceId.value = price.id;
    priceForm.value = {
      priceType: price.priceType,
      platform: price.platform,
      minPrice: price.minPrice,
      maxPrice: price.maxPrice,
      avgPrice: price.avgPrice,
      sampleCount: price.sampleCount ?? 0,
      referenceDate: price.referenceDate ?? new Date().toISOString().slice(0, 10),
      sourceType: price.sourceType || 'manual',
      remark: price.remark ?? '',
    };
  } else {
    editingPriceId.value = null;
    priceForm.value = { priceType: 'used', platform: '', minPrice: null, maxPrice: null, avgPrice: null, sampleCount: 0, referenceDate: new Date().toISOString().slice(0, 10), sourceType: 'manual', remark: '' };
  }
  showPriceForm.value = true;
}

async function handleSavePrice() {
  if (!detailProductId.value || !validatePriceForm()) return;
  try {
    if (editingPriceId.value) await updateProductPrice(detailProductId.value, editingPriceId.value, priceForm.value);
    else await createProductPrice(detailProductId.value, priceForm.value);
    toastStore.success(editingPriceId.value ? '价格修改成功' : '价格新增成功');
    showPriceForm.value = false;
    await loadTabData();
  } catch (e: any) { toastStore.error(e.response?.data?.message || '操作失败'); }
}

async function handleDeletePrice(priceId: number) {
  if (!detailProductId.value || !confirm('确定删除该价格参考吗？')) return;
  try { await deleteProductPrice(detailProductId.value, priceId); toastStore.success('价格已删除'); await loadTabData(); } catch (e: any) { toastStore.error(e.response?.data?.message || '删除失败'); }
}

// ========== 链接 ==========
function openLinkForm(link?: ProductLinkItem) {
  if (link) {
    editingLinkId.value = link.id;
    linkForm.value = { platform: link.platform, linkType: link.linkType, title: link.title, url: link.url, sortOrder: link.sortOrder, enabled: 1 };
  } else {
    editingLinkId.value = null;
    linkForm.value = { platform: '', linkType: 'official', title: '', url: '', sortOrder: 0, enabled: 1 };
  }
  showLinkForm.value = true;
}

async function handleSaveLink() {
  if (!detailProductId.value || !validateLinkForm()) return;
  try {
    if (editingLinkId.value) await updateProductLink(detailProductId.value, editingLinkId.value, linkForm.value);
    else await createProductLink(detailProductId.value, linkForm.value);
    toastStore.success(editingLinkId.value ? '链接修改成功' : '链接新增成功');
    showLinkForm.value = false;
    await loadTabData();
  } catch (e: any) { toastStore.error(e.response?.data?.message || '操作失败'); }
}

async function handleDeleteLink(linkId: number) {
  if (!detailProductId.value || !confirm('确定删除该购买链接吗？')) return;
  try { await deleteProductLink(detailProductId.value, linkId); toastStore.success('链接已删除'); await loadTabData(); } catch (e: any) { toastStore.error(e.response?.data?.message || '删除失败'); }
}

// ========== 标签 ==========
function tagTypeLabel(type: string) {
  return tagTypeOptions.find(item => item.value === type)?.label ?? type;
}

function openTagForm(tag?: ProductTagItem) {
  if (tag) {
    editingTagId.value = tag.id;
    tagForm.value = {
      tagType: tag.tagType,
      tagName: tag.tagName,
      tagValue: tag.tagValue ?? '',
      sortOrder: tag.sortOrder,
    };
  } else {
    editingTagId.value = null;
    tagForm.value = { tagType: 'selling_point', tagName: '', tagValue: '', sortOrder: 0 };
  }
  showTagForm.value = true;
}

async function handleSaveTag() {
  if (!detailProductId.value) return;
  if (!tagForm.value.tagType || !trimValue(tagForm.value.tagName)) {
    toastStore.error('请选择标签类型并填写标签名');
    return;
  }
  const payload = {
    tagType: tagForm.value.tagType,
    tagName: tagForm.value.tagName,
    tagValue: tagForm.value.tagValue || undefined,
    sortOrder: tagForm.value.sortOrder,
  };
  try {
    if (editingTagId.value) await updateProductTag(detailProductId.value, editingTagId.value, payload);
    else await createProductTag(detailProductId.value, payload);
    toastStore.success(editingTagId.value ? '标签修改成功' : '标签新增成功');
    showTagForm.value = false;
    await loadTabData();
  } catch (e: any) { toastStore.error(e.response?.data?.message || '操作失败'); }
}

async function handleDeleteTag(tagId: number) {
  if (!detailProductId.value || !confirm('确定删除该标签吗？')) return;
  try { await deleteProductTag(detailProductId.value, tagId); toastStore.success('标签已删除'); await loadTabData(); } catch (e: any) { toastStore.error(e.response?.data?.message || '删除失败'); }
}
</script>

<template>
  <section>
    <div class="admin-head">
      <h1>产品管理</h1>
      <div class="head-actions">
        <select v-model="statusFilter" @change="loadProducts" class="status-filter">
          <option :value="null">全部状态</option>
          <option :value="1">启用</option>
          <option :value="0">禁用</option>
        </select>
        <button class="primary-button" @click="openCreate">新增产品</button>
      </div>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="loadError" message="加载产品列表失败" @retry="loadProducts" />

    <EmptyState v-else-if="products.length === 0"
      title="暂无产品数据"
      description="新增产品后会出现在这里"
      button-label="新增产品"
      @action="openCreate"
    />

    <div v-else class="table-wrap panel">
      <table>
        <thead><tr><th>ID</th><th>名称</th><th>品牌</th><th>分类</th><th>官方价</th><th>评分</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="p in products" :key="p.id">
            <td>{{ p.id }}</td><td>{{ p.name }}</td><td>{{ p.brandName }}</td><td>{{ p.categoryName }}</td>
            <td>{{ p.officialPrice != null ? `¥${p.officialPrice}` : '-' }}</td><td>{{ p.score }}</td>
            <td>
              <span :class="p.status === 1 ? 'status-active' : 'status-disabled'">
                {{ p.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="actions">
              <button class="link-btn" @click="openEdit(p)">编辑</button>
              <button class="link-btn" :class="{ danger: p.status === 1 }" @click="handleToggleStatus(p.id, p.status)">
                {{ p.status === 1 ? '禁用' : '启用' }}
              </button>
              <button class="link-btn" @click="openDetail(p)">资料</button>
              <button class="link-btn danger" @click="handleDelete(p.id, p.name)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 产品新增/编辑弹窗 -->
    <Teleport to="body">
      <div v-if="showProductModal" class="modal-overlay" @click.self="showProductModal = false">
        <div class="modal panel">
          <h2>{{ editingId ? '编辑产品' : '新增产品' }}</h2>
          <form @submit.prevent="handleSave">
            <label><span>产品名称 *</span><input v-model="form.name" required /></label>
            <label><span>型号</span><input v-model="form.model" /></label>
            <label><span>分类 *</span><select v-model="form.categoryId" required><option :value="null" disabled>请选择</option><option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option></select></label>
            <label><span>品牌 *</span><select v-model="form.brandId" required><option :value="null" disabled>请选择</option><option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option></select></label>
            <label><span>官方价</span><input v-model.number="form.officialPrice" type="number" step="0.01" /></label>
            <label><span>简介</span><textarea v-model="form.summary" rows="3"></textarea></label>
            <label><span>评分</span><input v-model.number="form.score" type="number" /></label>
            <div class="modal-actions">
              <button type="button" class="ghost-button" @click="showProductModal = false">取消</button>
              <button type="submit" class="primary-button" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- 资料维护弹窗 -->
    <Teleport to="body">
      <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
        <div class="modal modal-wide panel">
          <h2>资料维护 - {{ detailProduct?.name }}</h2>
          <div class="tabs">
            <button :class="{ active: activeTab === 'images' }" @click="switchTab('images')">图片</button>
            <button :class="{ active: activeTab === 'specs' }" @click="switchTab('specs')">参数</button>
            <button :class="{ active: activeTab === 'prices' }" @click="switchTab('prices')">价格</button>
            <button :class="{ active: activeTab === 'links' }" @click="switchTab('links')">购买链接</button>
            <button :class="{ active: activeTab === 'tags' }" @click="switchTab('tags')">标签</button>
          </div>

          <!-- 图片 Tab -->
          <div v-if="activeTab === 'images'" class="tab-content">
            <div v-if="detailProduct" class="image-list">
              <div v-for="img in detailProduct.images" :key="img.id" class="image-item">
                <img :src="img.imageUrl" :alt="detailProduct?.name" />
                <span class="badge" :class="{ main: img.imageType === 'MAIN' }">{{ img.imageType === 'MAIN' ? '主图' : '图集' }}</span>
                <div class="item-actions">
                  <button v-if="img.imageType !== 'MAIN'" class="link-btn" @click="handleSetMain(img.id)">设为主图</button>
                  <button class="link-btn danger" @click="handleDeleteImage(img.id)">删除</button>
                </div>
              </div>
              <EmptyState v-if="detailProduct.images.length === 0" title="暂无图片" description="上传产品图片后会显示在这里" />
            </div>
            <div class="upload-area">
              <select v-model="uploadImageType" :disabled="uploading">
                <option value="GALLERY">图集图片</option>
                <option value="MAIN">主图候选</option>
              </select>
              <label class="upload-btn primary-button">{{ uploading ? '上传中...' : '上传图片' }}<input type="file" accept="image/jpeg,image/png,image/webp" @change="handleUpload" :disabled="uploading" hidden /></label>
              <span class="hint">支持 jpg、png、webp，最大 5MB；主图建议 1000x1000 或 1200x900，产品居中，无水印和评测奖章</span>
            </div>
            <div v-if="lastImageAudit" class="audit-panel" :class="{ pass: lastImageAudit.status === 'PASS' }">
              <div class="audit-head">
                <strong>{{ lastImageAudit.status === 'PASS' ? '规范审核通过' : '建议人工复核' }}</strong>
                <span v-if="lastImageAudit.width && lastImageAudit.height">
                  {{ lastImageAudit.width }}x{{ lastImageAudit.height }}
                  <template v-if="lastImageAudit.aspectRatio"> / 比例 {{ lastImageAudit.aspectRatio }}</template>
                </span>
              </div>
              <p>{{ lastImageAudit.summary }}</p>
              <div v-if="lastImageAudit.warnings.length" class="audit-list warning-list">
                <small>风险提示</small>
                <span v-for="item in lastImageAudit.warnings" :key="item">{{ item }}</span>
              </div>
              <div v-if="lastImageAudit.suggestions.length" class="audit-list">
                <small>处理建议</small>
                <span v-for="item in lastImageAudit.suggestions" :key="item">{{ item }}</span>
              </div>
            </div>
          </div>

          <!-- 参数 Tab -->
          <div v-if="activeTab === 'specs'" class="tab-content">
            <div class="tab-head"><button class="primary-button" @click="openSpecForm()">新增参数</button></div>
            <div v-if="showSpecForm" class="inline-form">
              <label><span>参数组</span><input v-model="specForm.specGroup" placeholder="如 性能、屏幕" /></label>
              <label><span>参数名</span><input v-model="specForm.specName" placeholder="如 处理器、尺寸" /></label>
              <label><span>参数值</span><input v-model="specForm.specValue" placeholder="如 第三代骁龙 8" /></label>
              <label><span>排序</span><input v-model.number="specForm.sortOrder" type="number" /></label>
              <div class="form-actions">
                <button class="ghost-button" @click="showSpecForm = false">取消</button>
                <button class="primary-button" @click="handleSaveSpec">保存</button>
              </div>
            </div>
            <table class="meta-table">
              <thead><tr><th>参数组</th><th>参数名</th><th>参数值</th><th>排序</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="s in specs" :key="s.id">
                  <td>{{ s.group }}</td><td>{{ s.name }}</td><td>{{ s.value }}</td><td>{{ s.sortOrder }}</td>
                  <td class="actions">
                    <button class="link-btn" @click="openSpecForm(s)">编辑</button>
                    <button class="link-btn danger" @click="handleDeleteSpec(s.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <EmptyState v-if="specs.length === 0 && !showSpecForm" title="暂无参数" description="新增核心参数后会显示在这里" />
          </div>

          <!-- 价格 Tab -->
          <div v-if="activeTab === 'prices'" class="tab-content">
            <div class="tab-head"><button class="primary-button" @click="openPriceForm()">新增价格</button></div>
            <div v-if="showPriceForm" class="inline-form">
              <label><span>类型</span><select v-model="priceForm.priceType"><option value="official">官方价</option><option value="used">二手参考</option><option value="channel">渠道参考</option></select></label>
              <label><span>平台</span><input v-model="priceForm.platform" placeholder="如 京东、二手行情估算" /></label>
              <label><span>最低价</span><input v-model.number="priceForm.minPrice" type="number" step="0.01" /></label>
              <label><span>最高价</span><input v-model.number="priceForm.maxPrice" type="number" step="0.01" /></label>
              <label><span>均价</span><input v-model.number="priceForm.avgPrice" type="number" step="0.01" /></label>
              <label><span>样本数</span><input v-model.number="priceForm.sampleCount" type="number" /></label>
              <label><span>参考日期</span><input v-model="priceForm.referenceDate" type="date" /></label>
              <label><span>来源</span><select v-model="priceForm.sourceType"><option value="manual">手动维护</option><option value="import">数据导入</option><option value="crawl">采集整理</option></select></label>
              <label><span>备注</span><input v-model="priceForm.remark" /></label>
              <div class="form-actions">
                <button class="ghost-button" @click="showPriceForm = false">取消</button>
                <button class="primary-button" @click="handleSavePrice">保存</button>
              </div>
            </div>
            <table class="meta-table">
              <thead><tr><th>类型</th><th>平台</th><th>最低价</th><th>最高价</th><th>均价</th><th>样本</th><th>日期</th><th>来源</th><th>备注</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="pr in prices" :key="pr.id">
                  <td>{{ pr.priceType === 'official' ? '官方价' : pr.priceType === 'used' ? '二手参考' : '渠道参考' }}</td><td>{{ pr.platform }}</td>
                  <td>{{ pr.minPrice != null ? `¥${pr.minPrice}` : '-' }}</td>
                  <td>{{ pr.maxPrice != null ? `¥${pr.maxPrice}` : '-' }}</td>
                  <td>{{ pr.avgPrice != null ? `¥${pr.avgPrice}` : '-' }}</td>
                  <td>{{ pr.sampleCount ?? 0 }}</td>
                  <td>{{ pr.referenceDate || '-' }}</td>
                  <td>{{ pr.sourceType === 'import' ? '数据导入' : pr.sourceType === 'crawl' ? '采集整理' : '手动维护' }}</td>
                  <td>{{ pr.remark || '-' }}</td>
                  <td class="actions">
                    <button class="link-btn" @click="openPriceForm(pr)">编辑</button>
                    <button class="link-btn danger" @click="handleDeletePrice(pr.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <EmptyState v-if="prices.length === 0 && !showPriceForm" title="暂无价格参考" description="新增官方价、二手参考或渠道参考" />
          </div>

          <!-- 购买链接 Tab -->
          <div v-if="activeTab === 'links'" class="tab-content">
            <div class="tab-head"><button class="primary-button" @click="openLinkForm()">新增链接</button></div>
            <div v-if="showLinkForm" class="inline-form">
              <label><span>平台</span><input v-model="linkForm.platform" placeholder="如 官网、京东" /></label>
              <label><span>类型</span><select v-model="linkForm.linkType"><option value="official">官网</option><option value="store">商店</option><option value="used_search">二手搜索</option></select></label>
              <label><span>标题</span><input v-model="linkForm.title" /></label>
              <label><span>URL</span><input v-model="linkForm.url" placeholder="https://..." /></label>
              <label><span>排序</span><input v-model.number="linkForm.sortOrder" type="number" /></label>
              <div class="form-actions">
                <button class="ghost-button" @click="showLinkForm = false">取消</button>
                <button class="primary-button" @click="handleSaveLink">保存</button>
              </div>
            </div>
            <table class="meta-table">
              <thead><tr><th>平台</th><th>类型</th><th>标题</th><th>URL</th><th>排序</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="lk in links" :key="lk.id">
                  <td>{{ lk.platform }}</td><td>{{ lk.linkType }}</td><td>{{ lk.title }}</td>
                  <td><a :href="lk.url" target="_blank" rel="noopener" class="url-link">{{ lk.url.length > 30 ? lk.url.slice(0, 30) + '...' : lk.url }}</a></td>
                  <td>{{ lk.sortOrder }}</td>
                  <td class="actions">
                    <button class="link-btn" @click="openLinkForm(lk)">编辑</button>
                    <button class="link-btn danger" @click="handleDeleteLink(lk.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <EmptyState v-if="links.length === 0 && !showLinkForm" title="暂无购买链接" description="新增官网或电商链接" />
          </div>

          <!-- 标签 Tab -->
          <div v-if="activeTab === 'tags'" class="tab-content">
            <div class="tab-head"><button class="primary-button" @click="openTagForm()">新增标签</button></div>
            <div v-if="showTagForm" class="inline-form">
              <label>
                <span>类型</span>
                <select v-model="tagForm.tagType">
                  <option v-for="item in tagTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                </select>
              </label>
              <label><span>标签名</span><input v-model="tagForm.tagName" placeholder="如 影像强、机身重、学生党" /></label>
              <label class="wide-field"><span>说明</span><input v-model="tagForm.tagValue" placeholder="可填写一句选购解释" /></label>
              <label><span>排序</span><input v-model.number="tagForm.sortOrder" type="number" /></label>
              <div class="form-actions">
                <button class="ghost-button" @click="showTagForm = false">取消</button>
                <button class="primary-button" @click="handleSaveTag">保存</button>
              </div>
            </div>
            <table class="meta-table">
              <thead><tr><th>类型</th><th>标签名</th><th>说明</th><th>排序</th><th>操作</th></tr></thead>
              <tbody>
                <tr v-for="tag in tags" :key="tag.id">
                  <td>{{ tagTypeLabel(tag.tagType) }}</td><td>{{ tag.tagName }}</td><td>{{ tag.tagValue || '-' }}</td><td>{{ tag.sortOrder }}</td>
                  <td class="actions">
                    <button class="link-btn" @click="openTagForm(tag)">编辑</button>
                    <button class="link-btn danger" @click="handleDeleteTag(tag.id)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <EmptyState v-if="tags.length === 0 && !showTagForm" title="暂无标签" description="新增卖点、短板、适合人群等标签" />
          </div>

          <div class="modal-actions" style="margin-top: 16px;">
            <button class="ghost-button" @click="showDetailModal = false">关闭</button>
          </div>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.admin-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px; }
.head-actions { display: flex; gap: 10px; align-items: center; }
.status-filter { height: 36px; border: 1px solid var(--line); border-radius: 8px; padding: 0 10px; font-size: 13px; }
.status-active { color: var(--green); font-weight: 700; }
.status-disabled { color: #b2472f; font-weight: 700; }
h1 { margin: 0; font-size: 28px; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 12px 14px; border-bottom: 1px solid var(--line); font-size: 14px; }
th { color: var(--muted); font-weight: 700; font-size: 12px; text-transform: uppercase; }
.actions { display: flex; gap: 8px; }
.link-btn { background: none; border: 0; color: var(--green); cursor: pointer; font-size: 13px; font-weight: 650; padding: 0; }
.link-btn.danger { color: #b2472f; }
.empty { text-align: center; padding: 32px; color: var(--muted); }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: grid; place-items: center; z-index: 100; }
.modal { width: min(520px, 90vw); max-height: 85vh; overflow-y: auto; padding: 28px; }
.modal-wide { width: min(780px, 92vw); }
.modal h2 { margin: 0 0 16px; font-size: 22px; }
.modal form, .modal label { display: grid; gap: 6px; }
.modal form { gap: 14px; }
.modal label { font-size: 13px; font-weight: 700; color: var(--ink); }
.modal input, .modal select, .modal textarea { height: 40px; border: 1px solid var(--line); border-radius: 8px; padding: 0 12px; font: inherit; color: var(--ink); }
.modal textarea { height: auto; padding: 8px 12px; resize: vertical; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 8px; }
.primary-button { height: 38px; padding: 0 18px; border: 0; border-radius: 8px; background: var(--ink); color: #fff; font-weight: 700; font-size: 13px; cursor: pointer; }
.ghost-button { height: 38px; padding: 0 14px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--ink); font-weight: 650; font-size: 13px; cursor: pointer; }

/* Tabs */
.tabs { display: flex; gap: 0; border-bottom: 2px solid var(--line); margin-bottom: 16px; }
.tabs button { flex: 1; height: 40px; border: 0; background: none; color: var(--muted); font-weight: 650; font-size: 14px; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; }
.tabs button.active { color: var(--ink); border-bottom-color: var(--ink); }
.tab-content { min-height: 200px; }
.tab-head { display: flex; justify-content: flex-end; margin-bottom: 12px; }

/* Inline form */
.inline-form { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding: 14px; border: 1px solid var(--line); border-radius: 8px; background: #fafaf8; margin-bottom: 14px; }
.inline-form label { font-size: 12px; }
.inline-form input, .inline-form select { height: 36px; font-size: 13px; }
.wide-field { grid-column: 1 / -1; }
.form-actions { grid-column: 1 / -1; display: flex; justify-content: flex-end; gap: 8px; }

/* Meta table */
.meta-table { width: 100%; border-collapse: collapse; }
.meta-table th, .meta-table td { text-align: left; padding: 10px 12px; border-bottom: 1px solid var(--line); font-size: 13px; }
.meta-table th { color: var(--muted); font-weight: 700; font-size: 12px; }
.url-link { color: var(--green); text-decoration: none; font-size: 12px; }
.url-link:hover { text-decoration: underline; }

/* Image list */
.image-list { display: grid; gap: 8px; margin-bottom: 12px; }
.image-item { display: grid; grid-template-columns: 60px auto 1fr auto; gap: 10px; align-items: center; padding: 6px; border: 1px solid var(--line); border-radius: 6px; }
.image-item img { width: 60px; height: 45px; object-fit: contain; border-radius: 4px; background: #f4f3ef; }
.badge { display: inline-block; padding: 2px 8px; border-radius: 999px; background: var(--soft); color: var(--muted); font-size: 11px; font-weight: 650; }
.badge.main { background: #eef5f2; color: var(--green); }
.item-actions { display: flex; gap: 6px; }
.upload-area { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }
.upload-area select { height: 36px; min-width: 120px; }
.upload-btn.primary-button { display: inline-flex; align-items: center; justify-content: center; color: #fff; cursor: pointer; }
.upload-btn.primary-button:has(input:disabled) { opacity: .55; cursor: not-allowed; }
.hint { color: var(--muted); font-size: 12px; }
.audit-panel { margin-top: 12px; border: 1px solid #e8d7bc; border-radius: 8px; background: #fff9ef; padding: 12px; }
.audit-panel.pass { border-color: #cfe5da; background: #f1faf5; }
.audit-head { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 6px; }
.audit-head strong { color: var(--ink); font-size: 14px; }
.audit-head span, .audit-panel p { color: var(--muted); font-size: 12px; }
.audit-panel p { margin: 0 0 10px; line-height: 1.6; }
.audit-list { display: grid; gap: 5px; margin-top: 8px; }
.audit-list small { color: var(--green); font-weight: 800; }
.audit-list span { color: var(--text); font-size: 12px; line-height: 1.5; }
.warning-list small { color: #9a611b; }
</style>
