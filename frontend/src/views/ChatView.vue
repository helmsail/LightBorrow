<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import ChatMessage from '@/components/ChatMessage.vue'
import ChatInput from '@/components/ChatInput.vue'
import { useChatStore } from '@/stores/chat'
import { useSession } from '@/composables/useSession'

const { employeeId, employeeName, setEmployee, clearEmployee } = useSession()
const store = useChatStore()
const loading = ref(false)
const messagesRef = ref<HTMLDivElement>()
const showSidebar = ref(false)
const showLoginDialog = ref(true)
const inputId = ref('')
const inputName = ref('')

const loggedIn = computed(() => !!employeeId.value)
const messages = computed(() => store.activeSession?.messages ?? [])

function handleLogin() {
  const id = inputId.value.trim()
  const name = inputName.value.trim()
  if (!id || !name) return
  setEmployee(id, name)
  showLoginDialog.value = false
}

function handleLogout() {
  clearEmployee()
  showLoginDialog.value = true
}

function handleNewSession() {
  const sid = store.createSession()
  showSidebar.value = false
  initSessionMessages(sid)
}

function handleDeleteSession(id: string) {
  store.deleteSession(id)
}

function handleSwitchSession(id: string) {
  store.switchSession(id)
  showSidebar.value = false
}

onMounted(() => {
  if (loggedIn.value) {
    showLoginDialog.value = false
    store.ensureSession()
    const sid = store.activeSessionId
    if (store.activeSession?.messages.length === 0) {
      initSessionMessages(sid)
    }
  }
})

function initSessionMessages(sessionId: string) {
  store.addMessage('assistant', `你好 ${employeeName.value}！我是 IT 资产助手「小灯」，可以帮你查询、借用、转借 IT 资产。请输入你的需求。`, sessionId)
}

async function handleSend(text: string) {
  const sid = store.activeSessionId
  if (!sid) return

  store.addMessage('user', text, sid)
  loading.value = true
  scrollToBottom()

  try {
    const url = new URL('/api/v1/chat/stream', window.location.origin)
    url.searchParams.set('userId', employeeId.value)
    url.searchParams.set('sessionId', sid)
    url.searchParams.set('content', text)

    const eventSource = new EventSource(url.toString())
    let finalContent = ''

    eventSource.addEventListener('progress', (event: MessageEvent) => {
      finalContent = event.data
    })

    eventSource.onerror = () => {
      eventSource.close()
      if (finalContent) {
        store.addMessage('assistant', finalContent, sid)
      } else {
        store.addMessage('assistant', '系统处理失败，请稍后再试。', sid)
      }
      loading.value = false
      scrollToBottom()
    }
  } catch (e: unknown) {
    const message = e instanceof Error ? e.message : '系统处理失败，请稍后再试。'
    store.addMessage('assistant', `系统错误：${message}`, sid)
    loading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick().then(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function handleClear() {
  if (store.activeSession) {
    store.clearMessages()
    store.addMessage('assistant', '对话已清空，请重新输入你的需求。', store.activeSessionId)
    scrollToBottom()
  }
}
</script>

<template>
  <div class="chat-view">
    <!-- 登录对话框 -->
    <el-dialog v-model="showLoginDialog" title="LightBorrow" width="360px" :close-on-click-modal="false" :show-close="false" align-center>
      <div class="login-desc">IT 资产助手</div>
      <el-form @submit.prevent="handleLogin">
        <el-input v-model="inputId" placeholder="请输入工号" maxlength="20" class="login-input" />
        <el-input v-model="inputName" placeholder="请输入姓名" maxlength="20" class="login-input" />
        <el-button type="primary" :disabled="!inputId.trim() || !inputName.trim()" @click="handleLogin" class="login-btn">进入</el-button>
      </el-form>
    </el-dialog>

    <!-- 会话侧栏 -->
    <el-drawer v-model="showSidebar" title="会话列表" direction="ltr" size="280px">
      <template #default>
        <el-button type="primary" class="sidebar-new-btn" @click="handleNewSession">+ 新建对话</el-button>
        <el-scrollbar class="sidebar-list">
          <div
            v-for="s in store.sessions"
            :key="s.id"
            class="sidebar-item"
            :class="{ 'sidebar-item--active': s.id === store.activeSessionId }"
            @click="handleSwitchSession(s.id)"
          >
            <span class="sidebar-item-title">{{ s.title }}</span>
            <el-button text type="danger" size="small" @click.stop="handleDeleteSession(s.id)">x</el-button>
          </div>
        </el-scrollbar>
      </template>
    </el-drawer>

    <!-- 主界面 -->
    <template v-if="loggedIn">
      <!-- 头部 -->
      <header class="chat-header">
        <div class="chat-header-inner">
          <el-button text @click="showSidebar = !showSidebar" title="会话列表">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
          </el-button>
          <h1 class="chat-header-title">LightBorrow</h1>
          <span class="chat-header-subtitle">IT 资产助手</span>
          <el-dropdown trigger="click" @command="handleLogout">
            <span class="chat-header-user">{{ employeeName }}({{ employeeId }})</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button text @click="handleNewSession" title="新建对话">+</el-button>
          <el-button text @click="handleClear" title="清空当前对话">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
          </el-button>
        </div>
      </header>

      <!-- 消息列表 -->
      <div ref="messagesRef" class="chat-messages">
        <ChatMessage v-for="msg in messages" :key="msg.id" :message="msg" />
        <div v-if="loading" class="loading-placeholder">
          <div class="loading-avatar">🤖</div>
          <div class="loading-bubble"><span class="loading-dots"><span>.</span><span>.</span><span>.</span></span></div>
        </div>
      </div>

      <!-- 输入框 -->
      <ChatInput :disabled="loading" @send="handleSend" />
    </template>
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
  position: relative;
}

.login-desc {
  text-align: center;
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}

.login-input {
  margin-bottom: 12px;
}

.login-btn {
  width: 100%;
}

.sidebar-new-btn {
  width: calc(100% - 32px);
  margin: 0 16px 12px;
}

.sidebar-list {
  padding: 0 12px;
  height: calc(100% - 60px);
}

.sidebar-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 2px;
  transition: background 0.15s;
}

.sidebar-item:hover {
  background: #e9ecef;
}

.sidebar-item--active {
  background: #dbeafe;
  font-weight: 600;
}

.sidebar-item-title {
  flex: 1;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-header {
  border-bottom: 1px solid var(--el-border-color);
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}

.chat-header-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 800px;
  margin: 0 auto;
  padding: 14px 16px;
  position: relative;
}

.chat-header-title {
  font-size: 18px;
  font-weight: 600;
}

.chat-header-subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  flex: 1;
}

.chat-header-user {
  font-size: 12px;
  color: var(--el-color-primary);
  background: #e8f4ff;
  padding: 2px 8px;
  border-radius: 4px;
  white-space: nowrap;
  cursor: pointer;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.loading-placeholder {
  display: flex;
  gap: 10px;
  max-width: 800px;
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
  border: 1px solid var(--el-border-color);
  border-radius: 12px 12px 12px 4px;
  padding: 10px 16px;
}

.loading-dots {
  display: inline-flex;
  gap: 2px;
  font-size: 18px;
  color: #666;
}

.loading-dots span {
  animation: dotPulse 1.4s infinite;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dotPulse {
  0%, 60%, 100% { opacity: 0.3; }
  30% { opacity: 1; }
}
</style>
