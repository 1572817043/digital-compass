<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Bot, Loader2, MessageSquare, Plus, SendHorizontal, UserRound } from '@lucide/vue';
import {
  createAssistantConversation,
  fetchAssistantConversations,
  fetchAssistantMessages,
  sendAssistantMessage,
} from '@/api/assistant';
import { fetchMyPreference } from '@/api/preference';
import AssistantProductCard from '@/components/assistant/AssistantProductCard.vue';
import type {
  AssistantConversationItem,
  AssistantRecommendationItem,
  ChatMessageItem,
} from '@/types/assistant';
import { useAuthStore } from '@/stores/authStore';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const conversations = ref<AssistantConversationItem[]>([]);
const activeConversationId = ref<number | null>(null);
const messages = ref<ChatMessageItem[]>([]);
const recommendations = ref<AssistantRecommendationItem[]>([]);
const input = ref('');
const bootstrapping = ref(false);
const loadingMessages = ref(false);
const sending = ref(false);
const errorMessage = ref('');
const chatBody = ref<HTMLElement | null>(null);
const preferenceHint = ref('');

const activeConversation = computed(() =>
  conversations.value.find(item => item.id === activeConversationId.value) || null
);

const recommendationsByMessage = computed(() => {
  const map = new Map<number, AssistantRecommendationItem[]>();
  for (const item of recommendations.value) {
    const list = map.get(item.messageId) || [];
    list.push(item);
    map.set(item.messageId, list);
  }
  return map;
});

onMounted(async () => {
  await initAssistant();
});

async function initAssistant() {
  bootstrapping.value = true;
  errorMessage.value = '';
  try {
    // 加载用户偏好
    if (authStore.isLoggedIn) {
      try {
        const pref = await fetchMyPreference();
        if (pref.id != null) {
          const parts: string[] = [];
          if (pref.minBudget != null || pref.maxBudget != null) {
            const min = pref.minBudget != null ? `¥${pref.minBudget}` : '不限';
            const max = pref.maxBudget != null ? `¥${pref.maxBudget}` : '不限';
            parts.push(`预算 ${min}-${max}`);
          }
          if (pref.priorityTags) parts.push(`关注 ${pref.priorityTags.replace(/,/g, '/')}`);
          if (parts.length > 0) preferenceHint.value = parts.join('，');
        }
      } catch { /* 静默 */ }
    }
    conversations.value = await fetchAssistantConversations();
    const queryPrompt = typeof route.query.q === 'string' ? route.query.q.trim() : '';

    if (queryPrompt) {
      const conversation = await createAssistantConversation(queryPrompt);
      conversations.value = [conversation, ...conversations.value];
      activeConversationId.value = conversation.id;
      await loadMessages(conversation.id);
      input.value = queryPrompt;
      await submit();
      await router.replace({ path: '/assistant' });
      return;
    }

    if (conversations.value.length > 0) {
      activeConversationId.value = conversations.value[0].id;
      await loadMessages(conversations.value[0].id);
    }
  } catch (error: any) {
    errorMessage.value = error.displayMessage || error.response?.data?.message || '选购助手加载失败';
  } finally {
    bootstrapping.value = false;
  }
}

async function startConversation() {
  errorMessage.value = '';
  const conversation = await createAssistantConversation('新的选购咨询');
  conversations.value = [conversation, ...conversations.value];
  activeConversationId.value = conversation.id;
  messages.value = [];
  recommendations.value = [];
  await scrollToBottom();
}

async function selectConversation(id: number) {
  if (id === activeConversationId.value || loadingMessages.value) return;
  activeConversationId.value = id;
  await loadMessages(id);
}

async function loadMessages(conversationId: number) {
  loadingMessages.value = true;
  errorMessage.value = '';
  try {
    const result = await fetchAssistantMessages(conversationId);
    messages.value = result.messages;
    recommendations.value = result.recommendations;
    await scrollToBottom();
  } catch (error: any) {
    errorMessage.value = error.displayMessage || error.response?.data?.message || '会话加载失败';
  } finally {
    loadingMessages.value = false;
  }
}

async function submit() {
  const content = input.value.trim();
  if (!content || sending.value) return;

  sending.value = true;
  errorMessage.value = '';
  try {
    if (activeConversationId.value == null) {
      await startConversation();
    }
    const result = await sendAssistantMessage(activeConversationId.value!, content);
    input.value = '';
    messages.value = [...messages.value, result.userMessage, result.assistantMessage];
    recommendations.value = [...recommendations.value, ...result.recommendations];
    conversations.value = await fetchAssistantConversations();
    await scrollToBottom();
  } catch (error: any) {
    errorMessage.value = error.displayMessage || error.response?.data?.message || '生成推荐失败';
  } finally {
    sending.value = false;
  }
}

