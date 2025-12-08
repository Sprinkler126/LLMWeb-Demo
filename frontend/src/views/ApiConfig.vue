<template>
  <div class="api-config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>API配置管理</span>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>
            添加配置
          </el-button>
        </div>
      </template>

      <el-table :data="configList" stripe>
        <el-table-column prop="configName" label="配置名称" />
        <el-table-column prop="provider" label="提供商" width="120" />
        <el-table-column prop="modelName" label="模型名称" />
        <el-table-column prop="apiType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.apiType === 'ONLINE' ? 'success' : 'info'">
              {{ row.apiType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="testApiConfig(row.id)">测试</el-button>
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteConfig(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="配置名称">
          <el-input v-model="form.configName" />
        </el-form-item>
        <el-form-item label="提供商">
          <el-select v-model="form.provider" placeholder="请选择">
            <el-option label="OpenAI" value="OpenAI" />
            <el-option label="Anthropic" value="Anthropic" />
            <el-option label="Google" value="Google" />
            <el-option label="DeepSeek" value="DeepSeek" />
            <el-option label="阿里云(通义千问)" value="Aliyun" />
            <el-option label="百度(文心一言)" value="Baidu" />
            <el-option label="本地模型" value="Local" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称">
          <el-autocomplete
            v-model="form.modelName"
            :fetch-suggestions="querySearchModels"
            placeholder="输入模型名称或从推荐中选择"
            style="width: 100%"
          >
            <template #default="{ item }">
              <div class="model-item">
                <span class="model-name">{{ item.value }}</span>
                <el-tag v-if="item.recommended" size="small" type="success">推荐</el-tag>
              </div>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item label="API端点">
          <el-input v-model="form.apiEndpoint" placeholder="会根据提供商自动填充">
            <template #append>
              <el-button @click="resetEndpoint">重置为默认</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="API密钥">
          <el-input 
            v-model="form.apiKey" 
            type="password" 
            :placeholder="apiKeyPlaceholder"
            show-password
          />
        </el-form-item>
        <el-form-item label="API类型">
          <el-radio-group v-model="form.apiType">
            <el-radio label="ONLINE">在线</el-radio>
            <el-radio label="LOCAL">本地</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="最大Token数">
          <el-input-number v-model="form.maxTokens" :min="100" :max="8000" />
        </el-form-item>
        <el-form-item label="温度">
          <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfigList, createConfig, updateConfig, deleteConfig as deleteConfigApi, testConfig } from '@/api/apiConfig'
import { useComponentLifecycle } from '@/composables/useComponentLifecycle'

// 使用生命周期管理
const { isUnmounted, safeAsync } = useComponentLifecycle()

// API 提供商配置信息
const providerConfigs = {
  OpenAI: {
    endpoint: 'https://api.openai.com/v1/chat/completions',
    models: ['gpt-4', 'gpt-4-turbo', 'gpt-3.5-turbo', 'gpt-4o', 'gpt-4o-mini'],
    placeholder: '输入您的 OpenAI API Key (sk-...)'
  },
  Anthropic: {
    endpoint: 'https://api.anthropic.com/v1/messages',
    models: ['claude-3-opus-20240229', 'claude-3-sonnet-20240229', 'claude-3-haiku-20240307', 'claude-3-5-sonnet-20241022'],
    placeholder: '输入您的 Anthropic API Key'
  },
  Google: {
    endpoint: 'https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent',
    models: ['gemini-pro', 'gemini-pro-vision', 'gemini-1.5-pro', 'gemini-1.5-flash'],
    placeholder: '输入您的 Google API Key'
  },
  DeepSeek: {
    endpoint: 'https://api.deepseek.com/v1/chat/completions',
    models: ['deepseek-chat', 'deepseek-coder', 'deepseek-reasoner'],
    placeholder: '输入您的 DeepSeek API Key (sk-...)'
  },
  Aliyun: {
    endpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
    models: ['qwen-turbo', 'qwen-plus', 'qwen-max', 'qwen-max-longcontext', 'qwen-flash'],
    placeholder: '输入您的阿里云 API Key (sk-...)'
  },
  Baidu: {
    endpoint: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions',
    models: ['ernie-bot-4', 'ernie-bot-turbo', 'ernie-bot', 'ernie-speed'],
    placeholder: '输入您的百度 API Key'
  },
  Local: {
    endpoint: 'http://localhost:11434/api/chat',
    models: ['llama2', 'mistral', 'codellama', 'qwen'],
    placeholder: '本地模型无需 API Key'
  }
}

const configList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加API配置')
const apiKeyPlaceholder = ref('输入您的 API Key')
const recommendedModels = ref([])
const form = ref({
  configName: '',
  provider: 'OpenAI',
  modelName: '',
  apiEndpoint: '',
  apiKey: '',
  apiType: 'ONLINE',
  maxTokens: 2000,
  temperature: 0.7,
  timeout: 30,
  status: 1
})

// 监听 provider 变化，自动填充端点和更新推荐模型
watch(() => form.value.provider, (newProvider) => {
  const config = providerConfigs[newProvider]
  if (config) {
    // 只在新建时自动填充，编辑时不覆盖
    if (!form.value.id) {
      form.value.apiEndpoint = config.endpoint
    }
    recommendedModels.value = config.models
    apiKeyPlaceholder.value = config.placeholder
  }
})

onMounted(() => {
  loadConfigList()
})

const loadConfigList = async () => {
  await safeAsync(async () => {
    const res = await getConfigList({ current: 1, size: 100 })
    configList.value = res.data.records
  })
}

const openDialog = (row = null) => {
  if (row) {
    dialogTitle.value = '编辑API配置'
    form.value = { ...row, temperature: Number(row.temperature) }
    // 编辑时也更新推荐模型列表
    const config = providerConfigs[row.provider]
    if (config) {
      recommendedModels.value = config.models
      apiKeyPlaceholder.value = config.placeholder
    }
  } else {
    dialogTitle.value = '添加API配置'
    form.value = {
      configName: '',
      provider: 'OpenAI',
      modelName: '',
      apiEndpoint: providerConfigs.OpenAI.endpoint,
      apiKey: '',
      apiType: 'ONLINE',
      maxTokens: 2000,
      temperature: 0.7,
      timeout: 30,
      status: 1
    }
    recommendedModels.value = providerConfigs.OpenAI.models
    apiKeyPlaceholder.value = providerConfigs.OpenAI.placeholder
  }
  dialogVisible.value = true
}

// 模型名称自动完成
const querySearchModels = (queryString, cb) => {
  const results = recommendedModels.value.map(model => ({
    value: model,
    recommended: true
  }))
  
  // 如果有输入，过滤结果
  const filtered = queryString
    ? results.filter(item => item.value.toLowerCase().includes(queryString.toLowerCase()))
    : results
  
  cb(filtered)
}

// 重置端点为默认值
const resetEndpoint = () => {
  const config = providerConfigs[form.value.provider]
  if (config) {
    form.value.apiEndpoint = config.endpoint
    ElMessage.success('已重置为默认端点')
  }
}

const saveConfig = async () => {
  await safeAsync(async () => {
    if (form.value.id) {
      await updateConfig(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createConfig(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadConfigList()
  })
}

const deleteConfig = (id) => {
  ElMessageBox.confirm('确定要删除这个API配置吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await safeAsync(async () => {
      await deleteConfigApi(id)
      ElMessage.success('删除成功')
      await loadConfigList()
    })
  }).catch(() => {
    // 用户取消，不做任何操作
  })
}

const testApiConfig = async (id) => {
  await safeAsync(async () => {
    try {
      await testConfig(id)
      ElMessage.success('API测试成功')
    } catch (error) {
      ElMessage.error('API测试失败')
    }
  })
}
</script>

<style scoped>
.api-config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.model-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.model-name {
  flex: 1;
}
</style>
