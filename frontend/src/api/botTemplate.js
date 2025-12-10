import request from '@/utils/request'

/**
 * 获取所有启用的机器人模板
 */
export function getEnabledTemplates() {
  return request({
    url: '/admin/bot-template/list',
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
