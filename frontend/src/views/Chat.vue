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
            <!-- API选择 -->
            <div class="api-selector" v-if="!currentSessionId">
              <el-select
                v-model="selectedApiId"
                placeholder="请选择AI模型"
                size="large"
                style="width: 300px"
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
            </div>

            <!-- 消息列表 -->
            <div class="message-list" ref="messageListRef">
              <div v-if="messages.length === 0" class="empty-chat">
                <el-empty description="开始你的对话吧！" />
              </div>

              <div v-for="message in messages" :key="message.id" class="message-item">
                <div :class="['message-bubble', message.role]">
                  <div class="message-role">
                    {{ message.role === 'user' ? '你' : 'AI助手' }}
                  </div>
                  <div class="message-content" v-html="renderMarkdown(message.content)"></div>
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
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入你的问题... (支持 Markdown 格式)"
              @keydown.enter.ctrl="sendMessage"
            />
            <div class="input-footer">
              <span class="input-tip">Ctrl + Enter 发送 | 支持 Markdown</span>
              <el-button
                type="primary"
                :loading="loading"
                :disabled="!inputMessage.trim() || (!currentSessionId && !selectedApiId)"
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
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserSessions, getSessionHistory, sendMessage as sendMessageApi, deleteSession } from '@/api/chat'
import { getEnabledConfigs } from '@/api/apiConfig'
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
const inputMessage = ref('')
const loading = ref(false)
const messageListRef = ref(null)

onMounted(() => {
  loadSessions()
  loadApiConfigs()
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
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    return
  }

  if (!selectedApiId.value) {
    ElMessage.warning('请先选择AI模型')
    return
  }

  const userMessage = inputMessage.value
  inputMessage.value = ''
  loading.value = true

  try {
    const res = await sendMessageApi({
      sessionId: currentSessionId.value,
      apiConfigId: selectedApiId.value,
      message: userMessage,
      sessionTitle: userMessage.substring(0, 30)
    })

    // 更新当前会话ID
    if (!currentSessionId.value) {
      currentSessionId.value = res.data.sessionId
      await loadSessions()
    }

    // 重新加载消息
    await selectSession(currentSessionId.value)

    // 更新API使用量
    userStore.updateApiUsage(res.data.apiUsed)

    scrollToBottom()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
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

.input-tip {
  font-size: 12px;
  color: #909399;
}
</style>
