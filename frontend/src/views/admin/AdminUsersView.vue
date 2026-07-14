<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Users } from '@lucide/vue';
import { fetchUsers, updateUserRole, updateUserStatus, resetUserPassword } from '@/api/admin';
import { useToastStore } from '@/stores/toastStore';
import type { UserListItem } from '@/api/admin';
import LoadingState from '@/components/common/LoadingState.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import ErrorState from '@/components/common/ErrorState.vue';

const users = ref<UserListItem[]>([]);
const loading = ref(true);
const error = ref(false);
const toastStore = useToastStore();
const keyword = ref('');
const roleFilter = ref('');
const statusFilter = ref<number | null>(null);

onMounted(() => loadUsers());

async function loadUsers() {
  loading.value = true;
  error.value = false;
  try {
    users.value = await fetchUsers({
      keyword: keyword.value || undefined,
      role: roleFilter.value || undefined,
      status: statusFilter.value ?? undefined,
    });
  } catch {
    error.value = true;
    users.value = [];
  } finally { loading.value = false; }
}

async function handleRoleChange(id: number, role: string) {
  try {
    await updateUserRole(id, role);
    toastStore.success('角色修改成功');
    await loadUsers();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  }
}

async function handleStatusToggle(id: number, currentStatus: number) {
  const newStatus = currentStatus === 1 ? 0 : 1;
  const action = newStatus === 0 ? '禁用' : '启用';
  if (!confirm(`确定${action}该用户吗？`)) return;
  try {
    await updateUserStatus(id, newStatus);
    toastStore.success(`${action}成功`);
    await loadUsers();
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  }
}

async function handleResetPassword(id: number, username: string) {
  if (!confirm(`确定重置用户「${username}」的密码为 123456 吗？`)) return;
  try {
    await resetUserPassword(id);
    toastStore.success('密码已重置为 123456');
  } catch (e: any) {
    toastStore.error(e.response?.data?.message || '操作失败');
  }
}

let searchTimer: ReturnType<typeof setTimeout> | null = null;
function onSearch() {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => loadUsers(), 300);
}
</script>

<template>
  <section>
    <h1>用户管理</h1>

    <div class="filter-bar">
      <input v-model="keyword" placeholder="搜索用户名或昵称" @input="onSearch" />
      <select v-model="roleFilter" @change="loadUsers">
        <option value="">全部角色</option>
        <option value="ADMIN">管理员</option>
        <option value="USER">普通用户</option>
      </select>
      <select v-model="statusFilter" @change="loadUsers">
        <option :value="null">全部状态</option>
        <option :value="1">启用</option>
        <option :value="0">禁用</option>
      </select>
    </div>

    <LoadingState v-if="loading" />

    <ErrorState v-else-if="error" message="加载用户列表失败" @retry="loadUsers" />

    <EmptyState v-else-if="users.length === 0"
      :icon="Users"
      :title="keyword || roleFilter || statusFilter !== null ? '暂无符合条件的用户' : '暂无用户'"
      description="系统中还没有注册用户"
    />

    <div v-else class="table-wrap panel">
      <table>
        <thead>
          <tr><th>ID</th><th>用户名</th><th>昵称</th><th>角色</th><th>状态</th><th>注册时间</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.nickname }}</td>
            <td>
              <select :value="u.role" @change="handleRoleChange(u.id, ($event.target as HTMLSelectElement).value)">
                <option value="ADMIN">管理员</option>
                <option value="USER">普通用户</option>
              </select>
            </td>
            <td>
              <span :class="u.status === 1 ? 'status-active' : 'status-disabled'">
                {{ u.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ u.createdAt }}</td>
            <td class="actions">
              <button class="link-btn" @click="handleStatusToggle(u.id, u.status)">
                {{ u.status === 1 ? '禁用' : '启用' }}
              </button>
              <button class="link-btn" @click="handleResetPassword(u.id, u.username)">重置密码</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="users.length === 0" class="empty">暂无用户数据</p>
    </div>
  </section>
</template>

<style scoped>
h1 { margin: 0 0 22px; font-size: 28px; }
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.filter-bar input, .filter-bar select { height: 38px; border: 1px solid var(--line); border-radius: 8px; padding: 0 12px; font-size: 13px; }
.filter-bar input { flex: 1; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 12px 14px; border-bottom: 1px solid var(--line); font-size: 14px; }
th { color: var(--muted); font-weight: 700; font-size: 12px; }
td select { height: 30px; border: 1px solid var(--line); border-radius: 6px; padding: 0 8px; font-size: 13px; }
.status-active { color: var(--green); font-weight: 700; }
.status-disabled { color: #b2472f; font-weight: 700; }
.actions { display: flex; gap: 8px; }
.link-btn { background: none; border: 0; color: var(--green); cursor: pointer; font-size: 13px; font-weight: 650; padding: 0; }
.empty { text-align: center; padding: 32px; color: var(--muted); }
</style>
