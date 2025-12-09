import request from '@/utils/request'

/**
 * 检测单条消息
 * @param {Object} data - { content: string }
 * @returns {Promise}
 */
export function checkSingleMessage(data) {
  return request({
    url: '/compliance/check-message',
    method: 'post',
    data
  })
}

/**
 * 创建合规检测任务
 * @param {Object} data - 任务数据
 * @returns {Promise}
 */
export function createComplianceTask(data) {
  return request({
    url: '/compliance/task',
    method: 'post',
    data
  })
}

/**
 * 获取任务详情
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function getTaskDetail(taskId) {
  return request({
    url: `/compliance/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 获取用户的任务列表
 * @param {number} current - 当前页
 * @param {number} size - 每页大小
 * @returns {Promise}
 */
export function getUserTasks(current = 1, size = 10) {
  return request({
    url: '/compliance/tasks',
    method: 'get',
    params: { current, size }
  })
}

/**
 * 触发检测任务
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function triggerCheck(taskId) {
  return request({
    url: `/compliance/task/${taskId}/trigger`,
    method: 'post'
  })
}
