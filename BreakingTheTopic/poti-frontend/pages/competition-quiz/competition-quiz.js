const guestUtils = require('../../utils/guest.js')

const TYPE_TEXT = { 1: '单选题', 2: '多选题', 3: '填空题', 4: '解答题' }

Page({
  data: {
    mode: 'loading', // loading | quiz | result | invalid
    invalidMsg: '',
    compTitle: '',
    // 答题模式
    questions: [],
    currentQuestion: null,
    currentIndex: 0,
    answers: [],
    remainSeconds: 0,
    countdownText: '--:--',
    timeWarning: false,
    submitting: false,
    // 成绩模式
    result: null,
    rankList: [],
    reviewList: []
  },

  id: null,
  timer: null,
  _isDestroyed: false,

  onLoad(options) {
    this.id = options.id
    this._isDestroyed = false
    const token = wx.getStorageSync('token')
    if (!token || guestUtils.checkGuest()) {
      this.setData({ mode: 'invalid', invalidMsg: '请先登录后再参与竞赛' })
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
    this.loadDetail()
  },

  onUnload() {
    this._isDestroyed = true
    this.stopTimer()
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

  loadDetail() {
    this.request({ url: `/competition/${this.id}` }).then(res => {
      if (this._isDestroyed) return
      if (res && res.code === 200 && res.data) {
        const comp = res.data
        if (comp.title) {
          wx.setNavigationBarTitle({ title: comp.title })
        }
        const submitted = comp.myRecord && comp.myRecord.score !== null && comp.myRecord.score !== undefined
        if (submitted) {
          // 已交卷：直接进入成绩视图
          this.enterResult({
            score: comp.myRecord.score,
            correctCount: comp.myRecord.correctCount,
            costSeconds: comp.myRecord.costSeconds,
            rank: comp.myRecord.rank
          })
        } else if (comp.state === '已结束') {
          this.setData({ mode: 'invalid', invalidMsg: comp.myRecord ? '成绩加载失败，请稍后重试' : '本场竞赛已结束，你未参与答题' })
        } else if (comp.state === '进行中' && comp.mySigned) {
          this.startQuiz(comp)
        } else if (comp.state === '报名中') {
          this.setData({ mode: 'invalid', invalidMsg: '竞赛尚未开始，开赛后可进入答题' })
        } else {
          this.setData({ mode: 'invalid', invalidMsg: comp.mySigned ? '竞赛暂未开始' : '请先报名后再参与本场竞赛' })
        }
      } else {
        this.setData({ mode: 'invalid', invalidMsg: (res && res.msg) || '竞赛信息加载失败' })
      }
    }).catch(err => {
      console.error('加载竞赛详情失败', err)
      if (!this._isDestroyed) {
        this.setData({ mode: 'invalid', invalidMsg: '网络异常，请稍后重试' })
      }
    })
  },

  startQuiz(comp) {
    wx.showLoading({ title: '加载试卷...', mask: true })
    this.request({
      url: `/competition/${this.id}/start`,
      method: 'POST'
    }).then(res => {
      wx.hideLoading()
      if (this._isDestroyed) return
      if (res && res.code === 200 && res.data && Array.isArray(res.data.questions)) {
        const questions = (res.data.questions || []).map(q => ({
          id: q.id,
          title: q.title,
          content: q.content,
          displayTitle: q.title || q.content || '（无题干）',
          type: q.type,
          typeText: TYPE_TEXT[q.type] || '单选题',
          options: (q.options || []).map((text, i) => ({
            text,
            letter: String.fromCharCode(65 + i),
            selected: false
          }))
        }))
        if (questions.length === 0) {
          this.setData({ mode: 'invalid', invalidMsg: '试卷为空，请联系管理员' })
          return
        }
        const durationMinutes = comp.durationMinutes || 30
        const remainSeconds = durationMinutes * 60
        this.setData({
          mode: 'quiz',
          compTitle: comp.title,
          questions,
          currentQuestion: questions[0],
          currentIndex: 0,
          // 填空题/解答题作答为字符串，多选题为数组
          answers: questions.map(q => (q.type === 2 ? [] : '')),
          remainSeconds,
          countdownText: this.formatCountdown(remainSeconds),
          timeWarning: false
        })
        this.startTimer()
      } else {
        wx.showToast({ title: (res && res.msg) || '开赛失败', icon: 'none' })
        this.setData({ mode: 'invalid', invalidMsg: (res && res.msg) || '开赛失败，请稍后重试' })
      }
    }).catch(err => {
      wx.hideLoading()
      console.error('开赛失败', err)
      if (!this._isDestroyed) {
        this.setData({ mode: 'invalid', invalidMsg: '网络异常，请稍后重试' })
      }
    })
  },

  // ---------- 倒计时 ----------
  startTimer() {
    this.stopTimer()
    this.timer = setInterval(() => {
      if (this._isDestroyed) {
        this.stopTimer()
        return
      }
      const r = this.data.remainSeconds - 1
      if (r <= 0) {
        this.setData({ remainSeconds: 0, countdownText: '00:00', timeWarning: true })
        this.stopTimer()
        this.autoSubmit()
      } else {
        this.setData({
          remainSeconds: r,
          countdownText: this.formatCountdown(r),
          timeWarning: r <= 60
        })
      }
    }, 1000)
  },

  stopTimer() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },

  formatCountdown(seconds) {
    const s = Math.max(0, Number(seconds) || 0)
    const mm = Math.floor(s / 60)
    const ss = s % 60
    return `${mm < 10 ? '0' + mm : mm}:${ss < 10 ? '0' + ss : ss}`
  },

  // ---------- 作答交互 ----------
  goQuestion(index) {
    if (index < 0 || index >= this.data.questions.length) return
    this.setData({
      currentIndex: index,
      currentQuestion: this.data.questions[index]
    })
  },

  prevQuestion() {
    if (this.data.currentIndex > 0) {
      this.goQuestion(this.data.currentIndex - 1)
    }
  },

  nextQuestion() {
    if (this.data.currentIndex < this.data.questions.length - 1) {
      this.goQuestion(this.data.currentIndex + 1)
    }
  },

  updateCurrentOptions(selectedLetter) {
    // 同步当前题 options 的 selected 标记
    const q = this.data.currentQuestion
    const options = q.options.map(opt => ({
      ...opt,
      selected: Array.isArray(selectedLetter)
        ? selectedLetter.indexOf(opt.letter) > -1
        : opt.letter === selectedLetter
    }))
    this.setData({
      currentQuestion: { ...q, options },
      [`questions[${this.data.currentIndex}].options`]: options
    })
  },

  selectSingle(e) {
    const idx = e.currentTarget.dataset.idx
    const opt = this.data.currentQuestion.options[idx]
    if (!opt) return
    this.setData({ [`answers[${this.data.currentIndex}]`]: opt.letter })
    this.updateCurrentOptions(opt.letter)
  },

  toggleMulti(e) {
    const idx = e.currentTarget.dataset.idx
    const opt = this.data.currentQuestion.options[idx]
    if (!opt) return
    const cur = this.data.answers[this.data.currentIndex] || []
    const pos = cur.indexOf(opt.letter)
    const next = pos > -1
      ? cur.filter(l => l !== opt.letter)
      : cur.concat(opt.letter)
    next.sort()
    this.setData({ [`answers[${this.data.currentIndex}]`]: next })
    this.updateCurrentOptions(next)
  },

  // 填空题/解答题：文本输入（两题型共用，作答存字符串）
  onTextInput(e) {
    this.setData({ [`answers[${this.data.currentIndex}]`]: e.detail.value })
  },

  // ---------- 交卷 ----------
  countUnanswered() {
    const { questions, answers } = this.data
    let count = 0
    for (let i = 0; i < questions.length; i++) {
      const a = answers[i]
      if (Array.isArray(a) ? a.length === 0 : !a) count++
    }
    return count
  },

  handleSubmit() {
    const unanswered = this.countUnanswered()
    if (unanswered > 0) {
      wx.showModal({
        title: '提示',
        content: `还有 ${unanswered} 题未作答，确定交卷吗？`,
        confirmText: '交卷',
        cancelText: '继续答题',
        success: (res) => {
          if (res.confirm) this.doSubmit()
        }
      })
    } else {
      this.doSubmit()
    }
  },

  autoSubmit() {
    if (this.data.submitting || this.data.mode !== 'quiz') return
    wx.showToast({ title: '时间到，自动交卷', icon: 'none' })
    this.doSubmit()
  },

  normalizeAnswer(a) {
    if (Array.isArray(a)) return a.slice().sort().join('')
    return a || ''
  },

  doSubmit() {
    if (this.data.submitting) return
    this.stopTimer()
    const { questions, answers } = this.data
    const payload = questions.map((q, i) => ({
      questionId: q.id,
      answer: this.normalizeAnswer(answers[i])
    }))
    this.setData({ submitting: true })
    wx.showLoading({ title: '交卷中...', mask: true })
    this.request({
      url: `/competition/${this.id}/submit`,
      method: 'POST',
      data: { answers: payload }
    }).then(res => {
      wx.hideLoading()
      if (this._isDestroyed) return
      if (res && res.code === 200 && res.data) {
        this.enterResult({
          score: res.data.score,
          correctCount: res.data.correctCount,
          costSeconds: res.data.costSeconds,
          rank: null
        })
      } else {
        wx.showToast({ title: (res && res.msg) || '交卷失败', icon: 'none' })
        this.setData({ submitting: false })
      }
    }).catch(err => {
      wx.hideLoading()
      console.error('交卷失败', err)
      if (!this._isDestroyed) {
        wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        this.setData({ submitting: false })
      }
    })
  },

  // ---------- 成绩与排行榜 ----------
  enterResult(record) {
    this.stopTimer()
    this.setData({
      mode: 'result',
      submitting: false,
      result: {
        score: record.score === null || record.score === undefined ? 0 : record.score,
        correctCount: record.correctCount === null || record.correctCount === undefined ? 0 : record.correctCount,
        costText: this.formatDuration(record.costSeconds),
        rank: record.rank || null
      }
    })
    this.loadRank()
    this.loadReview()
  },

  loadRank() {
    this.request({ url: `/competition/${this.id}/rank` }).then(res => {
      if (this._isDestroyed) return
      if (res && res.code === 200 && res.data && Array.isArray(res.data.list)) {
        const result = this.data.result || {}
        const rankList = (res.data.list || []).slice(0, 50).map((item, idx) => {
          const rank = idx + 1
          const isMe = result.rank
            ? rank === result.rank
            : (item.score === result.score && item.costSeconds === result.costSeconds)
          return {
            ...item,
            rank,
            isMe,
            costText: this.formatDuration(item.costSeconds)
          }
        })
        // 未从排行榜确认到名次时，用匹配行回填
        if (!result.rank && this.data.result) {
          const me = rankList.find(r => r.isMe)
          if (me) {
            this.setData({ result: { ...this.data.result, rank: me.rank } })
          }
        }
        this.setData({ rankList })
      }
    }).catch(err => {
      console.error('加载排行榜失败', err)
    })
  },

  // 试卷回顾：每题的我的作答、参考答案与解析（交卷后可看）
  loadReview() {
    this.setData({ reviewList: [] })
    this.request({ url: `/competition/${this.id}/review` }).then(res => {
      if (this._isDestroyed) return
      if (res && res.code === 200 && res.data && Array.isArray(res.data.list)) {
        const reviewList = res.data.list.map((q, idx) => {
          const isChoice = q.type === 1 || q.type === 2
          const userLetters = isChoice ? String(q.userAnswer || '') : ''
          const correctLetters = isChoice ? String(q.correctAnswer || '') : ''
          const options = (q.options || []).map((text, i) => {
            const letter = String.fromCharCode(65 + i)
            return {
              letter,
              text,
              isCorrect: correctLetters.indexOf(letter) > -1,
              isUser: userLetters.indexOf(letter) > -1
            }
          })
          // 填空题参考答案支持 | 多候选，展示时改为 A / B
          let correctAnswer = q.correctAnswer || ''
          if (q.type === 3 && correctAnswer) {
            correctAnswer = correctAnswer.split('|').map(s => s.trim()).filter(Boolean).join(' / ')
          }
          return {
            no: idx + 1,
            questionId: q.questionId,
            type: q.type,
            typeText: TYPE_TEXT[q.type] || '单选题',
            displayTitle: q.title || q.content || '（无题干）',
            options,
            userAnswer: q.userAnswer || '',
            correctAnswer,
            analysis: q.analysis || '',
            correct: q.correct,
            score: q.score === null || q.score === undefined ? 0 : q.score
          }
        })
        this.setData({ reviewList })
      }
    }).catch(err => {
      console.error('加载试卷回顾失败', err)
    })
  },

  goBack() {
    wx.navigateBack({
      fail: () => {
        wx.redirectTo({ url: '/pages/competition/competition' })
      }
    })
  },

  formatDuration(seconds) {
    const s = Number(seconds) || 0
    const mm = Math.floor(s / 60)
    const ss = s % 60
    if (mm <= 0) return `${ss}秒`
    return `${mm}分${ss < 10 ? '0' + ss : ss}秒`
  }
})
