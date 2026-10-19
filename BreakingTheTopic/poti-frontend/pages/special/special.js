const app = getApp()
const { questionUrl } = app.globalData

Page({
  onPullDownRefresh() {
    if (typeof guestUtils !== 'undefined' && guestUtils.checkGuest && guestUtils.checkGuest()) { wx.stopPullDownRefresh(); return }
    this.loadTopics()
    wx.stopPullDownRefresh()
  },
  data: {
    topics: [],
    loading: true
  },

  onLoad() {
    this.loadTopics()
  },

  loadTopics() {
    const token = app.globalData.token
    wx.request({
      url: `${questionUrl}/question/special/list`,
      method: 'GET',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        const list = (res.data && res.data.data) || []
        this.setData({
          topics: list.map((t) => ({
            ...t,
            difficultyLabel: ['', '简单', '中等', '困难'][t.difficulty] || '中等'
          })),
          loading: false
        })
      },
      fail: () => {
        this.setData({ loading: false })
        wx.showToast({ title: '加载失败，请重试', icon: 'none' })
      }
    })
  },

  goDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/special-detail/special-detail?id=${id}` })
  }
})