function useQuickPrompt(text: string) {
  input.value = text;
}

async function scrollToBottom() {
  await nextTick();
  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight;
  }
}
</script>

<template>
  <section class="assistant-page">
    <aside class="conversation-panel panel">
      <div class="panel-head">
        <div>
          <p>Assistant</p>
          <h1>选购助手</h1>
        </div>
        <button class="icon-button" type="button" @click="startConversation" title="新建会话">
          <Plus :size="18" />
        </button>
      </div>

      <div v-if="bootstrapping" class="loading-line">
        <Loader2 :size="16" />
        加载会话中
      </div>

      <div v-else class="conversation-list">
        <button
          v-for="conversation in conversations"
          :key="conversation.id"
          class="conversation-item"
          :class="{ active: conversation.id === activeConversationId }"
          type="button"
          @click="selectConversation(conversation.id)"
        >
          <MessageSquare :size="16" />
          <span>
            <strong>{{ conversation.title }}</strong>
            <small>{{ conversation.lastMessage || '暂无消息' }}</small>
          </span>
        </button>

        <div v-if="conversations.length === 0" class="empty-side">
          <p>还没有会话</p>
          <small>点击右上角 + 新建一个选购咨询</small>
        </div>
      </div>
    </aside>

    <main class="chat-panel panel">
      <header class="chat-header">
        <div>
          <p>Buying Agent</p>
          <h2>{{ activeConversation?.title || '新的选购咨询' }}</h2>
        </div>
        <span>产品库规则推荐</span>
      </header>

      <div ref="chatBody" class="chat-body">
        <div v-if="!activeConversationId && !bootstrapping" class="welcome-card">
          <Bot :size="28" />
          <h3>告诉我预算、用途和偏好</h3>
          <p>例如“预算 4000，拍照好，小屏手机”或“学生党轻薄本，主要写代码和上课”。</p>
        </div>

        <div v-if="loadingMessages" class="loading-line center">
          <Loader2 :size="16" />
          加载消息中
        </div>

        <template v-for="message in messages" :key="message.id">
          <div class="message-row" :class="message.role === 'USER' ? 'from-user' : 'from-assistant'">
            <div class="avatar">
              <UserRound v-if="message.role === 'USER'" :size="17" />
              <Bot v-else :size="17" />
            </div>
            <div class="message-content">
              <div class="bubble">{{ message.content }}</div>
              <div
                v-if="message.role === 'ASSISTANT' && recommendationsByMessage.get(message.id)?.length"
                class="recommendation-list"
              >
                <AssistantProductCard
                  v-for="item in recommendationsByMessage.get(message.id)"
                  :key="item.id"
                  :item="item"
                />
              </div>
            </div>
          </div>
        </template>

        <!-- 发送中临时状态 -->
        <div v-if="sending" class="message-row from-assistant">
          <div class="avatar"><Bot :size="17" /></div>
          <div class="message-content">
            <div class="bubble thinking">
              <Loader2 :size="14" class="spin-icon" />
              <span>正在分析需求，为你挑选最合适的产品...</span>
            </div>
          </div>
        </div>
      </div>

      <footer class="chat-footer">
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        <div v-if="preferenceHint" class="preference-hint">
          已加载你的选购偏好：{{ preferenceHint }}
        </div>
        <div class="quick-prompts">
          <button type="button" @click="useQuickPrompt('预算 4000，拍照好，小屏手机')">拍照手机</button>
          <button type="button" @click="useQuickPrompt('学生党，预算 6000，轻薄办公本')">学生轻薄本</button>
          <button type="button" @click="useQuickPrompt('可以接受二手，想要高性价比平板')">二手平板</button>
        </div>
        <form class="input-row" @submit.prevent="submit">
          <textarea
            v-model="input"
            rows="2"
            placeholder="输入预算、用途、品牌偏好或避坑点"
            @keydown.enter.exact.prevent="submit"
          />
          <button class="primary-button send-button" type="submit" :disabled="sending || !input.trim()">
            <Loader2 v-if="sending" :size="17" />
            <SendHorizontal v-else :size="17" />
            {{ sending ? '生成中' : '发送' }}
          </button>
        </form>
      </footer>
    </main>
  </section>
</template>

<style scoped>
.assistant-page {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 18px;
  height: calc(100vh - 112px);
  min-height: 640px;
  padding-top: 24px;
}

.conversation-panel,
.chat-panel {
  min-height: 0;
}

.conversation-panel {
  display: grid;
  grid-template-rows: auto 1fr;
  padding: 18px;
}

