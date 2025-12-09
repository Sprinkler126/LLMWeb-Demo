import request from '@/utils/request'

/**
 * 获取指定用户的会话列表
 */
export function getUserSessions(targetUserId) {
  return request({
    url: `/export/admin/user/${targetUserId}/sessions`,
    method: 'get'
  })
}

/**
 * 管理员导出指定用户的会话（JSON）
 * 注意：这个接口需要通过 window.open 打开，带 token 参数
 */
export function adminExportSessionJson(sessionId, targetUserId, token) {
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  return `${baseURL}/export/admin/session/${sessionId}/json?targetUserId=${targetUserId}&token=${token}`
}
