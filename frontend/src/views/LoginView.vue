<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, Compass, LockKeyhole, UserRound } from '@lucide/vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const username = ref('admin');
const password = ref('');
const errorMessage = ref('');

const redirectPath = computed(() => {
  const redirect = route.query.redirect;
  return typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/admin/dashboard';
});

async function submit() {
  errorMessage.value = '';
  if (!username.value.trim() || !password.value) {
    errorMessage.value = '请输入账号和密码';
    return;
  }

  try {
    await authStore.login(username.value.trim(), password.value);
    await router.replace(redirectPath.value);
  } catch (error: any) {
    if (error.displayMessage) {
      errorMessage.value = error.displayMessage;
    } else if (axios.isAxiosError(error)) {
      errorMessage.value = error.response?.data?.message || '账号或密码错误';
    } else {
      errorMessage.value = '登录失败，请稍后再试';
    }
  }
}
</script>

<template>
  <div class="login-page">
    <RouterLink to="/" class="back-link"><ArrowLeft :size="16" />返回首页</RouterLink>

    <main class="login-main">
      <section class="login-copy">
        <span class="brand-mark"><Compass :size="22" /></span>
        <p class="eyebrow">DigiCompass Admin</p>
        <h1>数码产品信息库管理入口</h1>
        <p class="lead">用于维护产品参数、官方链接、行情参考和知识库内容。用户端保持轻量，管理端负责数据质量。</p>
      </section>

      <section class="login-panel">
        <div class="panel-head">
          <p>管理员登录</p>
          <span>默认账号 admin，密码 123456</span>
        </div>

        <form @submit.prevent="submit">
          <label>
            <span>账号</span>
            <div class="input-wrap">
              <UserRound :size="16" />
              <input v-model="username" autocomplete="username" placeholder="请输入账号" />
            </div>
          </label>

          <label>
            <span>密码</span>
            <div class="input-wrap">
              <LockKeyhole :size="16" />
              <input v-model="password" type="password" autocomplete="current-password" placeholder="请输入密码" />
            </div>
          </label>

          <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

          <button class="primary-button login-button" type="submit" :disabled="authStore.loading">
            {{ authStore.loading ? '登录中...' : '登录管理端' }}
          </button>
        </form>
      </section>
    </main>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.7), rgba(246, 246, 244, 0.96)),
    var(--bg);
  padding: 28px;
}

.back-link {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--muted);
  font-size: 14px;
  font-weight: 700;
}

.login-main {
  min-height: calc(100vh - 92px);
  width: min(1060px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) 390px;
  align-items: center;
  gap: 72px;
}

.brand-mark {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: var(--ink);
  color: #fff;
}

.eyebrow {
  margin: 24px 0 10px;
  color: var(--green);
  font-size: 13px;
  font-weight: 850;
}

h1 {
  max-width: 560px;
  margin: 0;
  color: var(--ink);
  font-size: 46px;
  line-height: 1.08;
  letter-spacing: 0;
}

.lead {
  max-width: 520px;
  margin: 22px 0 0;
  color: var(--text);
  font-size: 17px;
  line-height: 1.9;
}

.login-panel {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  box-shadow: var(--shadow);
  padding: 28px;
}

.panel-head {
  margin-bottom: 26px;
}

.panel-head p {
  margin: 0 0 8px;
  color: var(--ink);
  font-size: 22px;
  font-weight: 850;
}

.panel-head span {
  color: var(--muted);
  font-size: 13px;
}

form {
  display: grid;
  gap: 18px;
}

label {
  display: grid;
  gap: 8px;
  color: var(--ink);
  font-size: 13px;
  font-weight: 750;
}

.input-wrap {
  height: 46px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  padding: 0 14px;
  color: var(--muted);
}

input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--ink);
  font: inherit;
}

.error-message {
  min-height: 20px;
  margin: 0;
  color: #b2472f;
  font-size: 13px;
  font-weight: 700;
}

.login-button {
  width: 100%;
  margin-top: 2px;
}

.login-button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

@media (max-width: 860px) {
  .login-page {
    padding: 22px;
  }

  .login-main {
    min-height: auto;
    grid-template-columns: 1fr;
    gap: 28px;
    padding: 48px 0;
  }

  h1 {
    font-size: 34px;
  }

  .lead {
    font-size: 15px;
  }
}
</style>
