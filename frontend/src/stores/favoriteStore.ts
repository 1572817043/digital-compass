import { defineStore } from 'pinia';
import { ref } from 'vue';
import { fetchFavoriteIds, addFavorite as apiAdd, removeFavorite as apiRemove } from '@/api/favorite';
import { useAuthStore } from './authStore';

export const useFavoriteStore = defineStore('favorite', () => {
  const favoriteIds = ref<number[]>([]);
  const loaded = ref(false);
  const loadedUserId = ref<number | null>(null);

  async function loadFavoriteIds() {
    const auth = useAuthStore();
    if (!auth.isLoggedIn || !auth.user) {
      resetFavorites();
      return;
    }
    if (loaded.value && loadedUserId.value === auth.user.id) {
      return;
    }
    try {
      favoriteIds.value = await fetchFavoriteIds();
      loadedUserId.value = auth.user.id;
      loaded.value = true;
    } catch {
      favoriteIds.value = [];
      loadedUserId.value = auth.user.id;
      loaded.value = true;
    }
  }

  function isFavorite(productId: number) {
    return favoriteIds.value.includes(productId);
  }

  async function toggleFavorite(productId: number) {
    const auth = useAuthStore();
    if (!auth.isLoggedIn) return false;

    if (isFavorite(productId)) {
      await apiRemove(productId);
      favoriteIds.value = favoriteIds.value.filter(id => id !== productId);
    } else {
      await apiAdd(productId);
      favoriteIds.value.push(productId);
    }
    return true;
  }

  function resetFavorites() {
    favoriteIds.value = [];
    loaded.value = false;
    loadedUserId.value = null;
  }

  return { favoriteIds, loaded, loadedUserId, loadFavoriteIds, isFavorite, toggleFavorite, resetFavorites };
});
