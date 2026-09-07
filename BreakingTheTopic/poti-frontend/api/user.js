const { get, post, put } = require('../utils/api.js')

const userApi = {
  getUserInfo() {
    return get('/user/info')
  },

  updateUserInfo(data) {
    return put('/user/info', data)
  },

  getStatistics() {
    return get('/user/statistics')
  },

  getCalendar(year, month) {
    return get('/user/calendar', { year, month })
  },

  getRanking(type, subtab, page, limit) {
    return get('/user/rank', { type, subtab, page, limit })
  },

  getAchievements(category) {
    return get('/user/achievement', { category })
  },

  checkAchievements(eventType, value) {
    return post('/user/achievement/check', { eventType, value })
  },

  initAchievements() {
    return post('/user/achievement/init')
  },

  syncAchievements() {
    return post('/user/achievement/sync')  
  }
}

module.exports = userApi
