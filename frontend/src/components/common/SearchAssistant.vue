<script setup lang="ts">
import { Sparkles } from '@lucide/vue';
import { ref } from 'vue';

const prompt = ref('预算 4000，拍照好，小屏手机');
</script>

<template>
  <div class="assistant-box">
    <label for="assistant-input">输入你的预算和用途</label>
    <div class="search-row">
      <input id="assistant-input" v-model="prompt" />
      <RouterLink :to="{ path: '/assistant', query: { q: prompt } }" class="primary-button">
        <Sparkles :size="17" />生成建议
      </RouterLink>
    </div>
    <div class="chips">
      <span>学生党手机</span>
      <span>轻薄办公本</span>
      <span>二手高性价比</span>
      <span>拍照优先</span>
    </div>
  </div>
</template>

<style scoped>
.assistant-box {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--panel);
  padding: 18px;
  box-shadow: var(--shadow);
}

label {
  display: block;
  margin-bottom: 10px;
  color: var(--muted);
  font-size: 13px;
  font-weight: 750;
}

.search-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

input {
  width: 100%;
  height: 46px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fdfdfc;
  color: var(--ink);
  padding: 0 13px;
  outline: none;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.chips span {
  height: 30px;
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 0 10px;
  color: var(--muted);
  background: #fdfdfc;
  font-size: 12px;
  font-weight: 650;
}

@media (max-width: 700px) {
  .search-row {
    grid-template-columns: 1fr;
  }
}
</style>