.panel-head,
.chat-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.panel-head p,
.chat-header p {
  margin: 0 0 6px;
  color: var(--green);
  font-size: 12px;
  font-weight: 850;
}

h1,
h2,
h3 {
  margin: 0;
  color: var(--ink);
}

h1 {
  font-size: 28px;
}

h2 {
  font-size: 24px;
}

.icon-button {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: #fff;
  color: var(--ink);
  cursor: pointer;
}

.conversation-list {
  min-height: 0;
  overflow: auto;
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.conversation-item {
  width: 100%;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 10px;
  align-items: center;
  border: 1px solid transparent;
  border-radius: 9px;
  background: transparent;
  color: var(--ink);
  padding: 11px;
  text-align: left;
  cursor: pointer;
}

.conversation-item.active {
  border-color: var(--green);
  background: #eef5f2;
}

.conversation-item span {
  min-width: 0;
}

.conversation-item strong,
.conversation-item small {
  display: block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.conversation-item strong {
  font-size: 13px;
}

.conversation-item small {
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
}

.chat-panel {
  display: grid;
  grid-template-rows: auto 1fr auto;
}

.chat-header {
  border-bottom: 1px solid var(--line);
  padding: 20px 22px;
}

.chat-header span {
  border: 1px solid var(--line);
  border-radius: 999px;
  color: var(--muted);
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 750;
}

.chat-body {
  min-height: 0;
  overflow: auto;
  padding: 22px;
  background: #f8f8f6;
}

.welcome-card,
.empty-side {
  display: grid;
  place-items: center;
  border: 1px dashed var(--line);
  border-radius: 10px;
  color: var(--muted);
  padding: 28px;
  text-align: center;
}

.empty-side {
  gap: 6px;
}

.empty-side p {
  margin: 0;
  font-size: 14px;
  font-weight: 650;
  color: var(--ink);
}

.empty-side small {
  margin: 0;
  font-size: 12px;
}

.welcome-card {
  min-height: 280px;
  gap: 10px;
  background: #fff;
}

.welcome-card p {
  max-width: 420px;
  margin: 0;
  line-height: 1.7;
}

.message-row {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 10px;
  margin-bottom: 18px;
}

.from-user {
  grid-template-columns: minmax(0, 1fr) 34px;
}

.from-user .avatar {
  grid-column: 2;
}

.from-user .message-content {
  grid-row: 1;
  justify-self: end;
  max-width: 72%;
}

.from-assistant .message-content {
  max-width: 760px;
}

.avatar {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 9px;
  background: #fff;
  color: var(--green);
  border: 1px solid var(--line);
}

.bubble {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fff;
  color: var(--text);
  padding: 12px 14px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.from-user .bubble {
  background: var(--ink);
  border-color: var(--ink);
  color: #fff;
}

.recommendation-list {
  display: grid;
  gap: 10px;
  margin-top: 10px;
}

.chat-footer {
  border-top: 1px solid var(--line);
  padding: 14px 16px 16px;
  background: #fff;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.quick-prompts button {
  height: 30px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: #fff;
  color: var(--muted);
  padding: 0 10px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.input-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: end;
}

textarea {
  width: 100%;
  min-height: 54px;
  max-height: 140px;
  resize: vertical;
  border: 1px solid var(--line);
  border-radius: 9px;
  background: #fdfdfc;
  color: var(--ink);
  padding: 11px 12px;
  line-height: 1.6;
  outline: none;
}

.send-button {
  height: 54px;
}

.send-button:disabled {
  opacity: .55;
  cursor: not-allowed;
}

.error-message {
  margin-bottom: 10px;
  border-radius: 8px;
  background: #fff1f1;
  color: #9f3030;
  padding: 9px 10px;
  font-size: 13px;
}

.preference-hint {
  margin-bottom: 10px;
  border-radius: 8px;
  background: #eef5f2;
  color: var(--green);
  padding: 9px 10px;
  font-size: 13px;
  font-weight: 600;
}

.thinking {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--muted);
}

.spin-icon {
  animation: spin 1s linear infinite;
  flex-shrink: 0;
}

.loading-line {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--muted);
  font-size: 13px;
}

.loading-line svg,
.send-button svg:first-child {
  animation: spin 1s linear infinite;
}

.center {
  width: 100%;
  justify-content: center;
  padding: 30px 0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 920px) {
  .assistant-page {
    grid-template-columns: 1fr;
    height: auto;
  }

  .conversation-panel {
    max-height: 260px;
  }

  .chat-panel {
    min-height: 680px;
  }

  .from-user .message-content,
  .from-assistant .message-content {
    max-width: 100%;
  }
}
</style>
