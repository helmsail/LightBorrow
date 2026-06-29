<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import type { ChatMessage } from '@/types/chat'

const props = defineProps<{
  message: ChatMessage
}>()

const isUser = computed(() => props.message.role === 'user')

const renderedContent = computed(() => {
  if (isUser.value) return props.message.content
  return marked.parse(props.message.content, { async: false }) as string
})

function formatTime(ts: number): string {
  const d = new Date(ts)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<template>
  <div class="message" :class="{ 'message--user': isUser, 'message--assistant': !isUser }">
    <div class="message-avatar">
      {{ isUser ? '👤' : '🤖' }}
    </div>
    <div class="message-body">
      <div class="message-bubble">
        <div v-if="isUser" class="message-text">{{ message.content }}</div>
        <div v-else class="message-markdown" v-html="renderedContent"></div>
      </div>
      <span class="message-time">{{ formatTime(message.timestamp) }}</span>
    </div>
  </div>
</template>

<style scoped>
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 16px;
}

.message--user {
  flex-direction: row-reverse;
}

.message--user .message-bubble {
  background: var(--el-color-primary);
  color: #fff;
  border-radius: 12px 12px 4px 12px;
}

.message--assistant .message-bubble {
  background: #fff;
  border: 1px solid var(--el-border-color);
  border-radius: 12px 12px 12px 4px;
}

.message-avatar {
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

.message-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.message--user .message-body {
  align-items: flex-end;
}

.message-bubble {
  padding: 10px 16px;
  line-height: 1.6;
  font-size: 15px;
  word-break: break-word;
}

.message-text {
  margin: 0;
  white-space: pre-wrap;
}

.message-markdown {
  line-height: 1.7;
  white-space: normal;
}

.message-markdown :deep(p) {
  margin: 0 0 8px;
}

.message-markdown :deep(p:last-child) {
  margin-bottom: 0;
}

.message-markdown :deep(ul),
.message-markdown :deep(ol) {
  padding-left: 20px;
  margin: 4px 0;
}

.message-markdown :deep(li) {
  margin: 2px 0;
}

.message-markdown :deep(code) {
  background: #f4f4f4;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Menlo', 'Monaco', monospace;
}

.message-markdown :deep(pre) {
  background: #f4f4f4;
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-markdown :deep(pre code) {
  background: none;
  padding: 0;
}

.message-markdown :deep(strong) {
  font-weight: 600;
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  padding: 0 4px;
}
</style>
