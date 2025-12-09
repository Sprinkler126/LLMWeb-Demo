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
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'admin-export',
        name: 'AdminExport',
        component: () => import('@/views/AdminExport.vue'),
        meta: { title: '导出用户记录', requiresAdmin: true }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '平台数据', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  console.log('🛡️ 路由守卫检查:', { 
    from: from.path, 
    to: to.path, 
    hasToken: !!userStore.token,
    role: userStore.role,
    isAdmin: userStore.isAdmin 
  })
  
  // 防止重复导航到同一路径
  if (to.path === from.path && from.path !== '/') {
    console.log('⚠️ 阻止重复导航到同一路径:', to.path)
    next(false) // 取消导航
    return
  }
  
  // 如果需要认证但未登录，跳转到登录页
  if (to.meta.requiresAuth && !userStore.token) {
    console.log('❌ 未登录，跳转到登录页')
    next('/login')
    return
  }
  
  // 如果需要合规检测权限但没有该权限
  if (to.meta.requiresPermission && !userStore.hasCompliancePermission) {
    console.log('❌ 无合规检测权限')
    if (from.path && from.path !== '/' && from.path !== to.path) {
      const { ElMessage } = await import('element-plus')
      ElMessage.warning('您没有合规检测权限')
      next(false) // 停留在当前页面
    } else {
      next('/chat') // 跳转到首页
    }
    return
  }
  
  // 如果需要管理员权限但不是管理员
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    console.log('❌ 无管理员权限, role:', userStore.role, 'isAdmin:', userStore.isAdmin)
    if (from.path && from.path !== '/' && from.path !== to.path) {
      const { ElMessage } = await import('element-plus')
      ElMessage.warning('您没有管理员权限')
      next(false) // 停留在当前页面
    } else {
      next('/chat') // 跳转到首页
    }
    return
  }
  
  // 允许访问
  console.log('✅ 路由守卫通过，允许访问:', to.path)
  next()
})

// 处理路由错误（防止重复导航警告）
router.onError((error) => {
  console.error('🚨 路由错误:', error)
  if (error.message.includes('Avoided redundant navigation')) {
    console.log('已拦截重复导航错误')
  }
})

export default router
