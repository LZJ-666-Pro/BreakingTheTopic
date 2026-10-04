Page({
  data: {
    loading: false,
    btnPressed: false,
    toastMsg: '',
    toastShow: false
  },

  onLoad() {
    this.checkLogin()
  },

  checkLogin() {
    const app = getApp()
    const token = wx.getStorageSync('token')
    if (token) {
      app.globalData.token = token
      app.globalData.userInfo = wx.getStorageSync('userInfo')
      wx.switchTab({
        url: '/pages/index/index'
      })
    }
  },

  onBtnStart() {
    this.setData({ btnPressed: true })
  },

  onBtnEnd() {
    this.setData({ btnPressed: false })
  },

  showToast(msg) {
    this.setData({ toastMsg: msg, toastShow: true })
    setTimeout(() => {
      this.setData({ toastShow: false })
      setTimeout(() => {
        this.setData({ toastMsg: '' })
      }, 300)
    }, 2000)
  },

  handleLogin() {
    if (this.data.loading) return

    this.setData({ loading: true })

    wx.login({
      success: (res) => {
        if (res.code) {
          this.getToken(res.code)
        } else {
          this.showToast('获取登录凭证失败')
          this.setData({ loading: false })
        }
      },
      fail: () => {
        this.showToast('登录失败，请重试')
        this.setData({ loading: false })
      }
    })
  },

  getToken(code) {
    const app = getApp()
    wx.request({
      url: `${app.globalData.authUrl}/auth/login`,
      method: 'POST',
      data: { code },
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          const { token, userId, nickname, avatarUrl } = data

          wx.setStorageSync('token', token)
          wx.setStorageSync('userId', userId)

          const userInfo = {
            id: userId,
            userId: userId,
            nickname: nickname || '微信用户',
            avatarUrl: avatarUrl || ''
          }

          wx.setStorageSync('userInfo', userInfo)

          app.globalData.token = token
          app.globalData.userId = userId
          app.globalData.userInfo = userInfo

          this.showToast(`授权成功，欢迎 ${userInfo.nickname}`)

          setTimeout(() => {
            wx.switchTab({ url: '/pages/index/index' })
          }, 1500)
        } else {
          this.showToast(res.data.msg || '登录失败')
        }
      },
      fail: () => {
        this.showToast('网络错误，请检查网络')
      },
      complete: () => {
        this.setData({ loading: false })
      }
    })
  },

  handlePhoneLogin() {
    wx.navigateTo({
      url: '/pages/phone-login/phone-login'
    })
  },

  goToRegister() {
    wx.navigateTo({
      url: '/pages/register/register'
    })
  },

  handleGuestLogin() {
    const app = getApp()
    
    const guestUserInfo = {
      id: 0,
      userId: 0,
      nickname: '游客用户',
      avatarUrl: '',
      isGuest: true
    }
    
    wx.setStorageSync('guestMode', true)
    wx.setStorageSync('userInfo', guestUserInfo)
    
    app.globalData.userInfo = guestUserInfo
    app.globalData.guestMode = true
    
    this.showToast('游客模式数据仅保存在本地，升级账号后可同步')
    
    setTimeout(() => {
      wx.switchTab({
        url: '/pages/index/index',
        fail: () => {
          wx.reLaunch({
            url: '/pages/index/index'
          })
        }
      })
    }, 1500)
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
  }
})
