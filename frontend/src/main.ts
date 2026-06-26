import { createApp } from 'vue'
import type { ComponentPublicInstance } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)

// 全局错误处理
app.config.errorHandler = (
  err: unknown,
  instance: ComponentPublicInstance | null,
  info: string,
) => {
  console.error('[Global Error]', err, info)
}

// 全局警告处理
app.config.warnHandler = (
  msg: string,
  instance: ComponentPublicInstance | null,
  trace: string,
) => {
  console.warn('[Global Warn]', msg, trace)
}

app.use(createPinia())
app.use(router)
app.mount('#app')
