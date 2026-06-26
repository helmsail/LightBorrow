<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  send: [content: string]
}>()

const disabled = defineModel<boolean>('disabled', { required: true })
const input = ref('')

function handleSend() {
  const text = input.value.trim()
  if (!text || disabled.value) return
  emit('send', text)
  input.value = ''
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}
</script>

<template>
  <div class="chat-input">
    <textarea
      v-model="input"
      class="chat-input-textarea"
      placeholder="输入消息..."
      :disabled="disabled"
      maxlength="4000"
      rows="1"
      @keydown="onKeydown"
    />
    <button
      class="chat-input-btn"
      :disabled="disabled || !input.trim()"
      @click="handleSend"
    >
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
        stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="22" y1="2" x2="11" y2="13" />
        <polygon points="22 2 15 22 11 13 2 9 22 2" />
      </svg>
    </button>
  </div>
</template>

<style scoped>
.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid var(--color-border);
}

.chat-input-textarea {
  flex: 1;
  resize: none;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 15px;
  line-height: 1.5;
  outline: none;
  font-family: inherit;
  max-height: 120px;
  transition: border-color 0.2s;
}

.chat-input-textarea:focus {
  border-color: var(--color-primary);
}

.chat-input-textarea:disabled {
  background: #fafafa;
  cursor: not-allowed;
}

.chat-input-btn {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: var(--color-primary);
  color: #fff;
  cursor: pointer;
  transition: opacity 0.2s;
}

.chat-input-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.chat-input-btn:not(:disabled):hover {
  opacity: 0.85;
}
</style>
