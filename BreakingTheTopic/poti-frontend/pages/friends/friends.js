const app = getApp()
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    searchKeyword: '',
    searchResults: [],
    activeTab: 0,
    friendList: [],
    pendingRequests: [],
    sentRequests: [],
    showRequestDialog: false,
    requestMessage: '',
    selectedUser: null,
    showInviteDialog: false,
    inviteCode: '',
    showRecommendDialog: false,
    recommendations: [],
    showAddByCodeDialog: false,
    inputInviteCode: ''
  },

  onLoad() {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '好友功能需要登录后才能使用',
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
    this.loadFriendList()
    this.loadPendingRequests()
    this.loadSentRequests()
  },

  onShow() {
    if (guestUtils.checkGuest()) {
      return
    }
    this.loadFriendList()
    this.loadPendingRequests()
    this.loadSentRequests()
  },

  onSearchChange(e) {
    this.setData({ searchKeyword: e.detail })
  },

  onSearch() {
    const keyword = this.data.searchKeyword.trim()
    if (!keyword) {
      wx.showToast({ title: '请输入搜索关键词', icon: 'none' })
      return
    }

    wx.request({
      url: `${app.globalData.userUrl}/user/search`,
      method: 'GET',
      data: { keyword },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          const results = res.data.data || []
          this.setData({ searchResults: results })
          if (results.length === 0) {
            wx.showToast({ title: '未找到相关用户', icon: 'none' })
          }
        } else {
          wx.showToast({ title: res.data.message || '搜索失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  sendRequest(e) {
    const user = e.currentTarget.dataset.user
    this.setData({
      selectedUser: user,
      showRequestDialog: true,
      requestMessage: ''
    })
  },

  onMessageInput(e) {
    this.setData({ requestMessage: e.detail.value })
  },

  confirmSendRequest() {
    const { selectedUser, requestMessage } = this.data
    if (!selectedUser) return

    wx.request({
      url: `${app.globalData.userUrl}/friendship/request`,
      method: 'POST',
      header: {
        'content-type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      data: {
        userId: app.globalData.userId || 1,
        friendId: selectedUser.id,
        message: requestMessage || ''
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '申请已发送', icon: 'success' })
          this.setData({
            showRequestDialog: false,
            searchResults: []
          })
          this.loadSentRequests()
        } else {
          wx.showToast({ title: res.data.message || '发送失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  cancelSendRequest() {
    this.setData({ showRequestDialog: false })
  },

  onTabChange(e) {
    this.setData({ activeTab: e.detail.index })
  },

  loadFriendList() {
    console.log('加载好友列表')
    wx.request({
      url: `${app.globalData.userUrl}/friendship/list`,
      method: 'GET',
      data: { userId: app.globalData.userId || 1 },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        console.log('好友列表响应:', res.data)
        if (res.data.code === 200) {
          const friendList = res.data.data || []
          console.log('好友列表数据:', friendList)
          
          wx.request({
            url: `${app.globalData.userUrl}/chat/conversations`,
            method: 'GET',
            data: { userId: app.globalData.userId || 1 },
            header: {
              'Authorization': `Bearer ${app.globalData.token}`,
              'X-User-Id': app.globalData.userId || 1
            },
            success: (convRes) => {
              if (convRes.data.code === 200) {
                const conversations = convRes.data.data || []
                
                friendList.forEach(friend => {
                  const conv = conversations.find(c => c.other_user_id === friend.user_id)
                  if (conv) {
                    friend.conversationId = conv.id
                    const isUser1 = conv.user1_id === (app.globalData.userId || 1)
                    friend.unreadCount = isUser1 ? conv.user1_unread : conv.user2_unread
                    friend.lastMessage = conv.last_message
                    
                    if (conv.last_message_time) {
                      const time = new Date(conv.last_message_time)
                      const now = new Date()
                      const diff = now - time
                      
                      if (diff < 60000) {
                        friend.lastMessageTime = '刚刚'
                      } else if (diff < 3600000) {
                        friend.lastMessageTime = Math.floor(diff / 60000) + '分钟前'
                      } else if (diff < 86400000) {
                        friend.lastMessageTime = Math.floor(diff / 3600000) + '小时前'
                      } else {
                        friend.lastMessageTime = time.toLocaleDateString()
                      }
                    }
                  } else {
                    friend.conversationId = null
                    friend.unreadCount = 0
                    friend.lastMessage = null
                    friend.lastMessageTime = null
                  }
                })
                
                this.setData({ friendList })
              }
            }
          })
        } else {
          console.error('获取好友列表失败:', res.data.message)
        }
      },
      fail: (err) => {
        console.error('网络错误:', err)
      }
    })
  },

  loadPendingRequests() {
    wx.request({
      url: `${app.globalData.userUrl}/friendship/pending`,
      method: 'GET',
      data: { userId: app.globalData.userId || 1 },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ pendingRequests: res.data.data || [] })
        }
      }
    })
  },

  loadSentRequests() {
    wx.request({
      url: `${app.globalData.userUrl}/friendship/sent`,
      method: 'GET',
      data: { userId: app.globalData.userId || 1 },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ sentRequests: res.data.data || [] })
        }
      }
    })
  },

  acceptRequest(e) {
    const requestId = e.currentTarget.dataset.id
    console.log('接受好友申请，requestId:', requestId)
    
    wx.request({
      url: `${app.globalData.userUrl}/friendship/accept`,
      method: 'POST',
      header: {
        'content-type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      data: {
        requestId,
        userId: app.globalData.userId || 1
      },
      success: (res) => {
        console.log('接受好友申请响应:', res.data)
        if (res.data.code === 200) {
          wx.showToast({ title: '已接受', icon: 'success' })
          this.loadPendingRequests()
          this.loadFriendList()
        } else {
          wx.showToast({ title: res.data.message || '操作失败', icon: 'none' })
        }
      },
      fail: (err) => {
        console.error('网络错误:', err)
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  rejectRequest(e) {
    const requestId = e.currentTarget.dataset.id
    wx.request({
      url: `${app.globalData.userUrl}/friendship/reject`,
      method: 'POST',
      header: {
        'content-type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      data: {
        requestId,
        userId: app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '已拒绝', icon: 'success' })
          this.loadPendingRequests()
        } else {
          wx.showToast({ title: res.data.message || '操作失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  deleteFriend(e) {
    const friendId = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '确定要删除该好友吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.userUrl}/friendship/delete`,
            method: 'DELETE',
            header: {
              'content-type': 'application/x-www-form-urlencoded',
              'Authorization': `Bearer ${app.globalData.token}`,
              'X-User-Id': app.globalData.userId || 1
            },
            data: {
              userId: app.globalData.userId || 1,
              friendId
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已删除', icon: 'success' })
                this.loadFriendList()
              } else {
                wx.showToast({ title: res.data.message || '删除失败', icon: 'none' })
              }
            },
            fail: () => {
              wx.showToast({ title: '网络错误', icon: 'none' })
            }
          })
        }
      }
    })
  },

  deleteConversation(e) {
    const friend = e.currentTarget.dataset.friend
    if (!friend.conversationId) {
      wx.showToast({
        title: '暂无聊天记录',
        icon: 'none'
      })
      return
    }
    
    wx.showModal({
      title: '确认删除',
      content: '确定要删除与该好友的聊天记录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.userUrl}/chat/conversation/${friend.conversationId}?userId=${app.globalData.userId || 1}`,
            method: 'DELETE',
            header: {
              'Authorization': `Bearer ${app.globalData.token}`,
              'X-User-Id': app.globalData.userId || 1
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({
                  title: '已删除聊天记录',
                  icon: 'success'
                })
                this.loadFriendList()
              } else {
                wx.showToast({
                  title: res.data.message || '删除失败',
                  icon: 'none'
                })
              }
            },
            fail: () => {
              wx.showToast({
                title: '网络错误',
                icon: 'none'
              })
            }
          })
        }
      }
    })
  },

  openChat(e) {
    const friend = e.currentTarget.dataset.friend
    wx.navigateTo({
      url: `/pages/chat/chat?friendId=${friend.user_id}&friendName=${friend.nickname || '好友'}&friendAvatar=${friend.avatar_url || ''}`
    })
  },
  
  showInviteCode() {
    this.setData({ showInviteDialog: true })
    
    wx.request({
      url: `${app.globalData.userUrl}/friendship/invite-code?userId=${app.globalData.userId || 1}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ inviteCode: res.data.data })
        } else {
          wx.showToast({
            title: res.data.message || '获取邀请码失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    })
  },
  
  closeInviteDialog() {
    this.setData({ showInviteDialog: false })
  },
  
  copyInviteCode() {
    wx.setClipboardData({
      data: this.data.inviteCode,
      success: () => {
        wx.showToast({
          title: '已复制邀请码',
          icon: 'success'
        })
      }
    })
  },
  
  showRecommendations() {
    this.setData({ showRecommendDialog: true })
    
    wx.request({
      url: `${app.globalData.userUrl}/friendship/recommendations?userId=${app.globalData.userId || 1}&limit=20`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ recommendations: res.data.data || [] })
        } else {
          wx.showToast({
            title: res.data.message || '获取推荐失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    })
  },
  
  closeRecommendDialog() {
    this.setData({ showRecommendDialog: false })
  },
  
  addRecommendFriend(e) {
    const user = e.currentTarget.dataset.user
    this.setData({
      showRecommendDialog: false,
      showRequestDialog: true,
      selectedUser: user,
      requestMessage: ''
    })
  },
  
  showAddByCodeDialog() {
    this.setData({
      showAddByCodeDialog: true,
      inputInviteCode: ''
    })
  },
  
  closeAddByCodeDialog() {
    this.setData({ showAddByCodeDialog: false })
  },
  
  onInviteCodeInput(e) {
    this.setData({ inputInviteCode: e.detail.value })
  },
  
  confirmAddByCode() {
    const inviteCode = this.data.inputInviteCode.trim()
    
    if (!inviteCode) {
      wx.showToast({
        title: '请输入邀请码',
        icon: 'none'
      })
      return
    }
    
    wx.request({
      url: `${app.globalData.userUrl}/friendship/add-by-code?userId=${app.globalData.userId || 1}&inviteCode=${inviteCode}`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '已发送好友申请',
            icon: 'success'
          })
          this.setData({
            showAddByCodeDialog: false,
            inputInviteCode: ''
          })
          this.loadSentRequests()
        } else {
          wx.showToast({
            title: res.data.message || '添加失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    })
  }
})
