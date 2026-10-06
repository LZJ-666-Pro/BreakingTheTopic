Page({
  data: {
    phone: '',
    password: '',
    verifyCode: '',
    loading: false,
    showPassword: false,
    countdown: 0,
    canSendCode: true,
    loginMode: 'password'
  },

  timer: null,

  goBack() {
    wx.navigateBack()
  },

  switchMode(e) {
    const mode = e.currentTarget.dataset.mode
    this.setData({ loginMode: mode })
  },

  onPhoneInput(e) {
    this.setData({
      phone: e.detail.value
    })
    this.updateSendButtonState()
  },

  onPasswordInput(e) {
    this.setData({
      password: e.detail.value
    })
  },

  onVerifyCodeInput(e) {
    this.setData({
      verifyCode: e.detail.value
    })
  },

  togglePassword() {
    this.setData({
      showPassword: !this.data.showPassword
    })
  },

  updateSendButtonState() {
    const { phone, countdown } = this.data
    const canSend = phone.length === 11 && /^1[3-9]\d{9}$/.test(phone) && countdown === 0
    this.setData({ canSendCode: canSend })
  },

  sendVerifyCode() {
    const { phone, countdown, canSendCode } = this.data
    
    if (!canSendCode || countdown > 0) {
      return
    }
    
    if (!phone || !phone.trim()) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      })
      return
    }
    
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }
    
    const app = getApp()
    wx.request({
      url: `${app.globalData.baseUrl}/sms/send`,
      method: 'POST',
      data: {
        phone: phone.trim()
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '验证码已发送',
            icon: 'success'
          })
          this.startCountdown(60)
        } else {
          wx.showToast({
            title: res.data.msg || '发送失败',
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

  startCountdown(seconds) {
    this.setData({ countdown: seconds, canSendCode: false })
    
    this.timer = setInterval(() => {
      const newCountdown = this.data.countdown - 1
      if (newCountdown <= 0) {
        clearInterval(this.timer)
        this.setData({ countdown: 0 })
        this.updateSendButtonState()
      } else {
        this.setData({ countdown: newCountdown })
      }
    }, 1000)
  },

  handleLogin() {
    const { phone, password, verifyCode, loading, loginMode } = this.data
    
    if (loading) return
    
    if (!phone || !phone.trim()) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      })
      return
    }
    
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }
    
    if (loginMode === 'password') {
      if (!password || !password.trim()) {
        wx.showToast({
          title: '请输入密码',
          icon: 'none'
        })
        return
      }
      
      if (password.length < 6) {
        wx.showToast({
          title: '密码长度至少6位',
          icon: 'none'
        })
        return
      }
      
      this.loginWithPassword()
    } else {
      if (!verifyCode || !verifyCode.trim()) {
        wx.showToast({
          title: '请输入验证码',
          icon: 'none'
        })
        return
      }
      
      this.loginWithCode()
    }
  },

  loginWithPassword() {
    const { phone, password } = this.data
    
    this.setData({ loading: true })
    
    const app = getApp()
    wx.request({
      url: `${app.globalData.baseUrl}/user/login/phone`,
      method: 'POST',
      data: {
        phone: phone.trim(),
        password: password
      },
      success: (res) => {
        if (res.data.code === 200 && res.data.data && res.data.data.token) {
          this.handleLoginSuccess(res.data.data)
        } else {
          wx.showToast({
            title: res.data.msg || '登录失败，请重试',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        })
      },
      complete: () => {
        this.setData({ loading: false })
      }
    })
  },

  loginWithCode() {
    const { phone, verifyCode } = this.data
    
    this.setData({ loading: true })
    
    const app = getApp()
    wx.request({
      url: `${app.globalData.baseUrl}/user/login/code`,
      method: 'POST',
      data: {
        phone: phone.trim(),
        verifyCode: verifyCode.trim()
      },
      success: (res) => {
        if (res.data.code === 200 && res.data.data && res.data.data.token) {
          this.handleLoginSuccess(res.data.data)
        } else {
          wx.showToast({
            title: res.data.msg || '登录失败，请重试',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        })
      },
      complete: () => {
        this.setData({ loading: false })
      }
    })
  },

  handleLoginSuccess(data) {
    if (!data || !data.token) {
      wx.showToast({
        title: '登录响应异常，请重试',
        icon: 'none'
      })
      return
    }
    const { token, userId, nickname, phone, email, uniqueId, avatarUrl } = data
    
    wx.setStorageSync('token', token)
    wx.setStorageSync('userId', userId)
    
    const userInfo = {
      id: userId,
      userId: userId,
      nickname: nickname || '用户',
      phone: phone,
      email: email,
      uniqueId: uniqueId,
      avatarUrl: avatarUrl || '',
      isGuest: false
    }
    
    wx.setStorageSync('userInfo', userInfo)
    wx.setStorageSync('guestMode', false)
    
    const app = getApp()
    app.globalData.token = token
    app.globalData.userId = userId
    app.globalData.userInfo = userInfo
    app.globalData.guestMode = false
    
    wx.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500
    })
    
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

  goToRegister() {
    wx.navigateTo({
      url: '/pages/register/register'
    })
  },

  goToForgotPassword() {
    wx.navigateTo({
      url: '/pages/forgot-password/forgot-password'
    })
  },

  onUnload() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  }
})
