<template>
  <div class="batch-compliance-container">
    <el-card class="header-card">
      <div class="header">
        <h2>📁 批量合规检测</h2>
        <el-tag type="info">支持JSON/CSV格式的导出文件</el-tag>
      </div>
    </el-card>

    <!-- 文件上传区域 -->
    <el-card class="upload-card">
      <template #header>
        <span>上传文件</span>
      </template>

      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        accept=".json,.csv"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持JSON或CSV格式的聊天记录导出文件，单个文件限制50MB
          </div>
        </template>
      </el-upload>

      <div v-if="selectedFile" class="file-info">
        <el-alert
          :title="`已选择文件：${selectedFile.name} (${formatFileSize(selectedFile.size)})`"
          type="success"
          :closable="false"
        />
      </div>

      <div class="action-buttons">
        <el-button type="primary" @click="handleBatchCheck" :loading="checking" :disabled="!selectedFile">
          <el-icon><Check /></el-icon>
          开始批量检测
        </el-button>
        <el-button @click="handleClear" :disabled="checking">
          <el-icon><Close /></el-icon>
          清除
        </el-button>
      </div>
    </el-card>

    <!-- 检测结果 -->
    <el-card v-if="batchResult" class="result-card">
      <template #header>
        <div class="result-header">
          <span>检测结果</span>
          <el-button type="primary" size="small" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出结果
          </el-button>
        </div>
      </template>

      <!-- 统计摘要 -->
      <div class="summary">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总计" :value="batchResult.total">
              <template #suffix>条</template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="通过" :value="batchResult.passedCount">
              <template #suffix>条</template>
              <template #prefix>
                <el-icon color="#67c23a"><SuccessFilled /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="失败" :value="batchResult.failedCount">
              <template #suffix>条</template>
              <template #prefix>
                <el-icon color="#f56c6c"><CircleCloseFilled /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="未检测" :value="batchResult.uncheckedCount">
              <template #suffix>条</template>
              <template #prefix>
                <el-icon color="#909399"><WarningFilled /></el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>

      <!-- 详细结果表格 -->
      <el-divider />
      <el-table :data="batchResult.items" stripe style="width: 100%" max-height="600">
        <el-table-column prop="index" label="序号" width="80" fixed />
        <el-table-column label="用户消息" min-width="200">
          <template #default="{ row }">
            <div class="message-content">{{ row.userContent }}</div>
            <el-tag v-if="row.userResult" :type="getResultType(row.userResult)" size="small">
              {{ row.userResult }}
            </el-tag>
            <el-tag v-if="row.userRiskLevel" :type="getRiskLevelType(row.userRiskLevel)" size="small">
              {{ row.userRiskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AI响应" min-width="200">
          <template #default="{ row }">
            <div class="message-content">{{ row.assistantContent || '-' }}</div>
            <el-tag v-if="row.assistantResult" :type="getResultType(row.assistantResult)" size="small">
              {{ row.assistantResult }}
            </el-tag>
            <el-tag v-if="row.assistantRiskLevel" :type="getRiskLevelType(row.assistantRiskLevel)" size="small">
              {{ row.assistantRiskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险类别" width="150">
          <template #default="{ row }">
            <div v-if="row.userRiskCategories">
              <el-tag size="small" type="danger">用户: {{ row.userRiskCategories }}</el-tag>
            </div>
            <div v-if="row.assistantRiskCategories">
              <el-tag size="small" type="warning">AI: {{ row.assistantRiskCategories }}</el-tag>
            </div>
            <span v-if="!row.userRiskCategories && !row.assistantRiskCategories">-</span>
          </template>
        </el-table-column>
        <el-table-column label="会话ID" width="100" prop="sessionId" />
        <el-table-column label="消息ID" width="100" prop="messageId" />
      </el-table>
    </el-card>

    <!-- 使用说明 -->
    <el-card class="help-card">
      <template #header>
        <span>💡 使用说明</span>
      </template>
      <el-steps direction="vertical" :active="3">
        <el-step title="导出对话记录" description="从'个人数据导出'页面导出您的对话记录为JSON或CSV格式" />
        <el-step title="上传文件" description="在上方上传区域选择或拖拽导出的文件" />
        <el-step title="批量检测" description="点击'开始批量检测'按钮，系统将自动解析文件并检测所有对话内容" />
        <el-step title="查看结果" description="查看检测结果统计和详细信息，可导出结果报告" />
      </el-steps>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  UploadFilled, 
  Check, 
  Close, 
  Download,
  SuccessFilled,
  CircleCloseFilled,
  WarningFilled
} from '@element-plus/icons-vue'
import { batchCheckFromFile } from '@/api/compliance'

const uploadRef = ref(null)
const selectedFile = ref(null)
const checking = ref(false)
const batchResult = ref(null)

// 文件选择
const handleFileChange = (file) => {
  selectedFile.value = file.raw
  ElMessage.success(`已选择文件: ${file.name}`)
}

// 超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传1个文件')
}

// 清除文件
const handleClear = () => {
  selectedFile.value = null
  batchResult.value = null
  uploadRef.value?.clearFiles()
  ElMessage.info('已清除')
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 批量检测
const handleBatchCheck = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  try {
    await ElMessageBox.confirm(
      '将对文件中的所有对话内容进行合规检测，这可能需要一些时间，是否继续？',
      '确认检测',
      {
        confirmButtonText: '开始检测',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    checking.value = true
    batchResult.value = null

    const res = await batchCheckFromFile(selectedFile.value)
    if (res.code === 200) {
      batchResult.value = res.data
      ElMessage.success(`批量检测完成！共检测 ${res.data.total} 条记录`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量检测失败:', error)
      ElMessage.error(error.message || '批量检测失败')
    }
  } finally {
    checking.value = false
  }
}

// 导出结果
const handleExport = () => {
  if (!batchResult.value) return

  // 转换为CSV格式
  const headers = ['序号', '会话ID', '消息ID', '用户消息', 'AI响应', '用户检测结果', 'AI检测结果', '用户风险等级', 'AI风险等级', '用户风险类别', 'AI风险类别']
  const rows = batchResult.value.items.map(item => [
    item.index,
    item.sessionId || '',
    item.messageId || '',
    `"${(item.userContent || '').replace(/"/g, '""')}"`,
    `"${(item.assistantContent || '').replace(/"/g, '""')}"`,
    item.userResult || '',
    item.assistantResult || '',
    item.userRiskLevel || '',
    item.assistantRiskLevel || '',
    item.userRiskCategories || '',
    item.assistantRiskCategories || ''
  ])

  const csvContent = [
    '\uFEFF' + headers.join(','),  // BOM for Excel UTF-8 support
    ...rows.map(row => row.join(','))
  ].join('\n')

  // 创建下载
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `batch_compliance_result_${Date.now()}.csv`
  link.click()

  ElMessage.success('结果已导出')
}

// 获取结果类型
const getResultType = (result) => {
  const map = {
    'PASS': 'success',
    'FAIL': 'danger',
    'UNCHECKED': 'info'
  }
  return map[result] || 'info'
}

// 获取风险等级类型
const getRiskLevelType = (level) => {
  const map = {
    'LOW': 'success',
    'MEDIUM': 'warning',
    'HIGH': 'danger',
    'UNKNOWN': 'info'
  }
  return map[level] || 'info'
}
</script>

<style scoped>
.batch-compliance-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  margin: 0;
  font-size: 24px;
}

.upload-card {
  margin-bottom: 20px;
}

.upload-area {
  width: 100%;
}

.file-info {
  margin-top: 20px;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.result-card {
  margin-bottom: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary {
  margin-bottom: 20px;
}

.message-content {
  margin-bottom: 8px;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.help-card {
  margin-bottom: 20px;
}
</style>
