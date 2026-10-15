const { getCategoryIcon } = require('../../utils/categoryLogo.js')
const userApi = require('../../api/user.js')

const guestUtils = require('../../utils/guest.js')


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
  'Java 集合': '#F9A825',
  'Java 并发': '#E76F00',
  'Java 虚拟机': '#D84315',
  'SpringBoot': '#6DB33F',
  'SpringCloud': '#36CFC9',
  'RabbitMQ': '#FF6600',
  'Kafka': '#E25A1C',
  'Elasticsearch': '#00BFB3',
  'AI Agent': '#7B61FF',
  '前端 HTML': '#E34F26',
  '前端 CSS': '#1572B6',
  'ES6': '#F7DF1E',
  '前端手写题': '#42B883',
  '前端工程化': '#0288D1',
  '前端性能优化': '#FF6F61',
  '后端场景题': '#795548',
  '前端场景题': '#795548',
  '智力题': '#FF9800',
  '大厂面经': '#607D8B',
  'DevOps': '#009688',
  '双指针': '#42A5F5',
  '滑动窗口': '#26A69A',
  '二分法': '#7E57C2',
  '排序': '#FFA726',
  '链表': '#EC407A',
  '栈': '#8D6E63',
  '树': '#66BB6A',
  '图': '#5C6BC0',
  '回溯': '#EF5350',
  '贪心': '#FFCA28',
  '动态规划': '#29B6F6',
  '前缀和': '#26C6DA',
  '差分': '#9CCC65',
  '位运算': '#78909C',
  'Trie': '#D4E157',
  '并查集': '#FF7043',
  '字符串': '#AB47BC',
  '数学': '#42B883',
  '大厂算法真题': '#FFD54F',
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
  'AI 大模型': '#8B5CF6',
  'Claude Code': '#D97757',
  '强化学习': '#F97316',
  'AI 应用': '#06B6D4',
  'Transformer': '#DC2626',
  'LangChain': '#16A34A',
  'Prompt 提示词工程': '#DB2777',
  'RAG 检索增强生成': '#2563EB',
  'Python 代码分析': '#3776AB',
  'Python 手写代码': '#7C3AED',
  'IT 运维工程师': '#0EA5E9',
  'DBA 数据库运维': '#7C3AED',
  '网络工程师': '#CA8A04',
  '系统运维': '#64748B',
  'Shell': '#4E9A06',
  '脚本编写': '#A855F7',
  '应用服务器': '#EA580C',
  'CI/CD': '#0D9488',
  'IaC': '#4F46E5',
  '云服务': '#38BDF8',
  'Windows 系统': '#0078D4',
  '系统管理': '#475569',
  '系统监控': '#E11D48',
  '游戏服务端开发': '#EA580C',
  'Oracle 数据库': '#C74634',
  '数据备份恢复': '#0288D1',
  '数据库高可用': '#D97706',
  '数据库体系结构': '#8B5CF6',
  '数据库性能优化': '#DC2626',
  'SQL': '#388E3C',
  'SQL 电商场景': '#F57C00',
  'SQL 网站场景': '#1976D2',
  'SQL 基础查询': '#F9A825',
  'SQL 进阶查询': '#E65100',
  '网络安全': '#1E88E5',
  '安全开发': '#43A047',
  '渗透测试': '#E53935',
  'C# 基础': '#5C2D91',
  'C# 集合': '#0288D1',
  'C# 面向对象': '#00695C',
  'C# 并发库': '#EF6C00',
  'C# 框架': '#303F9F',
  'C# 底层原理': '#455A64',
  'C# WPF': '#33691E',
  'C# .NET': '#512BD4',
  'PHP 基础': '#4F5B93',
  'PHP 面向对象': '#2E7D32',
  'PHP 框架': '#FF2D20',
  'PHP 应用场景': '#01579B',
  '系统故障排查': '#D32F2F',
  '网络配置': '#00897B',
  '网络协议': '#3949AB',
  '网络故障排查': '#D81B60',
  'AI 零代码应用生成平台项目': '#7E57C2',
  'AI 超级智能体项目': '#26A69A',
  '智能协同云图库项目': '#42A5F5',
  '面试刷题平台项目': '#FFB300',
  'AI 答题平台项目': '#8D6E63',
  '代码生成器平台项目': '#0288D1',
  '伙伴匹配项目': '#EF6C00',
  '智能 BI 项目': '#2E7D32',
  'API 开放平台项目': '#E53935',
  '聚合搜索项目': '#5C6BC0',
  'OJ 判题项目': '#00897B',
  '手写 RPC 框架项目': '#6D4C41',
  '用户中心项目': '#1565C0',
  'GitHub 文档翻译工具项目': '#37474F',
  'AI 爆款文章创作器项目': '#D81B60',
  'AI 万能视频下载总结器项目': '#6A1B9A',
  'AI 大模型评测平台项目': '#455A64',
  'AI 热点监控工具项目': '#F4511E',
  'AI 网关平台项目': '#00695C',
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
  'C++': '#00599C',
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
  'Solidity': '#5C6BC0',
  'Web3 开发': '#3B82F6',
  '区块链开发': '#00B8D4',
  '智能合约开发': '#7B61FF',
  '测试基础理论': '#EC4899',
  '自动化测试': '#10B981',
  '接口测试': '#22C55E',
  'Web 测试': '#3B82F6',
  '移动端测试': '#6366F1',
  '测试工具': '#8B5CF6',
  '性能测试': '#F59E0B',
  '安全测试': '#1E40AF',
  '测试场景': '#64748B',
  'C#基础': '#9B4F96',
  'C#集合': '#68217A',
  'C#面向对象': '#7B3F96',
  'C#并发库': '#5C2D91',
  'C#框架': '#4A235A',
  'C#底层原理': '#3D1F5C',
  'C#WPF': '#6B3FA0',
  'C#.NET': '#512BD4',
  'Android 基础': '#3DDC84',
  'Android 进阶': '#38B26D',
  'Android UI': '#34A853',
  'Android 库': '#2E9E5B',
  'Android 性能优化': '#43CC71',
  'Android 原理': '#27994F',
  'Android 数据存储': '#35B568',
  'Android 网络通信': '#2FA85C',
  'Swift': '#F05138',
  'Kotlin': '#7F52FF',
  'Flutter': '#02569B',
  'Dart': '#0175C2',
  'React Native': '#61DAFB',
  'H5 网站开发': '#E34F26',
  '鸿蒙开发': '#0A59F7',
  'Git 基础': '#F05032',
  'Git 进阶': '#E04425',
  'Git 操作': '#D63A1E',
  'Git 概念': '#C73518',
  'Git 协作应用': '#B83012'
}

