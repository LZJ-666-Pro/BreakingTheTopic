import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000
})

// 解析 JWT payload 的过期时间戳（秒）
function getJwtExp(token) {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')))
    return payload.exp || null
  } catch (e) {
    return null
  }
}

// JWT 无感刷新（滑动续期）：剩余有效期不足 12h 时换发新 token，后台操作期间不掉线；
// 已过期则刷新被拒，返回旧 token 让业务请求自然收到 401 走统一登出流程。
// 共享 Promise 防止并发请求触发多次刷新；用原生 axios 避免再次经过本文件拦截器。
let refreshingPromise = null
async function ensureFreshToken() {
  const token = localStorage.getItem('admin_token')
  if (!token) return null

  const exp = getJwtExp(token)
  if (!exp) return token

  const remainMs = exp * 1000 - Date.now()
  if (remainMs > 12 * 3600 * 1000) return token

  if (!refreshingPromise) {
    refreshingPromise = axios
      .post('/api/admin/refresh', null, {
        headers: { Authorization: `Bearer ${token}` }
      })
      .then((res) => {
        const data = res.data
        if (data && data.code === 200 && data.data && data.data.token) {
          localStorage.setItem('admin_token', data.data.token)
          return data.data.token
        }
        return token
      })
      .catch(() => token)
      .finally(() => {
        refreshingPromise = null
      })
  }
  return refreshingPromise
}

api.interceptors.request.use(
  async (config) => {
    const token = await ensureFreshToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求构造失败：', error)
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => {
    // 文件流（CSV 导出等）直接返回完整 response，不做 code 校验
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    console.error('响应错误：', error)
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_user')
      // 死循环防护：登录页自身的白名单请求（如验证码开关）401 时不跳转，
      // 否则 window.location 跳登录 → 页面重载 → 再 401 → 无限刷新
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    ElMessage.error(error.response?.data?.msg || '网络错误')
    return Promise.reject(error)
  }
)

export default api
