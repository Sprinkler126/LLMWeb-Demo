import request from '@/utils/request'

/**
 * 获取所有公开的机器人模板（用户聊天界面）
 */
export function getEnabledTemplates() {
  return request({
    url: '/admin/bot-template/list',
    method: 'get'
  })
}

/**
 * 获取所有机器人模板（管理员使用，包含所有状态）
 */
export function getAllTemplates() {
  return request({
    url: '/admin/bot-template/admin/all',
    method: 'get'
  })
}

/**
 * 创建机器人模板（管理员）
 */
export function createTemplate(data) {
  return request({
    url: '/admin/bot-template/create',
    method: 'post',
    data
  })
}

/**
 * 更新机器人模板（管理员）
 */
export function updateTemplate(data) {
  return request({
    url: '/admin/bot-template/update',
    method: 'put',
    data
  })
}

/**
 * 删除机器人模板（管理员）
 */
export function deleteTemplate(id) {
  return request({
    url: `/admin/bot-template/delete/${id}`,
    method: 'delete'
  })
}
