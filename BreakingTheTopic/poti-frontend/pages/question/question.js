const { getCategoryIcon } = require('../../utils/categoryLogo.js')

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
    tagList: [{ key: 'all', name: '全部' }],
    tagNameMap: { all: '全部题库' },
    displayTagList: [{ key: 'all', name: '全部' }],
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
      url: `${app.globalData.questionUrl}/question/category/groups`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: function(res) {
        const payload = res.data && res.data.data
        if (res.data.code === 200 && payload) {
          // 分组标签：全部固定在最前，hidden 为内部标记不在顶部展示
          const tagNameMap = { all: '全部题库' }
          const tagList = [{ key: 'all', name: '全部' }]
          const groups = payload.groups || []
          for (let i = 0; i < groups.length; i++) {
            const g = groups[i]
            if (g.key === 'hidden') continue
            tagNameMap[g.key] = g.name
            tagList.push({ key: g.key, name: g.name })
          }

          // 分类：挂 hidden 标签的不在宫格展示
          const categories = (payload.categories || [])
            .filter(function(cat) {
              return !(cat.groupKeys || []).some(function(k) { return k === 'hidden' })
            })
            .map(function(cat) {
              return {
                type: cat.icon || cat.name.toLowerCase(),
                name: cat.name,
                icon: getCategoryIcon(cat),
                bgColor: categoryColors[cat.name] || '#999999',
                questionCount: cat.questionCount || 0,
                id: cat.id,
                tags: cat.groupKeys || []
              }
            })

          that.setData({
            tagList: tagList,
            tagNameMap: tagNameMap,
            displayTagList: tagList.slice(0, 5),
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
    const { categories, tagNameMap } = this.data
    let filtered = []

    if (tag === 'all') {
      filtered = categories
    } else {
      filtered = categories.filter(function(c) {
        return c.tags && c.tags.indexOf(tag) >= 0
      })
    }

    this.setData({
      activeTag: tag,
      activeTagName: tagNameMap[tag] || tag,
      filteredCategories: filtered
    })
  },

  goToSearch() {
    wx.navigateTo({
      url: '/pages/search/search'
    })
  },

  goSpecial() {
    wx.navigateTo({
      url: '/pages/special/special'
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
