<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>QnA Platform</h2>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        class="menu"
        unique-opened
        @select="handleMenuSelect"
      >
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>对话</span>
        </el-menu-item>

        <el-menu-item index="/api-config">
          <el-icon><Setting /></el-icon>
          <span>API配置</span>
        </el-menu-item>

        <el-menu-item index="/compliance" v-if="userStore.hasCompliancePermission">
          <el-icon><DocumentChecked /></el-icon>
          <span>合规检测</span>
        </el-menu-item>

        <el-menu-item index="/export">
          <el-icon><Download /></el-icon>
          <span>数据导出</span>
        </el-menu-item>

        <el-menu-item index="/user-management" v-if="userStore.isAdmin">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/role-permission" v-if="userStore.isAdmin">
          <el-icon><Key /></el-icon>
          <span>角色权限</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>

        <div class="header-right">
          <div class="api-usage">
            <el-icon><Coin /></el-icon>
            <span>API额度: {{ userStore.apiUsed }} / {{ userStore.apiQuota }}</span>
          </div>

          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :src="userStore.avatar || defaultAvatar" />
              <span class="username">{{ userStore.nickname || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '首页')

// 🔍 调试：监控 store 状态变化
onMounted(() => {
  console.log('📱 Layout 组件已挂载，当前用户信息:', {
    token: !!userStore.token,
    role: userStore.role,
    isAdmin: userStore.isAdmin,
    hasCompliancePermission: userStore.hasCompliancePermission,
    username: userStore.username
  })
})

// 监控路由变化
watch(() => route.path, (newPath, oldPath) => {
  console.log('🚏 路由变化:', { from: oldPath, to: newPath, title: route.meta.title })
})

// 监控 store 状态变化
watch(() => userStore.role, (newRole) => {
  console.log('👤 用户角色变化:', newRole, 'isAdmin:', userStore.isAdmin)
})

// 菜单选择处理
const handleMenuSelect = (index) => {
  console.log('📋 菜单选择:', index)
  // 如果选择的是当前已激活的菜单项，不做任何操作
  if (index === route.path) {
    console.log('⚠️ 已在当前页面，阻止重复导航')
    return false
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #304156;
  color: white;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2c3e50;
  color: white;
}

.logo h2 {
  font-size: 18px;
  margin: 0;
}

.menu {
  border: none;
  background: #304156;
}

.menu :deep(.el-menu-item) {
  color: #bfcbd9;
}

.menu :deep(.el-menu-item:hover) {
  background-color: #263445 !important;
  color: #409eff;
}

.menu :deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
  color: white;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background: white;
  border-bottom: 1px solid #e6e6e6;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.api-usage {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 15px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
}
</style>
