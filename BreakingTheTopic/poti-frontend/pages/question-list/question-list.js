const { getCategoryIcon } = require('../../utils/categoryLogo.js')

const PAGE_SIZE = 20

Page({
  onPullDownRefresh() {
    if (typeof guestUtils !== 'undefined' && guestUtils.checkGuest && guestUtils.checkGuest()) { wx.stopPullDownRefresh(); return }
    this.loadQuestions()
    wx.stopPullDownRefresh()
  },
  data: {
    categoryId: null,
    categoryName: '',
    categoryIcon: '/images/questionLogo/default.png',
    questions: [],
    total: 0,
    page: 1,
    hasMore: false,
    loading: true,
    loadingMore: false
  },

  onLoad(options) {
    const categoryId = options.categoryId
    const categoryName = options.name ? decodeURIComponent(options.name) : ''

    this.setData({
      categoryId: categoryId,
      categoryName: categoryName,
      categoryIcon: getCategoryIcon(categoryName)
    })

    if (categoryId) {
      this.loadQuestions(categoryId)
    }

    wx.setNavigationBarTitle({
      title: categoryName || '题目列表'
    })
  },

  loadQuestions(categoryId) {
    const app = getApp()
    const that = this

    wx.request({
      url: `${app.globalData.questionUrl}/question/page?categoryId=${categoryId}&page=1&pageSize=${PAGE_SIZE}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: function(res) {
        if (res.data.code === 200 && res.data.data) {
          const questions = (res.data.data.list || []).map(function(q) {
            return that.formatQuestion(q)
          })
          const total = res.data.data.total || 0

          that.setData({
            questions: questions,
            total: total,
            page: 1,
            hasMore: questions.length < total,
            loading: false
          })
        } else {
          that.setData({ loading: false })
        }
      },
      fail: function() {
        that.setData({ loading: false })
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    })
  },

  formatQuestion(q) {
    return {
      id: q.id,
      title: q.title || '题目',
      type: this.getQuestionType(q.type),
      difficulty: this.getDifficultyText(q.difficulty),
      difficultyClass: this.getDifficultyClass(q.difficulty),
      viewCount: q.viewCount || 0,
      favoriteCount: q.favoriteCount || 0
    }
  },

  // 上拉触底加载下一页（后端分页，替代原全量拉取前端截断 20 条）
  onReachBottom() {
    if (this.data.loading || this.data.loadingMore || !this.data.hasMore) {
      return
    }
    const app = getApp()
    const that = this
    const nextPage = this.data.page + 1

    this.setData({ loadingMore: true })
    wx.request({
      url: `${app.globalData.questionUrl}/question/page?categoryId=${this.data.categoryId}&page=${nextPage}&pageSize=${PAGE_SIZE}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: function(res) {
        if (res.data.code === 200 && res.data.data) {
          const more = (res.data.data.list || []).map(function(q) {
            return that.formatQuestion(q)
          })
          const questions = that.data.questions.concat(more)
          that.setData({
            questions: questions,
            page: nextPage,
            hasMore: questions.length < (res.data.data.total || 0)
          })
        }
      },
      fail: function() {
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        })
      },
      complete: function() {
        that.setData({ loadingMore: false })
      }
    })
  },

  getQuestionType(type) {
    const typeMap = {
      1: '单选题',
      2: '多选题',
      3: '判断题',
      4: '简答题',
      5: '编程题'
    }
    return typeMap[type] || '未知'
  },

  getDifficultyText(difficulty) {
    const map = {
      1: '简单',
      2: '中等',
      3: '困难'
    }
    return map[difficulty] || '中等'
  },

  getDifficultyClass(difficulty) {
    const map = {
      1: 'easy',
      2: 'medium',
      3: 'hard'
    }
    return map[difficulty] || 'medium'
  },

  goToQuestion(e) {
    const questionId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/question-detail/question-detail?id=${questionId}`
    })
  },

  startPractice() {
    const { categoryId } = this.data
    wx.navigateTo({
      url: `/pages/answer/answer?categoryId=${categoryId}`
    })
  }
})
