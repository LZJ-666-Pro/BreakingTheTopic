const userApi = require('../../api/user.js')

const guestUtils = require('../../utils/guest.js')

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
  '网络安全': '🛡️',
  '渗透测试': '🎯',
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
  '行为面试': '💼',
  '区块链开发': '⛓️',
  'Web3开发': '🌐',
  'Solidity': '💎',
  '以太坊': '⟠',
  'Android开发': '🤖',
  'iOS开发': '🍎',
  'Flutter': '💙',
  'React Native': '📱',
  'Unity游戏开发': '🎮',
  '游戏引擎原理': '🕹️',
  '云原生': '☁️',
  'C++基础': '📘',
  'C++进阶': '📗',
  'C++新特性': '✨',
  'C++面向对象': '🔷',
  'C++STL': '📦',
  'C++并发编程': '🔄',
  'Go基础': '📘',
  'Go标准库': '📚',
  'Go面向对象': '🔷',
  'Go底层原理': '🔧',
  'Go性能优化': '⚡',
  'Go并发编程': '🔄',
  'Go垃圾回收': '🗑️',
  'Go代码分析': '🔍',
  'PHP基础': '📘',
  'PHP面向对象': '🔷',
  'PHP框架': '🏗️',
  'PHP应用场景': '🌐',
  'Python代码分析': '🔍',
  'Python手写代码': '✍️',
  '最全AI大模型库': '🤖',
  'C#': '💜',
  'C#基础': '📘',
  'C#集合': '📦',
  'C#面向对象': '🔷',
  'C#并发库': '🔄',
  'C#框架': '🏗️',
  'C#底层原理': '🔧',
  'C#WPF': '🖼️',
  'C#.NET': '🌐'
}

const categoryColors = {
  'Java': '#FF9500',
  'Python': '#4A90D9',
  'C/C++': '#00599C',
  'Go': '#00ADD8',
  'JavaScript': '#F7DF1E',
  'PHP': '#777BB4',
  '数据结构': '#FF6B6B',
  '操作系统': '#4ECDC4',
  '计算机网络': '#45B7D1',
  '计算机组成': '#96CEB4',
  '编译原理': '#DDA0DD',
  'MySQL': '#07C160',
  'Redis': '#FA5151',
  'MongoDB': '#4DB33D',
  'PostgreSQL': '#336791',
  'Spring': '#36CFC9',
  'MyBatis': '#E44D26',
  'Dubbo': '#FF6A00',
  'Netty': '#FF4500',
  '消息队列': '#7B61FF',
  'Nginx': '#009639',
  'Zookeeper': '#FF6B35',
  '微服务': '#1890FF',
  'Docker': '#2496ED',
  'Kubernetes': '#326CE5',
  'Vue': '#42B883',
  'React': '#61DAFB',
  'Node.js': '#339933',
  'TypeScript': '#3178C6',
  '机器学习': '#FF6F61',
  '深度学习': '#9B59B6',
  'NLP': '#3498DB',
  '计算机视觉': '#E74C3C',
  'Hadoop': '#66CCFF',
  'Spark': '#E25A1C',
  'Flink': '#E6526F',
  '数据仓库': '#8E44AD',
  'Web安全': '#C0392B',
  '密码学': '#16A085',
  '网络安全': '#2C3E50',
  '渗透测试': '#E74C3C',
  '软件测试': '#27AE60',
  'Linux': '#FCC624',
  'Git': '#F05032',
  '高等数学': '#E91E63',
  '线性代数': '#9C27B0',
  '概率论': '#673AB7',
  '离散数学': '#3F51B5',
  '算法面试': '#FF5722',
  '系统设计': '#795548',
  'HR面试': '#607D8B',
  '行为面试': '#795548',
  '区块链开发': '#F7931A',
  'Web3开发': '#627EEA',
  'Solidity': '#363636',
  '以太坊': '#627EEA',
  'Android开发': '#3DDC84',
  'iOS开发': '#147EFB',
  'Flutter': '#02569B',
  'React Native': '#61DAFB',
  'Unity游戏开发': '#222C37',
  '游戏引擎原理': '#FF6B6B',
  '云原生': '#009688',
  'C++基础': '#00599C',
  'C++进阶': '#0077B5',
  'C++新特性': '#00B4D8',
  'C++面向对象': '#0096C7',
  'C++STL': '#023E8A',
  'C++并发编程': '#03045E',
  'Go基础': '#00ADD8',
  'Go标准库': '#0096C7',
  'Go面向对象': '#0077B5',
  'Go底层原理': '#00599C',
  'Go性能优化': '#00B4D8',
  'Go并发编程': '#48CAE4',
  'Go垃圾回收': '#90E0EF',
  'Go代码分析': '#023E8A',
  'PHP基础': '#777BB4',
  'PHP面向对象': '#8892BF',
  'PHP框架': '#4F5B93',
  'PHP应用场景': '#8993BE',
  'Python代码分析': '#306998',
  'Python手写代码': '#FFD43B',
  '最全AI大模型库': '#646464',
  'C#': '#68217A',
  'C#基础': '#9B4F96',
  'C#集合': '#68217A',
  'C#面向对象': '#7B3F96',
  'C#并发库': '#5C2D91',
  'C#框架': '#4A235A',
  'C#底层原理': '#3D1F5C',
  'C#WPF': '#6B3FA0',
  'C#.NET': '#512BD4'
}

