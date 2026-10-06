const app = getApp()

// 方向图标与底色（与面试首页一致）
const TYPE_META = [
  ['Java', '☕', '#EAF2FF'],
  ['Python', '🐍', '#FFF7E6'],
  ['MySQL', '🐬', '#E8F4FF'],
  ['Redis', '🧱', '#FFEEE8'],
  ['Spring', '🍃', '#EAF9F0'],
  ['MQ', '📮', '#F3EEFF'],
  ['算法', '🧮', '#EAF2FF'],
  ['计算机网络', '🌐', '#E8F4FF'],
  ['操作系统', '⚙️', '#EEF1F6'],
  ['设计模式', '🧩', '#F3EEFF']
]

function metaFor(title) {
  const hit = TYPE_META.find(([kw]) => (title || '').indexOf(kw) !== -1)
  return hit ? { emoji: hit[1], tint: hit[2] } : { emoji: '📝', tint: '#EEF1F6' }
}

Page({
  data: {
    list: [],
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    loading: false,
    initialized: false
  },

  onLoad() {
    this.setData({ initialized: false })
  },

  onShow() {
    if (!this.data.initialized || !this.data.loading) {
      this.refreshList()
    }
  },

  refreshList() {
    this.setData({
      list: [],
      pageNum: 1,
      hasMore: true,
      loading: true,
      initialized: true
    })
    this.loadHistoryInternal()
  },

  loadHistory() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ loading: true })
    this.loadHistoryInternal()
  },

  loadHistoryInternal() {
    wx.request({
      url: `${app.globalData.interviewUrl}/interview/history`,
      method: 'GET',
      data: {
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          const newList = data.list || []
          
          const uniqueMap = new Map()
          const existingIds = new Set(this.data.list.map(item => item.id))
          
          this.data.list.forEach(item => uniqueMap.set(item.id, item))
          newList.forEach(item => {
            if (!existingIds.has(item.id)) {
              uniqueMap.set(item.id, item)
            }
          })
          
          const finalList = Array.from(uniqueMap.values()).sort((a, b) =>
            new Date(b.createTime) - new Date(a.createTime)
          ).map(item => {
            // 标题补空格：后端存的是「Java模拟面试」，原型要求「Java 模拟面试」
            let title = item.title || `${item.direction || ''}模拟面试`
            if (/模拟面试$/.test(title) && title.indexOf(' 模拟面试') === -1) {
              title = title.replace(/模拟面试$/, ' 模拟面试')
            }
            const scoreVal = Math.round(Number(item.score) || 0)
            return {
              ...item,
              title,
              score: scoreVal,
              // WXML 不能调用 JS 方法，日期/用时必须预格式化
              dateText: this.formatDate(item.createTime),
              spendText: this.formatTime(item.spendSeconds),
              emoji: metaFor(title).emoji,
              tint: metaFor(title).tint,
              scoreLevel: scoreVal >= 80 ? 'high' : scoreVal >= 70 ? 'good' : scoreVal >= 60 ? 'mid' : 'low'
            }
          })
          
          this.setData({
            list: finalList,
            hasMore: newList.length >= this.data.pageSize
          })
        }
      },
      complete: () => {
        this.setData({ loading: false })
      }
    })
  },

  onReachBottom() {
    if (this.data.hasMore) {
      this.setData({ pageNum: this.data.pageNum + 1 })
      this.loadHistory()
    }
  },

  viewDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/interview-result/interview-result?interviewId=${id}`
    })
  },

  clearAll() {
    wx.showModal({
      title: '提示',
      content: '确定清空所有面试记录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.interviewUrl}/interview/clear`,
            method: 'DELETE',
            header: {
              'Authorization': `Bearer ${app.globalData.token}`,
              'X-User-Id': app.globalData.userId
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已清空', icon: 'success' })
                this.setData({ list: [] })
              } else {
                wx.showToast({ title: '清空失败', icon: 'none' })
              }
            }
          })
        }
      }
    })
  },

  formatTime(seconds) {
    if (!seconds) return '0秒'
    const min = Math.floor(seconds / 60)
    const sec = seconds % 60
    return min > 0 ? `${min}分钟` : `${sec}秒`
  },

  formatDate(dateStr) {
    if (!dateStr) return ''
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return ''
    const p = (n) => (n < 10 ? '0' + n : '' + n)
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
  }
})
