const app = getApp()
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    userId: null,
    friendId: null,
    friendName: '',
    friendAvatar: '',
    userAvatar: '',
    messages: [],
    inputMessage: '',
    scrollToView: '',
    conversationId: null,
    isRecording: false,
    recorderManager: null,
    innerAudioContext: null
  },

  onLoad(options) {
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
    
    // 头像地址兼容历史数据：老数据存的是 localhost，真机无法访问，统一替换为配置的服务地址
    const fixAvatarUrl = (url) => (url || '').replace('http://localhost:8200', app.globalData.userUrl)
    this.setData({
      userId: app.globalData.userId || 1,
      friendId: parseInt(options.friendId),
      friendName: options.friendName || '好友',
      friendAvatar: fixAvatarUrl(decodeURIComponent(options.friendAvatar || '')),
      userAvatar: fixAvatarUrl(app.globalData.userInfo?.avatarUrl)
    })
    
    wx.setNavigationBarTitle({
      title: this.data.friendName
    })
    
    this.recorderManager = wx.getRecorderManager()
    this.innerAudioContext = wx.createInnerAudioContext()
    
    this.loadMessages()
    this.startPolling()
  },

  onUnload() {
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer)
    }
    if (this.innerAudioContext) {
      this.innerAudioContext.destroy()
    }
  },

  loadMessages() {
    wx.request({
      url: `${app.globalData.userUrl}/chat/conversations`,
      method: 'GET',
      data: { userId: this.data.userId },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          const conversations = res.data.data || []
          const conversation = conversations.find(c => c.other_user_id === this.data.friendId)
          
          if (conversation) {
            this.setData({ conversationId: conversation.id })
            this.getMessages(conversation.id)
          }
        }
      }
    })
  },

  getMessages(conversationId) {
    wx.request({
      url: `${app.globalData.userUrl}/chat/messages`,
      method: 'GET',
      data: {
        userId: this.data.userId,
        conversationId: conversationId,
        page: 1,
        size: 100
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          const messages = res.data.data || []
          this.setData({ messages })
          
          if (messages.length > 0) {
            const lastMsg = messages[messages.length - 1]
            this.setData({
              scrollToView: `msg-${lastMsg.id}`
            })
          }
        }
      }
    })
  },

  onInput(e) {
    this.setData({
      inputMessage: e.detail.value
    })
  },

  sendMessage() {
    if (!this.data.inputMessage.trim()) {
      return
    }
    
    const content = this.data.inputMessage.trim()
    
    wx.request({
      url: `${app.globalData.userUrl}/chat/send`,
      method: 'POST',
      header: {
        'content-type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      data: {
        senderId: this.data.userId,
        receiverId: this.data.friendId,
        content: content,
        type: 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            inputMessage: ''
          })
          
          if (this.data.conversationId) {
            this.getMessages(this.data.conversationId)
          } else {
            this.loadMessages()
          }
        } else {
          wx.showToast({
            title: '发送失败',
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

  chooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        this.uploadFile(tempFilePath, 2)
      }
    })
  },

  uploadFile(filePath, type) {
    wx.uploadFile({
      url: `${app.globalData.userUrl}/chat/upload`,
      filePath: filePath,
      name: 'file',
      formData: {
        type: type
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          const mediaUrl = data.data
          if (type === 2) {
            this.sendImageMessage(mediaUrl)
          } else if (type === 3) {
            this.sendVoiceMessage(mediaUrl)
          }
        } else {
          wx.showToast({
            title: '上传失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.showToast({
          title: '上传失败',
          icon: 'none'
        })
      }
    })
  },

  sendImageMessage(mediaUrl) {
    wx.request({
      url: `${app.globalData.userUrl}/chat/send`,
      method: 'POST',
      header: {
        'content-type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      data: {
        senderId: this.data.userId,
        receiverId: this.data.friendId,
        type: 2,
        mediaUrl: mediaUrl
      },
      success: (res) => {
        if (res.data.code === 200) {
          if (this.data.conversationId) {
            this.getMessages(this.data.conversationId)
          } else {
            this.loadMessages()
          }
        }
      }
    })
  },

  startRecord() {
    this.setData({ isRecording: true })
    this.recorderManager.start({
      format: 'mp3',
      duration: 60000
    })
    
    this.recorderManager.onStop((res) => {
      const { tempFilePath, duration } = res
      const durationSec = Math.floor(duration / 1000)
      this.uploadFile(tempFilePath, 3)
      this.recordDuration = durationSec
    })
  },

  stopRecord() {
    this.setData({ isRecording: false })
    this.recorderManager.stop()
  },

  sendVoiceMessage(mediaUrl) {
    wx.request({
      url: `${app.globalData.userUrl}/chat/send`,
      method: 'POST',
      header: {
        'content-type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      data: {
        senderId: this.data.userId,
        receiverId: this.data.friendId,
        type: 3,
        mediaUrl: mediaUrl,
        duration: this.recordDuration || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          if (this.data.conversationId) {
            this.getMessages(this.data.conversationId)
          } else {
            this.loadMessages()
          }
        }
      }
    })
  },

  playVoice(e) {
    const url = e.currentTarget.dataset.url
    this.innerAudioContext.src = url
    this.innerAudioContext.play()
  },

  previewImage(e) {
    const url = e.currentTarget.dataset.url
    wx.previewImage({
      urls: [url],
      current: url
    })
  },

  deleteMessage(e) {
    const messageId = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确定删除这条消息吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.userUrl}/chat/message/${messageId}?userId=${this.data.userId}`,
            method: 'DELETE',
            header: {
              'Authorization': `Bearer ${app.globalData.token}`,
              'X-User-Id': app.globalData.userId || 1
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({
                  title: '删除成功',
                  icon: 'success'
                })
                if (this.data.conversationId) {
                  this.getMessages(this.data.conversationId)
                }
              } else {
                wx.showToast({
                  title: res.data.message || '删除失败',
                  icon: 'none'
                })
              }
            }
          })
        }
      }
    })
  },

  startPolling() {
    this.pollingTimer = setInterval(() => {
      if (this.data.conversationId) {
        this.getMessages(this.data.conversationId)
      }
    }, 3000)
  }
})
