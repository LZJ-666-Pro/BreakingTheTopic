const app = getApp()
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    camps: [],
    showCamps: [],
    keyword: '',
    loading: true,
    myCamp: null
  },

  onShow() {
    if (guestUtils.checkGuest()) {
      return
    }
    this.loadCamps()
  },

  loadCamps() {
    this.setData({ loading: true })
    wx.request({
      url: `${app.globalData.interviewUrl}/camp/list`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          const camps = (res.data.data || []).map(c => ({
            ...c,
            joinText: this.formatJoin(c.joinCount),
            progressText: `${c.progressDays}/${c.totalDays}天`
          }))
          this.setData({
            camps,
            showCamps: this.filterCamps(camps, this.data.keyword),
            loading: false,
            myCamp: camps.find(c => c.joined && !c.finished) || null
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

  filterCamps(camps, keyword) {
    if (!keyword) return camps
    const kw = keyword.toLowerCase()
    return camps.filter(c =>
      (c.title || '').toLowerCase().indexOf(kw) !== -1 ||
      (c.subtitle || '').toLowerCase().indexOf(kw) !== -1 ||
      (c.intro || '').toLowerCase().indexOf(kw) !== -1
    )
  },

  onSearchInput(e) {
    const keyword = e.detail.value
    this.setData({
      keyword,
      showCamps: this.filterCamps(this.data.camps, keyword)
    })
  },

  // Hero Banner「立即查看」：优先进入进行中的营，否则看第一个营详情
  goHeroTarget() {
    if (this.data.myCamp) {
      wx.navigateTo({ url: `/pages/camp-home/camp-home?campId=${this.data.myCamp.id}` })
    } else if (this.data.camps.length > 0) {
      wx.navigateTo({ url: `/pages/camp-detail/camp-detail?campId=${this.data.camps[0].id}` })
    }
  },

  goDetail(e) {
    const campId = e.currentTarget.dataset.campid
    wx.navigateTo({ url: `/pages/camp-detail/camp-detail?campId=${campId}` })
  },

  // 已加入的营：按钮直接进入训练营
  onCampAction(e) {
    const campId = e.currentTarget.dataset.campid
    const joined = e.currentTarget.dataset.joined
    if (joined) {
      wx.navigateTo({ url: `/pages/camp-home/camp-home?campId=${campId}` })
    } else {
      wx.navigateTo({ url: `/pages/camp-detail/camp-detail?campId=${campId}` })
    }
  },

  goMyCamp() {
    if (this.data.myCamp) {
      wx.navigateTo({ url: `/pages/camp-home/camp-home?campId=${this.data.myCamp.id}` })
    } else {
      wx.showToast({ title: '还没有进行中的训练营', icon: 'none' })
    }
  }
})
