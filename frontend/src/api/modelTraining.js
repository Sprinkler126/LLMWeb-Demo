/**
 * 模型训练相关API
 */
import request from '@/utils/request'

/**
 * 获取支持的模型类型列表
 */
export function getModelTypes() {
  return request({
    url: '/training/model-types',
    method: 'get'
  })
}

/**
 * 创建训练任务
 */
export function createTrainingTask(data) {
  return request({
    url: '/training/task',
    method: 'post',
    data
  })
}

/**
 * 启动训练任务
 */
export function startTrainingTask(taskId) {
  return request({
    url: `/training/task/${taskId}/start`,
    method: 'post'
  })
}

/**
 * 停止训练任务
 */
export function stopTrainingTask(taskId) {
  return request({
    url: `/training/task/${taskId}/stop`,
    method: 'post'
  })
}

/**
 * 获取任务详情
 */
export function getTaskDetail(taskId) {
  return request({
    url: `/training/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 获取任务进度
 */
export function getTaskProgress(taskId) {
  return request({
    url: `/training/task/${taskId}/progress`,
    method: 'get'
  })
}

/**
 * 获取当前用户的训练任务列表
 */
export function getUserTasks() {
  return request({
    url: '/training/tasks',
    method: 'get'
  })
}

/**
 * 获取所有训练任务（超管）
 */
export function getAllTasks() {
  return request({
    url: '/training/tasks/all',
    method: 'get'
  })
}

/**
 * 删除训练任务
 */
export function deleteTask(taskId) {
  return request({
    url: `/training/task/${taskId}`,
    method: 'delete'
  })
}

/**
 * 获取训练日志
 */
export function getTrainingLog(taskId) {
  return request({
    url: `/training/task/${taskId}/log`,
    method: 'get'
  })
}