const categoryTags = {
  'Java': ['hot', 'backend', 'interview', 'java'],
  'Python': ['hot', 'backend', 'ai', 'python'],
  'C/C++': ['backend', 'interview', 'game', 'cpp'],
  'Go': ['hot', 'backend', 'interview', 'blockchain', 'go'],
  'JavaScript': ['hot', 'frontend', 'backend'],
  'PHP': ['backend', 'php'],
  'MySQL': ['hot', 'backend', 'interview'],
  'Redis': ['hot', 'backend', 'interview'],
  'MongoDB': ['backend'],
  'PostgreSQL': ['backend'],
  'Spring': ['hot', 'backend', 'interview', 'java'],
  'MyBatis': ['backend', 'interview', 'java'],
  'Dubbo': ['backend', 'java'],
  'Netty': ['backend', 'java'],
  '消息队列': ['backend', 'interview'],
  'Nginx': ['backend', 'interview', 'ops'],
  'Zookeeper': ['backend', 'ops'],
  '微服务': ['backend', 'interview'],
  'Docker': ['backend', 'ops'],
  'Kubernetes': ['backend', 'ops'],
  'Vue': ['hot', 'frontend', 'interview'],
  'React': ['hot', 'frontend', 'interview', 'mobile'],
  'Node.js': ['frontend', 'backend'],
  'TypeScript': ['frontend'],
  '数据结构': ['hot', 'backend', 'basic', 'interview', 'game'],
  '操作系统': ['backend', 'basic', 'interview', 'ops'],
  '计算机网络': ['backend', 'basic', 'interview'],
  '计算机组成': ['backend', 'basic'],
  '编译原理': ['backend', 'basic'],
  '机器学习': ['ai', 'python'],
  '深度学习': ['ai', 'python'],
  'NLP': ['ai', 'python'],
  '计算机视觉': ['ai', 'python'],
  'Hadoop': ['bigdata'],
  'Spark': ['bigdata'],
  'Flink': ['bigdata'],
  '数据仓库': ['bigdata'],
  'Web安全': ['backend', 'interview', 'security'],
  '密码学': ['backend', 'security', 'blockchain'],
  '网络安全': ['security'],
  '渗透测试': ['security'],
  '软件测试': ['test'],
  'Linux': ['backend', 'interview', 'ops'],
  'Git': ['backend'],
  '高等数学': ['basic'],
  '线性代数': ['basic'],
  '概率论': ['basic'],
  '离散数学': ['basic'],
  '算法面试': ['hot', 'interview'],
  '系统设计': ['hot', 'backend', 'interview'],
  'HR面试': ['interview'],
  '行为面试': ['interview'],
  '区块链开发': ['blockchain'],
  'Web3开发': ['blockchain'],
  'Solidity': ['blockchain'],
  '以太坊': ['blockchain'],
  'Android开发': ['mobile'],
  'iOS开发': ['mobile'],
  'Flutter': ['mobile', 'frontend'],
  'React Native': ['mobile', 'frontend'],
  'Unity游戏开发': ['game'],
  '游戏引擎原理': ['game'],
  '云原生': ['backend', 'ops'],
  'C++基础': ['backend', 'cpp', 'hidden'],
  'C++进阶': ['backend', 'cpp', 'hidden'],
  'C++新特性': ['backend', 'cpp', 'hidden'],
  'C++面向对象': ['backend', 'cpp', 'hidden'],
  'C++STL': ['backend', 'cpp', 'hidden'],
  'C++并发编程': ['backend', 'cpp', 'hidden'],
  'Go基础': ['backend', 'go', 'hidden'],
  'Go标准库': ['backend', 'go', 'hidden'],
  'Go面向对象': ['backend', 'go', 'hidden'],
  'Go底层原理': ['backend', 'go', 'hidden'],
  'Go性能优化': ['backend', 'go', 'hidden'],
  'Go并发编程': ['backend', 'go', 'hidden'],
  'Go垃圾回收': ['backend', 'go', 'hidden'],
  'Go代码分析': ['backend', 'go', 'hidden'],
  'PHP基础': ['backend', 'php', 'hidden'],
  'PHP面向对象': ['backend', 'php', 'hidden'],
  'PHP框架': ['backend', 'php', 'hidden'],
  'PHP应用场景': ['backend', 'php', 'hidden'],
  'Python代码分析': ['backend', 'ai', 'python', 'hidden'],
  'Python手写代码': ['backend', 'ai', 'python', 'hidden'],
  '最全AI大模型库': ['ai', 'python', 'hidden'],
  'C#': ['backend', 'csharp', 'hidden'],
  'C#基础': ['backend', 'csharp', 'hidden'],
  'C#集合': ['backend', 'csharp', 'hidden'],
  'C#面向对象': ['backend', 'csharp', 'hidden'],
  'C#并发库': ['backend', 'csharp', 'hidden'],
  'C#框架': ['backend', 'csharp', 'hidden'],
  'C#底层原理': ['backend', 'csharp', 'hidden'],
  'C#WPF': ['backend', 'csharp', 'hidden'],
  'C#.NET': ['backend', 'csharp', 'hidden']
}

