import request from '@/utils/request'

/**
 * 用户管理API
 */

// 获取所有用户列表
export const getAllUsers = () => {
  return request({
    url: '/admin/users',
    method: 'get'
  })
}

// 获取用户详情
export const getUserById = (userId) => {
  return request({
    url: `/admin/users/${userId}`,
    method: 'get'
  })
}

// 创建用户
export const createUser = (data) => {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  })
}

// 更新用户信息
export const updateUser = (userId, data) => {
  return request({
    url: `/admin/users/${userId}`,
    method: 'put',
    data
  })
}

// 删除用户
export const deleteUser = (userId) => {
  return request({
    url: `/admin/users/${userId}`,
    method: 'delete'
  })
}

// 为用户分配角色
export const assignRole = (userId, roleId) => {
  return request({
    url: `/admin/users/${userId}/role`,
    method: 'put',
    params: { roleId }
  })
}

// 更新用户状态
export const updateUserStatus = (userId, status) => {
  return request({
    url: `/admin/users/${userId}/status`,
    method: 'put',
    params: { status }
  })
}

// 更新API配额
export const updateApiQuota = (userId, quota) => {
  return request({
    url: `/admin/users/${userId}/quota`,
    method: 'put',
    params: { quota }
  })
}

/**
 * 角色权限API
 */

// 获取所有角色
export const getAllRoles = () => {
  return request({
    url: '/admin/roles',
    method: 'get'
  })
}

// 获取可用角色列表
export const getEnabledRoles = () => {
  return request({
    url: '/admin/roles/enabled',
    method: 'get'
  })
}

// 获取所有权限
export const getAllPermissions = () => {
  return request({
    url: '/admin/roles/permissions',
    method: 'get'
  })
}

// 获取角色的权限列表
export const getRolePermissions = (roleId) => {
  return request({
    url: `/admin/roles/${roleId}/permissions`,
    method: 'get'
  })
}

// 为角色分配权限
export const assignPermissions = (roleId, permissionIds) => {
  return request({
    url: `/admin/roles/${roleId}/permissions`,
    method: 'put',
    data: { roleId, permissionIds }
  })
}

// 获取用户权限列表
export const getUserPermissions = (userId) => {
  return request({
    url: `/admin/roles/user/${userId}/permissions`,
    method: 'get'
  })
}

// 检查用户是否有某个权限
export const hasPermission = (userId, permissionCode) => {
  return request({
    url: `/admin/roles/user/${userId}/has-permission`,
    method: 'get',
    params: { permissionCode }
  })
}
