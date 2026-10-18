const searchApi = require('../../api/search.js')

const difficultyNames = {
  1: '简单',
  2: '中等',
  3: '困难'
}

Page({
  data: {
    keyword: '',
    results: [],
    total: 0,
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    loading: false,
    searched: false,
    showInput: false,
    historyList: [],
    showSuggestions: false,
    suggestions: [],
    showFilterSheet: false,
    categoryList: [],
    currentCategoryId: '',
    currentDifficulty: '',
    activeFilters: [],
    hotKeywords: [],
    hotQuestions: []
  },

  onLoad(options) {
    this.loadHistory()
    this.loadCategories()
    this.loadHotKeywords()
    this.loadHotQuestions()
    if (options.keyword) {
      this.setData({ keyword: options.keyword })
      this.onSearch()
    }
  },

  loadCategories() {
    const app = getApp()
    wx.request({
      url: `${app.globalData.questionUrl}/question/category/list`,
      header: app.globalData.token ? { 'Authorization': `Bearer ${app.globalData.token}` } : {},
      success: (res) => {
        if (res.data.code === 200 && Array.isArray(res.data.data)) {
          this.setData({ categoryList: res.data.data })
        }
      },
      fail: (err) => {
        console.error('加载分类列表失败', err)
      }
    })
  },

  loadHotKeywords() {
    const app = getApp()
    wx.request({
      url: `${app.globalData.userUrl}/search/hot?limit=10`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data && res.data.data.keywords) {
          if (res.data.data.keywords.length > 0) {
            this.setData({ hotKeywords: res.data.data.keywords })
          } else {
            this.setData({ 
              hotKeywords: ['HashMap', 'Spring Boot', 'MySQL索引', 'Redis缓存', 'JVM调优', 'Spring Cloud', '消息队列', '微服务架构', '分布式事务', 'Docker容器']
            })
          }
        }
      },
      fail: () => {
        this.setData({ 
          hotKeywords: ['HashMap', 'Spring Boot', 'MySQL索引', 'Redis缓存', 'JVM调优', 'Spring Cloud', '消息队列', '微服务架构', '分布式事务', 'Docker容器']
        })
      }
    })
  },

  loadHotQuestions() {
    const app = getApp()
    const that = this
    wx.request({
      url: `${app.globalData.questionUrl}/question/random/global?limit=8`,
      method: 'GET',
      header: app.globalData.token ? { 'Authorization': `Bearer ${app.globalData.token}` } : {},
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const hotQuestions = res.data.data.map(item => ({
            id: item.id,
            title: item.title,
            viewCount: item.viewCount || 0,
            viewText: that.formatViewCount(item.viewCount || 0)
          }))
          that.setData({ hotQuestions })
        }
      },
      fail: (err) => {
        console.error('加载热门题目失败', err)
      }
    })
  },

  formatViewCount(count) {
    if (count >= 10000) {
      return (count / 10000).toFixed(1) + '万'
    }
    return count
  },

  onShow() {
    this.setData({ showInput: false })
    setTimeout(() => {
      this.setData({ showInput: true })
    }, 50)
  },

  goBack() {
    wx.navigateBack()
  },

  loadHistory() {
    const history = wx.getStorageSync('searchHistory') || []
    this.setData({ historyList: history })
  },

  saveHistory(keyword) {
    let history = wx.getStorageSync('searchHistory') || []
    history = history.filter(item => item !== keyword)
    history.unshift(keyword)
    if (history.length > 10) {
      history = history.slice(0, 10)
    }
    wx.setStorageSync('searchHistory', history)
    this.setData({ historyList: history })
  },

  recordSearch(keyword) {
    const app = getApp()
    // 未登录状态不上报搜索记录，避免401报错
    if (!app.globalData.token) {
      return
    }
    wx.request({
      url: `${app.globalData.userUrl}/search/record`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || ''
      },
      data: { keyword }
    })
  },

  clearHistory() {
    wx.showModal({
      title: '提示',
      content: '确定清空搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('searchHistory')
          this.setData({ historyList: [] })
        }
      }
    })
  },

  onInput(e) {
    const keyword = e.detail.value
    this.setData({ keyword })

    // 输入实时搜索（500ms 防抖）：停止输入后自动出结果
    if (this.debounceTimer) clearTimeout(this.debounceTimer)
    if (keyword.trim()) {
      this.getSuggestions(keyword)
      this.debounceTimer = setTimeout(() => {
        this.doSearch(false)
      }, 500)
    } else {
      this.setData({ showSuggestions: false, suggestions: [], searched: false, results: [], total: 0 })
    }
  },

  getSuggestions(keyword) {
    const suggestions = []
    const lowerKeyword = keyword.toLowerCase()

    // 本地候选：搜索历史 > 热搜词 > 分类名
    this.data.historyList.forEach(item => {
      if (item.toLowerCase().includes(lowerKeyword) && !suggestions.includes(item)) {
        suggestions.push(item)
      }
    })

    this.data.hotKeywords.forEach(item => {
      if (item.toLowerCase().includes(lowerKeyword) && !suggestions.includes(item)) {
        suggestions.push(item)
      }
    })

    this.data.categoryList.forEach(item => {
      if (item.name && item.name.toLowerCase().includes(lowerKeyword) && !suggestions.includes(item.name)) {
        suggestions.push(item.name)
      }
    })

    this.setData({
      suggestions: suggestions.slice(0, 10),
      showSuggestions: suggestions.length > 0
    })

    // 题库候选：后端从题干/标签/分类名联想，追加到本地候选之后
    searchApi.getSuggestions(keyword.trim())
      .then(res => {
        if (res.code === 200 && Array.isArray(res.data) && this.data.keyword.trim() === keyword.trim()) {
          const merged = [...this.data.suggestions]
          res.data.forEach(item => {
            if (item && !merged.includes(item)) {
              merged.push(item)
            }
          })
          this.setData({
            suggestions: merged.slice(0, 10),
            showSuggestions: merged.length > 0
          })
        }
      })
      .catch(() => {})
  },

  selectSuggestion(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({
      keyword,
      showSuggestions: false,
      suggestions: [],
      pageNum: 1,
      results: []
    })
    this.onSearch()
  },

  clearInput() {
    if (this.debounceTimer) clearTimeout(this.debounceTimer)
    this.setData({
      keyword: '',
      results: [],
      searched: false,
      showSuggestions: false,
      suggestions: []
    })
  },

  onUnload() {
    if (this.debounceTimer) clearTimeout(this.debounceTimer)
  },

  searchKeyword(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({
      keyword,
      pageNum: 1,
      results: [],
      showSuggestions: false
    })
    this.onSearch()
  },

  onSearch() {
    this.doSearch(true)
  },

  doSearch(saveHistory) {
    if (!this.data.keyword.trim()) {
      wx.showToast({ title: '请输入搜索关键词', icon: 'none' })
      return
    }

    const keyword = this.data.keyword.trim()
    // 只有主动触发（搜索按钮/回车/点建议）才记录历史，实时搜索不上报避免刷屏
    if (saveHistory) {
      this.saveHistory(keyword)
      this.recordSearch(keyword)
    }

    this.setData({
      loading: true,
      searched: true,
      pageNum: 1
    })

    searchApi.searchQuestions(keyword, this.data.currentCategoryId, this.data.currentDifficulty, 1, this.data.pageSize)
      .then(res => {
        const data = res.data || {}
        const results = (data.list || []).map(item => ({
          ...item,
          difficultyName: difficultyNames[item.difficulty] || '中等'
        }))

        this.setData({
          results,
          total: data.total || 0,
          hasMore: (data.list || []).length >= this.data.pageSize,
          loading: false
        })
      })
      .catch(err => {
        console.error('搜索失败', err)
        this.setData({ loading: false })
        wx.showToast({ title: '搜索失败', icon: 'none' })
      })
  },

  loadMore() {
    if (this.data.loading || !this.data.hasMore) return

    const pageNum = this.data.pageNum + 1
    this.setData({ loading: true, pageNum })

    searchApi.searchQuestions(this.data.keyword.trim(), this.data.currentCategoryId, this.data.currentDifficulty, pageNum, this.data.pageSize)
      .then(res => {
        const list = (res.data && res.data.list) || []
        const newResults = list.map(item => ({
          ...item,
          difficultyName: difficultyNames[item.difficulty] || '中等'
        }))

        this.setData({
          results: [...this.data.results, ...newResults],
          hasMore: list.length >= this.data.pageSize,
          loading: false
        })
      })
      .catch(err => {
        console.error('加载更多失败', err)
        this.setData({ loading: false, pageNum: pageNum - 1 })
      })
  },

  showFilter() {
    this.setData({ showFilterSheet: true })
  },

  closeFilter() {
    this.setData({ showFilterSheet: false })
  },

  selectCategory(e) {
    this.setData({ currentCategoryId: e.currentTarget.dataset.id || '' })
  },

  selectDifficulty(e) {
    const difficulty = e.currentTarget.dataset.difficulty
    this.setData({ currentDifficulty: difficulty })
  },

  resetFilter() {
    this.setData({
      currentCategoryId: '',
      currentDifficulty: '',
      activeFilters: []
    })
  },

  confirmFilter() {
    const activeFilters = []

    if (this.data.currentCategoryId !== '') {
      const cat = this.data.categoryList.find(c => String(c.id) === String(this.data.currentCategoryId))
      activeFilters.push({
        type: 'category',
        value: this.data.currentCategoryId,
        label: cat ? cat.name : '分类'
      })
    }

    if (this.data.currentDifficulty) {
      activeFilters.push({
        type: 'difficulty',
        value: this.data.currentDifficulty,
        label: difficultyNames[this.data.currentDifficulty] || '中等'
      })
    }

    this.setData({
      activeFilters,
      showFilterSheet: false,
      pageNum: 1,
      results: []
    })

    if (this.data.keyword) {
      this.onSearch()
    }
  },

  removeFilter(e) {
    const filter = e.currentTarget.dataset.filter
    const activeFilters = this.data.activeFilters.filter(f => f.type !== filter.type)

    if (filter.type === 'category') {
      this.setData({ currentCategoryId: '' })
    } else if (filter.type === 'difficulty') {
      this.setData({ currentDifficulty: '' })
    }

    this.setData({
      activeFilters,
      pageNum: 1,
      results: []
    })

    if (this.data.keyword) {
      this.onSearch()
    }
  },

  goToQuestion(e) {
    const item = e.currentTarget.dataset.item
    wx.navigateTo({
      url: `/pages/question-detail/question-detail?id=${item.id}`
    })
  }
})
