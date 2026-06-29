import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '@/types/chat'

function generateSessionId(): string {
  return 'sess-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 6)
}

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<{ id: string; title: string; messages: ChatMessage[] }[]>([])
  const activeSessionId = ref<string>('')
  let nextId = 1

  const activeSession = ref<{ id: string; title: string; messages: ChatMessage[] }>()

  function createSession() {
    const id = generateSessionId()
    const session = { id, title: `对话 ${sessions.value.length + 1}`, messages: [] }
    sessions.value.push(session)
    activeSessionId.value = id
    activeSession.value = session
    return id
  }

  function switchSession(id: string) {
    const session = sessions.value.find((s) => s.id === id)
    if (session) {
      activeSessionId.value = id
      activeSession.value = session
    }
  }

  function deleteSession(id: string) {
    const idx = sessions.value.findIndex((s) => s.id === id)
    if (idx === -1) return
    sessions.value.splice(idx, 1)
    if (activeSessionId.value === id) {
      if (sessions.value.length > 0) {
        switchSession(sessions.value[0].id)
      } else {
        activeSessionId.value = ''
        activeSession.value = undefined
      }
    }
  }

  function addMessage(role: 'user' | 'assistant', content: string, sessionId?: string) {
    const sid = sessionId || activeSessionId.value
    const session = sessions.value.find((s) => s.id === sid)
    if (!session) return

    const msg: ChatMessage = {
      id: `msg-${nextId++}`,
      role,
      content,
      timestamp: Date.now(),
    }
    session.messages.push(msg)

    if (session.messages.length === 1 && role === 'assistant') {
      session.title = content.slice(0, 20) + (content.length > 20 ? '...' : '')
    }
  }

  function clearMessages(sessionId?: string) {
    const sid = sessionId || activeSessionId.value
    const session = sessions.value.find((s) => s.id === sid)
    if (session) session.messages = []
  }

  // 确保至少有一个会话
  function ensureSession() {
    if (sessions.value.length === 0) {
      createSession()
    } else if (!activeSession.value) {
      activeSession.value = sessions.value[0]
      activeSessionId.value = sessions.value[0].id
    }
  }

  return {
    sessions,
    activeSessionId,
    activeSession,
    createSession,
    switchSession,
    deleteSession,
    addMessage,
    clearMessages,
    ensureSession,
  }
})
