<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import type { ChatMessage } from '@/types/chat'

const props = defineProps<{
  message: ChatMessage
}>()

const bubbleRef = ref<HTMLDivElement>()

watch(
  () => props.message.content,
  async () => {
    await nextTick()
    if (bubbleRef.value) {
      bubbleRef.value.scrollIntoView({ behavior: 'smooth' })
    }
  },
)

const isUser = () => props.message.role === 'user'

function formatTime(ts: number): string {
  const d = new Date(ts)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<template>
  <div class="message" :class="{ 'message--user': isUser(), 'message--assistant': !isUser() }">
    <div class="message-avatar">
      {{ isUser() ? '👤' : '🤖' }}
    </div>
    <div class="message-body">
      <div ref="bubbleRef" class="message-bubble">
        <p class="message-text">{{ message.content }}</p>
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
  max-width: var(--max-width);
  margin-left: auto;
  margin-right: auto;
  padding: 0 16px;
}

.message--user {
  flex-direction: row-reverse;
}

.message--user .message-bubble {
  background: var(--color-bg-user);
  color: #fff;
  border-radius: var(--radius) var(--radius) 4px var(--radius);
}

.message--assistant .message-bubble {
  background: var(--color-bg-assistant);
  border: 1px solid var(--color-border);
  border-radius: var(--radius) var(--radius) var(--radius) 4px;
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
  white-space: pre-wrap;
}

.message-text {
  margin: 0;
}

.message-time {
  font-size: 12px;
  color: var(--color-text-secondary);
  padding: 0 4px;
}
</style>
