Page({
  data: {
    phone: '',
    verifyCode: '',
    newPassword: '',
    confirmPassword: '',
    showPassword: false,
    showConfirmPassword: false,
    loading: false,
    countdown: 0,
    canSendCode: true
  },

  timer: null,

  goBack() {
    wx.navigateBack()
  },

  goToLogin() {
    wx.navigateBack()
  },

  onPhoneInput(e) {
    this.setData({
      phone: e.detail.value
    })
    this.updateSendButtonState()
  },

  onVerifyCodeInput(e) {
    this.setData({
      verifyCode: e.detail.value
    })
  },

  onNewPasswordInput(e) {
    this.setData({
      newPassword: e.detail.value
    })
  },

  onConfirmPasswordInput(e) {
    this.setData({
      confirmPassword: e.detail.value
    })
  },

  togglePassword() {
    this.setData({
      showPassword: !this.data.showPassword
    })
  },

  toggleConfirmPassword() {
    this.setData({
      showConfirmPassword: !this.data.showConfirmPassword
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

  handleReset() {
    const { phone, verifyCode, newPassword, confirmPassword, loading } = this.data
    
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
    
    if (!verifyCode || !verifyCode.trim()) {
      wx.showToast({
        title: '请输入验证码',
        icon: 'none'
      })
      return
    }
    
    if (!newPassword || !newPassword.trim()) {
      wx.showToast({
        title: '请输入新密码',
        icon: 'none'
      })
      return
    }
    
    if (newPassword.length < 6 || newPassword.length > 20) {
      wx.showToast({
        title: '密码长度需6-20位',
        icon: 'none'
      })
      return
    }
    
    if (!confirmPassword || !confirmPassword.trim()) {
      wx.showToast({
        title: '请确认新密码',
        icon: 'none'
      })
      return
    }
    
    if (newPassword !== confirmPassword) {
      wx.showToast({
        title: '两次密码不一致',
        icon: 'none'
      })
      return
    }
    
    this.setData({ loading: true })
    
    const app = getApp()
    wx.request({
      url: `${app.globalData.baseUrl}/user/password/reset`,
      method: 'POST',
      data: {
        phone: phone.trim(),
        verifyCode: verifyCode.trim(),
        newPassword: newPassword
      },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({
            title: '密码重置成功',
            icon: 'success',
            duration: 2000
          })
          
          setTimeout(() => {
            wx.navigateBack()
          }, 2000)
        } else {
          wx.showToast({
            title: res.data.msg || '重置失败',
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

  onUnload() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  }
})
