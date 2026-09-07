const app = getApp()
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    loading: true,
    answers: []
  },

  onLoad() {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '我的回答功能需要登录后才能使用',
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
    this.loadMyAnswers()
  },

  loadMyAnswers() {
    wx.request({
      url: `${app.globalData.baseUrl}/discussion/my-answers`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          const answers = (res.data.data || []).map(item => ({
            ...item,
            createTime: this.formatTime(item.createTime)
          }))
          this.setData({ answers, loading: false })
        } else {
          this.setData({ loading: false })
        }
      },
      fail: () => {
        this.setData({ loading: false })
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    })
  },

  formatTime(timeStr) {
    if (!timeStr) return ''
    
    const date = new Date(timeStr)
    const now = new Date()
    const diff = now - date
    
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
    
    return `${date.getMonth() + 1}月${date.getDate()}日`
  },

  goToQuestion(e) {
    const questionId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/question-detail/question-detail?id=${questionId}`
    })
  }
})
