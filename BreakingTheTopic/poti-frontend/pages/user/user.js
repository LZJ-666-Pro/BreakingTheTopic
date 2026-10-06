const userApi = require('../../api/user.js')
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    userInfo: null,
    userId: null,
    statistics: {
      totalQuestionCount: 0,
      correctCount: 0,
      wrongCount: 0,
      favoriteCount: 0,
      wrongbookCount: 0,
      todayCount: 0,
      lastPracticeTime: null
    },
    showEditDialog: false,
    editForm: {
      nickname: '',
      phone: '',
      email: '',
      gender: 0,
      uniqueId: ''
    },
    unreadCount: 0
  },

  onLoad() {},

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 4
      })
    }
    
    if (guestUtils.checkGuest()) {
      const guestUserInfo = wx.getStorageSync('userInfo')
      this.setData({
        userInfo: guestUserInfo,
        userId: 0,
        statistics: {
          totalQuestionCount: 0,
          correctCount: 0,
          wrongCount: 0,
          favoriteCount: 0,
          wrongbookCount: 0,
          todayCount: 0,
          lastPracticeTime: null
        },
        unreadCount: 0
      })
      return
    }
    
    this.loadUserInfo()
    this.loadStatistics()
    this.loadUnreadCount()
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo')
    const userId = wx.getStorageSync('userId')
    if (userInfo) {
      this.setData({
        userInfo: userInfo,
        userId: userId
      })
    }

    userApi.getUserInfo().then(res => {
      if (res && res.data) {
        const data = res.data
        this.setData({
          userInfo: data,
          userId: data.id
        })
        wx.setStorageSync('userInfo', data)
        const app = getApp()
        app.globalData.userInfo = data
      }
    }).catch(err => {
      console.error('加载用户信息失败', err)
    })
  },

  loadStatistics() {
    const localStats = wx.getStorageSync('practice_stats') || {}

    userApi.getStatistics().then(res => {
      const stats = res.data || {
        totalQuestionCount: 0,
        correctCount: 0,
        wrongCount: 0,
        favoriteCount: 0,
        todayCount: 0,
        lastPracticeTime: null
      }

      this.setData({
        statistics: stats
      })
    }).catch(err => {
      const stats = {
        totalQuestionCount: localStats.totalCompleted || 0,
        correctCount: localStats.totalCorrect || 0,
        wrongCount: (localStats.totalCompleted || 0) - (localStats.totalCorrect || 0),
        favoriteCount: 0,
        todayCount: 0
      }
      this.setData({ statistics: stats })
      console.error('加载统计数据失败', err)
    })
  },

  chooseAvatar(e) {
    const that = this
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFiles[0].tempFilePath
        that.uploadAvatar(tempFilePath)
      }
    })
  },

  uploadAvatar(filePath) {
    const app = getApp()
    const that = this
    
    wx.uploadFile({
      url: `${app.globalData.baseUrl}/user/avatar`,
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success(res) {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          wx.showToast({ title: '头像更新成功', icon: 'success' })
          that.loadUserInfo()
        } else {
          wx.showToast({ title: data.msg || '上传失败', icon: 'none' })
        }
      },
      fail() {
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    })
  },

  editProfile() {
    const { userInfo } = this.data
    this.setData({
      showEditDialog: true,
      editForm: {
        nickname: userInfo ? userInfo.nickname || '' : '',
        phone: userInfo ? userInfo.phone || '' : '',
        email: userInfo ? userInfo.email || '' : '',
        gender: userInfo ? userInfo.gender || 0 : 0,
        uniqueId: userInfo ? userInfo.uniqueId || '' : ''
      }
    })
  },

  onGenderChange(e) {
    this.setData({
      'editForm.gender': Number(e.detail)
    })
  },

  onNicknameChange(e) {
    this.setData({
      'editForm.nickname': e.detail
    })
  },

  onPhoneChange(e) {
    this.setData({
      'editForm.phone': e.detail
    })
  },

  onEmailChange(e) {
    this.setData({
      'editForm.email': e.detail
    })
  },

  onUniqueIdChange(e) {
    this.setData({
      'editForm.uniqueId': e.detail
    })
  },

  confirmEdit() {
    const { editForm } = this.data
    
    if (editForm.uniqueId && editForm.uniqueId.trim()) {
      if (editForm.uniqueId.length < 4 || editForm.uniqueId.length > 20) {
        wx.showToast({ title: 'ID长度必须在4-20个字符之间', icon: 'none' })
        return
      }
      
      if (!/^[a-zA-Z0-9_]+$/.test(editForm.uniqueId)) {
        wx.showToast({ title: 'ID只能包含字母、数字和下划线', icon: 'none' })
        return
      }
    }
    
    userApi.updateUserInfo(editForm).then(res => {
      wx.showToast({
        title: '修改成功',
        icon: 'success'
      })
      this.setData({ showEditDialog: false })
      this.loadUserInfo()
    }).catch(err => {
      console.error('更新用户信息失败', err)
    })
  },

  cancelEdit() {
    this.setData({ showEditDialog: false })
  },

  goToHistory() {
    wx.navigateTo({
      url: '/pages/history/history'
    })
  },

  goToCorrect() {
    wx.navigateTo({
      url: '/pages/wrongbook/wrongbook?tab=choice'
    })
  },

  goToWrongbook() {
    if (!guestUtils.requireLogin(null, '错题本功能需要登录后才能使用')) {
      return
    }
    wx.navigateTo({
      url: '/pages/wrongbook/wrongbook'
    })
  },

  goToFavorite() {
    if (!guestUtils.requireLogin(null, '收藏夹功能需要登录后才能使用')) {
      return
    }
    wx.navigateTo({
      url: '/pages/favorite/favorite'
    })
  },

  goToTrain() {
    wx.navigateTo({
      url: '/pages/camp-list/camp-list'
    })
  },

  goToInterview() {
    wx.navigateTo({
      url: '/pages/interview/interview'
    })
  },

  goToAnswer() {
    wx.navigateTo({
      url: '/pages/my-answers/my-answers'
    })
  },

  goToRank() {
    wx.navigateTo({
      url: '/pages/rank/rank'
    })
  },

  goToFriends() {
    if (!guestUtils.requireLogin(null, '好友功能需要登录后才能使用')) {
      return
    }
    wx.navigateTo({
      url: '/pages/friends/friends'
    })
  },

  loadUnreadCount() {
    if (guestUtils.checkGuest()) {
      this.setData({ unreadCount: 0 })
      return
    }
    const userId = wx.getStorageSync('userId') || 1
    const app = getApp()
    wx.request({
      url: `${app.globalData.userUrl}/chat/conversations`,
      method: 'GET',
      data: { userId },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          const conversations = res.data.data || []
          let totalUnread = 0
          conversations.forEach(conv => {
            const isUser1 = conv.user1_id === userId
            totalUnread += isUser1 ? conv.user1_unread : conv.user2_unread
          })
          this.setData({ unreadCount: totalUnread })
        }
      },
      fail: (err) => {
        console.error('加载未读消息数失败', err)
      }
    })
  },

  goToAchieve() {
    wx.navigateTo({
      url: '/pages/achievement/achievement'
    })
  },

  joinGroup() {
    wx.navigateTo({
      url: '/pages/group/group'
    })
  },

  contactUs() {
    wx.navigateTo({
      url: '/pages/contact/contact'
    })
  },

  goToSetting() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    })
  },

  goToFeedback() {
    wx.navigateTo({
      url: '/pages/feedback/feedback'
    })
  },

  goToAgreement() {
    wx.navigateTo({
      url: '/pages/agreement/agreement'
    })
  },

  goToPrivacy() {
    wx.navigateTo({
      url: '/pages/privacy/privacy'
    })
  },

  handleLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出当前账号吗？',
      confirmText: '退出',
      confirmColor: '#FA5151',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userId')
          wx.removeStorageSync('userInfo')
          wx.removeStorageSync('guestMode')
          
          const app = getApp()
          app.globalData.token = null
          app.globalData.userId = null
          app.globalData.userInfo = null

          wx.reLaunch({
            url: '/pages/login/login'
          })
        }
      }
    })
  }
})
