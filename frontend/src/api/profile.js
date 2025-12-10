import request from '@/utils/request'

/**
 * 获取个人信息
 */
export function getProfile() {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

/**
 * 更新个人信息
 */
export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 */
export function updatePassword(data) {
  return request({
    url: '/user/profile/password',
    method: 'put',
    data
  })
}

/**
 * 获取API使用统计
 */
export function getApiUsage() {
  return request({
    url: '/user/profile/api-usage',
    method: 'get'
  })
}

/**
 * 检查并重置API配额
 */
export function checkAndResetApiQuota() {
    return request({
        url: '/user/profile/check-quota',
        method: 'post'
    })
}
