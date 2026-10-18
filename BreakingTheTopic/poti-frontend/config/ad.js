/**
 * 微信广告全局配置（流量主预留方案：预留与展示分离）
 *
 * 开发阶段：enabled=false，页面仅渲染灰色占位（previewMode），不拉取真实广告。
 * 流量主达标（累计独立访客 UV ≥ 500）后：
 *   1. 微信公众平台 → 流量主 → 创建广告位（Banner / 激励视频）
 *   2. 将 unitId 填入下方对应字段
 *   3. enabled 改为 true
 * 广告即刻上线，无需改动任何页面代码。
 */
module.exports = {
  // 广告总开关：false 时任何真实广告都不会拉取/展示
  enabled: false,

  // 开发预览模式：enabled=false 时页面显示灰色占位条，用于确认广告位插入位置；上线前可改 false 隐藏
  previewMode: true,

  // Banner 广告位 ID（公众平台-流量主-Banner广告 创建后填入）
  bannerUnitId: '',

  // 激励视频广告位 ID（用于「AI 精讲」等场景，用户主动观看解锁）
  rewardUnitId: '',

  // 单用户单日广告总触达上限（Banner 计入；激励视频天然由用户主动触发，另计）
  dailyLimit: 5,

  // 新用户免广告天数：培养刷题习惯（以首次启动日计）
  newUserFreeDays: 7
}
