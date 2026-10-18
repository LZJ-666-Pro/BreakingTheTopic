const app = getApp()

Page({
  data: {
    contactInfo: {
      wechat: '',
      email: '',
      qq: '',
      weibo: '',
      workTime: ''
    },
    // 后台「网站设置」跨库下发（site_name/site_description/site_icp/site_copyright）
    siteInfo: {
      name: '',
      description: '',
      icp: '',
      copyright: ''
    },
    isAllEmpty: false
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
          const contactInfo = {
            wechat: config.contact_wechat || '',
            email: config.contact_email || '',
            qq: config.contact_qq || '',
            weibo: config.contact_weibo || '',
            workTime: config.contact_work_time || ''
          }
          const siteInfo = {
            name: config.site_name || '',
            description: config.site_description || '',
            icp: config.site_icp || '',
            copyright: config.site_copyright || ''
          }
          // 全部为空时展示空状态引导
          const isAllEmpty = !contactInfo.wechat && !contactInfo.qq &&
            !contactInfo.email && !contactInfo.weibo
          this.setData({ contactInfo, siteInfo, isAllEmpty })
        }
      }
    })
  },

  goFeedback() {
    wx.navigateTo({
      url: '/pages/feedback/feedback'
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
