<template>
  <div class="admin-layout">
    <aside>
      <RouterLink to="/" class="admin-brand">DigiCompass</RouterLink>
      <nav>
        <RouterLink to="/admin/dashboard">概览</RouterLink>
        <RouterLink to="/admin/products">产品管理</RouterLink>
        <span>价格管理</span>
        <RouterLink to="/admin/ai-knowledge">AI 知识库</RouterLink>
        <RouterLink to="/admin/ai-workflow-logs">AI 工作流</RouterLink>
        <RouterLink to="/admin/users">用户管理</RouterLink>
        <RouterLink to="/admin/taxonomy">分类品牌</RouterLink>
        <RouterLink to="/admin/ai-providers">AI 配置</RouterLink>
      </nav>
    </aside>
    <main>
      <header class="admin-topbar">
        <div>
          <span>当前登录</span>
          <strong>{{ authStore.user?.nickname || authStore.user?.username }}</strong>
        </div>
        <button class="ghost-button" type="button" @click="logout">退出登录</button>
      </header>
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

async function logout() {
  authStore.logout();
  await router.replace('/login');
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px 1fr;
  background: #f6f6f4;
}

aside {
  border-right: 1px solid var(--line);
  background: #fff;
  padding: 24px;
}

.admin-brand {
  display: block;
  margin-bottom: 28px;
  font-size: 20px;
  font-weight: 850;
}

nav {
  display: grid;
  gap: 10px;
}

nav a,
nav span {
  height: 38px;
  display: flex;
  align-items: center;
  border-radius: 8px;
  padding: 0 12px;
  color: var(--muted);
  font-size: 14px;
  font-weight: 650;
}

nav a.router-link-active {
  background: var(--soft);
  color: var(--ink);
}

main {
  padding: 28px;
}

.admin-topbar {
  min-height: 54px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 26px;
}

.admin-topbar div {
  display: grid;
  gap: 5px;
}

.admin-topbar span {
  color: var(--muted);
  font-size: 12px;
  font-weight: 700;
}

.admin-topbar strong {
  color: var(--ink);
  font-size: 18px;
}

@media (max-width: 900px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  aside {
    display: none;
  }
}
</style>
