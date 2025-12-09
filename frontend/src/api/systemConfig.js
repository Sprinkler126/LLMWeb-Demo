import request from '@/utils/request'

/**
 * 获取所有系统配置
 */
export function getAllConfigs() {
  return request({
    url: '/admin/system-config',
    method: 'get'
  })
}

/**
 * 根据配置键获取配置
 */
export function getConfigByKey(configKey) {
  return request({
    url: `/admin/system-config/${configKey}`,
    method: 'get'
  })
}

/**
 * 更新配置
 */
export function updateConfig(config) {
  return request({
    url: '/admin/system-config',
    method: 'put',
    data: config
  })
}

/**
 * 批量更新配置
 */
export function batchUpdateConfigs(configs) {
  return request({
    url: '/admin/system-config/batch',
    method: 'put',
    data: configs
  })
}

/**
 * 测试Python检测接口连接
 */
export function testPythonConnection() {
  return request({
    url: '/admin/system-config/test-python-connection',
    method: 'post'
  })
}

/**
 * 根据服务分组获取配置
 */
export function getConfigsByGroup(serviceGroup) {
  return request({
    url: `/admin/system-config/group/${serviceGroup}`,
    method: 'get'
  })
}

/**
 * 获取所有服务分组
 */
export function getAllServiceGroups() {
  return request({
    url: '/admin/system-config/groups',
    method: 'get'
  })
}
