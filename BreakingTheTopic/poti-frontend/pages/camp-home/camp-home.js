const app = getApp()
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    campId: '',
    camp: null,
    loading: true,
    checking: false
  },

  onLoad(options) {
    this.setData({ campId: options.campId || '' })
  },

  onShow() {
    if (guestUtils.checkGuest()) {
      return
    }
    if (this.data.campId) {
      this.loadHome()
    }
  },

  loadHome() {
    wx.request({
      url: `${app.globalData.interviewUrl}/camp/home/${this.data.campId}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.applyHome(res.data.data || {})
        } else {
          wx.showToast({ title: res.data.msg || '加载失败', icon: 'none' })
          setTimeout(() => wx.navigateBack(), 800)
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 800)
      }
    })
  },

  applyHome(data) {
    const totalDays = data.totalDays || 0
    const progressDays = data.progressDays || 0
    const percent = totalDays > 0 ? Math.round(progressDays / totalDays * 100) : 0

    // 打卡圆点（WXML 不能调用 JS 方法，全部预格式化）
    const dots = []
    for (let day = 1; day <= totalDays; day++) {
      dots.push({
        day,
        done: progressDays >= day,
        current: !data.finished && day === progressDays + 1
      })
    }

    // 打卡记录
    const checkins = (data.checkins || []).map(c => ({
      ...c,
      recordText: `第${c.dayNum}天 · ${c.dateText}`
    }))

    this.setData({
      camp: {
        id: data.id,
        title: data.title,
        subtitle: data.subtitle,
        emoji: data.emoji,
        tint: data.tint,
        accent: data.accent,
        totalDays,
        progressDays,
        percent,
        finished: data.finished,
        todayChecked: data.todayChecked,
        currentDay: data.currentDay,
        todayTopic: data.todayTopic,
        tasks: data.tasks || [],
        dots,
        checkins
      },
      loading: false
    })
  },

  checkin() {
    const camp = this.data.camp
    if (this.data.checking || !camp || camp.todayChecked || camp.finished) {
      return
    }
    this.setData({ checking: true })
    wx.request({
      url: `${app.globalData.interviewUrl}/camp/checkin`,
      method: 'POST',
      data: { campId: this.data.campId },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data.code === 200) {
          const d = res.data.data || {}
          wx.showToast({ title: d.finished ? '🎉 恭喜结营！' : `第${d.dayNum}天打卡成功`, icon: 'none' })
          this.loadHome()
        } else {
          wx.showToast({ title: res.data.msg || '打卡失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误，请重试', icon: 'none' })
      },
      complete: () => {
        this.setData({ checking: false })
      }
    })
  },

  // 任务跳转：复用现有刷题 / 模拟面试 / 错题本模块
  goTask(e) {
    const type = e.currentTarget.dataset.type
    if (type === 'question') {
      wx.switchTab({ url: '/pages/question/question' })
    } else if (type === 'practice') {
      wx.switchTab({ url: '/pages/practice/practice' })
    } else if (type === 'interview') {
      wx.navigateTo({ url: '/pages/interview/interview' })
    } else if (type === 'wrongbook') {
      wx.navigateTo({ url: '/pages/wrongbook/wrongbook' })
    }
  },

  goOutline() {
    wx.navigateTo({ url: `/pages/camp-detail/camp-detail?campId=${this.data.campId}` })
  }
})
