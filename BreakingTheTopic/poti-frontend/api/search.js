const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.searchUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${app.globalData.token}`,
        'X-User-Id': app.globalData.userId || 1,
        ...options.header
      },
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
  searchQuestions(keyword, type, pageNum = 1, pageSize = 20) {
    return get('/search/question', { keyword, type, pageNum, pageSize })
  },

  syncQuestion(data) {
    return post('/search/sync', data)
  },

  syncQuestionsBatch(data) {
    return post('/search/sync/batch', data)
  }
}

module.exports = searchApi
