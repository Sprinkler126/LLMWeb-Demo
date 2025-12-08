import request from '@/utils/request'

/**
 * 发送消息
 */
export function sendMessage(data) {
  return request({
    url: '/chat/send',
    method: 'post',
    data
  })
}

/**
 * 获取会话历史
 */
export function getSessionHistory(sessionId) {
  return request({
    url: `/chat/session/${sessionId}`,
    method: 'get'
  })
}

/**
 * 获取用户所有会话
 */
export function getUserSessions() {
  return request({
    url: '/chat/sessions',
    method: 'get'
  })
}

/**
 * 删除会话
 */
export function deleteSession(sessionId) {
  return request({
    url: `/chat/session/${sessionId}`,
    method: 'delete'
  })
}
