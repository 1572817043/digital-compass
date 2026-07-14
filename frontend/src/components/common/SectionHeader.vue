<script setup lang="ts">
defineProps<{
  title: string;
  hint?: string;
}>();
</script>

<template>
  <div class="section-header">
    <div>
      <p v-if="hint">{{ hint }}</p>
      <h2>{{ title }}</h2>
    </div>
    <slot />
  </div>
</template>

<style scoped>
.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

p {
  margin: 0 0 6px;
  color: var(--green);
  font-size: 13px;
  font-weight: 850;
}

h2 {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
}
</style>
