import { ref } from 'vue'

const STORAGE_KEY = 'lightborrow_session_id'

/** 生成 UUID v4 */
function generateUuid(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  // fallback: 兼容不支持 crypto.randomUUID 的浏览器
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/** 会话 ID，首次访问自动生成并持久化到 localStorage */
const sessionId = ref<string>('')

function initSession(): string {
  let id = localStorage.getItem(STORAGE_KEY)
  if (!id) {
    id = generateUuid()
    localStorage.setItem(STORAGE_KEY, id)
  }
  sessionId.value = id
  return id
}

// 初始化
initSession()

/**
 * 访问当前会话 ID。用户唯一标识，用于后端会话隔离。
 * 同一浏览器关闭再打开会自动恢复。
 */
export function useSession() {
  return {
    sessionId,
  }
}
