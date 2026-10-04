const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.practiceUrl}${options.url}`,
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

function put(url, data = {}) {
  return request({
    url,
    method: 'PUT',
    data
  })
}

function del(url, data = {}) {
  return request({
    url,
    method: 'DELETE',
    data
  })
}

const practiceApi = {
  submitAnswer(questionId, userAnswer, spendSeconds) {
    return post('/practice/submit', {
      questionId,
      userAnswer,
      spendSeconds
    })
  },

  getPracticeRecord(questionId) {
    return get(`/practice/record/${questionId}`)
  },

  getPracticeHistory(page = 1, size = 10) {
    return get('/practice/history', { page, size })
  },

  deleteRecord(recordId) {
    return del(`/practice/record/${recordId}`)
  },

  clearAllRecords() {
    return del('/practice/clear')
  }
}

module.exports = practiceApi
