<template>
  <div class="compliance-check-container">
    <!-- 页面标题 -->
    <el-card class="header-card">
      <div class="header">
        <h2>🛡️ 对话合规检测</h2>
        <el-tag :type="serviceStatus.type">{{ serviceStatus.text }}</el-tag>
      </div>
    </el-card>

    <!-- 检测模式选择 -->
    <el-card class="mode-card">
      <el-radio-group v-model="checkMode" size="large" @change="handleModeChange">
        <el-radio-button value="single">
          <el-icon><Edit /></el-icon>
          单条测试
        </el-radio-button>
        <el-radio-button value="batch">
          <el-icon><FolderOpened /></el-icon>
          批量测试
        </el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 单条测试模式 -->
    <el-card v-show="checkMode === 'single'" class="test-card">
      <template #header>
        <span>单条内容检测</span>
      </template>

      <el-form :model="singleForm" label-width="120px">
        <el-form-item label="检测内容">
          <el-input
            v-model="singleForm.content"
            type="textarea"
            :rows="8"
            placeholder="请输入要检测的文本内容..."
            clearable
            show-word-limit
            maxlength="5000"
          />
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleSingleCheck" 
            :loading="singleChecking"
            :disabled="!singleForm.content.trim()"
          >
            <el-icon><Select /></el-icon>
            开始检测
          </el-button>
          <el-button @click="handleClearSingle">
            <el-icon><Delete /></el-icon>
            清空
          </el-button>
          <el-button @click="handleCheckService" :loading="serviceChecking">
            <el-icon><Connection /></el-icon>
            检查服务
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 单条检测结果 -->
      <div v-if="singleResult" class="single-result">
        <el-divider>检测结果</el-divider>
        
        <el-result
          :icon="singleResult.result === 'PASS' ? 'success' : 'error'"
          :title="singleResult.result === 'PASS' ? '✅ 内容合规' : '⚠️ 内容存在风险'"
        >
          <template #sub-title>
            <div class="result-summary">
              <el-tag :type="getResultType(singleResult.result)" size="large">
                {{ singleResult.result }}
              </el-tag>
              <el-tag :type="getRiskLevelType(singleResult.risk_level)" size="large">
                风险等级: {{ getRiskLevelText(singleResult.risk_level) }}
              </el-tag>
              <el-tag type="info" size="large">
                置信度: {{ (singleResult.confidence_score * 100).toFixed(2) }}%
              </el-tag>
            </div>
          </template>
          <template #extra>
            <el-button type="primary" @click="showSingleDetail = true">
              查看详细信息
            </el-button>
          </template>
        </el-result>

        <!-- 简要信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="风险类别">
            <el-tag v-if="singleResult.risk_categories" type="danger">
              {{ singleResult.risk_categories }}
            </el-tag>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="详细说明">
            {{ singleResult.detail || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 批量测试模式 -->
    <el-card v-show="checkMode === 'batch'" class="test-card">
      <template #header>
        <span>批量文件检测</span>
      </template>

      <!-- 文件上传 -->
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
        <el-button 
          type="primary" 
          @click="handleBatchCheck" 
          :loading="batchChecking" 
          :disabled="!selectedFile"
        >
          <el-icon><Check /></el-icon>
          开始批量检测
        </el-button>
        <el-button @click="handleClearBatch" :disabled="batchChecking">
          <el-icon><Close /></el-icon>
          清除
        </el-button>
      </div>
    </el-card>

    <!-- 批量检测结果 -->
    <el-card v-if="batchResult" class="result-card">
      <template #header>
        <div class="result-header">
          <span>批量检测结果</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="handleExportBatch">
              <el-icon><Download /></el-icon>
              导出结果
            </el-button>
          </div>
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
      <el-table 
        :data="batchResult.items" 
        stripe 
        style="width: 100%" 
        max-height="600"
        @row-click="handleRowClick"
      >
        <el-table-column prop="index" label="序号" width="80" fixed />
        <el-table-column label="用户消息" min-width="200">
          <template #default="{ row }">
            <div class="message-preview">{{ row.userContent }}</div>
            <div class="tags">
              <el-tag v-if="row.userResult" :type="getResultType(row.userResult)" size="small">
                {{ row.userResult }}
              </el-tag>
              <el-tag v-if="row.userRiskLevel" :type="getRiskLevelType(row.userRiskLevel)" size="small">
                {{ row.userRiskLevel }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="AI响应" min-width="200">
          <template #default="{ row }">
            <div class="message-preview">{{ row.assistantContent || '-' }}</div>
            <div class="tags">
              <el-tag v-if="row.assistantResult" :type="getResultType(row.assistantResult)" size="small">
                {{ row.assistantResult }}
              </el-tag>
              <el-tag v-if="row.assistantRiskLevel" :type="getRiskLevelType(row.assistantRiskLevel)" size="small">
                {{ row.assistantRiskLevel }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="风险类别" width="150">
          <template #default="{ row }">
            <div v-if="row.userRiskCategories" class="risk-tag">
              <el-tag size="small" type="danger">{{ row.userRiskCategories }}</el-tag>
            </div>
            <div v-if="row.assistantRiskCategories" class="risk-tag">
              <el-tag size="small" type="warning">{{ row.assistantRiskCategories }}</el-tag>
            </div>
            <span v-if="!row.userRiskCategories && !row.assistantRiskCategories">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click.stop="showBatchDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 使用说明 -->
    <el-card class="help-card">
      <template #header>
        <span>💡 使用说明</span>
      </template>
      
      <el-tabs>
        <el-tab-pane label="单条测试">
          <el-steps direction="vertical" :active="2">
            <el-step title="输入内容" description="在文本框中输入需要检测的内容" />
            <el-step title="开始检测" description="点击'开始检测'按钮，系统将调用合规检测服务" />
            <el-step title="查看结果" description="查看检测结果和详细信息，包括风险等级、置信度等" />
          </el-steps>
        </el-tab-pane>
        
        <el-tab-pane label="批量测试">
          <el-steps direction="vertical" :active="3">
            <el-step title="导出记录" description="从'个人数据导出'页面导出对话记录为JSON或CSV格式" />
            <el-step title="上传文件" description="拖拽或点击选择导出的文件" />
            <el-step title="批量检测" description="点击'开始批量检测'，系统将解析文件并检测所有内容" />
            <el-step title="查看结果" description="查看统计摘要、详细结果，可点击每一条查看完整响应" />
          </el-steps>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 单条检测详细对话框 -->
    <el-dialog
      v-model="showSingleDetail"
      title="检测详细信息"
      width="800px"
    >
      <el-descriptions :column="1" border v-if="singleResult">
        <el-descriptions-item label="检测结果">
          <el-tag :type="getResultType(singleResult.result)" size="large">
            {{ singleResult.result }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="getRiskLevelType(singleResult.risk_level)" size="large">
            {{ getRiskLevelText(singleResult.risk_level) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="置信度">
          {{ (singleResult.confidence_score * 100).toFixed(2) }}%
        </el-descriptions-item>
        <el-descriptions-item label="风险类别">
          {{ singleResult.risk_categories || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="详细说明">
          {{ singleResult.detail || '无' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />
      <div class="json-viewer">
        <div class="json-header">
          <span>完整响应 (JSON)</span>
          <el-button size="small" @click="copyJson(singleResult)">
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
        </div>
        <pre class="json-content">{{ JSON.stringify(singleResult, null, 2) }}</pre>
      </div>
    </el-dialog>

    <!-- 批量检测详细对话框 -->
    <el-dialog
      v-model="showBatchDetailDialog"
      title="检测详细信息"
      width="900px"
    >
      <div v-if="selectedBatchItem">
        <el-tabs v-model="detailTab">
          <el-tab-pane label="用户消息" name="user">
            <div class="detail-section">
              <div class="content-display">
                <h4>消息内容：</h4>
                <el-scrollbar max-height="200px">
                  <p class="content-text">{{ selectedBatchItem.userContent }}</p>
                </el-scrollbar>
              </div>
              
              <el-divider />
              
              <el-descriptions :column="2" border>
                <el-descriptions-item label="检测结果">
                  <el-tag :type="getResultType(selectedBatchItem.userResult)">
                    {{ selectedBatchItem.userResult }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="风险等级">
                  <el-tag :type="getRiskLevelType(selectedBatchItem.userRiskLevel)">
                    {{ selectedBatchItem.userRiskLevel }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="风险类别" :span="2">
                  {{ selectedBatchItem.userRiskCategories || '无' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="AI响应" name="assistant" v-if="selectedBatchItem.assistantContent">
            <div class="detail-section">
              <div class="content-display">
                <h4>响应内容：</h4>
                <el-scrollbar max-height="200px">
                  <p class="content-text">{{ selectedBatchItem.assistantContent }}</p>
                </el-scrollbar>
              </div>
              
              <el-divider />
              
              <el-descriptions :column="2" border>
                <el-descriptions-item label="检测结果">
                  <el-tag :type="getResultType(selectedBatchItem.assistantResult)">
                    {{ selectedBatchItem.assistantResult }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="风险等级">
                  <el-tag :type="getRiskLevelType(selectedBatchItem.assistantRiskLevel)">
                    {{ selectedBatchItem.assistantRiskLevel }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="风险类别" :span="2">
                  {{ selectedBatchItem.assistantRiskCategories || '无' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="元数据" name="meta">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="序号">
                {{ selectedBatchItem.index }}
              </el-descriptions-item>
              <el-descriptions-item label="会话ID">
                {{ selectedBatchItem.sessionId || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="消息ID">
                {{ selectedBatchItem.messageId || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="检测时间">
                {{ new Date(selectedBatchItem.timestamp).toLocaleString('zh-CN') }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  FolderOpened,
  Select,
  Delete,
  Connection,
  UploadFilled,
  Check,
  Close,
  Download,
  SuccessFilled,
  CircleCloseFilled,
  WarningFilled,
  CopyDocument
} from '@element-plus/icons-vue'
import { checkSingleMessage, batchCheckFromFile } from '@/api/compliance'
import { getAllConfigs } from '@/api/systemConfig'

// 检测模式
const checkMode = ref('single')

// 服务状态
const serviceStatus = reactive({
  type: 'info',
  text: '检查中...'
})

// 单条测试
const singleForm = reactive({
  content: ''
})
const singleChecking = ref(false)
const singleResult = ref(null)
const showSingleDetail = ref(false)

// 批量测试
const uploadRef = ref(null)
const selectedFile = ref(null)
const batchChecking = ref(false)
const batchResult = ref(null)
const showBatchDetailDialog = ref(false)
const selectedBatchItem = ref(null)
const detailTab = ref('user')

// 服务检查
const serviceChecking = ref(false)

// 模式切换
const handleModeChange = () => {
  // 清空结果
  singleResult.value = null
  batchResult.value = null
}

// 检查服务状态
const handleCheckService = async () => {
  serviceChecking.value = true
  try {
    const { data } = await getAllConfigs()
    const pythonConfig = data.find(c => c.configKey === 'python.service.url')
    
    if (pythonConfig && pythonConfig.configValue) {
      serviceStatus.type = 'success'
      serviceStatus.text = '服务正常'
      ElMessage.success('合规检测服务已配置')
    } else {
      serviceStatus.type = 'warning'
      serviceStatus.text = '未配置'
      ElMessage.warning('请先在系统配置中配置Python服务地址')
    }
  } catch (error) {
    serviceStatus.type = 'danger'
    serviceStatus.text = '服务异常'
    ElMessage.error('检查服务失败')
  } finally {
    serviceChecking.value = false
  }
}

// 单条检测
const handleSingleCheck = async () => {
  if (!singleForm.content.trim()) {
    ElMessage.warning('请输入检测内容')
    return
  }

  singleChecking.value = true
  singleResult.value = null

  try {
    const { data } = await checkSingleMessage({ content: singleForm.content })
    
    // 解析结果
    if (typeof data === 'string') {
      singleResult.value = JSON.parse(data)
    } else {
      singleResult.value = data
    }
    
    ElMessage.success('检测完成')
  } catch (error) {
    console.error('检测失败:', error)
    ElMessage.error(error.message || '检测失败，请检查服务状态')
  } finally {
    singleChecking.value = false
  }
}

// 清空单条测试
const handleClearSingle = () => {
  singleForm.content = ''
  singleResult.value = null
}

// 文件选择
const handleFileChange = (file) => {
  selectedFile.value = file.raw
  ElMessage.success(`已选择文件: ${file.name}`)
}

// 文件数量超限
const handleExceed = () => {
  ElMessage.warning('最多只能上传1个文件')
}

// 清空批量测试
const handleClearBatch = () => {
  selectedFile.value = null
  batchResult.value = null
  uploadRef.value?.clearFiles()
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

    batchChecking.value = true
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
    batchChecking.value = false
  }
}

// 显示批量检测详情
const showBatchDetail = (item) => {
  selectedBatchItem.value = item
  detailTab.value = 'user'
  showBatchDetailDialog.value = true
}

// 表格行点击
const handleRowClick = (row) => {
  showBatchDetail(row)
}

// 导出批量结果
const handleExportBatch = () => {
  if (!batchResult.value) return

  const headers = [
    '序号', '会话ID', '消息ID', '用户消息', 'AI响应',
    '用户检测结果', 'AI检测结果', '用户风险等级', 'AI风险等级',
    '用户风险类别', 'AI风险类别'
  ]
  
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
    '\uFEFF' + headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `compliance_result_${Date.now()}.csv`
  link.click()

  ElMessage.success('结果已导出')
}

// 复制JSON
const copyJson = (data) => {
  const text = JSON.stringify(data, null, 2)
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
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

// 获取风险等级文本
const getRiskLevelText = (level) => {
  const map = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险',
    'UNKNOWN': '未知'
  }
  return map[level] || level
}

// 初始化
onMounted(() => {
  handleCheckService()
})
</script>

<style scoped>
.compliance-check-container {
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

.mode-card {
  margin-bottom: 20px;
  text-align: center;
}

.test-card {
  margin-bottom: 20px;
}

.single-result {
  margin-top: 30px;
}

.result-summary {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 10px;
}

.upload-area {
  margin-bottom: 20px;
}

.file-info {
  margin: 20px 0;
}

.action-buttons {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.summary {
  margin-bottom: 20px;
}

.message-preview {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  margin-bottom: 8px;
}

.tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.risk-tag {
  margin-bottom: 4px;
}

.help-card {
  margin-bottom: 20px;
}

.json-viewer {
  margin-top: 20px;
}

.json-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: bold;
}

.json-content {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
  max-height: 400px;
}

.detail-section {
  padding: 10px 0;
}

.content-display {
  margin-bottom: 20px;
}

.content-display h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.content-text {
  margin: 0;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
