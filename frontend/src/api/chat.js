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

/**
 * 上传文件并提取文本
 */
export function uploadFiles(files) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: '/chat/upload-files',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
