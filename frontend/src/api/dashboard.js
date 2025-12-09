import request from '@/utils/request'

/**
 * 获取平台统计数据
 */
export function getStatistics() {
  return request({
    url: '/admin/dashboard/statistics',
    method: 'get'
  })
}
