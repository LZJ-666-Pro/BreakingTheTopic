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

const tagNames = {
  all: '全部题库',
  hot: '热门题库',
  backend: '后端',
  frontend: '前端',
  ai: '人工智能',
  bigdata: '大数据',
  test: '测试',
  basic: '基础学科',
  interview: '面试',
  mobile: '移动开发',
  game: '游戏开发',
  ops: '运维',
  security: '安全',
  blockchain: '区块链',
  java: 'Java',
  cpp: 'C/C++',
  go: 'Go',
  python: 'Python',
  csharp: 'C#.NET',
  php: 'PHP'
}

const tagList = [
  { key: 'all', name: '全部' },
  { key: 'hot', name: '热门' },
  { key: 'backend', name: '后端' },
  { key: 'frontend', name: '前端' },
  { key: 'ai', name: '人工智能' },
  { key: 'bigdata', name: '大数据' },
  { key: 'test', name: '测试' },
  { key: 'basic', name: '基础学科' },
  { key: 'interview', name: '面试' },
  { key: 'mobile', name: '移动开发' },
  { key: 'game', name: '游戏开发' },
  { key: 'ops', name: '运维' },
  { key: 'security', name: '安全' },
  { key: 'blockchain', name: '区块链' },
  { key: 'java', name: 'Java' },
  { key: 'cpp', name: 'C/C++' },
  { key: 'go', name: 'Go' },
  { key: 'python', name: 'Python' },
  { key: 'csharp', name: 'C#.NET' },
  { key: 'php', name: 'PHP' }
]

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

Page({
  data: {
    tagList: tagList,
    displayTagList: tagList.slice(0, 5),
    activeTag: 'all',
    activeTagName: '全部题库',
    showAllTags: false,
    categories: [],
    displayCategories: [],
    showAllCategories: false,
    firstRowLast: [],
    secondRow: [],
    filteredCategories: []
  },

  onLoad() {
    this.loadCategories()
  },
  
  toggleTags() {
    this.setData({
      showAllTags: !this.data.showAllTags
    })
  },

  loadCategories() {
    const app = getApp()
    const that = this
    wx.request({
      url: `${app.globalData.questionUrl}/question/categories`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: function(res) {
        console.log('分类接口响应:', res.data)
        if (res.data.code === 200 && res.data.data) {
          const serverCategories = res.data.data
          console.log('原始数据:', serverCategories.slice(0, 3))
          
          const categories = serverCategories.map(function(cat) {
            return {
              type: cat.icon || cat.name.toLowerCase(),
              name: cat.name,
              icon: categoryIcons[cat.name] || '📚',
              bgColor: categoryColors[cat.name] || '#999999',
              questionCount: cat.questionCount || 0,
              id: cat.id,
              tags: categoryTags[cat.name] || []
            }
          })
          
          console.log('处理后数据:', categories.slice(0, 3))
          
          that.setData({ 
            categories: categories,
            filteredCategories: categories
          })
          that.initCategoryRows()
        }
      },
      fail: function(err) {
        console.error('加载分类失败', err)
      }
    })
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 })
    }
  },

  initCategoryRows() {
    const categories = this.data.categories
    const firstRow = categories.slice(0, 1)
    const secondRow = categories.slice(1, 4)
    
    this.setData({
      firstRowLast: firstRow,
      secondRow: secondRow
    })
  },

  toggleCategories() {
    const that = this
    const { categories, showAllCategories, activeTag } = this.data
    if (showAllCategories) {
      that.setData({
        showAllCategories: false,
        displayCategories: categories.slice(0, 8),
        filteredCategories: categories.slice(0, 8)
      })
    } else {
      that.setData({
        showAllCategories: true,
        displayCategories: categories,
        filteredCategories: categories
      })
    }
  },

  selectTag(e) {
    const tag = e.currentTarget.dataset.tag
    const { categories } = this.data
    let filtered = []
    
    if (tag === 'all') {
      filtered = categories.filter(function(c) {
        return !c.tags || c.tags.indexOf('hidden') < 0
      })
    } else if (tag === 'hot') {
      filtered = categories.filter(function(c) {
        return c.tags && c.tags.indexOf('hot') >= 0 && c.tags.indexOf('hidden') < 0
      })
    } else if (tag === 'cpp' || tag === 'go' || tag === 'php' || tag === 'python' || tag === 'csharp') {
      filtered = categories.filter(function(c) {
        return c.tags && c.tags.indexOf(tag) >= 0
      })
    } else if (tagNames[tag]) {
      filtered = categories.filter(function(c) {
        return c.tags && c.tags.indexOf(tag) >= 0 && c.tags.indexOf('hidden') < 0
      })
    } else {
      filtered = categories.filter(function(c) {
        return c.type === tag
      })
    }
    
    this.setData({
      activeTag: tag,
      activeTagName: tagNames[tag] || tag,
      filteredCategories: filtered
    })
  },

  goToSearch() {
    wx.navigateTo({
      url: '/pages/search/search'
    })
  },

  goToCategory(e) {
    const type = e.currentTarget.dataset.type
    const category = this.data.categories.find(function(c) {
      return c.type === type
    })
    if (category) {
      wx.navigateTo({
        url: `/pages/question-list/question-list?categoryId=${category.id}&name=${encodeURIComponent(category.name)}`
      })
    }
  }
})
