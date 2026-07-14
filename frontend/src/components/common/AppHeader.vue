<script setup lang="ts">
import { Bell, Compass, Clock, Heart, LogIn, LogOut, Search, Settings } from '@lucide/vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useFavoriteStore } from '@/stores/favoriteStore';

const authStore = useAuthStore();
const favoriteStore = useFavoriteStore();
const router = useRouter();

function handleLogout() {
  authStore.logout();
  favoriteStore.resetFavorites();
  router.push('/login');
}
</script>

<template>
  <header class="app-header">
    <RouterLink to="/" class="brand">
      <span class="brand-mark"><Compass :size="18" /></span>
      <span>DigiCompass 数码罗盘</span>
    </RouterLink>

    <nav class="nav">
      <RouterLink to="/products">产品库</RouterLink>
      <RouterLink to="/compare">对比工具</RouterLink>
      <RouterLink v-if="authStore.isLoggedIn" to="/favorites">
        <Heart :size="14" /> 收藏
      </RouterLink>
      <RouterLink v-if="authStore.isLoggedIn" to="/history">
        <Clock :size="14" /> 最近看过
      </RouterLink>
      <RouterLink v-if="authStore.isLoggedIn" to="/price-alerts">
        <Bell :size="14" /> 价格提醒
      </RouterLink>
      <RouterLink to="/market">价格行情</RouterLink>
      <RouterLink to="/assistant">选购助手</RouterLink>
      <RouterLink v-if="authStore.isLoggedIn" to="/preferences">
        <Settings :size="14" /> 偏好设置
      </RouterLink>
    </nav>

    <div class="header-actions">
      <button class="ghost-button"><Search :size="16" />搜索</button>
      <RouterLink v-if="authStore.isLoggedIn && authStore.isAdmin" to="/admin/dashboard" class="primary-button">管理端</RouterLink>
      <button v-if="authStore.isLoggedIn" class="primary-button" @click="handleLogout"><LogOut :size="16" />退出</button>
      <RouterLink v-else to="/login" class="primary-button"><LogIn :size="16" />登录</RouterLink>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--line);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 850;
}

.brand-mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 9px;
  background: var(--ink);
  color: #fff;
}

.nav {
  display: flex;
  align-items: center;
  gap: 30px;
  color: var(--muted);
  font-size: 14px;
  font-weight: 650;
}

.nav a.router-link-active {
  color: var(--ink);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 900px) {
  .nav,
  .header-actions {
    display: none;
  }
}
</style>
