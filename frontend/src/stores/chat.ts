import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '@/types/chat'

/**
 * 聊天状态管理。
 * 支持添加消息、清空对话、消息 ID 自增。
 */
export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])

  let nextId = 1

  function addMessage(role: 'user' | 'assistant', content: string) {
    messages.value.push({
      id: `msg-${nextId++}`,
      role,
      content,
      timestamp: Date.now(),
    })
  }

  function clearMessages() {
    messages.value = []
  }

  return { messages, addMessage, clearMessages }
})
