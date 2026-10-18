const guestUtils = require('../../utils/guest.js')

const TABS = [
  { key: 'signup', name: '报名中', state: '报名中' },
  { key: 'ongoing', name: '进行中', state: '进行中' },
  { key: 'ended', name: '已结束', state: '已结束' }
]

const PAGE_SIZE = 10

Page({
  data: {
    tabs: TABS,
    activeTab: 'signup',
    list: [],
    page: 1,
    total: 0,
    loading: false,
    finished: false
  },

  _isDestroyed: false,

  onLoad() {
    this._isDestroyed = false
    const token = wx.getStorageSync('token')
    if (!token || guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '竞赛功能需要登录后才能使用',
        confirmText: '去登录',
        cancelText: '返回',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/login/login' })
          } else {
            wx.navigateBack({
              fail: () => {
                wx.switchTab({ url: '/pages/index/index' })
              }
            })
          }
        }
      })
      return
    }
    this.loadList(true)
  },

  onUnload() {
    this._isDestroyed = true
    this.clearStartRefresh()
  },

  onShow() {
    // 从答题页返回后刷新列表（报名/交卷状态可能变化）
    if (!this._isDestroyed && this.data.list.length > 0) {
      this.loadList(true, null, true)
    }
    this.scheduleStartRefresh()
  },

  onHide() {
    this.clearStartRefresh()
  },

  onPullDownRefresh() {
    this.loadList(true, () => {
      wx.stopPullDownRefresh()
    }, true)
  },

  onReachBottom() {
    if (!this.data.finished && !this.data.loading) {
      this.loadList(false)
    }
  },

  switchTab(e) {
    const key = e.currentTarget.dataset.key
    if (key === this.data.activeTab) return
    this.setData({ activeTab: key })
    // 强制刷新：避免上一 tab 请求未完成时新 tab 请求被 loading 拦截
    this.loadList(true, null, true)
  },

  request(options) {
    const app = getApp()
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${app.globalData.practiceUrl}${options.url}`,
        method: options.method || 'GET',
        data: options.data || {},
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${app.globalData.token}`,
          'X-User-Id': app.globalData.userId || 1
        },
        success: (res) => resolve(res.data),
        fail: (err) => reject(err)
      })
    })
  },

  loadList(reset, done, force) {
    if (this.data.loading && !force) {
      if (done) done()
      return
    }
    // 请求序号：仅接受最新一次请求的响应，防止快速切换 tab 时旧响应覆盖新列表
    const seq = (this._reqSeq = (this._reqSeq || 0) + 1)
    const tabState = TABS.find(t => t.key === this.data.activeTab)
    const page = reset ? 1 : this.data.page + 1
    this.setData({ loading: true })
    this.request({
      url: '/competition/page',
      data: {
        pageNum: page,
        pageSize: PAGE_SIZE,
        tab: tabState ? tabState.key : ''
      }
    }).then(res => {
      if (this._isDestroyed || seq !== this._reqSeq) return
      if (res && res.code === 200 && res.data) {
        const rawList = res.data.list || []
        const mapped = rawList.map((item, idx) => ({ ...this.decorateItem(item), key: `${item.id}-${idx}` }))
        const list = reset ? mapped : this.data.list.concat(mapped)
        const total = res.data.total || 0
        this.setData({
          list,
          total,
          page,
          finished: list.length >= total || rawList.length < PAGE_SIZE
        })
      } else {
        if (reset) this.setData({ list: [], total: 0, finished: true })
        wx.showToast({ title: (res && res.msg) || '加载失败', icon: 'none' })
      }
    }).catch(err => {
      if (this._isDestroyed || seq !== this._reqSeq) return
      console.error('加载竞赛列表失败', err)
      if (reset) this.setData({ list: [], total: 0, finished: true })
      wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
    }).finally(() => {
      if (!this._isDestroyed && seq === this._reqSeq) {
        this.setData({ loading: false })
        // 列表更新后重算蹲点刷新定时器
        this.scheduleStartRefresh()
      }
      if (done) done()
    })
  },

  // 解析后端时间字符串为毫秒（兼容空格与 ISO 两种格式，iOS 需 T 分隔）
  parseTime(str) {
    const t = new Date(String(str || '').replace(' ', 'T')).getTime()
    return isNaN(t) ? 0 : t
  },

  // 蹲点刷新：有已报名、未开赛的竞赛时，到开赛时刻自动刷新列表（按钮变「进入比赛」）
  scheduleStartRefresh() {
    this.clearStartRefresh()
    if (this._isDestroyed) return
    const now = Date.now()
    let nearest = 0
    this.data.list.forEach(item => {
      if (!item.mySigned) return
      const t = this.parseTime(item.startTime)
      if (t > now && (!nearest || t < nearest)) nearest = t
    })
    if (!nearest) return
    // 兜底上限 10 分钟，避免挂一个超长定时器
    const delay = Math.min(nearest - now + 1000, 10 * 60 * 1000)
    this._startTimer = setTimeout(() => {
      this._startTimer = null
      if (!this._isDestroyed) this.loadList(true)
    }, delay)
  },

  clearStartRefresh() {
    if (this._startTimer) {
      clearTimeout(this._startTimer)
      this._startTimer = null
    }
  },

  // 根据状态推导按钮文案与样式
  decorateItem(item) {
    const state = item.state || ''
    const stateClassMap = {
      '报名中': 'signup',
      '进行中': 'ongoing',
      '已结束': 'ended'
    }
    let btnText = ''
    let btnClass = ''
    let btnAction = ''
    if (state === '报名中') {
      if (item.mySigned) {
        btnText = '查看详情'
        btnClass = 'btn-ghost'
        btnAction = 'detail'
      } else {
        btnText = '立即报名'
        btnClass = 'btn-primary'
        btnAction = 'signup'
      }
    } else if (state === '进行中') {
      if (item.mySigned) {
        // 已交卷时答题页会自动进入成绩视图
        btnText = '进入比赛'
        btnClass = 'btn-primary'
        btnAction = 'quiz'
      } else {
        // 报名窗口持续到结束时间（PRD），进行中未报名仍可报名
        btnText = '立即报名'
        btnClass = 'btn-primary'
        btnAction = 'signup'
      }
    } else if (state === '已结束') {
      if (item.mySigned) {
        btnText = '查看成绩'
        btnClass = 'btn-ghost'
        btnAction = 'quiz'
      } else {
        // 与其他按钮同尺寸的禁用态，点击无动作
        btnText = '已结束'
        btnClass = 'btn-disabled'
        btnAction = ''
      }
    }
    return {
      ...item,
      stateClass: stateClassMap[state] || 'ended',
      countdownText: this.buildCountdown(item),
      btnText,
      btnClass,
      btnAction
    }
  },

  // 报名截止/开赛倒计时文案（对齐原型「报名截止还有 2天」）
  buildCountdown(item) {
    if (!item.endTime) return ''
    const end = new Date(String(item.endTime).replace(/-/g, '/')).getTime()
    if (isNaN(end)) return ''
    const diff = end - Date.now()
    if (diff <= 0) return ''
    const days = Math.floor(diff / 86400000)
    if (days >= 1) return `报名截止还有 ${days} 天`
    const hours = Math.floor(diff / 3600000)
    if (hours >= 1) return `报名截止还有 ${hours} 小时`
    return '报名即将截止'
  },

  // 空状态跳转历史竞赛（已结束 tab）
  goHistory() {
    this.switchTab({ currentTarget: { dataset: { key: 'ended' } } })
  },

  onBtnTap(e) {
    const { id, action } = e.currentTarget.dataset
    if (action === 'signup') {
      this.signup(id)
    } else if (action === 'quiz') {
      wx.navigateTo({ url: `/pages/competition-quiz/competition-quiz?id=${id}` })
    } else if (action === 'detail') {
      this.showDetail(id)
    }
  },

  // 查看详情弹窗（时间/题量/时长）；已开赛的已报名竞赛提供直达入口
  showDetail(id) {
    const item = this.data.list.find(x => x.id === id)
    if (!item) return
    const content = `比赛时间：${this.formatTime(item.startTime)} ~ ${this.formatTime(item.endTime)}\n题目数量：${item.questionCount} 题\n比赛时长：${item.durationMinutes} 分钟\n每题得分：${item.scorePerQuestion || 10} 分`
    const startMs = this.parseTime(item.startTime)
    const started = startMs > 0 && Date.now() >= startMs
    if (!started) {
      wx.showModal({
        title: item.title,
        content,
        showCancel: false,
        confirmText: '知道了'
      })
    } else {
      wx.showModal({
        title: item.title,
        content,
        confirmText: '进入比赛',
        cancelText: '知道了',
        success: (r) => {
          if (r.confirm) {
            wx.navigateTo({ url: `/pages/competition-quiz/competition-quiz?id=${item.id}` })
          }
        }
      })
    }
  },

  signup(id) {
    wx.showLoading({ title: '报名中...', mask: true })
    this.request({
      url: `/competition/${id}/signup`,
      method: 'POST'
    }).then(res => {
      wx.hideLoading()
      if (res && res.code === 200) {
        wx.showToast({ title: '报名成功', icon: 'success' })
        this.loadList(true)
      } else {
        wx.showToast({ title: (res && res.msg) || '报名失败', icon: 'none' })
      }
    }).catch(err => {
      wx.hideLoading()
      console.error('报名失败', err)
      wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
    })
  },

  // 我的成绩：跳转独立成绩页
  goMyRecords() {
    wx.navigateTo({ url: '/pages/competition-my/competition-my' })
  },

  // ---------- 工具 ----------
  formatTime(str) {
    if (!str) return ''
    // 兼容 yyyy-MM-dd HH:mm:ss / ISO
    const m = String(str).match(/(\d{4})-(\d{2})-(\d{2})[T ]?(\d{2})?:?(\d{2})?/)
    if (!m) return String(str)
    return `${m[2]}-${m[3]} ${m[4] || '00'}:${m[5] || '00'}`
  },

  formatDuration(seconds) {
    const s = Number(seconds) || 0
    const mm = Math.floor(s / 60)
    const ss = s % 60
    if (mm <= 0) return `${ss}秒`
    return `${mm}分${ss < 10 ? '0' + ss : ss}秒`
  }
})
