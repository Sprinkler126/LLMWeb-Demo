<template>
  <div class="admin-export-container">
    <el-card class="export-card">
      <template #header>
        <div class="card-header">
          <span class="title">导出用户聊天记录</span>
          <el-tag type="warning">管理员功能</el-tag>
        </div>
      </template>

      <!-- 用户搜索 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="用户ID">
          <el-input 
            v-model="searchForm.userId" 
            placeholder="请输入用户ID"
            clearable
            @keyup.enter="handleSearchUser"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearchUser" :loading="loading">
            搜索用户会话
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 用户信息显示 -->
      <div v-if="userInfo" class="user-info-section">
        <el-alert 
          :title="`用户: ${userInfo.targetUsername} (ID: ${userInfo.targetUserId})`"
          type="info"
          :closable="false"
        >
          <template #default>
            <div>共找到 {{ userInfo.totalCount }} 个会话</div>
          </template>
        </el-alert>
      </div>

      <!-- 会话列表 -->
      <el-table
        v-if="sessions.length > 0"
        :data="sessions"
        style="width: 100%; margin-top: 20px"
        border
        stripe
      >
        <el-table-column prop="id" label="会话ID" width="80" />
        <el-table-column prop="title" label="会话标题" min-width="200">
          <template #default="{ row }">
            <span>{{ row.title || '未命名会话' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedTime" label="最后更新" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small"
              @click="handleExport(row.id)"
              :loading="exportingSessionId === row.id"
            >
              导出JSON
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty 
        v-if="searched && sessions.length === 0"
        description="未找到该用户的会话记录"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserSessions, adminExportSessionJson } from '@/api/adminExport'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const loading = ref(false)
const searched = ref(false)
const exportingSessionId = ref(null)

const searchForm = reactive({
  userId: ''
})

const userInfo = ref(null)
const sessions = ref([])

// 搜索用户会话
const handleSearchUser = async () => {
  if (!searchForm.userId) {
    ElMessage.warning('请输入用户ID')
    return
  }

  loading.value = true
  searched.value = true
  
  try {
    const { data } = await getUserSessions(searchForm.userId)
    userInfo.value = {
      targetUserId: data.targetUserId,
      targetUsername: data.targetUsername,
      totalCount: data.totalCount
    }
    sessions.value = data.sessions || []
    
    if (sessions.value.length === 0) {
      ElMessage.info('该用户暂无会话记录')
    } else {
      ElMessage.success(`找到 ${sessions.value.length} 个会话`)
    }
  } catch (error) {
    console.error('搜索用户会话失败:', error)
    ElMessage.error(error.response?.data?.message || '搜索失败')
    userInfo.value = null
    sessions.value = []
  } finally {
    loading.value = false
  }
}

// 导出会话
const handleExport = (sessionId) => {
  if (!userInfo.value) {
    ElMessage.error('请先搜索用户')
    return
  }

  exportingSessionId.value = sessionId
  
  try {
    const url = adminExportSessionJson(
      sessionId, 
      userInfo.value.targetUserId, 
      userStore.token
    )
    
    // 在新窗口打开下载链接
    window.open(url, '_blank')
    
    ElMessage.success('导出请求已发送')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    // 延迟清除loading状态
    setTimeout(() => {
      exportingSessionId.value = null
    }, 1000)
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>

<style scoped>
.admin-export-container {
  padding: 20px;
}

.export-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.search-form {
  margin-bottom: 20px;
}

.user-info-section {
  margin-bottom: 20px;
}
</style>
