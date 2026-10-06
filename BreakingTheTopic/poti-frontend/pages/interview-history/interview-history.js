const app = getApp()

Page({
  data: {
    list: [],
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    loading: false,
    initialized: false
  },

  onLoad() {
    this.setData({ initialized: false })
  },

  onShow() {
    if (!this.data.initialized || !this.data.loading) {
      this.refreshList()
    }
  },

  refreshList() {
    this.setData({
      list: [],
      pageNum: 1,
      hasMore: true,
      loading: true,
      initialized: true
    })
    this.loadHistoryInternal()
  },

  loadHistory() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ loading: true })
    this.loadHistoryInternal()
  },

  loadHistoryInternal() {
    wx.request({
      url: `${app.globalData.interviewUrl}/interview/history`,
      method: 'GET',
      data: {
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          const newList = data.list || []
          
          const uniqueMap = new Map()
          const existingIds = new Set(this.data.list.map(item => item.id))
          
          this.data.list.forEach(item => uniqueMap.set(item.id, item))
          newList.forEach(item => {
            if (!existingIds.has(item.id)) {
              uniqueMap.set(item.id, item)
            }
          })
          
          const finalList = Array.from(uniqueMap.values()).sort((a, b) => 
            new Date(b.createTime) - new Date(a.createTime)
          )
          
          this.setData({
            list: finalList,
            hasMore: newList.length >= this.data.pageSize
          })
        }
      },
      complete: () => {
        this.setData({ loading: false })
      }
    })
  },

  onReachBottom() {
    if (this.data.hasMore) {
      this.setData({ pageNum: this.data.pageNum + 1 })
      this.loadHistory()
    }
  },

  viewDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/interview-result/interview-result?interviewId=${id}`
    })
  },

  deleteItem(e) {
    const id = e.currentTarget.dataset.id
    const index = e.currentTarget.dataset.index
    
    wx.showModal({
      title: '提示',
      content: '确定删除这条记录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.interviewUrl}/interview/${id}`,
            method: 'DELETE',
            header: {
              'Authorization': `Bearer ${app.globalData.token}`,
              'X-User-Id': app.globalData.userId
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已删除', icon: 'success' })
                const list = this.data.list
                list.splice(index, 1)
                this.setData({ list })
              } else {
                wx.showToast({ title: '删除失败', icon: 'none' })
              }
            }
          })
        }
      }
    })
  },

  clearAll() {
    wx.showModal({
      title: '提示',
      content: '确定清空所有面试记录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${app.globalData.interviewUrl}/interview/clear`,
            method: 'DELETE',
            header: {
              'Authorization': `Bearer ${app.globalData.token}`,
              'X-User-Id': app.globalData.userId
            },
            success: (res) => {
              if (res.data.code === 200) {
                wx.showToast({ title: '已清空', icon: 'success' })
                this.setData({ list: [] })
              } else {
                wx.showToast({ title: '清空失败', icon: 'none' })
              }
            }
          })
        }
      }
    })
  },

  formatTime(seconds) {
    if (!seconds) return '0秒'
    const min = Math.floor(seconds / 60)
    const sec = seconds % 60
    return min > 0 ? `${min}分${sec}秒` : `${sec}秒`
  },

  formatDate(dateStr) {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now - date
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    if (days === 0) return '今天'
    if (days === 1) return '昨天'
    if (days < 7) return `${days}天前`
    
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}月${day}日`
  }
})
