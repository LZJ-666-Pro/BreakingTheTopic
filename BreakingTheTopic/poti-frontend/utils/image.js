/**
 * 媒体/静态资源地址归一化工具
 *
 * 背景：
 *   后端现在只存相对路径（如 /avatars/xxx.jpg、/chat/images/xxx.jpg），
 *   老数据里可能残留内网绝对地址（http://10.16.145.198:8200/avatars/xxx.jpg、
 *   http://localhost:8200/...）。小程序里图片必须用当前环境可访问的地址，
 *   否则本地 http 图片会被微信以「不再支持 HTTP 协议」拦掉。
 *
 * 做法：
 *   1. 相对路径  → config/index.js 里的 baseUrl + 路径
 *   2. 内网绝对地址 → 丢掉 host/port，只保留路径，再拼 baseUrl
 *   3. 第三方地址（如 https://thirdwx.qlogo.cn/...）原样保留
 *
 * 使用：
 *   - app.js 会给 wx.request 装一个响应拦截，自动归一化所有接口数据
 *   - 需要手动处理时调用 resolveImageUrl(url)
 */

// 视为「本机/内网」的 host，其地址会被重写为当前 baseUrl + 路径
const INTERNAL_HOSTS = ['10.16.145.198', 'localhost', '127.0.0.1']

// 后端静态资源路径前缀：命中则按相对路径处理
const MEDIA_PREFIXES = ['/avatars/', '/chat/images/', '/chat/voices/', '/uploads/']

function getBaseUrl() {
  try {
    const app = getApp()
    if (app && app.globalData && app.globalData.baseUrl) {
      return app.globalData.baseUrl
    }
  } catch (e) {
    // getApp() 可能尚未就绪
  }
  return ''
}

/**
 * 把任意媒体地址转换成当前环境可访问的绝对地址
 * @param {string} url
 * @returns {string}
 */
function resolveImageUrl(url) {
  if (!url || typeof url !== 'string') return url || ''
  const base = getBaseUrl()
  if (!base) return url

  const u = url.trim()
  if (!u) return u

  // 已经是当前 baseUrl 开头，直接用
  if (u.indexOf(base) === 0) return u

  // http(s)://host[:port][/path]
  const m = /^https?:\/\/([^\/:]+)(?::\d+)?(\/.*)?$/i.exec(u)
  if (m) {
    const host = m[1]
    if (INTERNAL_HOSTS.indexOf(host) >= 0) {
      // 内网地址：保留路径，换成本次运行环境的 baseUrl
      return base + (m[2] || '')
    }
    // 第三方 https 图片（微信头像、CDN 等）原样保留
    return u
  }

  // 相对路径
  if (u.charAt(0) === '/') return base + u
  return u
}

/**
 * 判断一个字符串是否可能是需要归一化的媒体地址
 */
function shouldNormalize(str) {
  if (!str || str.length > 512) return false
  for (let i = 0; i < MEDIA_PREFIXES.length; i++) {
    if (str.indexOf(MEDIA_PREFIXES[i]) === 0) return true
  }
  return /^https?:\/\/(10\.16\.145\.198|localhost|127\.0\.0\.1)(:\d+)?\//i.test(str)
}

/**
 * 递归归一化接口返回的数据（原地修改，深度上限 10 层防止异常结构）
 */
function normalizeDeep(node, depth) {
  const d = depth || 0
  if (!node || d > 10) return node

  if (typeof node === 'string') {
    return shouldNormalize(node) ? resolveImageUrl(node) : node
  }

  if (Array.isArray(node)) {
    for (let i = 0; i < node.length; i++) {
      const v = node[i]
      if (typeof v === 'string') {
        if (shouldNormalize(v)) node[i] = resolveImageUrl(v)
      } else if (v && typeof v === 'object') {
        normalizeDeep(v, d + 1)
      }
    }
    return node
  }

  if (typeof node === 'object') {
    const keys = Object.keys(node)
    for (let i = 0; i < keys.length; i++) {
      const k = keys[i]
      const v = node[k]
      if (typeof v === 'string') {
        if (shouldNormalize(v)) node[k] = resolveImageUrl(v)
      } else if (v && typeof v === 'object') {
        normalizeDeep(v, d + 1)
      }
    }
    return node
  }

  return node
}

module.exports = {
  resolveImageUrl,
  normalizeDeep,
  shouldNormalize
}
