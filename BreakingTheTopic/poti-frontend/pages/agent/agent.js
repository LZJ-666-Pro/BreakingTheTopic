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

    this.streamChat(question)
  },

  /** 真流式：enableChunked 接收 SSE，逐段渲染；异常时降级同步接口 */
  streamChat(question) {
    this._streamBuf = ''
    this._pendingBytes = []
    this._pendingDelta = ''
    this._gotMeta = false
    this._streamQuestion = question

    const doFallback = () => {
      if (this._gotMeta) return
      this.fallbackSync(question)
    }

    const task = wx.request({
      url: `${app.globalData.agentUrl}/agent/chat/stream`,
      method: 'POST',
      enableChunked: true,
      header: {
        'content-type': 'application/json',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId
      },
      data: {
        sessionId: this.data.sessionId,
        message: question
      },
      timeout: 130000,
      success: (res) => {
        if (res.statusCode === 401) {
          this.markFailed('登录已过期，请重新登录')
          app._handleUnauthorized()
          return
        }
        if (res.statusCode !== 200) {
          // 业务拒绝（限额/会话不存在等）后端返回 R 结构
          const msg = (res.data && (res.data.msg || res.data.message)) || '助手开小差了，请稍后再试'
          this.markFailed(msg)
          return
        }
        if (this._gotMeta) return
        // 部分环境不支持分块回调：整个响应体在 success 一次性返回（完整 SSE 文本），补解析
        if (typeof res.data === 'string' && res.data.indexOf('data:') >= 0) {
          this.consumeSseText(res.data)
        }
        if (!this._gotMeta) {
          // 没有任何有效流数据，降级同步接口
          doFallback()
        }
      },
      fail: () => {
        if (!this._gotMeta) this.markFailed('网络错误，请检查网络后重试')
      }
    })

    task.onChunkReceived((res) => {
      const text = this.decodeChunk(res.data)
      if (!text) return
      this.consumeSseText(text)
    })
  },

  /** 解析 SSE 文本：按空行分帧，取 data: 行 JSON 信封 */
  consumeSseText(text) {
    this._streamBuf = (this._streamBuf || '') + text
    const frames = this._streamBuf.split('\n\n')
    this._streamBuf = frames.pop() || ''
    for (const frame of frames) {
      const dataLine = frame.split('\n').find(l => l.indexOf('data:') === 0)
      if (!dataLine) continue
      let payload
      try {
        payload = JSON.parse(dataLine.slice(5).trim())
      } catch (e) {
        continue
      }
      this.handleStreamEvent(payload)
    }
  },

  handleStreamEvent(payload) {
    if (!payload || !payload.t) return
    const idx = this.data.messages.length - 1
    if (idx < 0 || !this.data.messages[idx] || !this.data.messages[idx].loading) {
      return // 用户已切换会话/气泡已被清理，丢弃迟到事件
    }
    if (payload.t === 'delta') {
      this.appendDelta(payload.d || '')
    } else if (payload.t === 'meta') {
      this._gotMeta = true
      this.flushDelta()
      if (payload.sessionId) this.setData({ sessionId: payload.sessionId })
      this.setData({
        [`messages[${idx}].loading`]: false,
        [`messages[${idx}].sources`]: payload.sources || [],
        [`messages[${idx}].actions`]: payload.actions || [],
        sending: false
      })
      this.scrollBottom()
    } else if (payload.t === 'error') {
      this._gotMeta = true
      this.markFailed(payload.m || '助手开小差了，请稍后再试')
    }
  },

  /** 增量 UTF-8 解码：处理多字节字符与 SSE 帧跨 chunk 边界 */
  decodeChunk(arrayBuffer) {
    const incoming = Array.from(new Uint8Array(arrayBuffer))
    const all = (this._pendingBytes || []).concat(incoming)
    this._pendingBytes = []
    let out = ''
    let i = 0
    while (i < all.length) {
      const b = all[i]
      let charLen = 1
      if (b >= 0xF0) charLen = 4
      else if (b >= 0xE0) charLen = 3
      else if (b >= 0xC0) charLen = 2
      if (i + charLen > all.length) {
        this._pendingBytes = all.slice(i)
        break
      }
      if (charLen === 1) {
        out += String.fromCharCode(b)
      } else {
        let code = 0
        if (charLen === 2) {
          code = ((b & 0x1F) << 6) | (all[i + 1] & 0x3F)
        } else if (charLen === 3) {
          code = ((b & 0x0F) << 12) | ((all[i + 1] & 0x3F) << 6) | (all[i + 2] & 0x3F)
        } else {
          code = ((b & 0x07) << 18) | ((all[i + 1] & 0x3F) << 12) | ((all[i + 2] & 0x3F) << 6) | (all[i + 3] & 0x3F)
        }
        out += String.fromCodePoint(code)
        i += charLen
        continue
      }
      i++
    }
    return out
  },

  /** delta 节流渲染：50ms 合并一次 setData，避免逐 token 刷屏 */
  appendDelta(text) {
    this._pendingDelta = (this._pendingDelta || '') + text
    if (this._deltaTimer) return
    this._deltaTimer = setTimeout(() => this.flushDelta(), 50)
  },

  flushDelta() {
    if (this._deltaTimer) {
      clearTimeout(this._deltaTimer)
      this._deltaTimer = null
    }
    const chunk = this._pendingDelta || ''
    this._pendingDelta = ''
    if (!chunk) return
    const idx = this.data.messages.length - 1
    if (idx < 0) return
    const cur = this.data.messages[idx].text || ''
    this.setData({ [`messages[${idx}].text`]: cur + chunk })
    this.scrollBottom()
  },

  /** 降级：同步整体返回 + 打字机渲染（流式链路异常时使用） */
  fallbackSync(question) {
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
