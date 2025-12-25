<template>
  <div class="chat-container">
    <el-row :gutter="20" class="chat-row">
      <!-- 会话列表 -->
      <el-col :span="6">
        <el-card class="session-list-card">
          <template #header>
            <div class="card-header">
              <span>会话列表</span>
              <el-button size="small" type="primary" @click="createNewSession">
                <el-icon><Plus /></el-icon>
                新建会话
              </el-button>
            </div>
          </template>

          <el-scrollbar height="calc(100vh - 200px)">
            <div
              v-for="session in sessions"
              :key="session.id"
              :class="['session-item', { active: currentSessionId === session.id }]"
              @click="selectSession(session.id)"
            >
              <div class="session-title">{{ session.sessionTitle }}</div>
              <div class="session-info">
                <span>{{ session.messageCount }} 条消息</span>
                <el-icon class="delete-icon" @click.stop="deleteSessionConfirm(session.id)">
                  <Delete />
                </el-icon>
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <!-- 聊天区域 -->
      <el-col :span="18">
        <el-card class="chat-card">
          <div class="chat-content-wrapper">
            <!-- 消息列表 -->
            <div class="message-list" ref="messageListRef">
              <div v-if="messages.length === 0" class="empty-chat">
                <el-empty description="开始你的对话吧！" />
              </div>

              <div v-for="message in messages" :key="message.id" class="message-item">
                <div :class="['message-bubble', message.role]">
                  <div class="message-role">
                    {{ message.role === 'user' ? '你' : 'AI助手' }}
                    
                    <!-- 合规状态标签 -->
                    <el-tag 
                      v-if="message.complianceStatus === 'PASS'" 
                      type="success" 
                      size="small"
                      class="compliance-tag">
                      ✓ 合规
                    </el-tag>
                    
                    <el-tag 
                      v-else-if="message.complianceStatus === 'FAIL'" 
                      type="danger" 
                      size="small"
                      class="compliance-tag">
                      ⚠ 风险
                    </el-tag>
                    
                    <el-tag 
                      v-else-if="message.complianceStatus === 'UNCHECKED'" 
                      type="info" 
                      size="small"
                      class="compliance-tag">
                      ○ 未检测
                    </el-tag>
                  </div>
                  <div class="message-content" v-html="renderMarkdown(message.content)"></div>
                  
                  <!-- 显示风险详情（如果有） -->
                  <div v-if="message.complianceResult && message.complianceStatus === 'FAIL'" 
                       class="compliance-detail">
                    <el-alert 
                      type="warning" 
                      :closable="false"
                      show-icon>
                      <template #title>
                        风险提示：{{ getComplianceDetail(message.complianceResult) }}
                      </template>
                    </el-alert>
                  </div>
                  <div class="message-time">
                    {{ formatTime(message.createdTime) }}
                    <span v-if="message.responseTime" class="response-time">
                      ({{ message.responseTime }}ms)
                    </span>
                  </div>
                </div>
              </div>

              <div v-if="loading" class="loading-message">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>AI正在思考中...</span>
              </div>
            </div>
          </div>

          <!-- 输入框 - 固定在底部 -->
          <div class="input-area-fixed">
            <!-- API和Bot选择 -->
            <div class="api-selector-inline" v-if="!currentSessionId" style="margin-bottom: 15px;">
              <el-select
                v-model="selectedApiId"
                placeholder="请选择AI模型"
                size="small"
                style="width: 200px; margin-right: 10px;"
              >
                <el-option
                  v-for="config in apiConfigs"
                  :key="config.id"
                  :label="config.configName"
                  :value="config.id"
                >
                  <span>{{ config.configName }}</span>
                  <span style="color: #8492a6; font-size: 12px; margin-left: 10px">
                    {{ config.provider }}
                  </span>
                </el-option>
              </el-select>
              
              <el-select
                v-model="selectedBotId"
                placeholder="选择机器人角色（可选）"
                size="small"
                clearable
                style="width: 200px;"
              >
                <el-option
                  v-for="bot in botTemplates"
                  :key="bot.id"
                  :label="bot.name"
                  :value="bot.id"
                >
                  <div style="display: flex; flex-direction: column">
                    <span style="font-weight: 500">{{ bot.name }}</span>
                    <span style="color: #8492a6; font-size: 12px">
                      {{ bot.description }}
                    </span>
                  </div>
                </el-option>
              </el-select>
              
              <el-tooltip
                content="选择机器人角色可以让AI以特定的身份和风格回答问题"
                placement="top"
              >
                <el-icon style="margin-left: 8px; color: #909399; cursor: help">
                  <QuestionFilled />
                </el-icon>
              </el-tooltip>
            </div>
            
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入你的问题... (支持 Markdown 格式和文件上传)"
              @keydown.enter.ctrl="sendMessage"
            />
            
            <!-- 文件上传区域 -->
            <div v-if="uploadedFiles.length > 0" class="uploaded-files">
              <el-tag
                v-for="(file, index) in uploadedFiles"
                :key="index"
                closable
                @close="removeFile(index)"
                class="file-tag"
              >
                <el-icon><Document /></el-icon>
                {{ file.fileName }} ({{ formatFileSize(file.fileSize) }})
              </el-tag>
            </div>
            
            <div class="input-footer">
              <div class="input-actions">
                <span class="input-tip">
                  Ctrl + Enter 发送 | 支持 Markdown
                  <span 
                    v-if="totalContentLength > 0" 
                    :class="['content-length', totalContentLength > 200000 ? 'length-warning' : '']"
                  >
                     | {{ (totalContentLength / 1024).toFixed(2) }} KB
                    <span v-if="totalContentLength > 200000"> (超出限制)</span>
                  </span>
                </span>
                <el-upload
                  ref="uploadRef"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleFileChange"
                  :accept="'.txt,.md,.log,.json,.xml,.csv,.java,.py,.js,.ts,.html,.css,.yml,.yaml,.properties'"
                  multiple
                  :limit="10"
                >
                  <el-button size="small" :icon="Paperclip" :loading="uploading">
                    {{ uploading ? '上传中...' : '上传文件' }}
                  </el-button>
                </el-upload>
              </div>
              <el-button
                type="primary"
                :loading="loading"
                :disabled="(!inputMessage.trim() && uploadedFiles.length === 0) || (!currentSessionId && !selectedApiId)"
                @click="sendMessage"
              >
                <el-icon><Promotion /></el-icon>
                发送
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Loading, Promotion, QuestionFilled, Paperclip, Document } from '@element-plus/icons-vue'
import { getUserSessions, getSessionHistory, sendMessage as sendMessageApi, deleteSession, uploadFiles } from '@/api/chat'
import { getEnabledConfigs } from '@/api/apiConfig'
import { getEnabledTemplates } from '@/api/botTemplate'
import { useUserStore } from '@/store/user'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const userStore = useUserStore()

