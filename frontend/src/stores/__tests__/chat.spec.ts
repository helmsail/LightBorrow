import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChatStore } from '@/stores/chat'

describe('chat store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should create a session on first access', () => {
    const store = useChatStore()
    store.ensureSession()
    expect(store.sessions).toHaveLength(1)
    expect(store.activeSession).toBeDefined()
    expect(store.activeSessionId).toBeTruthy()
  })

  it('should add a user message to the active session', () => {
    const store = useChatStore()
    store.ensureSession()
    store.addMessage('user', 'hello')
    expect(store.activeSession!.messages).toHaveLength(1)
    expect(store.activeSession!.messages[0].role).toBe('user')
    expect(store.activeSession!.messages[0].content).toBe('hello')
  })

  it('should add an assistant message', () => {
    const store = useChatStore()
    store.ensureSession()
    store.addMessage('assistant', 'hi')
    expect(store.activeSession!.messages).toHaveLength(1)
    expect(store.activeSession!.messages[0].role).toBe('assistant')
  })

  it('should generate unique message IDs', () => {
    const store = useChatStore()
    store.ensureSession()
    store.addMessage('user', 'msg1')
    store.addMessage('assistant', 'msg2')
    expect(store.activeSession!.messages[0].id).not.toBe(store.activeSession!.messages[1].id)
  })

  it('should set timestamp on each message', () => {
    const store = useChatStore()
    store.ensureSession()
    store.addMessage('user', 'hello')
    expect(store.activeSession!.messages[0].timestamp).toBeGreaterThan(0)
  })

  it('should clear messages in the active session', () => {
    const store = useChatStore()
    store.ensureSession()
    store.addMessage('user', 'hello')
    store.addMessage('assistant', 'world')
    store.clearMessages()
    expect(store.activeSession!.messages).toHaveLength(0)
  })

  it('should support multiple sessions', () => {
    const store = useChatStore()
    store.ensureSession()
    const s1 = store.activeSessionId
    store.addMessage('user', 'in session 1')

    store.createSession()
    const s2 = store.activeSessionId
    store.addMessage('user', 'in session 2')

    expect(s1).not.toBe(s2)
    expect(store.sessions).toHaveLength(2)

    // Switch back to session 1
    store.switchSession(s1)
    expect(store.activeSession!.messages).toHaveLength(1)
    expect(store.activeSession!.messages[0].content).toBe('in session 1')
  })
})
