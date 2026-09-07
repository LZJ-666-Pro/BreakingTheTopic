const guestUtils = {
  checkGuest() {
    const guestMode = wx.getStorageSync('guestMode')
    return guestMode === true
  },

  requireLogin(callback, message = '该功能需要登录后才能使用') {
    if (this.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: message,
        confirmText: '去登录',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/login/login'
            })
          }
        }
      })
      return false
    }
    
    if (callback && typeof callback === 'function') {
      callback()
    }
    return true
  },

  checkLoginAndExecute(callback, message) {
    return this.requireLogin(callback, message)
  }
}

module.exports = guestUtils
