/** 聊天消息 */
export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

/** 发送消息请求 */
export interface ChatRequest {
  userId: string
  sessionId?: string
  content: string
}

/** 聊天回复 */
export interface ChatResponse {
  type: 'final' | 'question' | 'confirm' | 'error'
  content: string
}
