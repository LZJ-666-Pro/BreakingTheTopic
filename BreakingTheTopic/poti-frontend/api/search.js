const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    const header = {
      'Content-Type': 'application/json'
    }
    // 登录态才携带凭据，未登录不冒充用户
    if (app.globalData.token) {
      header['Authorization'] = `Bearer ${app.globalData.token}`
      header['X-User-Id'] = app.globalData.userId || ''
    }
    wx.request({
      url: `${app.globalData.searchUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

function get(url, data = {}) {
  return request({
    url,
    method: 'GET',
    data
  })
}

function post(url, data = {}) {
  return request({
    url,
    method: 'POST',
    data
  })
}

const searchApi = {
  searchQuestions(keyword, categoryId, difficulty, pageNum = 1, pageSize = 20) {
    const data = { keyword, pageNum, pageSize }
    if (categoryId) data.categoryId = categoryId
    if (difficulty) data.difficulty = difficulty
    return get('/search/question', data)
  },

  syncQuestion(data) {
    return post('/search/sync', data)
  },

  syncQuestionsBatch(data) {
    return post('/search/sync/batch', data)
  }
}

module.exports = searchApi
