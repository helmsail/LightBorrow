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
  if (e.isComposing) return
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}
</script>

<template>
  <div class="chat-input">
    <el-input
      v-model="input"
      type="textarea"
      :rows="1"
      placeholder="输入消息..."
      :disabled="disabled"
      maxlength="4000"
      resize="none"
      class="chat-input-textarea"
      @keydown="onKeydown"
    />
    <el-button
      type="primary"
      :disabled="disabled || !input.trim()"
      :icon="''"
      class="chat-input-btn"
      @click="handleSend"
    >
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
        stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="22" y1="2" x2="11" y2="13" />
        <polygon points="22 2 15 22 11 13 2 9 22 2" />
      </svg>
    </el-button>
  </div>
</template>

<style scoped>
.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 800px;
  margin: 0 auto;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid var(--el-border-color);
}

.chat-input-textarea {
  flex: 1;
}

.chat-input-textarea :deep(.el-textarea__inner) {
  resize: none;
  min-height: 44px;
  max-height: 120px;
  font-size: 15px;
  line-height: 1.5;
}

.chat-input-btn {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.chat-input-btn :deep(svg) {
  width: 18px;
  height: 18px;
}
</style>
