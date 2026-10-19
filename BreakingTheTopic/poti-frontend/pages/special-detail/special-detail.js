const app = getApp()
const { questionUrl } = app.globalData

Page({
  onPullDownRefresh() {
    if (typeof guestUtils !== 'undefined' && guestUtils.checkGuest && guestUtils.checkGuest()) { wx.stopPullDownRefresh(); return }
    this.loadDetail()
    wx.stopPullDownRefresh()
  },
  data: {
    id: null,
    info: null,
    questions: [],
    loading: true
  },

  onLoad(options) {
    this.setData({ id: options.id })
  },

  onShow() {
    if (this.data.id) {
      this.loadDetail()
    }
  },

  loadDetail() {
    const token = app.globalData.token
    wx.request({
      url: `${questionUrl}/question/special/${this.data.id}/questions`,
      method: 'GET',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        const data = (res.data && res.data.data) || null
        if (data) {
          this.setData({
            info: data,
            questions: data.questions || [],
            loading: false
          })
          wx.setNavigationBarTitle({ title: data.name || '专题详情' })
        } else {
          this.setData({ loading: false })
          wx.showToast({ title: res.data.msg || '专题不存在', icon: 'none' })
        }
      },
      fail: () => {
        this.setData({ loading: false })
        wx.showToast({ title: '加载失败，请重试', icon: 'none' })
      }
    })
  },

  goQuestion(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/question-detail/question-detail?id=${id}` })
  }
})
