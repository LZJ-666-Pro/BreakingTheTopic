const guestUtils = require('../../utils/guest.js')

Page({
  data: {
    vip: false,
    expireTimeText: '',
    price: null,
    months: 1,
    plans: [],
    selectedPlan: 3,
    loading: true,
    purchasing: false,
    // 权益列表：from/to 为右侧摘要，before/after 为展开面板的开通前后对比
    expandedBenefit: '',
    benefits: [
      {
        key: 'ai', icon: 'chat-o', title: 'AI freestyle 提问次数',
        from: '20次/天', to: '100次/天',
        before: '每天 20 次 freestyle 提问额度，用完只能等次日重置',
        after: '每天 100 次提问额度，题目讲解、代码点评、追问答疑都够用'
      },
      {
        key: 'analysis', icon: 'records', title: '官方题解无限查看',
        from: '5篇/天', to: '无限查看',
        before: '每天限看 5 篇官方题解，超出部分需等待次日',
        after: '全部官方题解不限次数查看，错题复盘随时随地'
      },
      {
        key: 'competition', icon: 'medal', title: '竞赛优先报名',
        from: '同步开抢', to: '提前24h报名',
        before: '与所有用户同一时间开放报名，热门竞赛手慢无',
        after: '开赛前 24 小时优先开放报名通道，热门赛事稳占名额'
      },
      {
        key: 'ad', icon: 'shield-o', title: '去广告纯净刷题',
        from: '穿插广告', to: '纯净无广告',
        before: '刷题间隙穿插推广横幅，打扰答题节奏',
        after: '全程屏蔽广告，界面纯净、专注刷题本身'
      }
    ]
  },

  _isDestroyed: false,

  onLoad() {
    this._isDestroyed = false
  },

  onShow() {
    this._isDestroyed = false
    this.loadStatus()
  },

  onHide() {
    this._isDestroyed = true
  },

  onUnload() {
    this._isDestroyed = true
  },

  request(options) {
    const app = getApp()
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${app.globalData.userUrl}${options.url}`,
        method: options.method || 'GET',
        data: options.data || {},
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${app.globalData.token}`,
          'X-User-Id': app.globalData.userId || 1
        },
        success: (res) => resolve(res.data),
        fail: (err) => reject(err)
      })
    })
  },

  isLoggedIn() {
    return !!wx.getStorageSync('token') && !guestUtils.checkGuest()
  },

  // 后端不可用/请求失败时的兜底三档（与后端默认价一致）
  FALLBACK_PLANS: [
    { months: 1, name: '月卡', price: 1.99, unit: '/月' },
    { months: 3, name: '季卡', price: 4.99, unit: '/季' },
    { months: 12, name: '年卡', price: 9.99, unit: '/年' }
  ],

  loadStatus() {
    if (!this.isLoggedIn()) {
      // 未登录也展示三档套餐，点开通时再引导登录
      require('../../utils/ad.js').cacheVipStatus(false)
      this.setData({ loading: false, vip: false, price: null, plans: this.decoratePlans(this.FALLBACK_PLANS) })
      return
    }
    this.setData({ loading: true })
    this.request({ url: '/vip/status' }).then(res => {
      if (this._isDestroyed) return
      if (res && res.code === 200 && res.data) {
        const d = res.data
        // 缓存会员状态供免广告等跨页权益判断使用（utils/ad.js）
        require('../../utils/ad.js').cacheVipStatus(d.vip === true)
        const raw = Array.isArray(d.plans) && d.plans.length > 0
          ? d.plans
          : [{ months: 1, name: '月卡', price: d.price, unit: '/月', badge: '' }]
        const plans = this.decoratePlans(raw)
        this.setData({
          vip: !!d.vip,
          expireTimeText: this.formatDate(d.expireTime),
          price: d.price === null || d.price === undefined ? null : d.price,
          months: d.months || 1,
          plans,
          selectedPlan: plans.some(p => p.months === this.data.selectedPlan) ? this.data.selectedPlan : plans[1].months,
          loading: false
        })
      } else {
        this.setData({ loading: false, plans: this.decoratePlans(this.FALLBACK_PLANS) })
        wx.showToast({ title: (res && res.msg) || '会员状态加载失败', icon: 'none' })
      }
    }).catch(err => {
      console.error('加载会员状态失败', err)
      if (!this._isDestroyed) this.setData({ loading: false, plans: this.decoratePlans(this.FALLBACK_PLANS) })
    })
  },

  // 按原型装饰：季卡顶部「推荐」；三档均显示每月均价
  decoratePlans(list) {
    return (list || []).map(p => {
      const item = Object.assign({}, p)
      const price = Number(p.price)
      if (!isNaN(price)) {
        const per = p.months === 12 ? price / 12 : p.months === 3 ? price / 3 : price
        item.sub = '¥' + per.toFixed(1) + '/月'
      }
      if (p.months === 3) item.topBadge = '推荐'
      return item
    })
  },

  // 选择套餐
  selectPlan(e) {
    const months = Number(e.currentTarget.dataset.months)
    if (months) this.setData({ selectedPlan: months, months })
  },

  // 展开或收起某个权益的开通前后对比（手风琴：同项再点收起，换项自动切换）
  toggleBenefit(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ expandedBenefit: this.data.expandedBenefit === key ? '' : key })
  },

  handlePurchase() {
    if (this.data.purchasing) return
    if (!this.isLoggedIn()) {
      wx.showModal({
        title: '提示',
        content: '开通会员需要先登录',
        confirmText: '去登录',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/login/login' })
          }
        }
      })
      return
    }
    const plan = this.data.plans.find(p => p.months === this.data.selectedPlan) || this.data.plans[0]
    if (!plan || plan.price === null || plan.price === undefined) {
      wx.showToast({ title: '价格信息加载中，请稍后', icon: 'none' })
      return
    }
    wx.showModal({
      title: this.data.vip ? '续费会员' : '开通会员',
      content: `确认支付 ${plan.name} ¥${plan.price}${plan.unit || ''}？`,
      confirmText: '确认支付',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) this.doPay()
      }
    })
  },

  // 统一支付入口：微信支付就绪走真实拉起收银台，未配置商户参数时后端自动降级模拟支付
  doPay() {
    this.setData({ purchasing: true })
    wx.showLoading({ title: '创建订单...', mask: true })
    this.request({
      url: '/vip/pay/create',
      method: 'POST',
      data: { months: this.data.months || 1 }
    }).then(res => {
      wx.hideLoading()
      if (this._isDestroyed) return
      if (!(res && res.code === 200 && res.data)) {
        this.setData({ purchasing: false })
        wx.showToast({ title: (res && res.msg) || '下单失败', icon: 'none' })
        return
      }
      const data = res.data
      if (data.mock) {
        // 模拟支付：后端已直接开通
        this.setData({ purchasing: false })
        wx.showToast({ title: this.data.vip ? '续费成功' : '开通成功', icon: 'success' })
        this.loadStatus()
        return
      }
      // 真实支付：拉起微信收银台
      this.requestWxPayment(data)
    }).catch(err => {
      wx.hideLoading()
      console.error('创建支付订单失败', err)
      if (!this._isDestroyed) this.setData({ purchasing: false })
      wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
    })
  },

  // 拉起微信收银台并轮询确认支付结果
  requestWxPayment(data) {
    const p = data.payParams || {}
    wx.requestPayment({
      timeStamp: p.timeStamp,
      nonceStr: p.nonceStr,
      package: p.package,
      signType: p.signType || 'RSA',
      paySign: p.paySign,
      success: () => this.confirmPayResult(data.orderNo, 6),
      fail: (err) => {
        console.warn('支付未完成', err)
        if (this._isDestroyed) return
        this.setData({ purchasing: false })
        const cancelled = err && err.errMsg && err.errMsg.indexOf('cancel') >= 0
        wx.showToast({ title: cancelled ? '已取消支付' : '支付未完成，订单已保留', icon: 'none' })
      }
    })
  },

  // 支付完成后的结果确认（轮询查单，兼容回调延迟；次数用尽时提示稍后刷新）
  confirmPayResult(orderNo, remain) {
    if (!remain) {
      if (!this._isDestroyed) {
        this.setData({ purchasing: false })
        wx.showModal({
          title: '支付确认中',
          content: '支付结果确认稍有延迟，如已扣款请稍后返回本页查看会员状态',
          showCancel: false,
          confirmText: '知道了'
        })
      }
      return
    }
    this.request({
      url: `/vip/pay/query?orderNo=${encodeURIComponent(orderNo)}`,
      method: 'GET'
    }).then(res => {
      if (this._isDestroyed) return
      if (res && res.code === 200 && res.data && res.data.paid) {
        this.setData({ purchasing: false })
        wx.showToast({ title: this.data.vip ? '续费成功' : '开通成功', icon: 'success' })
        this.loadStatus()
      } else {
        setTimeout(() => this.confirmPayResult(orderNo, remain - 1), 1200)
      }
    }).catch(() => {
      setTimeout(() => this.confirmPayResult(orderNo, remain - 1), 1200)
    })
  },

  formatDate(str) {
    if (!str) return ''
    const m = String(str).match(/(\d{4})-(\d{2})-(\d{2})/)
    if (!m) return String(str)
    return `${m[1]}-${m[2]}-${m[3]}`
  }
})
