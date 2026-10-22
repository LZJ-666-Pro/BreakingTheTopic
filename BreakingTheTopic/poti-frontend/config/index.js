// 环境配置 - 小程序环境
// 当前模式：公网 cpolar HTTPS 隧道
// 注意：cpolar 免费版重启后子域名可能变化，变更后用 bash use-public-url.sh 自动探测更新
const base = 'https://420453e0.r16.cpolar.top'

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
