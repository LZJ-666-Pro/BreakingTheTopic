import Toast from '@vant/weapp/toast/toast';

const app = getApp()
const guestUtils = require('../../utils/guest.js')

const typeNames = {
  'java': 'Java',
  'python': 'Python',
  'mysql': 'MySQL',
  'redis': 'Redis',
  'spring': 'Spring',
  'mq': '消息队列'
}

const difficultyNames = {
  1: '简单',
  2: '中等',
  3: '困难'
}

Page({
  data: {
    loading: true,
    questionId: null,
    question: {},
    options: [],
    selectedOption: null,
    showAnswer: false,
    isFavorited: false,
    isMarked: false,
    currentTab: 'analysis',
    typeName: '',
    difficultyName: '',
    moreQuestions: [],
    discussions: [],
    discussionContent: ''
  },

  onLoad(options) {
    const questionId = options.id || options.questionId
    if (questionId) {
      this.setData({ questionId })
      this.loadQuestionDetail(questionId)
      this.loadMoreQuestions()
      this.checkMarkedStatus()
    }
  },

  loadQuestionDetail(questionId) {
    this.setData({ loading: true })
    
    wx.request({
      url: `${app.globalData.questionUrl}/question/detail/${questionId}`,
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
          }
          
          this.setData({
            question: questionData,
            options: options,
            typeName: typeNames[questionData.type] || questionData.type,
            difficultyName: difficultyNames[questionData.difficulty] || '中等',
            loading: false
          })
          
          this.checkFavoriteStatus()
        } else {
          Toast.fail('加载失败')
          this.setData({ loading: false })
        }
      },
      fail: (err) => {
        console.error('加载题目详情失败', err)
        Toast.fail('加载失败')
        this.setData({ loading: false })
      }
    })
  },

  loadMoreQuestions() {
    wx.request({
      url: `${app.globalData.questionUrl}/question/random/global?limit=5`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const questions = res.data.data.filter(q => q.id != this.data.questionId)
          this.setData({
            moreQuestions: questions.map(q => ({
              ...q,
              typeName: typeNames[q.type] || q.type,
              difficultyName: difficultyNames[q.difficulty] || '中等'
            }))
          })
        }
      }
    })
  },

  checkFavoriteStatus() {
    wx.request({
      url: `${app.globalData.favoriteUrl}/favorite/check/${this.data.questionId}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            isFavorited: res.data.data || false
          })
        }
      }
    })
  },

  checkMarkedStatus() {
    const markedQuestions = wx.getStorageSync('markedQuestions') || []
    const isMarked = markedQuestions.includes(this.data.questionId)
    this.setData({ isMarked })
  },

  selectOption(e) {
    const index = e.currentTarget.dataset.index
    this.setData({
      selectedOption: index
    })
  },

  showAnswerHandler() {
    this.setData({
      showAnswer: true
    })
  },

  toggleFavorite() {
    if (!guestUtils.requireLogin(null, '收藏功能需要登录后才能使用')) {
      return
    }
    
    if (this.data.isFavorited) {
      wx.request({
        url: `${app.globalData.favoriteUrl}/favorite/remove/${this.data.questionId}`,
        method: 'DELETE',
        header: {
          'Authorization': `Bearer ${app.globalData.token}`,
          'X-User-Id': app.globalData.userId
        },
        success: (res) => {
          if (res.data.code === 200) {
            this.setData({
              isFavorited: false
            })
            Toast.success('取消收藏')
          }
        }
      })
    } else {
      wx.request({
        url: `${app.globalData.favoriteUrl}/favorite/add`,
        method: 'POST',
        data: {
          questionId: this.data.questionId,
          title: this.data.question.title,
          type: this.data.question.type,
          difficulty: this.data.question.difficulty
        },
        header: {
          'Authorization': `Bearer ${app.globalData.token}`,
          'X-User-Id': app.globalData.userId
        },
        success: (res) => {
          if (res.data.code === 200) {
            this.setData({
              isFavorited: true
            })
            Toast.success('收藏成功')
          }
        }
      })
    }
  },

  markQuestion() {
    if (!guestUtils.requireLogin(null, '标记功能需要登录后才能使用')) {
      return
    }
    
    const markedQuestions = wx.getStorageSync('markedQuestions') || []
    const questionId = this.data.questionId
    
    const index = markedQuestions.indexOf(questionId)
    if (index > -1) {
      markedQuestions.splice(index, 1)
      wx.setStorageSync('markedQuestions', markedQuestions)
      this.setData({ isMarked: false })
      Toast.success('已取消标记')
    } else {
      markedQuestions.unshift(questionId)
      wx.setStorageSync('markedQuestions', markedQuestions)
      this.setData({ isMarked: true })
      Toast.success('已标记')
    }
  },

  shareQuestion() {
    wx.showActionSheet({
      itemList: ['分享给好友', '生成海报', '复制链接'],
      success: (res) => {
        switch (res.tapIndex) {
          case 0:
            wx.showShareMenu({
              withShareTicket: true,
              menus: ['shareAppMessage']
            })
            break
          case 1:
            Toast('海报生成功能开发中')
            break
          case 2:
            wx.setClipboardData({
              data: `题目：${this.data.question.title}`,
              success: () => {
                Toast.success('已复制')
              }
            })
            break
        }
      }
    })
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({
      currentTab: tab
    })
    
    if (tab === 'discussion') {
      this.loadDiscussions()
    }
  },

  loadDiscussions() {
    wx.request({
      url: `${app.globalData.baseUrl}/discussion/list/${this.data.questionId}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          const currentUserId = app.globalData.userId
          const discussions = (data.discussions || []).map(item => ({
            ...item,
            createTime: this.formatTime(item.createTime),
            isLiked: data.likeStatus ? (data.likeStatus[item.id] || false) : false,
            isOwner: item.userId === currentUserId
          }))
          this.setData({ discussions })
        }
      }
    })
  },

  formatTime(timeStr) {
    if (!timeStr) return ''
    
    const date = new Date(timeStr)
    const now = new Date()
    const diff = now - date
    
    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
    
    return `${date.getMonth() + 1}月${date.getDate()}日`
  },

  onDiscussionInput(e) {
    this.setData({
      discussionContent: e.detail.value
    })
  },

  submitDiscussion() {
    if (!guestUtils.requireLogin(null, '发表评论需要登录后才能使用')) {
      return
    }
    
    if (!this.data.discussionContent.trim()) {
      Toast('请输入评论内容')
      return
    }
    
    wx.request({
      url: `${app.globalData.baseUrl}/discussion/add`,
      method: 'POST',
      data: {
        questionId: this.data.questionId,
        content: this.data.discussionContent
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          Toast.success('发表成功')
          this.setData({ discussionContent: '' })
          this.loadDiscussions()
        } else {
          Toast.fail(res.data.msg || '发表失败，请稍后重试')
        }
      },
      fail: () => {
        Toast.fail('网络错误，请重试')
      }
    })
  },

  likeDiscussion(e) {
    const id = e.currentTarget.dataset.id
    
    wx.request({
      url: `${app.globalData.baseUrl}/discussion/like/${id}`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.loadDiscussions()
        }
      }
    })
  },

  replyDiscussion(e) {
    const id = e.currentTarget.dataset.id
    const name = e.currentTarget.dataset.name
    const discussion = this.data.discussions.find(item => item.id === id)
    if (discussion) {
      this.setData({
        discussionContent: `@${name} `
      })
    }
  },

  showDiscussionActions(e) {
    const id = e.currentTarget.dataset.id
    wx.showActionSheet({
      itemList: ['删除评论'],
      itemColor: '#FA5151',
      success: (res) => {
        if (res.tapIndex === 0) {
          this.deleteDiscussion(id)
        }
      }
    })
  },

  deleteDiscussion(id) {
    wx.showModal({
      title: '提示',
      content: '确定删除这条评论吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.baseUrl}/discussion/${id}`,
            method: 'DELETE',
            header: {
              'Authorization': `Bearer ${app.globalData.token}`,
              'X-User-Id': app.globalData.userId
            },
            success: (res) => {
              if (res.data.code === 200) {
                Toast.success('删除成功')
                this.loadDiscussions()
              } else {
                Toast.fail('删除失败')
              }
            }
          })
        }
      }
    })
  },

  startAnswer() {
    wx.navigateTo({
      url: `/pages/answer/answer?questionId=${this.data.questionId}`
    })
  },

  refreshMore() {
    this.loadMoreQuestions()
    Toast.success('已刷新')
  },

  goToQuestion(e) {
    const item = e.currentTarget.dataset.item
    wx.redirectTo({
      url: `/pages/question-detail/question-detail?id=${item.id}`
    })
  },

  goBack() {
    wx.navigateBack()
  },

  onShareAppMessage() {
    return {
      title: this.data.question.title,
      path: `/pages/question-detail/question-detail?id=${this.data.questionId}`
    }
  }
})
