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

// ========== LLM合规检测API ==========

/**
 * 获取可用的API配置列表（用于模型选择）
 * @returns {Promise}
 */
export function getAvailableApiConfigs() {
  return request({
    url: '/llm-compliance/api-configs',
    method: 'get'
  })
}

/**
 * 创建LLM合规检测任务
 * @param {Object} data - 任务数据
 * @returns {Promise}
 */
export function createLlmComplianceTask(data) {
  return request({
    url: '/llm-compliance/task',
    method: 'post',
    data
  })
}

/**
 * 启动LLM合规检测任务
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function startLlmComplianceTask(taskId) {
  return request({
    url: `/llm-compliance/task/${taskId}/start`,
    method: 'post'
  })
}

/**
 * 获取LLM合规检测任务详情
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function getLlmComplianceTask(taskId) {
  return request({
    url: `/llm-compliance/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 获取LLM合规检测任务结果列表
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function getLlmComplianceTaskResults(taskId) {
  return request({
    url: `/llm-compliance/task/${taskId}/results`,
    method: 'get'
  })
}

/**
 * 获取用户的LLM合规检测任务列表
 * @param {number} current - 当前页
 * @param {number} size - 每页大小
 * @returns {Promise}
 */
export function getLlmComplianceTasks(current = 1, size = 10) {
  return request({
    url: '/llm-compliance/tasks',
    method: 'get',
    params: { current, size }
  })
}

/**
 * 删除LLM合规检测任务
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function deleteLlmComplianceTask(taskId) {
  return request({
    url: `/llm-compliance/task/${taskId}`,
    method: 'delete'
  })
}
