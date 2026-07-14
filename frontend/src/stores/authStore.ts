import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getCurrentUser, login as loginApi } from '@/api/auth';
import type { LoginUser } from '@/types/auth';

const tokenKey = 'digicompass_token';
const userKey = 'digicompass_user';

function readStoredUser(): LoginUser | null {
  const raw = localStorage.getItem(userKey);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as LoginUser;
  } catch {
    localStorage.removeItem(userKey);
    return null;
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(tokenKey) || '');
  const user = ref<LoginUser | null>(readStoredUser());
  const loading = ref(false);

  const isLoggedIn = computed(() => Boolean(token.value && user.value));
  const isAdmin = computed(() => user.value?.role === 'ADMIN');

  function saveSession(nextToken: string, nextUser: LoginUser) {
    token.value = nextToken;
    user.value = nextUser;
    localStorage.setItem(tokenKey, nextToken);
    localStorage.setItem(userKey, JSON.stringify(nextUser));
  }

  async function login(username: string, password: string) {
    loading.value = true;
    try {
      const result = await loginApi({ username, password });
      saveSession(result.token, result.user);
      return result.user;
    } finally {
      loading.value = false;
    }
  }

  async function restoreSession() {
    if (!token.value) {
      return null;
    }
    try {
      const currentUser = await getCurrentUser();
      saveSession(token.value, currentUser);
      return currentUser;
    } catch {
      logout();
      return null;
    }
  }

  function logout() {
    token.value = '';
    user.value = null;
    localStorage.removeItem(tokenKey);
    localStorage.removeItem(userKey);
  }

  return {
    token,
    user,
    loading,
    isLoggedIn,
    isAdmin,
    login,
    restoreSession,
    logout,
  };
});
