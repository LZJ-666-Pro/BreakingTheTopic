const userApi = require('../../api/user.js')

Page({
  data: {
    activeCategory: 'all',
    achievementSummary: {
      total: 0,
      achieved: 0,
      points: 0
    },
    achievements: [],
    filteredAchievements: [],
    showAchievementDialog: false,
    currentAchievement: {},
    loading: false
  },

  onLoad() {
    this.loadUserAchievements()
  },

  loadUserAchievements() {
    if (this.data.loading) return
    
    this.setData({ loading: true })
    wx.showLoading({ title: '加载中' })
    
    userApi.syncAchievements()
      .then(() => {
        return userApi.getAchievements(this.data.activeCategory)
      })
      .then(res => {
        wx.hideLoading()
        
        if (res.code === 200 && res.data) {
          const data = res.data
          
          this.setData({
            achievementSummary: data.summary || {},
            achievements: data.achievements || [],
            filteredAchievements: data.achievements || [],
            loading: false
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
        console.error('获取成就列表失败:', err)
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
        this.setData({ loading: false })
      })
  },

  switchCategory(e) {
    const category = e.currentTarget.dataset.category
    this.setData({ activeCategory: category })
    this.loadUserAchievements()
  },

  filterAchievements() {
    const { achievements, activeCategory } = this.data
    let filtered = achievements

    if (activeCategory !== 'all') {
      filtered = achievements.filter(item => item.category === activeCategory)
    }

    this.setData({ filteredAchievements: filtered })
  },

  showAchievementDetail(e) {
    const achievement = e.currentTarget.dataset.achievement
    this.setData({
      currentAchievement: achievement,
      showAchievementDialog: true
    })
  },

  closeAchievementDialog() {
    this.setData({ showAchievementDialog: false })
  },

  checkAchievement(eventType, value) {
    userApi.checkAchievements(eventType, value)
      .then(res => {
        if (res.code === 200) {
          this.loadUserAchievements()
        }
      })
      .catch(err => {
        console.error('检查成就失败:', err)
      })
  },

  unlockAchievement(achievement) {
    wx.showModal({
      title: '🎉 成就解锁！',
      content: `恭喜获得"${achievement.name}"成就！\n奖励：${achievement.points}成就点`,
      showCancel: false,
      confirmText: '太棒了'
    })
  }
})
