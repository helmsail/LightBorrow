import axios, { type AxiosResponse } from 'axios'
import type { ChatRequest, ChatResponse } from '@/types/chat'

/** 统一后端返回结构 */
interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

const http = axios.create({
  baseURL: '/api',
  timeout: 60_000,
})

/** 响应拦截器：自动解包 ApiResult.data，非 200 码时 reject */
http.interceptors.response.use(
  (response: AxiosResponse) => {
    const body = response.data as ApiResult<unknown>
    // 后端统一返回 { code, msg, data } 结构
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code !== 200) {
        const err = new Error(body.msg || '请求失败')
        if (body.code === 429) {
          err.name = 'RateLimitError'
        }
        return Promise.reject(err)
      }
      // 将 data 替换为 ApiResult 层级下的实际数据，后续 .data 拿到的就是泛型 T
      response.data = body.data
    }
    return response
  },
  (error: { code?: string; message?: string }) => {
    // 网络错误或超时
    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请检查网络连接'))
    }
    return Promise.reject(new Error(error.message || '网络错误'))
  },
)

/** 发送聊天消息 */
export async function sendMessage(req: ChatRequest): Promise<ChatResponse> {
  const res = await http.post<ChatResponse>('/chat', req)
  return res.data
}
