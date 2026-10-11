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
    
    if (keyword.trim()) {
      this.getSuggestions(keyword)
    } else {
      this.setData({ showSuggestions: false, suggestions: [] })
    }
  },

  getSuggestions(keyword) {
    const suggestions = []
    const lowerKeyword = keyword.toLowerCase()
    
    this.data.hotKeywords.forEach(item => {
      if (item.toLowerCase().includes(lowerKeyword)) {
        suggestions.push(item)
      }
    })
    
    this.data.historyList.forEach(item => {
      if (item.toLowerCase().includes(lowerKeyword) && !suggestions.includes(item)) {
        suggestions.push(item)
      }
    })
    
    this.setData({
      suggestions: suggestions.slice(0, 8),
      showSuggestions: suggestions.length > 0
    })
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
    this.setData({
      keyword: '',
      results: [],
      searched: false,
      showSuggestions: false,
      suggestions: []
    })
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
    if (!this.data.keyword.trim()) {
      wx.showToast({ title: '请输入搜索关键词', icon: 'none' })
      return
    }

    const keyword = this.data.keyword.trim()
    this.saveHistory(keyword)
    this.recordSearch(keyword)

    this.setData({
      loading: true,
      searched: true,
      pageNum: 1,
      showSuggestions: false
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
