const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    app.request({
      ...options,
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

module.exports = {
  request,
  get,
  post,
  put,
  del
}
