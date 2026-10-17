// AI 导师悬浮球：答题场景免跳首页快速提问，可 × 收起（重进小程序自动恢复）
Component({
  properties: {
    // 当前题目上下文（题干摘要），跳转 AI 导师页时自动预填提问
    questionTitle: { type: String, value: '' }
  },

  data: {
    visible: true
  },

  lifetimes: {
    attached() {
      if (wx.getStorageSync('agentFabClosed')) {
        this.setData({ visible: false })
      }
    }
  },

  methods: {
    onTap() {
      const t = (this.data.questionTitle || '').trim().slice(0, 40)
      const url = t
        ? `/pages/agent/agent?prefill=${encodeURIComponent(`这道题怎么理解：「${t}」`)}`
        : '/pages/agent/agent'
      wx.navigateTo({ url })
    },

    onClose() {
      wx.setStorageSync('agentFabClosed', 1)
      this.setData({ visible: false })
      wx.showToast({ title: '已收起，重进小程序恢复', icon: 'none' })
    }
  }
})
