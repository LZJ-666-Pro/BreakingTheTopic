const PAGE_SIZE = 10

Page({
  data: {
    records: [],
    page: 1,
    total: 0,
    loading: false,
    finished: false
  },

  _isDestroyed: false,

  onLoad() {
    this._isDestroyed = false
    this.loadRecords(true)
  },

  onUnload() {
    this._isDestroyed = true
  },

  onPullDownRefresh() {
    this.loadRecords(true, () => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    if (!this.data.finished && !this.data.loading) {
      this.loadRecords(false)
    }
  },

  request(options) {
    const app = getApp()
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${app.globalData.practiceUrl}${options.url}`,
        method: options.method || 'GET',
        data: options.data || {},
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${app.globalData.token}`,
          'X-User-Id': app.globalData.userId || 1
        },
        success: (res) => resolve(res.data),
        fail: (err) => reject(err)
      })
    })
  },

  loadRecords(reset, done) {
    if (this.data.loading) {
      if (done) done()
      return
    }
    const page = reset ? 1 : this.data.page + 1
    this.setData({ loading: true })
    this.request({
      url: '/competition/my/list',
      data: { pageNum: page, pageSize: PAGE_SIZE }
    }).then(res => {
      if (this._isDestroyed) return
      if (res && res.code === 200 && res.data) {
        const rawList = res.data.list || []
        const mapped = rawList.map((item, idx) => ({
          ...item,
          key: `${idx}-${item.competitionId || ''}-${item.submitTime || ''}`,
          costText: this.formatDuration(item.costSeconds),
          submitTimeText: this.formatTime(item.submitTime),
          score: item.score === null || item.score === undefined ? '--' : item.score,
          rank: item.rank === null || item.rank === undefined ? 0 : item.rank
        }))
        const records = reset ? mapped : this.data.records.concat(mapped)
        const total = res.data.total || 0
        this.setData({
          records,
          total,
          page,
          finished: records.length >= total || rawList.length < PAGE_SIZE
        })
      } else {
        wx.showToast({ title: (res && res.msg) || '加载失败', icon: 'none' })
      }
    }).catch(err => {
      console.error('加载我的成绩失败', err)
      wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
    }).finally(() => {
      if (!this._isDestroyed) {
        this.setData({ loading: false })
        if (done) done()
      }
    })
  },

  // 查看该场竞赛的完整成绩与试卷回顾（答题页对已交卷竞赛自动进入成绩模式）
  viewRecord(e) {
    const compId = e.currentTarget.dataset.comp
    if (!compId) return
    wx.navigateTo({ url: `/pages/competition-quiz/competition-quiz?id=${compId}` })
  },

  // ---------- 工具 ----------
  formatTime(str) {
    if (!str) return ''
    // 兼容 yyyy-MM-dd HH:mm:ss / ISO
    const m = String(str).match(/(\d{4})-(\d{2})-(\d{2})[T ]?(\d{2})?:?(\d{2})?/)
    if (!m) return String(str)
    return `${m[2]}-${m[3]} ${m[4] || '00'}:${m[5] || '00'}`
  },

  formatDuration(seconds) {
    const s = Number(seconds) || 0
    const mm = Math.floor(s / 60)
    const ss = s % 60
    if (mm <= 0) return `${ss}秒`
    return `${mm}分${ss < 10 ? '0' + ss : ss}秒`
  }
})
