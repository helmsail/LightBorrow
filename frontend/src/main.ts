import { createApp } from 'vue'
import type { ComponentPublicInstance } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import 'element-plus/theme-chalk/el-message-box.css'
import 'element-plus/theme-chalk/el-message.css'
import './assets/main.css'

const app = createApp(App)

// 全局错误处理
app.config.errorHandler = (
  err: unknown,
  _instance: ComponentPublicInstance | null,
  info: string,
) => {
  console.error('[Global Error]', err, info)
}

app.use(createPinia())
app.use(router)
app.mount('#app')
