import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

const STORAGE_KEY = 'digicompass_compare';
const MAX_COMPARE = 3;

function loadIds(): number[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    const ids = parsed
      .map(Number)
      .filter(id => Number.isSafeInteger(id) && id > 0);
    return Array.from(new Set(ids)).slice(0, MAX_COMPARE);
  } catch {
    return [];
  }
}

export const useCompareStore = defineStore('compare', () => {
  const selectedIds = ref<number[]>(loadIds());

  function persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(selectedIds.value));
  }

  const count = computed(() => selectedIds.value.length);
  const canAdd = computed(() => selectedIds.value.length < MAX_COMPARE);

  function isSelected(id: number) {
    return selectedIds.value.includes(id);
  }

  function addProduct(id: number) {
    if (selectedIds.value.includes(id)) return;
    if (selectedIds.value.length >= MAX_COMPARE) return;
    selectedIds.value.push(id);
    persist();
  }

  function removeProduct(id: number) {
    selectedIds.value = selectedIds.value.filter(i => i !== id);
    persist();
  }

  function clear() {
    selectedIds.value = [];
    persist();
  }

  return { selectedIds, count, canAdd, isSelected, addProduct, removeProduct, clear };
});