const hiddenCategories = [
  'C++基础', 'C++进阶', 'C++新特性', 'C++面向对象', 'C++STL', 'C++并发编程',
  'Go基础', 'Go标准库', 'Go面向对象', 'Go底层原理', 'Go性能优化', 'Go并发编程', 'Go垃圾回收', 'Go代码分析',
  'PHP基础', 'PHP面向对象', 'PHP框架', 'PHP应用场景',
  'Python代码分析', 'Python手写代码', '最全AI大模型库',
  'C#', 'C#基础', 'C#集合', 'C#面向对象', 'C#并发库', 'C#框架', 'C#底层原理', 'C#WPF', 'C#.NET'
]

Page({
  data: {
    userInfo: {},
    todayCount: 0,
    wrongCount: 0,
    favoriteCount: 0,
    unreadCount: 0,
    totalQuestions: 0,
    historyCount: 0,
    accuracy: 0,
    streakDays: 0,
    hasCheckedIn: false,
    todayGoal: 10,
    progressPercent: 0,
    hasUnfinished: false,
    unfinishedCategory: '',
    unfinishedProgress: 0,
    categories: [],
    displayCategories: [],
    showAllCategories: false
  },

  onLoad() {
    this.checkLogin()
    this.loadUserInfo()
    this.loadCategories()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0
      })
    }
    this.loadUserInfo()
    this.loadStats()
    this.checkUnfinished()
    this.checkTodayCheckIn()
    this.loadUnreadCount()
  },

  loadUnreadCount() {
    const app = getApp()
    const token = app.globalData.token || wx.getStorageSync('token')
    const userId = wx.getStorageSync('userId')
    // 未登录且非游客模式：无有效凭据，不发请求
    if (!token || !userId) {
      this.setData({ unreadCount: 0 })
      return
    }
    wx.request({
      url: `${app.globalData.userUrl}/chat/conversations`,
      method: 'GET',
      data: { userId },
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        if (res.statusCode === 401 || (res.data && res.data.code === 401)) {
          // 令牌过期/失效，走全局处理清空凭据并提示重新登录
          app._handleUnauthorized()
          this.setData({ unreadCount: 0 })
          return
        }
        if (res.data && res.data.code === 200) {
          const conversations = res.data.data || []
          let totalUnread = 0
          conversations.forEach(conv => {
            const isUser1 = conv.user1_id === userId
            totalUnread += isUser1 ? conv.user1_unread : conv.user2_unread
          })
          this.setData({ unreadCount: totalUnread })
        }
      },
      fail: (err) => {
        console.error('加载未读消息数失败', err)
      }
    })
  },

  goToMessages() {
    if (!guestUtils.requireLogin()) return
    wx.navigateTo({
      url: '/pages/chat/chat-list'
    })
  },

  loadCategories() {
    const app = getApp()
    console.log('请求分类接口:', `${app.globalData.questionUrl}/question/categories`)
    wx.request({
      url: `${app.globalData.questionUrl}/question/categories`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        console.log('分类接口响应:', res)
        if (res.data.code === 200 && res.data.data) {
          const serverCategories = res.data.data
          console.log('分类数据:', serverCategories)
          
          const allCategories = serverCategories.map(cat => ({
            type: cat.icon || cat.name.toLowerCase(),
            name: cat.name,
            questionCount: cat.questionCount || 0,
            completedCount: 0,
            progress: 0,
            difficulty: this.getDifficulty(cat.questionCount),
            icon: categoryIcons[cat.name] || '📚',
            iconUrl: cat.iconUrl || '',
            bgColor: categoryColors[cat.name] || '#999999',
            id: cat.id
          }))
          
          const categories = allCategories.filter(cat => hiddenCategories.indexOf(cat.name) < 0)
          
          console.log('处理后的分类:', categories)
          
          const totalQuestions = categories.reduce((sum, cat) => sum + (cat.questionCount || 0), 0)
          const displayCategories = categories.slice(0, 8)
          
          this.setData({ 
            categories,
            displayCategories,
            totalQuestions
          })
        } else {
          console.error('分类接口返回错误:', res.data)
        }
      },
      fail: (err) => {
        console.error('加载分类失败', err)
      }
    })
  },

  toggleCategories() {
    const { categories, showAllCategories } = this.data
    if (showAllCategories) {
      this.setData({
        showAllCategories: false,
        displayCategories: categories.slice(0, 8)
      })
    } else {
      this.setData({
        showAllCategories: true,
        displayCategories: categories
      })
    }
  },

  getDifficulty(questionCount) {
    if (!questionCount) return '中等'
    if (questionCount < 100) return '简单'
    if (questionCount < 300) return '中等'
    return '困难'
  },

  checkTodayCheckIn() {
    const app = getApp()
    if (!app.globalData.userId) {
      return
    }
    
    wx.request({
      url: `${app.globalData.baseUrl}/checkin/status?userId=${app.globalData.userId}`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            hasCheckedIn: res.data.data.hasCheckedIn,
            streakDays: res.data.data.streakDays
          })
        }
      }
    })
  },

  checkLogin() {
    const app = getApp()
    const token = wx.getStorageSync('token')
    const guestMode = wx.getStorageSync('guestMode')
    const userInfo = wx.getStorageSync('userInfo')
    
    if (guestMode) {
      return
    }
    
    if (userInfo && userInfo.userId) {
      return
    }
    
    if (!token && !app.globalData.token) {
      wx.redirectTo({
        url: '/pages/login/login'
      })
    }
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.setData({
        userInfo: userInfo
      })
    }

    const app = getApp()
    if (!app.globalData.token) {
      return
    }

    userApi.getUserInfo().then(res => {
      if (res && res.data) {
        const data = res.data
        this.setData({
          userInfo: data
        })
        wx.setStorageSync('userInfo', data)
        app.globalData.userInfo = data
      }
    }).catch(err => {
      console.error('加载用户信息失败', err)
    })
  },

  loadStats() {
    const app = getApp()
    if (!app.globalData.token) {
      return
    }

    userApi.getStatistics().then(res => {
      const data = res.data || {}
      const totalCompleted = data.totalQuestionCount || 0
      const correctCount = data.correctCount || 0
      const accuracy = totalCompleted > 0 ? Math.round((correctCount / totalCompleted) * 100) : 0
      const todayCount = data.todayCount || 0
      const progressPercent = Math.min(100, Math.round((todayCount / this.data.todayGoal) * 100))

      const totalQuestions = this.data.totalQuestions || 1
      const categories = this.data.categories.map(cat => {
        const completed = Math.min(cat.questionCount, Math.floor(totalCompleted * (cat.questionCount / totalQuestions)))
        const progress = cat.questionCount > 0 ? Math.round((completed / cat.questionCount) * 100) : 0
        return {
          ...cat,
          completedCount: completed,
          progress: progress
        }
      })

      this.setData({
        todayCount: todayCount,
        wrongCount: data.wrongCount || 0,
        favoriteCount: data.favoriteCount || 0,
        historyCount: totalCompleted,
        accuracy: accuracy,
        progressPercent: progressPercent,
        categories: categories
      })
    }).catch(err => {
      console.error('加载统计数据失败', err)
    })
  },

  checkUnfinished() {
    const unfinished = wx.getStorageSync('unfinishedPractice')
    if (unfinished && unfinished.timestamp) {
      const now = Date.now()
      const diff = now - unfinished.timestamp
      if (diff < 24 * 60 * 60 * 1000) {
        this.setData({
          hasUnfinished: true,
          unfinishedCategory: unfinished.categoryName || '练习',
          unfinishedProgress: Math.round((unfinished.currentIndex / unfinished.totalCount) * 100)
        })
      }
    }
  },

  goToUser() {
    wx.switchTab({
      url: '/pages/user/user'
    })
  },

  goToSearch() {
    wx.navigateTo({
      url: '/pages/search/search'
    })
  },

  goToWrongbook() {
    wx.navigateTo({
      url: '/pages/wrongbook/wrongbook'
    })
  },

  goToFavorite() {
    wx.navigateTo({
      url: '/pages/favorite/favorite'
    })
  },

  goToHistory() {
    wx.navigateTo({
      url: '/pages/history/history'
    })
  },

  goToPractice() {
    this.getTabBar().setData({ showPicker: true })
  },

  continuePractice() {
    const unfinished = wx.getStorageSync('unfinishedPractice')
    if (unfinished) {
      wx.navigateTo({
        url: `/pages/answer/answer?category=${unfinished.category}&index=${unfinished.currentIndex}`
      })
    }
  },

  randomPractice() {
    const app = getApp()
    
    wx.request({
      url: `${app.globalData.questionUrl}/question/random/global?limit=10`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          const questions = res.data.data
          const questionIds = questions.map(q => q.id).join(',')
          
          wx.navigateTo({
            url: `/pages/answer/answer?mode=random&questionIds=${questionIds}`
          })
        } else {
          wx.showToast({
            title: '暂无题目',
            icon: 'none'
          })
        }
      },
      fail: (err) => {
        console.error('获取随机题目失败', err)
        wx.showToast({
          title: '获取题目失败',
          icon: 'none'
        })
      }
    })
  },

  goToQuestion() {
    wx.switchTab({
      url: '/pages/question/question'
    })
  },

  selectCategory(e) {
    const type = e.currentTarget.dataset.type
    const category = this.data.categories.find(function(c) {
      return c.type === type
    })
    if (category) {
      wx.navigateTo({
        url: `/pages/question-list/question-list?categoryId=${category.id}&name=${encodeURIComponent(category.name)}`
      })
    }
  },

  onBannerTap(e) {
    const { type, id } = e.currentTarget.dataset
    switch(type) {
      case 'challenge':
        this.randomPractice()
        break
      case 'rank':
        wx.navigateTo({
          url: '/pages/rank/rank'
        })
        break
      case 'wrongbook':
        wx.navigateTo({
          url: '/pages/wrongbook/wrongbook'
        })
        break
      default:
        break
    }
  },

  handleCheckIn() {
    if (!guestUtils.requireLogin(null, '签到功能需要登录后才能使用')) {
      return
    }
    
    if (this.data.hasCheckedIn) {
      wx.showToast({
        title: '今日已签到',
        icon: 'none'
      })
      return
    }
    
    const app = getApp()
    if (!app.globalData.userId) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }
    
    wx.request({
      url: `${app.globalData.baseUrl}/checkin/do?userId=${app.globalData.userId}`,
      method: 'POST',
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({
            hasCheckedIn: true,
            streakDays: res.data.data.streakDays
          })
          wx.showToast({
            title: `签到成功！连续${res.data.data.streakDays}天`,
            icon: 'success'
          })
        } else {
          wx.showToast({
            title: res.data.message || '签到失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    })
  }
})
