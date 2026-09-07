const practiceApi = require('../../api/practice.js')
const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    activeTab: 'wrong',
    activeFilter: 'all',
    wrongList: [],
    filteredList: [],
    practiceRecords: [],
    interviewWrongList: []
  },

  _isDestroyed: false,

  onLoad(options) {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: '错题本功能需要登录后才能使用',
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
    console.log('错题本页面加载')
    this._isDestroyed = false
    
    if (options.tab === 'choice') {
      this.setData({ activeTab: 'choice' })
    }
    
    this.loadWrongbook()
    this.loadPracticeRecords()
    this.loadInterviewWrong()
  },

  onShow() {
    if (guestUtils.checkGuest()) {
      return
    }
    console.log('错题本页面显示')
    if (!this._isDestroyed) {
      this.loadWrongbook()
      this.loadPracticeRecords()
      this.loadInterviewWrong()
    }
  },

  onUnload() {
    console.log('错题本页面卸载')
    this._isDestroyed = true
  },

  loadWrongbook() {
    const app = getApp()
    wx.request({
      url: `${app.globalData.wrongbookUrl}/wrongbook/list`,
      method: 'GET',
      header: {
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200) {
          const typeMap = { 1: '单选', 2: '多选', 3: '判断' }
          const difficultyMap = { 1: '简单', 2: '中等', 3: '困难' }
          const difficultyLevelMap = { 1: 'easy', 2: 'medium', 3: 'hard' }
          const wrongList = (res.data.data || []).map(item => ({
            ...item,
            typeText: typeMap[item.type] || '单选',
            difficultyText: difficultyMap[item.difficulty] || '中等',
            difficultyLevel: difficultyLevelMap[item.difficulty] || 'medium',
            isPassed: item.mastered === 1,
            userAnswer: item.userAnswer || 'A',
            correctAnswer: item.correctAnswer || 'B',
            categoryName: item.categoryName || '综合',
            knowledgePoints: item.knowledgePoints || ['基础知识']
          }))
          this.setData({ wrongList })
          this.applyFilter()
        }
      }
    })
  },

  loadPracticeRecords() {
    const app = getApp()
    practiceApi.getPracticeHistory(1, 20).then(res => {
      console.log('练习记录返回数据：', res)
      if (res.code === 200 && !this._isDestroyed) {
        console.log('练习记录列表：', res.data.list)
        const records = (res.data.list || []).map((item, index) => {
          console.log('单条记录：', item)
          const practiceTime = item.practiceTime || item.createTime
          
          return {
            id: item.id,
            questionId: item.questionId,
            questionTitle: item.questionTitle || '题目 #' + item.questionId,
            categoryName: item.categoryName || '综合',
            categoryType: `question_${item.questionId}`,
            completeTime: this.formatDateTime(practiceTime),
            correctCount: item.isCorrect ? 1 : 0,
            totalCount: 1,
            correctRate: item.isCorrect ? 100 : 0,
            spendSeconds: item.spendSeconds || 0,
            duration: this.formatDuration(item.spendSeconds || 0),
            isCorrect: item.isCorrect
          }
        })
        if (!this._isDestroyed) {
          this.setData({ practiceRecords: records })
        }
      }
    }).catch(err => {
      console.error('加载练习记录失败', err)
      if (!this._isDestroyed) {
        const mockRecords = [
        {
          id: 1,
          questionId: 1,
          questionTitle: 'Java中哪个关键字用于定义类？',
          categoryName: 'Java',
          categoryType: 'question_1',
          completeTime: '2026-04-06 14:30:25',
          correctCount: 1,
          totalCount: 1,
          correctRate: 100,
          spendSeconds: 332,
          duration: '05:32',
          isCorrect: true
        },
        {
          id: 2,
          questionId: 31,
          questionTitle: 'Redis默认端口号是？',
          categoryName: 'Redis',
          categoryType: 'question_31',
          completeTime: '2026-04-06 12:15:48',
          correctCount: 1,
          totalCount: 1,
          correctRate: 100,
          spendSeconds: 525,
          duration: '08:45',
          isCorrect: true
        },
        {
          id: 3,
          questionId: 21,
          questionTitle: 'MySQL中主键的特点是？',
          categoryName: 'MySQL',
          categoryType: 'question_21',
          completeTime: '2026-04-05 16:20:12',
          correctCount: 1,
          totalCount: 1,
          correctRate: 100,
          spendSeconds: 612,
          duration: '10:12',
          isCorrect: false
        }
      ]
      this.setData({ practiceRecords: mockRecords })
      }
    })
  },

  loadInterviewWrong() {
    const app = getApp()
    wx.request({
      url: `${app.globalData.interviewUrl}/interview/wrong`,
      method: 'GET',
      header: {
        'X-User-Id': app.globalData.userId || 1
      },
      success: (res) => {
        if (res.data.code === 200 && !this._isDestroyed) {
          const interviewWrongList = (res.data.data || []).map(item => {
            let correctAnswerContent = item.correctAnswer || item.referenceAnswer || '参考答案'
            let userAnswerContent = item.userAnswer || '未作答'
            
            if (item.options) {
              try {
                const options = JSON.parse(item.options)
                if (Array.isArray(options)) {
                  const optionMap = {}
                  options.forEach((opt, index) => {
                    const letter = String.fromCharCode(65 + index)
                    optionMap[letter] = opt
                  })
                  
                  if (item.correctAnswer && optionMap[item.correctAnswer.toUpperCase()]) {
                    correctAnswerContent = `${item.correctAnswer}. ${optionMap[item.correctAnswer.toUpperCase()]}`
                  }
                  
                  if (item.userAnswer && optionMap[item.userAnswer.toUpperCase()]) {
                    userAnswerContent = `${item.userAnswer}. ${optionMap[item.userAnswer.toUpperCase()]}`
                  }
                }
              } catch (e) {
                console.error('解析选项失败', e)
              }
            }
            
            return {
              ...item,
              createTimeText: this.formatDateTime(item.createTime),
              correctAnswerContent,
              userAnswerContent
            }
          })
          this.setData({ interviewWrongList })
        }
      },
      fail: (err) => {
        console.error('加载面试错题失败', err)
      }
    })
  },

  formatDateTime(dateStr) {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    const second = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}:${second}`
  },

  formatDuration(seconds) {
    const m = Math.floor(seconds / 60)
    const s = seconds % 60
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
  },

  switchFilter(e) {
    const filter = e.currentTarget.dataset.filter
    this.setData({ activeFilter: filter })
    this.applyFilter()
  },

  applyFilter() {
    const { wrongList, activeFilter } = this.data
    let filteredList = [...wrongList]
    
    if (activeFilter === 'failed') {
      filteredList = wrongList.filter(item => !item.isPassed)
    } else if (activeFilter === 'passed') {
      filteredList = wrongList.filter(item => item.isPassed)
    }
    
    this.setData({ filteredList })
  },

  viewQuestion(e) {
    const questionId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/question-detail/question-detail?id=${questionId}`
    })
  },

  viewInterviewQuestion(e) {
    const question = e.currentTarget.dataset.item
    wx.showModal({
      title: '题目详情',
      content: question.title || '暂无题目内容',
      showCancel: false
    })
  },

  removeWrong(e) {
    const questionId = e.currentTarget.dataset.id
    const app = getApp()
    
    wx.showModal({
      title: '提示',
      content: '确定要从错题本中移除吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.wrongbookUrl}/wrongbook/${questionId}`,
            method: 'DELETE',
            header: {
              'X-User-Id': app.globalData.userId || 1
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({
                  title: '移除成功',
                  icon: 'success'
                })
                this.loadWrongbook()
              }
            }
          })
        }
      }
    })
  },

  removeInterviewWrong(e) {
    const id = e.currentTarget.dataset.id
    const app = getApp()
    
    wx.showModal({
      title: '提示',
      content: '确定要从面试错题中移除吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.interviewUrl}/interview/wrong/${id}`,
            method: 'DELETE',
            header: {
              'X-User-Id': app.globalData.userId || 1
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({
                  title: '移除成功',
                  icon: 'success'
                })
                this.loadInterviewWrong()
              } else {
                wx.showToast({
                  title: res.data.msg || '移除失败',
                  icon: 'none'
                })
              }
            },
            fail: () => {
              wx.showToast({
                title: '移除失败',
                icon: 'none'
              })
            }
          })
        }
      }
    })
  },

  retryQuestion(e) {
    const questionId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/answer/answer?questionId=${questionId}`
    })
  },

  retryInterviewQuestion(e) {
    const question = e.currentTarget.dataset.item
    if (question.questionId) {
      wx.navigateTo({
        url: `/pages/question-detail/question-detail?id=${question.questionId}`
      })
    } else {
      wx.showToast({
        title: '题目不存在',
        icon: 'none'
      })
    }
  },

  retryAll() {
    const { filteredList } = this.data
    if (filteredList.length === 0) {
      wx.showToast({
        title: '暂无错题',
        icon: 'none'
      })
      return
    }
    
    const questionIds = filteredList.map(item => item.questionId).join(',')
    wx.navigateTo({
      url: `/pages/answer/answer?questionIds=${questionIds}`
    })
  },

  retryAllInterview() {
    const { interviewWrongList } = this.data
    if (interviewWrongList.length === 0) {
      wx.showToast({
        title: '暂无错题',
        icon: 'none'
      })
      return
    }
    
    const questionIds = interviewWrongList
      .filter(item => item.questionId)
      .map(item => item.questionId)
      .join(',')
    
    if (questionIds) {
      wx.navigateTo({
        url: `/pages/answer/answer?questionIds=${questionIds}`
      })
    } else {
      wx.showToast({
        title: '暂无可重做的题目',
        icon: 'none'
      })
    }
  },

  retryPractice(e) {
    const questionId = e.currentTarget.dataset.questionId
    if (questionId) {
      wx.navigateTo({
        url: `/pages/answer/answer?questionId=${questionId}`
      })
    } else {
      wx.showToast({
        title: '题目不存在',
        icon: 'none'
      })
    }
  },

  deleteRecord(e) {
    const recordId = e.currentTarget.dataset.id
    const index = e.currentTarget.dataset.index
    
    wx.showModal({
      title: '提示',
      content: '确定要删除这条记录吗？',
      success: (res) => {
        if (res.confirm) {
          practiceApi.deleteRecord(recordId).then(result => {
            if (result.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              this.loadPracticeRecords()
            } else {
              wx.showToast({
                title: result.msg || '删除失败',
                icon: 'none'
              })
            }
          }).catch(err => {
            console.error('删除记录失败', err)
            wx.showToast({
              title: '删除失败',
              icon: 'none'
            })
          })
        }
      }
    })
  },

  clearAllRecords() {
    wx.showModal({
      title: '提示',
      content: '确定要清空所有练习记录吗？此操作不可恢复！',
      confirmText: '确定清空',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          practiceApi.clearAllRecords().then(result => {
            if (result.code === 200) {
              wx.showToast({
                title: '清空成功',
                icon: 'success'
              })
              this.setData({ practiceRecords: [] })
            } else {
              wx.showToast({
                title: result.msg || '清空失败',
                icon: 'none'
              })
            }
          }).catch(err => {
            console.error('清空记录失败', err)
            wx.showToast({
              title: '清空失败',
              icon: 'none'
            })
          })
        }
      }
    })
  },

  clearAllWrongbook() {
    wx.showModal({
      title: '提示',
      content: '确定要清空错题本吗？此操作不可恢复！',
      confirmText: '确定清空',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          const app = getApp()
          wx.request({
            url: `${app.globalData.wrongbookUrl}/wrongbook/clear`,
            method: 'DELETE',
            header: {
              'X-User-Id': app.globalData.userId || 1
            },
            success: (response) => {
              if (response.data.code === 200) {
                wx.showToast({
                  title: '清空成功',
                  icon: 'success'
                })
                this.setData({ 
                  wrongList: [],
                  filteredList: []
                })
              } else {
                wx.showToast({
                  title: response.data.msg || '清空失败',
                  icon: 'none'
                })
              }
            },
            fail: () => {
              wx.showToast({
                title: '清空失败',
                icon: 'none'
              })
            }
          })
        }
      }
    })
  },

  clearAllInterviewWrong() {
    wx.showModal({
      title: '提示',
      content: '确定要清空所有面试错题吗？此操作不可恢复！',
      confirmText: '确定清空',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          const app = getApp()
          wx.request({
            url: `${app.globalData.interviewUrl}/interview/wrong/clear`,
            method: 'DELETE',
            header: {
              'X-User-Id': app.globalData.userId || 1
            },
            success: (response) => {
              if (response.data.code === 200) {
                wx.showToast({
                  title: '清空成功',
                  icon: 'success'
                })
                this.setData({ interviewWrongList: [] })
              } else {
                wx.showToast({
                  title: response.data.msg || '清空失败',
                  icon: 'none'
                })
              }
            },
            fail: () => {
              wx.showToast({
                title: '清空失败',
                icon: 'none'
              })
            }
          })
        }
      }
    })
  }
})
