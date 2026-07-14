import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useFavoriteStore } from '@/stores/favoriteStore';
import { useToastStore } from '@/stores/toastStore';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    {
      path: '/',
      component: () => import('@/layouts/UserLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/user/HomeView.vue') },
        { path: 'products', name: 'products', component: () => import('@/views/user/ProductListView.vue') },
        { path: 'products/:id', name: 'product-detail', component: () => import('@/views/user/ProductDetailView.vue') },
        { path: 'assistant', name: 'assistant', component: () => import('@/views/user/AssistantView.vue'), meta: { requiresAuth: true } },
        { path: 'compare', name: 'compare', component: () => import('@/views/user/CompareView.vue') },
        { path: 'favorites', name: 'favorites', component: () => import('@/views/user/FavoritesView.vue'), meta: { requiresAuth: true } },
        { path: 'history', name: 'history', component: () => import('@/views/user/HistoryView.vue'), meta: { requiresAuth: true } },
        { path: 'price-alerts', name: 'price-alerts', component: () => import('@/views/user/PriceAlertsView.vue'), meta: { requiresAuth: true } },
        { path: 'market', name: 'market', component: () => import('@/views/user/MarketView.vue') },
        { path: 'preferences', name: 'preferences', component: () => import('@/views/user/PreferencesView.vue'), meta: { requiresAuth: true } },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue') },
        { path: 'products', name: 'admin-products', component: () => import('@/views/admin/AdminProductsView.vue') },
        { path: 'ai-providers', name: 'admin-ai-providers', component: () => import('@/views/admin/AiProvidersView.vue') },
        { path: 'ai-knowledge', name: 'admin-ai-knowledge', component: () => import('@/views/admin/AiKnowledgeView.vue') },
        { path: 'ai-workflow-logs', name: 'admin-ai-workflow-logs', component: () => import('@/views/admin/AiWorkflowLogsView.vue') },
        { path: 'users', name: 'admin-users', component: () => import('@/views/admin/AdminUsersView.vue') },
        { path: 'taxonomy', name: 'admin-taxonomy', component: () => import('@/views/admin/AdminTaxonomyView.vue') },
      ],
    },
  ],
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  const favoriteStore = useFavoriteStore();
  const toastStore = useToastStore();

  if (!authStore.isLoggedIn) {
    favoriteStore.resetFavorites();
  } else if (!favoriteStore.loaded || favoriteStore.loadedUserId !== authStore.user?.id) {
    await favoriteStore.loadFavoriteIds();
  }

  if (to.name === 'login' && authStore.isLoggedIn) {
    return authStore.isAdmin ? '/admin/dashboard' : '/';
  }

  if (!to.meta.requiresAuth) {
    return true;
  }

  if (!authStore.token) {
    return { path: '/login', query: { redirect: to.fullPath } };
  }

  try {
    await authStore.restoreSession();
  } catch {
    authStore.logout();
    return { path: '/login', query: { redirect: to.fullPath } };
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    toastStore.error('没有权限访问该功能');
    return '/';
  }

  return true;
});

export default router;
