<template>
  <div class="compliance-container">
    <el-card>
      <template #header>
        <div class="header">
          <span>合规检测测试</span>
          <el-tag :type="serviceStatus.type">{{ serviceStatus.text }}</el-tag>
        </div>
      </template>

      <!-- 服务状态检测 -->
      <el-alert
        v-if="!serviceConfigured"
        title="服务未配置"
        type="warning"
        description="合规检测功能需要先在系统配置中配置Python服务地址"
        show-icon
        :closable="false"
      />

      <!-- 测试表单 -->
      <div v-else class="test-section">
        <el-form :model="testForm" label-width="120px">
          <el-form-item label="测试内容">
            <el-input
              v-model="testForm.content"
              type="textarea"
              :rows="6"
              placeholder="请输入要检测的文本内容..."
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleTest" 
              :loading="testing"
              :disabled="!testForm.content"
            >
              <el-icon><Select /></el-icon>
              开始检测
            </el-button>
            <el-button @click="handleCheckService" :loading="checking">
              <el-icon><Connection /></el-icon>
              检查服务状态
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 检测结果 -->
        <div v-if="testResult" class="result-section">
          <el-divider />
          <div class="result-header">
            <el-icon><Document /></el-icon>
            <span>检测结果</span>
          </div>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="检测结果">
              <el-tag :type="testResult.result === 'PASS' ? 'success' : 'danger'">
                {{ testResult.result === 'PASS' ? '通过' : '未通过' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag 
                :type="getRiskLevelType(testResult.risk_level)"
              >
                {{ getRiskLevelText(testResult.risk_level) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="置信度">
              {{ (testResult.confidence_score * 100).toFixed(2) }}%
            </el-descriptions-item>
            <el-descriptions-item label="风险类别">
              {{ testResult.risk_categories || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="详细说明" :span="2">
              {{ testResult.detail }}
            </el-descriptions-item>
          </el-descriptions>

          <el-collapse style="margin-top: 20px">
            <el-collapse-item title="查看完整响应" name="1">
              <pre>{{ JSON.stringify(testResult, null, 2) }}</pre>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>

      <!-- 服务信息 -->
      <div class="service-info">
        <el-divider />
        <el-descriptions title="服务信息" :column="1" border>
          <el-descriptions-item label="服务地址">
            {{ serviceConfig.endpoint || '未配置' }}
          </el-descriptions-item>
          <el-descriptions-item label="超时时间">
            {{ serviceConfig.timeout }}ms
          </el-descriptions-item>
          <el-descriptions-item label="服务状态">
            {{ serviceConfig.enabled ? '已启用' : '已禁用' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Select, Connection, Document } from '@element-plus/icons-vue'
import { getConfigByKey } from '@/api/systemConfig'
import { checkSingleMessage } from '@/api/compliance'

const testing = ref(false)
const checking = ref(false)
const serviceConfigured = ref(false)
const testResult = ref(null)

const testForm = reactive({
  content: ''
})

const serviceConfig = reactive({
  endpoint: '',
  timeout: 30000,
  enabled: false
})

const serviceStatus = computed(() => {
  if (!serviceConfigured.value) {
    return { type: 'info', text: '未配置' }
  }
  if (!serviceConfig.enabled) {
    return { type: 'warning', text: '已禁用' }
  }
  return { type: 'success', text: '已启用' }
})

// 检查服务配置
const checkServiceConfig = async () => {
  try {
    const { data: endpointConfig } = await getConfigByKey('python.compliance.endpoint')
    const { data: timeoutConfig } = await getConfigByKey('python.compliance.timeout')
    const { data: enabledConfig } = await getConfigByKey('python.compliance.enabled')
    
    if (endpointConfig && endpointConfig.configValue) {
      serviceConfig.endpoint = endpointConfig.configValue
      serviceConfig.timeout = parseInt(timeoutConfig?.configValue || '30000')
      serviceConfig.enabled = enabledConfig?.configValue === 'true'
      serviceConfigured.value = true
    }
  } catch (error) {
    console.error('获取服务配置失败:', error)
    serviceConfigured.value = false
  }
}

// 检查服务状态
const handleCheckService = async () => {
  checking.value = true
  try {
    await checkServiceConfig()
    if (serviceConfigured.value) {
      ElMessage.success('服务配置已加载')
    } else {
      ElMessage.warning('服务未配置，请先在系统配置中配置Python服务')
    }
  } catch (error) {
    ElMessage.error('检查服务状态失败')
  } finally {
    checking.value = false
  }
}

// 测试检测
const handleTest = async () => {
  if (!testForm.content.trim()) {
    ElMessage.warning('请输入测试内容')
    return
  }

  testing.value = true
  testResult.value = null

  try {
    const { data } = await checkSingleMessage({ content: testForm.content })
    
    // data 是字符串形式的JSON，需要解析
    if (typeof data === 'string') {
      testResult.value = JSON.parse(data)
    } else {
      testResult.value = data
    }
    
    ElMessage.success('检测完成')
  } catch (error) {
    console.error('检测失败:', error)
    ElMessage.error(error.response?.data?.message || '检测失败')
  } finally {
    testing.value = false
  }
}

// 获取风险等级类型
const getRiskLevelType = (level) => {
  const map = {
    'LOW': 'success',
    'MEDIUM': 'warning',
    'HIGH': 'danger'
  }
  return map[level] || 'info'
}

// 获取风险等级文本
const getRiskLevelText = (level) => {
  const map = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险'
  }
  return map[level] || level
}

onMounted(() => {
  checkServiceConfig()
})
</script>

<style scoped>
.compliance-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.test-section {
  margin-top: 20px;
}

.result-section {
  margin-top: 20px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
}

.result-section pre {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.5;
}

.service-info {
  margin-top: 20px;
}
</style>
