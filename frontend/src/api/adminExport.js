import request from '@/utils/request'

/**
 * 获取指定用户的会话列表
 */
// 在 api/adminExport.js 中更新 getUserSessions 方法
export function getUserSessions(userId, username) {
    // 构造查询参数
    const params = {}
    if (userId) {
        params.userId = userId
    }
    if (username) {
        params.username = username
    }

    return request({
        url: '/export/admin/user/sessions',
        method: 'get',
        params
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
