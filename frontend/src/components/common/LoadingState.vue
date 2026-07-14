<script setup lang="ts">
import { Loader2 } from '@lucide/vue';

defineProps<{
  text?: string;
}>();
</script>

<template>
  <div class="loading-state">
    <Loader2 :size="24" class="spin" />
    <span>{{ text || '加载中...' }}</span>
  </div>
</template>

<style scoped>
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 24px;
  color: var(--muted);
  font-size: 14px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
