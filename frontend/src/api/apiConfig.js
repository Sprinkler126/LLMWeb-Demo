import request from '@/utils/request'

/**
 * 获取所有启用的API配置
 */
export function getEnabledConfigs() {
  return request({
    url: '/api-config/enabled',
    method: 'get'
  })
}

/**
 * 分页查询API配置
 */
export function getConfigList(params) {
  return request({
    url: '/api-config/list',
    method: 'get',
    params
  })
}

/**
 * 创建API配置
 */
export function createConfig(data) {
  return request({
    url: '/api-config',
    method: 'post',
    data
  })
}

/**
 * 更新API配置
 */
export function updateConfig(id, data) {
  return request({
    url: `/api-config/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除API配置
 */
export function deleteConfig(id) {
  return request({
    url: `/api-config/${id}`,
    method: 'delete'
  })
}

/**
 * 测试API配置
 */
export function testConfig(id) {
  return request({
    url: `/api-config/${id}/test`,
    method: 'post'
  })
}

/**
 * 获取API配置详情
 */
export function getConfigById(id) {
  return request({
    url: `/api-config/${id}`,
    method: 'get'
  })
}