// 配置 marked
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (err) {
        console.error(err)
      }
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const apiConfigs = ref([])
const selectedApiId = ref(null)
const botTemplates = ref([])
const selectedBotId = ref(null)
const inputMessage = ref('')
const loading = ref(false)
const messageListRef = ref(null)
const uploadedFiles = ref([])
const uploading = ref(false)
const uploadRef = ref(null)

// 计算总内容长度（输入 + 所有文件内容）
const totalContentLength = computed(() => {
  let total = inputMessage.value.length
  uploadedFiles.value.forEach(file => {
    total += file.content.length
  })
  return total
})

onMounted(() => {
  loadSessions()
  loadApiConfigs()
  loadBotTemplates()
})

const loadSessions = async () => {
  try {
    const res = await getUserSessions()
    sessions.value = res.data.sessions
  } catch (error) {
    console.error(error)
  }
}

const loadApiConfigs = async () => {
  try {
    const res = await getEnabledConfigs()
    apiConfigs.value = res.data
    if (apiConfigs.value.length > 0) {
      selectedApiId.value = apiConfigs.value[0].id
    }
  } catch (error) {
    console.error(error)
  }
}

const loadBotTemplates = async () => {
  try {
    const res = await getEnabledTemplates()
    botTemplates.value = res.data
  } catch (error) {
    console.error('加载机器人模板失败:', error)
  }
}

const selectSession = async (sessionId) => {
  currentSessionId.value = sessionId
  try {
    const res = await getSessionHistory(sessionId)
    messages.value = res.data.messages
    selectedApiId.value = res.data.session.apiConfigId
    scrollToBottom()
  } catch (error) {
    console.error(error)
  }
}

