const config = require('./config/index.js')

App({
  onLaunch() {
    console.log('小程序启动')
    this.checkLogin()
  },

  onShow() {
    console.log('小程序显示')
  },

  onHide() {
    console.log('小程序隐藏')
  },

  onError(msg) {
    console.error('小程序错误', msg)
  },

  checkLogin() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
      this.globalData.userId = wx.getStorageSync('userId')
      this.globalData.userInfo = wx.getStorageSync('userInfo')
    }
  },

  request(options) {
    const token = this.globalData.token
    const userId = this.globalData.userId

    if (this.globalData._isLoggingOut) {
      return wx.request({
        url: `${this.globalData.baseUrl}${options.url}`,
        method: options.method || 'GET',
        data: options.data || {},
        header: options.header || {}
      })
    }

    const defaultOptions = {
      url: `${this.globalData.baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      success: (res) => {
        const data = res.data
        if (data && data.code === 401) {
          this._handleUnauthorized()
          return
        }

        if (options.success) {
          options.success(res)
        }
      },
      fail: (err) => {
        console.error('请求失败', options.url, err)
        if (options.fail) {
          options.fail(err)
        }
      },
      complete: options.complete
    }

    if (token) {
      defaultOptions.header['Authorization'] = `Bearer ${token}`
    }

    if (userId) {
      defaultOptions.header['X-User-Id'] = userId
    }

    return wx.request(defaultOptions)
  },

  _handleUnauthorized() {
    if (this.globalData._isLoggingOut) return
    
    const guestMode = wx.getStorageSync('guestMode')
    if (guestMode) {
      console.log('游客模式，忽略401错误')
      return
    }
    
    const token = wx.getStorageSync('token')
    if (!token) {
      console.log('非微信登录用户，忽略401错误')
      return
    }
    
    this.globalData._isLoggingOut = true

    wx.removeStorageSync('token')
    wx.removeStorageSync('userId')
    wx.removeStorageSync('userInfo')
    this.globalData.token = null
    this.globalData.userId = null
    this.globalData.userInfo = null

    wx.showToast({
      title: '登录已过期，请重新登录',
      icon: 'none',
      duration: 1500
    })

    setTimeout(() => {
      this.globalData._isLoggingOut = false
      wx.reLaunch({
        url: '/pages/login/login'
      })
    }, 1500)
  },

  globalData: {
    userInfo: null,
    token: null,
    userId: null,
    baseUrl: config.baseUrl,
    authUrl: config.authUrl,
    userUrl: config.userUrl,
    questionUrl: config.questionUrl,
    practiceUrl: config.practiceUrl,
    wrongbookUrl: config.wrongbookUrl,
    favoriteUrl: config.favoriteUrl,
    interviewUrl: config.interviewUrl,
    searchUrl: config.searchUrl,
    viewQuestionId: null,
    practiceType: null
  }
})
