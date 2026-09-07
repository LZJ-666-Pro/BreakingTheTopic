Page({
  data: {
    form: {
      nickname: '',
      phone: '',
      email: '',
      password: '',
      confirmPassword: '',
      uniqueId: '',
      gender: 1,
      avatarUrl: '',
      verifyCode: ''
    },
    loading: false,
    errors: {},
    countdown: 0,
    canSendCode: false
  },

  timer: null,

  chooseAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0]
        this.uploadAvatar(tempFilePath)
      }
    })
  },

  uploadAvatar(filePath) {
    wx.showLoading({
      title: '上传中...',
    })

    const app = getApp()
    
    wx.uploadFile({
      url: `${app.globalData.baseUrl}/user/avatar/upload`,
      filePath: filePath,
      name: 'file',
      success: (res) => {
        wx.hideLoading()
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          this.setData({
            'form.avatarUrl': data.data
          })
          wx.showToast({
            title: '上传成功',
            icon: 'success'
          })
        } else {
          wx.showToast({
            title: data.msg || '上传失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({
          title: '上传失败',
          icon: 'none'
        })
      }
    })
  },

  onInputChange(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    this.setData({
      [`form.${field}`]: value
    })
    this.validateField(field, value)
    
    if (field === 'phone') {
      this.updateSendButtonState()
    }
  },

  onGenderChange(e) {
    this.setData({
      'form.gender': parseInt(e.detail.value)
    })
  },

  updateSendButtonState() {
    const { form, countdown } = this.data
    const canSend = form.phone.length === 11 && /^1[3-9]\d{9}$/.test(form.phone) && countdown === 0
    this.setData({ canSendCode: canSend })
  },

  sendVerifyCode() {
    const { form, countdown, canSendCode } = this.data
    
    if (!canSendCode || countdown > 0) {
      return
    }
    
    if (!form.phone || !form.phone.trim()) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      })
      return
    }
    
    if (!/^1[3-9]\d{9}$/.test(form.phone)) {
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
        phone: form.phone.trim()
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

  validateField(field, value) {
    const errors = this.data.errors
    
    switch (field) {
      case 'nickname':
        if (!value || value.trim().length === 0) {
          errors.nickname = '请输入昵称'
        } else if (value.length > 20) {
          errors.nickname = '昵称不能超过20个字符'
        } else {
          delete errors.nickname
        }
        break
        
      case 'phone':
        if (!value || value.trim().length === 0) {
          errors.phone = '请输入手机号'
        } else if (!/^1[3-9]\d{9}$/.test(value)) {
          errors.phone = '手机号格式不正确'
        } else {
          delete errors.phone
        }
        break
        
      case 'verifyCode':
        if (!value || value.trim().length === 0) {
          errors.verifyCode = '请输入验证码'
        } else if (value.length !== 6) {
          errors.verifyCode = '验证码为6位数字'
        } else {
          delete errors.verifyCode
        }
        break
        
      case 'password':
        if (!value || value.length < 6) {
          errors.password = '密码长度至少6位'
        } else {
          delete errors.password
        }
        if (this.data.form.confirmPassword && value !== this.data.form.confirmPassword) {
          errors.confirmPassword = '两次密码不一致'
        } else if (this.data.form.confirmPassword) {
          delete errors.confirmPassword
        }
        break
        
      case 'confirmPassword':
        if (!value || value.length < 6) {
          errors.confirmPassword = '请确认密码'
        } else if (value !== this.data.form.password) {
          errors.confirmPassword = '两次密码不一致'
        } else {
          delete errors.confirmPassword
        }
        break
        
      case 'uniqueId':
        if (value && value.trim().length > 0) {
          if (value.length < 4 || value.length > 20) {
            errors.uniqueId = '唯一ID长度必须在4-20个字符之间'
          } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
            errors.uniqueId = '唯一ID只能包含字母、数字和下划线'
          } else {
            delete errors.uniqueId
          }
        } else {
          delete errors.uniqueId
        }
        break
    }
    
    this.setData({ errors })
  },

  validateForm() {
    const { form } = this.data
    const errors = {}
    
    if (!form.nickname || form.nickname.trim().length === 0) {
      errors.nickname = '请输入昵称'
    }
    
    if (!form.phone || form.phone.trim().length === 0) {
      errors.phone = '请输入手机号'
    } else if (!/^1[3-9]\d{9}$/.test(form.phone)) {
      errors.phone = '手机号格式不正确'
    }
    
    if (!form.verifyCode || form.verifyCode.trim().length === 0) {
      errors.verifyCode = '请输入验证码'
    }
    
    if (!form.password || form.password.length < 6) {
      errors.password = '密码长度至少6位'
    }
    
    if (!form.confirmPassword) {
      errors.confirmPassword = '请确认密码'
    } else if (form.password !== form.confirmPassword) {
      errors.confirmPassword = '两次密码不一致'
    }
    
    if (form.uniqueId && form.uniqueId.trim().length > 0) {
      if (form.uniqueId.length < 4 || form.uniqueId.length > 20) {
        errors.uniqueId = '唯一ID长度必须在4-20个字符之间'
      } else if (!/^[a-zA-Z0-9_]+$/.test(form.uniqueId)) {
        errors.uniqueId = '唯一ID只能包含字母、数字和下划线'
      }
    }
    
    this.setData({ errors })
    return Object.keys(errors).length === 0
  },

  handleRegister() {
    if (!this.validateForm()) {
      wx.showToast({
        title: '请完善信息',
        icon: 'none'
      })
      return
    }
    
    if (this.data.loading) return
    
    this.setData({ loading: true })
    
    const app = getApp()
    const { form } = this.data
    
    wx.request({
      url: `${app.globalData.baseUrl}/user/register`,
      method: 'POST',
      data: {
        nickname: form.nickname.trim(),
        phone: form.phone.trim(),
        email: form.email ? form.email.trim() : null,
        password: form.password,
        uniqueId: form.uniqueId ? form.uniqueId.trim() : null,
        gender: form.gender,
        avatarUrl: form.avatarUrl,
        verifyCode: form.verifyCode.trim()
      },
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
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
            title: '注册成功',
            icon: 'success'
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
        } else {
          wx.showToast({
            title: res.data.msg || '注册失败',
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

  goToLogin() {
    wx.navigateBack()
  },

  onUnload() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  }
})