Page({
  data: {
    userInfo: {},
    dailyQuestion: null,
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
    this.loadDailyQuestion()
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
    console.log('请求分类接口:', `${app.globalData.questionUrl}/question/category/groups`)
    wx.request({
      url: `${app.globalData.questionUrl}/question/category/groups`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        console.log('分类接口响应:', res)
        const payload = res.data && res.data.data
        if (res.data.code === 200 && payload) {
          const categories = (payload.categories || [])
            .filter(cat => !(cat.groupKeys || []).some(k => k === 'hidden'))
            .map(cat => ({
              type: cat.icon || cat.name.toLowerCase(),
              name: cat.name,
              questionCount: cat.questionCount || 0,
              completedCount: 0,
              progress: 0,
              difficulty: this.getDifficulty(cat.questionCount),
              icon: getCategoryIcon(cat),
              iconUrl: cat.iconUrl || '',
              bgColor: categoryColors[cat.name] || '#999999',
              id: cat.id
            }))
          
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
      header: { 'Authorization': `Bearer ${app.globalData.token || wx.getStorageSync('token')}` },
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

  goToAgent() {
    wx.navigateTo({
      url: '/pages/agent/agent'
    })
  },

  loadDailyQuestion() {
    const app = getApp()
    const token = app.globalData.token
    wx.request({
      url: `${app.globalData.questionUrl}/question/daily/today`,
      method: 'GET',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        const d = (res.data && res.data.data) || null
        if (d && d.questionId) {
          this.setData({ dailyQuestion: d })
        } else {
          this.setData({ dailyQuestion: null })
        }
      }
    })
  },

  goDailyQuestion() {
    const qid = this.data.dailyQuestion && this.data.dailyQuestion.questionId
    if (qid) {
      wx.navigateTo({ url: `/pages/question-detail/question-detail?id=${qid}` })
    }
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
      header: { 'Authorization': `Bearer ${app.globalData.token || wx.getStorageSync('token')}` },
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
