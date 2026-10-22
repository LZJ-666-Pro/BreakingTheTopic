/**
 * 媒体地址工具
 *
 * 后端只存相对路径（/avatars/xxx.png、/uploads/xxx.png），由同源代理转发到后端。
 * 这里额外加一个版本参数：当历史缓存里存了错误内容（例如某路径曾被 SPA 回退成
 * index.html）时，加上 ?v=N 就能强制浏览器重新拉取，避免一直显示破图。
 *
 * 升级媒体内容后，把 MEDIA_VERSION 加 1 即可让所有客户端失效旧缓存。
 */
export const MEDIA_VERSION = 2

export function mediaUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const u = url.trim()
  if (!u) return ''
  // 第三方绝对地址（如微信头像 CDN）原样返回
  if (/^https?:\/\//i.test(u)) return u
  const sep = u.indexOf('?') >= 0 ? '&' : '?'
  return `${u}${sep}v=${MEDIA_VERSION}`
}
