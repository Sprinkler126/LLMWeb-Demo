import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/chat',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/Chat.vue'),
        meta: { title: '对话' }
      },
      {
        path: 'api-config',
        name: 'ApiConfig',
        component: () => import('@/views/ApiConfig.vue'),
        meta: { title: 'API配置管理' }
      },
      {
        path: 'compliance',
        name: 'Compliance',
        component: () => import('@/views/Compliance.vue'),
        meta: { title: '合规检测', requiresPermission: true }
      },
      {
        path: 'export',
        name: 'Export',
        component: () => import('@/views/Export.vue'),
        meta: { title: '数据导出' }
      },
      {
        path: 'user-management',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: { title: '用户管理', requiresAdmin: true }
      },
      {
        path: 'role-permission',
        name: 'RolePermission',
        component: () => import('@/views/RolePermission.vue'),
        meta: { title: '角色权限配置', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 如果需要认证但未登录，跳转到登录页
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
    return
  }
  
  // 如果需要合规检测权限但没有该权限
  if (to.meta.requiresPermission && !userStore.hasCompliancePermission) {
    // 防止重复导航到同一路径
    if (from.path !== to.path) {
      const { ElMessage } = await import('element-plus')
      ElMessage.warning('您没有合规检测权限')
    }
    // 如果 from 是空的（首次访问），跳转到首页
    next(from.path || '/chat')
    return
  }
  
  // 如果需要管理员权限但不是管理员
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    // 防止重复导航到同一路径
    if (from.path !== to.path) {
      const { ElMessage } = await import('element-plus')
      ElMessage.warning('您没有管理员权限')
    }
    // 如果 from 是空的（首次访问），跳转到首页
    next(from.path || '/chat')
    return
  }
  
  // 允许访问
  next()
})

export default router
