import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_user') || 'null'))

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('admin_token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('admin_user', JSON.stringify(info))
  }

  const updateUserInfo = (info) => {
    userInfo.value = { ...userInfo.value, ...info }
    localStorage.setItem('admin_user', JSON.stringify(userInfo.value))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    updateUserInfo,
    logout
  }
})
