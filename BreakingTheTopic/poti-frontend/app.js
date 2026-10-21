const config = require('./config/index.js')
const imageUtil = require('./utils/image.js')

App({
  onLaunch() {
    console.log('小程序启动')
    // 统一归一化接口返回的图片地址（相对路径 / 内网绝对地址 → 当前 baseUrl）
    this.installMediaUrlNormalizer()
    // 每次冷启动重置 AI 悬浮球收起状态，避免用户永久找不到入口
    wx.removeStorageSync('agentFabClosed')
    this.checkLogin()
  },

  onShow() {
    console.log('小程序显示')
    this.ensureFreshToken()
  },

  onHide() {
    console.log('小程序隐藏')
  },

  onError(msg) {
    console.error('小程序错误', msg)
  },

  /**
   * 给 wx.request 装一个响应拦截：自动把返回数据里的头像/聊天媒体地址
   * 归一化为当前 baseUrl 下的绝对地址。
   * 后端只存相对路径（/avatars/xxx.jpg），这样切本地 http / 公网 https 都能显示。
   */
  installMediaUrlNormalizer() {
    if (this._mediaUrlPatched) return
    this._mediaUrlPatched = true

    const rawRequest = wx.request
    wx.request = function (options) {
      if (options && typeof options.success === 'function') {
        const originSuccess = options.success
        options.success = function (res) {
          try {
            if (res && res.data) {
              imageUtil.normalizeDeep(res.data, 0)
            }
          } catch (e) {
            console.warn('图片地址归一化失败', e)
          }
          return originSuccess(res)
        }
      }
      return rawRequest(options)
    }
  },

  checkLogin() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
      this.globalData.userId = wx.getStorageSync('userId')
      const cachedUser = wx.getStorageSync('userInfo')
      if (cachedUser && cachedUser.avatarUrl) {
        cachedUser.avatarUrl = imageUtil.resolveImageUrl(cachedUser.avatarUrl)
        wx.setStorageSync('userInfo', cachedUser)
      }
      this.globalData.userInfo = cachedUser
    }
  },

  // 解析 JWT payload（小程序无 atob，用 wx.base64ToArrayBuffer）
  _decodeJwtPayload(token) {
    try {
      const parts = token.split('.')
      if (parts.length !== 3) return null
      let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
      while (base64.length % 4 !== 0) {
        base64 += '='
      }
      const bytes = new Uint8Array(wx.base64ToArrayBuffer(base64))
      let str = ''
      for (let i = 0; i < bytes.length; i++) {
        str += String.fromCharCode(bytes[i])
      }
      return JSON.parse(str)
    } catch (e) {
      return null
    }
  },

  // JWT 无感刷新（滑动续期）：剩余有效期不足 12h 时换发新 token，活跃用户不掉线；
  // 已过期则刷新被拒，走原有 401 踢登录兜底。onShow 节流 1 小时检查一次。
  ensureFreshToken() {
    const token = this.globalData.token || wx.getStorageSync('token')
    if (!token || this.globalData._isLoggingOut) return

    const now = Date.now()
    if (now - (this._lastTokenCheck || 0) < 3600 * 1000) return
    this._lastTokenCheck = now

    const payload = this._decodeJwtPayload(token)
    if (!payload || !payload.exp) return

    const remainMs = payload.exp * 1000 - now
    if (remainMs > 12 * 3600 * 1000) return

    if (this._refreshingToken) return
    this._refreshingToken = true

    wx.request({
      url: `${this.globalData.userUrl}/user/refresh`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        const data = res.data
        if (data && data.code === 200 && data.data && data.data.token) {
          wx.setStorageSync('token', data.data.token)
          this.globalData.token = data.data.token
          console.log('Token 已无感续期')
        } else {
          console.log('Token 续期被拒（可能已过期），将走重新登录流程')
        }
      },
      fail: (err) => {
        console.error('Token 续期请求失败', err)
      },
      complete: () => {
        this._refreshingToken = false
      }
    })
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
    agentUrl: config.agentUrl,
    viewQuestionId: null,
    practiceType: null
  }
})
