const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.favoriteUrl}${options.url}`,
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

function del(url, data = {}) {
  return request({
    url,
    method: 'DELETE',
    data
  })
}

const favoriteApi = {
  getFavoriteList(page = 1, size = 10) {
    return get('/favorite/list', { page, size })
  },

  addFavorite(data) {
    return post('/favorite/add', data)
  },

  removeFavorite(questionId) {
    return del(`/favorite/remove/${questionId}`)
  },

  checkFavorite(questionId) {
    return get(`/favorite/check/${questionId}`)
  },

  clearAll() {
    return del('/favorite/clear')
  }
}

module.exports = favoriteApi
