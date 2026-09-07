// 日志工具类
const env = process.env.NODE_ENV || 'development'

const logger = {
  log(...args) {
    if (env === 'development') {
      console.log(...args)
    }
  },
  
  error(...args) {
    console.error(...args)
  },
  
  warn(...args) {
    if (env === 'development') {
      console.warn(...args)
    }
  },
  
  info(...args) {
    if (env === 'development') {
      console.info(...args)
    }
  },
  
  debug(...args) {
    if (env === 'development') {
      console.debug(...args)
    }
  }
}

module.exports = logger
