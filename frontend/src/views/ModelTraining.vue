<template>
  <div class="model-training-container">
    <el-card class="header-card">
      <div class="header">
        <h2>🤖 模型训练管理</h2>
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建训练任务
        </el-button>
      </div>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="tasks-card">
      <template #header>
        <div class="card-header">
          <span>训练任务列表</span>
          <el-button text @click="loadTasks">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" width="180" />
        <el-table-column prop="modelType" label="模型类型" width="150">
          <template #default="{ row }">
            <el-tag>{{ getModelTypeLabel(row.modelType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.taskStatus)">
              {{ getStatusLabel(row.taskStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="180">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress 
                :percentage="row.progress || 0" 
                :status="getProgressStatus(row.taskStatus)"
              />
              <span v-if="row.currentEpoch && row.totalEpochs" class="epoch-info">
                {{ row.currentEpoch }}/{{ row.totalEpochs }} 轮
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="训练指标" width="200">
          <template #default="{ row }">
            <div v-if="row.trainAccuracy" class="metrics">
              <div>训练准确率: {{ (row.trainAccuracy * 100).toFixed(2) }}%</div>
              <div v-if="row.valAccuracy">验证准确率: {{ (row.valAccuracy * 100).toFixed(2) }}%</div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.taskStatus === 'PENDING' || row.taskStatus === 'FAILED'"
              size="small" 
              type="success" 
              @click="startTask(row.id)"
              :loading="row.starting"
            >
              启动
            </el-button>
            <el-button 
              v-if="row.taskStatus === 'RUNNING'"
              size="small" 
              type="warning" 
              @click="stopTask(row.id)"
              :loading="row.stopping"
            >
              停止
            </el-button>
            <el-button 
              size="small" 
              @click="viewDetail(row)"
            >
              详情
            </el-button>
            <el-button 
              size="small" 
              @click="viewLog(row.id)"
            >
              日志
            </el-button>
            <el-button 
              v-if="row.taskStatus !== 'RUNNING'"
              size="small" 
              type="danger" 
              @click="deleteTask(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建任务对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建训练任务"
      width="600px"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="createForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="模型类型" prop="modelType">
          <el-select v-model="createForm.modelType" placeholder="请选择模型类型" style="width: 100%">
            <el-option
              v-for="type in modelTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="训练轮次" prop="epochs">
          <el-input-number v-model="createForm.epochs" :min="1" :max="100" :step="1" />
          <span class="form-tip">建议：10-50轮</span>
        </el-form-item>
        <el-form-item label="批次大小" prop="batchSize">
          <el-input-number v-model="createForm.batchSize" :min="8" :max="128" :step="8" />
          <span class="form-tip">建议：16-64</span>
        </el-form-item>
        <el-form-item label="学习率" prop="learningRate">
          <el-input-number 
            v-model="createForm.learningRate" 
            :min="0.0001" 
            :max="0.1" 
            :step="0.0001" 
            :precision="4"
          />
          <span class="form-tip">建议：0.001-0.01</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="任务详情"
      width="800px"
    >
      <el-descriptions v-if="currentTask" :column="2" border>
        <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
        <el-descriptions-item label="模型类型">{{ getModelTypeLabel(currentTask.modelType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTask.taskStatus)">
            {{ getStatusLabel(currentTask.taskStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="进度">{{ currentTask.progress }}%</el-descriptions-item>
        <el-descriptions-item label="轮次">{{ currentTask.currentEpoch }}/{{ currentTask.totalEpochs }}</el-descriptions-item>
        <el-descriptions-item label="训练损失">{{ currentTask.trainLoss || '-' }}</el-descriptions-item>
        <el-descriptions-item label="训练准确率">{{ currentTask.trainAccuracy ? (currentTask.trainAccuracy * 100).toFixed(2) + '%' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="验证损失">{{ currentTask.valLoss || '-' }}</el-descriptions-item>
        <el-descriptions-item label="验证准确率">{{ currentTask.valAccuracy ? (currentTask.valAccuracy * 100).toFixed(2) + '%' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(currentTask.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(currentTask.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="训练时长" :span="2">{{ formatDuration(currentTask.durationSeconds) }}</el-descriptions-item>
        <el-descriptions-item label="模型保存路径" :span="2">{{ currentTask.modelSavePath || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentTask.errorMessage" label="错误信息" :span="2">
          <el-alert type="error" :title="currentTask.errorMessage" :closable="false" />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 日志对话框 -->
    <el-dialog
      v-model="showLogDialog"
      title="训练日志"
      width="800px"
    >
      <el-scrollbar height="400px">
        <pre class="log-content">{{ currentLog || '暂无日志' }}</pre>
      </el-scrollbar>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  getModelTypes,
  createTrainingTask,
  startTrainingTask,
  stopTrainingTask,
  getUserTasks,
  deleteTask as deleteTaskApi,
  getTaskDetail,
  getTrainingLog
} from '@/api/modelTraining'

// 状态
const loading = ref(false)
const creating = ref(false)
const tasks = ref([])
const modelTypes = ref([])
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showLogDialog = ref(false)
const currentTask = ref(null)
const currentLog = ref('')

// 创建表单
const createFormRef = ref(null)
const createForm = ref({
  taskName: '',
  modelType: '',
  epochs: 10,
  batchSize: 32,
  learningRate: 0.001
})

const createRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }],
  epochs: [{ required: true, message: '请输入训练轮次', trigger: 'blur' }],
  batchSize: [{ required: true, message: '请输入批次大小', trigger: 'blur' }],
  learningRate: [{ required: true, message: '请输入学习率', trigger: 'blur' }]
}

// 自动刷新定时器
let refreshTimer = null

// 加载模型类型
const loadModelTypes = async () => {
  try {
    const res = await getModelTypes()
    if (res.code === 200) {
      modelTypes.value = res.data
    }
  } catch (error) {
    console.error('加载模型类型失败', error)
  }
}

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    const res = await getUserTasks()
    if (res.code === 200) {
      tasks.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

// 创建任务
const handleCreate = async () => {
  const formRef = createFormRef.value
  if (!formRef) return

  await formRef.validate(async (valid) => {
    if (!valid) return

    creating.value = true
    try {
      const res = await createTrainingTask(createForm.value)
      if (res.code === 200) {
        ElMessage.success('任务创建成功')
        showCreateDialog.value = false
        createForm.value = {
          taskName: '',
          modelType: '',
          epochs: 10,
          batchSize: 32,
          learningRate: 0.001
        }
        formRef.resetFields()
        loadTasks()
      }
    } catch (error) {
      ElMessage.error(error.message || '创建任务失败')
    } finally {
      creating.value = false
    }
  })
}

// 启动任务
const startTask = async (taskId) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (task) task.starting = true
  
  try {
    const res = await startTrainingTask(taskId)
    if (res.code === 200) {
      ElMessage.success('任务已启动')
      loadTasks()
    }
  } catch (error) {
    ElMessage.error(error.message || '启动任务失败')
  } finally {
    if (task) task.starting = false
  }
}

// 停止任务
const stopTask = async (taskId) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (task) task.stopping = true
  
  try {
    const res = await stopTrainingTask(taskId)
    if (res.code === 200) {
      ElMessage.success('任务已停止')
      loadTasks()
    }
  } catch (error) {
    ElMessage.error(error.message || '停止任务失败')
  } finally {
    if (task) task.stopping = false
  }
}

// 查看详情
const viewDetail = async (task) => {
  try {
    const res = await getTaskDetail(task.id)
    if (res.code === 200) {
      currentTask.value = res.data
      showDetailDialog.value = true
    }
  } catch (error) {
    ElMessage.error('获取任务详情失败')
  }
}

// 查看日志
const viewLog = async (taskId) => {
  try {
    const res = await getTrainingLog(taskId)
    if (res.code === 200) {
      currentLog.value = res.data.log
      showLogDialog.value = true
    }
  } catch (error) {
    ElMessage.error('获取训练日志失败')
  }
}

// 删除任务
const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除任务"${task.taskName}"吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteTaskApi(task.id)
    if (res.code === 200) {
      ElMessage.success('任务已删除')
      loadTasks()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除任务失败')
    }
  }
}

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    'PENDING': '等待中',
    'RUNNING': '训练中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'STOPPED': '已停止'
  }
  return statusMap[status] || status
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'RUNNING': 'warning',
    'COMPLETED': 'success',
    'FAILED': 'danger',
    'STOPPED': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取进度状态
const getProgressStatus = (status) => {
  if (status === 'COMPLETED') return 'success'
  if (status === 'FAILED') return 'exception'
  return undefined
}

// 获取模型类型标签
const getModelTypeLabel = (modelType) => {
  const type = modelTypes.value.find(t => t.value === modelType)
  return type ? type.label : modelType
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '-'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (hours > 0) {
    return `${hours}小时${minutes}分${secs}秒`
  } else if (minutes > 0) {
    return `${minutes}分${secs}秒`
  } else {
    return `${secs}秒`
  }
}

// 启动自动刷新
const startAutoRefresh = () => {
  // 每5秒刷新一次任务列表
  refreshTimer = setInterval(() => {
    // 只在有运行中的任务时刷新
    const hasRunningTask = tasks.value.some(t => t.taskStatus === 'RUNNING')
    if (hasRunningTask) {
      loadTasks()
    }
  }, 5000)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 初始化
onMounted(() => {
  loadModelTypes()
  loadTasks()
  startAutoRefresh()
})

// 清理
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.model-training-container {
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

.tasks-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.epoch-info {
  font-size: 12px;
  color: #909399;
}

.metrics {
  font-size: 12px;
}

.metrics div {
  margin: 2px 0;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.log-content {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
}
</style>
