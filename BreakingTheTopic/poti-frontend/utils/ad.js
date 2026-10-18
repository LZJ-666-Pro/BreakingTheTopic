/**
 * 广告工具（微信流量主预留方案）
 * 页面接入方式：
 *   js:   const ad = require('../../utils/ad.js'); data 里放 bannerAd: ad.getBannerState()
 *   wxml: <view wx:if="{{bannerAd.show}}"> 占位或 <ad unit-id="{{bannerAd.unitId}}" binderror="onAdError"/> </view>
 * 展示后调用 ad.recordBannerShow() 计入每日频次。
 */
const adConfig = require('../config/ad.js')

const SHOW_COUNT_KEY_PREFIX = 'adShowCount_'

/** VIP 用户免广告（去广告权益）：读取我的页/会员页写入的缓存 */
function isVip() {
  try {
    return wx.getStorageSync('vipStatusCache') === true
  } catch (e) {
    return false
  }
}

/** 新用户免广告：首次启动 7 天内不展示 */
function isNewUser() {
  try {
    let first = wx.getStorageSync('firstLaunchDate')
    if (!first) {
      first = formatToday()
      wx.setStorageSync('firstLaunchDate', first)
    }
    const diffDays = (Date.now() - new Date(first.replace(/-/g, '/')).getTime()) / 86400000
    return diffDays < adConfig.newUserFreeDays
  } catch (e) {
    return false
  }
}

function formatToday() {
  const d = new Date()
  const m = d.getMonth() + 1
  const day = d.getDate()
  return d.getFullYear() + '-' + (m < 10 ? '0' + m : m) + '-' + (day < 10 ? '0' + day : day)
}

/** 今日已展示次数 */
function todayCount() {
  try {
    return wx.getStorageSync(SHOW_COUNT_KEY_PREFIX + formatToday()) || 0
  } catch (e) {
    return 0
  }
}

/** 记一次广告触达（按天计数，跨天自动重置） */
function recordShow() {
  try {
    const key = SHOW_COUNT_KEY_PREFIX + formatToday()
    wx.setStorageSync(key, todayCount() + 1)
  } catch (e) { /* 存储异常不影响主流程 */ }
}

/**
 * Banner 广告位展示状态（页面 data 直接使用）
 * @returns {show, preview, unitId}
 *  - 开发期（enabled=false）：previewMode=true 时显示灰色占位条，用于确认插入位置
 *  - 上线后：VIP / 新用户 / 超频次 / 无 unitId 均不展示
 */
function getBannerState() {
  if (!adConfig.enabled || !adConfig.bannerUnitId) {
    return { show: !!adConfig.previewMode, preview: true, unitId: '' }
  }
  if (isVip() || isNewUser() || todayCount() >= adConfig.dailyLimit) {
    return { show: false, preview: false, unitId: '' }
  }
  return { show: true, preview: false, unitId: adConfig.bannerUnitId }
}

/**
 * 激励视频实例（AI 精讲解锁等场景预留）
 * 未启用时返回 null —— 调用方据此直接放行，不拦截用户
 */
function createRewardedAd(onClose) {
  if (!adConfig.enabled || !adConfig.rewardUnitId) {
    return null
  }
  const rewardedAd = wx.createRewardedAd({ adUnitId: adConfig.rewardUnitId })
  if (onClose) {
    rewardedAd.onClose(onClose)
  }
  return rewardedAd
}

/** VIP 状态缓存写入（在「我的」页与会员中心加载会员状态成功后调用） */
function cacheVipStatus(isVipUser) {
  try {
    wx.setStorageSync('vipStatusCache', isVipUser === true)
  } catch (e) { /* 忽略 */ }
}

module.exports = {
  getBannerState,
  recordShow,
  createRewardedAd,
  cacheVipStatus,
  isVip
}
