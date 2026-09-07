Page({
  data: {
    result: null,
    formatTime: '00:00'
  },

  onLoad() {
    const result = wx.getStorageSync('practiceResult')
    if (result) {
      this.setData({
        result,
        formatTime: this.formatTime(result.elapsedTime)
      })
      wx.removeStorageSync('practiceResult')
    } else {
      wx.showToast({
        title: '数据加载失败',
        icon: 'none'
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }
  },

  formatTime(seconds) {
    if (!seconds && seconds !== 0) return '00:00'
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  },

  retryPractice() {
    const { result } = this.data
    if (result && result.categoryName) {
      const typeMap = {
        'Java': 'java',
        'Python': 'python',
        'MySQL': 'mysql',
        'Redis': 'redis',
        'Spring': 'spring',
        '消息队列': 'mq',
        '操作系统': 'os',
        '计算机网络': 'network',
        'Docker': 'docker',
        'Git': 'git'
      }
      const type = typeMap[result.categoryName] || 'java'
      wx.redirectTo({
        url: `/pages/answer/answer?type=${type}`
      })
    } else {
      wx.redirectTo({
        url: '/pages/answer/answer?type=java'
      })
    }
  },

  goBack() {
    wx.navigateBack()
  }
})
