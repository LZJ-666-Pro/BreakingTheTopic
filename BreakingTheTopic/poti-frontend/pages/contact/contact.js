const app = getApp()

Page({
  data: {
    contactInfo: {
      wechat: '',
      email: '',
      qq: '',
      weibo: '',
      workTime: ''
    }
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
          this.setData({
            contactInfo: {
              wechat: config.contact_wechat || '',
              email: config.contact_email || '',
              qq: config.contact_qq || '',
              weibo: config.contact_weibo || '',
              workTime: config.contact_work_time || ''
            }
          })
        }
      }
    })
  },

  copyText(e) {
    const text = e.currentTarget.dataset.text
    if (!text) {
      wx.showToast({ title: '暂未配置', icon: 'none' })
      return
    }
    wx.setClipboardData({
      data: text,
      success: () => {
        wx.showToast({ title: '已复制', icon: 'success' })
      }
    })
  }
})
