<script setup lang="ts">
import { AlertTriangle } from '@lucide/vue';

defineProps<{
  message?: string;
  buttonLabel?: string;
}>();

const emit = defineEmits<{
  (e: 'retry'): void;
  (e: 'action'): void;
}>();

function handleClick() {
  emit('retry');
  emit('action');
}
</script>

<template>
  <div class="error-state">
    <AlertTriangle :size="40" class="error-icon" />
    <p class="error-title">{{ message || '加载失败' }}</p>
    <p class="error-desc">请检查网络连接后重试</p>
    <button class="error-action" @click="handleClick">{{ buttonLabel || '重试' }}</button>
  </div>
</template>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 24px;
  text-align: center;
}

.error-icon {
  color: #f59e0b;
  margin-bottom: 12px;
}

.error-title {
  margin: 0;
  color: var(--ink);
  font-size: 16px;
  font-weight: 700;
}

.error-desc {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 14px;
}

.error-action {
  margin-top: 16px;
  height: 36px;
  padding: 0 18px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--ink);
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
}
</style>