const createNewSession = () => {
  currentSessionId.value = null
  messages.value = []
  inputMessage.value = ''
  selectedBotId.value = null
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() && uploadedFiles.value.length === 0) {
    return
  }

  if (!selectedApiId.value) {
    ElMessage.warning('请先选择AI模型')
    return
  }

  // 构建完整消息内容（发送给AI）：用户输入 + 文件内容
  let fullMessageContent = inputMessage.value
  
  // 计算总文本长度（输入 + 所有文件内容）
  let totalLength = inputMessage.value.length
  const filesInfo = [...uploadedFiles.value]
  
  filesInfo.forEach(file => {
    totalLength += file.content.length
  })
  
  // 检查总长度限制（200KB = 200,000字符）
  const MAX_LENGTH = 200000
  if (totalLength > MAX_LENGTH) {
    ElMessageBox.confirm(
      `消息总长度为 ${(totalLength / 1024).toFixed(2)} KB，超过了 ${(MAX_LENGTH / 1024).toFixed(0)} KB 的限制。\n\n是否自动截断内容并继续发送？`,
      '消息内容过长',
      {
        confirmButtonText: '截断并发送',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    ).then(() => {
      // 用户选择截断，继续发送
      doSendMessage(true, filesInfo)
    }).catch(() => {
      // 用户取消，不发送
      ElMessage.info('已取消发送')
    })
    return
  }
  
  // 长度正常，直接发送
  doSendMessage(false, filesInfo)
}

// 实际发送消息的函数
const doSendMessage = async (shouldTruncate, filesInfo) => {
  // 构建完整消息内容（发送给AI）
  let fullMessageContent = inputMessage.value
  
  if (filesInfo.length > 0) {
    fullMessageContent += '\n\n--- 附件内容 ---\n'
    filesInfo.forEach(file => {
      let content = file.content
      // 如果需要截断，限制每个文件内容为50KB
      if (shouldTruncate && content.length > 50000) {
        content = content.substring(0, 50000) + '\n\n[内容过长，已截断]'
      }
      fullMessageContent += `\n【文件: ${file.fileName}】\n${content}\n`
    })
  }
  
  // 构建用户可见的消息内容（只显示文件名）
  let displayMessageContent = inputMessage.value
  
  if (filesInfo.length > 0) {
    displayMessageContent += '\n\n📎 附件：'
    filesInfo.forEach((file, index) => {
      displayMessageContent += `\n${index + 1}. ${file.fileName} (${formatFileSize(file.fileSize)})`
    })
  }

  const userMessage = inputMessage.value
  inputMessage.value = ''
  
  // 清空已上传的文件
  uploadedFiles.value = []
  
  // 立即显示用户消息气泡
  const tempUserMessage = {
    id: Date.now(), // 临时ID
    role: 'user',
    content: displayMessageContent,
    createdTime: new Date().toISOString(),
    complianceStatus: 'UNCHECKED'
  }
  messages.value.push(tempUserMessage)
  scrollToBottom()
  
  loading.value = true

  try {
    const res = await sendMessageApi({
      sessionId: currentSessionId.value,
      apiConfigId: selectedApiId.value,
      message: fullMessageContent, // 发送完整内容给AI
      displayMessage: displayMessageContent, // 显示用的内容（只含文件名）
      sessionTitle: userMessage.substring(0, 30) || '文件对话',
      botTemplateId: selectedBotId.value
    })

    // 更新当前会话ID
    if (!currentSessionId.value) {
      currentSessionId.value = res.data.sessionId
      await loadSessions()
    }

    // 重新加载消息（会替换临时消息）
    await selectSession(currentSessionId.value)

    // 更新API使用量
    userStore.updateApiUsage(res.data.apiUsed)

    scrollToBottom()
  } catch (error) {
    console.error(error)
    // 发送失败，移除临时消息
    const tempIndex = messages.value.findIndex(m => m.id === tempUserMessage.id)
    if (tempIndex !== -1) {
      messages.value.splice(tempIndex, 1)
    }
    // 恢复输入和文件列表
    inputMessage.value = userMessage
    uploadedFiles.value = filesInfo
    ElMessage.error('发送失败，请重试')
  } finally {
    loading.value = false
  }
}

// 处理文件选择
const handleFileChange = async (uploadFile) => {
  const file = uploadFile.raw
  
  // 检查文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error(`文件 ${file.name} 超过10MB限制`)
    return
  }
  
  // 检查文件数量
  if (uploadedFiles.value.length >= 10) {
    ElMessage.warning('最多只能上传10个文件')
    return
  }
  
  uploading.value = true
  
  try {
    const res = await uploadFiles([file])
    const result = res.data[0]
    
    if (result.success) {
      uploadedFiles.value.push(result)
      ElMessage.success(`文件 ${result.fileName} 上传成功`)
    } else {
      ElMessage.error(`文件 ${result.fileName} 上传失败: ${result.errorMessage}`)
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error(`文件 ${file.name} 上传失败`)
  } finally {
    uploading.value = false
  }
}

