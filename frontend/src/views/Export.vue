<template>
  <div class="export-container">
    <el-card>
      <template #header>
        <span>数据导出</span>
      </template>
      
      <el-form label-width="120px">
        <el-form-item label="导出格式">
          <el-radio-group v-model="exportFormat">
            <el-radio label="json">JSON</el-radio>
            <el-radio label="csv">CSV</el-radio>
            <el-radio label="excel">Excel</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="exportAll">
            <el-icon><Download /></el-icon>
            导出所有对话记录
          </el-button>
        </el-form-item>
      </el-form>
      
      <el-divider />
      
      <h3>会话列表</h3>
      <el-table :data="sessions" stripe style="margin-top: 20px;">
        <el-table-column prop="sessionTitle" label="会话标题" />
        <el-table-column prop="messageCount" label="消息数" width="100" />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="exportSession(row.id, 'json')">JSON</el-button>
            <el-button size="small" @click="exportSession(row.id, 'csv')">CSV</el-button>
            <el-button size="small" @click="exportSession(row.id, 'excel')">Excel</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserSessions } from '@/api/chat'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const sessions = ref([])
const exportFormat = ref('json')

onMounted(() => {
  loadSessions()
})

const loadSessions = async () => {
  try {
    const res = await getUserSessions()
    sessions.value = res.data.sessions
  } catch (error) {
    console.error(error)
  }
}

const exportSession = (sessionId, format) => {
  // 携带 token 到 URL
  const token = userStore.token
  const url = `${import.meta.env.VITE_API_BASE_URL || '/api'}/export/session/${sessionId}/${format}?token=${token}`
  window.open(url, '_blank')
  ElMessage.success('开始导出...')
}

const exportAll = () => {
  // 携带 token 到 URL
  const token = userStore.token
  const url = `${import.meta.env.VITE_API_BASE_URL || '/api'}/export/all/${exportFormat.value}?token=${token}`
  window.open(url, '_blank')
  ElMessage.success('开始导出...')
}
</script>

<style scoped>
.export-container {
  padding: 20px;
}
</style>
