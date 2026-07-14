<script setup lang="ts">
import { onMounted } from 'vue';
import { useToastStore } from '@/stores/toastStore';
import { registerToastHandler } from '@/api/http';
import { CheckCircle, Info, XCircle } from '@lucide/vue';

const toastStore = useToastStore();

onMounted(() => {
  registerToastHandler((message, type) => {
    toastStore.show(message, type);
  });
});

const iconMap = {
  success: CheckCircle,
  error: XCircle,
  info: Info,
};
</script>

<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div v-for="toast in toastStore.toasts" :key="toast.id" class="toast-item" :class="toast.type">
          <component :is="iconMap[toast.type]" :size="16" />
          <span>{{ toast.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
  font-size: 14px;
  font-weight: 600;
  pointer-events: auto;
  max-width: 360px;
}

.toast-item.success {
  border-left: 3px solid #16a34a;
  color: #16a34a;
}

.toast-item.error {
  border-left: 3px solid #dc2626;
  color: #dc2626;
}

.toast-item.info {
  border-left: 3px solid #2563eb;
  color: #2563eb;
}

.toast-enter-active { transition: all 0.3s ease; }
.toast-leave-active { transition: all 0.2s ease; }
.toast-enter-from { opacity: 0; transform: translateX(30px); }
.toast-leave-to { opacity: 0; transform: translateX(30px); }
</style>
