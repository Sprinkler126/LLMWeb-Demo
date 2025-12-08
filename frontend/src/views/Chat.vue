<template>
  <div class="chat-container">
    <el-row :gutter="20">
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
          <el-scrollbar height="calc(100vh - 350px)" ref="scrollbarRef" class="message-list">
            <div v-if="messages.length === 0" class="empty-chat">
              <el-empty description="开始你的对话吧！" />
            </div>

            <div v-for="message in messages" :key="message.id" class="message-item">
              <div :class="['message-bubble', message.role]">
                <div class="message-role">
                  {{ message.role === 'user' ? '你' : 'AI助手' }}
                </div>
                <div class="message-content">{{ message.content }}</div>
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
          </el-scrollbar>

          <!-- 输入框 -->
          <div class="input-area">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入你的问题..."
              @keydown.enter.ctrl="sendMessage"
            />
            <div class="input-footer">
              <span class="input-tip">Ctrl + Enter 发送</span>
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

const userStore = useUserStore()

const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const apiConfigs = ref([])
const selectedApiId = ref(null)
const inputMessage = ref('')
const loading = ref(false)
const scrollbarRef = ref(null)

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
    if (scrollbarRef.value) {
      scrollbarRef.value.setScrollTop(999999)
    }
  })
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
}

.session-list-card,
.chat-card {
  height: calc(100vh - 120px);
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
}

.message-list {
  padding: 20px;
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

.input-area {
  padding: 20px;
  border-top: 1px solid #e6e6e6;
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
