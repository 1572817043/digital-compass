<script setup lang="ts">
import { Inbox } from '@lucide/vue';

defineProps<{
  icon?: any;
  title?: string;
  description?: string;
  buttonLabel?: string;
}>();

const emit = defineEmits<{
  (e: 'action'): void;
}>();
</script>

<template>
  <div class="empty-state">
    <component :is="icon || Inbox" :size="40" class="empty-icon" />
    <p class="empty-title">{{ title || '暂无数据' }}</p>
    <p v-if="description" class="empty-desc">{{ description }}</p>
    <button v-if="buttonLabel" class="empty-action" @click="emit('action')">{{ buttonLabel }}</button>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 24px;
  text-align: center;
}

.empty-icon {
  color: #d1d5db;
  margin-bottom: 12px;
}

.empty-title {
  margin: 0;
  color: var(--ink);
  font-size: 16px;
  font-weight: 700;
}

.empty-desc {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 14px;
}

.empty-action {
  margin-top: 16px;
  height: 36px;
  padding: 0 18px;
  border: 0;
  border-radius: 8px;
  background: var(--ink);
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
}
</style>
