const app = getApp()
const guestUtils = require('../../utils/guest.js')

let msgSeq = 1

Page({
  data: {
    messages: [],
    input: '',
    sessionId: null,
    sending: false,
    scrollToView: '',
    historyShow: false,
    sessions: []
  },

  lastQuestion: '',

  onLoad(options) {
    if (guestUtils.checkGuest()) {
      wx.showModal({
        title: '提示',
        content: 'AI 助手需要登录后才能使用',
        confirmText: '去登录',
        cancelText: '返回',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/login/login' })
          } else {
            wx.navigateBack({
              fail: () => wx.switchTab({ url: '/pages/index/index' })
            })
          }
        }
      })
      return
    }
    // 用户头像（未设置头像时用默认图）
    const info = wx.getStorageSync('userInfo') || {}
    this.setData({ userAvatar: info.avatarUrl || '/images/default-avatar.png' })
    // 悬浮球带入的题目问题：开新会话自动提问（不混入最近会话上下文）
    const prefill = options && options.prefill ? decodeURIComponent(options.prefill) : ''
    if (prefill) {
      this.setData({ sessionId: null, input: prefill })
      wx.nextTick(() => this.sendMessage())
      return
    }
    this.loadLatestSession()
  },

  /** 恢复最近一次会话，没有则显示欢迎语 */
  loadLatestSession() {
    wx.request({
      url: `${app.globalData.agentUrl}/agent/sessions`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200 && res.data.data && res.data.data.length > 0) {
          const session = res.data.data[0]
          this.setData({ sessionId: session.id })
          this.loadMessages(session.id)
        }
      }
    })
  },

  /** 打开聊天记录弹层并拉取全部历史会话 */
  openHistory() {
    wx.request({
      url: `${app.globalData.agentUrl}/agent/sessions`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          const sessions = (res.data.data || []).map(s => ({
            id: s.id,
            title: s.title,
            timeText: this.fmtTime(s.updateTime || s.createTime)
          }))
          this.setData({ historyShow: true, sessions })
        } else if (res.data.code === 401) {
          app._handleUnauthorized()
        }
      }
    })
  },

  closeHistory() {
    this.setData({ historyShow: false })
  },

  noop() { /* 阻止冒泡关闭弹层 */ },

  /** 切换到选中的历史会话 */
  onSessionTap(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ historyShow: false, sessionId: id })
    this.loadMessages(id)
  },

  /** 删除历史会话（连带消息与 AI 记忆）；删的是当前会话则回到欢迎态 */
  onDeleteSession(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '删除会话',
      content: '删除后聊天记录将无法恢复，确定删除吗？',
      confirmColor: '#EF4444',
      success: (res) => {
        if (!res.confirm) return
        wx.request({
          url: `${app.globalData.agentUrl}/agent/session/${id}`,
          method: 'DELETE',
          header: {
            'Authorization': `Bearer ${app.globalData.token}`,
            'X-User-Id': app.globalData.userId
          },
          success: (r) => {
            if (r.data.code === 200) {
              const sessions = this.data.sessions.filter(s => s.id !== id)
              const patch = { sessions }
              if (this.data.sessionId === id) {
                patch.sessionId = null
                patch.messages = []
              }
              this.setData(patch)
              wx.showToast({ title: '已删除', icon: 'success' })
            } else {
              wx.showToast({ title: r.data.msg || '删除失败', icon: 'none' })
            }
          }
        })
      }
    })
  },

  /** "2026-10-17T09:46:54" → "10-17 09:46" */
  fmtTime(iso) {
    if (!iso) return ''
    const s = String(iso).replace('T', ' ')
    return s.length >= 16 ? s.slice(5, 16) : s
  },

  loadMessages(sessionId) {
    wx.request({
      url: `${app.globalData.agentUrl}/agent/messages`,
      method: 'GET',
      data: { sessionId },
      header: {
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      success: (res) => {
        if (res.data.code === 200) {
          const list = (res.data.data || []).map(m => this.toViewMessage(m))
          this.setData({ messages: list })
          this.scrollBottom()
        } else if (res.data.code === 401) {
          app._handleUnauthorized()
        }
      }
    })
  },

  /** 后端消息结构 → 页面渲染结构 */
  toViewMessage(m) {
    let sources = []
    let actions = []
    try { sources = m.sources ? JSON.parse(m.sources) : [] } catch (e) { /* 忽略脏数据 */ }
    try { actions = m.actions ? JSON.parse(m.actions) : [] } catch (e) { /* 忽略脏数据 */ }
    return {
      id: m.id || ('local-' + msgSeq++),
      role: m.role,
      text: m.content,
      sources,
      actions,
      loading: false,
      failed: false
    }
  },

  onInput(e) {
    this.setData({ input: e.detail.value })
  },

  quickAsk(e) {
    const q = e.currentTarget.dataset.q
    if (this.data.sending) return
    this.setData({ input: q })
    this.sendMessage()
  },

  sendMessage() {
    const question = (this.data.input || '').trim()
    if (!question || this.data.sending) return

    this.lastQuestion = question
    const userMsg = { id: 'u-' + msgSeq++, role: 'user', text: question }
    const aiMsg = { id: 'a-' + msgSeq++, role: 'assistant', text: '', sources: [], actions: [], loading: true }

    this.setData({
      input: '',
      sending: true,
      messages: [...this.data.messages, userMsg, aiMsg]
    })
    this.scrollBottom()

    wx.request({
      url: `${app.globalData.agentUrl}/agent/chat`,
      method: 'POST',
      header: {
        'content-type': 'application/json',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      data: {
        sessionId: this.data.sessionId,
        message: question
      },
      timeout: 70000,
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          this.setData({ sessionId: data.sessionId })
          // 打字机效果渲染回答
          this.typeOut(data.messageId || 'a', data.text || '', data.sources || [], data.actions || [])
        } else if (res.data.code === 401) {
          this.markFailed('登录已过期，请重新登录')
          app._handleUnauthorized()
        } else {
          this.markFailed(res.data.msg || '助手开小差了，请稍后再试')
        }
      },
      fail: () => {
        this.markFailed('网络错误，请检查网络后重试')
      }
    })
  },

  retrySend() {
    if (this.data.sending || !this.lastQuestion) return
    // 移除失败气泡后重发
    const messages = this.data.messages.filter(m => !m.failed)
    this.setData({ messages })
    this.setData({ input: this.lastQuestion })
    this.sendMessage()
  },

  /** 逐字渲染，模拟流式输出观感 */
  typeOut(messageId, fullText, sources, actions) {
    const idx = this.data.messages.length - 1
    let pos = 0
    const tick = () => {
      pos = Math.min(pos + 2, fullText.length)
      const key = `messages[${idx}].text`
      this.setData({ [key]: fullText.slice(0, pos) })
      if (pos < fullText.length) {
        this.typingTimer = setTimeout(tick, 30)
      } else {
        this.setData({
          [`messages[${idx}].loading`]: false,
          [`messages[${idx}].sources`]: sources || [],
          [`messages[${idx}].actions`]: actions || [],
          sending: false
        })
        this.scrollBottom()
      }
    }
    tick()
    this.scrollBottom()
  },

  markFailed(tip) {
    const idx = this.data.messages.length - 1
    if (idx < 0) return
    this.setData({
      [`messages[${idx}].text`]: tip,
      [`messages[${idx}].loading`]: false,
      [`messages[${idx}].failed`]: true,
      sending: false
    })
    this.scrollBottom()
  },

  /** 动作卡片跳转 */
  handleAction(e) {
    const a = e.currentTarget.dataset.action
    if (!a || !a.type) return
    switch (a.type) {
      case 'PRACTICE_CATEGORY':
        if (a.categoryId) {
          wx.navigateTo({
            url: `/pages/question-list/question-list?categoryId=${a.categoryId}&categoryName=${encodeURIComponent(a.label || '分类练习')}`
          })
        }
        break
      case 'VIEW_QUESTION':
        if (a.questionId) {
          wx.navigateTo({
            url: `/pages/question-detail/question-detail?id=${a.questionId}`
          })
        }
        break
      case 'VIEW_WRONGBOOK':
        wx.navigateTo({ url: '/pages/wrongbook/wrongbook' })
        break
      case 'VIEW_STATISTICS':
        wx.navigateTo({ url: '/pages/statistics/statistics' })
        break
      default:
        wx.showToast({ title: '暂不支持的动作', icon: 'none' })
    }
  },

  startNewSession() {
    if (this.typingTimer) clearTimeout(this.typingTimer)
    this.setData({
      sessionId: null,
      messages: [],
      sending: false,
      input: ''
    })
  },

  scrollBottom() {
    this.setData({ scrollToView: '' })
    this.setData({ scrollToView: 'msg-bottom' })
  },

  onUnload() {
    if (this.typingTimer) clearTimeout(this.typingTimer)
  }
})
