const guestUtils = require('../../utils/guest.js')

// 方向图标与底色映射（Java☕ Python🐍 MySQL🐬 Redis🧱 Spring🍃 MQ📮 等）
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

// 分组规则：依次匹配，命中即归组；都不命中归「其他」
const GROUP_RULES = [
  ['backend', '后端开发', '包含常见后端技术栈和框架',
    ['Java', 'Python', 'MySQL', 'Redis', 'Spring', 'MQ', '消息队列', 'Kafka', 'Elastic', '微服务', '分布式', '并发', '多线程', 'JVM', 'Linux', 'Docker', 'Netty', 'MyBatis']],
  ['basic', '基础能力', '计算机基础与核心能力',
    ['算法', '计算机网络', '操作系统', '数据结构', '设计模式', '计算机']],
  ['other', '其他', '更多面试方向', []]
]

const HOT_KEYWORDS = ['Java', 'Python', '算法', 'Spring', 'MySQL', 'Redis', '计算机网络']

function groupKeyOf(title) {
  const t = (title || '').toLowerCase()
  for (const [key, , , kws] of GROUP_RULES) {
    if (kws.some(k => t.indexOf(k.toLowerCase()) !== -1)) return key
  }
  return 'other'
}

Page({
  data: {
    hotTypes: [],
    groups: [],
    expandedMap: { backend: true, basic: false, other: false },
    searchText: '',
    statistics: {
      totalInterviews: 0,
      totalQuestions: 0,
      accuracy: 0
    },
    selectedType: '',
    isStarting: false,
    searchEmpty: false
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
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200 && res.data.data && res.data.data.length > 0) {
          const all = res.data.data.map(t => ({
            ...t,
            emoji: t.emoji || metaFor(t.title || t.type).emoji,
            tint: metaFor(t.title || t.type).tint
          }))
          this._allTypes = all

          // 热门推荐：按关键词挑，凑不齐就用前 7 个
          let hot = HOT_KEYWORDS
            .map(kw => all.find(t => (t.title || '').indexOf(kw) !== -1))
            .filter(Boolean)
          if (hot.length < 7) {
            all.forEach(t => {
              if (hot.length < 7 && hot.indexOf(t) === -1) hot.push(t)
            })
          }
          this.setData({ hotTypes: hot })
          this.rebuildGroups()
        }
      }
    })
  },

  rebuildGroups() {
    const kw = this.data.searchText.trim().toLowerCase()
    const source = this._allTypes || []
    const filtered = kw
      ? source.filter(t => (t.title || '').toLowerCase().indexOf(kw) !== -1)
      : source

    const expandedMap = kw
      ? { backend: true, basic: true, other: true }
      : this.data.expandedMap

    const groups = GROUP_RULES.map(([key, name, desc]) => ({
      key,
      name,
      desc,
      list: filtered.filter(t => groupKeyOf(t.title) === key)
    }))

    const searchEmpty = !!kw && groups.every(g => g.list.length === 0)
    this.setData({ groups, expandedMap, searchEmpty })
  },

  onSearchInput(e) {
    this.setData({ searchText: e.detail.value })
    this.rebuildGroups()
  },

  onToggleGroup(e) {
    const key = e.currentTarget.dataset.key
    const expandedMap = { ...this.data.expandedMap, [key]: !this.data.expandedMap[key] }
    this.setData({ expandedMap })
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
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
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

  onSelectType(e) {
    this.setData({ selectedType: e.currentTarget.dataset.type })
  },

  startInterview() {
    if (this.data.isStarting) return

    const type = this.data.selectedType
    if (!type) {
      wx.showToast({ title: '请先选择面试方向', icon: 'none' })
      return
    }

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
