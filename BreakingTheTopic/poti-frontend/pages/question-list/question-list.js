const categoryIcons = {
  'Java': '☕',
  'Python': '🐍',
  'C/C++': '⚙️',
  'Go': '🔵',
  'JavaScript': '🟨',
  'PHP': '🐘',
  '数据结构': '📊',
  '操作系统': '💻',
  '计算机网络': '🌐',
  '计算机组成': '🔧',
  '编译原理': '📝',
  'MySQL': '🗄️',
  'Redis': '🚀',
  'MongoDB': '🍃',
  'PostgreSQL': '🐘',
  'Spring': '🍃',
  'MyBatis': '📄',
  'Dubbo': '📞',
  'Netty': '⚡',
  '消息队列': '📨',
  'Nginx': '🌐',
  'Zookeeper': '🌳',
  '微服务': '🔷',
  'Docker': '🐳',
  'Kubernetes': '☸️',
  'Vue': '💚',
  'React': '⚛️',
  'Node.js': '💚',
  'TypeScript': '🔷',
  '机器学习': '🤖',
  '深度学习': '🧠',
  'NLP': '💬',
  '计算机视觉': '👁️',
  'Hadoop': '🐘',
  'Spark': '⚡',
  'Flink': '🌊',
  '数据仓库': '📦',
  'Web安全': '🔒',
  '密码学': '🔐',
  '软件测试': '🧪',
  'Linux': '🐧',
  'Git': '📚',
  '高等数学': '📐',
  '线性代数': '📈',
  '概率论': '🎲',
  '离散数学': '🔢',
  '算法面试': '🎯',
  '系统设计': '🏗️',
  'HR面试': '👥',
  '行为面试': '💼'
}

Page({
  data: {
    categoryId: null,
    categoryName: '',
    categoryIcon: '📚',
    questions: [],
    loading: true,
    showAll: false,
    displayQuestions: []
  },

  onLoad(options) {
    const categoryId = options.categoryId
    const categoryName = options.name ? decodeURIComponent(options.name) : ''
    
    this.setData({
      categoryId: categoryId,
      categoryName: categoryName,
      categoryIcon: categoryIcons[categoryName] || '📚'
    })
    
    if (categoryId) {
      this.loadQuestions(categoryId)
    }
    
    wx.setNavigationBarTitle({
      title: categoryName || '题目列表'
    })
  },

  loadQuestions(categoryId) {
    const app = getApp()
    const that = this
    
    wx.request({
      url: `${app.globalData.questionUrl}/question/list?categoryId=${categoryId}`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: function(res) {
        if (res.data.code === 200 && res.data.data) {
          const questions = res.data.data.map(function(q, index) {
            return {
              id: q.id,
              title: q.title || `题目 ${index + 1}`,
              type: that.getQuestionType(q.type),
              difficulty: that.getDifficultyText(q.difficulty),
              difficultyClass: that.getDifficultyClass(q.difficulty),
              viewCount: q.viewCount || 0,
              favoriteCount: q.favoriteCount || 0
            }
          })
          
          const displayQuestions = questions.slice(0, 20)
          
          that.setData({
            questions: questions,
            displayQuestions: displayQuestions,
            loading: false
          })
        } else {
          that.setData({ loading: false })
        }
      },
      fail: function() {
        that.setData({ loading: false })
        wx.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    })
  },

  getQuestionType(type) {
    const typeMap = {
      1: '单选题',
      2: '多选题',
      3: '判断题',
      4: '简答题',
      5: '编程题'
    }
    return typeMap[type] || '未知'
  },

  getDifficultyText(difficulty) {
    const map = {
      1: '简单',
      2: '中等',
      3: '困难'
    }
    return map[difficulty] || '中等'
  },

  getDifficultyClass(difficulty) {
    const map = {
      1: 'easy',
      2: 'medium',
      3: 'hard'
    }
    return map[difficulty] || 'medium'
  },

  toggleShowAll() {
    const { questions, showAll } = this.data
    if (showAll) {
      this.setData({
        showAll: false,
        displayQuestions: questions.slice(0, 20)
      })
    } else {
      this.setData({
        showAll: true,
        displayQuestions: questions
      })
    }
  },

  goToQuestion(e) {
    const questionId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/question-detail/question-detail?id=${questionId}`
    })
  },

  startPractice() {
    const { categoryId } = this.data
    wx.navigateTo({
      url: `/pages/answer/answer?categoryId=${categoryId}`
    })
  }
})