// 移除文件
const removeFile = (index) => {
  uploadedFiles.value.splice(index, 1)
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const deleteSessionConfirm = (sessionId) => {
  ElMessageBox.confirm('确定要删除这个会话吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSession(sessionId)
      ElMessage.success('删除成功')
      if (currentSessionId.value === sessionId) {
        currentSessionId.value = null
        messages.value = []
      }
      await loadSessions()
    } catch (error) {
      console.error(error)
    }
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// Markdown 渲染函数
const renderMarkdown = (content) => {
  if (!content) return ''
  try {
    return marked(content)
  } catch (error) {
    console.error('Markdown rendering error:', error)
    return content
  }
}

// 获取合规检测详情
const getComplianceDetail = (complianceResult) => {
  try {
    const result = JSON.parse(complianceResult)
    return result.detail || '内容存在风险'
  } catch (error) {
    return '内容存在风险'
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 80px);
  overflow: hidden;
}

.chat-row {
  height: 100%;
}

.session-list-card,
.chat-card {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.chat-card {
  position: relative;
  padding-bottom: 180px !important; /* 为固定输入框留出空间 */
}

.chat-card :deep(.el-card__body) {
  padding: 0 !important;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-content-wrapper {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.session-title {
  font-size: 14px;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.delete-icon {
  color: #f56c6c;
}

.api-selector {
  padding: 20px;
  text-align: center;
  flex-shrink: 0;
}

.selector-row {
  display: flex;
  justify-content: center;
  align-items: center;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;
}

.empty-chat {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.message-item {
  margin-bottom: 20px;
}

.message-bubble {
  padding: 12px;
  border-radius: 8px;
  max-width: 70%;
}

.message-bubble.user {
  background: #409eff;
  color: white;
  margin-left: auto;
}

.message-bubble.assistant {
  background: #f4f4f5;
  color: #333;
}

.message-role {
  font-size: 12px;
  margin-bottom: 5px;
  opacity: 0.8;
  display: flex;
  align-items: center;
  gap: 8px;
}

.compliance-tag {
  font-size: 11px;
  padding: 2px 8px;
  margin-left: auto;
}

.compliance-detail {
  margin-top: 10px;
  padding: 8px;
  background: rgba(255, 193, 7, 0.05);
  border-radius: 4px;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
}

/* Markdown 样式优化 */
.message-content :deep(pre) {
  background: #282c34;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
  margin: 10px 0;
}

.message-content :deep(code) {
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
}

.message-content :deep(p code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 3px;
  color: #e83e8c;
}

.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3) {
  margin-top: 15px;
  margin-bottom: 10px;
  font-weight: 600;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  margin: 10px 0;
  padding-left: 25px;
}

.message-content :deep(li) {
  margin: 5px 0;
}

.message-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 15px;
  margin: 10px 0;
  color: #666;
  font-style: italic;
}

.message-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
}

.message-content :deep(th),
.message-content :deep(td) {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.message-content :deep(th) {
  background-color: #f2f2f2;
  font-weight: bold;
}

.message-content :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.message-content :deep(a:hover) {
  text-decoration: underline;
}

.message-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 5px;
  margin: 10px 0;
}

/* 用户消息中的代码块样式 */
.message-bubble.user .message-content :deep(p code) {
  background: rgba(255, 255, 255, 0.2);
  color: #ffe58f;
}

.message-bubble.user .message-content :deep(pre) {
  background: rgba(0, 0, 0, 0.3);
}

.message-time {
  font-size: 12px;
  margin-top: 5px;
  opacity: 0.7;
}

.response-time {
  margin-left: 10px;
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  color: #909399;
}

.input-area-fixed {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: #fff;
  border-top: 1px solid #e6e6e6;
  z-index: 10;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.input-tip {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.content-length {
  font-weight: 500;
  color: #409eff;
}

.length-warning {
  color: #f56c6c;
  font-weight: 600;
}

.uploaded-files {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.file-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.file-tag .el-icon {
  font-size: 14px;
}
</style>
