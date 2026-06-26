<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ChatMessage from '@/components/ChatMessage.vue'
import ChatInput from '@/components/ChatInput.vue'
import { useChatStore } from '@/stores/chat'
import { sendMessage } from '@/api/chat'
import { useSession } from '@/composables/useSession'
import type { ChatMessage as ChatMessageType } from '@/types/chat'

const { sessionId } = useSession()
const store = useChatStore()
const loading = ref(false)
const messagesRef = ref<HTMLDivElement>()

onMounted(() => {
  if (store.messages.length === 0) {
    store.addMessage(
      'assistant',
      '你好！我是 IT 资产助手「小灯」，可以帮你查询、借用、转借 IT 资产。请输入你的需求。',
    )
  }
})

async function handleSend(text: string) {
  store.addMessage('user', text)
  loading.value = true

  try {
    const res = await sendMessage({ userId: sessionId.value, content: text })

    if (res.type === 'error') {
      store.addMessage('assistant', `系统错误：${res.content}`)
    } else {
      store.addMessage('assistant', res.content)
      if (res.type === 'question') {
        store.addMessage('assistant', '（请根据上面的问题补充信息后继续提问）')
      } else if (res.type === 'confirm') {
        store.addMessage('assistant', '（请确认上面的操作，回复"确认"或"取消"）')
      }
    }
  } catch (e: unknown) {
    if (e instanceof Error && e.name === 'RateLimitError') {
      store.addMessage('assistant', e.message)
    } else {
      const message = e instanceof Error ? e.message : '系统处理失败，请稍后再试。'
      store.addMessage('assistant', `系统错误：${message}`)
    }
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  setTimeout(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  }, 50)
}

async function handleClear() {
  store.clearMessages()
  store.addMessage('assistant', '对话已清空，请重新输入你的需求。')
  scrollToBottom()
}
</script>

<template>
  <div class="chat-view">
    <!-- 头部 -->
    <header class="chat-header">
      <div class="chat-header-inner">
        <h1 class="chat-header-title">LightBorrow</h1>
        <span class="chat-header-subtitle">IT 资产助手</span>
        <button class="chat-header-clear" @click="handleClear" title="清空对话">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" />
            <path d="M10 11v6" />
            <path d="M14 11v6" />
          </svg>
        </button>
      </div>
    </header>

    <!-- 消息列表 -->
    <div ref="messagesRef" class="chat-messages">
      <div v-if="store.messages.length === 0" class="chat-empty">
        <p>暂无消息，开始对话吧</p>
      </div>
      <ChatMessage
        v-for="msg in store.messages"
        :key="msg.id"
        :message="msg"
      />
      <!-- 加载中占位 -->
      <div v-if="loading" class="loading-placeholder">
        <div class="loading-avatar">🤖</div>
        <div class="loading-bubble">
          <span class="loading-dots">思考中</span>
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <ChatInput :disabled="loading" @send="handleSend" />
  </div>
</template>

<style scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.05);
}

/* 头部 */
.chat-header {
  border-bottom: 1px solid var(--color-border);
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}

.chat-header-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 14px 16px;
}

.chat-header-title {
  font-size: 18px;
  font-weight: 600;
}

.chat-header-subtitle {
  font-size: 13px;
  color: var(--color-text-secondary);
  flex: 1;
}

.chat-header-clear {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}

.chat-header-clear:hover {
  background: #f5f5f5;
  color: #333;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.chat-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--color-text-secondary);
  font-size: 14px;
}

/* 加载占位 */
.loading-placeholder {
  display: flex;
  gap: 10px;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 16px;
}

.loading-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: #f0f0f0;
  border-radius: 50%;
}

.loading-bubble {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius) var(--radius) var(--radius) 4px;
  padding: 10px 16px;
}

/* 加载动画 */
.loading-dots {
  display: inline-block;
  animation: pulse 1.4s infinite;
  font-size: 14px;
  color: #666;
}

@keyframes pulse {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}
</style>
