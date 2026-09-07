const app = getApp()

Page({
  data: {
    currentType: 'bug',
    content: '',
    contact: '',
    submitting: false,
    feedbackList: []
  },

  onLoad() {
    this.loadFeedbackList()
  },

  onShow() {
    this.loadFeedbackList()
  },

  loadFeedbackList() {
    const userId = app.globalData.userId
    if (!userId) return
    
    wx.request({
      url: `${app.globalData.userUrl}/feedback/list`,
      method: 'GET',
      header: { 'X-User-Id': userId },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ feedbackList: res.data.data || [] })
        }
      }
    })
  },

  selectType(e) {
    this.setData({ currentType: e.currentTarget.dataset.type })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  onContactInput(e) {
    this.setData({ contact: e.detail.value })
  },

  submitFeedback() {
    if (!this.data.content.trim()) {
      wx.showToast({ title: '请输入内容', icon: 'none' })
      return
    }
    
    if (this.data.content.length < 10) {
      wx.showToast({ title: '内容至少10字', icon: 'none' })
      return
    }
    
    this.setData({ submitting: true })
    
    const userId = app.globalData.userId
    const userInfo = app.globalData.userInfo || {}
    
    wx.request({
      url: `${app.globalData.userUrl}/feedback/submit`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'X-User-Id': userId || ''
      },
      data: {
        userId: userId,
        nickname: userInfo.nickname || '',
        type: this.data.currentType,
        content: this.data.content,
        contact: this.data.contact
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '提交成功', icon: 'success' })
          this.setData({ content: '', contact: '', currentType: 'bug' })
          this.loadFeedbackList()
        } else {
          wx.showToast({ title: res.data.msg || '提交失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '提交失败', icon: 'none' })
      },
      complete: () => {
        this.setData({ submitting: false })
      }
    })
  },

  deleteFeedback(e) {
    const id = e.currentTarget.dataset.id
    
    wx.showModal({
      title: '提示',
      content: '确定删除这条反馈吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.userUrl}/feedback/delete/${id}`,
            method: 'DELETE',
            header: { 'X-User-Id': app.globalData.userId },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '删除成功', icon: 'success' })
                this.loadFeedbackList()
              } else {
                wx.showToast({ title: res.data.msg || '删除失败', icon: 'none' })
              }
            },
            fail: () => {
              wx.showToast({ title: '删除失败', icon: 'none' })
            }
          })
        }
      }
    })
  }
})
