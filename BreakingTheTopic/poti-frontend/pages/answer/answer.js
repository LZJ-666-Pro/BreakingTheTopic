const practiceApi = require('../../api/practice.js')
const favoriteApi = require('../../api/favorite.js')
const wrongbookApi = require('../../api/wrongbook.js')
const guestUtils = require('../../utils/guest.js')

const categoryMap = {
  java: { id: 1, name: 'Java' },
  python: { id: 2, name: 'Python' },
  mysql: { id: 3, name: 'MySQL' },
  redis: { id: 4, name: 'Redis' },
  spring: { id: 5, name: 'Spring' },
  mq: { id: 6, name: '消息队列' },
  os: { id: 7, name: '操作系统' },
  network: { id: 8, name: '计算机网络' },
  docker: { id: 9, name: 'Docker' },
  git: { id: 10, name: 'Git' },
  ai: { id: 11, name: '人工智能' },
  game: { id: 12, name: '游戏开发' },
  bigdata: { id: 13, name: '大数据' },
  ds: { id: 14, name: '数据结构' },
  cpp: { id: 15, name: 'C++' },
  basic: { id: 16, name: '计算机基础' },
  go: { id: 17, name: 'Go' },
  php: { id: 18, name: 'PHP' },
  mobile: { id: 19, name: '移动开发' },
  test: { id: 20, name: '测试' },
  lang: { id: 21, name: '编程语言' },
  hr: { id: 22, name: 'HR面试' }
}

