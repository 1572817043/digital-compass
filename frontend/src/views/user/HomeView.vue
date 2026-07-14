<script setup lang="ts">
import { onMounted } from 'vue';
import { ArrowRight, BarChart3, ShieldCheck, SlidersHorizontal } from '@lucide/vue';
import SearchAssistant from '@/components/common/SearchAssistant.vue';
import SectionHeader from '@/components/common/SectionHeader.vue';
import ProductCard from '@/components/product/ProductCard.vue';
import { useProductStore } from '@/stores/productStore';

const productStore = useProductStore();

onMounted(() => {
  if (productStore.products.length === 0) {
    productStore.loadProducts();
  }
});
</script>

<template>
  <section class="hero">
    <div>
      <div class="eyebrow">面向普通用户的数码产品选购决策平台</div>
      <h1>输入预算和用途，得到更清楚的数码购买建议。</h1>
      <p class="lead">
        平台先重点支持手机和笔记本电脑，整合产品参数、官方价格、二手参考价和购买链接，
        并通过 AI 生成推荐理由、备选方案和风险提醒。
      </p>
      <SearchAssistant />
    </div>

    <aside class="preview panel">
      <div class="preview-head">
        <div>
          <span>AI 选购报告预览</span>
          <h2>适合你的候选产品</h2>
        </div>
        <strong>匹配度</strong>
      </div>
      <div class="candidate" v-for="product in productStore.topProducts" :key="product.id">
        <div>
          <strong>{{ product.name }}</strong>
          <p>{{ product.summary }}</p>
        </div>
        <span>{{ product.score }}</span>
      </div>
    </aside>
  </section>

  <section class="section home-grid">
    <div>
      <SectionHeader title="热门产品" hint="手机 / 笔记本电脑">
        <RouterLink to="/products" class="ghost-button">查看全部 <ArrowRight :size="16" /></RouterLink>
      </SectionHeader>
      <div class="product-grid">
        <ProductCard v-for="product in productStore.topProducts" :key="product.id" :product="product" />
      </div>
    </div>

    <aside class="side-stack">
      <div class="panel side-panel">
        <SectionHeader title="本周榜单" hint="综合推荐" />
        <div class="rank" v-for="(product, index) in productStore.topProducts" :key="product.id">
          <span>{{ index + 1 }}</span>
          <div>
            <strong>{{ product.name }}</strong>
            <p>{{ product.brandName }} / {{ product.categoryName }}</p>
          </div>
          <em>{{ product.score }}</em>
        </div>
      </div>

      <div class="panel side-panel">
        <SectionHeader title="价格行情" hint="二手参考" />
        <div class="market" v-for="product in productStore.topProducts.slice(0, 2)" :key="product.id">
          <div>
            <strong>{{ product.name }}</strong>
            <p>官方价 {{ product.officialPrice != null ? `¥${product.officialPrice}` : '待定' }}</p>
          </div>
          <em>{{ product.score }} 分</em>
        </div>
      </div>
    </aside>
  </section>

  <section class="workflow section">
    <article>
      <SlidersHorizontal :size="22" />
      <h3>需求解析</h3>
      <p>提取预算、用途、品牌偏好和二手接受度。</p>
    </article>
    <article>
      <BarChart3 :size="22" />
      <h3>候选召回</h3>
      <p>从产品库、参数库、价格库中筛选产品。</p>
    </article>
    <article>
      <ShieldCheck :size="22" />
      <h3>风险提醒</h3>
      <p>说明二手验机、保修、电池健康和价格时机。</p>
    </article>
  </section>
</template>

<style scoped>
.hero {
  display: grid;
  grid-template-columns: 1.02fr .98fr;
  gap: 52px;
  align-items: center;
  padding: 58px 0 42px;
}

.eyebrow {
  margin-bottom: 14px;
  color: var(--green);
  font-size: 13px;
  font-weight: 850;
}

h1 {
  margin: 0;
  max-width: 720px;
  font-size: clamp(42px, 5vw, 64px);
  line-height: 1.06;
  font-weight: 850;
  letter-spacing: -0.02em;
}

.lead {
  margin: 20px 0 28px;
  max-width: 640px;
  color: var(--text);
  font-size: 16px;
  line-height: 1.85;
}

.preview {
  padding: 24px;
}

.preview-head {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--line);
}

.preview-head span {
  color: var(--green);
  font-size: 13px;
  font-weight: 850;
}

.preview-head h2 {
  margin: 8px 0 0;
  font-size: 24px;
}

.preview-head strong {
  height: 28px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  background: #eef5f2;
  color: var(--green);
  padding: 0 10px;
  font-size: 12px;
}

.candidate {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 16px;
  align-items: center;
  margin-top: 14px;
  border: 1px solid var(--line);
  border-radius: 9px;
  padding: 14px;
  background: #fdfdfc;
}

.candidate p {
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.55;
}

.candidate span {
  color: var(--green);
  font-size: 22px;
  font-weight: 900;
}

.home-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 22px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.side-stack {
  display: grid;
  gap: 18px;
}

.side-panel {
  padding: 18px;
}

.rank,
.market {
  display: grid;
  grid-template-columns: 34px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 13px 0;
  border-top: 1px solid var(--line);
}

.market {
  grid-template-columns: 1fr auto;
}

.rank span {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--soft);
  color: var(--green);
  font-weight: 900;
}

.rank p,
.market p {
  margin: 3px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.rank em,
.market em {
  color: var(--amber);
  font-style: normal;
  font-weight: 850;
}

.workflow {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.workflow article {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--panel);
  padding: 18px;
}

.workflow svg {
  color: var(--green);
}

.workflow h3 {
  margin: 12px 0 8px;
  font-size: 17px;
}

.workflow p {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.65;
}

@media (max-width: 1100px) {
  .hero,
  .home-grid {
    grid-template-columns: 1fr;
  }

  .product-grid,
  .workflow {
    grid-template-columns: 1fr;
  }
}
</style>
