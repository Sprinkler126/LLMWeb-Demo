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
  
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (to.meta.requiresPermission && !userStore.hasCompliancePermission) {
    ElMessage.warning('您没有合规检测权限')
    next(from.path)
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    ElMessage.warning('您没有管理员权限')
    next(from.path)
  } else {
    next()
  }
})

export default router
