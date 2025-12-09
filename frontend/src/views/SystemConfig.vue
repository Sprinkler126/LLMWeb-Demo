<template>
  <div class="system-config-container">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span class="title">系统配置管理</span>
          <el-tag type="danger">超级管理员功能</el-tag>
        </div>
      </template>

      <!-- Python检测接口配置 -->
      <div class="config-section">
        <div class="section-header">
          <el-icon><Link /></el-icon>
          <span class="section-title">Python合规检测接口配置</span>
        </div>

        <el-form :model="pythonConfig" label-width="180px" class="config-form">
          <el-form-item label="接口地址">
            <el-input 
              v-model="pythonConfig.endpoint" 
              placeholder="例如: http://localhost:5000/check_compliance"
              clearable
            >
              <template #prepend>
                <el-icon><Link /></el-icon>
              </template>
            </el-input>
            <div class="form-help">Python合规检测服务的完整URL地址</div>
          </el-form-item>

          <el-form-item label="超时时间（毫秒）">
            <el-input-number 
              v-model="pythonConfig.timeout" 
              :min="1000"
              :max="60000"
              :step="1000"
              controls-position="right"
              style="width: 100%"
            />
            <div class="form-help">接口调用超时时间，建议设置为30000毫秒（30秒）</div>
          </el-form-item>

          <el-form-item label="是否启用">
            <el-switch 
              v-model="pythonConfig.enabled"
              active-text="启用"
              inactive-text="禁用"
            />
            <div class="form-help">关闭后将不会调用Python检测接口</div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSave" :loading="saving">
              <el-icon><Select /></el-icon>
              保存配置
            </el-button>
            <el-button @click="handleTestConnection" :loading="testing">
              <el-icon><Connection /></el-icon>
              测试连接
            </el-button>
            <el-button @click="loadConfigs">
              <el-icon><Refresh /></el-icon>
              重新加载
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 测试结果 -->
      <div v-if="testResult" class="test-result">
        <el-divider />
        <div class="section-header">
          <el-icon><Operation /></el-icon>
          <span class="section-title">连接测试结果</span>
        </div>

        <el-alert
          :title="testResult.success ? '连接成功' : '连接失败'"
          :type="testResult.success ? 'success' : 'error'"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="test-result-content">
              <p><strong>接口地址:</strong> {{ testResult.endpoint }}</p>
              <p v-if="testResult.success">
                <strong>响应时间:</strong> {{ testResult.responseTime }}ms
              </p>
              <p><strong>消息:</strong> {{ testResult.message }}</p>
              <p v-if="testResult.error">
                <strong>错误类型:</strong> {{ testResult.error }}
              </p>
              <el-collapse v-if="testResult.response">
                <el-collapse-item title="查看完整响应" name="1">
                  <pre>{{ JSON.stringify(testResult.response, null, 2) }}</pre>
                </el-collapse-item>
              </el-collapse>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 所有配置列表 -->
      <div class="config-list">
        <el-divider />
        <div class="section-header">
          <el-icon><List /></el-icon>
          <span class="section-title">所有系统配置</span>
          <el-button 
            type="primary" 
            size="small" 
            @click="handleBatchSave"
            :loading="batchSaving"
            style="margin-left: auto"
          >
            <el-icon><Select /></el-icon>
            批量保存修改
          </el-button>
        </div>

        <el-table :data="allConfigs" border stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="configKey" label="配置键" min-width="200" />
          <el-table-column label="配置值" min-width="300">
            <template #default="{ row }">
              <el-input 
                v-model="row.configValue" 
                placeholder="请输入配置值"
                clearable
                @change="markAsModified(row)"
              >
                <template #suffix>
                  <el-icon v-if="isModified(row)" color="#67c23a">
                    <CircleCheck />
                  </el-icon>
                </template>
              </el-input>
            </template>
          </el-table-column>
          <el-table-column label="描述" min-width="250">
            <template #default="{ row }">
              <el-input 
                v-model="row.configDesc" 
                placeholder="请输入描述"
                @change="markAsModified(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="configType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.configType)" size="small">
                {{ row.configType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="serviceGroup" label="服务分组" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.serviceGroup" type="info" size="small">
                {{ row.serviceGroup }}
              </el-tag>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleSaveSingle(row)"
                :disabled="!isModified(row)"
                :loading="row.saving"
              >
                保存
              </el-button>
              <el-button 
                size="small" 
                @click="handleResetSingle(row)"
                :disabled="!isModified(row)"
              >
                重置
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="modifiedConfigs.size > 0" class="modification-tip">
          <el-alert
            type="warning"
            :closable="false"
            show-icon
          >
            <template #title>
              当前有 {{ modifiedConfigs.size }} 项配置已修改但未保存
            </template>
          </el-alert>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Link, 
  Select, 
  Connection, 
  Refresh, 
  Operation, 
  List,
  CircleCheck
} from '@element-plus/icons-vue'
import { 
  getAllConfigs, 
  batchUpdateConfigs, 
  testPythonConnection 
} from '@/api/systemConfig'

const saving = ref(false)
const testing = ref(false)
const batchSaving = ref(false)
const testResult = ref(null)

const pythonConfig = reactive({
  endpoint: '',
  timeout: 30000,
  enabled: true
})

const allConfigs = ref([])
const configIdMap = reactive({})
const originalConfigs = reactive({}) // 存储原始配置值
const modifiedConfigs = reactive(new Set()) // 记录修改过的配置ID

// 加载配置
const loadConfigs = async () => {
  try {
    const { data } = await getAllConfigs()
    allConfigs.value = (data || []).map(config => ({
      ...config,
      saving: false // 添加单行保存状态
    }))
    
    // 存储原始配置值
    data.forEach(config => {
      configIdMap[config.configKey] = config.id
      originalConfigs[config.id] = {
        configValue: config.configValue,
        configDesc: config.configDesc
      }
      
      if (config.configKey === 'python.compliance.endpoint') {
        pythonConfig.endpoint = config.configValue
      } else if (config.configKey === 'python.compliance.timeout') {
        pythonConfig.timeout = parseInt(config.configValue)
      } else if (config.configKey === 'python.compliance.enabled') {
        pythonConfig.enabled = config.configValue === 'true'
      }
    })
    
    // 清空修改记录
    modifiedConfigs.clear()
    
    ElMessage.success('配置加载成功')
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error(error.response?.data?.message || '加载配置失败')
  }
}

// 标记配置为已修改
const markAsModified = (row) => {
  const original = originalConfigs[row.id]
  if (!original) return
  
  // 检查是否真的修改了
  if (row.configValue !== original.configValue || row.configDesc !== original.configDesc) {
    modifiedConfigs.add(row.id)
  } else {
    modifiedConfigs.delete(row.id)
  }
}

// 检查配置是否被修改
const isModified = (row) => {
  return modifiedConfigs.has(row.id)
}

// 获取类型标签颜色
const getTypeTagType = (type) => {
  const map = {
    'STRING': '',
    'NUMBER': 'success',
    'BOOLEAN': 'warning',
    'JSON': 'danger'
  }
  return map[type] || 'info'
}

// 保存单个配置
const handleSaveSingle = async (row) => {
  if (!isModified(row)) {
    ElMessage.warning('配置未修改')
    return
  }
  
  row.saving = true
  try {
    await batchUpdateConfigs([{
      id: row.id,
      configKey: row.configKey,
      configValue: row.configValue,
      configDesc: row.configDesc
    }])
    
    // 更新原始值
    originalConfigs[row.id] = {
      configValue: row.configValue,
      configDesc: row.configDesc
    }
    
    // 移除修改标记
    modifiedConfigs.delete(row.id)
    
    ElMessage.success('配置保存成功')
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error(error.response?.data?.message || '保存配置失败')
  } finally {
    row.saving = false
  }
}

// 重置单个配置
const handleResetSingle = (row) => {
  const original = originalConfigs[row.id]
  if (!original) return
  
  row.configValue = original.configValue
  row.configDesc = original.configDesc
  modifiedConfigs.delete(row.id)
  
  ElMessage.info('已重置为原始值')
}

// 批量保存所有修改
const handleBatchSave = async () => {
  if (modifiedConfigs.size === 0) {
    ElMessage.warning('没有需要保存的修改')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认保存 ${modifiedConfigs.size} 项配置修改吗？`,
      '批量保存确认',
      {
        confirmButtonText: '确认保存',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    batchSaving.value = true
    
    // 收集所有修改的配置
    const modifiedConfigList = allConfigs.value
      .filter(config => modifiedConfigs.has(config.id))
      .map(config => ({
        id: config.id,
        configKey: config.configKey,
        configValue: config.configValue,
        configDesc: config.configDesc
      }))
    
    await batchUpdateConfigs(modifiedConfigList)
    
    // 更新原始值
    modifiedConfigList.forEach(config => {
      originalConfigs[config.id] = {
        configValue: config.configValue,
        configDesc: config.configDesc
      }
    })
    
    // 清空修改记录
    modifiedConfigs.clear()
    
    ElMessage.success(`成功保存 ${modifiedConfigList.length} 项配置`)
    
    // 重新加载配置
    await loadConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量保存失败:', error)
      ElMessage.error(error.response?.data?.message || '批量保存失败')
    }
  } finally {
    batchSaving.value = false
  }
}

// 保存配置
const handleSave = async () => {
  if (!pythonConfig.endpoint) {
    ElMessage.warning('请输入接口地址')
    return
  }

  saving.value = true
  try {
    const configs = [
      {
        id: configIdMap['python.compliance.endpoint'],
        configKey: 'python.compliance.endpoint',
        configValue: pythonConfig.endpoint
      },
      {
        id: configIdMap['python.compliance.timeout'],
        configKey: 'python.compliance.timeout',
        configValue: pythonConfig.timeout.toString()
      },
      {
        id: configIdMap['python.compliance.enabled'],
        configKey: 'python.compliance.enabled',
        configValue: pythonConfig.enabled.toString()
      }
    ]
    
    await batchUpdateConfigs(configs)
    ElMessage.success('配置保存成功')
    
    // 重新加载配置
    await loadConfigs()
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error(error.response?.data?.message || '保存配置失败')
  } finally {
    saving.value = false
  }
}

// 测试连接
const handleTestConnection = async () => {
  testing.value = true
  testResult.value = null
  
  try {
    const { data } = await testPythonConnection()
    testResult.value = data
    
    if (data.success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error('连接测试失败')
    }
  } catch (error) {
    console.error('测试连接失败:', error)
    ElMessage.error(error.response?.data?.message || '测试连接失败')
    testResult.value = {
      success: false,
      message: error.response?.data?.message || error.message,
      endpoint: pythonConfig.endpoint
    }
  } finally {
    testing.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.system-config-container {
  padding: 20px;
}

.config-card {
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

.config-section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 10px;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.config-form {
  max-width: 800px;
}

.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.test-result {
  margin-top: 20px;
}

.test-result-content {
  line-height: 1.8;
}

.test-result-content p {
  margin: 8px 0;
}

.test-result-content pre {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
}

.config-list {
  margin-top: 30px;
}

.modification-tip {
  margin-top: 20px;
}

:deep(.el-input__suffix) {
  display: flex;
  align-items: center;
}
</style>
