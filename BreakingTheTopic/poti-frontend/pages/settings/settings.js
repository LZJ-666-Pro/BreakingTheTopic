Page({
  data: {
    fontSize: 'normal',
    showAnswer: false,
    cachedCount: 0
  },

  onLoad() {
    this.loadSettings()
    this.loadCachedCount()
  },

  loadSettings() {
    const settings = wx.getStorageSync('practiceSettings') || {}
    this.setData({
      fontSize: settings.fontSize || 'normal',
      showAnswer: settings.showAnswer || false
    })
  },

  loadCachedCount() {
    const cachedQuestions = wx.getStorageSync('cachedQuestions') || []
    this.setData({
      cachedCount: cachedQuestions.length
    })
  },

  setFontSize(e) {
    const size = e.currentTarget.dataset.size
    this.setData({ fontSize: size })
    
    const settings = wx.getStorageSync('practiceSettings') || {}
    settings.fontSize = size
    wx.setStorageSync('practiceSettings', settings)
    
    wx.showToast({
      title: '设置成功',
      icon: 'success'
    })
  },

  toggleShowAnswer(e) {
    const showAnswer = e.detail
    this.setData({ showAnswer })
    
    const settings = wx.getStorageSync('practiceSettings') || {}
    settings.showAnswer = showAnswer
    wx.setStorageSync('practiceSettings', settings)
  },

  clearCache() {
    wx.showModal({
      title: '确认清除',
      content: '清除后将无法在无网络时刷题，是否继续？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('cachedQuestions')
          this.setData({ cachedCount: 0 })
          
          wx.showToast({
            title: '清除成功',
            icon: 'success'
          })
        }
      }
    })
  }
})
