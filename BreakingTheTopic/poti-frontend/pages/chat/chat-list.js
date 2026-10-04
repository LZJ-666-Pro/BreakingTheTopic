const app = getApp()
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    conversations: []
  },

  onLoad() {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '聊天功能需要登录后才能使用',
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
    this.loadConversations()
    this.startPolling()
  },

  onShow() {
    if (guestUtils.checkGuest()) {
      return
    }
    this.loadConversations()
  },

  onUnload() {
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer)
    }
  },

  loadConversations() {
    wx.request({
      url: `${app.globalData.userUrl}/chat/conversations`,
      method: 'GET',
      data: { userId: app.globalData.userId || 1 },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data && res.data.code === 200) {
          const conversations = res.data.data || []
          let totalUnread = 0
          
          conversations.forEach(conv => {
            const isUser1 = conv.user1_id === (app.globalData.userId || 1)
            conv.unread = isUser1 ? conv.user1_unread : conv.user2_unread
            totalUnread += conv.unread
            
            if (conv.last_message_time) {
              const time = new Date(conv.last_message_time)
              const now = new Date()
              const diff = now - time
              
              if (diff < 60000) {
                conv.last_message_time = '刚刚'
              } else if (diff < 3600000) {
                conv.last_message_time = Math.floor(diff / 60000) + '分钟前'
              } else if (diff < 86400000) {
                conv.last_message_time = Math.floor(diff / 3600000) + '小时前'
              } else {
                conv.last_message_time = time.toLocaleDateString()
              }
            }
          })
          
          this.setData({ conversations })
          
          if (totalUnread > 0) {
            wx.setTabBarBadge({
              index: 3,
              text: totalUnread > 99 ? '99+' : totalUnread.toString()
            })
          } else {
            wx.removeTabBarBadge({
              index: 3
            })
          }
        }
      }
    })
  },

  startPolling() {
    this.pollingTimer = setInterval(() => {
      this.loadConversations()
    }, 5000)
  },

  openChat(e) {
    const item = e.currentTarget.dataset.item
    wx.navigateTo({
      url: `/pages/chat/chat?friendId=${item.other_user_id}&friendName=${item.nickname}&friendAvatar=${item.avatar_url}`
    })
  }
})
