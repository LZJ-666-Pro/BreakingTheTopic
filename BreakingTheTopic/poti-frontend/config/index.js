// 环境配置 - 小程序环境
// ============================================================
// 当前模式：本地联调（Windows 上 IDEA 启动的微服务）
//   前端 → http://127.0.0.1:8080（网关）→ 各微服务
// 前提：微信开发者工具需勾选「不校验合法域名」（urlCheck 已为 false）
//
// 要切回公网 cpolar 隧道模式：
//   用 config-backup/index.public.cpolar.js 覆盖本文件即可
// ============================================================
const base = 'http://127.0.0.1:8080'

const config = {
  baseUrl: base,
  authUrl: base,
  userUrl: base,
  questionUrl: base,
  practiceUrl: base,
  wrongbookUrl: base,
  favoriteUrl: base,
  interviewUrl: base,
  searchUrl: base,
  agentUrl: base
}

module.exports = config
