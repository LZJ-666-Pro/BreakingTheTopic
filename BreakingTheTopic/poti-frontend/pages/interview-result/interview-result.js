const app = getApp()

Page({
  data: {
    interviewId: null,
    result: {
      score: 0,
      totalQuestions: 0,
      correctCount: 0,
      spendSeconds: 0,
      aiFeedback: ''
    },
    details: [],
    scoreLevel: '',
    scoreText: ''
  },

  onLoad(options) {
    this.setData({ interviewId: options.interviewId })
    this.loadResult()
  },

  loadResult() {
    wx.showLoading({ title: '加载中...' })

    wx.request({
      url: `${app.globalData.interviewUrl}/interview/detail/${this.data.interviewId}`,
      method: 'GET',
      header: { 'X-User-Id': app.globalData.userId || 1 },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          const data = res.data.data
          const spendSeconds = data.spendSeconds || 0
          this.setData({
            result: {
              score: data.score || 0,
              totalQuestions: data.totalQuestions || 0,
              correctCount: data.correctCount || 0,
              spendSeconds: spendSeconds,
              spendTime: this.formatTime(spendSeconds),
              aiFeedback: data.aiFeedback || ''
            },
            details: data.details || []
          })
          this.calculateScoreLevel()
        } else {
          wx.showToast({ title: res.data.msg || '加载失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  calculateScoreLevel() {
    const score = this.data.result.score
    let level = ''
    let text = ''

    if (score >= 90) {
      level = 'excellent'
      text = '优秀'
    } else if (score >= 80) {
      level = 'good'
      text = '良好'
    } else if (score >= 60) {
      level = 'pass'
      text = '及格'
    } else {
      level = 'fail'
      text = '需努力'
    }

    this.setData({ scoreLevel: level, scoreText: text })
  },

  formatTime(seconds) {
    if (!seconds) return '0分'
    const min = Math.floor(seconds / 60)
    const sec = seconds % 60
    if (min > 0) {
      return `${min}分${sec}秒`
    }
    return `${sec}秒`
  },

  retryInterview() {
    wx.navigateBack()
  },

  goBack() {
    wx.switchTab({ url: '/pages/index/index' })
  }
})
