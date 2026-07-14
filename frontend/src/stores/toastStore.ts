import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface ToastItem {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

let nextId = 0;

export const useToastStore = defineStore('toast', () => {
  const toasts = ref<ToastItem[]>([]);

  function show(message: string, type: ToastItem['type'] = 'info', duration = 3000) {
    const id = nextId++;
    toasts.value.push({ id, message, type });
    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== id);
    }, duration);
  }

  function success(message: string) { show(message, 'success'); }
  function error(message: string) { show(message, 'error', 4000); }
  function info(message: string) { show(message, 'info'); }

  return { toasts, show, success, error, info };
});
