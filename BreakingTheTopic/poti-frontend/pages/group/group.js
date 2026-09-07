const app = getApp()

Page({
  data: {
    groups: [],
    wechatHelper: ''
  },

  onLoad() {
    this.loadConfig()
  },

  loadConfig() {
    wx.request({
      url: `${app.globalData.userUrl}/config/contact`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const config = res.data.data
          const groups = []
          
          for (let i = 1; i <= 4; i++) {
            const key = `group_qq_${i}`
            if (config[key]) {
              const parts = config[key].split(':')
              groups.push({
                id: i,
                name: parts[0] || config[key],
                qqNumber: parts[1] || ''
              })
            }
          }
          
          this.setData({
            groups,
            wechatHelper: config.contact_wechat || ''
          })
        }
      }
    })
  },

  copyQQNumber(e) {
    const qq = e.currentTarget.dataset.qq
    if (!qq) {
      wx.showToast({ title: '群号暂未配置', icon: 'none' })
      return
    }
    wx.setClipboardData({
      data: qq,
      success: () => {
        wx.showToast({ title: '已复制', icon: 'success' })
      }
    })
  },

  copyWechat() {
    if (!this.data.wechatHelper) {
      wx.showToast({ title: '微信号暂未配置', icon: 'none' })
      return
    }
    wx.setClipboardData({
      data: this.data.wechatHelper,
      success: () => {
        wx.showToast({ title: '已复制', icon: 'success' })
      }
    })
  }
})
