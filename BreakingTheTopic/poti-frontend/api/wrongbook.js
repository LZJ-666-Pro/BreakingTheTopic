const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.wrongbookUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
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

const wrongbookApi = {
  getWrongbookList(page = 1, size = 10) {
    return get('/wrongbook/list', { page, size })
  },

  addWrongbook(data) {
    return post('/wrongbook/add', data)
  },

  removeWrongbook(questionId) {
    return del(`/wrongbook/${questionId}`)
  },

  clearAllWrongbook() {
    return del('/wrongbook/clear')
  },

  markAsMastered(questionId) {
    return put(`/wrongbook/mastered/${questionId}`)
  },

  getStatistics() {
    return get('/wrongbook/statistics')
  }
}

module.exports = wrongbookApi
