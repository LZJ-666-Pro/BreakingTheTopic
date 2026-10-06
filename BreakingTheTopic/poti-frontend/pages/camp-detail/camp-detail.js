const app = getApp()
const guestUtils = require('../../utils/guest.js')

const OUTLINE_PREVIEW_COUNT = 7

Page({
  data: {
    campId: '',
    camp: null,
    loading: true,
    outlineExpanded: false,
    outlineVisible: [],
    joining: false
  },

  onLoad(options) {
    this.setData({ campId: options.campId || '' })
  },

  onShow() {
    if (guestUtils.checkGuest()) {
      return
    }
    this.loadDetail()
  },

  loadDetail() {
    this.setData({ loading: true })
    wx.request({
      url: `${app.globalData.interviewUrl}/camp/detail/${this.data.campId}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          const camp = res.data.data || {}
          camp.joinText = this.formatJoin(camp.joinCount)
          this.setData({
            camp,
            loading: false,
            outlineVisible: this.sliceOutline(camp.outline || [], false)
          })
        } else {
          this.setData({ loading: false })
          wx.showToast({ title: res.data.msg || '加载失败', icon: 'none' })
        }
      },
      fail: () => {
        this.setData({ loading: false })
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  formatJoin(count) {
    if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
    if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
    return String(count || 0)
  },

  sliceOutline(outline, expanded) {
    return expanded ? outline : outline.slice(0, OUTLINE_PREVIEW_COUNT)
  },

  toggleOutline() {
    const expanded = !this.data.outlineExpanded
    this.setData({
      outlineExpanded: expanded,
      outlineVisible: this.sliceOutline(this.data.camp.outline || [], expanded)
    })
  },

  joinCamp() {
    if (this.data.joining) return
    // 已加入：直接进入训练营
    if (this.data.camp && this.data.camp.joined) {
      wx.navigateTo({ url: `/pages/camp-home/camp-home?campId=${this.data.campId}` })
      return
    }
    this.setData({ joining: true })
    wx.showLoading({ title: '加入中...' })
    wx.request({
      url: `${app.globalData.interviewUrl}/camp/join`,
      method: 'POST',
      data: { campId: this.data.campId },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          wx.showToast({ title: '加入成功', icon: 'success' })
          setTimeout(() => {
            wx.navigateTo({ url: `/pages/camp-home/camp-home?campId=${this.data.campId}` })
          }, 600)
        } else {
          wx.showToast({ title: res.data.msg || '加入失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '网络错误，请重试', icon: 'none' })
      },
      complete: () => {
        this.setData({ joining: false })
      }
    })
  }
})
