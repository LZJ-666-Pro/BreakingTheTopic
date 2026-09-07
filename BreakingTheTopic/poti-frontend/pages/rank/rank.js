const userApi = require('../../api/user.js')
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    activeTab: 'total',
    activeSubTab: 'world',
    scoreLabel: '累计刷题',
    topThree: [],
    rankList: [],
    myRank: null,
    currentPage: 0,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  onLoad(options) {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '排行榜功能需要登录后才能使用',
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
    const tab = options.tab || 'total'
    this.setData({ activeTab: tab })
    this.updateScoreLabel()
    this.loadRankData()
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ 
      activeTab: tab,
      currentPage: 0,
      hasMore: true,
      rankList: [],
      topThree: []
    })
    this.updateScoreLabel()
    this.loadRankData()
  },

  switchSubTab(e) {
    const subtab = e.currentTarget.dataset.subtab
    this.setData({ 
      activeSubTab: subtab,
      currentPage: 0,
      hasMore: true,
      rankList: [],
      topThree: []
    })
    this.loadRankData()
  },

  updateScoreLabel() {
    const labels = {
      total: '累计刷题',
      today: '今日刷题',
      accuracy: '正确率',
      streak: '连续天数'
    }
    this.setData({ scoreLabel: labels[this.data.activeTab] })
  },

  loadRankData() {
    if (this.data.loading) return
    
    this.setData({ loading: true })
    wx.showLoading({ title: '加载中' })
    
    const { activeTab, activeSubTab, currentPage, pageSize } = this.data
    
    userApi.getRanking(activeTab, activeSubTab, currentPage, pageSize)
      .then(res => {
        wx.hideLoading()
        
        if (res.code === 200 && res.data) {
          const data = res.data
          const newRankList = data.rankList || []
          
          this.setData({
            topThree: data.topThree || [],
            rankList: this.data.currentPage === 0 ? newRankList : [...this.data.rankList, ...newRankList],
            myRank: data.myRank,
            hasMore: data.hasMore || false,
            loading: false,
            currentPage: this.data.currentPage + 1
          })
        } else {
          wx.showToast({
            title: res.message || '加载失败',
            icon: 'none'
          })
          this.setData({ loading: false })
        }
      })
      .catch(err => {
        wx.hideLoading()
        console.error('获取排行榜失败:', err)
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
        this.setData({ loading: false })
      })
  },

  loadMore() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadRankData()
    }
  },

  goToFriendRecommend() {
    wx.navigateTo({
      url: '/pages/friend-recommend/friend-recommend'
    })
  }
})