Page({
  data: {
    type: '',
    categoryName: '',
    questions: [],
    currentIndex: 0,
    currentQuestion: null,
    selectedOption: null,
    isAnswered: false,
    correctAnswer: null,
    analysis: '',
    answeredCount: 0,
    correctCount: 0,
    wrongCount: 0,
    showAnswerSheet: false,
    answerList: [],
    isFavorite: false,
    loading: true,
    startTime: 0,
    elapsedTime: 0,
    timerText: '00:00',
    isSingleQuestion: false,
    fontSize: 'normal',
    showAnswerDefault: false,
    viewedQuestions: []
  },

  timer: null,

  onLoad(options) {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '答题功能需要登录后才能使用',
        confirmText: '去登录',
        cancelText: '返回',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/login/login'
            })
          } else {
            wx.navigateBack({
              fail: () => {
                wx.switchTab({
                  url: '/pages/index/index'
                })
              }
            })
          }
        }
      })
      return
    }
    
    this.loadSettings()
    
    if (options.questionId) {
      this.loadSingleQuestion(options.questionId)
    } else if (options.mode === 'random' && options.questionIds) {
      this.loadRandomQuestions(options.questionIds)
    } else {
      this.loadQuestionSet(options)
    }
  },

  loadSettings() {
    const settings = wx.getStorageSync('practiceSettings') || {}
    this.setData({
      fontSize: settings.fontSize || 'normal',
      showAnswerDefault: settings.showAnswer || false
    })
  },

  loadSingleQuestion(questionId) {
    const app = getApp()
    this.setData({ loading: true, isSingleQuestion: true })
    
    wx.request({
      url: `${app.globalData.questionUrl}/question/detail/${questionId}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200) {
          const questionData = res.data.data
          console.log('题目详情数据：', questionData)
          
          let options = []
          if (Array.isArray(questionData.options)) {
            options = questionData.options.map((text, index) => ({
              label: String.fromCharCode(65 + index),
              text: text
            }))
          } else if (typeof questionData.options === 'string') {
            options = this.parseOptions(questionData.options)
          }
          
          const question = {
            id: questionData.id,
            type: 'single',
            content: questionData.content,
            difficulty: questionData.difficulty === 1 ? '简单' : questionData.difficulty === 2 ? '中等' : '困难',
            difficultyLevel: questionData.difficulty === 1 ? 'easy' : questionData.difficulty === 2 ? 'medium' : 'hard',
            options: options,
            answer: this.getAnswerIndex(questionData.answer),
            analysis: questionData.analysis
          }
          
          console.log('处理后的题目：', question)
          
          this.setData({
            questions: [question],
            categoryName: questionData.categoryName || '题目详情',
            startTime: Date.now(),
            loading: false
          })
          
          this.initQuestion()
          this.startTimer()
        } else {
          wx.showToast({
            title: '题目不存在',
            icon: 'none'
          })
          setTimeout(() => {
            wx.navigateBack()
          }, 1500)
        }
      },
      fail: (err) => {
        console.error('加载题目失败', err)
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      }
    })
  },

  parseOptions(optionsStr) {
    try {
      const options = JSON.parse(optionsStr)
      return options.map((text, index) => ({
        label: String.fromCharCode(65 + index),
        text: text
      }))
    } catch (e) {
      return []
    }
  },

  loadRandomQuestions(questionIdsStr) {
    const app = getApp()
    const questionIds = questionIdsStr.split(',')
    
    this.setData({ 
      loading: true,
      categoryName: '随机刷题',
      startTime: Date.now(),
      isSingleQuestion: false
    })

    const promises = questionIds.map(id => {
      return new Promise((resolve, reject) => {
        wx.request({
          url: `${app.globalData.questionUrl}/question/detail/${id}`,
          method: 'GET',
          header: {
            'Authorization': `Bearer ${app.globalData.token}`
          },
          success: (res) => {
            if (res.data.code === 200) {
              const questionData = res.data.data
              
              let options = []
              if (Array.isArray(questionData.options)) {
                options = questionData.options.map((text, index) => ({
                  label: String.fromCharCode(65 + index),
                  text: text
                }))
              } else if (typeof questionData.options === 'string') {
                options = this.parseOptions(questionData.options)
              }
              
              const question = {
                id: questionData.id,
                type: 'single',
                content: questionData.content,
                difficulty: questionData.difficulty === 1 ? '简单' : questionData.difficulty === 2 ? '中等' : '困难',
                difficultyLevel: questionData.difficulty === 1 ? 'easy' : questionData.difficulty === 2 ? 'medium' : 'hard',
                options: options,
                answer: this.getAnswerIndex(questionData.answer),
                analysis: questionData.analysis,
                categoryId: questionData.categoryId
              }
              
              resolve(question)
            } else {
              resolve(null)
            }
          },
          fail: (err) => {
            console.error(`加载题目${id}失败`, err)
            resolve(null)
          }
        })
      })
    })

    Promise.all(promises).then(questions => {
      const validQuestions = questions.filter(q => q !== null)
      
      if (validQuestions.length === 0) {
        wx.showToast({
          title: '题目不存在',
          icon: 'none'
        })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
        return
      }

      this.setData({
        questions: validQuestions,
        loading: false
      })

      this.startTimer()
      this.initQuestion()
    })
  },

  getAnswerIndex(answer) {
    const answerMap = { 'A': 0, 'B': 1, 'C': 2, 'D': 3 }
    return answerMap[answer] || 0
  },

  loadQuestionSet(options) {
    const type = options.type || 'java'
    const categoryId = options.categoryId || categoryMap[type]?.id || 1
    
    console.log('loadQuestionSet - type:', type, 'categoryId:', categoryId)
    
    this.setData({ 
      type, 
      categoryName: '加载中...',
      loading: true,
      startTime: Date.now(),
      isSingleQuestion: false
    })

    const app = getApp()
    
    const categoryUrl = `${app.globalData.questionUrl}/question/categories`
    wx.request({
      url: categoryUrl,
      method: 'GET',
      success: (catRes) => {
        let categoryName = type
        if (catRes.data.code === 200 && catRes.data.data) {
          const category = catRes.data.data.find(c => c.id == categoryId)
          if (category) {
            categoryName = category.name
          }
        }
        this.setData({ categoryName })
      },
      fail: () => {
        const categoryName = categoryMap[type]?.name || type
        this.setData({ categoryName: categoryName })
      }
    })
    
    const url = `${app.globalData.questionUrl}/question/list?categoryId=${categoryId}`
    console.log('请求URL:', url)
    
    wx.request({
      url: url,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        console.log('API返回数据:', res.data)
        
        if (res.data.code === 200 && res.data.data) {
          console.log('题目数量:', res.data.data.length)
          
          if (res.data.data.length === 0) {
            this.setData({ loading: false })
            wx.showModal({
              title: '提示',
              content: `「${categoryName}」题库暂无题目，请选择其他题库`,
              showCancel: false,
              success: () => {
                wx.navigateBack()
              }
            })
            return
          }
          
          const questions = res.data.data.map(q => {
            let options = []
            if (q.options) {
              try {
                let optionsArr = null
                
                if (typeof q.options === 'string') {
                  try {
                    optionsArr = JSON.parse(q.options)
                  } catch (e) {
                    if (q.options.startsWith('[') || q.options.startsWith('A.')) {
                      optionsArr = q.options
                        .replace(/^\[|\]$/g, '')
                        .split(/,\s*(?=[A-Z]\.)/)
                        .map(item => item.replace(/^[A-Z]\.\s*/, '').trim())
                    } else {
                      optionsArr = q.options.split(',').map(item => item.trim())
                    }
                  }
                } else if (Array.isArray(q.options)) {
                  optionsArr = q.options
                }
                
                if (Array.isArray(optionsArr)) {
                  options = optionsArr.map((text, index) => ({
                    label: String.fromCharCode(65 + index),
                    text: typeof text === 'string' ? text.replace(/^[A-Z]\.\s*/, '') : String(text)
                  }))
                }
              } catch (e) {
                console.error('解析选项失败', e, q.options)
              }
            }
            
            return {
              id: q.id,
              type: 'single',
              content: q.content,
              difficulty: q.difficulty === 1 ? '简单' : q.difficulty === 2 ? '中等' : '困难',
              difficultyLevel: q.difficulty === 1 ? 'easy' : q.difficulty === 2 ? 'medium' : 'hard',
              options: options,
              answer: this.getAnswerIndex(q.answer),
              analysis: q.analysis || ''
            }
          })
          
          const shuffledQuestions = this.shuffleArray(questions)
          const limitedQuestions = shuffledQuestions.slice(0, 10)
          console.log('处理后的题目数量:', limitedQuestions.length)
          
          this.setData({ 
            questions: limitedQuestions,
            loading: false
          })
          
          if (limitedQuestions.length > 0) {
            this.initQuestion()
            this.startTimer()
          } else {
            wx.showToast({
              title: '暂无题目',
              icon: 'none'
            })
          }
        } else {
          this.setData({ loading: false })
          wx.showToast({
            title: '加载失败',
            icon: 'none'
          })
        }
      },
      fail: (err) => {
        console.error('加载题目失败', err)
        this.setData({ loading: false })
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    })
  },

  onUnload() {
    this.stopTimer()
  },

  startTimer() {
    this.timer = setInterval(() => {
      const elapsed = Math.floor((Date.now() - this.data.startTime) / 1000)
      const minutes = Math.floor(elapsed / 60)
      const seconds = elapsed % 60
      this.setData({
        elapsedTime: elapsed,
        timerText: `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
      })
    }, 1000)
  },

  stopTimer() {
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },

  formatTime(seconds) {
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  },

  shuffleArray(array) {
    const newArray = [...array]
    for (let i = newArray.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [newArray[i], newArray[j]] = [newArray[j], newArray[i]]
    }
    return newArray
  },

  initQuestion() {
    const { questions, currentIndex, showAnswerDefault, answerList, viewedQuestions } = this.data
    if (questions.length === 0) return
    
    const question = questions[currentIndex]
    const answeredInfo = answerList[currentIndex]
    
    if (viewedQuestions.indexOf(question.id) === -1) {
      this.incrementViewCount(question.id)
      this.setData({
        viewedQuestions: [...viewedQuestions, question.id]
      })
    }
    
    if (answeredInfo) {
      this.setData({
        currentQuestion: question,
        selectedOption: answeredInfo.userAnswer,
        isAnswered: true,
        correctAnswer: question.answer,
        analysis: question.analysis
      })
    } else {
      this.setData({
        currentQuestion: question,
        selectedOption: null,
        isAnswered: false,
        correctAnswer: null,
        analysis: ''
      })
      
      if (showAnswerDefault) {
        this.setData({
          isAnswered: true,
          correctAnswer: question.answer,
          analysis: question.analysis
        })
      }
    }
    
    this.checkFavorite()
  },

  incrementViewCount(questionId) {
    const app = getApp()
    wx.request({
      url: `${app.globalData.questionUrl}/question/view/${questionId}`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      }
    })
  },

  checkFavorite() {
    const app = getApp()
    const questionId = this.data.currentQuestion?.id
    if (!questionId) return
    
    favoriteApi.checkFavorite(questionId).then(res => {
      this.setData({ isFavorite: res.data })
    }).catch(() => {
      this.setData({ isFavorite: false })
    })
  },

  selectOption(e) {
    if (this.data.isAnswered) return
    
    const index = e.currentTarget.dataset.index
    this.setData({ selectedOption: index })
  },

  submitAnswer() {
    const { selectedOption, currentQuestion, questions, currentIndex, answerList } = this.data
    if (selectedOption === null) return
    
    const isCorrect = selectedOption === currentQuestion.answer
    const newAnswerList = [...answerList]
    newAnswerList[currentIndex] = {
      questionId: currentQuestion.id,
      userAnswer: selectedOption,
      isCorrect
    }
    
    this.setData({
      isAnswered: true,
      correctAnswer: currentQuestion.answer,
      answeredCount: this.data.answeredCount + 1,
      correctCount: isCorrect ? this.data.correctCount + 1 : this.data.correctCount,
      wrongCount: !isCorrect ? this.data.wrongCount + 1 : this.data.wrongCount,
      answerList: newAnswerList
    })
    
    this.submitToBackend(currentQuestion.id, selectedOption, isCorrect)
    
    if (!isCorrect) {
      this.addToWrongbook()
    } else {
      this.markAsMastered()
    }
  },

  markAsMastered() {
    const { currentQuestion } = this.data
    if (!currentQuestion?.id) return
    
    wrongbookApi.markAsMastered(currentQuestion.id).then(() => {
      console.log('已标记为已掌握')
    }).catch(err => {
      console.error('标记已掌握失败', err)
    })
  },

  submitToBackend(questionId, userAnswer, isCorrect) {
    const app = getApp()
    const spendSeconds = this.data.elapsedTime || 0
    practiceApi.submitAnswer(questionId, userAnswer, spendSeconds).catch(err => {
      console.error('提交答题记录失败', err)
    })
  },

  addToWrongbook() {
    const { currentQuestion, type } = this.data
    const app = getApp()
    
    wrongbookApi.addWrongbook({
      questionId: currentQuestion.id,
      categoryId: categoryMap[type]?.id || 1,
      title: currentQuestion.content,
      type: currentQuestion.type === 'single' ? 1 : 2,
      difficulty: currentQuestion.difficultyLevel === 'easy' ? 1 : (currentQuestion.difficultyLevel === 'medium' ? 2 : 3)
    }).catch(err => {
      console.error('添加错题失败', err)
    })
  },

  prevQuestion() {
    const { currentIndex } = this.data
    if (currentIndex === 0) return
    
    this.setData({ currentIndex: currentIndex - 1 })
    this.initQuestion()
  },

  nextQuestion() {
    if (this.data.isSingleQuestion) {
      wx.navigateBack()
      return
    }
    
    const { currentIndex, questions } = this.data
    if (currentIndex < questions.length - 1) {
      this.setData({ currentIndex: currentIndex + 1 })
      this.initQuestion()
    } else {
      this.showResult()
    }
  },

  showResult() {
    this.stopTimer()
    const { correctCount, wrongCount, questions, answerList, elapsedTime, categoryName } = this.data
    const totalCount = questions.length
    const correctRate = Math.round(correctCount / totalCount * 100)
    
    const resultData = {
      categoryName,
      totalCount,
      correctCount,
      wrongCount,
      correctRate,
      elapsedTime,
      questions: questions.map((q, idx) => ({
        id: q.id,
        content: q.content,
        options: q.options,
        answer: q.answer,
        analysis: q.analysis,
        difficulty: q.difficulty,
        difficultyLevel: q.difficultyLevel,
        userAnswer: answerList[idx]?.userAnswer,
        isCorrect: answerList[idx]?.isCorrect
      }))
    }
    
    wx.setStorageSync('practiceResult', resultData)
    wx.redirectTo({
      url: '/pages/result/result'
    })
  },

  restartPractice() {
    const { type } = this.data
    const categoryId = categoryMap[type]?.id || 1
    
    const app = getApp()
    const url = `${app.globalData.questionUrl}/question/list?categoryId=${categoryId}`
    
    this.setData({ loading: true })
    
    wx.request({
      url: url,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const questions = res.data.data.map(q => {
            let options = []
            if (q.options) {
              try {
                const optionsArr = JSON.parse(q.options)
                options = optionsArr.map((text, index) => ({
                  label: String.fromCharCode(65 + index),
                  text: text
                }))
              } catch (e) {
                console.error('解析选项失败', e)
              }
            }
            
            return {
              id: q.id,
              type: 'single',
              content: q.content,
              difficulty: q.difficulty === 1 ? '简单' : q.difficulty === 2 ? '中等' : '困难',
              difficultyLevel: q.difficulty === 1 ? 'easy' : q.difficulty === 2 ? 'medium' : 'hard',
              options: options,
              answer: this.getAnswerIndex(q.answer),
              analysis: q.analysis || ''
            }
          })
          
          const shuffledQuestions = this.shuffleArray(questions)
          const limitedQuestions = shuffledQuestions.slice(0, 10)
          
          this.setData({
            questions: limitedQuestions,
            currentIndex: 0,
            answeredCount: 0,
            correctCount: 0,
            wrongCount: 0,
            answerList: [],
            loading: false
          })
          
          this.initQuestion()
        } else {
          this.setData({ loading: false })
          wx.showToast({
            title: '加载失败',
            icon: 'none'
          })
        }
      },
      fail: (err) => {
        console.error('加载题目失败', err)
        this.setData({ loading: false })
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    })
  },

  toggleAnswerSheet() {
    this.setData({ showAnswerSheet: !this.data.showAnswerSheet })
  },

  goToQuestion(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ 
      currentIndex: index,
      showAnswerSheet: false
    })
    this.initQuestion()
  },

  toggleFavorite() {
    const { isFavorite, currentQuestion } = this.data
    const questionId = currentQuestion?.id
    if (!questionId) return
    
    if (isFavorite) {
      favoriteApi.removeFavorite(questionId).then(() => {
        this.setData({ isFavorite: false })
        wx.showToast({ title: '已取消收藏', icon: 'success' })
      }).catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
    } else {
      favoriteApi.addFavorite({
        questionId: questionId,
        categoryId: categoryMap[this.data.type]?.id || 1,
        title: currentQuestion.content,
        type: currentQuestion.type === 'single' ? 1 : 2,
        difficulty: currentQuestion.difficultyLevel === 'easy' ? 1 : (currentQuestion.difficultyLevel === 'medium' ? 2 : 3)
      }).then(() => {
        this.setData({ isFavorite: true })
        wx.showToast({ title: '收藏成功', icon: 'success' })
      }).catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
    }
  },

  goBack() {
    wx.navigateBack()
  }
})
