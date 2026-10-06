const userApi = require('../../api/user.js')
const wrongbookApi = require('../../api/wrongbook.js')
const guestUtils = require('../../utils/guest.js')

const WEEK_LABELS = ['日', '一', '二', '三', '四', '五', '六']

// 兼容 "yyyy-MM-dd(...)" 字符串与毫秒时间戳两种日期格式
function parseDayOfMonth(value) {
  if (value === null || value === undefined || value === '') return 0
  if (typeof value === 'number' || /^\d+$/.test(String(value))) {
    return new Date(Number(value)).getDate() || 0
  }
  const m = String(value).match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (m) return parseInt(m[3], 10)
  return 0
}

Page({
  data: {
    // 总览统计
    statistics: {
      totalQuestionCount: 0,
      correctCount: 0,
      wrongCount: 0,
      todayCount: 0,
      favoriteCount: 0,
      wrongbookCount: 0,
      lastPracticeTime: null
    },
    accuracy: 0,
    streakDays: 0,

    // 答题分布环形图
    correctPct: 0,
    wrongPct: 0,
    ringStyle: 'background: conic-gradient(#EEF1F6 0% 100%)',

    // 日历
    calendarYear: 0,
    calendarMonth: 0,
    monthLabel: '',
    weekLabels: WEEK_LABELS,
    calendarDays: [],
    monthPracticeDays: 0,
    monthPracticeTotal: 0,
    canGoNextMonth: false,

    // 错题本统计
    wrongStats: {
      totalWrong: 0,
      mastered: 0,
      notMastered: 0,
      byCategory: []
    },
    wrongCategories: [],
    masteredRate: 0,

    loading: true
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 })
    }

    const now = new Date()
    this.setData({
      calendarYear: now.getFullYear(),
      calendarMonth: now.getMonth() + 1
    })
    this.updateMonthLabel()

    if (guestUtils.checkGuest()) {
      this.setData({
        loading: false,
        statistics: {
          totalQuestionCount: 0,
          correctCount: 0,
          wrongCount: 0,
          todayCount: 0,
          favoriteCount: 0,
          wrongbookCount: 0,
          lastPracticeTime: null
        },
        accuracy: 0,
        streakDays: 0,
        correctPct: 0,
        wrongPct: 0,
        ringStyle: 'background: conic-gradient(#EEF1F6 0% 100%)',
        wrongStats: { totalWrong: 0, mastered: 0, notMastered: 0, byCategory: [] },
        wrongCategories: [],
        masteredRate: 0
      })
      this.buildCalendar()
      return
    }

    this.loadData()
  },

  loadData() {
    this.setData({ loading: true })
    this.loadStatistics()
    this.loadCalendar()
    this.loadCheckinStreak()
    this.loadWrongStats()
  },

  loadStatistics() {
    userApi.getStatistics().then(res => {
      const stats = res && res.data ? res.data : {}
      const total = Number(stats.totalQuestionCount) || 0
      const correct = Number(stats.correctCount) || 0
      const accuracy = total > 0 ? Math.round(correct * 100 / total) : 0
      const correctPct = accuracy
      const wrongPct = total > 0 ? 100 - accuracy : 0
      // 环形图：答对绿色 + 答错红色；无数据时灰色
      const ringStyle = total > 0
        ? 'background: conic-gradient(#07C160 0% ' + correctPct + '%, #FA5151 ' + correctPct + '% 100%)'
        : 'background: conic-gradient(#EEF1F6 0% 100%)'
      this.setData({
        statistics: {
          totalQuestionCount: total,
          correctCount: correct,
          wrongCount: Number(stats.wrongCount) || 0,
          todayCount: Number(stats.todayCount) || 0,
          favoriteCount: Number(stats.favoriteCount) || 0,
          wrongbookCount: Number(stats.wrongbookCount) || 0,
          lastPracticeTime: stats.lastPracticeTime || null
        },
        accuracy,
        correctPct,
        wrongPct,
        ringStyle,
        loading: false
      })
    }).catch(err => {
      console.error('加载统计数据失败', err)
      this.setData({ loading: false })
    })
  },

  loadWrongStats() {
    wrongbookApi.getStatistics().then(res => {
      const data = (res && res.data) || {}
      const byCategory = Array.isArray(data.byCategory) ? data.byCategory : []
      const totalWrong = Number(data.totalWrong) || 0
      const mastered = Number(data.mastered) || 0
      const notMastered = Number(data.notMastered) || 0
      const masteredRate = totalWrong > 0 ? Math.round(mastered * 100 / totalWrong) : 0
      let maxCategoryCount = 0
      byCategory.forEach(item => {
        if (Number(item.count) > maxCategoryCount) maxCategoryCount = Number(item.count)
      })
      // 分类分布：计算占总错题的百分比与条形图宽度
      const wrongCategories = byCategory.map(item => {
        const count = Number(item.count) || 0
        return {
          categoryName: item.categoryName,
          count,
          pct: totalWrong > 0 ? Math.round(count * 100 / totalWrong) : 0,
          widthPct: maxCategoryCount > 0 ? Math.round(count * 100 / maxCategoryCount) : 0
        }
      })
      this.setData({
        wrongStats: { totalWrong, mastered, notMastered, byCategory },
        wrongCategories,
        masteredRate
      })
    }).catch(err => {
      console.error('加载错题统计失败', err)
    })
  },

  // 连续天数与首页签到共用同一数据源（check_in 表），签到后切到本页即实时同步
  loadCheckinStreak() {
    const app = getApp()
    const token = app.globalData.token || wx.getStorageSync('token')
    const userId = wx.getStorageSync('userId')
    if (!token || !userId) {
      this.setData({ streakDays: 0 })
      return
    }
    wx.request({
      url: `${app.globalData.baseUrl}/checkin/status?userId=${userId}`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.statusCode === 401 || (res.data && res.data.code === 401)) {
          app._handleUnauthorized()
          return
        }
        if (res.data && res.data.code === 200 && res.data.data) {
          this.setData({ streakDays: Number(res.data.data.streakDays) || 0 })
        }
      }
    })
  },

  loadCalendar() {
    const { calendarYear: year, calendarMonth: month } = this.data
    userApi.getCalendar(year, month).then(res => {
      const list = (res && res.data && res.data.list) || []
      const countByDay = {}
      list.forEach(item => {
        const day = parseDayOfMonth(item.date)
        if (day > 0) {
          countByDay[day] = (countByDay[day] || 0) + (Number(item.count) || 0)
        }
      })
      this._countByDay = countByDay
      this.mergeCalendarDays(year, month, list)
      this.buildCalendar()
      this.computeStreak()
    }).catch(err => {
      console.error('加载刷题日历失败', err)
      this._countByDay = {}
      this.buildCalendar()
    })
  },

  buildCalendar() {
    const year = this.data.calendarYear
    const month = this.data.calendarMonth
    const countByDay = this._countByDay || {}

    const firstWeekday = new Date(year, month - 1, 1).getDay()
    const daysInMonth = new Date(year, month, 0).getDate()

    const today = new Date()
    const isCurrentMonth = today.getFullYear() === year && (today.getMonth() + 1) === month
    const todayDate = isCurrentMonth ? today.getDate() : 0

    const cells = []
    for (let i = 0; i < firstWeekday; i++) {
      cells.push({ empty: true })
    }
    let practiceDays = 0
    let monthTotal = 0
    for (let day = 1; day <= daysInMonth; day++) {
      const count = countByDay[day] || 0
      if (count > 0) {
        practiceDays++
        monthTotal += count
      }
      cells.push({
        empty: false,
        day,
        count,
        today: day === todayDate,
        level: count <= 0 ? 0 : count <= 2 ? 1 : count <= 5 ? 2 : count <= 10 ? 3 : 4
      })
    }

    const now = new Date()
    const nextY = month === 12 ? year + 1 : year
    const nextM = month === 12 ? 1 : month + 1
    const canGoNextMonth = !(now.getFullYear() === nextY && (now.getMonth() + 1) === nextM)

    this.setData({
      calendarDays: cells,
      monthPracticeDays: practiceDays,
      monthPracticeTotal: monthTotal,
      canGoNextMonth
    })
  },

  updateMonthLabel() {
    this.setData({
      monthLabel: `${this.data.calendarYear}年${this.data.calendarMonth}月`
    })
  },

  prevMonth() {
    let { calendarYear: year, calendarMonth: month } = this.data
    month--
    if (month < 1) { month = 12; year-- }
    this.setData({ calendarYear: year, calendarMonth: month })
    this.updateMonthLabel()
    this.loadCalendar()
  },

  nextMonth() {
    if (!this.data.canGoNextMonth) return
    let { calendarYear: year, calendarMonth: month } = this.data
    month++
    if (month > 12) { month = 1; year++ }
    this.setData({ calendarYear: year, calendarMonth: month })
    this.updateMonthLabel()
    this.loadCalendar()
  },

  goPractice() {
    wx.switchTab({ url: '/pages/practice/practice' })
  },

  goWrongbook() {
    if (!guestUtils.requireLogin()) return
    wx.navigateTo({ url: '/pages/wrongbook/wrongbook' })
  },

  goFavorite() {
    if (!guestUtils.requireLogin()) return
    wx.navigateTo({ url: '/pages/favorite/favorite' })
  },

  goHistory() {
    if (!guestUtils.requireLogin()) return
    wx.navigateTo({ url: '/pages/history/history' })
  }
})
