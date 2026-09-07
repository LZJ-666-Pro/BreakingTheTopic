const practiceApi = require('../../api/practice.js')
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    loading: true,
    historyList: [],
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    statistics: {
      totalQuestionCount: 0,
      correctCount: 0,
      wrongCount: 0,
      correctRate: 0
    }
  },

  onLoad() {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '历史记录功能需要登录后才能使用',
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
    this.loadStatistics()
    this.loadHistory()
  },

  onShow() {
    if (guestUtils.checkGuest()) {
      return
    }
    this.refreshList()
  },

  refreshList() {
    this.setData({
      pageNum: 1,
      hasMore: true,
      historyList: []
    })
    this.loadStatistics()
    this.loadHistory()
  },

  onPullDownRefresh() {
    this.setData({
      pageNum: 1,
      hasMore: true,
      historyList: []
    })
    this.loadStatistics()
    this.loadHistory(() => {
      wx.stopPullDownRefresh()
    })
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadHistory()
    }
  },

  loadStatistics() {
    const app = getApp()
    wx.request({
      url: `${app.globalData.practiceUrl}/practice/internal/statistics`,
      method: 'GET',
      header: {
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          const stats = res.data.data
          const correctRate = stats.totalQuestionCount > 0 
            ? Math.round((stats.correctCount / stats.totalQuestionCount) * 100) 
            : 0
          
          this.setData({
            statistics: {
              ...stats,
              correctRate
            }
          })
        }
      }
    })
  },

  loadHistory(callback) {
    if (!this.data.hasMore) {
      callback && callback()
      return
    }

    this.setData({ loading: true })

    practiceApi.getPracticeHistory(this.data.pageNum, this.data.pageSize).then(res => {
      if (res.code === 200) {
        const newList = (res.data.list || []).map(item => {
          return {
            ...item,
            questionTitle: item.questionTitle || '题目 #' + item.questionId,
            practiceTime: this.formatDateTime(item.practiceTime || item.createTime)
          }
        })
        const historyList = [...this.data.historyList, ...newList]
        
        this.setData({
          historyList,
          loading: false,
          hasMore: newList.length === this.data.pageSize,
          pageNum: this.data.pageNum + 1
        })
      } else {
        this.setData({ loading: false })
      }
      
      callback && callback()
    }).catch(err => {
      console.error('加载刷题记录失败', err)
      this.setData({ loading: false })
      callback && callback()
    })
  },

  viewQuestion(e) {
    const questionId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/question-detail/question-detail?id=${questionId}`
    })
  },

  deleteItem(e) {
    const id = e.currentTarget.dataset.id
    const index = e.currentTarget.dataset.index
    const app = getApp()
    
    wx.showModal({
      title: '提示',
      content: '确定删除这条记录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.practiceUrl}/practice/record/${id}`,
            method: 'DELETE',
            header: {
              'X-User-Id': app.globalData.userId || 1
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已删除', icon: 'success' })
                const historyList = this.data.historyList
                historyList.splice(index, 1)
                this.setData({ historyList })
                this.loadStatistics()
              } else {
                wx.showToast({ title: '删除失败', icon: 'none' })
              }
            }
          })
        }
      }
    })
  },

  clearAll() {
    const app = getApp()
    
    wx.showModal({
      title: '提示',
      content: '确定清空所有刷题记录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.practiceUrl}/practice/clear`,
            method: 'DELETE',
            header: {
              'X-User-Id': app.globalData.userId || 1
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已清空', icon: 'success' })
                this.setData({ 
                  historyList: [],
                  statistics: {
                    totalQuestionCount: 0,
                    correctCount: 0,
                    wrongCount: 0,
                    correctRate: 0
                  }
                })
              } else {
                wx.showToast({ title: '清空失败', icon: 'none' })
              }
            }
          })
        }
      }
    })
  },

  formatDate(dateStr) {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now - date
    
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
    
    return `${date.getMonth() + 1}月${date.getDate()}日`
  },

  formatDateTime(dateStr) {
    if (!dateStr) return ''
    
    if (dateStr.includes('T')) {
      dateStr = dateStr.replace('T', ' ')
    }
    
    if (dateStr.includes('.')) {
      dateStr = dateStr.split('.')[0]
    }
    
    return dateStr
  },

  goToPractice() {
    wx.switchTab({
      url: '/pages/practice/practice'
    })
  }
})
