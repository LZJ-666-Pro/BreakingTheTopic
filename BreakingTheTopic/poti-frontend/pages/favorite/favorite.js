const guestUtils = require('../../utils/guest.js')
const favoriteApi = require('../../api/favorite.js')

Page({
  data: {
    searchKeyword: '',
    activeDifficulty: 'all',
    favoriteList: [],
    filteredList: []
  },

  onLoad() {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '收藏夹功能需要登录后才能使用',
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
    this.loadFavorites()
  },

  onShow() {
    if (guestUtils.checkGuest()) {
      return
    }
    this.loadFavorites()
  },

  onUnload() {
  },

  loadFavorites() {
    const app = getApp()
    wx.request({
      url: `${app.globalData.favoriteUrl}/favorite/list`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          const typeMap = { 1: '单选', 2: '多选', 3: '判断' }
          const difficultyMap = { 1: '简单', 2: '中等', 3: '困难' }
          const difficultyLevelMap = { 1: 'easy', 2: 'medium', 3: 'hard' }
          const favoriteList = (res.data.data || []).map(item => ({
            ...item,
            typeText: typeMap[item.type] || '单选',
            difficultyText: difficultyMap[item.difficulty] || '中等',
            difficultyLevel: difficultyLevelMap[item.difficulty] || 'medium'
          }))
          this.setData({ favoriteList })
          this.applyFilter()
        }
      }
    })
  },

  onSearchInput(e) {
    this.setData({
      searchKeyword: e.detail.value
    })
  },

  clearSearch() {
    this.setData({
      searchKeyword: ''
    })
    this.applyFilter()
  },

  doSearch() {
    this.applyFilter()
  },

  filterByDifficulty(e) {
    const difficulty = e.currentTarget.dataset.difficulty
    this.setData({ activeDifficulty: difficulty })
    this.applyFilter()
  },

  applyFilter() {
    const { favoriteList, searchKeyword, activeDifficulty } = this.data
    let filteredList = [...favoriteList]

    if (searchKeyword) {
      const keyword = searchKeyword.toLowerCase()
      filteredList = filteredList.filter(item => 
        (item.title && item.title.toLowerCase().includes(keyword))
      )
    }

    if (activeDifficulty !== 'all') {
      filteredList = filteredList.filter(item => 
        item.difficultyLevel === activeDifficulty
      )
    }

    this.setData({ filteredList })
  },

  viewQuestion(e) {
    const questionId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/question-detail/question-detail?id=${questionId}`
    })
  },

  removeFavorite(e) {
    const questionId = e.currentTarget.dataset.id
    const app = getApp()
    
    wx.showModal({
      title: '提示',
      content: '确定要取消收藏吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.favoriteUrl}/favorite/remove/${questionId}`,
            method: 'DELETE',
            header: {
              'Authorization': `Bearer ${app.globalData.token}`,
              'X-User-Id': app.globalData.userId || 1
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({
                  title: '已取消收藏',
                  icon: 'success'
                })
                this.loadFavorites()
              }
            }
          })
        }
      }
    })
  },

  clearAllFavorites() {
    wx.showModal({
      title: '提示',
      content: '确定要清空所有收藏吗？此操作不可恢复。',
      confirmText: '清空',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          favoriteApi.clearAll().then(res => {
            if (res.code === 200) {
              wx.showToast({
                title: '已清空收藏',
                icon: 'success'
              })
              this.setData({
                favoriteList: [],
                filteredList: []
              })
            }
          }).catch(err => {
            wx.showToast({
              title: '清空失败',
              icon: 'none'
            })
          })
        }
      }
    })
  }
})
