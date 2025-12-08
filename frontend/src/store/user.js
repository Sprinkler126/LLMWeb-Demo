import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    userId: null,
    username: '',
    nickname: '',
    role: '',
    avatar: '',
    apiQuota: 0,
    apiUsed: 0,
    hasCompliancePermission: false
  }),

  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN'
  },

  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    setUserInfo(userInfo) {
      this.userId = userInfo.userId
      this.username = userInfo.username
      this.nickname = userInfo.nickname
      this.role = userInfo.role
      this.avatar = userInfo.avatar
      this.apiQuota = userInfo.apiQuota
      this.apiUsed = userInfo.apiUsed
      this.hasCompliancePermission = userInfo.hasCompliancePermission === 1

      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    updateApiUsage(used) {
      this.apiUsed = used
      this.userInfo.apiUsed = used
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    logout() {
      this.token = ''
      this.userInfo = {}
      this.userId = null
      this.username = ''
      this.nickname = ''
      this.role = ''
      this.avatar = ''
      this.apiQuota = 0
      this.apiUsed = 0
      this.hasCompliancePermission = false

      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
