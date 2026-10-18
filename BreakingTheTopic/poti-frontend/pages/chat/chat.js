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
    
    // 头像地址兼容历史数据：老数据存的是 localhost/旧 IP，统一把 host 部分替换为当前配置的服务地址
    const fixAvatarUrl = (url) => (url ? url.replace(/^https?:\/\/[^/]+/, app.globalData.userUrl) : url)
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

    // 录音结束监听只注册一次，避免每次录音重复上传
    this.recorderManager.onStop((res) => {
      const { tempFilePath, duration } = res
      this.recordDuration = Math.floor(duration / 1000)
      this.uploadFile(tempFilePath, 3)
    })

    // 播放失败输出日志，避免静默无响应
    this.innerAudioContext.onError((e) => {
      console.error('语音播放失败', e)
    })

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
          // 媒体地址兼容历史数据：老数据存的是 localhost/旧 IP，统一把 host 部分替换为当前配置的服务地址
          const fixMediaUrl = (url) => (url ? url.replace(/^https?:\/\/[^/]+/, app.globalData.userUrl) : url)
          const messages = (res.data.data || []).map(msg => ({
            ...msg,
            media_url: fixMediaUrl(msg.media_url)
          }))
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
        console.log('[chat] 上传响应', res.statusCode, res.data)
        let data = null
        try {
          data = JSON.parse(res.data)
        } catch (e) {
          console.error('[chat] 上传响应解析失败', res.data)
          wx.showToast({ title: '上传响应异常', icon: 'none' })
          return
        }
        if (data.code === 200) {
          const mediaUrl = data.data
          if (type === 2) {
            this.sendImageMessage(mediaUrl)
          } else if (type === 3) {
            this.sendVoiceMessage(mediaUrl)
          }
        } else if (data.code === 401) {
          app._handleUnauthorized()
        } else {
          wx.showToast({ title: data.msg || '上传失败', icon: 'none' })
        }
      },
      fail: (err) => {
        console.error('[chat] 上传请求失败', err)
        wx.showToast({ title: '网络错误', icon: 'none' })
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
        } else if (res.data.code === 401) {
          app._handleUnauthorized()
        } else {
          console.error('[chat] 图片消息发送失败', res.data)
          wx.showToast({ title: res.data.msg || '发送失败', icon: 'none' })
        }
      },
      fail: (err) => {
        console.error('[chat] 图片消息请求失败', err)
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  startRecord() {
    this.setData({ isRecording: true })
    // 格式必须用 aac：开发者工具不支持 mp3 录制，录出的文件无法播放
    this.recorderManager.start({
      format: 'aac',
      duration: 60000
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
        } else if (res.data.code === 401) {
          app._handleUnauthorized()
        } else {
          console.error('[chat] 语音消息发送失败', res.data)
          wx.showToast({ title: res.data.msg || '发送失败', icon: 'none' })
        }
      },
      fail: (err) => {
        console.error('[chat] 语音消息请求失败', err)
        wx.showToast({ title: '网络错误', icon: 'none' })
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
      this.pollMessages()
    }, 3000)
  },

  // 轮询增量拉取：只取最后一条消息之后的新消息，避免每 3 秒全量重拉 + 重复写库
  pollMessages() {
    if (!this.data.conversationId) {
      return
    }
    const messages = this.data.messages
    const afterId = messages.length > 0 ? messages[messages.length - 1].id : null
    wx.request({
      url: `${app.globalData.userUrl}/chat/messages`,
      method: 'GET',
      data: afterId
        ? { userId: this.data.userId, conversationId: this.data.conversationId, afterId: afterId, size: 50 }
        : { userId: this.data.userId, conversationId: this.data.conversationId, page: 1, size: 100 },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code !== 200) {
          return
        }
        const newMsgs = res.data.data || []
        if (afterId) {
          if (newMsgs.length > 0) {
            this.setData({
              messages: messages.concat(newMsgs),
              scrollToView: `msg-${newMsgs[newMsgs.length - 1].id}`
            })
          }
        } else if (newMsgs.length > 0) {
          this.setData({ messages: newMsgs })
          this.setData({ scrollToView: `msg-${newMsgs[newMsgs.length - 1].id}` })
        }
      }
    })
  }
})
