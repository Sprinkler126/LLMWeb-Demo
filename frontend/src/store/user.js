import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => {
    // 从 localStorage 恢复用户信息
    const savedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    
    return {
      token: localStorage.getItem('token') || '',
      userInfo: savedUserInfo,
      userId: savedUserInfo.userId || null,
      username: savedUserInfo.username || '',
      nickname: savedUserInfo.nickname || '',
      role: savedUserInfo.role || '',
      avatar: savedUserInfo.avatar || '',
      apiQuota: savedUserInfo.apiQuota || 0,
      apiUsed: savedUserInfo.apiUsed || 0,
      hasCompliancePermission: savedUserInfo.hasCompliancePermission === 1
    }
  },

  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN' || state.role === 'SUPER_ADMIN'
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
