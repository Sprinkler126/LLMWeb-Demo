import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => {
    // 从 localStorage 恢复用户信息
    const savedToken = localStorage.getItem('token') || ''
    const savedUserInfoStr = localStorage.getItem('userInfo')
    const savedUserInfo = savedUserInfoStr ? JSON.parse(savedUserInfoStr) : {}
    
    console.log('🔄 Store 初始化 - 从 localStorage 恢复数据:', {
      hasToken: !!savedToken,
      savedUserInfo
    })
    
    return {
      token: savedToken,
      userInfo: savedUserInfo,
      userId: savedUserInfo.userId || null,
      username: savedUserInfo.username || '',
      nickname: savedUserInfo.nickname || '',
      role: savedUserInfo.role || '',
      avatar: savedUserInfo.avatar || '',
      apiQuota: savedUserInfo.apiQuota || 0,
      apiUsed: savedUserInfo.apiUsed || 0,
      // 修复：兼容后端返回的 0/1 数字类型
      hasCompliancePermission: savedUserInfo.hasCompliancePermission === 1 || savedUserInfo.hasCompliancePermission === true
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
      console.log('📝 设置用户信息:', userInfo)
      
      this.userId = userInfo.userId
      this.username = userInfo.username
      this.nickname = userInfo.nickname
      this.role = userInfo.role
      this.avatar = userInfo.avatar
      this.apiQuota = userInfo.apiQuota
      this.apiUsed = userInfo.apiUsed
      // 修复：兼容后端返回的 0/1 数字类型
      this.hasCompliancePermission = userInfo.hasCompliancePermission === 1 || userInfo.hasCompliancePermission === true

      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      
      console.log('✅ 用户信息已保存到 store 和 localStorage:', {
        role: this.role,
        isAdmin: this.isAdmin,
        hasCompliancePermission: this.hasCompliancePermission
      })
    },

    updateApiUsage(used) {
      this.apiUsed = used
      this.userInfo.apiUsed = used
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    setApiUsed(used) {
      this.apiUsed = used
      this.userInfo.apiUsed = used
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },

    setApiQuota(quota) {
      this.apiQuota = quota
      this.userInfo.apiQuota = quota
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
