const app = getApp()
const recorderManager = wx.getRecorderManager()
const MAX_TIME = 120

Page({
  data: {
    interviewId: null,
    type: '',
    currentOrder: 1,
    totalQuestions: 0,
    currentQuestion: null,
    userAnswer: '',
    answerMode: 'text',
    isRecording: false,
    recordDuration: 0,
    audioUrl: '',
    canSubmit: false,
    isLastQuestion: false,
    progress: 0,
    timerText: '02:00',
    timeLeft: MAX_TIME,
    isWarning: false,
    startTime: 0,
    timer: null,
    interviewStartTime: 0,
    isFinishing: false,
    isSubmitting: false,
    isStarting: false,
    isFinished: false
  },

  onLoad(options) {
    this.setData({
      type: options.type,
      interviewId: options.interviewId ? parseInt(options.interviewId) : null,
      interviewStartTime: Date.now()
    })
    
    if (this.data.interviewId) {
      this.loadQuestion(1)
    } else {
      this.startInterview()
    }

    recorderManager.onStop((res) => {
      this.uploadAudio(res.tempFilePath)
    })
  },

  onUnload() {
    this.stopTimer()
    if (this.data.interviewId && !this.data.isFinished) {
      this.autoFinishInterview()
    }
  },

  autoFinishInterview() {
    const spendSeconds = Math.floor((Date.now() - this.data.interviewStartTime) / 1000)
    wx.request({
      url: `${app.globalData.interviewUrl}/interview/finish`,
      method: 'POST',
      data: { 
        interviewId: this.data.interviewId,
        spendSeconds: spendSeconds
      },
      header: {
        'X-User-Id': app.globalData.userId || 1,
        'content-type': 'application/json'
      }
    })
  },

  startInterview() {
    if (this.data.isStarting) return
    this.setData({ isStarting: true })
    
    wx.showLoading({ title: '加载中...' })
    
    wx.request({
      url: `${app.globalData.interviewUrl}/interview/start`,
      method: 'GET',
      data: { type: this.data.type },
      header: { 'X-User-Id': app.globalData.userId || 1 },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          const data = res.data.data
          this.setData({
            interviewId: data.interviewId,
            totalQuestions: data.totalQuestions,
            currentQuestion: data.questions[0],
            isStarting: false
          })
          this.startTimer()
        } else {
          this.setData({ isStarting: false })
          wx.showToast({ title: res.data.msg || '加载失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        this.setData({ isStarting: false })
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  loadQuestion(orderNum) {
    wx.request({
      url: `${app.globalData.interviewUrl}/interview/question`,
      method: 'GET',
      data: {
        interviewId: this.data.interviewId,
        orderNum: orderNum
      },
      header: { 'X-User-Id': app.globalData.userId || 1 },
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          this.setData({
            currentQuestion: data,
            currentOrder: orderNum,
            isLastQuestion: orderNum === this.data.totalQuestions,
            progress: (orderNum / this.data.totalQuestions) * 100,
            userAnswer: '',
            audioUrl: '',
            recordDuration: 0,
            canSubmit: false,
            timeLeft: MAX_TIME,
            isSubmitting: false
          })
          this.startTimer()
        }
      }
    })
  },

  startTimer() {
    this.stopTimer()
    this.setData({ startTime: Date.now() })
    
    this.data.timer = setInterval(() => {
      let timeLeft = this.data.timeLeft - 1
      if (timeLeft <= 0) {
        this.stopTimer()
        this.submitAnswer()
        return
      }
      
      const minutes = Math.floor(timeLeft / 60)
      const seconds = timeLeft % 60
      this.setData({
        timeLeft,
        timerText: `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`,
        isWarning: timeLeft <= 30
      })
    }, 1000)
  },

  stopTimer() {
    if (this.data.timer) {
      clearInterval(this.data.timer)
      this.setData({ timer: null })
    }
  },

  switchMode(e) {
    this.setData({ answerMode: e.currentTarget.dataset.mode })
  },

  onAnswerInput(e) {
    this.setData({
      userAnswer: e.detail.value,
      canSubmit: e.detail.value.trim().length > 0
    })
  },

  startRecord() {
    this.setData({ isRecording: true, recordDuration: 0 })
    
    recorderManager.start({
      format: 'mp3',
      duration: 60000
    })

    this.recordTimer = setInterval(() => {
      let duration = this.data.recordDuration + 1
      if (duration >= 60) {
        this.stopRecord()
        return
      }
      this.setData({ recordDuration: duration })
    }, 1000)
  },

  stopRecord() {
    this.setData({ isRecording: false })
    if (this.recordTimer) {
      clearInterval(this.recordTimer)
    }
    recorderManager.stop()
  },

  uploadAudio(tempFilePath) {
    wx.uploadFile({
      url: `${app.globalData.baseUrl}/upload/audio`,
      filePath: tempFilePath,
      name: 'file',
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          this.setData({
            audioUrl: data.data.url,
            canSubmit: true
          })
        }
      }
    })
  },

  submitAnswer() {
    if (this.data.isSubmitting) return
    if (!this.data.canSubmit && this.data.answerMode === 'text') {
      wx.showToast({ title: '请输入答案', icon: 'none' })
      return
    }

    this.stopTimer()
    this.setData({ isSubmitting: true })
    const answerTime = Math.floor((Date.now() - this.data.startTime) / 1000)

    wx.showLoading({ title: '提交中...' })

    wx.request({
      url: `${app.globalData.interviewUrl}/interview/answer`,
      method: 'POST',
      data: {
        interviewId: this.data.interviewId,
        questionId: this.data.currentQuestion.questionId,
        userAnswer: this.data.userAnswer,
        audioUrl: this.data.audioUrl,
        answerTimeSeconds: answerTime
      },
      header: {
        'X-User-Id': app.globalData.userId || 1,
        'content-type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          if (this.data.isLastQuestion) {
            this.finishInterview()
          } else {
            this.loadQuestion(this.data.currentOrder + 1)
          }
        } else {
          this.setData({ isSubmitting: false })
          wx.showToast({ title: res.data.msg || '提交失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        this.setData({ isSubmitting: false })
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  },

  skipQuestion() {
    this.stopTimer()
    if (this.data.isLastQuestion) {
      this.finishInterview()
    } else {
      this.loadQuestion(this.data.currentOrder + 1)
    }
  },

  finishInterview() {
    if (this.data.isFinishing) return
    this.setData({ isFinishing: true })
    
    wx.showLoading({ title: '计算成绩...' })
    
    const spendSeconds = Math.floor((Date.now() - this.data.interviewStartTime) / 1000)

    wx.request({
      url: `${app.globalData.interviewUrl}/interview/finish`,
      method: 'POST',
      data: { 
        interviewId: this.data.interviewId,
        spendSeconds: spendSeconds
      },
      header: {
        'X-User-Id': app.globalData.userId || 1,
        'content-type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          this.setData({ isFinished: true })
          const result = res.data.data
          wx.redirectTo({
            url: `/pages/interview-result/interview-result?interviewId=${this.data.interviewId}`
          })
        } else {
          this.setData({ isFinishing: false })
          wx.showToast({ title: res.data.msg || '提交失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        this.setData({ isFinishing: false })
        wx.showToast({ title: '网络错误', icon: 'none' })
      }
    })
  }
})
