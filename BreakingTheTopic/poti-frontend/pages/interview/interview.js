const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    interviewTypes: [],
    statistics: {
      totalInterviews: 0,
      totalQuestions: 0,
      accuracy: 0
    },
    isStarting: false
  },

  onLoad() {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '模拟面试功能需要登录后才能使用',
        confirmText: '去登录',
        cancelText: '返回',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/login/login'
            })
          } else {
            wx.navigateBack({
              fail: () => {
                wx.switchTab({
                  url: '/pages/index/index'
                })
              }
            })
          }
        }
      })
      return
    }
    this.loadTypes()
    this.loadStatistics()
  },

  onShow() {
    if (guestUtils.checkGuest()) {
      return
    }
    this.loadStatistics()
  },

  loadTypes() {
    const app = getApp()
    
    wx.request({
      url: `${app.globalData.interviewUrl}/interview/types`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data && res.data.data.length > 0) {
          this.setData({
            interviewTypes: res.data.data
          })
        }
      }
    })
  },

  loadStatistics() {
    const app = getApp()
    if (!app.globalData.token) {
      return
    }
    
    wx.request({
      url: `${app.globalData.interviewUrl}/interview/statistics`,
      method: 'GET',
      header: {
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            statistics: res.data.data
          })
        }
      }
    })
  },

  startInterview(e) {
    if (this.data.isStarting) return
    
    const type = e.currentTarget.dataset.type
    const app = getApp()
    
    if (!app.globalData.token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    this.setData({ isStarting: true })
    
    wx.navigateTo({
      url: `/pages/interview-answer/interview-answer?type=${type}`,
      complete: () => {
        setTimeout(() => {
          this.setData({ isStarting: false })
        }, 500)
      }
    })
  },

  viewHistory() {
    wx.navigateTo({
      url: '/pages/interview-history/interview-history'
    })
  }
})
